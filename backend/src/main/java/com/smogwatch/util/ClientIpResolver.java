package com.smogwatch.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the original <strong>client public</strong> IP for HTTP requests behind reverse proxies / ingress.
 * <p>
 * Only addresses that are suitable for Geo-IP (globally routable, not RFC1918 / CGNAT / ULA / loopback)
 * are returned. This avoids calling third-party IP APIs with the pod network or the server's egress IP
 * by mistake.
 * <p>
 * Header precedence follows common CDN / ingress conventions. When
 * {@code server.forward-headers-strategy=framework} is enabled, {@link HttpServletRequest#getRemoteAddr()}
 * may already reflect the client after Spring unwraps forwarding headers.
 * <p>
 * Security: clients can spoof {@code X-Forwarded-For} if they reach the app directly. Deploy behind a
 * trusted ingress that strips or overwrites untrusted forwarding headers.
 */
public final class ClientIpResolver {

    private static final Logger log = LoggerFactory.getLogger(ClientIpResolver.class);

    private static final Pattern FORWARDED_FOR = Pattern.compile(
            "(?:^|[;,]\\s*)for\\s*=\\s*(?:\"\\[([^\"\\]]+)\\]\"|\\[([^\\]]+)\\]|\"([^\"]+)\"|([^\\s;,]+))",
            Pattern.CASE_INSENSITIVE);

    private ClientIpResolver() {}

    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        List<String> candidates = new ArrayList<>();
        addHeader(request, "True-Client-IP", candidates);
        addHeader(request, "CF-Connecting-IP", candidates);
        addForwardedForClauses(request.getHeader("Forwarded"), candidates);
        addHeader(request, "X-Real-IP", candidates);
        addXForwardedFor(request.getHeader("X-Forwarded-For"), candidates);
        candidates.add(request.getRemoteAddr());

        for (String raw : candidates) {
            String normalized = normalizeToGeoIpClient(raw);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private static void addHeader(HttpServletRequest request, String name, List<String> out) {
        String v = request.getHeader(name);
        if (v != null && !v.isBlank()) {
            out.add(v.trim());
        }
    }

    private static void addForwardedForClauses(String forwardedHeader, List<String> out) {
        if (forwardedHeader == null || forwardedHeader.isBlank()) {
            return;
        }
        for (String part : forwardedHeader.split(",")) {
            Matcher m = FORWARDED_FOR.matcher(part.trim());
            if (m.find()) {
                for (int g = 1; g <= m.groupCount(); g++) {
                    String cap = m.group(g);
                    if (cap != null && !cap.isBlank()) {
                        out.add(cap.trim());
                        break;
                    }
                }
            }
        }
    }

    private static void addXForwardedFor(String xff, List<String> out) {
        if (xff == null || xff.isBlank()) {
            return;
        }
        for (String token : xff.split(",")) {
            String t = token.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
    }

    /**
     * Parses {@code raw} into a canonical IP string suitable for Geo-IP, or {@code null}.
     */
    public static String normalizeToGeoIpClient(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty() || "unknown".equalsIgnoreCase(s)) {
            return null;
        }
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).trim();
        }
        int slash = s.indexOf('/');
        if (slash > 0) {
            s = s.substring(0, slash).trim();
        }
        int zone = s.indexOf('%');
        if (zone > 0) {
            s = s.substring(0, zone);
        }
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
        }
        if (s.isEmpty()) {
            return null;
        }
        try {
            InetAddress addr = InetAddress.getByName(s);
            if (!isSuitableForGeoIp(addr)) {
                return null;
            }
            return addr.getHostAddress().toLowerCase(Locale.ROOT);
        } catch (Exception ex) {
            log.trace("Ignoring invalid client IP token: {}", raw, ex);
            return null;
        }
    }

    /**
     * Rejects addresses that should not be sent to a public Geo-IP provider as "the end user".
     */
    static boolean isSuitableForGeoIp(InetAddress addr) {
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
            return false;
        }
        byte[] b = addr.getAddress();
        if (b.length == 4) {
            int a0 = b[0] & 0xff;
            int a1 = b[1] & 0xff;
            if (a0 == 10) {
                return false;
            }
            if (a0 == 172 && a1 >= 16 && a1 <= 31) {
                return false;
            }
            if (a0 == 192 && a1 == 168) {
                return false;
            }
            // RFC 6598 Carrier-grade NAT
            if (a0 == 100 && a1 >= 64 && a1 <= 127) {
                return false;
            }
            return true;
        }
        if (b.length == 16) {
            if (isIpv4MappedIpv6(b)) {
                byte[] v4 = new byte[] { b[12], b[13], b[14], b[15] };
                try {
                    return isSuitableForGeoIp(InetAddress.getByAddress(null, v4));
                } catch (Exception e) {
                    return false;
                }
            }
            // Unique local IPv6 (fc00::/7)
            if ((b[0] & 0xfe) == 0xfc) {
                return false;
            }
            // Link-local fe80::/10
            if (b[0] == (byte) 0xfe && (b[1] & 0xc0) == 0x80) {
                return false;
            }
            // Deprecated site-local fec0::/10
            if (b[0] == (byte) 0xfe && (b[1] & 0xc0) == 0xc0) {
                return false;
            }
            return true;
        }
        return false;
    }

    private static boolean isIpv4MappedIpv6(byte[] b) {
        for (int i = 0; i < 10; i++) {
            if (b[i] != 0) {
                return false;
            }
        }
        return b[10] == (byte) 0xff && b[11] == (byte) 0xff;
    }
}
