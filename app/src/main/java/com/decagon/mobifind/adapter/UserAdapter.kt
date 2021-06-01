package com.decagon.mobifind.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.databinding.DashBoardRecyclerviewItemBinding
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Track
import com.decagon.mobifind.utils.load

class UserAdapter(private val clickListener: ClickListener? = null) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users = mutableListOf<Track>()

    inner class UserViewHolder(val binding: DashBoardRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Track) {
            clickListener?.let { clickListener ->
                binding.root.setOnClickListener {
                    clickListener.onClick(user)
                }
            }
            binding.fullName.text = user.name
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
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    class ClickListener(val clickListener: (user: Track) -> Unit) {
        fun onClick(user: Track) = clickListener(user)
    }

    fun loadUsers(users: List<Track>) {
        this.users = users as MutableList<Track>
        notifyDataSetChanged()
    }

    fun getTrack(id: Int): Track = users[id]
}