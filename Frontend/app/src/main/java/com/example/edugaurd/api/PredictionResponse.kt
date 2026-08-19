package com.example.edugaurd.api

data class PredictionResponse(
    val student_id: Int,
    val features: PredictionFeatures,
    val prediction: PredictionResult
)

data class PredictionFeatures(
    val academic: Double,
    val attendance: Double,
    val study: Double,
    val assignments: Double
)

data class PredictionResult(
    val dropout_risk: Double,
    val risk_level: String,
    val model_prediction: Int
)