package com.sagorika.foodoz.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.sagorika.foodoz.R
import com.sagorika.foodoz.util.ConnectionManager
import com.sagorika.foodoz.util.SessionManager
import com.sagorika.foodoz.util.Validations
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    lateinit var sessionManager: SessionManager
    lateinit var sharedPreferences: SharedPreferences
    lateinit var etNameR: EditText
    lateinit var etMobileNumber: EditText
    lateinit var etAddress: EditText
    lateinit var etEmail: EditText
    lateinit var etPassword: EditText
    lateinit var toolbar: Toolbar
    lateinit var etConfirmedPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(this)
        sharedPreferences =
            this.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)
        etNameR = findViewById(R.id.etNameR)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfirmedPassword = findViewById(R.id.etConfirmedPassword)

        btnRegister.setOnClickListener {
            val name = etNameR.text.toString().trim()
            val phone = etMobileNumber.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val addr = etAddress.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val cpass = etConfirmedPassword.text.toString().trim()

            when {
                name.isEmpty() -> {
                    etNameR.setError("Name can not be empty")
                    etNameR.requestFocus()
                }

                !Validations.validateNameLength(name) -> {
                    etNameR.setError("Minimum 3 characters required")
                    etNameR.requestFocus()
                }

                phone.isEmpty() -> {
                    etMobileNumber.setError("Mobile Number can not be empty")
                    etMobileNumber.requestFocus()
                }

                !Validations.validateMobile(phone) -> {
                    etMobileNumber.setError("Invalid mobile number")
                    etMobileNumber.requestFocus()
                }

                !Validations.validateEmail(email) -> {
                    etEmail.setError("Invalid Email")
                    etEmail.requestFocus()
                }

                addr.isEmpty() -> {
                    etAddress.setError("Address can not be empty")
                    etAddress.requestFocus()
                }

                pass.isEmpty() -> {
                    etPassword.setError("Password can not be empty")
                    etPassword.requestFocus()
                }

                !Validations.validatePasswordLength(pass) -> {
                    etPassword.setError("Minimum 4 characters required")
                    etPassword.requestFocus()
                }

                !Validations.matchPassword(pass, cpass) -> {
                    etConfirmedPassword.setError("Passwords don't match")
                    etConfirmedPassword.requestFocus()
                }

                else -> {

                    if (ConnectionManager().checkConnectivity(this@RegisterActivity)) {

                        //send registration request
                        sendRegisterRequest(name, phone, addr, pass, email)

                    } else {

                        val dialog = AlertDialog.Builder(this@RegisterActivity)
                        dialog.setTitle("Error")
                        dialog.setMessage("Internet Connection Not Found")
                        dialog.setPositiveButton("Open Settings") { text, listener ->
                            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            startActivity(settingsIntent)
                            finish()
                        }
                        dialog.setNegativeButton("Exit") { text, listener ->
                            ActivityCompat.finishAffinity(this@RegisterActivity)
                        }
                        dialog.create()
                        dialog.show()

                    }
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        finish()
    }


    fun sendRegisterRequest(
        name: String,
        phone: String,
        address: String,
        password: String,
        email: String
    ) {

        val queue = Volley.newRequestQueue(this)
        val REGISTER = "http://13.235.250.119/v2/register/fetch_result"

        val jsonParams = JSONObject()
        jsonParams.put("name", name)
        jsonParams.put("mobile_number", phone)
        jsonParams.put("password", password)
        jsonParams.put("address", address)
        jsonParams.put("email", email)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            REGISTER,
            jsonParams,
            Response.Listener {
                // try block for JSON exception
                try {

                    val data = it.getJSONObject("data")

                    val success = data.getBoolean("success")

                    if (success) {
                        val response = data.getJSONObject("data")
                        sharedPreferences.edit().putString("user_id", response.getString("user_id"))
                            .apply()
                        sharedPreferences.edit().putString("nameR", response.getString("name"))
                            .apply()
                        sharedPreferences.edit()
                            .putString("numberR", response.getString("mobile_number")).apply()
                        sharedPreferences.edit().putString("addrR", response.getString("address"))
                            .apply()
                        sharedPreferences.edit().putString("emailR", response.getString("email"))
                            .apply()

                        sessionManager.setLogin(true)

                        Toast.makeText(
                            this@RegisterActivity,
                            "You've been registered successfully !",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()

                    } else {

                        val errorMessage = data.getString("errorMessage")

                        Toast.makeText(
                            this@RegisterActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "$e error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            }, Response.ErrorListener {

                Toast.makeText(
                    this@RegisterActivity,
                    "Some volley error occurred..",
                    Toast.LENGTH_SHORT
                )
                    .show()

            }
        ) {

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "abb428ebfb485a"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }
}
