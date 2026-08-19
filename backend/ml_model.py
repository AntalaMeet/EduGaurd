import joblib
import os


MODEL_PATH = os.path.join(
    os.path.dirname(__file__),
    "dropout_model.pkl"
)


model = joblib.load(MODEL_PATH)


def predict_dropout(
    attendance,
    academic,
    study,
    assignments
):

    features = [[
        attendance,
        academic,
        study,
        assignments
    ]]

    prediction = model.predict(features)[0]

    probabilities = model.predict_proba(features)[0]

    dropout_probability = probabilities[1] * 100

    return {
        "dropped_out_prediction": int(prediction),
        "dropout_risk": round(
            float(dropout_probability),
            2
        )
    }