"""Data access for ReportSchedule (same role as a Spring Data repository)."""
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models import ReportSchedule


def find_all(db: Session, city_id: int | None = None) -> list[ReportSchedule]:
    stmt = select(ReportSchedule).order_by(ReportSchedule.id)
    if city_id is not None:
        stmt = stmt.where(ReportSchedule.city_id == city_id)
    return list(db.scalars(stmt))


def find_by_id(db: Session, schedule_id: int) -> ReportSchedule | None:
    return db.get(ReportSchedule, schedule_id)


def exists_by_id(db: Session, schedule_id: int) -> bool:
    return db.get(ReportSchedule, schedule_id) is not None


def save(db: Session, schedule: ReportSchedule) -> ReportSchedule:
    db.add(schedule)
    db.commit()
    db.refresh(schedule)
    return schedule


def delete(db: Session, schedule: ReportSchedule) -> None:
    db.delete(schedule)
    db.commit()
