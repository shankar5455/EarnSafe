package com.earnsafe.service;

import com.earnsafe.entity.WeatherData;
import com.earnsafe.entity.WeatherEvent;
import com.earnsafe.repository.WeatherDataRepository;
import com.earnsafe.repository.WeatherEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    @Value("${app.weather.api.key:}")
    private String weatherApiKey;

    @Value("${app.weather.api.url:https://api.openweathermap.org/data/2.5}")
    private String weatherApiUrl;

    @Value("${app.weather.timeout-ms:8000}")
    private long timeoutMs;

    private final RestTemplateBuilder restTemplateBuilder;
    private final WeatherEventRepository weatherEventRepository;
    private final WeatherDataRepository weatherDataRepository;

    public WeatherEvent fetchAndStoreCurrentWeather(String city, String zone) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required for weather ingestion");
        }
        if (weatherApiKey == null || weatherApiKey.isBlank()) {
            throw new IllegalStateException("OpenWeatherMap API key is not configured");
        }

        RestTemplate client = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();

        String url = weatherApiUrl + "/weather?q=" + city.trim() + ",IN&units=metric&appid=" + weatherApiKey;
        ResponseEntity<Map> response = client.getForEntity(url, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch weather for city " + city);
        }

        WeatherPayload payload = parsePayload(response.getBody());
        int aqi = fetchAqi(client, payload.latitude(), payload.longitude());
        WeatherEvent event = toWeatherEvent(city.trim(), zone, payload, aqi);
        WeatherData data = toWeatherData(city.trim(), zone, payload, aqi, event.getEventType());

        weatherEventRepository.save(event);
        weatherDataRepository.save(data);

        return event;
    }

    public BigDecimal calculateSeverity(WeatherEvent event) {
        double rainfall = event.getRainfallMm() != null ? event.getRainfallMm().doubleValue() : 0.0;
        double temperature = event.getTemperature() != null ? event.getTemperature().doubleValue() : 30.0;
        double aqi = event.getAqi() != null ? event.getAqi() : 100;

        double severity = (Math.min(rainfall, 120.0) / 120.0) * 4.0
                + (Math.max(0, temperature - 30.0) / 20.0) * 3.0
                + (Math.min(aqi, 500.0) / 500.0) * 3.0;

        if (Boolean.TRUE.equals(event.getFloodAlert()) || Boolean.TRUE.equals(event.getClosureAlert())) {
            severity += 1.0;
        }

        return BigDecimal.valueOf(Math.min(10.0, Math.max(0.0, severity))).setScale(2, RoundingMode.HALF_UP);
    }

    private int fetchAqi(RestTemplate client, Double lat, Double lon) {
        if (lat == null || lon == null) {
            return 100;
        }
        try {
            String url = weatherApiUrl + "/air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + weatherApiKey;
            ResponseEntity<Map> response = client.getForEntity(url, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return 100;
            }
            Object listObj = response.getBody().get("list");
            if (listObj instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map) {
                Object mainObj = map.get("main");
                if (mainObj instanceof Map<?, ?> main && main.get("aqi") instanceof Number aqiScale) {
                    return Math.max(0, Math.min(500, aqiScale.intValue() * 100));
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch AQI: {}", ex.getMessage());
        }
        return 100;
    }

    private WeatherEvent toWeatherEvent(String city, String zone, WeatherPayload payload, int aqi) {
        String eventType = resolveEventType(payload.rainfallMm(), payload.temperature(), aqi, payload.conditionCode());
        return WeatherEvent.builder()
                .city(city)
                .zone((zone == null || zone.isBlank()) ? city : zone)
                .eventType(eventType)
                .temperature(payload.temperatureBd())
                .rainfallMm(payload.rainfallBd())
                .aqi(aqi)
                .floodAlert(payload.rainfallMm() >= 50.0 || eventType.equals("FLOOD_ALERT"))
                .closureAlert(eventType.equals("ZONE_CLOSURE"))
                .eventTimestamp(LocalDateTime.now())
                .sourceType("OPEN_WEATHER")
                .build();
    }

    private WeatherData toWeatherData(String city, String zone, WeatherPayload payload, int aqi, String eventType) {
        WeatherEvent tempEvent = WeatherEvent.builder()
                .temperature(payload.temperatureBd())
                .rainfallMm(payload.rainfallBd())
                .aqi(aqi)
                .floodAlert("FLOOD_ALERT".equals(eventType))
                .closureAlert("ZONE_CLOSURE".equals(eventType))
                .build();

        return WeatherData.builder()
                .city(city)
                .zone((zone == null || zone.isBlank()) ? city : zone)
                .latitude(payload.latitude())
                .longitude(payload.longitude())
                .temperature(payload.temperatureBd())
                .rainfallMm(payload.rainfallBd())
                .aqi(aqi)
                .windSpeed(payload.windBd())
                .weatherCondition(payload.conditionText())
                .alerts(payload.conditionText())
                .severityScore(calculateSeverity(tempEvent))
                .observedAt(LocalDateTime.now())
                .build();
    }

    private String resolveEventType(double rainfallMm, double temperature, int aqi, int conditionCode) {
        if (rainfallMm >= 50.0 || (conditionCode >= 200 && conditionCode < 300)) return "FLOOD_ALERT";
        if (rainfallMm >= 30.0) return "HEAVY_RAIN";
        if (temperature >= 42.0) return "HEATWAVE";
        if (aqi >= 300) return "POLLUTION_SPIKE";
        if (conditionCode >= 700 && conditionCode <= 781) return "ZONE_CLOSURE";
        return "HEAVY_RAIN";
    }

    private WeatherPayload parsePayload(Map<String, Object> body) {
        Map<String, Object> main = asMap(body.get("main"));
        Map<String, Object> coord = asMap(body.get("coord"));
        Map<String, Object> rain = asMap(body.get("rain"));
        Map<String, Object> wind = asMap(body.get("wind"));

        List<?> weather = body.get("weather") instanceof List<?> list ? list : List.of();
        Map<String, Object> weather0 = !weather.isEmpty() && weather.get(0) instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();

        double temp = toDouble(main.get("temp"), 30.0);
        double rainfall = toDouble(rain.get("1h"), toDouble(rain.get("3h"), 0.0));
        double windSpeed = toDouble(wind.get("speed"), 4.0);
        int conditionCode = (int) toDouble(weather0.get("id"), 800);
        String conditionText = weather0.get("description") != null ? weather0.get("description").toString() : "clear";

        return new WeatherPayload(
                toDouble(coord.get("lat"), 0.0),
                toDouble(coord.get("lon"), 0.0),
                temp,
                rainfall,
                windSpeed,
                conditionCode,
                conditionText
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private double toDouble(Object value, double fallback) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private record WeatherPayload(
            Double latitude,
            Double longitude,
            double temperature,
            double rainfallMm,
            double windSpeed,
            int conditionCode,
            String conditionText
    ) {
        BigDecimal temperatureBd() {
            return BigDecimal.valueOf(temperature).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal rainfallBd() {
            return BigDecimal.valueOf(Math.max(0, rainfallMm)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal windBd() {
            return BigDecimal.valueOf(Math.max(0, windSpeed)).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
