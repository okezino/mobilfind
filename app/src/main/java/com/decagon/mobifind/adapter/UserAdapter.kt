package com.decagon.mobifind.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.databinding.DashBoardRecyclerviewItemBinding
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.utils.load

class UserAdapter(private val clickListener: ClickListener) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users = mutableListOf<MobifindUser>()

    inner class UserViewHolder(val binding: DashBoardRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: MobifindUser) {
            binding.fullName.text = user.userName
            binding.phoneNumber.text = user.phoneNumber
            user.photoUri?.let {
                binding.profileImage.load(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = DashBoardRecyclerviewItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.itemView.setOnClickListener { clickListener.onClick(user) }
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    class ClickListener(val clickListener: (user: MobifindUser) -> Unit) {
        fun onClick(user: MobifindUser) = clickListener(user)
    }

    fun loadUsers(users: List<MobifindUser>) {
        this.users = users as MutableList<MobifindUser>
        notifyDataSetChanged()
    }
}