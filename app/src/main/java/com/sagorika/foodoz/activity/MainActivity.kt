package com.sagorika.foodoz.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.sagorika.foodoz.R
import com.sagorika.foodoz.adapter.RestaurantMenuAdapter
import com.sagorika.foodoz.fragment.*
import com.sagorika.foodoz.fragment.RestaurantFragment.Companion.resId
import com.sagorika.foodoz.util.SessionManager

class MainActivity : AppCompatActivity() {

    lateinit var sessionManager: SessionManager

    //declaring the navigation drawer variables
    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView

    lateinit var txtDrawerName: TextView
    lateinit var txtDrawerMob: TextView
    lateinit var imgDrawerImage: ImageView

    var nameSent: String? = "No msg"
    var mobSent: String? = "No msg"

    lateinit var sharedPreferences: SharedPreferences

    var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        var sharedPreferences =
            getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)

        nameSent = sharedPreferences.getString("nameR", "xyz")
        mobSent = sharedPreferences.getString("numberR", "000000000")

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frame)
        navigationView = findViewById(R.id.navigationView)

        //changing the drawer header details dynamically
        val convertView =
            LayoutInflater.from(this@MainActivity).inflate(R.layout.drawer_header, null)

        txtDrawerName = convertView.findViewById(R.id.txtDrawerName)
        txtDrawerMob = convertView.findViewById(R.id.txtDrawerMob)
        imgDrawerImage = convertView.findViewById(R.id.imgDrawerImage)

        txtDrawerName.text = nameSent
        txtDrawerMob.text = "+91-" + mobSent
        navigationView.addHeaderView(convertView)

        setUpToolbar()
        openDashboard()

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        //to make the nav items listen to events when clicked
        navigationView.setNavigationItemSelectedListener {

            //highlighting the selected menu item
            if (previousMenuItem != null)
                previousMenuItem?.isChecked = false //unchecked the previous item

            //check the current item
            it.isCheckable = true
            it.isChecked = true
            //make current item previous
            previousMenuItem = it

            when (it.itemId) {
                R.id.dashboard -> {

                    openDashboard()

                    //to close drawer
                    drawerLayout.closeDrawers()

                }

                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            ProfileFragment()
                        )
                        .commit()

                    //giving title to individual fragments
                    supportActionBar?.title = "My Profile"

                    drawerLayout.closeDrawers()
                }

                R.id.favourites -> {

                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FavouritesFragment()
                        )
                        .commit()

                    supportActionBar?.title = "Favourite Restaurants"

                    drawerLayout.closeDrawers()

                }

                R.id.orders -> {

                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            OrderHistoryFragment()
                        )
                        .commit()

                    supportActionBar?.title = "Order History"

                    drawerLayout.closeDrawers()

                }

                R.id.faq -> {

                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FaqFragment()
                        )
                        .commit()

                    supportActionBar?.title = "Frequently Asked Questions"

                    drawerLayout.closeDrawers()

                }

                R.id.logout -> {
                    //logs out

                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Confirmation")
                    dialog.setMessage("Are you sure you want to log out?")
                    dialog.setPositiveButton("YES") { text, listener ->
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        sharedPreferences.edit().clear().apply()
                        finish()
                    }
                    dialog.setNegativeButton("NO") { text, listener ->
                        openDashboard()
                    }
                    dialog.create()
                    dialog.show()
                }
            }
            return@setNavigationItemSelectedListener true
        }

    }


    fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Toolbar Title"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    //to open the nav drawer
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home)
            drawerLayout.openDrawer(GravityCompat.START)

        return super.onOptionsItemSelected(item)
    }

    //to open dashboard fragment
    fun openDashboard() {
        val fragment = DashboardFragment()
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.frame, fragment)
        transaction.commit()

        supportActionBar?.title = "All Restaurants"

        //check dashboard item on opening
        navigationView.setCheckedItem(R.id.dashboard)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frame)

        when (frag) {
            is DashboardFragment -> {
                Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
                super.onBackPressed()
            }

            is RestaurantFragment -> {

                if (!RestaurantMenuAdapter.isCartEmpty) {

                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Confirmation")
                        .setMessage("Going back will reset the cart items. Do you still want to proceed?")
                        .setPositiveButton("Yes") { _, _ ->
                            val clearCart =
                                CartActivity.ClearDBAsync(applicationContext, resId.toString())
                                    .execute().get()
                            openDashboard()
                            RestaurantMenuAdapter.isCartEmpty = true
                        }
                        .setNegativeButton("No") { _, _ ->

                        }
                        .create()
                        .show()

                } else {
                    openDashboard()
                }
            }
            else -> openDashboard()
        }
    }
}
