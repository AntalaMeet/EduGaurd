from pydantic import BaseModel


class LoginRequest(BaseModel):
    username: str
    password: str


class LoginResponse(BaseModel):
    success: bool
    message: str
    role: str | None = None
    student_id: int | None = None
    faculty_id: int | None = None