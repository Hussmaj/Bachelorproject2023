package com.hdtchat.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserViewModel(application: Application): AndroidViewModel(application) {

    private val repository: UserRepository
    private var userDao: Dao

    init {
        userDao = Userdatabase.getDatabase(application).dao()
        repository = UserRepository(userDao)
    }

    fun verifyUser(username: String, password: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.verifyUser(username, password)
            resultLiveData.postValue(result)
        }
        return resultLiveData
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }
}




