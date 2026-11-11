"""Business rules for ReportSchedule."""
from datetime import date, timedelta

from sqlalchemy.orm import Session

from app.exceptions import ConflictError, NotFoundError
from app.models import Frequency, Report, ReportSchedule
from app.repositories import report_repository
from app.repositories import schedule_repository as repo
from app.schemas import ReportIn, ReportScheduleIn
from app.services import report_service

PERIOD_DAYS = {Frequency.DAILY: 1, Frequency.WEEKLY: 7, Frequency.MONTHLY: 30}


def find_all(db: Session, city_id: int | None = None) -> list[ReportSchedule]:
    return repo.find_all(db, city_id)


def find_by_id(db: Session, schedule_id: int) -> ReportSchedule:
    schedule = repo.find_by_id(db, schedule_id)
    if schedule is None:
        raise NotFoundError("ReportSchedule", schedule_id)
    return schedule


def create(db: Session, data: ReportScheduleIn) -> ReportSchedule:
    return repo.save(db, ReportSchedule(**data.model_dump()))


def update(db: Session, schedule_id: int, data: ReportScheduleIn) -> ReportSchedule:
    schedule = find_by_id(db, schedule_id)
    for key, value in data.model_dump().items():
        setattr(schedule, key, value)
    return repo.save(db, schedule)


def delete(db: Session, schedule_id: int) -> None:
    schedule = find_by_id(db, schedule_id)
    if report_repository.exists_by_schedule_id(db, schedule_id):
        raise ConflictError(f"ReportSchedule {schedule_id} still has reports: delete them first")
    repo.delete(db, schedule)


def run_now(db: Session, schedule_id: int) -> Report:
    """Creates and generates the report of the last period (1, 7 or 30 days up to today)."""
    schedule = find_by_id(db, schedule_id)
    if not schedule.active:
        raise ConflictError(f"ReportSchedule {schedule_id} is not active")
    end = date.today()
    start = end - timedelta(days=PERIOD_DAYS[schedule.frequency] - 1)
    data = ReportIn(
        title=f"{schedule.name} ({start.isoformat()} to {end.isoformat()})",
        city_id=schedule.city_id,
        report_type=schedule.report_type,
        format=schedule.format,
        period_start=start,
        period_end=end,
        schedule_id=schedule.id,
    )
    report = report_service.create(db, data)
    return report_service.generate(db, report.id)
