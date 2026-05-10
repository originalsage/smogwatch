<template>
  <view class="page">
    <view class="header">
      <text class="title">天气详情</text>
      <text class="sub">{{ cityLabel || '—' }}</text>
      <text class="date">{{ subtitle }}</text>
    </view>

    <view v-if="errorMsg" class="banner-err">
      <text>{{ errorMsg }}</text>
    </view>

    <view v-if="points.length" class="card chart-card">
      <view class="chart-head">
        <text class="card-title">温度 · 湿度趋势</text>
        <view class="legend">
          <view class="leg-item">
            <view class="line-mark t" />
            <text class="leg-text">温度 ℃</text>
          </view>
          <view class="leg-item">
            <view class="line-mark h" />
            <text class="leg-text">湿度 %</text>
          </view>
        </view>
      </view>

      <text class="hint">
        {{ sourceFromForecast ? '数据来源：百度未来 24 小时逐小时预报' : '数据来源：当日服务端已记录的实时快照' }}
      </text>

      <scroll-view scroll-x class="chart-scroll" :show-scrollbar="false">
        <view class="chart-canvas" :style="{ width: chartWidth + 'rpx', height: chartHeight + 'rpx' }">
          <!-- 网格线 -->
          <view
            v-for="g in gridLines"
            :key="'g' + g"
            class="grid-line"
            :style="{ top: g + 'rpx' }"
          />

          <!-- 温度折线（带阴影） -->
          <view class="poly poly-temp">
            <view
              v-for="(seg, i) in tempSegments"
              :key="'tseg' + i"
              class="seg seg-temp"
              :style="seg"
            />
            <view
              v-for="(d, i) in tempDots"
              :key="'tdot' + i"
              class="dot dot-temp"
              :style="d"
            />
          </view>

          <!-- 湿度折线 -->
          <view class="poly poly-hum">
            <view
              v-for="(seg, i) in humSegments"
              :key="'hseg' + i"
              class="seg seg-hum"
              :style="seg"
            />
            <view
              v-for="(d, i) in humDots"
              :key="'hdot' + i"
              class="dot dot-hum"
              :style="d"
            />
          </view>

          <!-- 横轴时间 -->
          <view class="x-axis" :style="{ width: chartWidth + 'rpx' }">
            <view
              v-for="(p, i) in points"
              :key="'tk' + i"
              class="x-tick"
              :style="{ left: tickX(i) + 'rpx' }"
            >
              <text class="x-tick-text">{{ p.shortTime }}</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <view class="legend-axis">
        <text class="axis-text">温度区间 {{ minTemp }}℃ ~ {{ maxTemp }}℃</text>
        <text class="axis-text">湿度区间 {{ minHum }}% ~ {{ maxHum }}%</text>
      </view>
    </view>

    <view v-else-if="!loading" class="empty">
      <text>{{ emptyText }}</text>
    </view>

    <view v-if="points.length" class="card">
      <text class="card-title">逐小时明细</text>
      <view v-for="(p, i) in points" :key="'d' + i" class="row">
        <text class="time">{{ p.dataTime || p.observedAt || p.shortTime }}</text>
        <text class="vals">
          {{ p.temp != null ? p.temp + '℃' : '—' }} ·
          湿度 {{ p.humidity != null ? p.humidity + '%' : '—' }}
          <text v-if="p.weatherText" class="vals-tip"> · {{ p.weatherText }}</text>
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchHourlyForecast, fetchWeatherHistory } from '../../api/smogwatch.js'

const cityLabel = ref('')
const queryCity = ref('')
const queryDistrictId = ref('')
const dateStr = ref('')
const points = ref([])
const errorMsg = ref('')
const loading = ref(true)
const sourceFromForecast = ref(false)

const COL_RPX = 100 // 每个采样点列宽（rpx）
const CHART_INNER_HEIGHT = 360 // 折线绘制高度（rpx）
const CHART_PADDING_TOP = 40
const CHART_PADDING_BOTTOM = 60

const chartHeight = CHART_INNER_HEIGHT + CHART_PADDING_TOP + CHART_PADDING_BOTTOM
const chartWidth = computed(() => Math.max(640, points.value.length * COL_RPX + 80))

const subtitle = computed(() => {
  if (sourceFromForecast.value) return '未来 24 小时预报'
  return dateStr.value || ''
})

const emptyText = computed(() => {
  if (errorMsg.value) return ''
  return '暂无可绘制的曲线数据，请稍后再试或检查网络。'
})

function pad(n) {
  return n < 10 ? `0${n}` : String(n)
}

function today() {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function shortTime(s) {
  if (!s) return ''
  const m = String(s).match(/(\d{1,2}):(\d{2})/)
  if (m) return `${m[1]}:${m[2]}`
  return String(s).slice(-8)
}

const validTemps = computed(() => points.value.map((p) => p.temp).filter((t) => t != null))
const validHums = computed(() => points.value.map((p) => p.humidity).filter((h) => h != null))

const minTemp = computed(() => (validTemps.value.length ? Math.min(...validTemps.value) : 0))
const maxTemp = computed(() => (validTemps.value.length ? Math.max(...validTemps.value) : 1))
const minHum = computed(() => (validHums.value.length ? Math.min(...validHums.value) : 0))
const maxHum = computed(() => (validHums.value.length ? Math.max(...validHums.value) : 100))

function tickX(i) {
  return 40 + i * COL_RPX
}

/** 把 value 映射到画布 y 坐标（rpx，自顶向下） */
function mapY(value, minV, maxV) {
  if (value == null) return null
  const span = maxV - minV || 1
  const ratio = (value - minV) / span
  // 画布上方留空，反向映射
  return CHART_PADDING_TOP + (1 - ratio) * CHART_INNER_HEIGHT
}

/**
 * 把相邻两点用一个有角度的细条连起来。
 * 使用 transform:rotate 实现纯 CSS 折线段，跨端通用（H5/小程序均可）。
 */
function buildSegments(getY) {
  const segs = []
  if (points.value.length < 2) return segs
  for (let i = 0; i < points.value.length - 1; i++) {
    const y1 = getY(i)
    const y2 = getY(i + 1)
    if (y1 == null || y2 == null) continue
    const x1 = tickX(i)
    const x2 = tickX(i + 1)
    const dx = x2 - x1
    const dy = y2 - y1
    const length = Math.sqrt(dx * dx + dy * dy)
    const angleDeg = (Math.atan2(dy, dx) * 180) / Math.PI
    segs.push({
      left: x1 + 'rpx',
      top: y1 + 'rpx',
      width: length + 'rpx',
      transform: `rotate(${angleDeg}deg)`,
      transformOrigin: '0 50%',
    })
  }
  return segs
}

function buildDots(getY) {
  const dots = []
  for (let i = 0; i < points.value.length; i++) {
    const y = getY(i)
    if (y == null) continue
    dots.push({
      left: tickX(i) + 'rpx',
      top: y + 'rpx',
    })
  }
  return dots
}

const tempSegments = computed(() =>
  buildSegments((i) => mapY(points.value[i].temp, minTemp.value, maxTemp.value))
)
const tempDots = computed(() =>
  buildDots((i) => mapY(points.value[i].temp, minTemp.value, maxTemp.value))
)
const humSegments = computed(() =>
  buildSegments((i) => mapY(points.value[i].humidity, minHum.value, maxHum.value))
)
const humDots = computed(() =>
  buildDots((i) => mapY(points.value[i].humidity, minHum.value, maxHum.value))
)

/** 横向网格线 4 条（顶/中两条/底） */
const gridLines = computed(() => {
  const lines = []
  for (let i = 0; i <= 4; i++) {
    lines.push(CHART_PADDING_TOP + (CHART_INNER_HEIGHT * i) / 4)
  }
  return lines
})

function normalizeForecastList(list) {
  return (list || [])
    .map((row) => ({
      temp: row.temp,
      humidity: row.humidity,
      weatherText: row.weatherText,
      windClass: row.windClass,
      windDir: row.windDir,
      dataTime: row.dataTime,
      shortTime: shortTime(row.dataTime),
    }))
    .filter((p) => p.temp != null || p.humidity != null)
}

function normalizeHistoryList(list) {
  return (list || [])
    .map((row) => ({
      temp: row.temp,
      humidity: row.humidity,
      weatherText: row.weatherText,
      windClass: row.windClass,
      windDir: row.windDir,
      observedAt: row.observedAt,
      shortTime: shortTime(row.observedAt),
    }))
    .filter((p) => p.temp != null || p.humidity != null)
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  // 优先使用百度未来 24 小时逐小时预报（接口稳定且数据较密）
  if (queryDistrictId.value) {
    try {
      const list = await fetchHourlyForecast(queryDistrictId.value)
      const mapped = normalizeForecastList(list)
      if (mapped.length) {
        sourceFromForecast.value = true
        points.value = mapped
        loading.value = false
        return
      }
    } catch (e) {
      console.warn('[detail] forecast failed, fallback to history:', e?.message)
    }
  }

  // 回退到当日历史快照
  try {
    if (!queryCity.value) {
      throw new Error('缺少城市参数')
    }
    const list = await fetchWeatherHistory(queryCity.value, dateStr.value)
    const mapped = normalizeHistoryList(list)
    mapped.sort((a, b) => String(a.observedAt).localeCompare(String(b.observedAt)))
    sourceFromForecast.value = false
    points.value = mapped
  } catch (e) {
    errorMsg.value = e?.message || '加载失败'
    points.value = []
  } finally {
    loading.value = false
  }
}

onLoad((q) => {
  queryCity.value = decodeURIComponent(q.city || '')
  queryDistrictId.value = decodeURIComponent(q.districtId || '')
  cityLabel.value = decodeURIComponent(q.label || q.city || '')
  dateStr.value = q.date || today()
  if (!queryCity.value && !queryDistrictId.value) {
    errorMsg.value = '缺少城市参数'
    loading.value = false
    return
  }
  load()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  min-height: 100dvh;
  padding: 24rpx 28rpx 32rpx;
  padding-bottom: calc(32rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, #dff6ff 0%, #fffbeb 36%, #ffffff 100%);
  box-sizing: border-box;
}
.header {
  margin-bottom: 28rpx;
}
.title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #0c4a6e;
}
.sub {
  display: block;
  margin-top: 8rpx;
  font-size: 30rpx;
  color: #0369a1;
}
.date {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #64748b;
}
.banner-err {
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 26rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #fecaca;
}
.card {
  background: #ffffff;
  border: 1rpx solid rgba(14, 165, 233, 0.15);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 36rpx rgba(14, 165, 233, 0.08);
}
.chart-card {
  padding: 24rpx 24rpx 16rpx;
}
.chart-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
}
.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #0f172a;
}
.legend {
  display: flex;
  flex-direction: row;
  gap: 24rpx;
}
.leg-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
}
.line-mark {
  width: 28rpx;
  height: 6rpx;
  border-radius: 3rpx;
}
.line-mark.t {
  background: #0ea5e9;
}
.line-mark.h {
  background: #16a34a;
}
.leg-text {
  font-size: 24rpx;
  color: #475569;
}
.hint {
  display: block;
  font-size: 22rpx;
  color: #64748b;
  margin: 8rpx 0 12rpx;
}
.chart-scroll {
  width: 100%;
  white-space: nowrap;
}
.chart-canvas {
  position: relative;
  display: inline-block;
}
.grid-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 0;
  border-top: 1rpx dashed #e2e8f0;
}
.poly {
  position: absolute;
  left: 0;
  top: 0;
}
.seg {
  position: absolute;
  height: 4rpx;
  border-radius: 4rpx;
}
.seg-temp {
  background: linear-gradient(90deg, #38bdf8, #0284c7);
  box-shadow: 0 0 6rpx rgba(14, 165, 233, 0.35);
}
.seg-hum {
  background: linear-gradient(90deg, #4ade80, #16a34a);
  box-shadow: 0 0 6rpx rgba(34, 197, 94, 0.3);
}
.dot {
  position: absolute;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  margin-left: -7rpx;
  margin-top: -7rpx;
  border: 2rpx solid #ffffff;
}
.dot-temp {
  background: #0ea5e9;
}
.dot-hum {
  background: #16a34a;
}
.x-axis {
  position: absolute;
  left: 0;
  bottom: 12rpx;
  height: 32rpx;
}
.x-tick {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
  white-space: nowrap;
}
.x-tick-text {
  font-size: 20rpx;
  color: #64748b;
}
.legend-axis {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-top: 8rpx;
}
.axis-text {
  font-size: 22rpx;
  color: #94a3b8;
}
.empty {
  padding: 40rpx 24rpx;
  color: #64748b;
  font-size: 26rpx;
  line-height: 1.6;
  text-align: center;
}
.row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #e2e8f0;
}
.row:last-child {
  border-bottom: none;
}
.time {
  font-size: 24rpx;
  color: #64748b;
}
.vals {
  font-size: 26rpx;
  color: #0f172a;
}
.vals-tip {
  font-size: 24rpx;
  color: #64748b;
}
</style>
