"""Builds the table that goes into a report, from the data of the other microservices."""
from dataclasses import dataclass, field
from datetime import date, datetime

from app.clients.meteo_client import MeteoClient
from app.models import ReportType


@dataclass
class ReportContent:
    title: str
    meta: list[str]
    columns: list[str]
    rows: list[list[str]] = field(default_factory=list)
    landscape: bool = False


def _day(value: str | None) -> date | None:
    """'2026-11-10T14:00:00' or '2026-11-10' -> date(2026, 11, 10)."""
    if not value:
        return None
    return date.fromisoformat(value[:10])


def _in_period(value: str | None, start: date, end: date) -> bool:
    day = _day(value)
    return day is not None and start <= day <= end


def _num(value, digits: int = 1) -> str:
    if value is None or value == "":
        return ""
    return f"{float(value):.{digits}f}"


def _when(value: str | None) -> str:
    return value.replace("T", " ")[:16] if value else ""


def _avg(values: list[float]) -> float | None:
    return sum(values) / len(values) if values else None


def build_content(report_type: ReportType, city_id: int, start: date, end: date,
                  client: MeteoClient, title: str = "") -> ReportContent:
    city = client.get_city(city_id)
    country = (city.get("country") or {}).get("name", "")
    city_label = f"{city.get('name', city_id)}" + (f", {country}" if country else "")
    meta = [
        f"City: {city_label}",
        f"Period: {start.isoformat()} to {end.isoformat()}",
        f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M')}",
    ]
    title = title or f"{report_type.value.title()} report - {city.get('name', city_id)}"

    if report_type == ReportType.OBSERVATIONS:
        return _observations(title, meta, city_id, start, end, client)
    if report_type == ReportType.FORECASTS:
        return _forecasts(title, meta, city_id, start, end, client)
    if report_type == ReportType.ALERTS:
        return _alerts(title, meta, city_id, start, end, client)
    return _summary(title, meta, city_id, start, end, client)


def _station_observations(city_id: int, start: date, end: date, client: MeteoClient):
    stations = client.get_stations(city_id)
    pairs = []
    for station in stations:
        for obs in client.get_observations(station["id"]):
            if _in_period(obs.get("observedAt"), start, end):
                pairs.append((station, obs))
    pairs.sort(key=lambda p: (p[1].get("observedAt") or "", p[0].get("code") or ""))
    return stations, pairs


def _observations(title, meta, city_id, start, end, client) -> ReportContent:
    _, pairs = _station_observations(city_id, start, end, client)
    content = ReportContent(
        title, meta,
        ["Station", "Observed at", "Temp (°C)", "Humidity (%)", "Pressure (hPa)",
         "Wind (km/h)", "Wind dir (°)", "Rain (mm)"],
        landscape=True,
    )
    for station, obs in pairs:
        content.rows.append([
            station.get("code", ""), _when(obs.get("observedAt")), _num(obs.get("temperature")),
            _num(obs.get("humidity"), 0), _num(obs.get("pressure")), _num(obs.get("windSpeed")),
            str(obs.get("windDirection") if obs.get("windDirection") is not None else ""),
            _num(obs.get("precipitation")),
        ])
    return content


def _forecasts(title, meta, city_id, start, end, client) -> ReportContent:
    forecasts = [f for f in client.get_forecasts(city_id) if _in_period(f.get("forecastDate"), start, end)]
    forecasts.sort(key=lambda f: f.get("forecastDate") or "")
    content = ReportContent(title, meta, ["Date", "Condition", "Min (°C)", "Max (°C)", "Rain (%)", "Wind (km/h)"])
    for f in forecasts:
        content.rows.append([
            f.get("forecastDate", ""), (f.get("condition") or "").replace("_", " ").title(),
            _num(f.get("minTemperature")), _num(f.get("maxTemperature")),
            str(f.get("precipitationProbability") if f.get("precipitationProbability") is not None else ""),
            _num(f.get("windSpeed")),
        ])
    return content


def _alert_overlaps(alert: dict, start: date, end: date) -> bool:
    first = _day(alert.get("startTime"))
    last = _day(alert.get("endTime")) or first
    return first is not None and first <= end and last >= start


def _alerts(title, meta, city_id, start, end, client) -> ReportContent:
    alerts = [a for a in client.get_alerts(city_id) if _alert_overlaps(a, start, end)]
    alerts.sort(key=lambda a: a.get("startTime") or "")
    content = ReportContent(title, meta, ["Title", "Type", "Severity", "Start", "End", "Active"], landscape=True)
    for a in alerts:
        content.rows.append([
            a.get("title", ""), (a.get("type") or "").replace("_", " ").title(), a.get("severity", ""),
            _when(a.get("startTime")), _when(a.get("endTime")), "Yes" if a.get("active") else "No",
        ])
    return content


def _summary(title, meta, city_id, start, end, client) -> ReportContent:
    stations, pairs = _station_observations(city_id, start, end, client)
    observations = [obs for _, obs in pairs]
    temps = [o["temperature"] for o in observations if o.get("temperature") is not None]
    humidity = [o["humidity"] for o in observations if o.get("humidity") is not None]
    rain = [o["precipitation"] for o in observations if o.get("precipitation") is not None]
    wind = [o["windSpeed"] for o in observations if o.get("windSpeed") is not None]
    air = [a for a in client.get_air_quality(city_id) if _in_period(a.get("measuredAt"), start, end)]
    aqi = [a["aqi"] for a in air if a.get("aqi") is not None]
    forecasts = [f for f in client.get_forecasts(city_id) if _in_period(f.get("forecastDate"), start, end)]
    alerts = [a for a in client.get_alerts(city_id) if _alert_overlaps(a, start, end)]
    by_severity = {level: sum(1 for a in alerts if a.get("severity") == level) for level in ("RED", "ORANGE", "YELLOW")}

    content = ReportContent(title, meta, ["Indicator", "Value"])
    content.rows = [
        ["Weather stations", str(len(stations))],
        ["Observations in period", str(len(observations))],
        ["Minimum temperature (°C)", _num(min(temps)) if temps else ""],
        ["Average temperature (°C)", _num(_avg(temps))],
        ["Maximum temperature (°C)", _num(max(temps)) if temps else ""],
        ["Average humidity (%)", _num(_avg(humidity), 0)],
        ["Total precipitation (mm)", _num(sum(rain)) if rain else ""],
        ["Maximum wind speed (km/h)", _num(max(wind)) if wind else ""],
        ["Average air quality index", _num(_avg(aqi), 0)],
        ["Forecast days in period", str(len(forecasts))],
        ["Alerts in period", str(len(alerts))],
        ["Alerts by severity", f"RED {by_severity['RED']}, ORANGE {by_severity['ORANGE']}, YELLOW {by_severity['YELLOW']}"],
    ]
    return content
