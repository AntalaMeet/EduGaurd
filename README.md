# EduGrade
EDUGUARD — SIH PROJECT README

1. PROJECT OVERVIEW

EduGuard is an AI-powered student monitoring and early-warning system designed to help educational institutions identify students who may be at risk of poor academic performance or dropout.

The system combines attendance, academic performance, learning behaviour, and assignment completion to generate a student risk assessment and provide actionable insights for faculty and administrators.

2. PROBLEM STATEMENT

Educational institutions often identify struggling students only after their academic performance has already declined.

Traditional monitoring methods have several limitations:
- Student data is distributed across different systems.
- Manual monitoring is time-consuming.
- Early warning signs can be missed.
- Faculty may not have a consolidated view of student performance.
- Intervention is often reactive rather than preventive.

EduGuard addresses this problem by bringing student indicators together and identifying risk at an early stage.

3. PROPOSED SOLUTION

EduGuard provides a centralized platform for:
- Student performance monitoring
- Attendance analysis
- Academic performance analysis
- Learning behaviour analysis
- Assignment completion tracking
- AI/ML-based dropout risk prediction
- Faculty-level risk monitoring
- Division-wise analysis
- Subject-wise risk analysis
- Early intervention support
- Admin management

4. KEY FEATURES

STUDENT MODULE
- Personal dashboard
- Attendance percentage
- Academic health
- Learning behaviour
- Assignment performance
- Dropout risk prediction
- Detailed student analysis
- Performance trend visualization
- Recommendations for improvement

FACULTY MODULE
- Faculty dashboard
- My Division
- Subject-wise monitoring
- Student risk classification
- High / Medium / Low risk categories
- Student-level risk details
- Performance metrics
- Attendance and academic analysis
- Subject-specific monitoring

ADMIN MODULE
- Admin dashboard
- Faculty management
- Student management
- Division management
- Faculty-subject assignment
- Faculty-division assignment
- Centralized institutional monitoring

5. RISK CLASSIFICATION

HIGH
Immediate attention and intervention.

MEDIUM
Requires monitoring and support.

LOW
Normal monitoring.

Faculty can click the risk cards to filter students according to their risk category.

6. STUDENT RISK INDICATORS

- Attendance
- Academic performance
- Learning behaviour
- Assignment completion

These indicators are combined for risk prediction and early intervention.

7. PERFORMANCE TREND

EduGuard provides historical performance visualization using real database records.

The trend can display:
- Attendance
- Academic performance
- Assignment completion

The system identifies whether individual indicators are:
- Improving
- Declining
- Stable

Missing historical data is treated separately from genuine zero performance.

8. SUBJECT-SPECIFIC ANALYSIS

Performance can be analyzed at the subject level rather than treating all attendance and academic data as one combined value.

For each subject, the system can consider:
- Subject-specific attendance
- Subject marks
- Subject assignments
- Subject-level risk

9. SYSTEM ARCHITECTURE

Android Application
        |
     REST API
        |
FastAPI Backend
        |
Business Logic + ML Model + API Processing
        |
Oracle Database

10. TECHNOLOGY STACK

FRONTEND
- Android
- Kotlin
- XML layouts
- Retrofit
- MPAndroidChart

BACKEND
- Python
- FastAPI
- REST APIs
- Uvicorn

DATABASE
- Oracle Database
- SQL
- Relational data model

MACHINE LEARNING
- Student risk prediction
- Feature-based analysis
- Dropout-risk classification

DEVELOPMENT TOOLS
- Android Studio
- Visual Studio Code
- Git
- GitHub
- Postman / Swagger

11. DATABASE

Major entities include:
- STUDENTS
- FACULTY
- DIVISIONS
- SUBJECTS
- ATTENDANCE
- MARKS
- EXAMS
- ASSIGNMENTS
- ASSIGNMENT_SUBMISSIONS
- LEARNING_BEHAVIOR
- FACULTY_SUBJECTS

12. API ARCHITECTURE

STUDENT APIs
GET /students/{student_id}/dashboard
GET /students/{student_id}/detailed-analysis
GET /students/{student_id}/prediction
GET /student/{student_id}/performance-trend

FACULTY APIs
GET /faculty/{faculty_id}/dashboard
GET /faculty/{faculty_id}/risk

ADMIN APIs
GET /admin/dashboard
GET /admin/faculty
POST /admin/faculty
GET /admin/students
GET /admin/divisions

13. DATA FLOW

Student Data ->Oracle Database ->FastAPI ->Feature Calculation ->ML Risk Prediction ->Risk Classification ->Android Application->Faculty / Student / Admin

14. AI/ML APPROACH

Important features include:
- Attendance
- Academic performance
- Study behaviour
- LMS activity
- Assignment completion

The prediction system produces a dropout-risk score, which is classified as HIGH, MEDIUM, or LOW.

15. EARLY INTERVENTION

Detect -> Analyze -> Predict -> Intervene -> Monitor

Possible interventions:
- Faculty mentoring
- Academic support
- Attendance counselling
- Assignment follow-up
- Subject-specific guidance
- Additional learning resources

16. FEASIBILITY & VIABILITY

TECHNICAL
- Established technologies
- REST-based architecture
- Relational database
- Android-compatible APIs
- Modular backend

ECONOMIC
- Widely available development tools
- Reduced manual monitoring
- Incremental deployment
- Lower administrative effort

OPERATIONAL
- Role-based dashboards
- Faculty-focused risk monitoring
- Centralized administration
- Student insights

17. ADVANTAGES

- Early identification of at-risk students
- Centralized monitoring
- Data-driven decision making
- Subject-specific analysis
- Reduced manual monitoring
- Visual performance trends
- Role-based access
- Scalable architecture

18. FUTURE SCOPE

- Automated faculty alerts
- Push notifications
- Parent/guardian dashboard
- More advanced ML models
- Personalized learning recommendations
- Automated intervention workflows
- Historical semester comparison
- Institution-wide analytics
- Explainable AI risk factors
- ERP/LMS integration

19. EXPECTED IMPACT

EduGuard shifts student monitoring from reactive monitoring to proactive intervention.

Traditional:
Problem -> Result -> Action

EduGuard:
Data -> Early Warning -> Analysis -> Intervention -> Improvement

20. PROJECT GOAL

EduGuard aims to transform student monitoring into a proactive, data-driven early-warning system that enables timely intervention and supports better academic outcomes.
