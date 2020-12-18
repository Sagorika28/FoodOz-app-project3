package com.sagorika.foodoz.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestEntity(
    @PrimaryKey val rest_id: Int,
    @ColumnInfo(name = "rest_name") val restName: String,
    @ColumnInfo(name = "rest_rating") val restRating: String,
    @ColumnInfo(name = "rest_price") val restPrice: String,
    @ColumnInfo(name = "rest_image") val restImage: String
)