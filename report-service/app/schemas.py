"""Request/response bodies (DTOs) validated by Pydantic. JSON uses camelCase like the Java services."""
from datetime import date, datetime

from pydantic import BaseModel, ConfigDict, Field, model_validator
from pydantic.alias_generators import to_camel

from app.models import Frequency, ReportFormat, ReportStatus, ReportType

EMAIL_PATTERN = r"^[^@\s]+@[^@\s]+\.[^@\s]+$"


class CamelModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True, from_attributes=True)


# ---------------------------------------------------------------- ReportSchedule
class ReportScheduleIn(CamelModel):
    name: str = Field(min_length=1, max_length=100)
    city_id: int = Field(gt=0, description="City id from geo-service")
    report_type: ReportType
    format: ReportFormat = ReportFormat.PDF
    frequency: Frequency
    recipient_email: str | None = Field(default=None, max_length=120, pattern=EMAIL_PATTERN)
    active: bool = True


class ReportScheduleOut(ReportScheduleIn):
    id: int
    created_at: datetime


# ---------------------------------------------------------------- Report
class ReportIn(CamelModel):
    title: str = Field(min_length=1, max_length=150)
    city_id: int = Field(gt=0, description="City id from geo-service")
    report_type: ReportType
    format: ReportFormat = ReportFormat.PDF
    period_start: date
    period_end: date
    schedule_id: int | None = None

    @model_validator(mode="after")
    def check_period(self):
        if self.period_end < self.period_start:
            raise ValueError("periodEnd must be on or after periodStart")
        return self


class ReportOut(ReportIn):
    id: int
    status: ReportStatus
    file_name: str | None = None
    error_message: str | None = None
    created_at: datetime
    generated_at: datetime | None = None
