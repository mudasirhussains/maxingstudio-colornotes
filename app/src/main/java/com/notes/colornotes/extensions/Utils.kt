package com.notes.colornotes.extensions

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.provider.Settings
import android.widget.TextView
import java.util.*
import java.util.regex.Pattern

class Utils {

    companion object {
        const val SHOP_BUNDLE = "SHOP_BUNDLE"
        const val CITY_BUNDLE = "CITY_BUNDLE"
    }

     fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
         return true
    }

    fun setupConnectivity(context: Context, dismiss : Boolean) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Permission to access the internet is required for this app.")
            .setTitle("Internet required")

        builder.setPositiveButton(
            "Go to Settings"
        ) { dialog, id ->
            dialog.dismiss()
            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }

        builder.setNegativeButton(
            "Retry"
        ) { dialog, id ->
            if (isNetworkAvailable(context)){
                dialog.dismiss()
            }

        }

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }


    fun isValidEmail(email: String): Boolean {
        return Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }

    fun emptyCheck(pass: String):Boolean{
        return pass.isNotEmpty() && pass.length >= 5
    }

    fun toCamelCase(s: String): String {
        if (s.length == 0) {
            return s
        }
        val parts = s.split(" ").toTypedArray()
        var camelCaseString = ""
        for (part in parts) {
            camelCaseString = camelCaseString + toProperCase(part) + " "
        }
        return camelCaseString
    }

    //M
    fun toProperCase(s: String): String {
        return s.substring(0, 1).uppercase(Locale.getDefault()) +
                s.substring(1).lowercase(Locale.getDefault())
    }


    //double animate
    fun animateDoubleTextView(initialValue: Float, finalValue: Float, textview: TextView) {
        val valueAnimator = ValueAnimator.ofFloat(initialValue.toFloat(), finalValue)
        valueAnimator.duration = 3000
        valueAnimator.addUpdateListener { valueAnimator ->
            textview.text = "£" + String.format("%.2f", valueAnimator.animatedValue)
        }
        valueAnimator.start()
    }


}