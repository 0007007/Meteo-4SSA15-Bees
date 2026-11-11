"""SQLAlchemy engine and session (same role as Spring Data JPA in the Java services)."""
import logging
from collections.abc import Generator

from sqlalchemy import create_engine, text
from sqlalchemy.engine import make_url
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker

from app.config import settings

log = logging.getLogger(__name__)


class Base(DeclarativeBase):
    pass


engine = create_engine(settings.database_url, pool_pre_ping=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


def get_db() -> Generator[Session, None, None]:
    """FastAPI dependency: one session per request, always closed."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def ensure_database_exists() -> None:
    """Creates report_db on first start if it is missing (like createDatabaseIfNotExist for MySQL)."""
    url = make_url(settings.database_url)
    if not url.drivername.startswith("postgresql") or not url.database:
        return
    admin_engine = create_engine(url.set(database="postgres"), isolation_level="AUTOCOMMIT")
    try:
        with admin_engine.connect() as conn:
            found = conn.execute(
                text("SELECT 1 FROM pg_database WHERE datname = :name"), {"name": url.database}
            ).scalar()
            if not found:
                conn.execute(text(f'CREATE DATABASE "{url.database}"'))
                log.info("Database %s created", url.database)
    except Exception as exc:  # the service still starts; create_all will report the real problem
        log.warning("Could not check or create database %s: %s", url.database, exc)
    finally:
        admin_engine.dispose()
