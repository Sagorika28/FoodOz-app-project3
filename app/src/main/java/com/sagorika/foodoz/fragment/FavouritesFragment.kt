package com.sagorika.foodoz.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.sagorika.foodoz.R
import com.sagorika.foodoz.adapter.FavouriteRecyclerAdapter
import com.sagorika.foodoz.database.RestDatabase
import com.sagorika.foodoz.database.RestEntity
import com.sagorika.foodoz.model.Restaurant

class FavouritesFragment : Fragment() {

    lateinit var recyclerFavourite: RecyclerView
    lateinit var progressLayout: RelativeLayout
    lateinit var noFavRestLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: FavouriteRecyclerAdapter
    var restaurantList = arrayListOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        recyclerFavourite = view.findViewById(R.id.recyclerFavourite)
        progressLayout = view.findViewById(R.id.progressLayout)
        noFavRestLayout = view.findViewById(R.id.noFavRestLayout)
        progressBar = view.findViewById(R.id.progressBar)
        layoutManager = LinearLayoutManager(activity)
        //to show the progress bar when fragment is being loaded
        progressLayout.visibility = View.VISIBLE

        //retrieving the fav books from DB and storing it in dbBookList
        val dbRestList = RetrieveFavourites(activity as Context).execute().get()

        //checking whether dbBookList and hosting activity are not null then initialising adapter & displaying the recycler view
        if (dbRestList.isNotEmpty()) {
            progressLayout.visibility = View.GONE
            noFavRestLayout.visibility = View.GONE

            for (i in dbRestList) {
                restaurantList.add(
                    Restaurant(
                        i.rest_id.toString(),
                        i.restName,
                        i.restRating,
                        i.restPrice,
                        i.restImage
                    )
                )
            }
            recyclerAdapter = FavouriteRecyclerAdapter(activity as Context, restaurantList)
            recyclerFavourite.adapter = recyclerAdapter
            recyclerFavourite.layoutManager = layoutManager

        } else {
            noFavRestLayout.visibility = View.VISIBLE
            progressLayout.visibility = View.GONE
        }

        return view
    }

    //create AsyncTask class to retrieve list of fav books
    class RetrieveFavourites(val context: Context) : AsyncTask<Void, Void, List<RestEntity>>() {

        override fun doInBackground(vararg params: Void?): List<RestEntity> {

            val db = Room.databaseBuilder(context, RestDatabase::class.java, "rest-db").build()

            return db.restDao().getAllRests()
        }

    }
}