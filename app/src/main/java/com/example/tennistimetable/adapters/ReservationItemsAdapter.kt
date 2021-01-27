package com.example.tennistimetable.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tennistimetable.R
import com.example.tennistimetable.activities.ReservationListActivity
import com.example.tennistimetable.models.Reservation
import kotlinx.android.synthetic.main.item_reservation_test.view.*
import java.text.SimpleDateFormat
import java.util.*


open class ReservationItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Reservation>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ReservationItemsAdapter.MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_reservation_test, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        holder.itemView.tv_date_set.text =
            SimpleDateFormat("yyyy/MM/dd").format(Date(model.reservationDate))

        holder.itemView.tv_hour_start_set.text = SimpleDateFormat("HH:mm").format(Date(model.hourStart))

        holder.itemView.tv_hour_end_set.text = SimpleDateFormat("HH:mm").format(Date(model.hourEnd))

        holder.itemView.tv_court_set.text = model.courtNumber.toString()

        holder.itemView.ib_edit_reservation.setOnClickListener {  }

        holder.itemView.ib_delete_reservation.setOnClickListener { alertDialogForDeleteList(position) }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun alertDialogForDeleteList(position: Int) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Uwaga")
        //set message for alert dialog
        builder.setMessage("Czy jesteś pewien, że chcesz anulować rezerwację")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Tak") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is ReservationListActivity) {
                context.deleteReservation(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("Nie") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }



    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}