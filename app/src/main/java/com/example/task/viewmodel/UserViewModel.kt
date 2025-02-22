package com.example.task.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.task.database.UserDatabase
import com.example.task.database.UserTable
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = UserDatabase.getDatabase(application).userDao()

    private val _users = MutableLiveData<List<UserTable>>()
    val users: LiveData<List<UserTable>> get() = _users

    fun insertUsers(users: List<UserTable>) {
        viewModelScope.launch {
            userDao.insertUsers(users)
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            val filteredUsers = userDao.searchUsers(query)
            Log.d("SearchDebug", "Filtered Users: $filteredUsers")  // Debugging
            _users.postValue(filteredUsers)
        }
    }

    fun getPaginatedUsers(limit: Int) {
        viewModelScope.launch {
            _users.postValue(userDao.getPaginatedUsers(limit))
        }
    }



    fun getAllUsers() {
        viewModelScope.launch {
            _users.postValue(userDao.getAllUsers())
        }
    }
}
