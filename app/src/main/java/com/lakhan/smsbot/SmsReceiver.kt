package com.lakhan.smsbot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val bundle: Bundle? = intent.extras
        if (bundle != null) {

            val pdus = bundle["pdus"] as Array<*>

            for (pdu in pdus) {

                val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                val message = sms.messageBody

                if (message.contains("credited") &&
                    message.contains("5098")) {

                    val amountRegex = Pattern.compile("Rs\\.(\\d+\\.\\d+)")
                    val utrRegex = Pattern.compile("UPI Ref No (\\d+)")

                    val amount = amountRegex.matcher(message)
                    val utr = utrRegex.matcher(message)

                    var amt = ""
                    var ref = ""

                    if (amount.find()) amt = amount.group(1)
                    if (utr.find()) ref = utr.group(1)

                    val time = SimpleDateFormat(
                        "dd-MM-yyyy HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())

                    sendToTelegram(amt, ref, time)
                }
            }
        }
    }

    private fun sendToTelegram(amount: String, utr: String, time: String) {

        val botToken = "7847409608:AAGD3HbHbgbcky_14ib_j47115RQc7k5Yso"
        val chatId = "1386134836"

        val text = "Payment Received\nAmount: ₹$amount\nUTR: $utr\nAccount: 5098\nTime: $time"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text")
            .build()

        client.newCall(request).execute()
    }
}
