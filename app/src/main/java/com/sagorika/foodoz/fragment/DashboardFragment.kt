package com.sagorika.foodoz.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.sagorika.foodoz.R
import com.sagorika.foodoz.adapter.DashboardRecyclerAdapter
import com.sagorika.foodoz.database.RestDatabase
import com.sagorika.foodoz.database.RestEntity
import com.sagorika.foodoz.model.Restaurant
import com.sagorika.foodoz.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class DashboardFragment : Fragment() {

    lateinit var recyclerDashboard: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager

    lateinit var recyclerAdaper: DashboardRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    var previousMenuItem: MenuItem? = null

    val restInfoList = arrayListOf<Restaurant>()

    //variable for comparing restaurants to sort acc. to their rating
    var ratingComparator = Comparator<Restaurant> { rest1, rest2 ->
        if (rest1.restRating.compareTo(rest2.restRating, true) == 0) {
            rest1.restName.compareTo(rest2.restName, true)
        } else {
            rest1.restRating.compareTo(rest2.restRating, true)
        }
    }

    //variable for comparing restaurants to sort acc. to their cost
    var costComparator = Comparator<Restaurant> { rest1, rest2 ->
        if (rest1.restPrice.compareTo(rest2.restPrice, true) == 0) {
            rest1.restName.compareTo(rest2.restName, true)
        } else {
            rest1.restPrice.compareTo(rest2.restPrice, true)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)

        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)
        layoutManager = LinearLayoutManager(activity)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)

        //to show the progress bar when fragment is being loaded
        progressLayout.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        if (ConnectionManager().checkConnectivity(activity as Context)) {

            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener {
                    // try block for JSON exception
                    try {

                        //to hide the progress layout when data has been loaded
                        progressLayout.visibility = View.GONE

                        val data = it.getJSONObject("data")

                        val success = data.getBoolean("success")

                        if (success) {
                            val restArray = data.getJSONArray("data")

                            //extracting JSONObjects from JSONArray
                            for (i in 0 until restArray.length()) {
                                val restJsonObject = restArray.getJSONObject(i)
                                val restObject = Restaurant(
                                    restJsonObject.getString("id"),
                                    restJsonObject.getString("name"),
                                    restJsonObject.getString("rating"),
                                    restJsonObject.getString("cost_for_one"),
                                    restJsonObject.getString("image_url")
                                )
                                restInfoList.add(restObject)

                                ////sending the bookInfoList to adapter
                                recyclerAdaper =
                                    DashboardRecyclerAdapter(activity as Context, restInfoList)

                                //initialise adapter and layoutManager and attach them to their resp. files
                                recyclerDashboard.adapter = recyclerAdaper
                                recyclerDashboard.layoutManager = layoutManager

                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some Error Occurred",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected json error occurred :( ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                },
                Response.ErrorListener {
                    //to make sure the app doesn't crash when we try to open fav without waiting for dashboard to load
                    if (activity != null) {
                        Toast.makeText(
                            activity as Context,
                            "Volley error occurred ! ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "abb428ebfb485a"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

        } else {

            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection Not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //store id of the clicked item
        var id = item?.itemId

        //highlighting the selected menu item
        if (previousMenuItem != null)
            previousMenuItem?.isChecked = false //unchecked the previous item

        //check the current item
        item.isCheckable = true
        item.isChecked = true
        //make current item previous
        previousMenuItem = item

        //check which item id is clicked by comparing the item id clicked to the item id of menu options
        when (id) {

            R.id.action_sort_rating -> {

                //sort the restaurants according to rating (increasing order)
                Collections.sort(restInfoList, ratingComparator)

                //rearranging the lists in the descending order
                restInfoList.reverse()

            }

            R.id.action_sort_LH -> {

                //sort the restaurants according to cost (low to high)
                Collections.sort(restInfoList, costComparator)

            }

            R.id.action_sort_HL -> {

                //sort the restaurants according to cost (high to low)
                Collections.sort(restInfoList, costComparator)

                //rearranging the lists in the descending order
                restInfoList.reverse()

            }
        }

        //notify the adapter about the changes made to reflect them on the screen
        recyclerAdaper.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)
    }
}