package com.example.tennistimetable.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tennistimetable.R
import com.example.tennistimetable.models.Group
import kotlinx.android.synthetic.main.item_group.view.*

open class GroupItemsAdapter(private val context: Context, private var list: ArrayList<Group>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_group, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            if (model.groupType == 0)
                holder.itemView.iv_board_image.setBackgroundResource(R.drawable.s2_icon)
            else holder.itemView.iv_board_image.setBackgroundResource(R.drawable.t_icon)
        }
        holder.itemView.tv_name.text = model.name

        holder.itemView.setOnClickListener {
            if (onClickListener != null){
                onClickListener!!.onClick(position, model)
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position: Int, model: Group)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}