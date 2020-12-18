package com.sagorika.foodoz.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RestEntity::class, OrderEntity::class], version = 1)
abstract class RestDatabase : RoomDatabase() {
    //to tell that functions we'll perform on the data will be performed by the DAO interface
    abstract fun restDao(): RestDao
    abstract fun orderDao(): OrderDao
}