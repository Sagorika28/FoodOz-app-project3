package com.sagorika.foodoz.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.sagorika.foodoz.R
import com.sagorika.foodoz.util.ConnectionManager
import com.sagorika.foodoz.util.Validations
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var etOTP: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmNewPassword: EditText
    lateinit var btnSubmitOTP: Button
    lateinit var rlOTP: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var mobileNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etOTP = findViewById(R.id.etOTP)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnSubmitOTP = findViewById(R.id.btnSubmitOTP)
        rlOTP = findViewById(R.id.rlOTP)
        progressBar = findViewById(R.id.progressBar)

        rlOTP.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        if (intent != null) {
            mobileNumber = intent.getStringExtra("user_mobile") as String
        }

        btnSubmitOTP.setOnClickListener {

            rlOTP.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            if (ConnectionManager().checkConnectivity(this@ResetPasswordActivity)) {

                if (etOTP.text.length == 4) {

                    if (Validations.validatePasswordLength(etNewPassword.text.toString())) {

                        if (Validations.matchPassword(
                                etNewPassword.text.toString(),
                                etConfirmNewPassword.text.toString()
                            )
                        ) {

                            resetPassword(
                                mobileNumber,
                                etOTP.text.toString(),
                                etNewPassword.text.toString()
                            )

                        } else {

                            rlOTP.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE

                            Toast.makeText(
                                this@ResetPasswordActivity,
                                "Passwords do not match!",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    } else {

                        rlOTP.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE

                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Invalid Password!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                } else {

                    rlOTP.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    Toast.makeText(this@ResetPasswordActivity, "Incorrect OTP!", Toast.LENGTH_SHORT)
                        .show()

                }

            } else {

                rlOTP.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                val dialog = AlertDialog.Builder(this@ResetPasswordActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection Not Found")
                dialog.setPositiveButton("Open Settings") { text, listener ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit") { text, listener ->
                    ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                }
                dialog.create()
                dialog.show()
            }
        }
    }

    fun resetPassword(mobileNumber: String, otp: String, password: String) {

        val queue = Volley.newRequestQueue(this)
        val RESET_PASSWORD = "http://13.235.250.119/v2/reset_password/fetch_result"

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("password", password)
        jsonParams.put("otp", otp)

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, RESET_PASSWORD, jsonParams, Response.Listener {

                try {

                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if (success) {

                        progressBar.visibility = View.INVISIBLE

                        val builder = AlertDialog.Builder(this@ResetPasswordActivity)
                        builder.setTitle("Confirmation")
                        builder.setMessage("Your password has been successfully changed")
                        builder.setIcon(R.drawable.ic_action_success)
                        builder.setCancelable(false)
                        builder.setPositiveButton("OK") { text, listener ->
                            startActivity(
                                Intent(
                                    this@ResetPasswordActivity,
                                    LoginActivity::class.java
                                )
                            )
                            ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                        }
                        builder.create().show()

                    } else {

                        rlOTP.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE

                        val error = data.getString("errorMessage")

                        Toast.makeText(this@ResetPasswordActivity, error, Toast.LENGTH_SHORT).show()

                    }

                } catch (e: Exception) {

                    rlOTP.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Incorrect response!!",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }, Response.ErrorListener {

                rlOTP.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                VolleyLog.e("Error::::", "/post request fail! Error: ${it.message}")

                Toast.makeText(this@ResetPasswordActivity, it.message, Toast.LENGTH_SHORT).show()

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

}
