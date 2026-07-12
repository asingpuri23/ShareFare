package com.example

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

enum class Screen {
    LOGIN,
    MAIN,
    SET_PROFILE,
    SET_PAYMENTS_LIST, // SettlePaymentsActivity
    MATCH_RIDERS,      // MatchRidersActivity
    MATCHED_USER,      // MatchedUserActivity
    MATCHED_CHAT,      // MatchedUserChatActivity
    SET_PAYMENTS_SCREEN // SetPaymentsScreenActivity
}

data class Place(val name: String, val lat: Double, val lng: Double)

data class MockRider(
    val id: Int,
    val name: String,
    val phone: String,
    val gender: String,
    val initialDistanceMeters: Double,
    val numRiders: Int,
    val sourceOffsetLat: Double,
    val sourceOffsetLng: Double,
    val destOffsetLat: Double,
    val destOffsetLng: Double,
    var matchClickedByPartner: Boolean = false
)

class ShareFareViewModel(
    application: Application,
    private val repository: ShareFareRepository
) : AndroidViewModel(application) {

    // Popular Bangalore locations for realistic search suggestions
    val popularPlaces = listOf(
        Place("Koramangala 5th Block (Starbucks)", 12.9343, 77.6193),
        Place("Indiranagar Metro Station", 12.9784, 77.6408),
        Place("HSR Layout Sector 1 (BDAs Complex)", 12.9105, 77.6450),
        Place("MG Road Metro Station", 12.9755, 77.6068),
        Place("Kempegowda International Airport", 13.1986, 77.7066),
        Place("Marathahalli Bridge", 12.9562, 77.6974),
        Place("Whitefield ITPL Main Gate", 12.9830, 77.7500),
        Place("Majestic Bus Stand / Railway Station", 12.9779, 77.5724),
        Place("Jayanagar 4th Block Shopping Complex", 12.9284, 77.5833),
        Place("Electronic City Phase 1 Toll Plaza", 12.8499, 77.6592)
    )

    // Screen stack for back navigation
    private val _screenStack = mutableStateListOf<Screen>()
    val currentScreen: Screen get() = _screenStack.lastOrNull() ?: Screen.LOGIN

    // State flows
    val loggedInUser: StateFlow<UserEntity?> = repository.loggedInUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeRide: StateFlow<RideMatchEntity?> = loggedInUser
        .flatMapLatest { user ->
            if (user != null) repository.getActiveRide(user.id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val disputesAgainstUser: StateFlow<List<DisputeEntity>> = loggedInUser
        .flatMapLatest { user ->
            if (user != null) repository.getDisputesAgainst(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disputesRaisedByUser: StateFlow<List<DisputeEntity>> = loggedInUser
        .flatMapLatest { user ->
            if (user != null) repository.getDisputesRaisedBy(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live chat messages
    val chatMessages: StateFlow<List<MessageEntity>> = combine(loggedInUser, activeRide) { user, ride ->
        Pair(user, ride)
    }.flatMapLatest { (user, ride) ->
        if (user != null && ride != null) {
            val partnerId = if (ride.passengerId == user.id) ride.partnerId else ride.passengerId
            repository.getMessages(user.id, partnerId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form inputs
    var loginPhone by mutableStateOf("")
    var registerName by mutableStateOf("")
    var registerEmail by mutableStateOf("")
    var registerGender by mutableStateOf("Male") // Male, Female, Other

    // Search fields
    var sourceQuery by mutableStateOf("")
    var destQuery by mutableStateOf("")
    var numRiders by mutableStateOf(1)
    var selectedSourcePlace by mutableStateOf<Place?>(null)
    var selectedDestPlace by mutableStateOf<Place?>(null)

    // Map & simulation states
    var userLat by mutableStateOf(12.971598) // Default Bangalore
    var userLng by mutableStateOf(77.594566)
    var isBroadcasting by mutableStateOf(false)
    var showDisputeAlertOnOpen by mutableStateOf(false)

    // Current lists
    var matchingRiders = mutableStateListOf<MockRider>()
    var selectedMockRider by mutableStateOf<MockRider?>(null)

    // Distance simulation between matched users
    var simulatedDistanceMeters by mutableStateOf(150.0) // Users start 150m apart
    var isWalkingSimulated by mutableStateOf(false)

    // Payment state
    var setPaymentAmountInput by mutableStateOf("")
    var paymentAcceptedByUser1 by mutableStateOf(false)
    var paymentAcceptedByUser2 by mutableStateOf(false)
    var paymentCalculationResult by mutableStateOf<String?>(null)
    var isRidePaymentSettling by mutableStateOf(false)
    var showPaymentReceipt by mutableStateOf(false)

    // Dispute Settle Chat
    var currentDisputeUser by mutableStateOf<UserEntity?>(null)
    var activeDisputeToSettle by mutableStateOf<DisputeEntity?>(null)
    var disputeChatMessages = mutableStateListOf<Pair<String, Boolean>>() // message to (isMe)
    var disputeChatInput by mutableStateOf("")

    init {
        viewModelScope.launch {
            loggedInUser.collect { user ->
                if (user != null) {
                    userLat = user.lat
                    userLng = user.lng
                    repository.prepopulateMockData(user.id)
                    
                    // Check if they have active disputes to show notification on open (Feature 19)
                    val activeDisputes = repository.getDisputesCountForUser(user.id)
                    if (activeDisputes > 0) {
                        showDisputeAlertOnOpen = true
                    }
                    if (_screenStack.isEmpty() || _screenStack.firstOrNull() == Screen.LOGIN) {
                        navigateToRoot(Screen.MAIN)
                    }
                } else {
                    navigateToRoot(Screen.LOGIN)
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _screenStack.add(screen)
    }

    fun navigateBack() {
        if (_screenStack.size > 1) {
            _screenStack.removeLast()
        }
    }

    fun navigateToRoot(screen: Screen) {
        _screenStack.clear()
        _screenStack.add(screen)
    }

    // Auth actions
    fun loginOrRegister() {
        if (loginPhone.isBlank() || loginPhone.length < 10) return
        viewModelScope.launch {
            val existing = repository.getUserByPhone(loginPhone)
            if (existing != null) {
                // Log them in
                repository.saveUser(existing.copy(isLoggedIn = true))
            } else {
                // Create profile first
                navigateTo(Screen.SET_PROFILE)
            }
        }
    }

    fun completeRegistration() {
        if (registerName.isBlank() || registerEmail.isBlank()) return
        viewModelScope.launch {
            val newUser = UserEntity(
                phone = loginPhone,
                name = registerName,
                email = registerEmail,
                gender = registerGender,
                isLoggedIn = true
            )
            repository.saveUser(newUser)
        }
    }

    fun updateProfile(name: String, email: String, gender: String) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(user.copy(name = name, email = email, gender = gender))
            navigateBack()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _screenStack.clear()
            _screenStack.add(Screen.LOGIN)
            // Reset state
            selectedSourcePlace = null
            selectedDestPlace = null
            sourceQuery = ""
            destQuery = ""
            matchingRiders.clear()
            selectedMockRider = null
        }
    }

    // Location selection helper
    fun selectSource(place: Place) {
        selectedSourcePlace = place
        sourceQuery = place.name
        userLat = place.lat
        userLng = place.lng
    }

    fun selectDest(place: Place) {
        selectedDestPlace = place
        destQuery = place.name
    }

    // Search and Matching Engine (Features 9, 10, 11)
    fun searchSimilarRiders() {
        val user = loggedInUser.value ?: return
        val source = selectedSourcePlace ?: return
        val dest = selectedDestPlace ?: return

        viewModelScope.launch {
            // Check dispute block constraint (Feature 19 - 3 or more disputes blocks searches)
            val disputeCount = repository.getDisputesCountForUser(user.id)
            if (disputeCount >= 3) {
                // block
                return@launch
            }

            isBroadcasting = true
            delay(2000) // Realistic broadcasting simulation delay (Feature 9)
            isBroadcasting = false

            // Generate candidates matching constraints (within 200m source, 500m destination)
            matchingRiders.clear()

            // Dynamic offsets based on meters to guarantee they match the constraints
            // One degree of latitude is ~111,000 meters.
            // 200 meters is roughly 0.0018 degrees.
            // 500 meters is roughly 0.0045 degrees.
            val ridersList = listOf(
                MockRider(
                    id = 101,
                    name = "Ananya Sharma",
                    phone = "9876543210",
                    gender = "Female",
                    initialDistanceMeters = 85.0,
                    numRiders = 1,
                    sourceOffsetLat = 0.0006,
                    sourceOffsetLng = 0.0005,
                    destOffsetLat = 0.0012,
                    destOffsetLng = 0.0011
                ),
                MockRider(
                    id = 102,
                    name = "Rohan Verma",
                    phone = "9876543211",
                    gender = "Male",
                    initialDistanceMeters = 140.0,
                    numRiders = 2,
                    sourceOffsetLat = -0.0011,
                    sourceOffsetLng = -0.0009,
                    destOffsetLat = -0.0022,
                    destOffsetLng = -0.0018
                ),
                MockRider(
                    id = 104,
                    name = "Vikram Singh",
                    phone = "9876543213",
                    gender = "Male",
                    initialDistanceMeters = 190.0,
                    numRiders = 1,
                    sourceOffsetLat = 0.0015,
                    sourceOffsetLng = 0.0012,
                    destOffsetLat = 0.0035,
                    destOffsetLng = 0.0028
                )
            )

            // Let's filter to make sure they are within constraints mathematically using Haversine
            ridersList.forEach { rider ->
                val riderSourceLat = source.lat + rider.sourceOffsetLat
                val riderSourceLng = source.lng + rider.sourceOffsetLng
                val riderDestLat = dest.lat + rider.destOffsetLat
                val riderDestLng = dest.lng + rider.destOffsetLng

                val sourceDist = calculateDistanceMeters(source.lat, source.lng, riderSourceLat, riderSourceLng)
                val destDist = calculateDistanceMeters(dest.lat, dest.lng, riderDestLat, riderDestLng)

                if (sourceDist <= 200.0 && destDist <= 500.0) {
                    matchingRiders.add(rider)
                }
            }

            navigateTo(Screen.MATCH_RIDERS)
        }
    }

    // Match Action (Feature 10, 11)
    fun initiateMatch(rider: MockRider) {
        val currentUser = loggedInUser.value ?: return
        val source = selectedSourcePlace ?: return
        val dest = selectedDestPlace ?: return

        selectedMockRider = rider
        simulatedDistanceMeters = rider.initialDistanceMeters
        isWalkingSimulated = false

        viewModelScope.launch {
            // Simulate matching delay and both clicking match (Feature 11)
            delay(1500)
            rider.matchClickedByPartner = true

            // Create a real active ride in local DB
            val distance1 = calculateDistanceMeters(source.lat, source.lng, dest.lat, dest.lng) / 1000.0 // km
            // Partner distance to their destination (slightly different due to offsets)
            val partnerDestLat = dest.lat + rider.destOffsetLat
            val partnerDestLng = dest.lng + rider.destOffsetLng
            val distance2 = calculateDistanceMeters(source.lat + rider.sourceOffsetLat, source.lng + rider.sourceOffsetLng, partnerDestLat, partnerDestLng) / 1000.0 // km

            val ride = RideMatchEntity(
                passengerId = currentUser.id,
                partnerId = rider.id,
                sourceName = source.name,
                destName = dest.name,
                sourceLat = source.lat,
                sourceLng = source.lng,
                destLat = dest.lat,
                destLng = dest.lng,
                riderCount = numRiders,
                status = "MATCHED",
                distance1 = distance1,
                distance2 = distance2
            )
            val rideId = repository.createRide(ride)

            // Seed initial greeting in chat
            repository.sendMessage(
                MessageEntity(
                    senderId = rider.id,
                    receiverId = currentUser.id,
                    text = "Hey! I am also heading to ${dest.name.take(25)}... Let's meet up and share a ride."
                )
            )

            navigateTo(Screen.MATCHED_USER)
        }
    }

    fun unmatchActiveRide() {
        val ride = activeRide.value ?: return
        viewModelScope.launch {
            repository.updateRide(ride.copy(status = "UNMATCHED"))
            selectedMockRider = null
            navigateBack()
        }
    }

    // Walking closer simulation (Feature 13)
    fun simulateWalkCloser() {
        if (isWalkingSimulated) return
        isWalkingSimulated = true
        viewModelScope.launch {
            while (simulatedDistanceMeters > 5.0 && isWalkingSimulated) {
                delay(500)
                simulatedDistanceMeters = max(0.0, simulatedDistanceMeters - 15.0)
            }
            isWalkingSimulated = false
        }
    }

    fun stopWalkSimulation() {
        isWalkingSimulated = false
    }

    // Chat actions (Feature 12)
    fun sendMessage(text: String) {
        val user = loggedInUser.value ?: return
        val ride = activeRide.value ?: return
        if (text.isBlank()) return

        val partnerId = if (ride.passengerId == user.id) ride.partnerId else ride.passengerId

        viewModelScope.launch {
            repository.sendMessage(
                MessageEntity(
                    senderId = user.id,
                    receiverId = partnerId,
                    text = text
                )
            )

            // Generate realistic automatic response after 2 seconds
            delay(1500)
            val partnerResponse = getBotResponse(text)
            repository.sendMessage(
                MessageEntity(
                    senderId = partnerId,
                    receiverId = user.id,
                    text = partnerResponse
                )
            )
        }
    }

    // Ride Start (Feature 14)
    fun startRideShare() {
        val ride = activeRide.value ?: return
        viewModelScope.launch {
            repository.updateRide(ride.copy(status = "PAYMENT_SETTING"))
            // Reset payments screen inputs
            setPaymentAmountInput = ""
            paymentAcceptedByUser1 = false
            paymentAcceptedByUser2 = false
            paymentCalculationResult = null
            showPaymentReceipt = false
            navigateTo(Screen.SET_PAYMENTS_SCREEN)
        }
    }

    // Settle payments / Fare Splitting logic (Features 15, 16, 17)
    fun proposePaymentAmount() {
        val amount = setPaymentAmountInput.toDoubleOrNull() ?: return
        val ride = activeRide.value ?: return

        // Splitting calculation based on distances (Feature 17)
        // eg: User 1 goes distance1 km, User 2 goes distance2 km
        // Share 1 = d1 / (d1 + d2) * Fare
        // Share 2 = d2 / (d1 + d2) * Fare
        val d1 = ride.distance1
        val d2 = ride.distance2
        val totalD = d1 + d2

        if (totalD > 0) {
            val share1 = (d1 / totalD) * amount
            val share2 = (d2 / totalD) * amount

            val name1 = "You"
            val name2 = selectedMockRider?.name ?: "Partner"

            paymentCalculationResult = String.format(
                "Calculated Share:\n• %s (%.2f km): ₹%.2f\n• %s (%.2f km): ₹%.2f",
                name1, d1, share1, name2, d2, share2
            )
        }
    }

    fun acceptPaymentProposal(userIndex: Int) {
        if (userIndex == 1) {
            paymentAcceptedByUser1 = true
            // Simulate partner accepting automatically after 1 second
            viewModelScope.launch {
                delay(1000)
                paymentAcceptedByUser2 = true
            }
        }
    }

    fun denyPaymentProposal() {
        paymentAcceptedByUser1 = false
        paymentAcceptedByUser2 = false
        paymentCalculationResult = null
    }

    fun submitRidePayment() {
        val ride = activeRide.value ?: return
        val amount = setPaymentAmountInput.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            repository.updateRide(ride.copy(
                status = "IN_PROGRESS",
                totalFare = amount
            ))
            showPaymentReceipt = true
        }
    }

    // Complete ride, pay, and raise dispute (Feature 18, 19)
    fun completePayment(raisedDispute: Boolean) {
        val user = loggedInUser.value ?: return
        val ride = activeRide.value ?: return
        val rider = selectedMockRider ?: return

        viewModelScope.launch {
            if (raisedDispute) {
                // Raise dispute against partner (Feature 19)
                repository.raiseDispute(
                    DisputeEntity(
                        raisedByUserId = user.id,
                        raisedAgainstUserId = rider.id,
                        raisedByName = user.name,
                        amount = ride.totalFare * (ride.distance2 / (ride.distance1 + ride.distance2)),
                        status = "PENDING"
                    )
                )

                // Update the partner user's dispute count in DB
                val partnerUser = repository.allUsers.firstOrNull()?.find { it.id == rider.id }
                if (partnerUser != null) {
                    repository.updateUser(partnerUser.copy(disputeCount = partnerUser.disputeCount + 1))
                }
            }

            // Mark ride as complete
            repository.updateRide(ride.copy(status = "COMPLETED"))
            selectedMockRider = null
            navigateToRoot(Screen.MAIN)
        }
    }

    // Dispute Settle Chat Action (Feature 8)
    fun openDisputeChat(dispute: DisputeEntity) {
        activeDisputeToSettle = dispute
        disputeChatMessages.clear()
        disputeChatInput = ""
        viewModelScope.launch {
            val user = repository.allUsers.firstOrNull()?.find { it.id == dispute.raisedByUserId }
            currentDisputeUser = user

            // Initial messages
            disputeChatMessages.add(Pair("Hi, you raised a dispute for ₹${String.format("%.2f", dispute.amount)}. Let's settle this.", true))
            disputeChatMessages.add(Pair("Yes, the ride was completed but I didn't receive my share of the fare. Please upload a screenshot of your payment transfer.", false))

            navigateTo(Screen.SET_PAYMENTS_LIST)
        }
    }

    fun sendDisputeChatMessage() {
        if (disputeChatInput.isBlank()) return
        val msg = disputeChatInput
        disputeChatMessages.add(Pair(msg, true))
        disputeChatInput = ""

        // Bot responds with a simulated dispute resolution acknowledgment
        viewModelScope.launch {
            delay(1500)
            if (msg.lowercase().contains("screenshot") || msg.lowercase().contains("paid") || msg.lowercase().contains("sent") || msg.lowercase().contains("attach")) {
                disputeChatMessages.add(Pair("Received the screenshot. The payment looks successful. Thank you! I will resolve the dispute now.", false))
            } else {
                disputeChatMessages.add(Pair("Please attach the screenshot or verify the transaction ID so I can resolve it.", false))
            }
        }
    }

    fun attachScreenshotToDispute() {
        // Simulate attaching a payment receipt screenshot (Feature 8)
        viewModelScope.launch {
            disputeChatMessages.add(Pair("[Uploaded: payment_receipt_screenshot.png]", true))
            delay(1500)
            disputeChatMessages.add(Pair("Excellent, thank you for sending the proof. Settle payment received! I am clicking resolve now.", false))
        }
    }

    fun resolveActiveDispute() {
        val dispute = activeDisputeToSettle ?: return
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            repository.resolveDispute(dispute.id)

            // Decrement current user's dispute count if we were the ones disputed
            if (user.disputeCount > 0) {
                repository.updateUser(user.copy(disputeCount = max(0, user.disputeCount - 1)))
            }

            activeDisputeToSettle = null
            currentDisputeUser = null
            navigateBack()
        }
    }

    // Haversine formula to compute actual distances in meters
    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // BOT simulator chat content (Feature 12)
    private fun getBotResponse(userMsg: String): String {
        val lower = userMsg.lowercase()
        return when {
            lower.contains("hi") || lower.contains("hello") || lower.contains("hey") ->
                "Hey! Where should we meet up? I am standing close by."
            lower.contains("where") || lower.contains("standing") || lower.contains("location") ->
                "I am near the main gate. Let's walk and meet halfway or at the pickup point."
            lower.contains("ok") || lower.contains("sure") || lower.contains("great") || lower.contains("yes") ->
                "Awesome! I am walking towards you now. Let's match up within 20m."
            lower.contains("coming") || lower.contains("walk") || lower.contains("move") ->
                "Perfect! I see your location updating on the map. I'm almost there too."
            lower.contains("cab") || lower.contains("book") || lower.contains("uber") || lower.contains("ola") ->
                "I have the app open. I can book it, or you can. We can split it based on the exact distance!"
            else -> "Sounds good! Let's get together and start the ride share."
        }
    }
}

class ShareFareViewModelFactory(
    private val application: Application,
    private val repository: ShareFareRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareFareViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShareFareViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
