package com.example.tennistimetable.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.tennistimetable.R
import com.example.tennistimetable.firebase.FirestoreClass
import com.example.tennistimetable.models.Group
import com.example.tennistimetable.models.Reservation
import com.example.tennistimetable.models.User
import com.example.tennistimetable.utils.Constants
import kotlinx.android.synthetic.main.activity_add_trening_reservation.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.sql.Time
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddTreningReservationActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

    private var hoursList = ArrayList<String>()
    private var secureArrayList = ArrayList<String>()
    private var reservationListFB = ArrayList<Reservation>()
    private var coachesNamesList = ArrayList<String>()
    private var coachesFullList = ArrayList<User>()

    private lateinit var user: User
    private lateinit var group: Group

    private lateinit var adapterCoachesNames: ArrayAdapter<String>

    private var reservationDate: Long = 0
    private var courtNumber: Int = 0
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var coachId: String = ""
    private var coachfcm: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trening_reservation)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        FirestoreClass().loadUserData(this)

        var groupDocumentId = ""
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            groupDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)
            FirestoreClass().getGroupDetails(this, groupDocumentId)
        }




        populateArrayList()


        val adapterHours: ArrayAdapter<*> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, hoursList as List<Any?>)
        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_training_time.adapter = adapterHours


        setupActionBar()

        calenderPreparation()

        checkingDate()

        btn_create_training_reservation.setOnClickListener {

            createTrainingReservation(groupDocumentId)
        }

        reservation_training_calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val year = year.toString()
            val month = (month + 1).toString()
            val dayOfMonth = dayOfMonth.toString()
            reservationDatePreparation(year, month, dayOfMonth)

            if (courtNumber > 0) {
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getSelectedDateReservationList(this, reservationDate, courtNumber)
            }

            if (segmented_training_type.visibility == View.VISIBLE) {

            }
        }

        segmented_court_training.setOnCheckedChangeListener { group, checkedId ->
            if (tv_training_time.visibility == View.GONE) {
                tv_training_time.visibility = View.VISIBLE
                spinner_training_time.visibility = View.VISIBLE
            }

            when (checkedId) {
                R.id.rb_court1 ->
                    courtNumber = 1
                R.id.rb_court2 ->
                    courtNumber = 2
                R.id.rb_court3 ->
                    courtNumber = 3
                R.id.rb_court4 ->
                    courtNumber = 4
            }

            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getSelectedDateReservationList(this, reservationDate, courtNumber)
        }

        segmented_training_length.setOnCheckedChangeListener { group, checkedId ->

            val format = SimpleDateFormat("HH:mm")
            var d1 = format.parse("0:0") as Date


            when (checkedId) {
                R.id.rb_length1 ->
                    d1 = format.parse("0:30") as Date
                R.id.rb_length2 ->
                    d1 = format.parse("1:00") as Date
                R.id.rb_length3 ->
                    d1 = format.parse("1:30") as Date
                R.id.rb_length4 ->
                    d1 = format.parse("2:00") as Date
            }

            val ppstime = Time(d1.time)
            endTime = startTime + ppstime.time

            if (user.userType == 0) {
                tv_coach_name.visibility = View.VISIBLE
                spinner_coach_select.visibility = View.VISIBLE

                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getCoachesList(this)
            }
        }

        spinner_training_time.onItemSelectedListener = this
        spinner_coach_select.onItemSelectedListener = this

    }


    /*Ustawienie action bar`a*/
    private fun setupActionBar() {

        setSupportActionBar(toolbar_add_trening_reservation_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.add_training)
        }

        toolbar_add_trening_reservation_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /*Przygotowanie kalendarza, min/max date */
    private fun calenderPreparation() {
        reservation_training_calendar.setFirstDayOfWeek(2);
        reservation_training_calendar.minDate = System.currentTimeMillis() - 1000
        reservation_training_calendar.maxDate =
            System.currentTimeMillis() - 1000 + (1000 * 60 * 60 * 24 * 7)
    }

    /*Ustawia datę rezerwacji w formacie Long*/
    private fun reservationDatePreparation(year: String, month: String, dayOfMonth: String) {
        val format = SimpleDateFormat("yyyy-MM-dd")
        val date = "$year-$month-$dayOfMonth"
        val d1 = format.parse(date) as Date
        val ppstime = Time(d1.time)
        reservationDate = ppstime.time
    }

    /*Sprawdza czy użytkowanik wybrał datę a jak nie to za wybraną uznaje się dzisiejszą*/
    private fun checkingDate() {
        if (reservationDate == 0L) {
            val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val today = Date()
            val todayWithZeroTime: Date = formatter.parse(formatter.format(today))

            reservationDate = todayWithZeroTime.time
        }
    }

    private fun showTrainingLength() {
        if (reservationListFB.isNullOrEmpty()) {
            rb_length1.visibility = View.VISIBLE
            rb_length2.visibility = View.VISIBLE
            rb_length3.visibility = View.VISIBLE
            rb_length4.visibility = View.VISIBLE
            segmented_training_type.visibility = View.VISIBLE
            segmented_training_length.visibility = View.VISIBLE
        }
        for (reservation in reservationListFB) {
            if (startTime + 5400000 == reservation.hourStart) {
                rb_length1.visibility = View.VISIBLE
                rb_length2.visibility = View.VISIBLE
                rb_length3.visibility = View.VISIBLE
                rb_length4.visibility = View.INVISIBLE
                segmented_training_type.visibility = View.VISIBLE
                segmented_training_length.visibility = View.VISIBLE

            } else if (startTime + 3600000 == reservation.hourStart) {
                rb_length1.visibility = View.VISIBLE
                rb_length2.visibility = View.VISIBLE
                rb_length3.visibility = View.INVISIBLE
                rb_length4.visibility = View.INVISIBLE
                segmented_training_type.visibility = View.VISIBLE
                segmented_training_length.visibility = View.VISIBLE

            } else if (startTime + 1800000 == reservation.hourStart) {
                rb_length1.visibility = View.VISIBLE
                rb_length2.visibility = View.INVISIBLE
                rb_length3.visibility = View.INVISIBLE
                rb_length4.visibility = View.INVISIBLE
                segmented_training_type.visibility = View.VISIBLE
                segmented_training_length.visibility = View.VISIBLE
            } else {
                rb_length1.visibility = View.VISIBLE
                rb_length2.visibility = View.VISIBLE
                rb_length3.visibility = View.VISIBLE
                rb_length4.visibility = View.VISIBLE
                segmented_training_type.visibility = View.VISIBLE
                segmented_training_length.visibility = View.VISIBLE
            }
        }
    }

    fun getUserData(user: User) {
        this.user = user
    }

    fun getGroupDetails(group: Group) {
        this.group = group
    }

    /*Funkcja odpowiedzialna za pokazywanie dostępnych godzin na rezerwację kortu*/
    fun setupAvailableHours(reservationList: ArrayList<Reservation>) {
        hideProgressDialog()
        hoursList.clear()
        reservationListFB.clear()
        secureArrayList.clear()
        populateArrayList()
        reservationListFB.addAll(reservationList)

        for (reservation in reservationList) {
            for (i in hoursList.indices) {
                val format = SimpleDateFormat("HH:mm")
                val d1 = format.parse(hoursList[i]) as Date
                val ppstime = Time(d1.time)
                val temp: Long = ppstime.time
                if (reservation.hourStart <= temp && temp < reservation.hourEnd) {

                    secureArrayList.add(hoursList[i])

                }
            }
            for (position in secureArrayList) {

                hoursList.remove(position)

            }
        }

        showTrainingLength()

    }

    fun setupAvailableCoaches(coachesList: ArrayList<User>) {
        hideProgressDialog()
        coachesNamesList.clear()
        coachesFullList.clear()

        coachesFullList.addAll(coachesList)

        for (coach in coachesList) {
            coachesNamesList.add(coach.name)
        }

        adapterCoachesNames = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            coachesNamesList as List<String>
        )
        adapterCoachesNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_coach_select.adapter = adapterCoachesNames


    }


    private fun createTrainingReservation(groupDocumentId: String) {


        if (validateForm()) {

            val assignedUsersArrayList: ArrayList<String> = ArrayList()

            if (user.userType == 0) {
                assignedUsersArrayList.add(getCurrentUserID())
            } else {
                coachId = getCurrentUserID()
            }

            val reservation = Reservation(
                courtNumber,
                assignedUsersArrayList,
                reservationDate,
                startTime,
                endTime,
                coachId,
                groupDocumentId
            )

            if (group.coachAdded == 0) {
                FirestoreClass().addCoachToGroup(this, coachId, groupDocumentId)
            }

            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().createReservation(this, reservation)
        }


    }

    private fun validateForm(): Boolean {

        if (reservationDate == 0L) {
            Toast.makeText(this, "Data nie może być pusta", Toast.LENGTH_SHORT).show()
            return false
        }
        if (courtNumber == 0) {
            Toast.makeText(this, "Wybierz kort", Toast.LENGTH_SHORT).show()
            return false
        }
        if (startTime == 0L) {
            Toast.makeText(this, "Wybierz godzinę rozpoczęcia", Toast.LENGTH_SHORT).show()
            return false
        }
        if (endTime == 0L) {
            Toast.makeText(this, "Wybierz długość trwania", Toast.LENGTH_SHORT).show()
            return false
        }
        if (coachId == "") {
            Toast.makeText(this, "Wskaż trenera", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    fun reservationCreatedSuccessfully() {
        hideProgressDialog()

        SendNotificationToUserAsyncTask(coachfcm).execute()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun populateArrayList() {
        hoursList.add("8:00")
        hoursList.add("8:30")
        hoursList.add("9:00")
        hoursList.add("9:30")
        hoursList.add("10:00")
        hoursList.add("10:30")
        hoursList.add("11:00")
        hoursList.add("11:30")
        hoursList.add("12:00")
        hoursList.add("12:30")
        hoursList.add("13:00")
        hoursList.add("13:30")
        hoursList.add("14:00")
        hoursList.add("14:30")
        hoursList.add("15:00")
        hoursList.add("15:30")
        hoursList.add("16:00")
        hoursList.add("16:30")
        hoursList.add("17:00")
        hoursList.add("17:30")
        hoursList.add("18:00")
        hoursList.add("18:30")
        hoursList.add("19:00")
        hoursList.add("19:30")
        hoursList.add("20:00")
        hoursList.add("20:30")
        hoursList.add("21:00")
        hoursList.add("21:30")
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if (parent != null) {
            if (parent.id == R.id.spinner_training_time) {
                val itemSelected: String = parent?.getItemAtPosition(position).toString()
                val format = SimpleDateFormat("HH:mm")
                val d1 = format.parse(itemSelected) as Date
                val ppstime = Time(d1.time)
                startTime = ppstime.time

                showTrainingLength()
            }

            if (parent.id == R.id.spinner_coach_select && user.userType == 0) {
                val itemSelected: String = parent?.getItemAtPosition(position).toString()
                for (coach in coachesFullList) {
                    if (coach.name == itemSelected) {
                        coachId = coach.id
                        coachfcm = coach.fcmToken
                    }
                }
            }
        }


    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private inner class SendNotificationToUserAsyncTask(val token: String) :
        AsyncTask<Any, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }


        override fun doInBackground(vararg params: Any?): String {
            var result: String

            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false
                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Nowy trening")
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "Masz nowy trening dodany przez ${user.name}"
                )
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()


                val httpResult: Int =
                    connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {

                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try {

                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {

                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {

                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }

            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            hideProgressDialog()

            Log.e("JSON Response Result", result)
        }


    }

}