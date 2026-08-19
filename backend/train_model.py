import pandas as pd
import joblib

from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report

from db import get_connection


# -------------------------------------------------
# DATABASE CONNECTION
# -------------------------------------------------

connection = get_connection()


# -------------------------------------------------
# LOAD TRAINING DATA
# -------------------------------------------------

query = """
SELECT
    ATTENDANCE_SCORE,
    ACADEMIC_SCORE,
    STUDY_SCORE,
    ASSIGNMENT_SCORE,
    DROPPED_OUT
FROM DROPOUT_TRAINING_DATA
"""

df = pd.read_sql(
    query,
    connection
)

connection.close()


# -------------------------------------------------
# DISPLAY DATA
# -------------------------------------------------

print("\nTraining Data:")
print(df)

print("\nDataset Shape:")
print(df.shape)


# -------------------------------------------------
# FEATURES
# -------------------------------------------------

X = df[
    [
        "ATTENDANCE_SCORE",
        "ACADEMIC_SCORE",
        "STUDY_SCORE",
        "ASSIGNMENT_SCORE"
    ]
]

y = df["DROPPED_OUT"]


# -------------------------------------------------
# TRAIN / TEST SPLIT
# -------------------------------------------------

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.2,
    random_state=42,
    stratify=y
)


# -------------------------------------------------
# RANDOM FOREST MODEL
# -------------------------------------------------

model = RandomForestClassifier(
    n_estimators=200,
    max_depth=5,
    random_state=42
)

model.fit(
    X_train,
    y_train
)


# -------------------------------------------------
# PREDICTION
# -------------------------------------------------

predictions = model.predict(X_test)


# -------------------------------------------------
# EVALUATION
# -------------------------------------------------

accuracy = accuracy_score(
    y_test,
    predictions
)

print("\nModel Accuracy:")
print(
    round(accuracy * 100, 2),
    "%"
)

print("\nClassification Report:")

print(
    classification_report(
        y_test,
        predictions,
        zero_division=0
    )
)


# -------------------------------------------------
# FEATURE IMPORTANCE
# -------------------------------------------------

print("\nFeature Importance:")

features = [
    "Attendance",
    "Academic",
    "Study",
    "Assignments"
]

for feature, importance in zip(
    features,
    model.feature_importances_
):

    print(
        f"{feature}: "
        f"{importance:.4f}"
    )


# -------------------------------------------------
# SAVE MODEL
# -------------------------------------------------

joblib.dump(
    model,
    "dropout_model.pkl"
)

print("\nModel saved successfully:")
print("dropout_model.pkl")