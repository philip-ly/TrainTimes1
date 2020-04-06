package com.anfantanion.traintimes1.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class NotifyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        NotifyManager.makeChangeNotification(context!!,intent)
    }

}