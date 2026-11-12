"""Business rules for Report (same role as ReportServiceImpl in a Java service)."""
import os
from datetime import datetime

from sqlalchemy.orm import Session

from app.clients.meteo_client import MeteoClient
from app.config import settings
from app.exceptions import ConflictError, NotFoundError
from app.exports.content import build_content
from app.exports.writers import write_csv, write_pdf
from app.models import Report, ReportFormat, ReportStatus
from app.repositories import report_repository as repo
from app.repositories import schedule_repository
from app.schemas import ReportIn

MEDIA_TYPES = {ReportFormat.CSV: "text/csv", ReportFormat.PDF: "application/pdf"}


def find_all(db: Session, city_id: int | None = None) -> list[Report]:
    return repo.find_all(db, city_id)


def find_by_id(db: Session, report_id: int) -> Report:
    report = repo.find_by_id(db, report_id)
    if report is None:
        raise NotFoundError("Report", report_id)
    return report


def create(db: Session, data: ReportIn) -> Report:
    _check_schedule(db, data.schedule_id)
    report = Report(**data.model_dump(), status=ReportStatus.PENDING)
    return repo.save(db, report)


def update(db: Session, report_id: int, data: ReportIn) -> Report:
    report = find_by_id(db, report_id)
    _check_schedule(db, data.schedule_id)
    for key, value in data.model_dump().items():
        setattr(report, key, value)
    # the content changed, so the old file is out of date
    _remove_file(report)
    report.file_name = None
    report.status = ReportStatus.PENDING
    report.generated_at = None
    report.error_message = None
    return repo.save(db, report)


def delete(db: Session, report_id: int) -> None:
    report = find_by_id(db, report_id)
    _remove_file(report)
    repo.delete(db, report)


def generate(db: Session, report_id: int, client: MeteoClient | None = None) -> Report:
    """Collects the data from the other microservices and writes the CSV or PDF file."""
    report = find_by_id(db, report_id)
    client = client or MeteoClient(settings.gateway_url)
    try:
        content = build_content(report.report_type, report.city_id, report.period_start,
                                report.period_end, client, report.title)
        os.makedirs(settings.reports_dir, exist_ok=True)
        file_name = f"report-{report.id}-{report.report_type.value.lower()}.{report.format.value.lower()}"
        path = os.path.join(settings.reports_dir, file_name)
        if report.format == ReportFormat.CSV:
            write_csv(path, content)
        else:
            write_pdf(path, content)
        report.file_name = file_name
        report.status = ReportStatus.GENERATED
        report.generated_at = datetime.now()
        report.error_message = None
    except Exception as exc:  # keep the reason so the user can see why it failed
        report.status = ReportStatus.FAILED
        report.error_message = str(exc)[:500]
    return repo.save(db, report)


def get_file(db: Session, report_id: int) -> tuple[str, str, str]:
    """Returns (path, file name, media type) of a generated report."""
    report = find_by_id(db, report_id)
    path = _path(report)
    if report.status != ReportStatus.GENERATED or path is None or not os.path.isfile(path):
        raise ConflictError(f"Report {report_id} has no file yet: call POST /api/reports/{report_id}/generate first")
    return path, report.file_name, MEDIA_TYPES[report.format]


def _check_schedule(db: Session, schedule_id: int | None) -> None:
    if schedule_id is not None and not schedule_repository.exists_by_id(db, schedule_id):
        raise NotFoundError("ReportSchedule", schedule_id)


def _path(report: Report) -> str | None:
    return os.path.join(settings.reports_dir, report.file_name) if report.file_name else None


def _remove_file(report: Report) -> None:
    path = _path(report)
    if path and os.path.isfile(path):
        os.remove(path)
