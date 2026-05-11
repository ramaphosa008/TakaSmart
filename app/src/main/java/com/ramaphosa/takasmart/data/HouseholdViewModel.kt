package com.ramaphosa.takasmart.data

import com.ramaphosa.takasmart.data.repository.FirestoreRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    init {
        loadUserData()
        loadItems()
        loadUpcomingPickup()
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