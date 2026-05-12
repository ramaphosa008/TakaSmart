package com.ramaphosa.takasmart.data

import com.ramaphosa.takasmart.data.repository.FirestoreRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EwasteItem(
    val id        : String = "",
    val category  : String = "",
    val condition : String = "",
    val model     : String = "",
    val status    : String = ""
)

data class PickupSummary(
    val id          : String = "",
    val status      : String = "",
    val scheduledAt : String = "",
    val itemCount   : Int    = 0
)

data class RedemptionItem(
    val id           : String = "",
    val rewardType   : String = "",
    val pointsSpent  : Int    = 0,
    val amountKes    : Int    = 0,
    val status       : String = "",
    val createdAt    : String = ""
)

data class RewardHistoryItem(
    val id            : String = "",
    val pointsEarned  : Int    = 0,
    val reason        : String = "",
    val pickupId      : String = "",
    val certId        : String = "",
    val createdAt     : String = ""
)


class HouseholdViewModel : ViewModel() {

    private val db  = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Points balance
    private val _points = MutableStateFlow(0)
    val points: StateFlow<Int> = _points

    // Recycled kg
    private val _recycledKg = MutableStateFlow(0.0)
    val recycledKg: StateFlow<Double> = _recycledKg

    // Pending items
    private val _items = MutableStateFlow<List<EwasteItem>>(emptyList())
    val items: StateFlow<List<EwasteItem>> = _items

    // Upcoming pickup
    private val _upcomingPickup = MutableStateFlow<PickupSummary?>(null)
    val upcomingPickup: StateFlow<PickupSummary?> = _upcomingPickup

    // Completed pickups count
    private val _pickupsDone = MutableStateFlow(0)
    val pickupsDone: StateFlow<Int> = _pickupsDone


    private val _rewardHistory =
        MutableStateFlow<List<RewardHistoryItem>>(emptyList())

    val rewardHistory: StateFlow<List<RewardHistoryItem>> =
        _rewardHistory


    // Redemption history
    private val _redemptions =
        MutableStateFlow<List<RedemptionItem>>(emptyList())

    val redemptions: StateFlow<List<RedemptionItem>> =
        _redemptions

    init {
        loadUserData()
        loadItems()
        loadUpcomingPickup()
        loadRewardHistory()
        loadRedemptions()
    }



    private fun loadRedemptions() {

        db.collection("redemptions")
            .whereEqualTo("user_id", uid)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snaps, _ ->

                _redemptions.value =
                    snaps?.documents?.map { doc ->

                        RedemptionItem(
                            id          = doc.id,
                            rewardType  = doc.getString("reward_type") ?: "",
                            pointsSpent = doc.getLong("points_spent")?.toInt() ?: 0,
                            amountKes   = doc.getLong("amount_kes")?.toInt() ?: 0,
                            status      = doc.getString("status") ?: "",
                            createdAt   = doc.get("created_at")?.toString() ?: ""
                        )

                    } ?: emptyList()
            }
    }


    private fun loadRewardHistory() {

        db.collection("rewards")
            .whereEqualTo("user_id", uid)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snaps, _ ->

                _rewardHistory.value =
                    snaps?.documents?.map { doc ->

                        RewardHistoryItem(
                            id           = doc.id,
                            pointsEarned = doc.getLong("points_earned")?.toInt() ?: 0,
                            reason       = doc.getString("reason") ?: "",
                            pickupId     = doc.getString("pickup_id") ?: "",
                            certId       = doc.getString("cert_id") ?: "",
                            createdAt    = doc.get("created_at")?.toString() ?: ""
                        )

                    } ?: emptyList()
            }
    }

    private fun loadUserData() {
        db.collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                _points.value     = snap?.getLong("points_balance")?.toInt() ?: 0
                _recycledKg.value = snap?.getDouble("recycled_kg") ?: 0.0
                _pickupsDone.value= snap?.getLong("pickups_done")?.toInt() ?: 0
            }
    }

    private fun loadItems() {
        db.collection("ewaste_items")
            .whereEqualTo("user_id", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snaps, _ ->
                _items.value = snaps?.documents?.map { doc ->
                    EwasteItem(
                        id        = doc.id,
                        category  = doc.getString("category") ?: "",
                        condition = doc.getString("condition") ?: "",
                        model     = doc.getString("model") ?: "",
                        status    = doc.getString("status") ?: ""
                    )
                } ?: emptyList()
            }
    }

    private fun loadUpcomingPickup() {
        db.collection("pickups")
            .whereEqualTo("household_id", uid)
            .whereIn("status", listOf("requested", "confirmed", "en_route"))
            .limit(1)
            .addSnapshotListener { snaps, _ ->
                val doc = snaps?.documents?.firstOrNull()
                _upcomingPickup.value = doc?.let {
                    PickupSummary(
                        id          = it.id,
                        status      = it.getString("status") ?: "",
                        scheduledAt = it.getString("scheduled_at") ?: "",
                        itemCount   = (it.get("item_ids") as? List<*>)?.size ?: 0
                    )
                }
            }
    }
    fun addItem(
        category  : String,
        condition : String,
        model     : String,
        photoUrl  : String
    ) {
        viewModelScope.launch {
            db.collection("ewaste_items").add(
                mapOf(
                    "user_id"    to uid,
                    "category"   to category,
                    "condition"  to condition,
                    "model"      to model,
                    "photo_url"  to photoUrl,
                    "status"     to "pending",
                    "created_at" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            ).await()
        }
    }
}