package com.example.task.database

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "users")
data class UserTable(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val imageUrl: String,
    val email: String,       // Add email
    val country: String,     // Add country
    val state: String,       // Add state
    val city: String         // Add city
)
