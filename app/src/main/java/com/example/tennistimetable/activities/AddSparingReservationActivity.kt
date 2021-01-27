package com.example.tennistimetable.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.tennistimetable.R
import com.example.tennistimetable.firebase.FirestoreClass
import com.example.tennistimetable.models.Reservation
import com.example.tennistimetable.models.User
import com.example.tennistimetable.utils.Constants
import kotlinx.android.synthetic.main.activity_add_sparing_reservation.*
import kotlinx.android.synthetic.main.activity_add_trening_reservation.*
import java.sql.Time
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class AddSparingReservationActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

    private var hoursList = ArrayList<String>()
    private var secureArrayList = ArrayList<String>()
    private var reservationListFB = ArrayList<Reservation>()
    private var usersList = ArrayList<String>()
    private var playersNamesList = ArrayList<String>()
    private var playersFullList = ArrayList<User>()

    private lateinit var adapterUsersList: ArrayAdapter<String>
    private lateinit var adapterPlayersNames: ArrayAdapter<String>

    private var reservationDate: Long = 0
    private var courtNumber: Int = 0
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var sparingpartnerId: String = ""
    private var sparingpartenrfcm: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sparing_reservation)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        var groupDocumentId = ""
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            groupDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)
        }

        populateArrayList()

        val adapterHours: ArrayAdapter<*> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, hoursList as List<Any?>)
        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_sparing_time.adapter = adapterHours

        setupActionBar()

        calenderPreparation()

        checkingDate()

        btn_create_sparing_reservation.setOnClickListener {

            createSparingReservation(groupDocumentId)
        }

        reservation_calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val year = year.toString()
            val month = (month + 1).toString()
            val dayOfMonth = dayOfMonth.toString()
            reservationDatePreparation(year, month, dayOfMonth)

            if (courtNumber > 0) {
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getSelectedDateReservationList(this, reservationDate, courtNumber)
            }

            if (segmented_sparing_type.visibility == View.VISIBLE) {

            }
        }

        segmented_court_sparing.setOnCheckedChangeListener { group, checkedId ->
            if (tv_sparing_time.visibility == View.GONE) {
                tv_sparing_time.visibility = View.VISIBLE
                spinner_sparing_time.visibility = View.VISIBLE
            }

            when (checkedId) {
                R.id.rb_court1_spar ->
                    courtNumber = 1
                R.id.rb_court2_spar ->
                    courtNumber = 2
                R.id.rb_court3_spar ->
                    courtNumber = 3
                R.id.rb_court4_spar ->
                    courtNumber = 4
            }

            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getSelectedDateReservationList(this, reservationDate, courtNumber)
        }

        segmented_sparing_length.setOnCheckedChangeListener { group, checkedId ->

            val format = SimpleDateFormat("HH:mm")
            var d1 = format.parse("0:0") as Date


            when (checkedId) {
                R.id.rb_length1_spar ->
                    d1 = format.parse("0:30") as Date
                R.id.rb_length2_spar ->
                    d1 = format.parse("1:00") as Date
                R.id.rb_length3_spar ->
                    d1 = format.parse("1:30") as Date
                R.id.rb_length4_spar ->
                    d1 = format.parse("2:00") as Date
            }

            val ppstime = Time(d1.time)
            endTime = startTime + ppstime.time


            tv_sparing_partner_name.visibility = View.VISIBLE
            spinner_partner_select.visibility = View.VISIBLE

            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getPlayersList(this)

        }

        spinner_sparing_time.onItemSelectedListener = this
        spinner_partner_select.onItemSelectedListener = this

    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_add_sparing_reservation_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_sparing_reservation_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun calenderPreparation() {
        reservation_calendar.setFirstDayOfWeek(2);
        reservation_calendar.minDate = System.currentTimeMillis() - 1000
        reservation_calendar.maxDate =
            System.currentTimeMillis() - 1000 + (1000 * 60 * 60 * 24 * 7)
    }

    private fun reservationDatePreparation(year: String, month: String, dayOfMonth: String) {
        val format = SimpleDateFormat("yyyy-MM-dd")
        val date = "$year-$month-$dayOfMonth"
        val d1 = format.parse(date) as Date
        val ppstime = Time(d1.time)
        reservationDate = ppstime.time
    }

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
            rb_length1_spar.visibility = View.VISIBLE
            rb_length2_spar.visibility = View.VISIBLE
            rb_length3_spar.visibility = View.VISIBLE
            rb_length4_spar.visibility = View.VISIBLE
            segmented_sparing_type.visibility = View.VISIBLE
            segmented_sparing_length.visibility = View.VISIBLE
        }
        for (reservation in reservationListFB) {
            if (startTime + 5400000 == reservation.hourStart) {
                rb_length1_spar.visibility = View.VISIBLE
                rb_length2_spar.visibility = View.VISIBLE
                rb_length3_spar.visibility = View.VISIBLE
                rb_length4_spar.visibility = View.INVISIBLE
                segmented_sparing_type.visibility = View.VISIBLE
                segmented_sparing_length.visibility = View.VISIBLE

            } else if (startTime + 3600000 == reservation.hourStart) {
                rb_length1_spar.visibility = View.VISIBLE
                rb_length2_spar.visibility = View.VISIBLE
                rb_length3_spar.visibility = View.INVISIBLE
                rb_length4_spar.visibility = View.INVISIBLE
                segmented_sparing_type.visibility = View.VISIBLE
                segmented_sparing_length.visibility = View.VISIBLE

            } else if (startTime + 1800000 == reservation.hourStart) {
                rb_length1_spar.visibility = View.VISIBLE
                rb_length2_spar.visibility = View.INVISIBLE
                rb_length3_spar.visibility = View.INVISIBLE
                rb_length4_spar.visibility = View.INVISIBLE
                segmented_sparing_type.visibility = View.VISIBLE
                segmented_sparing_length.visibility = View.VISIBLE
            } else {
                rb_length1_spar.visibility = View.VISIBLE
                rb_length2_spar.visibility = View.VISIBLE
                rb_length3_spar.visibility = View.VISIBLE
                rb_length4_spar.visibility = View.VISIBLE
                segmented_sparing_type.visibility = View.VISIBLE
                segmented_sparing_length.visibility = View.VISIBLE
            }
        }
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

    fun setupAvailablePlayers(playersList: ArrayList<User>) {
        hideProgressDialog()
        playersNamesList.clear()
        playersFullList.clear()

        playersFullList.addAll(playersList)

        for (player in playersList) {
            playersNamesList.add(player.name)
        }

        adapterPlayersNames = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            playersNamesList as List<String>
        )
        adapterPlayersNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_partner_select.adapter = adapterPlayersNames


    }

    private fun createSparingReservation(groupDocumentId: String) {

        if (validateForm()) {
            showProgressDialog(resources.getString(R.string.please_wait))
            val assignedUsersArrayList: ArrayList<String> = ArrayList()

            assignedUsersArrayList.add(getCurrentUserID())
            assignedUsersArrayList.add(sparingpartnerId)
            val reservation = Reservation(
                courtNumber,
                assignedUsersArrayList,
                reservationDate,
                startTime,
                endTime,
                "",
                groupDocumentId
            )
            FirestoreClass().addSparingPartnerToGroup(this, sparingpartnerId, groupDocumentId)

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
        if (sparingpartnerId == "") {
            Toast.makeText(this, "Wskaż z kim chcesz zagrać", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    fun reservationCreatedSuccessfully() {
        hideProgressDialog()

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
            if (parent.id == R.id.spinner_sparing_time) {
                val itemSelected: String = parent?.getItemAtPosition(position).toString()
                val format = SimpleDateFormat("HH:mm")
                val d1 = format.parse(itemSelected) as Date
                val ppstime = Time(d1.time)
                startTime = ppstime.time

                showTrainingLength()
            }
            if (parent.id == R.id.spinner_partner_select) {
                val itemSelected: String = parent?.getItemAtPosition(position).toString()
                for (player in playersFullList) {
                    if (player.name == itemSelected) {
                        sparingpartnerId = player.id
                        sparingpartenrfcm = player.fcmToken
                    }
                }
            }
        }


    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}