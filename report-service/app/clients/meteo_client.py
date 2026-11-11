"""Reads data from the Java microservices through the API Gateway (REST + JSON)."""
import requests


class MeteoClientError(Exception):
    pass


class MeteoClient:
    def __init__(self, gateway_url: str, timeout: float = 10.0):
        self.base_url = gateway_url.rstrip("/")
        self.timeout = timeout
        self.session = requests.Session()

    def _get(self, path: str, params: dict | None = None):
        url = f"{self.base_url}{path}"
        try:
            response = self.session.get(url, params=params, timeout=self.timeout)
        except requests.RequestException as exc:
            raise MeteoClientError(f"Cannot reach {url}: {exc}") from exc
        if response.status_code == 404:
            raise MeteoClientError(f"{path} returned 404 (not found)")
        if not response.ok:
            raise MeteoClientError(f"{path} returned HTTP {response.status_code}")
        return response.json()

    # geo-service
    def get_city(self, city_id: int) -> dict:
        return self._get(f"/api/cities/{city_id}")

    # station-service
    def get_stations(self, city_id: int) -> list[dict]:
        return self._get("/api/stations", {"cityId": city_id})

    # observation-service
    def get_observations(self, station_id: int) -> list[dict]:
        return self._get("/api/observations", {"stationId": station_id})

    def get_air_quality(self, city_id: int) -> list[dict]:
        return self._get("/api/air-quality", {"cityId": city_id})

    # forecast-service
    def get_forecasts(self, city_id: int) -> list[dict]:
        return self._get("/api/forecasts", {"cityId": city_id})

    # alert-service
    def get_alerts(self, city_id: int) -> list[dict]:
        return self._get("/api/alerts", {"cityId": city_id})
