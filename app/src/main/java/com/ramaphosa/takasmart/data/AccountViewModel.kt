package com.ramaphosa.takasmart.data

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AccountData(
    val role: String = "",
    val name: String = "",
    val uniqueId: String = ""
)

class AccountViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val uid =
        FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _accountData =
        MutableStateFlow(AccountData())

    val accountData: StateFlow<AccountData> =
        _accountData

    init {
        loadAccountData()
    }

    private fun loadAccountData() {

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val role =
                    doc.getString("role") ?: ""

                val name =
                    doc.getString("name") ?: ""

                val uniqueId = when(role) {

                    "household" ->
                        doc.getString("phone") ?: ""

                    "collector",
                    "facility" ->
                        doc.getString("entity_id") ?: ""

                    else -> ""
                }

                _accountData.value =
                    AccountData(
                        role = role,
                        name = name,
                        uniqueId = uniqueId
                    )
            }
    }
}