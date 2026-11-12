package com.meteo.station.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * WeatherStation - owned by station-service (Member 2).
 */
@Entity
@Table(name = "weather_stations")
public class WeatherStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "code is required")
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "latitude is required")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @Column(nullable = false)
    private Double longitude;

    private Double altitude;

    private LocalDate installationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StationStatus status = StationStatus.ACTIVE;

    @NotNull(message = "cityId is required (City from geo-service)")
    @Column(name = "city_id", nullable = false)
    private Long cityId;

    public WeatherStation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }
}
