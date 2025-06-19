package com.example.recipely.utils

import android.app.Activity
import android.app.AlertDialog
import com.example.recipely.R

class LoadingUtils(private val activity: Activity) {
    private lateinit var alertDialog: AlertDialog

    fun show() {
        val builder = AlertDialog.Builder(activity)

        val dialogView = activity.layoutInflater.inflate(R.layout.loading, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog.show()
    }

    fun dismiss() {
        alertDialog.dismiss()
    }
}