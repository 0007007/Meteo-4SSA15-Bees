package com.meteo.observation.document;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * AirQuality - owned by observation-service (Member 3).
 */
@Document(collection = "air_quality")
public class AirQuality {

    @Id
    private String id;

    @NotNull(message = "cityId is required (City from geo-service)")
    private Long cityId;

    @NotNull(message = "measuredAt is required")
    private LocalDateTime measuredAt;

    @NotNull(message = "aqi is required")
    @Min(0)
    @Max(500)
    private Integer aqi;

    @DecimalMin("0.0")
    private Double pm25;

    @DecimalMin("0.0")
    private Double pm10;

    @DecimalMin("0.0")
    private Double o3;

    @DecimalMin("0.0")
    private Double no2;

    private AirQualityLevel level;

    public AirQuality() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getO3() {
        return o3;
    }

    public void setO3(Double o3) {
        this.o3 = o3;
    }

    public Double getNo2() {
        return no2;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public AirQualityLevel getLevel() {
        return level;
    }

    public void setLevel(AirQualityLevel level) {
        this.level = level;
    }
}
