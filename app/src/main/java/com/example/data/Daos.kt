package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAll()
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes WHERE raisedAgainstUserId = :userId AND status = 'PENDING' ORDER BY timestamp DESC")
    fun getDisputesAgainstUser(userId: Int): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes WHERE raisedByUserId = :userId ORDER BY timestamp DESC")
    fun getDisputesRaisedByUser(userId: Int): Flow<List<DisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    @Delete
    suspend fun deleteDispute(dispute: DisputeEntity)

    @Query("DELETE FROM disputes WHERE id = :id")
    suspend fun deleteDisputeById(id: Int)

    @Query("SELECT COUNT(*) FROM disputes WHERE raisedAgainstUserId = :userId AND status = 'PENDING'")
    suspend fun getDisputesCountForUser(userId: Int): Int
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getChatMessages(userId1: Int, userId2: Int): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}

@Dao
interface RideDao {
    @Query("SELECT * FROM rides WHERE (passengerId = :userId OR partnerId = :userId) AND status != 'COMPLETED' AND status != 'UNMATCHED' LIMIT 1")
    fun getActiveRideForUser(userId: Int): Flow<RideMatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideMatchEntity): Long

    @Update
    suspend fun updateRide(ride: RideMatchEntity)

    @Query("SELECT * FROM rides WHERE id = :id")
    fun getRideById(id: Int): Flow<RideMatchEntity?>
}
