package com.example.peertutoringapplication.data.repository

import com.example.peertutoringapplication.data.datasource.UserDao
import com.example.peertutoringapplication.data.model.User

class UserRepository(
    private val userDao: UserDao
) {

    suspend fun registerUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
}