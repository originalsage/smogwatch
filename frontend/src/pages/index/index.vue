<template>
  <view class="page">
    <view class="hero">
      <text class="brand">SmogWatch</text>
      <text class="headline">雾霾与空气质量</text>
      <button class="loc-chip" type="default" @tap="openSheet">
        <text class="chip-k">当前城市</text>
        <text class="chip-v">{{ cityLine || '点击设置' }}</text>
        <text class="chip-arrow">›</text>
      </button>
      <text v-if="bannerErr" class="err">{{ bannerErr }}</text>
    </view>

    <view v-if="weather" class="card fade-in">
      <view class="card-h">
        <text class="card-title">实时天气</text>
        <navigator
          v-if="detailPageUrl"
          class="link-nav"
          :url="detailPageUrl"
          open-type="navigate"
          hover-class="link-nav-hover"
        >
          <text class="link-text">详情与曲线 ›</text>
        </navigator>
        <view v-else class="link-nav link-nav--disabled" @tap.stop="onDetailNoCity">
          <text class="link-text">详情与曲线 ›</text>
        </view>
      </view>
      <text class="wx">{{ weather.weatherText || '—' }}</text>
      <view class="grid">
        <view class="cell">
          <text class="lab">温度</text>
          <text class="num">{{ weather.temp != null ? weather.temp + '℃' : '—' }}</text>
        </view>
        <view class="cell">
          <text class="lab">体感</text>
          <text class="num">{{ weather.feelsLike != null ? weather.feelsLike + '℃' : '—' }}</text>
        </view>
        <view class="cell">
          <text class="lab">湿度</text>
          <text class="num">{{ weather.humidity != null ? weather.humidity + '%' : '—' }}</text>
        </view>
        <view class="cell">
          <text class="lab">风力</text>
          <text class="num">{{ weather.windClass || '—' }}</text>
        </view>
        <view class="cell wide">
          <text class="lab">风向</text>
          <text class="num">{{ weather.windDir || '—' }}</text>
        </view>
      </view>
    </view>

    <view v-if="weather" class="card aqi-card fade-in">
      <text class="card-title">空气质量</text>
      <view class="aqi-row">
        <text class="aqi-num" :style="aqiNumColorStyle">{{ weather.aqi != null ? weather.aqi : '—' }}</text>
        <text class="aqi-lv">{{ weather.airLevel || '暂无' }}</text>
      </view>
      <view class="track">
        <view class="fill" :style="aqiFillStyle" />
      </view>
      <view class="scale-row">
        <text class="scale-tick">0</text>
        <text class="scale-tick">50</text>
        <text class="scale-tick">100</text>
        <text class="scale-tick">150</text>
        <text class="scale-tick">200</text>
        <text class="scale-tick">300+</text>
      </view>
      <text class="adv">{{ weather.healthAdvice || '暂无建议' }}</text>
      <text v-if="weather.pm25 != null" class="pm">PM2.5：{{ weather.pm25 }} μg/m³</text>
    </view>

    <view v-if="!weather && loading" class="loading">
      <text>加载中…</text>
    </view>

    <view v-if="sheet" class="mask" @tap="closeSheet">
      <view class="sheet" @tap.stop>
        <view class="sheet-grab" aria-hidden="true" />
        <view class="sheet-head-row">
          <text class="sheet-title">定位与城市</text>
          <button class="close-top" type="default" @tap="closeSheet">关闭</button>
        </view>

        <scroll-view scroll-y class="sheet-top-scroll" :show-scrollbar="false">
          <view class="sheet-banner">
            <view class="sheet-txt">
              <text class="cap">当前</text>
              <text class="line">{{ cityLine || '未设置' }}</text>
            </view>
            <button class="btn-o" type="default" :disabled="relocating" @tap="reLocate">
              {{ relocating ? '定位中…' : '重新定位' }}
            </button>
          </view>
          <text v-if="sheetErr" class="err in">{{ sheetErr }}</text>

          <view class="steps">
            <text :class="['dot', step >= 1 ? 'on' : '']">1</text>
            <view class="line-d" />
            <text :class="['dot', step >= 2 ? 'on' : '']">2</text>
            <view class="line-d" />
            <text :class="['dot', step >= 3 ? 'on' : '']">3</text>
          </view>
        </scroll-view>

        <view class="sheet-main">
          <view v-if="step === 1" class="panel">
            <picker-view
              v-if="provinces.length"
              :key="'pv-p-' + provinces.length"
              class="pv"
              :value="[pIndex]"
              :indicator-style="pvIndicatorStyle"
              :mask-style="pvMaskStyle"
              @change="onPvProvince"
            >
              <picker-view-column>
                <view v-for="(name, idx) in provinces" :key="'p' + idx" class="pv-row">{{ name }}</view>
              </picker-view-column>
            </picker-view>
            <view v-else class="pick-empty">
              <text>正在加载省份…</text>
            </view>
            <button class="pv-next" type="default" :disabled="!provinces.length" @tap="confirmProvinceStep">
              下一步：选择城市
            </button>
          </view>

          <view v-if="step === 2" class="panel">
            <text class="crumb">{{ selProvince }}</text>
            <picker-view
              v-if="cityLabels.length"
              :key="'pv-c-' + cityLabels.length"
              class="pv"
              :value="[cIndex]"
              :indicator-style="pvIndicatorStyle"
              :mask-style="pvMaskStyle"
              @change="onPvCity"
            >
              <picker-view-column>
                <view v-for="(name, idx) in cityLabels" :key="'c' + idx" class="pv-row">{{ name }}</view>
              </picker-view-column>
            </picker-view>
            <view v-else class="pick-empty">
              <text>请先完成上一步</text>
            </view>
            <view class="nav2">
              <button class="ghost" type="default" @tap="backP">上一步</button>
              <button class="pv-next sm" type="default" :disabled="!cityLabels.length" @tap="confirmCityStep">
                下一步：区县
              </button>
            </view>
          </view>

          <view v-if="step === 3" class="panel">
            <text class="crumb">{{ selProvince }} · {{ cityLabels[cIndex] }}</text>
            <picker-view
              v-if="distLabels.length"
              :key="'pv-d-' + distLabels.length"
              class="pv"
              :value="[dIndex]"
              :indicator-style="pvIndicatorStyle"
              :mask-style="pvMaskStyle"
              @change="onPvDistrict"
            >
              <picker-view-column>
                <view v-for="(name, idx) in distLabels" :key="'d' + idx" class="pv-row">{{ name }}</view>
              </picker-view-column>
            </picker-view>
            <view v-else class="pick-empty">
              <text>暂无区县数据</text>
            </view>
            <view class="nav2">
              <button class="ghost" type="default" :disabled="confirming" @tap="backC">上一步</button>
              <button
                class="pri"
                type="default"
                :disabled="confirming || !districts.length"
                @tap="confirmDistrict"
              >
                {{ confirming ? '更新中…' : '确定' }}
              </button>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import {
  fetchCities,
  fetchCurrentWeather,
  fetchDistricts,
  fetchProvinces,
  resolveLocation,
  saveManualCity,
} from '../../api/smogwatch.js'

/** 与 .pv-row 高度一致，避免指示条与行错位 */
const pvIndicatorStyle =
  'height:68rpx;border-top:1rpx solid rgba(56,189,248,.4);border-bottom:1rpx solid rgba(56,189,248,.4);'
const pvMaskStyle =
  'background: linear-gradient(180deg, rgba(255,255,255,0.95), rgba(255,255,255,0)), linear-gradient(0deg, rgba(255,255,255,0.95), rgba(255,255,255,0));'

const cityLine = ref('')
const weather = ref(null)
const currentDistrictId = ref('')
const bannerErr = ref('')
const sheet = ref(false)
const sheetErr = ref('')
const relocating = ref(false)
const confirming = ref(false)
const loading = ref(false)

const provinces = ref([])
const cities = ref([])
const districts = ref([])
const pIndex = ref(0)
const cIndex = ref(0)
const dIndex = ref(0)
const step = ref(1)

const selProvince = computed(() => provinces.value[pIndex.value] || '')

const cityLabels = computed(() => cities.value.map((c) => c.city))
const distLabels = computed(() => districts.value.map((d) => d.district))

const aqiPct = computed(() => {
  const v = weather.value?.aqi
  if (v == null || v < 0) return 0
  return Math.min(100, Math.round((v / 300) * 100))
})

/** 详情页路由（navigator 比透明 button 在 H5 上更稳定） */
const detailPageUrl = computed(() => {
  const w = weather.value
  if (!w) return ''
  const city = (w.city || '').trim() || (cityLine.value || '').trim()
  if (!city) return ''
  const label = (cityLine.value || city).trim()
  const did = (currentDistrictId.value || '').trim()
  const didPart = did ? `&districtId=${encodeURIComponent(did)}` : ''
  return `/pages/detail/detail?city=${encodeURIComponent(city)}&label=${encodeURIComponent(label)}${didPart}`
})

/** 把 AQI 0-300+ 映射到分级颜色（不再用整条彩虹渐变） */
const aqiColor = computed(() => {
  const v = weather.value?.aqi
  if (v == null || v < 0) return '#cbd5e1'
  if (v <= 50) return '#22c55e' // 优
  if (v <= 100) return '#eab308' // 良
  if (v <= 150) return '#f97316' // 轻度污染
  if (v <= 200) return '#ef4444' // 中度污染
  if (v <= 300) return '#b91c1c' // 重度污染
  return '#7e22ce' // 严重污染
})

const aqiNumColorStyle = computed(() => ({ color: aqiColor.value }))
const aqiFillStyle = computed(() => ({
  width: aqiPct.value + '%',
  background: aqiColor.value,
}))

function onDetailNoCity() {
  uni.showToast({ title: '暂无城市信息', icon: 'none' })
}

watch(sheet, (open) => {
  if (open) {
    step.value = 1
    sheetErr.value = ''
  }
})

async function loadWeather(districtId) {
  loading.value = true
  try {
    weather.value = await fetchCurrentWeather(districtId)
    currentDistrictId.value = districtId || ''
    bannerErr.value = ''
  } catch (e) {
    bannerErr.value = e?.message || '天气获取失败'
    weather.value = null
  } finally {
    loading.value = false
  }
}

async function initProvinces() {
  provinces.value = await fetchProvinces()
}

async function afterProvincePick() {
  cities.value = selProvince.value ? await fetchCities(selProvince.value) : []
  cIndex.value = 0
  districts.value = []
  dIndex.value = 0
}

function onPvProvince(e) {
  const i = e.detail.value?.[0]
  if (i !== undefined && i !== null) pIndex.value = i
}

function confirmProvinceStep() {
  sheetErr.value = ''
  if (!provinces.value.length) return
  afterProvincePick().then(() => {
    if (cities.value.length) {
      step.value = 2
      cIndex.value = 0
    } else {
      sheetErr.value = '未加载到城市列表'
    }
  })
}

async function afterCityPick() {
  const row = cities.value[cIndex.value]
  districts.value = []
  dIndex.value = 0
  if (!row || !selProvince.value) return
  districts.value = await fetchDistricts(selProvince.value, row.city, row.cityGeocode)
}

function onPvCity(e) {
  const i = e.detail.value?.[0]
  if (i !== undefined && i !== null) cIndex.value = i
}

function confirmCityStep() {
  sheetErr.value = ''
  if (!cities.value.length) return
  afterCityPick().then(() => {
    if (districts.value.length) {
      step.value = 3
      dIndex.value = 0
    } else {
      sheetErr.value = '未加载到区县列表'
    }
  })
}

function onPvDistrict(e) {
  const i = e.detail.value?.[0]
  if (i !== undefined && i !== null) dIndex.value = i
}

function backP() {
  step.value = 1
  cities.value = []
  districts.value = []
  cIndex.value = 0
  dIndex.value = 0
}

function backC() {
  step.value = 2
  dIndex.value = 0
}

async function confirmDistrict() {
  const d = districts.value[dIndex.value]
  const c = cities.value[cIndex.value]
  if (!d || !c || !selProvince.value || confirming.value) return
  confirming.value = true
  sheetErr.value = ''
  try {
    await saveManualCity({
      province: selProvince.value,
      city: c.city,
      cityGeocode: c.cityGeocode,
      district: d.district,
      districtId: d.districtId,
    })
    const loc = `${selProvince.value}${c.city}${d.district}`
    cityLine.value = loc
    await loadWeather(d.districtId)
    closeSheet()
  } catch (e) {
    sheetErr.value = e?.message || '更新失败'
  } finally {
    confirming.value = false
  }
}

async function syncSelectors(loc) {
  const pv = loc.province || ''
  if (!pv || !provinces.value.length) return
  const pi = provinces.value.indexOf(pv)
  if (pi < 0) return
  pIndex.value = pi
  await afterProvincePick()
  const ci = cities.value.findIndex((x) => x.city === (loc.city || ''))
  if (ci < 0) return
  cIndex.value = ci
  await afterCityPick()
  const ad = loc.adcode || ''
  const di = districts.value.findIndex((x) => x.districtId === ad || x.district === (loc.district || ''))
  if (di >= 0) dIndex.value = di
}

async function runLocate() {
  const loc = await resolveLocation()
  cityLine.value = `${loc.province || ''}${loc.city || ''}${loc.district || ''}`
  await syncSelectors(loc)
  const id = loc.adcode || districts.value[dIndex.value]?.districtId
  if (!id) throw new Error('无法解析区县编码，请手动选择区县')
  await loadWeather(id)
}

async function autoLocate() {
  try {
    await runLocate()
  } catch (e) {
    bannerErr.value = e?.message || '首次定位失败，可点击城市条手动选择'
  }
}

async function reLocate() {
  relocating.value = true
  sheetErr.value = ''
  try {
    await runLocate()
  } catch (e) {
    sheetErr.value = e?.message || '重新定位失败'
  } finally {
    relocating.value = false
  }
}

function openSheet() {
  sheet.value = true
  bannerErr.value = ''
}

function closeSheet() {
  sheet.value = false
}

onMounted(() => {
  initProvinces().then(() => autoLocate())
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  min-height: 100dvh;
  padding: 40rpx 28rpx 32rpx;
  padding-bottom: calc(32rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, #dff6ff 0%, #fffbeb 38%, #ffffff 100%);
  box-sizing: border-box;
}
.hero {
  margin-bottom: 36rpx;
}
.brand {
  display: block;
  font-size: 24rpx;
  letter-spacing: 0.12em;
  color: #0ea5e9;
  text-transform: uppercase;
}
.headline {
  display: block;
  margin-top: 12rpx;
  font-size: 44rpx;
  font-weight: 700;
  color: #0c4a6e;
}
.loc-chip {
  margin-top: 28rpx;
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 22rpx 26rpx;
  border-radius: 999rpx;
  background: #ffffff;
  border: 2rpx solid rgba(14, 165, 233, 0.35);
  box-shadow: 0 8rpx 28rpx rgba(14, 165, 233, 0.12);
  color: #0f172a;
}
.chip-k {
  font-size: 22rpx;
  color: #64748b;
  margin-right: 12rpx;
}
.chip-v {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  text-align: left;
  color: #0f172a;
}
.chip-arrow {
  font-size: 36rpx;
  color: #0ea5e9;
  opacity: 0.9;
}
.err {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #dc2626;
}
.err.in {
  margin-top: 12rpx;
  margin-bottom: 8rpx;
}
.card {
  background: #ffffff;
  border: 1rpx solid rgba(14, 165, 233, 0.15);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(15, 118, 110, 0.06);
}
.card-h {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}
.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #0f172a;
}
.link-nav {
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  padding: 12rpx 16rpx;
  margin: -8rpx -8rpx -8rpx 0;
  line-height: 1.4;
}
.link-text {
  font-size: 26rpx;
  color: #0284c7;
  font-weight: 600;
}
.link-nav-hover .link-text {
  opacity: 0.72;
}
.link-nav--disabled .link-text {
  color: #94a3b8;
  font-weight: 500;
}
.wx {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 20rpx;
}
.grid {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 16rpx;
}
.cell {
  width: calc(50% - 8rpx);
  background: linear-gradient(145deg, #f0f9ff 0%, #ecfeff 100%);
  border-radius: 16rpx;
  padding: 18rpx 20rpx;
  box-sizing: border-box;
  border: 1rpx solid rgba(125, 211, 252, 0.35);
}
.cell.wide {
  width: 100%;
}
.lab {
  display: block;
  font-size: 22rpx;
  color: #64748b;
}
.num {
  display: block;
  margin-top: 6rpx;
  font-size: 28rpx;
  color: #0f172a;
  font-weight: 600;
}
.aqi-card .aqi-row {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 16rpx;
  margin: 12rpx 0 20rpx;
}
.aqi-num {
  font-size: 72rpx;
  font-weight: 800;
  color: #0284c7;
  transition: color 0.4s ease;
}
.aqi-lv {
  font-size: 28rpx;
  color: #0369a1;
}
.track {
  position: relative;
  height: 14rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  overflow: hidden;
}
.fill {
  height: 100%;
  border-radius: 999rpx;
  transition: width 0.6s ease, background 0.4s ease;
  box-shadow: 0 0 8rpx rgba(15, 118, 110, 0.18);
}
.scale-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-top: 8rpx;
  padding: 0 2rpx;
}
.scale-tick {
  font-size: 20rpx;
  color: #94a3b8;
  flex: 1;
  text-align: center;
}
.scale-tick:first-child {
  text-align: left;
}
.scale-tick:last-child {
  text-align: right;
}
.adv {
  display: block;
  margin-top: 20rpx;
  font-size: 26rpx;
  line-height: 1.55;
  color: #475569;
}
.pm {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #64748b;
}
.loading {
  text-align: center;
  padding: 80rpx;
  color: #64748b;
  font-size: 28rpx;
}
.fade-in {
  animation: fade 0.45s ease-out;
}
@keyframes fade {
  from {
    opacity: 0;
    transform: translateY(12rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.35);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  box-sizing: border-box;
}
.sheet {
  width: 100%;
  max-height: 88vh;
  max-height: 88dvh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #ffffff;
  border-radius: 28rpx 28rpx 0 0;
  padding-bottom: calc(16rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  border: 1rpx solid rgba(14, 165, 233, 0.2);
  border-bottom: none;
  box-sizing: border-box;
  box-shadow: 0 -8rpx 48rpx rgba(14, 165, 233, 0.12);
  position: relative;
  z-index: 1001;
}
.sheet-grab {
  width: 72rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  margin: 16rpx auto 8rpx;
}
.sheet-head-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 8rpx 28rpx 12rpx;
  flex-shrink: 0;
}
.sheet-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 700;
  color: #0f172a;
}
.close-top {
  flex-shrink: 0;
  font-size: 28rpx;
  color: #0284c7;
  background: #f0f9ff;
  border: 1rpx solid #bae6fd;
  border-radius: 999rpx;
  padding: 12rpx 28rpx;
  margin: 0;
  line-height: 1.2;
}
.sheet-top-scroll {
  flex-shrink: 0;
  height: 200rpx;
  max-height: 26vh;
  padding: 0 28rpx;
  box-sizing: border-box;
}
.sheet-main {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 0 28rpx 4rpx;
  box-sizing: border-box;
}
.sheet-banner {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}
.sheet-txt {
  flex: 1;
}
.cap {
  display: block;
  font-size: 22rpx;
  color: #64748b;
}
.line {
  display: block;
  margin-top: 4rpx;
  font-size: 28rpx;
  color: #0f172a;
}
.btn-o {
  font-size: 24rpx;
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: #e0f2fe;
  color: #0369a1;
  border: 1rpx solid #38bdf8;
}
.steps {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  margin: 12rpx 0 16rpx;
}
.dot {
  width: 44rpx;
  height: 44rpx;
  line-height: 44rpx;
  text-align: center;
  border-radius: 50%;
  font-size: 22rpx;
  color: #94a3b8;
  background: #f1f5f9;
}
.dot.on {
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  color: #ffffff;
}
.line-d {
  width: 40rpx;
  height: 4rpx;
  background: #e2e8f0;
  border-radius: 4rpx;
}
.panel {
  padding: 4rpx 0 8rpx;
  overflow: visible;
}
.crum {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 10rpx;
}
.pv {
  width: 100%;
  height: 280rpx;
  max-height: 36vh;
  background: #f8fafc;
  border-radius: 20rpx;
  border: 1rpx solid #e2e8f0;
  overflow: hidden;
}
.pv-row {
  height: 68rpx;
  line-height: 68rpx;
  text-align: center;
  font-size: 28rpx;
  color: #0f172a;
}
.pick-empty {
  padding: 48rpx 24rpx;
  text-align: center;
  font-size: 26rpx;
  color: #64748b;
  background: #f8fafc;
  border-radius: 20rpx;
  border: 1rpx dashed #cbd5e1;
}
.pv-next {
  margin-top: 16rpx;
  width: 100%;
  background: linear-gradient(90deg, #0ea5e9, #22d3ee);
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 20rpx;
  border: none;
  padding: 18rpx 22rpx;
}
.pv-next.sm {
  margin-top: 0;
  flex: 1;
}
.ghost {
  margin-top: 0;
  flex: 0 0 auto;
  min-width: 160rpx;
  background: #ffffff;
  color: #475569;
  font-size: 26rpx;
  border: 1rpx solid #cbd5e1;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
}
.nav2 {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  gap: 16rpx;
  margin-top: 16rpx;
}
.pri {
  flex: 1;
  background: linear-gradient(90deg, #0ea5e9, #2563eb);
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 20rpx;
  border: none;
  padding: 18rpx 22rpx;
}
</style>
