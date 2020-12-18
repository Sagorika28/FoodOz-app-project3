package com.sagorika.foodoz.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RestDao {
    @Insert
    fun insertRest(restEntity: RestEntity)

    @Delete
    fun deleteRest(restEntity: RestEntity)

    @Query("SELECT * FROM restaurants")
    fun getAllRests(): List<RestEntity>

    //to check whether a restaurant is present in fav or not
    @Query("SELECT * FROM restaurants WHERE rest_id = :restId")
    fun getRestById(restId: String): RestEntity
}