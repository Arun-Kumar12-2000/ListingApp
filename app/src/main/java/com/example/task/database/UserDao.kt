package com.example.task.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserTable>)

    @Query("SELECT * FROM users WHERE firstName LIKE :query || '%' OR lastName LIKE :query || '%'")
    suspend fun searchUsers(query: String): List<UserTable>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserTable>

    @Query("SELECT * FROM users LIMIT :limit")
    suspend fun getPaginatedUsers(limit: Int): List<UserTable>

}
