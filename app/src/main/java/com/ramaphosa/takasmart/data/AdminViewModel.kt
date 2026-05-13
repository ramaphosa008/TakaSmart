package com.ramaphosa.takasmart.data

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DisputedPickup(
    val id                 : String = "",
    val householdId        : String = "",
    val collectorId        : String = "",
    val address            : String = "",
    val scheduledAt        : String = "",
    val collectorLoggedKg  : Double = 0.0,
    val facilityVerifiedKg : Double = 0.0,
    val disputeReason      : String = "",
    val itemCount          : Int    = 0
)

class AdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _disputes    = MutableStateFlow<List<DisputedPickup>>(emptyList())
    val disputes: StateFlow<List<DisputedPickup>> = _disputes

    private val _resolvedCount = MutableStateFlow(0)
    val resolvedCount: StateFlow<Int> = _resolvedCount

    init {
        loadDisputes()
        loadResolvedCount()
    }

    private fun loadDisputes() {
        db.collection("pickups")
            .whereEqualTo("status", "disputed")
            .addSnapshotListener { snaps, _ ->
                _disputes.value = snaps?.documents?.map { doc ->
                    DisputedPickup(
                        id                 = doc.id,
                        householdId        = doc.getString("household_id") ?: "",
                        collectorId        = doc.getString("collector_id") ?: "",
                        address            = doc.getString("address") ?: "",
                        scheduledAt        = doc.getString("scheduled_at") ?: "",
                        collectorLoggedKg  = doc.getDouble("collector_logged_kg") ?: 0.0,
                        facilityVerifiedKg = doc.getDouble("facility_verified_kg") ?: 0.0,
                        disputeReason      = doc.getString("dispute_reason") ?: "",
                        itemCount          = (doc.get("item_ids") as? List<*>)?.size ?: 0
                    )
                } ?: emptyList()
            }
    }

    private fun loadResolvedCount() {
        db.collection("pickups")
            .whereEqualTo("status", "completed")
            .whereGreaterThan("dispute_reason", "")
            .addSnapshotListener { snaps, _ ->
                _resolvedCount.value = snaps?.documents?.size ?: 0
            }
    }

    // Admin approves facility weight — proceeds to certificate
    fun approveWithFacilityWeight(
        pickupId   : String,
        facilityKg : Double,
        onSuccess  : () -> Unit,
        onError    : (String) -> Unit
    ) {
        db.collection("pickups").document(pickupId)
            .update(
                mapOf(
                    "status"             to "completed",
                    "verified_kg"        to facilityKg,
                    "dispute_resolved_by" to "admin",
                    "dispute_resolution" to "Approved — facility weight accepted"
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to resolve.") }
    }

    // Admin approves collector weight instead
    fun approveWithCollectorWeight(
        pickupId    : String,
        collectorKg : Double,
        onSuccess   : () -> Unit,
        onError     : (String) -> Unit
    ) {
        db.collection("pickups").document(pickupId)
            .update(
                mapOf(
                    "status"             to "completed",
                    "verified_kg"        to collectorKg,
                    "dispute_resolved_by" to "admin",
                    "dispute_resolution" to "Approved — collector weight accepted"
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to resolve.") }
    }

    // Admin rejects the pickup entirely
    fun rejectPickup(
        pickupId  : String,
        reason    : String,
        onSuccess : () -> Unit,
        onError   : (String) -> Unit
    ) {
        db.collection("pickups").document(pickupId)
            .update(
                mapOf(
                    "status"             to "rejected",
                    "dispute_resolved_by" to "admin",
                    "dispute_resolution" to reason
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to reject.") }
    }
}