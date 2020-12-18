package com.sagorika.foodoz.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sagorika.foodoz.R
import com.sagorika.foodoz.util.SessionManager

class ProfileFragment : Fragment() {

    //values sent from registration activity to be stored in these variables
    var nameSent: String? = "No message given"
    var mobSent: String? = "No message given"
    var emailSent: String? = "No message given"
    var addrSent: String? = "No message given"

    lateinit var txtNameA: TextView
    lateinit var txtEmailA: TextView
    lateinit var txtPhoneA: TextView
    lateinit var txtAddressA: TextView
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        txtNameA = view.findViewById(R.id.txtNameA)
        txtEmailA = view.findViewById(R.id.txtEmailA)
        txtPhoneA = view.findViewById(R.id.txtPhoneA)
        txtAddressA = view.findViewById(R.id.txtAddressA)

        sessionManager = SessionManager(activity as Context)

        var sharedPreferences =
            activity?.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)

        nameSent = sharedPreferences?.getString("nameR", "xyz")
        emailSent = sharedPreferences?.getString("emailR", "xyz@gmail.com")
        mobSent = sharedPreferences?.getString("numberR", "0000000000")
        addrSent = sharedPreferences?.getString("addrR", "abd town")

        txtNameA.text = nameSent
        txtEmailA.text = emailSent
        txtPhoneA.text = mobSent
        txtAddressA.text = addrSent

        return view
    }
}