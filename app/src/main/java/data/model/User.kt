package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val credits: Int = 100,
    val userLevel: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)