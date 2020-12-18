package com.sagorika.foodoz.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.sagorika.foodoz.R
import com.sagorika.foodoz.database.RestDatabase
import com.sagorika.foodoz.database.RestEntity
import com.sagorika.foodoz.fragment.RestaurantFragment
import com.sagorika.foodoz.model.Restaurant
import com.squareup.picasso.Picasso

class DashboardRecyclerAdapter(val context: Context, val itemList: ArrayList<Restaurant>) :
    RecyclerView.Adapter<DashboardRecyclerAdapter.DashboardViewHolder>() {

    class DashboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
        val imgRestImage: ImageView = view.findViewById(R.id.imgRestImage)
        val txtRestName: TextView = view.findViewById(R.id.txtRestName)
        val txtRestPrice: TextView = view.findViewById(R.id.txtRestPrice)
        val txtRestRating: TextView = view.findViewById(R.id.txtRestRating)
        val imgFav: ImageView = view.findViewById(R.id.imgFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_dashboard_single_row, parent, false)

        return DashboardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val restaurant = itemList[position]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.imgRestImage.clipToOutline = true
        }
        holder.txtRestName.text = restaurant.restName
        holder.txtRestPrice.text = "₹" + restaurant.restPrice + "/person"
        holder.txtRestRating.text = restaurant.restRating
        Picasso.get().load(restaurant.restImage).error(R.drawable.default_food_image)
            .into(holder.imgRestImage)


        //setting the colour of the favourite button to indicate whether restaurant is fav or not
        val listOfFavourites = GetAllFavAsyncTask(context).execute().get()

        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(restaurant.restId)) {
            holder.imgFav.setImageResource(R.drawable.ic_fav)
        } else {
            holder.imgFav.setImageResource(R.drawable.ic_notfav)
        }

        //toggling action to set fav or not
        holder.imgFav.setOnClickListener {
            val restEntity = RestEntity(
                restaurant.restId.toInt(),
                restaurant.restName,
                restaurant.restRating,
                restaurant.restPrice,
                restaurant.restImage
            )

            if (!DBAsyncTask(context, restEntity, 1).execute().get()) {
                val async = DBAsyncTask(context, restEntity, 2).execute()
                val result = async.get()
                if (result) {
                    holder.imgFav.setImageResource(R.drawable.ic_fav)
                } else {
                    Toast.makeText(context, "Some error occurred!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val async = DBAsyncTask(context, restEntity, 3).execute()
                val result = async.get()
                if (result) {
                    holder.imgFav.setImageResource(R.drawable.ic_notfav)
                } else {
                    Toast.makeText(context, "Some error occurred!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        holder.llContent.setOnClickListener {

            val fragment = RestaurantFragment()
            val args = Bundle()
            args.putInt("id", restaurant.restId.toInt())
            args.putString("name", restaurant.restName)
            fragment.arguments = args
            val transaction =
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, fragment)
            transaction.commit()
            (context as AppCompatActivity).supportActionBar?.title =
                holder.txtRestName.text.toString()

        }
    }

    //AsynTask class declaration
    class DBAsyncTask(context: Context, val restEntity: RestEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        /*
        Mode 1 -> Check DB if the restaurant is favourite or not
        Mode 2 -> Save the restaurant into DB as favourite
        Mode 3 -> Remove the favourite restaurant
         */

        //DB global initialization for DBAsyncTask class
        val db = Room.databaseBuilder(context, RestDatabase::class.java, "rest-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {

                1 -> {
                    //Check DB if the restaurant is favourite or not
                    val rest: RestEntity? = db.restDao().getRestById(restEntity.rest_id.toString())
                    db.close()
                    return rest != null
                }

                2 -> {
                    //Save the restaurant into DB as favourite (insert restaurant)
                    db.restDao().insertRest(restEntity)
                    db.close()
                    return true
                }

                3 -> {
                    //Remove the favourite restaurant (delete restaurant)
                    db.restDao().deleteRest(restEntity)
                    db.close()
                    return true
                }

            }

            return false
        }

    }

    class GetAllFavAsyncTask(context: Context) : AsyncTask<Void, Void, List<String>>() {

        val db = Room.databaseBuilder(context, RestDatabase::class.java, "rest-db").build()

        override fun doInBackground(vararg params: Void?): List<String> {
            val list = db.restDao().getAllRests()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.rest_id.toString())
            }
            return listOfIds
        }

    }
}