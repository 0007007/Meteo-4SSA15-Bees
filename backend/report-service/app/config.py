"""Settings read from environment variables (same role as application.yml in the Java services)."""
import os
from dataclasses import dataclass


def _env(name: str, default: str) -> str:
    return os.getenv(name, default)


@dataclass(frozen=True)
class Settings:
    app_name: str = _env("APP_NAME", "report-service")
    port: int = int(_env("PORT", "8087"))
    instance_host: str = _env("INSTANCE_HOST", "localhost")
    instance_ip: str = _env("INSTANCE_IP", "127.0.0.1")
    database_url: str = _env("DATABASE_URL", "postgresql+psycopg://meteo:meteo@localhost:5432/report_db")
    gateway_url: str = _env("GATEWAY_URL", "http://localhost:8090")
    eureka_url: str = _env("EUREKA_URL", "http://localhost:8761/eureka/")
    eureka_enabled: bool = _env("EUREKA_ENABLED", "true").lower() == "true"
    reports_dir: str = _env("REPORTS_DIR", "generated-reports")


settings = Settings()
