"""REST endpoints for ReportSchedule."""
from typing import Annotated

from fastapi import APIRouter, Depends, Query, Response, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas import ReportOut, ReportScheduleIn, ReportScheduleOut
from app.services import schedule_service

router = APIRouter(prefix="/api/report-schedules", tags=["report schedules"])
Db = Annotated[Session, Depends(get_db)]


@router.get("", response_model=list[ReportScheduleOut])
def get_all(db: Db, city_id: Annotated[int | None, Query(alias="cityId")] = None):
    return schedule_service.find_all(db, city_id)


@router.get("/{schedule_id}", response_model=ReportScheduleOut)
def get_by_id(schedule_id: int, db: Db):
    return schedule_service.find_by_id(db, schedule_id)


@router.post("", response_model=ReportScheduleOut, status_code=status.HTTP_201_CREATED)
def create(data: ReportScheduleIn, db: Db):
    return schedule_service.create(db, data)


@router.put("/{schedule_id}", response_model=ReportScheduleOut)
def update(schedule_id: int, data: ReportScheduleIn, db: Db):
    return schedule_service.update(db, schedule_id, data)


@router.delete("/{schedule_id}", status_code=status.HTTP_204_NO_CONTENT, response_class=Response)
def delete(schedule_id: int, db: Db):
    schedule_service.delete(db, schedule_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post("/{schedule_id}/run", response_model=ReportOut, status_code=status.HTTP_201_CREATED)
def run_now(schedule_id: int, db: Db):
    """Creates and generates the report for the last period of this schedule."""
    return schedule_service.run_now(db, schedule_id)
