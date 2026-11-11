"""Data access for Report (same role as a Spring Data repository)."""
from sqlalchemy import exists, select
from sqlalchemy.orm import Session

from app.models import Report


def find_all(db: Session, city_id: int | None = None) -> list[Report]:
    stmt = select(Report).order_by(Report.id)
    if city_id is not None:
        stmt = stmt.where(Report.city_id == city_id)
    return list(db.scalars(stmt))


def find_by_id(db: Session, report_id: int) -> Report | None:
    return db.get(Report, report_id)


def exists_by_schedule_id(db: Session, schedule_id: int) -> bool:
    return bool(db.scalar(select(exists().where(Report.schedule_id == schedule_id))))


def save(db: Session, report: Report) -> Report:
    db.add(report)
    db.commit()
    db.refresh(report)
    return report


def delete(db: Session, report: Report) -> None:
    db.delete(report)
    db.commit()
