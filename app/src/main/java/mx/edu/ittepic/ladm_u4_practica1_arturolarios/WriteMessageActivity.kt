package mx.edu.ittepic.ladm_u4_practica1_arturolarios

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_write_message.*
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.DB
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.Utils

class WriteMessageActivity : AppCompatActivity() {

    private var action : Boolean = false
    private val db = DB.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_message)

        action = getMessages()

        btnSave.setOnClickListener {
            saveMessages()
        }
    }

    override fun onDestroy()
    {
        db?.close()
        super.onDestroy()
    }

    private fun saveMessages()
    {
        if (action)
        {
            updateMessages()
            return
        }
        insertMessages()
    }

    @SuppressLint("Recycle")
    private fun getMessages() : Boolean
    {
        try {
            val select = db?.readableDatabase

            val columns = arrayOf("*")

            val cursor = select?.query(DB.MESSAGES, columns, null, null, null, null, null)

            cursor?.let { c ->
                if(c.moveToFirst())
                {
                    val messages = ArrayList<String>()

                    do
                    {
                        messages.add(c.getString(1))
                    }while(c.moveToNext())

                    txtMessageGood.setText(messages[0])
                    txtMessageBad.setText(messages[1])

                    return true
                }
            }
        }
        catch (e : SQLiteException){}
        return false
    }

    private fun updateMessages()
    {
        try
        {
            val update = db?.writableDatabase

            val dataGood = ContentValues()
            dataGood.put("message", txtMessageGood.text.toString())
            dataGood.put("type", 1)

            var answer = update?.update(DB.MESSAGES, dataGood, "type = 1", null)

            if(answer == 0)
            {
                Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
            }
            else
            {
                val dataBad = ContentValues()
                dataBad.put("message", txtMessageBad.text.toString())
                dataBad.put("type", 0)

                answer = update?.update(DB.MESSAGES, dataBad, "type = 0", null)

                if(answer == 0)
                {
                    Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
                    return
                }
                Utils.showToastMessageLong("Se actualizó correctamente", this)
            }
        }
        catch(e : SQLiteException)
        {
            Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
        }
    }

    private fun insertMessages()
    {
        try
        {
            val insert = db?.writableDatabase

            val dataGood = ContentValues()
            dataGood.put("message", txtMessageGood.text.toString())
            dataGood.put("type", 1)

            var answer = insert?.insert(DB.MESSAGES, "idActivity", dataGood)

            if(answer?.toInt() == -1)
            {
                Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
            }
            else
            {
                val dataBad = ContentValues()

                dataBad.put("message", txtMessageBad.text.toString())
                dataBad.put("type", 0)

                answer = insert?.insert(DB.MESSAGES, "idMessage", dataBad)

                if(answer?.toInt() == -1)
                {
                    Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
                    return
                }
                Utils.showToastMessageLong("Se insertó correctamente", this)
            }
        }
        catch(e : SQLiteException)
        {
            Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
        }
    }
}
