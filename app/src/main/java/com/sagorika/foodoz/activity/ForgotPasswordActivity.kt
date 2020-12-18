package com.sagorika.foodoz.activity

import android.app.AlertDialog
import android.content.Intent
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

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etEmail: EditText
    lateinit var btnNext: Button
    lateinit var progressBar: ProgressBar
    lateinit var rlContentMain: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmail = findViewById(R.id.etEmail)
        btnNext = findViewById(R.id.btnNext)
        progressBar = findViewById(R.id.progressBar)
        rlContentMain = findViewById(R.id.rlContentMain)

        rlContentMain.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        btnNext.setOnClickListener {

            val mobileNumber = etMobileNumber.text.toString()
            val email = etEmail.text.toString()

            if (Validations.validateMobile(mobileNumber)) {

                etMobileNumber.error = null

                if (Validations.validateEmail(email)) {

                    if (ConnectionManager().checkConnectivity(this@ForgotPasswordActivity)) {

                        rlContentMain.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE

                        sendOTP(mobileNumber, email)

                    } else {

                        rlContentMain.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE

                        val dialog = AlertDialog.Builder(this@ForgotPasswordActivity)
                        dialog.setTitle("Error")
                        dialog.setMessage("Internet Connection Not Found")
                        dialog.setPositiveButton("Open Settings") { text, listener ->
                            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            startActivity(settingsIntent)
                            finish()
                        }
                        dialog.setNegativeButton("Exit") { text, listener ->
                            ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
                        }
                        dialog.create()
                        dialog.show()

                    }
                } else {

                    rlContentMain.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    etEmail.setError("Invalid Email")
                    etEmail.requestFocus()

                }
            } else {

                rlContentMain.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                etMobileNumber.setError("Invalid Mobile Number")
                etMobileNumber.requestFocus()

            }
        }
    }

    fun sendOTP(mobileNumber: String, email: String) {

        val queue = Volley.newRequestQueue(this)
        val FORGOT_PASSWORD = "http://13.235.250.119/v2/forgot_password/fetch_result"

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("email", email)

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, FORGOT_PASSWORD, jsonParams, Response.Listener {

                try {

                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if (success) {

                        val firstTry = data.getBoolean("first_try")

                        if (firstTry) {

                            val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                            builder.setTitle("Information")
                            builder.setMessage("Please check your registered email for the OTP")
                            builder.setCancelable(false)
                            builder.setPositiveButton("OK") { text, listener ->
                                val intent = Intent(
                                    this@ForgotPasswordActivity,
                                    ResetPasswordActivity::class.java
                                )
                                intent.putExtra("user_mobile", mobileNumber)
                                startActivity(intent)
                            }
                            builder.create().show()

                        } else {

                            val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                            builder.setTitle("Information")
                            builder.setMessage("Please refer to the previous email for the OTP")
                            builder.setCancelable(false)
                            builder.setPositiveButton("OK") { text, listener ->
                                val intent = Intent(
                                    this@ForgotPasswordActivity,
                                    ResetPasswordActivity::class.java
                                )
                                intent.putExtra("user_mobile", mobileNumber)
                                startActivity(intent)
                            }
                            builder.create().show()

                        }

                    } else {

                        rlContentMain.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE

                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Mobile Number not registered!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                } catch (e: Exception) {

                    rlContentMain.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Incorrect response error!",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }, Response.ErrorListener {

                rlContentMain.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                VolleyLog.e("Error::::", "/post request fail! Error: ${it.message}")
                Toast.makeText(this@ForgotPasswordActivity, it.message, Toast.LENGTH_SHORT).show()

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
