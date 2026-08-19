from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from db import get_connection
from schemas import LoginRequest, LoginResponse
from ml_model import predict_dropout


app = FastAPI(
    title="EduGuard API",
    description="AI-Powered Academic Dropout Prediction and Intervention System",
    version="1.0.0"
)

class AddFacultyRequest(BaseModel):
    first_name: str
    last_name: str
    email: str
    division_id: int
    username: str
    password: str

class AdminStudent(BaseModel):
    student_id: int
    enrollment_number: str
    first_name: str
    last_name: str
    division_id: int | None
    division_code: str | None


class AdminStudentResponse(BaseModel):
    success: bool
    students: list[AdminStudent]

class AdminDivision(BaseModel):
    division_id: int
    division_code: str
    start_enrollment: int
    end_enrollment: int

class FacultyRiskStudent(BaseModel):
    student_id: int
    enrollment_number: str
    first_name: str
    last_name: str

    attendance: float
    academic: float
    study: float
    assignments: float

    dropout_risk: float
    risk_level: str


class FacultyRiskResponse(BaseModel):
    success: bool

    mode: str

    division_id: int
    division_code: str

    subject_id: int | None = None
    subject_code: str | None = None
    subject_name: str | None = None

    total_students: int

    average_attendance: float
    average_academic: float

    high_risk: int
    medium_risk: int
    low_risk: int

    students: list[FacultyRiskStudent]

class AdminDivisionResponse(BaseModel):
    success: bool
    divisions: list[AdminDivision]

def risk_level(risk):
    if risk < 30:
        return "LOW"
    if risk < 60:
        return "MEDIUM"
    return "HIGH"


def recommendation(metrics):
    if metrics["attendance"] < 75:
        return "Your attendance is below 75%. Try to attend classes regularly."

    if metrics["academic"] < 50:
        return "Your academic performance needs improvement. Consider meeting your faculty mentor."

    if metrics["study"] < 50:
        return "Your study activity is low. Try to increase your weekly study time."

    return "Your academic indicators are healthy. Keep maintaining your current performance."


def get_student_metrics(
    cursor,
    student_id,
    subject_id=None
):
    """
    Fetch the student's data from SQL and calculate
    dashboard / ML features.

    If subject_id is provided:
        - Attendance = selected subject only
        - Academic = selected subject only
        - Learning behavior = overall
        - Assignments = overall for now

    If subject_id is None:
        - All metrics are calculated overall.
    """

    # =========================================================
    # ATTENDANCE
    # =========================================================

    if subject_id is not None:

        cursor.execute(
            """
            SELECT
                COUNT(*),
                NVL(
                    SUM(
                        CASE
                            WHEN present = 1
                            THEN 1
                            ELSE 0
                        END
                    ),
                    0
                )
            FROM attendance
            WHERE student_id = :student_id
              AND subject_id = :subject_id
            """,
            student_id=student_id,
            subject_id=subject_id
        )

    else:

        cursor.execute(
            """
            SELECT
                COUNT(*),
                NVL(
                    SUM(
                        CASE
                            WHEN present = 1
                            THEN 1
                            ELSE 0
                        END
                    ),
                    0
                )
            FROM attendance
            WHERE student_id = :student_id
            """,
            student_id=student_id
        )

    row = cursor.fetchone()

    attendance_total = int(
        row[0] or 0
    )

    attendance_present = int(
        row[1] or 0
    )

    attendance = round(
        attendance_present /
        attendance_total * 100,
        2
    ) if attendance_total > 0 else 0


    # =========================================================
    # ACADEMIC PERFORMANCE
    # =========================================================

    if subject_id is not None:

        cursor.execute(
            """
            SELECT
                NVL(SUM(marks), 0),
                NVL(SUM(max_marks), 0)
            FROM marks
            WHERE student_id = :student_id
              AND subject_id = :subject_id
            """,
            student_id=student_id,
            subject_id=subject_id
        )

    else:

        cursor.execute(
            """
            SELECT
                NVL(SUM(marks), 0),
                NVL(SUM(max_marks), 0)
            FROM marks
            WHERE student_id = :student_id
            """,
            student_id=student_id
        )

    row = cursor.fetchone()

    total_marks = float(
        row[0] or 0
    )

    total_max_marks = float(
        row[1] or 0
    )

    academic = round(
        total_marks /
        total_max_marks * 100,
        2
    ) if total_max_marks > 0 else 0


    # =========================================================
    # LEARNING BEHAVIOR
    # =========================================================

    cursor.execute(
        """
        SELECT
            NVL(AVG(lms_logins), 0),
            NVL(AVG(study_hours), 0),
            NVL(AVG(videos_watched), 0),
            NVL(AVG(resources_accessed), 0)
        FROM learning_behavior
        WHERE student_id = :student_id
        """,
        student_id=student_id
    )

    row = cursor.fetchone()

    avg_logins = round(
        float(row[0] or 0),
        2
    )

    avg_study_hours = round(
        float(row[1] or 0),
        2
    )

    avg_videos = round(
        float(row[2] or 0),
        2
    )

    avg_resources = round(
        float(row[3] or 0),
        2
    )


    study = round(
        min(avg_logins / 10, 1) * 25
        + min(avg_study_hours / 5, 1) * 35
        + min(avg_videos / 10, 1) * 20
        + min(avg_resources / 10, 1) * 20,
        2
    )


    # =========================================================
    # ASSIGNMENT COMPLETION
    # =========================================================

    if subject_id is not None:

        cursor.execute(
            """
            SELECT
                COUNT(*),
                NVL(
                    SUM(
                        CASE
                            WHEN UPPER(s.status)
                                IN ('SUBMITTED', 'LATE')
                            THEN 1
                            ELSE 0
                        END
                    ),
                    0
                ),
                NVL(AVG(s.score), 0)
            FROM assignment_submissions s
            JOIN assignments a
                ON a.assignment_id = s.assignment_id
            WHERE s.student_id = :student_id
            AND a.subject_id = :subject_id
            """,
            student_id=student_id,
            subject_id=subject_id
        )

    else:

        cursor.execute(
            """
            SELECT
                COUNT(*),
                NVL(
                    SUM(
                        CASE
                            WHEN UPPER(status)
                                IN ('SUBMITTED', 'LATE')
                            THEN 1
                            ELSE 0
                        END
                    ),
                    0
                ),
                NVL(AVG(score), 0)
            FROM assignment_submissions
            WHERE student_id = :student_id
            """,
            student_id=student_id
        )

    row = cursor.fetchone()

    assignment_total = int(row[0] or 0)

    assignment_completed = int(row[1] or 0)

    average_assignment_score = round(
        float(row[2] or 0),
        2
    )

    assignment_completion = round(
        assignment_completed /
        assignment_total * 100,
        2
    ) if assignment_total > 0 else 0

    assignment_score = assignment_completion

    # =========================================================
    # RETURN
    # =========================================================

    return {

        "attendance":
            attendance,

        "attendance_total":
            attendance_total,

        "attendance_present":
            attendance_present,

        "attendance_absent":
            attendance_total -
            attendance_present,


        "academic":
            academic,


        "study":
            study,

        "avg_logins":
            avg_logins,

        "avg_study_hours":
            avg_study_hours,

        "avg_videos":
            avg_videos,

        "avg_resources":
            avg_resources,


        "assignments":
            assignment_score,

        "assignment_total":
            assignment_total,

        "assignment_submitted":
            assignment_completed,

        "assignment_completion":
            assignment_completion,

        "average_assignment_score":
            average_assignment_score
    }


def get_prediction(metrics):
    """Run the ML model using the common student features."""

    return predict_dropout(
        attendance=metrics["attendance"],
        academic=metrics["academic"],
        study=metrics["study"],
        assignments=metrics["assignments"]
    )


def get_student_profile(cursor, student_id, include_division=False):
    columns = """
        student_id,
        enrollment_number,
        first_name,
        last_name
    """

    if include_division:
        columns += ", division_id"

    cursor.execute(
        f"""
        SELECT {columns}
        FROM students
        WHERE student_id = :student_id
        """,
        student_id=student_id
    )

    row = cursor.fetchone()

    if row is None:
        raise HTTPException(status_code=404, detail="Student not found")

    profile = {
        "student_id": row[0],
        "enrollment_number": row[1],
        "first_name": row[2],
        "last_name": row[3]
    }

    if include_division:
        profile["division_id"] = row[4]

    return profile


def close_db(connection, cursor):
    if cursor:
        cursor.close()

    if connection:
        connection.close()




@app.get("/")
def home():
    return {
        "message": "EduGuard API is running"
    }

@app.get("/student/{student_id}/performance-trend")
def student_performance_trend(student_id: int):

    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        # =====================================================
        # 1. CHECK STUDENT
        # =====================================================

        cursor.execute(
            """
            SELECT
                student_id,
                first_name,
                last_name
            FROM students
            WHERE student_id = :student_id
            """,
            student_id=student_id
        )

        student_row = cursor.fetchone()

        if student_row is None:
            raise HTTPException(
                status_code=404,
                detail="Student not found"
            )

        # =====================================================
        # 2. ATTENDANCE TREND
        # =====================================================

        cursor.execute(
            """
            SELECT
                TRUNC(attendance_date, 'IW') AS week_start,

                ROUND(
                    SUM(
                        CASE
                            WHEN present = 1 THEN 1
                            ELSE 0
                        END
                    ) * 100 /
                    NULLIF(COUNT(*), 0),
                    2
                ) AS attendance

            FROM attendance

            WHERE student_id = :student_id

            GROUP BY
                TRUNC(attendance_date, 'IW')

            ORDER BY
                week_start
            """,
            student_id=student_id
        )

        attendance_rows = cursor.fetchall()

        # =====================================================
        # 3. ACADEMIC PERFORMANCE TREND
        # =====================================================
        # Marks are connected to EXAMS.
        # EXAMS provides the actual exam date.

        cursor.execute(
            """
            SELECT
                TRUNC(e.exam_date, 'IW') AS week_start,

                ROUND(
                    SUM(m.marks) * 100 /
                    NULLIF(SUM(m.max_marks), 0),
                    2
                ) AS academic

            FROM marks m

            JOIN exams e
                ON e.exam_id = m.exam_id

            WHERE m.student_id = :student_id

            GROUP BY
                TRUNC(e.exam_date, 'IW')

            ORDER BY
                week_start
            """,
            student_id=student_id
        )

        academic_rows = cursor.fetchall()

        # =====================================================
        # 4. ASSIGNMENT COMPLETION TREND
        # =====================================================

        cursor.execute(
            """
            SELECT
                TRUNC(a.due_date, 'IW') AS week_start,

                ROUND(
                    SUM(
                        CASE
                            WHEN UPPER(s.status)
                                 IN ('SUBMITTED', 'LATE')
                            THEN 1
                            ELSE 0
                        END
                    ) * 100 /
                    NULLIF(COUNT(*), 0),
                    2
                ) AS assignments

            FROM assignment_submissions s

            JOIN assignments a
                ON a.assignment_id = s.assignment_id

            WHERE s.student_id = :student_id

            GROUP BY
                TRUNC(a.due_date, 'IW')

            ORDER BY
                week_start
            """,
            student_id=student_id
        )

        assignment_rows = cursor.fetchall()

        # =====================================================
        # 5. COMBINE ALL DATA BY WEEK
        # =====================================================

        trend_map = {}

        # -------------------------
        # Attendance
        # -------------------------

        for row in attendance_rows:

            week = row[0]

            if week not in trend_map:
                trend_map[week] = {}

            trend_map[week]["attendance"] = float(
                row[1] or 0
            )

        # -------------------------
        # Academic
        # -------------------------

        for row in academic_rows:

            week = row[0]

            if week not in trend_map:
                trend_map[week] = {}

            trend_map[week]["academic"] = float(
                row[1] or 0
            )

        # -------------------------
        # Assignments
        # -------------------------

        for row in assignment_rows:

            week = row[0]

            if week not in trend_map:
                trend_map[week] = {}

            trend_map[week]["assignments"] = float(
                row[1] or 0
            )

        # =====================================================
        # 6. BUILD TREND RESPONSE
        # =====================================================

        trend = []

        for week in sorted(trend_map.keys()):

            values = trend_map[week]

            trend.append(
                {
                    "week": week.strftime("%Y-%m-%d"),

                    "attendance":
                        values.get("attendance"),

                    "academic":
                        values.get("academic"),

                    "assignments":
                        values.get("assignments")
                }
            )

        # =====================================================
        # 7. DETERMINE INDIVIDUAL TRENDS
        # =====================================================

        def calculate_trend(values):

            # Remove only NULL values.
            # A real 0 is kept because 0% is valid data.
            values = [
                float(value)
                for value in values
                if value is not None
            ]

            # Not enough data to determine a trend
            if len(values) < 2:
                return "STABLE"

            first_value = values[0]
            last_value = values[-1]

            difference = last_value - first_value

            if difference >= 5:
                return "IMPROVING"

            elif difference <= -5:
                return "DECLINING"

            else:
                return "STABLE"


        attendance_values = [
            item["attendance"]
            for item in trend
        ]

        academic_values = [
            item["academic"]
            for item in trend
        ]

        assignment_values = [
            item["assignments"]
            for item in trend
        ]


        attendance_trend = calculate_trend(
            attendance_values
        )

        academic_trend = calculate_trend(
            academic_values
        )

        assignment_trend = calculate_trend(
            assignment_values
        )
        # =====================================================
        # 8. RESPONSE
        # =====================================================

        return {
            "success": True,

            "student": {
                "student_id": student_row[0],
                "first_name": student_row[1],
                "last_name": student_row[2]
            },

            "attendance_trend": attendance_trend,

            "academic_trend": academic_trend,

            "assignment_trend": assignment_trend,

            "trend": trend
        }

    except HTTPException:
        raise

    except Exception as e:

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

    finally:

        close_db(
            connection,
            cursor
        )   


@app.get("/test-db")
def test_database():
    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        cursor.execute("SELECT 1 FROM dual")
        result = cursor.fetchone()

        return {
            "success": True,
            "message": "Oracle database connected",
            "result": result[0]
        }

    finally:
        close_db(connection, cursor)


@app.post("/login", response_model=LoginResponse)
def login(data: LoginRequest):
    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        cursor.execute(
            """
            SELECT
                user_id,
                username,
                password_hash,
                role,
                student_id,
                faculty_id
            FROM users
            WHERE username = :username
            """,
            username=data.username
        )

        user = cursor.fetchone()

        if user is None or data.password != user[2]:
            raise HTTPException(
                status_code=401,
                detail="Invalid username or password"
            )

        return LoginResponse(
            success=True,
            message="Login successful",
            role=user[3],
            student_id=user[4],
            faculty_id=user[5]
        )

    finally:
        close_db(connection, cursor)


@app.get("/students/{student_id}/dashboard")
def student_dashboard(student_id: int):
    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        student = get_student_profile(
            cursor,
            student_id,
            include_division=True
        )

        metrics = get_student_metrics(cursor, student_id)
        prediction = get_prediction(metrics)

        dropout_risk = prediction["dropout_risk"]

        return {
            "student": student,

            "overall": {
                "dropout_risk": dropout_risk,
                "risk_level": risk_level(dropout_risk)
            },

            "attendance": {
                "percentage": metrics["attendance"],
                "total": metrics["attendance_total"],
                "present": metrics["attendance_present"]
            },

            "academic": {
                "score": metrics["academic"]
            },

            "study": {
                "score": metrics["study"]
            },

            "assignments": {
                "score": metrics["assignments"]
            },

            "recommendation": recommendation(metrics)
        }

    except HTTPException:
        raise

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        close_db(connection, cursor)




@app.get("/students/{student_id}/detailed-analysis")
def detailed_analysis(student_id: int):
    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        student = get_student_profile(cursor, student_id)
        metrics = get_student_metrics(cursor, student_id)

        # Subject-wise marks
        cursor.execute(
            """
            SELECT
                s.subject_id,
                s.subject_name,

                NVL(MAX(CASE WHEN m.exam_id = 1 THEN m.marks END), 0),
                NVL(MAX(CASE WHEN m.exam_id = 1 THEN m.max_marks END), 0),

                NVL(MAX(CASE WHEN m.exam_id = 2 THEN m.marks END), 0),
                NVL(MAX(CASE WHEN m.exam_id = 2 THEN m.max_marks END), 0),

                NVL(MAX(CASE WHEN m.exam_id = 3 THEN m.marks END), 0),
                NVL(MAX(CASE WHEN m.exam_id = 3 THEN m.max_marks END), 0)

            FROM subjects s
            LEFT JOIN marks m
                ON m.subject_id = s.subject_id
                AND m.student_id = :student_id

            GROUP BY
                s.subject_id,
                s.subject_name

            ORDER BY
                s.subject_id
            """,
            student_id=student_id
        )

        rows = cursor.fetchall()
        subjects = []

        for row in rows:
            exam1 = float(row[2] or 0)
            exam1_max = float(row[3] or 0)
            exam2 = float(row[4] or 0)
            exam2_max = float(row[5] or 0)
            exam3 = float(row[6] or 0)
            exam3_max = float(row[7] or 0)

            subject_marks = exam1 + exam2 + exam3
            subject_max = exam1_max + exam2_max + exam3_max

            percentage = round(
                subject_marks / subject_max * 100,
                2
            ) if subject_max > 0 else 0

            subjects.append({
                "subject_id": row[0],
                "subject_name": row[1],
                "exam1": exam1,
                "exam1_max": exam1_max,
                "exam2": exam2,
                "exam2_max": exam2_max,
                "exam3": exam3,
                "exam3_max": exam3_max,
                "percentage": percentage
            })

        return {
            "student": student,

            "overall_academic": metrics["academic"],

            "attendance": {
                "percentage": metrics["attendance"],
                "total": metrics["attendance_total"],
                "present": metrics["attendance_present"],
                "absent": metrics["attendance_absent"]
            },

            "study": {
                "score": metrics["study"],
                "avg_logins": metrics["avg_logins"],
                "avg_study_hours": metrics["avg_study_hours"],
                "avg_videos": metrics["avg_videos"],
                "avg_resources": metrics["avg_resources"]
            },

            "assignments": {
                "score": metrics["assignments"],
                "total": metrics["assignment_total"],
                "submitted": metrics["assignment_submitted"],
                "completion": metrics["assignment_completion"],
                "average_score": metrics["average_assignment_score"]
            },

            "subjects": subjects
        }

    except HTTPException:
        raise

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        close_db(connection, cursor)


@app.get("/students/{student_id}/prediction")
def student_prediction(student_id: int):
    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        metrics = get_student_metrics(cursor, student_id)
        prediction = get_prediction(metrics)

        dropout_risk = prediction["dropout_risk"]

        return {
            "student_id": student_id,

            "features": {
                "academic": metrics["academic"],
                "attendance": metrics["attendance"],
                "study": metrics["study"],
                "assignments": metrics["assignments"]
            },

            "prediction": {
                "dropout_risk": dropout_risk,
                "risk_level": risk_level(dropout_risk),
                "model_prediction": prediction["dropped_out_prediction"]
            }
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        close_db(connection, cursor)




@app.get("/faculty/{faculty_id}/dashboard")
def faculty_dashboard(faculty_id: int):

    connection = None
    cursor = None

    try:

        connection = get_connection()
        cursor = connection.cursor()

        # =========================================================
        # 1. FACULTY INFORMATION
        # =========================================================

        cursor.execute(
            """
            SELECT
                faculty_id,
                first_name,
                last_name,
                email
            FROM faculty
            WHERE faculty_id = :faculty_id
            """,
            faculty_id=faculty_id
        )

        faculty_row = cursor.fetchone()

        if faculty_row is None:
            raise HTTPException(
                status_code=404,
                detail="Faculty not found"
            )


        # =========================================================
        # 2. SUBJECT ASSIGNMENTS
        # =========================================================

        cursor.execute(
            """
            SELECT
                fs.assignment_id,
                fs.division_id,
                d.division_code,
                fs.subject_id,
                s.subject_code,
                s.subject_name
            FROM faculty_subjects fs

            JOIN divisions d
                ON d.division_id = fs.division_id

            JOIN subjects s
                ON s.subject_id = fs.subject_id

            WHERE fs.faculty_id = :faculty_id

            ORDER BY
                fs.division_id,
                fs.subject_id
            """,
            faculty_id=faculty_id
        )

        assignment_rows = cursor.fetchall()


        assignments = []

        for row in assignment_rows:

            assignments.append({
                "assignment_id": row[0],
                "division_id": row[1],
                "division_code": row[2],
                "subject_id": row[3],
                "subject_code": row[4],
                "subject_name": row[5]
            })


        # =========================================================
        # 3. CLASS MENTOR ASSIGNMENTS
        # =========================================================

        cursor.execute(
            """
            SELECT
                cm.mentor_id,
                cm.division_id,
                d.division_code
            FROM class_mentors cm

            JOIN divisions d
                ON d.division_id = cm.division_id

            WHERE cm.faculty_id = :faculty_id

            ORDER BY cm.division_id
            """,
            faculty_id=faculty_id
        )

        mentor_rows = cursor.fetchall()


        mentor_divisions = []

        for row in mentor_rows:

            mentor_divisions.append({
                "mentor_id": row[0],
                "division_id": row[1],
                "division_code": row[2]
            })


        # =========================================================
        # 4. FIND ALL DIVISIONS FACULTY CAN ACCESS
        #
        #    Subject faculty:
        #       FACULTY_SUBJECTS
        #
        #    Class mentor:
        #       CLASS_MENTORS
        #
        #    Using a SET prevents duplicate divisions.
        # =========================================================

        accessible_divisions = set()

        for row in assignment_rows:

            accessible_divisions.add(
                row[1]
            )

        for row in mentor_rows:

            accessible_divisions.add(
                row[1]
            )


        # =========================================================
        # 5. GET STUDENTS FROM ALL ACCESSIBLE DIVISIONS
        # =========================================================

        students = []

        total_attendance = 0
        total_academic = 0

        high_risk = 0
        medium_risk = 0
        low_risk = 0


        for division_id in accessible_divisions:

            cursor.execute(
                """
                SELECT
                    student_id,
                    enrollment_number,
                    first_name,
                    last_name,
                    division_id
                FROM students
                WHERE division_id = :division_id
                ORDER BY student_id
                """,
                division_id=division_id
            )

            student_rows = cursor.fetchall()


            for row in student_rows:

                student_id = row[0]

                # -------------------------------------------------
                # Prevent duplicate students
                # -------------------------------------------------

                if any(
                    student["student_id"] == student_id
                    for student in students
                ):
                    continue


                # -------------------------------------------------
                # Student metrics
                # -------------------------------------------------

                metrics =get_student_metrics(
                        cursor,
                        student_id
                    )

                prediction =get_prediction(
                        metrics
                    )


                dropout_risk =prediction["dropout_risk"]

                level =risk_level(
                        dropout_risk
                    )


                # -------------------------------------------------
                # Risk counters
                # -------------------------------------------------

                if level == "HIGH":

                    high_risk += 1

                elif level == "MEDIUM":

                    medium_risk += 1

                else:

                    low_risk += 1


                # -------------------------------------------------
                # Student object
                # -------------------------------------------------

                students.append({

                    "student_id":
                        student_id,

                    "enrollment_number":
                        row[1],

                    "first_name":
                        row[2],

                    "last_name":
                        row[3],

                    "division_id":
                        row[4],

                    "attendance":
                        metrics["attendance"],

                    "academic":
                        metrics["academic"],

                    "study":
                        metrics["study"],

                    "assignments":
                        metrics["assignments"],

                    "dropout_risk":
                        dropout_risk,

                    "risk_level":
                        level
                })


                total_attendance += \
                    metrics["attendance"]

                total_academic += \
                    metrics["academic"]


        # =========================================================
        # 6. AVERAGES
        # =========================================================

        student_count = len(students)


        average_attendance = (
            round(
                total_attendance /
                student_count,
                2
            )
            if student_count > 0
            else 0
        )


        average_academic = (
            round(
                total_academic /
                student_count,
                2
            )
            if student_count > 0
            else 0
        )


        # =========================================================
        # 7. FACULTY RESPONSE
        # =========================================================

        faculty = {

            "faculty_id":
                faculty_row[0],

            "first_name":
                faculty_row[1],

            "last_name":
                faculty_row[2],

            "email":
                faculty_row[3]
        }


        # =========================================================
        # 8. FINAL RESPONSE
        # =========================================================

        return {

            "faculty":
                faculty,

            "assignments":
                assignments,

            "mentor_divisions":
                mentor_divisions,

            "overview": {

                "total_students":
                    student_count,

                "average_attendance":
                    average_attendance,

                "average_academic":
                    average_academic,

                "high_risk":
                    high_risk,

                "medium_risk":
                    medium_risk,

                "low_risk":
                    low_risk
            },

            "students":
                students
        }


    except HTTPException:

        raise


    except Exception as e:

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )


    finally:

        close_db(
            connection,
            cursor
        )

@app.get(
    "/faculty/{faculty_id}/risk",
    response_model=FacultyRiskResponse
)
def faculty_risk_dashboard(
    faculty_id: int,
    division_id: int,
    subject_id: int | None = None
):

    connection = None
    cursor = None

    try:

        connection = get_connection()
        cursor = connection.cursor()

        # =====================================================
        # 1. CHECK FACULTY
        # =====================================================

        cursor.execute(
            """
            SELECT
                faculty_id
            FROM faculty
            WHERE faculty_id = :faculty_id
            """,
            faculty_id=faculty_id
        )

        faculty_row = cursor.fetchone()

        if faculty_row is None:

            raise HTTPException(
                status_code=404,
                detail="Faculty not found"
            )


        # =====================================================
        # 2. CHECK DIVISION
        # =====================================================

        cursor.execute(
            """
            SELECT
                division_id,
                division_code
            FROM divisions
            WHERE division_id = :division_id
            """,
            division_id=division_id
        )

        division_row = cursor.fetchone()

        if division_row is None:

            raise HTTPException(
                status_code=404,
                detail="Division not found"
            )


        division_code = division_row[1]


        # =====================================================
        # 3. IF SUBJECT IS PROVIDED
        # =====================================================

        subject_code = None
        subject_name = None

        if subject_id is not None:

            cursor.execute(
                """
                SELECT
                    subject_id,
                    subject_code,
                    subject_name
                FROM subjects
                WHERE subject_id = :subject_id
                """,
                subject_id=subject_id
            )

            subject_row = cursor.fetchone()

            if subject_row is None:

                raise HTTPException(
                    status_code=404,
                    detail="Subject not found"
                )

            subject_code = subject_row[1]
            subject_name = subject_row[2]


        # =====================================================
        # 4. GET STUDENTS
        # =====================================================

        cursor.execute(
            """
            SELECT
                student_id,
                enrollment_number,
                first_name,
                last_name
            FROM students
            WHERE division_id = :division_id
            ORDER BY student_id
            """,
            division_id=division_id
        )

        student_rows = cursor.fetchall()


        students = []

        total_attendance = 0
        total_academic = 0

        high_risk = 0
        medium_risk = 0
        low_risk = 0


        # =====================================================
        # 5. CALCULATE RISK
        # =====================================================

        for row in student_rows:

            student_id = row[0]

            metrics = get_student_metrics(
                cursor,
                student_id,
                subject_id
            )

            prediction = get_prediction(
                metrics
            )

            dropout_risk = prediction[
                "dropout_risk"
            ]

            level = risk_level(
                dropout_risk
            )


            if level == "HIGH":

                high_risk += 1

            elif level == "MEDIUM":

                medium_risk += 1

            else:

                low_risk += 1


            students.append(
                FacultyRiskStudent(
                    student_id=student_id,
                    enrollment_number=row[1],
                    first_name=row[2],
                    last_name=row[3],
                    attendance=metrics["attendance"],
                    academic=metrics["academic"],
                    study=metrics["study"],
                    assignments=metrics["assignments"],
                    dropout_risk=dropout_risk,
                    risk_level=level
                )
            )


            total_attendance += \
                metrics["attendance"]

            total_academic += \
                metrics["academic"]


        # =====================================================
        # 6. SORT HIGHEST RISK FIRST
        # =====================================================

        students.sort(
            key=lambda student:
                student.dropout_risk,
            reverse=True
        )


        total_students = len(
            students
        )


        average_attendance = (
            round(
                total_attendance /
                total_students,
                2
            )
            if total_students > 0
            else 0
        )


        average_academic = (
            round(
                total_academic /
                total_students,
                2
            )
            if total_students > 0
            else 0
        )


        # =====================================================
        # 7. RESPONSE
        # =====================================================

        return FacultyRiskResponse(

            success=True,

            mode=(
                "SUBJECT"
                if subject_id is not None
                else "DIVISION"
            ),

            division_id=division_id,

            division_code=division_code,

            subject_id=subject_id,

            subject_code=subject_code,

            subject_name=subject_name,

            total_students=total_students,

            average_attendance=
                average_attendance,

            average_academic=
                average_academic,

            high_risk=high_risk,

            medium_risk=medium_risk,

            low_risk=low_risk,

            students=students
        )


    except HTTPException:

        raise


    except Exception as e:

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )


    finally:

        close_db(
            connection,
            cursor
        )



@app.get("/admin/dashboard")
def admin_dashboard():
    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        cursor.execute(
            """
            SELECT
                (SELECT COUNT(*) FROM students),
                (SELECT COUNT(*) FROM faculty),
                (SELECT COUNT(*) FROM divisions),
                (SELECT COUNT(*) FROM users)
            FROM dual
            """
        )

        row = cursor.fetchone()

        return {
            "total_students": row[0] or 0,
            "total_faculty": row[1] or 0,
            "total_divisions": row[2] or 0,
            "total_users": row[3] or 0
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        close_db(connection, cursor)



@app.get("/admin/faculty")
def get_all_faculty():

    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        # =====================================================
        # GET FACULTY
        # =====================================================

        cursor.execute(
            """
            SELECT
                f.faculty_id,
                f.first_name,
                f.last_name,
                f.email,
                f.division_id,
                d.division_code
            FROM faculty f
            LEFT JOIN divisions d
                ON f.division_id = d.division_id
            ORDER BY f.faculty_id
            """
        )

        faculty_rows = cursor.fetchall()

        # =====================================================
        # GET FACULTY SUBJECT ASSIGNMENTS
        # =====================================================

        cursor.execute(
            """
            SELECT
                fs.assignment_id,
                fs.faculty_id,
                fs.division_id,
                d.division_code,
                fs.subject_id,
                s.subject_code,
                s.subject_name
            FROM faculty_subjects fs
            JOIN divisions d
                ON d.division_id = fs.division_id
            JOIN subjects s
                ON s.subject_id = fs.subject_id
            ORDER BY
                fs.faculty_id,
                fs.division_id,
                fs.subject_id
            """
        )

        assignment_rows = cursor.fetchall()

        # =====================================================
        # CREATE ASSIGNMENT MAP
        # =====================================================

        assignment_map = {}

        for row in assignment_rows:

            assignment = {
                "assignment_id": row[0],
                "division_id": row[2],
                "division_code": row[3],
                "subject_id": row[4],
                "subject_code": row[5],
                "subject_name": row[6]
            }

            faculty_id = row[1]

            if faculty_id not in assignment_map:
                assignment_map[faculty_id] = []

            assignment_map[faculty_id].append(
                assignment
            )

        # =====================================================
        # BUILD RESPONSE
        # =====================================================

        faculty = []

        for row in faculty_rows:

            faculty_id = row[0]

            faculty.append(
                {
                    "faculty_id": faculty_id,
                    "first_name": row[1],
                    "last_name": row[2],
                    "email": row[3],
                    "division_id": row[4],
                    "division_code": row[5],

                    "assignments": assignment_map.get(
                        faculty_id,
                        []
                    )
                }
            )

        return {
            "success": True,
            "faculty": faculty
        }

    except Exception as e:

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

    finally:

        close_db(
            connection,
            cursor
        )
        
@app.post("/admin/faculty")
def add_faculty(data: AddFacultyRequest):

    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        # --------------------------------
        # CHECK USERNAME
        # --------------------------------

        cursor.execute(
            """
            SELECT COUNT(*)
            FROM users
            WHERE username = :username
            """,
            username=data.username
        )

        if cursor.fetchone()[0] > 0:

            raise HTTPException(
                status_code=400,
                detail="Username already exists"
            )

        # --------------------------------
        # CHECK DIVISION
        # --------------------------------

        cursor.execute(
            """
            SELECT COUNT(*)
            FROM divisions
            WHERE division_id = :division_id
            """,
            division_id=data.division_id
        )

        if cursor.fetchone()[0] == 0:

            raise HTTPException(
                status_code=400,
                detail="Invalid division"
            )

        # --------------------------------
        # GENERATE FACULTY ID
        # --------------------------------

        cursor.execute(
            """
            SELECT NVL(MAX(faculty_id), 0) + 1
            FROM faculty
            """
        )

        faculty_id = cursor.fetchone()[0]

        # --------------------------------
        # GENERATE USER ID
        # --------------------------------

        cursor.execute(
            """
            SELECT NVL(MAX(user_id), 0) + 1
            FROM users
            """
        )

        user_id = cursor.fetchone()[0]

        # --------------------------------
        # INSERT FACULTY
        # --------------------------------

        cursor.execute(
            """
            INSERT INTO faculty (
                faculty_id,
                first_name,
                last_name,
                email,
                division_id
            )
            VALUES (
                :faculty_id,
                :first_name,
                :last_name,
                :email,
                :division_id
            )
            """,
            faculty_id=faculty_id,
            first_name=data.first_name,
            last_name=data.last_name,
            email=data.email,
            division_id=data.division_id
        )

        # --------------------------------
        # INSERT USER ACCOUNT
        # --------------------------------

        cursor.execute(
            """
            INSERT INTO users (
                user_id,
                username,
                password_hash,
                role,
                student_id,
                created_at,
                faculty_id
            )
            VALUES (
                :user_id,
                :username,
                :password_hash,
                'FACULTY',
                NULL,
                SYSDATE,
                :faculty_id
            )
            """,
            user_id=user_id,
            username=data.username,
            password_hash=data.password,
            faculty_id=faculty_id
        )

        # --------------------------------
        # COMMIT BOTH
        # --------------------------------

        connection.commit()

        return {
            "success": True,
            "message": "Faculty added successfully",
            "faculty_id": faculty_id,
            "user_id": user_id
        }

    except HTTPException:
        if connection:
            connection.rollback()
        raise

    except Exception as e:

        if connection:
            connection.rollback()

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

    finally:

        if cursor:
            cursor.close()

        if connection:
            connection.close()

@app.get(
    "/admin/students",
    response_model=AdminStudentResponse
)
def get_admin_students():

    connection = None
    cursor = None

    try:

        connection = get_connection()
        cursor = connection.cursor()

        cursor.execute(
            """
            SELECT
                s.student_id,
                s.enrollment_number,
                s.first_name,
                s.last_name,
                s.division_id,
                d.division_code
            FROM students s
            LEFT JOIN divisions d
                ON s.division_id = d.division_id
            ORDER BY s.enrollment_number
            """
        )

        rows = cursor.fetchall()

        students = []

        for row in rows:

            students.append(
                AdminStudent(
                    student_id=row[0],
                    enrollment_number=row[1],
                    first_name=row[2],
                    last_name=row[3],
                    division_id=row[4],
                    division_code=row[5]
                )
            )

        return AdminStudentResponse(
            success=True,
            students=students
        )

    except Exception as e:

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

    finally:

        if cursor:
            cursor.close()

        if connection:
            connection.close()

@app.get(
    "/admin/divisions",
    response_model=AdminDivisionResponse
)
def get_admin_divisions():

    connection = None
    cursor = None

    try:

        connection = get_connection()
        cursor = connection.cursor()

        cursor.execute(
            """
            SELECT
                division_id,
                division_code,
                start_enrollment,
                end_enrollment
            FROM divisions
            ORDER BY division_id
            """
        )

        rows = cursor.fetchall()

        divisions = []

        for row in rows:

            divisions.append(
                AdminDivision(
                    division_id=row[0],
                    division_code=row[1],
                    start_enrollment=row[2],
                    end_enrollment=row[3]
                )
            )

        return AdminDivisionResponse(
            success=True,
            divisions=divisions
        )

    except Exception as e:

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

    finally:

        if cursor:
            cursor.close()

        if connection:
            connection.close()