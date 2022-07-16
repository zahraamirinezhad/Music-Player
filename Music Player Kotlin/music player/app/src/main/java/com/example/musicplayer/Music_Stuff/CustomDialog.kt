package com.example.musicplayer.Music_Stuff

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.musicplayer.Music_Stuff.Constants.Companion.OK
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CustomDialog {
    companion object{
        fun getDialogForOnLongClickListener(context: Context, customDialog: View): AlertDialog {
            val dialog = MaterialAlertDialogBuilder(context).setView(customDialog)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
            return dialog
        }

        fun getInfoDialog(context: Context, detailsDialog: View) {
            val dDialog = MaterialAlertDialogBuilder(context)
                .setBackground(ColorDrawable(0x99000000.toInt()))
                .setView(detailsDialog)
                .setPositiveButton(OK) { self, _ -> self.dismiss() }
                .setCancelable(false)
                .create()
            dDialog.show()
            dDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
            dDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
        }
    }
}