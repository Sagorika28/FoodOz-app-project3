package com.sagorika.foodoz.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.sagorika.foodoz.R
import com.sagorika.foodoz.adapter.CartItemAdapter
import com.sagorika.foodoz.adapter.RestaurantMenuAdapter
import com.sagorika.foodoz.database.OrderEntity
import com.sagorika.foodoz.database.RestDatabase
import com.sagorika.foodoz.fragment.RestaurantFragment
import com.sagorika.foodoz.model.FoodItem
import com.sagorika.foodoz.util.SessionManager
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerCart: RecyclerView
    private lateinit var cartItemAdapter: CartItemAdapter
    private var orderList = ArrayList<FoodItem>()
    private lateinit var txtResName: TextView
    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlCart: RelativeLayout
    private lateinit var btnPlaceOrder: Button
    private var resId: Int = 0
    private var resName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        init()
        setupToolbar()
        setUpCartList()
        placeOrder()

    }

    private fun init() {
        sessionManager = SessionManager(this)
        rlLoading = findViewById(R.id.rlLoading)
        rlCart = findViewById(R.id.rlCart)
        txtResName = findViewById(R.id.txtCartResName)
        txtResName.text = RestaurantFragment.resName
        val bundle = intent.getBundleExtra("data")
        resId = bundle?.getInt("resId", 0) as Int
        resName = bundle.getString("resName", "") as String
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpCartList() {
        recyclerCart = findViewById(R.id.recyclerCartItems)
        val dbList = GetItemsFromDBAsync(applicationContext).execute().get()

        /*Extracting the data saved in database and then using Gson to convert the String of food items into a list
         of food items*/
        for (element in dbList) {
            orderList.addAll(
                Gson().fromJson(element.foodItems, Array<FoodItem>::class.java).asList()
            )
        }

        //If the order list extracted from DB is empty we do not display the cart
        if (orderList.isEmpty()) {
            rlCart.visibility = View.GONE
            rlLoading.visibility = View.VISIBLE
        } else {
            rlCart.visibility = View.VISIBLE
            rlLoading.visibility = View.GONE
        }

        //Else we display the cart using the cart item adapter
        cartItemAdapter = CartItemAdapter(orderList, this@CartActivity)
        val mLayoutManager = LinearLayoutManager(this@CartActivity)
        recyclerCart.layoutManager = mLayoutManager
        recyclerCart.itemAnimator = DefaultItemAnimator()
        recyclerCart.adapter = cartItemAdapter
    }


    private fun placeOrder() {
        btnPlaceOrder = findViewById(R.id.btnConfirmOrder)

        //Displaying the total cost of the items ordered on the place order button
        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost
        }
        val total = "Place Order (Total: ₹$sum)"
        btnPlaceOrder.text = total

        btnPlaceOrder.setOnClickListener {
            rlLoading.visibility = View.VISIBLE
            rlCart.visibility = View.INVISIBLE
            sendServerRequest()
        }
    }

    private fun sendServerRequest() {
        val queue = Volley.newRequestQueue(this)
        val PLACE_ORDER = "http://13.235.250.119/v2/place_order/fetch_result/"

        val jsonParams = JSONObject()

        jsonParams.put(
            "user_id",
            this@CartActivity.getSharedPreferences(
                sessionManager.PREF_NAME,
                sessionManager.PRIVATE_MODE
            )
                .getString(
                    "user_id",
                    null
                ) as String
        )

        jsonParams.put("restaurant_id", RestaurantFragment.resId?.toString() as String)

        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost
        }

        jsonParams.put("total_cost", sum.toString())

        val foodArray = JSONArray()
        for (i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].id)
            foodArray.put(i, foodId)
        }
        jsonParams.put("food", foodArray)

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, PLACE_ORDER, jsonParams, Response.Listener {

                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    /*If order is placed, clear the DB for the recently added items
                     Once the DB is cleared, notify the user that the order has been placed*/

                    if (success) {
                        val clearCart =
                            ClearDBAsync(applicationContext, resId.toString()).execute().get()
                        RestaurantMenuAdapter.isCartEmpty = true

                        //Using the Dialog class to display the order placed message
                        val dialog = Dialog(
                            this@CartActivity,
                            android.R.style.Theme_Black_NoTitleBar_Fullscreen
                        )

                        dialog.setContentView(R.layout.order_placed_dialog)
                        dialog.show()
                        dialog.setCancelable(false)
                        val btnOk = dialog.findViewById<Button>(R.id.btnOk)

                        btnOk.setOnClickListener {
                            dialog.dismiss()
                            val clearCart =
                                CartActivity.ClearDBAsync(
                                    applicationContext,
                                    RestaurantFragment.resId.toString()
                                ).execute().get()
                            RestaurantMenuAdapter.isCartEmpty = true

                            startActivity(Intent(this@CartActivity, MainActivity::class.java))
                            ActivityCompat.finishAffinity(this@CartActivity)
                        }
                    } else {
                        rlCart.visibility = View.VISIBLE
                        Toast.makeText(this@CartActivity, "Some Error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }

                } catch (e: Exception) {
                    rlCart.visibility = View.VISIBLE
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                rlCart.visibility = View.VISIBLE
                Toast.makeText(this@CartActivity, it.message, Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "abb428ebfb485a"
                    return headers
                }
            }

        queue.add(jsonObjectRequest)

    }


    //Asynctask class for extracting the items from the database
    class GetItemsFromDBAsync(context: Context) : AsyncTask<Void, Void, List<OrderEntity>>() {
        val db = Room.databaseBuilder(context, RestDatabase::class.java, "rest-db").build()

        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            return db.orderDao().getAllOrders()
        }

    }

    //Asynctask class for clearing the recently added items from the database
    class ClearDBAsync(context: Context, val resId: String) : AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, RestDatabase::class.java, "rest-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteOrders(resId)
            db.close()
            return true
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val clearCart =
            CartActivity.ClearDBAsync(applicationContext, RestaurantFragment.resId.toString())
                .execute().get()
        RestaurantMenuAdapter.isCartEmpty = true

        onBackPressed()
        return true
    }
}