package com.sagorika.foodoz.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.sagorika.foodoz.R
import com.sagorika.foodoz.util.ConnectionManager
import com.sagorika.foodoz.util.SessionManager
import com.sagorika.foodoz.util.Validations
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    //to manage the login session
    lateinit var sessionManager: SessionManager

    //shared preference object created as a class member
    lateinit var sharedPreferences: SharedPreferences

    //view objects declared
    lateinit var etMobileNumber: EditText
    lateinit var etAPassword: EditText
    lateinit var btnLogin: Button
    lateinit var txtForgotPassword: TextView
    lateinit var txtRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //initialising session variable
        sessionManager = SessionManager(this)
        sharedPreferences =
            this.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)

        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //view objects initialised
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etAPassword = findViewById(R.id.etAPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtRegister = findViewById(R.id.txtRegister)

        //jumps to ForgotPassword Activity on click
        txtForgotPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }

        //jumps to Register Activity on click
        txtRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {

            //hiding login button when process is going on
            btnLogin.visibility = View.INVISIBLE

            //validate the mobile number and password length
            if (Validations.validateMobile(etMobileNumber.text.toString()) && Validations.validatePasswordLength(
                    etAPassword.text.toString()
                )
            ) {
                if (ConnectionManager().checkConnectivity(this@LoginActivity)) {

                    val queue = Volley.newRequestQueue(this@LoginActivity)
                    val LOGIN = "http://13.235.250.119/v2/login/fetch_result/"

                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", etMobileNumber.text.toString())
                    jsonParams.put("password", etAPassword.text.toString())

                    //sending the json object request
                    val jsonObjectRequest = object :
                        JsonObjectRequest(Method.POST, LOGIN, jsonParams, Response.Listener {

                            try {

                                val data = it.getJSONObject("data")

                                val success = data.getBoolean("success")

                                if (success) {
                                    val response = data.getJSONObject("data")
                                    sharedPreferences.edit()
                                        .putString("user_id", response.getString("user_id"))
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString("nameR", response.getString("name"))
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString(
                                            "numberR",
                                            response.getString("mobile_number")
                                        )
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString("addrR", response.getString("address"))
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString("emailR", response.getString("email"))
                                        .apply()

                                    sessionManager.setLogin(true)

                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()

                                } else {

                                    btnLogin.visibility = View.VISIBLE
                                    txtForgotPassword.visibility = View.VISIBLE
                                    txtRegister.visibility = View.VISIBLE

                                    val errorMessage = data.getString("errorMessage")

                                    Toast.makeText(
                                        this@LoginActivity,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            } catch (e: JSONException) {

                                btnLogin.visibility = View.VISIBLE
                                txtForgotPassword.visibility = View.VISIBLE
                                txtRegister.visibility = View.VISIBLE
                                Toast.makeText(
                                    this@LoginActivity,
                                    "$e error occurred",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()

                            }
                        },
                            Response.ErrorListener {

                                btnLogin.visibility = View.VISIBLE
                                txtForgotPassword.visibility = View.VISIBLE
                                txtRegister.visibility = View.VISIBLE

                                Toast.makeText(
                                    this@LoginActivity,
                                    "Some volley error occurred..",
                                    Toast.LENGTH_SHORT
                                ).show()

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

                    btnLogin.visibility = View.VISIBLE
                    txtForgotPassword.visibility = View.VISIBLE
                    txtRegister.visibility = View.VISIBLE

                    val dialog = AlertDialog.Builder(this@LoginActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection Not Found")
                    dialog.setPositiveButton("Open Settings") { text, listener ->
                        val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        finish()
                    }
                    dialog.setNegativeButton("Exit") { text, listener ->
                        ActivityCompat.finishAffinity(this@LoginActivity)
                    }
                    dialog.create()
                    dialog.show()

                }
            } else {

                btnLogin.visibility = View.VISIBLE
                txtForgotPassword.visibility = View.VISIBLE
                txtRegister.visibility = View.VISIBLE

                Toast.makeText(
                    this@LoginActivity,
                    "Invalid phone number or password",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }
}
