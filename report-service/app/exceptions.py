"""Errors returned as JSON in the same shape as the Java services (RFC 9457 ProblemDetail)."""
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse


class NotFoundError(Exception):
    def __init__(self, resource: str, resource_id: object):
        super().__init__(f"{resource} not found with id {resource_id}")


class ConflictError(Exception):
    pass


def problem(status: int, title: str, detail: str, **extra) -> JSONResponse:
    body = {"type": "about:blank", "title": title, "status": status, "detail": detail, **extra}
    return JSONResponse(status_code=status, content=body, media_type="application/problem+json")


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(NotFoundError)
    async def handle_not_found(request: Request, exc: NotFoundError):
        return problem(404, "Resource not found", str(exc))

    @app.exception_handler(ConflictError)
    async def handle_conflict(request: Request, exc: ConflictError):
        return problem(409, "Conflict", str(exc))

    @app.exception_handler(RequestValidationError)
    async def handle_validation(request: Request, exc: RequestValidationError):
        errors: dict[str, str] = {}
        for error in exc.errors():
            location = [str(part) for part in error.get("loc", ()) if part != "body"]
            errors[".".join(location) or "body"] = error.get("msg", "invalid value")
        return problem(400, "Validation failed", "One or more fields are invalid", errors=errors)
