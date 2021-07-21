package com.decagon.mobifind.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.R

class PhoneContactAdapter(
    private var onStatusClicked: OnclickPhoneContact,
    private val list: List<String>
) : RecyclerView.Adapter<PhoneContactAdapter.PhoneViewHolder>() {

    private val phoneList: List<String> = list.sorted()

    inner class PhoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var statusView: TextView = itemView.findViewById(R.id.phone_contact_status)
        var nameView: TextView = itemView.findViewById(R.id.phone_contact_name)
        var phoneNumberView: TextView = itemView.findViewById(R.id.phone_contact_number)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.phone_contact_recyclerview, parent, false)

        return PhoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhoneViewHolder, position: Int) {
        val (name,number) = phoneList[position].split(":")
        holder.nameView.text = name
        holder.phoneNumberView.text = number
        holder.statusView.setOnClickListener {
            onStatusClicked.onClickStatus(name, number)
        }

    }

    override fun getItemCount(): Int {
        return phoneList.size
    }

}