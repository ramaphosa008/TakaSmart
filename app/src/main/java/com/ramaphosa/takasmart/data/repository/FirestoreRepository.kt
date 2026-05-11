package com.ramaphosa.takasmart.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.models.EwasteItem
import com.ramaphosa.takasmart.models.Pickup
import com.ramaphosa.takasmart.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ── User ──────────────────────────────────────────────────────────────

    // Listen to current user document in real time
    fun getUserFlow(): Flow<User?> = callbackFlow {
        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                val user = snap?.let {
                    User(
                        uid           = it.id,
                        phone         = it.getString("phone") ?: "",
                        role          = it.getString("role") ?: "household",
                        pointsBalance = it.getLong("points_balance")?.toInt() ?: 0,
                        recycledKg    = it.getDouble("recycled_kg") ?: 0.0,
                        pickupsDone   = it.getLong("pickups_done")?.toInt() ?: 0
                    )
                }
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    // Create user document on first login
    suspend fun createUserIfNotExists(phone: String, role: String) {
        val snap = db.collection("users").document(uid).get().await()
        if (!snap.exists()) {
            db.collection("users").document(uid).set(
                mapOf(
                    "uid"            to uid,
                    "phone"          to phone,
                    "role"           to role,
                    "points_balance" to 0,
                    "recycled_kg"    to 0.0,
                    "pickups_done"   to 0,
                    "created_at"     to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    // ── E-waste items ─────────────────────────────────────────────────────

    // Listen to this user's pending items in real time
    fun getItemsFlow(): Flow<List<EwasteItem>> = callbackFlow {
        val listener = db.collection("ewaste_items")
            .whereEqualTo("user_id", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snaps, _ ->
                val items = snaps?.documents?.map { doc ->
                    EwasteItem(
                        id        = doc.id,
                        userId    = doc.getString("user_id") ?: "",
                        category  = doc.getString("category") ?: "",
                        condition = doc.getString("condition") ?: "",
                        model     = doc.getString("model") ?: "",
                        photoUrl  = doc.getString("photo_url") ?: "",
                        status    = doc.getString("status") ?: "pending"
                    )
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    // Save a new item — photoUrl comes from Cloudinary
    suspend fun addEwasteItem(
        category  : String,
        condition : String,
        model     : String,
        photoUrl  : String
    ): String {
        val ref = db.collection("ewaste_items").add(
            mapOf(
                "user_id"    to uid,
                "category"   to category,
                "condition"  to condition,
                "model"      to model,
                "photo_url"  to photoUrl,
                "status"     to "pending",
                "created_at" to FieldValue.serverTimestamp()
            )
        ).await()
        return ref.id
    }

    // ── Pickups ───────────────────────────────────────────────────────────

    // Listen to upcoming pickups for this household
    fun getUpcomingPickupFlow(): Flow<Pickup?> = callbackFlow {
        val listener = db.collection("pickups")
            .whereEqualTo("household_id", uid)
            .whereIn("status", listOf("requested", "confirmed", "en_route"))
            .limit(1)
            .addSnapshotListener { snaps, _ ->
                val doc    = snaps?.documents?.firstOrNull()
                val pickup = doc?.let {
                    Pickup(
                        id          = it.id,
                        householdId = it.getString("household_id") ?: "",
                        collectorId = it.getString("collector_id") ?: "",
                        address     = it.getString("address") ?: "",
                        scheduledAt = it.getString("scheduled_at") ?: "",
                        itemIds     = (it.get("item_ids") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        status      = it.getString("status") ?: ""
                    )
                }
                trySend(pickup)
            }
        awaitClose { listener.remove() }
    }

    // Listen to a single pickup by ID
    fun getPickupByIdFlow(pickupId: String): Flow<Pickup?> = callbackFlow {
        val listener = db.collection("pickups").document(pickupId)
            .addSnapshotListener { snap, _ ->
                val pickup = snap?.let {
                    Pickup(
                        id                 = it.id,
                        householdId        = it.getString("household_id") ?: "",
                        collectorId        = it.getString("collector_id") ?: "",
                        address            = it.getString("address") ?: "",
                        scheduledAt        = it.getString("scheduled_at") ?: "",
                        itemIds            = (it.get("item_ids") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        status             = it.getString("status") ?: "",
                        otp                = it.getString("otp") ?: "",
                        collectorLoggedKg  = it.getDouble("collector_logged_kg") ?: 0.0,
                        facilityVerifiedKg = it.getDouble("facility_verified_kg") ?: 0.0,
                        verifiedKg         = it.getDouble("verified_kg") ?: 0.0
                    )
                }
                trySend(pickup)
            }
        awaitClose { listener.remove() }
    }

    // Schedule a new pickup
    suspend fun schedulePickup(
        address     : String,
        scheduledAt : String,
        itemIds     : List<String>
    ): String {
        // Generate a 6-digit OTP for collector verification
        val otp = (100000..999999).random().toString()

        val ref = db.collection("pickups").add(
            mapOf(
                "household_id" to uid,
                "address"      to address,
                "scheduled_at" to scheduledAt,
                "item_ids"     to itemIds,
                "status"       to "requested",
                "otp"          to otp,
                "created_at"   to FieldValue.serverTimestamp()
            )
        ).await()
        return ref.id
    }

    // Update pickup status
    suspend fun updatePickupStatus(pickupId: String, status: String) {
        db.collection("pickups").document(pickupId)
            .update("status", status)
            .await()
    }

    // ── Points ────────────────────────────────────────────────────────────

    suspend fun deductPoints(userId: String, points: Int) {
        db.collection("users").document(userId)
            .update("points_balance", FieldValue.increment(-points.toLong()))
            .await()
    }
}