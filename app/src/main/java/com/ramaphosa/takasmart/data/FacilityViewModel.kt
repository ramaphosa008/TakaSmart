package com.ramaphosa.takasmart.data

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class IncomingLoad(
    val id                 : String = "",
    val collectorId        : String = "",
    val address            : String = "",
    val scheduledAt        : String = "",
    val itemCount          : Int    = 0,
    val status             : String = "",
    val collectorLoggedKg  : Double = 0.0,
    val householdId        : String = ""
)
class FacilityViewModel : ViewModel() {
    private val db  = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _incomingLoads = MutableStateFlow<List<IncomingLoad>>(emptyList())
    val incomingLoads: StateFlow<List<IncomingLoad>> = _incomingLoads

    private val _completedToday = MutableStateFlow(0)
    val completedToday: StateFlow<Int> = _completedToday

    init {
        fetchFacilityIdAndLoad()
    }

    private fun fetchFacilityIdAndLoad() {
        // First get the entity_id (e.g. FAC002) from the user document
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val entityId = userDoc.getString("entity_id") ?: uid // Fallback to uid if not found


                if (entityId.isEmpty()) {
                    // entity_id not set — nothing to load
                    return@addOnSuccessListener
                }

                loadIncomingLoads(entityId)
            }
    }

    private fun loadIncomingLoads(facilityId: String) {
        db.collection("pickups")
            .whereEqualTo("facility_id", facilityId)
            .whereIn("status", listOf("at_facility", "completed"))
            .addSnapshotListener { snaps, _ ->
                val docs = snaps?.documents ?: emptyList()
                _incomingLoads.value = docs.map { doc ->
                    IncomingLoad(
                        id                = doc.id,
                        collectorId       = doc.getString("collector_id") ?: "",
                        address           = doc.getString("address") ?: "",
                        scheduledAt       = doc.getString("scheduled_at") ?: "",
                        itemCount         = (doc.get("item_ids") as? List<*>)?.size ?: 0,
                        status            = doc.getString("status") ?: "",
                        collectorLoggedKg = doc.getDouble("collector_logged_kg") ?: 0.0,
                        householdId       = doc.getString("household_id") ?: ""
                    )
                }
                _completedToday.value = docs.count { it.getString("status") == "completed" }
            }
    }
}
