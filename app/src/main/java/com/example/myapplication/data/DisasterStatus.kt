package com.example.myapplication.data

/**
 * Represents the user's current status during a disaster.
 */
data class DisasterPayload(
    val userId: String,
    val ability: String,
    val status: String,
    val lat: Double,
    val lng: Double,
    val battery: Int
)

enum class UserStatus(val description: String) {
    SAFE("I'm safe"),
    TRAPPED("I'm trapped"),
    INJURED("I'm injured"),
    NEED_HELP("I need help")
}
