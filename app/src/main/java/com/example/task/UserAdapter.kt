package com.example.task

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.task.database.UserTable
import com.example.task.databinding.ItemUserBinding
import com.example.task.model.Coordinates
import com.example.task.model.Dob
import com.example.task.model.Id
import com.example.task.model.Location
import com.example.task.model.Login
import com.example.task.model.Name
import com.example.task.model.Picture
import com.example.task.model.Registered
import com.example.task.model.Street
import com.example.task.model.Timezone
import com.example.task.model.User

class UserAdapter(private val users: MutableList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, position: Int) {
            Log.d("TAG", "userList: "+user)
            Glide.with(binding.root.context)
                .load(user.picture.large)
                .into(binding.thumbnailImageView)

            binding.userNameTextView.text = "${user.name.first} ${user.name.last}"

            val imageHeight = if (position % 2 == 0) 500 else 200
            binding.thumbnailImageView.layoutParams.height = imageHeight

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, DetailsScreenActivity::class.java).apply {
                    putExtra("email_id", user.email)
                    putExtra("city_name", user.location.city)
                    putExtra("state_name", user.location.state)
                    putExtra("country_name", user.location.country)
                    putExtra("profile_img", user.picture.large)
                    putExtra("first_name", user.name.first)
                    putExtra("last_name", user.name.last)

                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], position)
    }

    override fun getItemCount(): Int = users.size

    fun setUsers(newUsers: List<UserTable>) {
        users.clear()
        users.addAll(newUsers.map {
            User(
                gender = "unknown",
                name = Name("", it.firstName, it.lastName),
                location = Location(
                    street = Street(0, ""),
                    it.city,           // Fetch city name
                    it.state,         // Fetch state name
                    it.country,     // Fetch country name
                    "",
                    coordinates = Coordinates("", ""),
                    timezone = Timezone("", "")
                ),
                email = it.email, // Fetch email
                login = Login("", "", "", "", "", "", ""),
                dob = Dob("", 0),
                registered = Registered("", 0),
                phone = "",
                cell = "",
                id = Id("", ""),
                picture = Picture(it.imageUrl, "", ""),
                nat = ""
            )
        })
        notifyDataSetChanged()
    }

    fun addUsers(newUsers: List<User>) {
        val startPosition = users.size
        users.addAll(newUsers)
        notifyItemRangeInserted(startPosition, newUsers.size)
    }

    private val userList = mutableListOf<UserTable>()

    fun updateUsers(newUsers: List<UserTable>) {
        Log.d("LoadData", "Updating adapter with ${newUsers.size} users")
        userList.clear() // If you need to replace data
        userList.addAll(newUsers) // Append new data
        notifyDataSetChanged()
    }


}
