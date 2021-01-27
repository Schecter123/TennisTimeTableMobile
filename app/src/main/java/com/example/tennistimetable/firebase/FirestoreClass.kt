package com.example.tennistimetable.firebase

import  android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.tennistimetable.activities.*
import com.example.tennistimetable.models.Group
import com.example.tennistimetable.models.Reservation
import com.example.tennistimetable.models.User
import com.example.tennistimetable.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Błąd podczas rejestracji",
                    e
                )
            }
    }

    fun createGroup(activity: CreateGroupActivity, groupInfo: Group) {
        mFireStore.collection(Constants.GROUPS).document()
            .set(groupInfo, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Utworzono nową grupę.")
                Toast.makeText(activity, "Utworzono nową grupę.", Toast.LENGTH_SHORT).show()
                activity.groupCreatedSuccessfully()
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Błąd podczas tworzenia grupy",
                    e
                )
            }
    }

    fun createReservation(
        activity: Activity,
        reservationInfo: Reservation
    ) {
        mFireStore.collection(Constants.RESERVATIONS).document()
            .set(reservationInfo, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Utworzono rezerwajcę treningową")
                Toast.makeText(activity, "Utworzono nową rezerwajcę", Toast.LENGTH_SHORT).show()
                when (activity) {
                    is AddTreningReservationActivity -> {
                        activity.reservationCreatedSuccessfully()
                    }
                    is AddSparingReservationActivity -> {
                        activity.reservationCreatedSuccessfully()
                    }
                }

            }.addOnFailureListener { e ->
                when (activity) {
                    is AddTreningReservationActivity -> {
                        activity.hideProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName, "Błąd podczas tworzenia grupy",
                            e
                        )
                    }
                    is AddSparingReservationActivity -> {
                        activity.hideProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName, "Błąd podczas tworzenia grupy",
                            e
                        )
                    }
                }
            }
    }

    fun loadUserData(activity: Activity, readGroupsList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readGroupsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                    is AddTreningReservationActivity -> {
                        activity.getUserData(loggedInUser)
                    }

                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Błąd podczas pobierania danych", e)
            }
    }

    fun getGroupsList(activity: MainActivity) {
        mFireStore.collection(Constants.GROUPS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val groupList: ArrayList<Group> = ArrayList()
                for (i in document.documents) {
                    val group = i.toObject(Group::class.java)!!
                    group.documentId = i.id
                    groupList.add(group)
                }

                activity.populateGroupListToUI(groupList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Błąd podczas tworzenia grupy", e)
            }
    }

    fun getReservationList(activity: ReservationListActivity, groupId: String) {
        mFireStore.collection(Constants.RESERVATIONS)
            .whereEqualTo(Constants.ASSIGNED_GROUP, groupId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val reservationList: ArrayList<Reservation> = ArrayList()
                for (i in document.documents) {
                    val reservation = i.toObject(Reservation::class.java)!!
                    reservation.documentId = i.id
                    if (reservation.assignedTo.contains(getCurrentUserId()) || reservation.coachId == getCurrentUserId()) {
                        reservationList.add(reservation)
                    }
                }
                activity.populateReservationListToUI(reservationList)
            }

    }

    fun getPlayersList(activity: AddSparingReservationActivity, userType: Int = 0) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.USER_TYPE, userType)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val playerList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val player = i.toObject(User::class.java)!!
                    if (player.id != getCurrentUserId()){
                        playerList.add(player)
                    }
                }
                activity.setupAvailablePlayers(playerList)
            }

    }

    fun getSelectedDateReservationList(activity: Activity, date: Long, courtNumber: Int) {
        mFireStore.collection(Constants.RESERVATIONS)
            .whereEqualTo(Constants.RESERVATION_DATE, date)
            .whereEqualTo(Constants.COURT_NUMBER, courtNumber)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val reservationList: ArrayList<Reservation> = ArrayList()
                for (i in document.documents) {
                    val reservation = i.toObject(Reservation::class.java)!!
                    reservation.documentId = i.id
                    reservationList.add(reservation)
                }

                when (activity) {
                    is AddTreningReservationActivity -> {
                        activity.setupAvailableHours(reservationList)
                    }
                    is AddSparingReservationActivity -> {
                        activity.setupAvailableHours(reservationList)
                    }
                }


            }.addOnFailureListener { e ->
                when (activity) {
                    is AddTreningReservationActivity -> {
                        activity.hideProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Błąd podczas tworzenia listy rezerwacji",
                            e
                        )
                    }
                    is AddSparingReservationActivity -> {
                        activity.hideProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Błąd podczas tworzenia listy rezerwacji",
                            e
                        )
                    }
                }
                Log.e(activity.javaClass.simpleName, "Błąd podczas tworzenia listy rezerwacji", e)
            }
    }

    fun getCoachesList(activity: AddTreningReservationActivity, userType: Int = 1) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.USER_TYPE, userType)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val coachesList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val coach = i.toObject(User::class.java)!!
                    coachesList.add(coach)
                }

                activity.setupAvailableCoaches(coachesList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Błąd podczas tworzenia listy trenerów", e)
            }
    }

    fun addCoachToGroup(activity: AddTreningReservationActivity, coachId: String, groupId: String) {
        mFireStore.collection(Constants.GROUPS)
            .document(groupId)
            .update(Constants.ASSIGNED_TO, FieldValue.arrayUnion(coachId))
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Zaktualizowano grupę")
            }
        mFireStore.collection(Constants.GROUPS)
            .document(groupId)
            .update(Constants.COACH_ADDED, 1)
            .addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "Błąd podczas aktualizacji")
            }
    }

    fun addSparingPartnerToGroup(activity: AddSparingReservationActivity, sparingpartnerId: String, groupId: String) {
        mFireStore.collection(Constants.GROUPS)
            .document(groupId)
            .update(Constants.ASSIGNED_TO, FieldValue.arrayUnion(sparingpartnerId))
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Zaktualizowano grupę")
            }
    }


    fun getGroupDetails(activity: Activity, documentId: String) {
        mFireStore.collection(Constants.GROUPS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val group = document.toObject(Group::class.java)!!
                group.documentId = documentId

                when (activity) {
                    is ReservationListActivity -> {
                        activity.groupDeatils(group)
                    }
                    is AddTreningReservationActivity -> {
                        activity.getGroupDetails(group)
                    }
                }


            }.addOnFailureListener { e ->
                when (activity) {
                    is ReservationListActivity -> {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Bład podczas pobierania grupy")
                    }
                    is AddTreningReservationActivity -> {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Bład podczas pobierania grupy")
                    }
                }

            }
    }

    fun updateUserProfileData(
        activity: Activity, userHashMap: HashMap<String, Any>
    ) {
        mFireStore.collection(Constants.USERS) // Collection Name
            .document(getCurrentUserId()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.e(activity.javaClass.simpleName, "Profil zaktualizowany")

                Toast.makeText(activity, "Profil zaktualizowany", Toast.LENGTH_SHORT).show()

                // Notify the success result.

                when (activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }


            }
            .addOnFailureListener { e ->
                when (activity) {
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Błąd podczas aktualizacji",
                            e
                        )
                    }
                }
            }
    }

        fun deleteReservation(activity: ReservationListActivity, reservationId: String) {
            mFireStore.collection(Constants.RESERVATIONS)
                .document(reservationId)
                .delete()
                .addOnSuccessListener {
                    activity.updateReservationListSuccess()
                }
        }


        fun getCurrentUserId(): String {
            var currentUser = FirebaseAuth.getInstance().currentUser
            var currentUserID = ""
            if (currentUser != null) {
                currentUserID = currentUser.uid
            }
            return currentUserID
        }


    }