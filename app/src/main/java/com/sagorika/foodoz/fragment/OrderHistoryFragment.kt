package com.sagorika.foodoz.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.sagorika.foodoz.R
import com.sagorika.foodoz.adapter.OrderHistoryAdapter
import com.sagorika.foodoz.model.OrderDetails
import com.sagorika.foodoz.util.SessionManager

class OrderHistoryFragment : Fragment() {

    lateinit var sessionManager: SessionManager
    lateinit var orderHistoryAdapter: OrderHistoryAdapter
    lateinit var recyclerOrderHistory: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var rlLoading: RelativeLayout
    lateinit var llHasOrders: LinearLayout
    lateinit var rlNoOrders: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        rlLoading = view.findViewById(R.id.rlLoading)
        llHasOrders = view.findViewById(R.id.llHasOrders)
        rlNoOrders = view.findViewById(R.id.rlNoOrders)
        recyclerOrderHistory = view.findViewById(R.id.recyclerOrderHistory)
        rlLoading.visibility = View.VISIBLE

        sessionManager = SessionManager(activity as Context)

        var sharedPreferences =
            activity?.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)

        val userId = sharedPreferences?.getString("user_id", "")

        val orderHistoryList = arrayListOf<OrderDetails>()

        val queue = Volley.newRequestQueue(activity as Context)
        val FETCH_PREVIOUS_ORDERS = "http://13.235.250.119/v2/orders/fetch_result/$userId"

        val jsonObjectRequest = object :
            JsonObjectRequest(Method.GET, FETCH_PREVIOUS_ORDERS, null, Response.Listener {
                rlLoading.visibility = View.GONE

                try {

                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if (success) {

                        val resArray = data.getJSONArray("data")

                        if (resArray.length() == 0) {

                            llHasOrders.visibility = View.GONE
                            rlNoOrders.visibility = View.VISIBLE

                        } else {

                            for (i in 0 until resArray.length()) {
                                val orderObject = resArray.getJSONObject(i)
                                val foodItems = orderObject.getJSONArray("food_items")
                                val orderDetails = OrderDetails(
                                    orderObject.getInt("order_id"),
                                    orderObject.getString("restaurant_name"),
                                    orderObject.getString("order_placed_at"),
                                    foodItems
                                )
                                orderHistoryList.add(orderDetails)

                                if (orderHistoryList.isEmpty()) {
                                    llHasOrders.visibility = View.GONE
                                    rlNoOrders.visibility = View.VISIBLE
                                } else {
                                    llHasOrders.visibility = View.VISIBLE
                                    rlNoOrders.visibility = View.GONE

                                    if (activity != null) {

                                        orderHistoryAdapter = OrderHistoryAdapter(
                                            activity as Context,
                                            orderHistoryList
                                        )
                                        val mLayoutManager =
                                            LinearLayoutManager(activity as Context)
                                        recyclerOrderHistory.layoutManager = mLayoutManager
                                        recyclerOrderHistory.itemAnimator = DefaultItemAnimator()
                                        recyclerOrderHistory.adapter = orderHistoryAdapter

                                    } else {
                                        queue.cancelAll(this::class.java.simpleName)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener {
                Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "abb428ebfb485a"
                return headers
            }
        }
        queue.add(jsonObjectRequest)

        return view
    }
}