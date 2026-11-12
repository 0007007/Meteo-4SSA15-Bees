"""REST endpoints for Report (same role as a @RestController)."""
from typing import Annotated

from fastapi import APIRouter, Depends, Query, Response, status
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas import ReportIn, ReportOut
from app.services import report_service

router = APIRouter(prefix="/api/reports", tags=["reports"])
Db = Annotated[Session, Depends(get_db)]


@router.get("", response_model=list[ReportOut])
def get_all(db: Db, city_id: Annotated[int | None, Query(alias="cityId")] = None):
    return report_service.find_all(db, city_id)


@router.get("/{report_id}", response_model=ReportOut)
def get_by_id(report_id: int, db: Db):
    return report_service.find_by_id(db, report_id)


@router.post("", response_model=ReportOut, status_code=status.HTTP_201_CREATED)
def create(data: ReportIn, db: Db):
    return report_service.create(db, data)


@router.put("/{report_id}", response_model=ReportOut)
def update(report_id: int, data: ReportIn, db: Db):
    return report_service.update(db, report_id, data)


@router.delete("/{report_id}", status_code=status.HTTP_204_NO_CONTENT, response_class=Response)
def delete(report_id: int, db: Db):
    report_service.delete(db, report_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post("/{report_id}/generate", response_model=ReportOut)
def generate(report_id: int, db: Db):
    """Fetches the data through the gateway and writes the CSV or PDF file."""
    return report_service.generate(db, report_id)


@router.get("/{report_id}/download")
def download(report_id: int, db: Db):
    path, file_name, media_type = report_service.get_file(db, report_id)
    return FileResponse(path, media_type=media_type, filename=file_name)
