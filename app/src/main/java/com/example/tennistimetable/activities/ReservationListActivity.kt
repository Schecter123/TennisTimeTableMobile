package com.example.tennistimetable.activities

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tennistimetable.R
import com.example.tennistimetable.adapters.ReservationItemsAdapter
import com.example.tennistimetable.firebase.FirestoreClass
import com.example.tennistimetable.models.Group
import com.example.tennistimetable.models.Reservation
import com.example.tennistimetable.utils.Constants
import kotlinx.android.synthetic.main.activity_reservation_list.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class ReservationListActivity : BaseActivity() {

    companion object {
        const val CREATE_RESERVATION_REQUEST_CODE: Int = 12
    }

    private lateinit var mGroupDetails: Group
    private lateinit var reservationList: ArrayList<Reservation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_list)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        var groupDocumentId = ""
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            groupDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getGroupDetails(this, groupDocumentId)


        fab_add_reservation.setOnClickListener {

            if (mGroupDetails.groupType == 0) {
                val intent = Intent(this, AddSparingReservationActivity::class.java)
                intent.putExtra(Constants.DOCUMENT_ID, mGroupDetails.documentId)
                startActivityForResult(intent, CREATE_RESERVATION_REQUEST_CODE)

            } else if (mGroupDetails.groupType == 1) {
                val intent = Intent(this, AddTreningReservationActivity::class.java)
                intent.putExtra(Constants.DOCUMENT_ID, mGroupDetails.documentId)
                startActivityForResult(intent, CREATE_RESERVATION_REQUEST_CODE)
            }

        }

    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_reservation_list_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mGroupDetails.name
        }

        toolbar_reservation_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun groupDeatils(group: Group) {
        hideProgressDialog()

        mGroupDetails = group

        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getReservationList(this, mGroupDetails.documentId)


        /* if(group.reservationList.size > 0)
         {
             //group.reservationList
             rv_reservation_list.layoutManager = LinearLayoutManager(this)

             val adapter = ReservationItemsAdapter(this,group.reservationList)
         } else {
             rv_reservation_list.visibility = View.GONE
             tv_no_reservations_available.visibility = View.VISIBLE
         }

         */
    }

    fun populateReservationListToUI(reservationList: ArrayList<Reservation>) {
        hideProgressDialog()
        this.reservationList = reservationList
        if (reservationList.size > 0) {
            rv_reservation_list.layoutManager = LinearLayoutManager(this)
            rv_reservation_list.setHasFixedSize(true)

            val adapter = ReservationItemsAdapter(this, reservationList)
            rv_reservation_list.adapter = adapter

        } else {
            rv_reservation_list.visibility = View.GONE
        }
    }

    fun updateReservationListSuccess() {

        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getGroupDetails(this, mGroupDetails.documentId)
    }

    fun deleteReservation(position: Int) {
        reservationList[position].documentId

        //SendNotificationToUserAsyncTask()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().deleteReservation(this, reservationList[position].documentId)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CREATE_RESERVATION_REQUEST_CODE) {
            FirestoreClass().getReservationList(this, mGroupDetails.documentId)
        } else {
            Log.e("Anulowano", "Anulowano")
        }
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
                if (mGroupDetails.groupType == 0) {
                    dataObject.put(
                        Constants.FCM_KEY_MESSAGE,
                        "Odwołano sparing"
                    )
                } else {
                    dataObject.put(
                        Constants.FCM_KEY_MESSAGE,
                        "Odwołano trening"
                    )
                }

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