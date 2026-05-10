import { getJson, postJson } from '../utils/http.js'

export async function resolveLocation() {
  const data = await postJson('/api/location/resolve', {})
  if (!data.success) throw new Error(data.message || '定位失败')
  return data.data
}

export async function saveManualCity(payload) {
  const data = await postJson('/api/location/manual', payload)
  if (!data.success) throw new Error(data.message || '保存地区失败')
  return data.data
}

export async function fetchCurrentWeather(districtId) {
  const data = await getJson('/api/weather/current', { city: districtId })
  if (!data.success) throw new Error(data.message || '天气获取失败')
  return data.data
}

export async function fetchWeatherHistory(city, date) {
  const data = await getJson('/api/weather/history', { city, date })
  if (!data.success) throw new Error(data.message || '历史数据获取失败')
  return data.data || []
}

export async function fetchHourlyForecast(districtId) {
  const data = await getJson('/api/weather/forecast', { city: districtId })
  if (!data.success) throw new Error(data.message || '逐小时预报获取失败')
  return data.data || []
}

export async function fetchProvinces() {
  const data = await getJson('/api/regions/provinces')
  if (!data.success) throw new Error(data.message || '省份列表失败')
  return data.data || []
}

export async function fetchCities(province) {
  const data = await getJson('/api/regions/cities', { province })
  if (!data.success) throw new Error(data.message || '城市列表失败')
  return data.data || []
}

export async function fetchDistricts(province, city, cityGeocode) {
  const data = await getJson('/api/regions/districts', { province, city, cityGeocode })
  if (!data.success) throw new Error(data.message || '区县列表失败')
  return data.data || []
}
