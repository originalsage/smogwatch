CREATE DATABASE IF NOT EXISTS smogwatch DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE smogwatch;

CREATE TABLE IF NOT EXISTS weather_snapshot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  city_name VARCHAR(50) NOT NULL,
  temp INT NULL,
  feels_like INT NULL,
  humidity INT NULL,
  weather_text VARCHAR(50) NULL,
  wind_class VARCHAR(30) NULL,
  wind_dir VARCHAR(30) NULL,
  aqi INT NULL,
  pm25 INT NULL,
  observed_at_text VARCHAR(40) NULL,
  raw_json JSON NULL,
  observed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_city_time(city_name, observed_at)
);

CREATE TABLE IF NOT EXISTS weather_district (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  district_id VARCHAR(20) NOT NULL,
  province VARCHAR(50) NOT NULL,
  city VARCHAR(50) NOT NULL,
  city_geocode VARCHAR(20) NOT NULL,
  district VARCHAR(50) NOT NULL,
  district_geocode VARCHAR(20) NOT NULL,
  lon DECIMAL(12,6) NULL,
  lat DECIMAL(12,6) NULL,
  UNIQUE KEY uk_district_id(district_id),
  INDEX idx_province_city(province, city, city_geocode)
);
