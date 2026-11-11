"""The two entities of report-service, mapped with SQLAlchemy (like @Entity in Java)."""
import enum
from datetime import date, datetime

from sqlalchemy import Date, DateTime, Enum, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class ReportType(str, enum.Enum):
    SUMMARY = "SUMMARY"
    OBSERVATIONS = "OBSERVATIONS"
    FORECASTS = "FORECASTS"
    ALERTS = "ALERTS"


class ReportFormat(str, enum.Enum):
    CSV = "CSV"
    PDF = "PDF"


class ReportStatus(str, enum.Enum):
    PENDING = "PENDING"
    GENERATED = "GENERATED"
    FAILED = "FAILED"


class Frequency(str, enum.Enum):
    DAILY = "DAILY"
    WEEKLY = "WEEKLY"
    MONTHLY = "MONTHLY"


def _enum_column(enum_class: type[enum.Enum]) -> Enum:
    # Stored as text, like @Enumerated(EnumType.STRING)
    return Enum(enum_class, native_enum=False, length=20)


class ReportSchedule(Base):
    __tablename__ = "report_schedules"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(100))
    city_id: Mapped[int]                                   # City from geo-service
    report_type: Mapped[ReportType] = mapped_column(_enum_column(ReportType))
    format: Mapped[ReportFormat] = mapped_column(_enum_column(ReportFormat))
    frequency: Mapped[Frequency] = mapped_column(_enum_column(Frequency))
    recipient_email: Mapped[str | None] = mapped_column(String(120))
    active: Mapped[bool] = mapped_column(default=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)


class Report(Base):
    __tablename__ = "reports"

    id: Mapped[int] = mapped_column(primary_key=True)
    title: Mapped[str] = mapped_column(String(150))
    city_id: Mapped[int]                                   # City from geo-service
    report_type: Mapped[ReportType] = mapped_column(_enum_column(ReportType))
    format: Mapped[ReportFormat] = mapped_column(_enum_column(ReportFormat))
    period_start: Mapped[date] = mapped_column(Date)
    period_end: Mapped[date] = mapped_column(Date)
    status: Mapped[ReportStatus] = mapped_column(_enum_column(ReportStatus), default=ReportStatus.PENDING)
    file_name: Mapped[str | None] = mapped_column(String(255))
    error_message: Mapped[str | None] = mapped_column(String(500))
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)
    generated_at: Mapped[datetime | None] = mapped_column(DateTime)
    schedule_id: Mapped[int | None] = mapped_column(ForeignKey("report_schedules.id"))

    schedule: Mapped[ReportSchedule | None] = relationship()
