package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val name: String,
    val email: String,
    val gender: String,
    val isLoggedIn: Boolean = false,
    val lat: Double = 12.971598, // Default Bangalore coordinates
    val lng: Double = 77.594566,
    val disputeCount: Int = 0
)

@Entity(tableName = "disputes")
data class DisputeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val raisedByUserId: Int,
    val raisedAgainstUserId: Int,
    val raisedByName: String,
    val amount: Double,
    val status: String, // "PENDING", "RESOLVED"
    val screenshotPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: Int,
    val receiverId: Int,
    val text: String,
    val screenshotPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "rides")
data class RideMatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val passengerId: Int,
    val partnerId: Int,
    val sourceName: String,
    val destName: String,
    val sourceLat: Double,
    val sourceLng: Double,
    val destLat: Double,
    val destLng: Double,
    val riderCount: Int,
    val status: String, // "PENDING", "MATCHED", "WALKING", "STARTING", "IN_PROGRESS", "PAYMENT_SETTING", "COMPLETED"
    val totalFare: Double = 0.0,
    val distance1: Double = 0.0, // distance in km for user 1
    val distance2: Double = 0.0, // distance in km for user 2
    val rider1Accepted: Boolean = false,
    val rider2Accepted: Boolean = false,
    val rider1Paid: Boolean = false,
    val rider2Paid: Boolean = false
)
