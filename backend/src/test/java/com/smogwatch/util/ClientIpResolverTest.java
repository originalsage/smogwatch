package com.smogwatch.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientIpResolverTest {

    @Test
    void xForwardedFor_leftmostPublicWins() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        req.setRemoteAddr("10.0.0.2");
        assertEquals("203.0.113.10", ClientIpResolver.resolve(req));
    }

    @Test
    void xRealIpUsedWhenXffOnlyPrivate() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        req.addHeader("X-Real-IP", "198.51.100.22");
        req.setRemoteAddr("10.0.0.3");
        assertEquals("198.51.100.22", ClientIpResolver.resolve(req));
    }

    @Test
    void trueClientIpPrecedenceOverXff() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("True-Client-IP", "198.51.100.1");
        req.addHeader("X-Forwarded-For", "203.0.113.9");
        req.setRemoteAddr("127.0.0.1");
        assertEquals("198.51.100.1", ClientIpResolver.resolve(req));
    }

    @Test
    void forwardedHeaderParsed() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Forwarded", "for=192.0.2.88;proto=https");
        req.setRemoteAddr("10.0.0.1");
        assertEquals("192.0.2.88", ClientIpResolver.resolve(req));
    }

    @Test
    void remoteAddrPublicWhenNoHeaders() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("203.0.113.55");
        assertEquals("203.0.113.55", ClientIpResolver.resolve(req));
    }

    @Test
    void allPrivateYieldsNull() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "10.1.1.1, 192.168.1.1");
        req.setRemoteAddr("172.16.0.5");
        assertNull(ClientIpResolver.resolve(req));
    }

    @Test
    void loopbackRejected() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("127.0.0.1");
        assertNull(ClientIpResolver.resolve(req));
    }

    @Test
    void suitableForGeoIp_flags() throws Exception {
        assertTrue(ClientIpResolver.isSuitableForGeoIp(InetAddress.getByName("8.8.8.8")));
        assertFalse(ClientIpResolver.isSuitableForGeoIp(InetAddress.getByName("10.1.2.3")));
        assertFalse(ClientIpResolver.isSuitableForGeoIp(InetAddress.getByName("192.168.0.1")));
        assertFalse(ClientIpResolver.isSuitableForGeoIp(InetAddress.getByName("100.64.0.1")));
        assertFalse(ClientIpResolver.isSuitableForGeoIp(InetAddress.getByName("127.0.0.1")));
        assertFalse(ClientIpResolver.isSuitableForGeoIp(InetAddress.getByName("fe80::1")));
    }
}
