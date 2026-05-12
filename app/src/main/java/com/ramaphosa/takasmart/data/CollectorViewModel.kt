package com.ramaphosa.takasmart.data

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class JobSummary(
    val id          : String = "",
    val address     : String = "",
    val scheduledAt : String = "",
    val itemCount   : Int    = 0,
    val status      : String = "",
    val householdId : String = ""
)

data class PayoutRecord(
    val jobId    : String = "",
    val amountKes: Double = 0.0,
    val verifiedKg: Double = 0.0,
    val date     : String = ""
)

class CollectorViewModel : ViewModel() {

    private val db  = FirebaseFirestore.getInstance()
    private val authUid =
        FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var collectorEntityId = ""


    // Available jobs — status is 'requested' and no collector assigned yet
    private val _availableJobs = MutableStateFlow<List<JobSummary>>(emptyList())
    val availableJobs: StateFlow<List<JobSummary>> = _availableJobs

    // This collector's active job
    private val _activeJob = MutableStateFlow<JobSummary?>(null)
    val activeJob: StateFlow<JobSummary?> = _activeJob

    // Earnings this month
    private val _monthlyEarnings = MutableStateFlow(0.0)
    val monthlyEarnings: StateFlow<Double> = _monthlyEarnings

    // Completed pickups count
    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount

    // Total kg collected
    private val _totalKg = MutableStateFlow(0.0)
    val totalKg: StateFlow<Double> = _totalKg

    // Payout history
    private val _payouts = MutableStateFlow<List<PayoutRecord>>(emptyList())
    val payouts: StateFlow<List<PayoutRecord>> = _payouts

    init {
        fetchCollectorEntityId()
    }

    private fun fetchCollectorEntityId() {

        db.collection("users")
            .document(authUid)
            .get()
            .addOnSuccessListener { doc ->

                collectorEntityId =
                    doc.getString("entity_id") ?: ""

                if (collectorEntityId.isNotEmpty()) {

                    loadAvailableJobs()
                    loadActiveJob()
                    loadEarnings()
                }
            }
    }

    private fun loadAvailableJobs() {
        db.collection("pickups")
            .whereEqualTo("status", "requested")
            .addSnapshotListener { snaps, _ ->
                _availableJobs.value = snaps?.documents?.map { doc ->
                    JobSummary(
                        id          = doc.id,
                        address     = doc.getString("address") ?: "",
                        scheduledAt = doc.getString("scheduled_at") ?: "",
                        itemCount   = (doc.get("item_ids") as? List<*>)?.size ?: 0,
                        status      = doc.getString("status") ?: "",
                        householdId = doc.getString("household_id") ?: ""
                    )
                } ?: emptyList()
            }
    }

    private fun loadActiveJob() {
        db.collection("pickups")
            .whereEqualTo("collector_id", collectorEntityId)
            .whereIn("status", listOf("confirmed", "en_route", "at_household", "at_facility"))
            .limit(1)
            .addSnapshotListener { snaps, _ ->
                val doc = snaps?.documents?.firstOrNull()
                _activeJob.value = doc?.let {
                    JobSummary(
                        id          = it.id,
                        address     = it.getString("address") ?: "",
                        scheduledAt = it.getString("scheduled_at") ?: "",
                        itemCount   = (it.get("item_ids") as? List<*>)?.size ?: 0,
                        status      = it.getString("status") ?: "",
                        householdId = it.getString("household_id") ?: ""
                    )
                }
            }
    }

    private fun loadEarnings() {
        db.collection("pickups")
            .whereEqualTo("collector_id", collectorEntityId)
            .whereEqualTo("status", "completed")
            .addSnapshotListener { snaps, _ ->
                val docs = snaps?.documents ?: emptyList()
                _completedCount.value  = docs.size
                _totalKg.value         = docs.sumOf { it.getDouble("verified_kg") ?: 0.0 }
                _monthlyEarnings.value = docs.sumOf {
                    (it.getDouble("verified_kg") ?: 0.0) * 20
                }
                _payouts.value = docs.map { doc ->
                    PayoutRecord(
                        jobId     = doc.id,
                        amountKes = (doc.getDouble("verified_kg") ?: 0.0) * 20,
                        verifiedKg= doc.getDouble("verified_kg") ?: 0.0,
                        date      = doc.getString("scheduled_at") ?: ""
                    )
                }
            }
    }

    fun acceptJob(jobId: String, onDone: () -> Unit) {
        db.collection("pickups").document(jobId)
            .update(
                mapOf(
                    "status"       to "confirmed",
                    "collector_id" to collectorEntityId
                )
            )
            .addOnSuccessListener { onDone() }
    }
}