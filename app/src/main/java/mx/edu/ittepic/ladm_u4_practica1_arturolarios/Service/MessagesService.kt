package mx.edu.ittepic.ladm_u4_practica1_arturolarios.Service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.CallLog
import android.telephony.SmsManager
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.DB
import java.util.*
import kotlin.collections.ArrayList


class MessagesService : Service() {

    private var mTimer : Timer? = null
    private var missedCalls = ArrayList<String>()
    private var currentCallsList = ArrayList<String>()
    private var firstTime = true
    private val db = DB.getInstance(this)

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mTimer = Timer()

        mTimer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    findMissedCalls()
                }
            }, 0, 1000 * 30)
    }

    private fun findMissedCalls() {
        val t = Thread(Runnable {
            loadMissedCalls()
        })
        t.start()
    }

    @SuppressLint("Recycle")
    private fun loadMissedCalls()
    {
        try {
            val callsUri = CallLog.Calls.CONTENT_URI

            val cursor = contentResolver.query(callsUri, null, null, null, null)

            cursor?.let { c ->
                if (c.moveToFirst())
                {
                    missedCalls = ArrayList()
                    do{
                        if (c.getInt(c.getColumnIndex(CallLog.Calls.TYPE)) == CallLog.Calls.MISSED_TYPE)
                        {
                            missedCalls.add(c.getString(c.getColumnIndex(CallLog.Calls.NUMBER)))
                        }
                    }while (c.moveToNext())
                }

                if (firstTime)
                {
                    currentCallsList = missedCalls
                    firstTime = false
                    return
                }

                if (missedCalls.size > currentCallsList.size)
                {
                    currentCallsList = missedCalls
                    sendMessage()
                }
            }
        }
        catch (e : SecurityException){}
    }

    private fun sendMessage()
    {
        val data = getNumber()

        if (data.size != 0)
        {
            val message = getMessage(data[0])
            SmsManager.getDefault().sendTextMessage(data[1], null, message, null, null)
        }
    }

    private fun getNumber() : ArrayList<String>
    {
        val number = currentCallsList[currentCallsList.size - 1]
        val select = db?.readableDatabase
        val contact = ArrayList<String>()

        val cursor = select?.query(DB.NUMBERS, arrayOf("idContact", "number"), null, null, null, null, null)

        cursor?.let { c ->
            if (c.moveToFirst())
            {
                var DBnumber = ""
                do {
                    DBnumber =
                        if (c.getString(1).contains("-"))
                        {
                            c.getString(1).replace("-", "")
                        }
                        else
                        {
                            c.getString(1)
                        }

                    if (DBnumber == number)
                    {
                        contact.add(c.getString(0)) // id
                        contact.add(number) // number
                        return contact
                    }
                }while (c.moveToNext())
            }
        }
        cursor?.close()
        return contact
    }

    @SuppressLint("Recycle")
    private fun getMessage(id : String) : String
    {
        val select = db?.readableDatabase
        var cursor = select?.query(DB.CONTACTS, arrayOf("type"), "idContact=?", arrayOf(id), null, null, null)

        cursor?.let { c ->
            var type = ""
            if (c.moveToFirst())
            {
                type = c.getString(0)

                cursor = select?.query(DB.MESSAGES, arrayOf("message"), "type=?", arrayOf(type), null, null, null)

                cursor?.let { cursor ->
                    if (cursor.moveToFirst())
                    {
                        return cursor.getString(0)
                    }
                }
            }
        }

        return ""
    }
}
