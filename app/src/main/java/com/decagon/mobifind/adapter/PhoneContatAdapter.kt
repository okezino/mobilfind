package com.decagon.mobifind.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.R
import com.decagon.mobifind.model.data.Contact

class PhoneContactAdapter(
    private var onStatusClicked: OnclickPhoneContact,
    private val phoneList: ArrayList<Contact>
) : RecyclerView.Adapter<PhoneContactAdapter.PhoneViewHolder>() {


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
        holder.nameView.text = phoneList[position].name
        holder.phoneNumberView.text = phoneList[position].number
        holder.statusView.setOnClickListener {
            onStatusClicked.onClickStatus(phoneList[position].name, phoneList[position].number)
        }

    }

    override fun getItemCount(): Int {
        return phoneList.size
    }

}