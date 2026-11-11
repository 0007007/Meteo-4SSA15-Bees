package com.meteo.forecast.document;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ForecastModel - owned by forecast-service (Member 4).
 */
@Document(collection = "forecast_models")
public class ForecastModel {

    @Id
    private String id;

    @NotBlank(message = "name is required")
    @Size(max = 50)
    private String name;

    @Size(max = 100)
    private String provider;

    @Size(max = 20)
    private String version;

    @DecimalMin("0.0")
    private Double resolutionKm;

    @Min(1)
    @Max(30)
    private Integer horizonDays;

    @Size(max = 500)
    private String description;

    public ForecastModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Double getResolutionKm() {
        return resolutionKm;
    }

    public void setResolutionKm(Double resolutionKm) {
        this.resolutionKm = resolutionKm;
    }

    public Integer getHorizonDays() {
        return horizonDays;
    }

    public void setHorizonDays(Integer horizonDays) {
        this.horizonDays = horizonDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
