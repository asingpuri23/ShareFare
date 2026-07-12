package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ShareFareRepository(
    private val userDao: UserDao,
    private val disputeDao: DisputeDao,
    private val messageDao: MessageDao,
    private val rideDao: RideDao
) {
    val loggedInUser: Flow<UserEntity?> = userDao.getLoggedInUser()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getUserByPhone(phone: String): UserEntity? {
        return userDao.getUserByPhone(phone)
    }

    suspend fun saveUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun logout() {
        userDao.logoutAll()
    }

    fun getActiveRide(userId: Int): Flow<RideMatchEntity?> {
        return rideDao.getActiveRideForUser(userId)
    }

    fun getRideById(id: Int): Flow<RideMatchEntity?> {
        return rideDao.getRideById(id)
    }

    suspend fun createRide(ride: RideMatchEntity): Long {
        return rideDao.insertRide(ride)
    }

    suspend fun updateRide(ride: RideMatchEntity) {
        rideDao.updateRide(ride)
    }

    fun getMessages(userId1: Int, userId2: Int): Flow<List<MessageEntity>> {
        return messageDao.getChatMessages(userId1, userId2)
    }

    suspend fun sendMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    fun getDisputesAgainst(userId: Int): Flow<List<DisputeEntity>> {
        return disputeDao.getDisputesAgainstUser(userId)
    }

    fun getDisputesRaisedBy(userId: Int): Flow<List<DisputeEntity>> {
        return disputeDao.getDisputesRaisedByUser(userId)
    }

    suspend fun raiseDispute(dispute: DisputeEntity) {
        disputeDao.insertDispute(dispute)
        // Increment the dispute count of the disputed user
        // We'll simulate this by fetching the user and updating their dispute count
    }

    suspend fun resolveDispute(disputeId: Int) {
        disputeDao.deleteDisputeById(disputeId)
    }

    suspend fun getDisputesCountForUser(userId: Int): Int {
        return disputeDao.getDisputesCountForUser(userId)
    }

    suspend fun prepopulateMockData(currentUserId: Int) {
        // Prepopulate mock users if not already present
        val existingMock = userDao.getUserByPhone("9876543210")
        if (existingMock == null) {
            userDao.insertUser(UserEntity(id = 101, phone = "9876543210", name = "Ananya Sharma", email = "ananya@example.com", gender = "Female", disputeCount = 0))
            userDao.insertUser(UserEntity(id = 102, phone = "9876543211", name = "Rohan Verma", email = "rohan@example.com", gender = "Male", disputeCount = 0))
            userDao.insertUser(UserEntity(id = 103, phone = "9876543212", name = "Priya Patel", email = "priya@example.com", gender = "Female", disputeCount = 3)) // Blocked since disputes >= 3
            userDao.insertUser(UserEntity(id = 104, phone = "9876543213", name = "Vikram Singh", email = "vikram@example.com", gender = "Male", disputeCount = 1))

            // Prepopulate an initial dispute against the current user to demonstrate SettlePaymentsActivity
            if (currentUserId != 0) {
                disputeDao.insertDispute(
                    DisputeEntity(
                        id = 1,
                        raisedByUserId = 103, // Priya Patel
                        raisedAgainstUserId = currentUserId,
                        raisedByName = "Priya Patel",
                        amount = 145.0,
                        status = "PENDING"
                    )
                )
                disputeDao.insertDispute(
                    DisputeEntity(
                        id = 2,
                        raisedByUserId = 104, // Vikram Singh
                        raisedAgainstUserId = currentUserId,
                        raisedByName = "Vikram Singh",
                        amount = 80.0,
                        status = "PENDING"
                    )
                )
            }
        }
    }
}
