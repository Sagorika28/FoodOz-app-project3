package com.sagorika.foodoz.util

object Validations {
    fun validateMobile(mobile: String): Boolean {
        return mobile.length == 10
    }

    fun validatePasswordLength(password: String): Boolean {
        return password.length >= 4
    }

    fun validateNameLength(name: String): Boolean {
        return name.length >= 3
    }

    fun matchPassword(pass: String, confirmPass: String): Boolean {
        return pass == confirmPass
    }

    fun validateEmail(email: String): Boolean {
        var emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
        return (!email.isEmpty() && email.matches(emailPattern))
    }
}
