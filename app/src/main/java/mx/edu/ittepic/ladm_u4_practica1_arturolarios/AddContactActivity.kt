package mx.edu.ittepic.ladm_u4_practica1_arturolarios

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_add_contact.*
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.DB
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.Utils

class AddContactActivity : AppCompatActivity() {

    private var type = 0
    private val db = DB.getInstance(this)
    private var update = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)
        this.title = "Agregar Contacto"

        initialize()
    }

    private fun initialize()
    {
        val extra = intent.extras
        txtName.setText(extra?.getString("name"))
        txtPhones.setText(extra?.getString("number")?.substring(0, extra.getString("number")!!.length - 1))
        initializeSpinner()

        val id = extra?.getInt("id")

        id?.let {
            update = true
            this.title = "Modificar Contacto de la lista"
            val selection = if (extra.getInt("type") == 0) 2 else 1
            spinner.setSelection(selection)
        }

        btnSaveContact.setOnClickListener {
            if (type == 0)
            {
                Utils.showAlertMessage("Atención", "Selecciones un tipo de mensaje", this)
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Atención")
                .setMessage("¿Está seguro de esta acción?")
                .setPositiveButton("SI"){_, _ ->
                    if (update)
                        updateContact(id)
                    else
                        saveContact()
                }
                .setNegativeButton("NO"){_, _ ->}
                .show()
        }
    }

    private fun updateContact(id : Int?)
    {
        val update = db?.writableDatabase

        val data = ContentValues()
        data.put("name", txtName.text.toString())

        when(type)
        {
            1 -> data.put("type", 1)
            2 -> data.put("type", 0)
        }

        val answer = update?.update(DB.CONTACTS, data, "idContact=?", arrayOf(id?.toString()))

        if (answer == 0)
        {
            Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
            return
        }

        Utils.showToastMessageLong("Se actualizó correctamente", this)
        finish()
    }

    private fun saveContact()
    {
        try {
            val insert = db?.writableDatabase

            var data = ContentValues()
            data.put("name", txtName.text.toString())

            when(type)
            {
                1 -> data.put("type", 1)
                2 -> data.put("type", 0)
            }

            var answer = insert?.insert(DB.CONTACTS, "idContact", data)

            if(answer?.toInt() == -1)
            {
                Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
            }
            else
            {
                val phones = txtPhones.text.toString().split("\n")
                val id = getLastContact()

                if (id == -1)
                {
                    Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
                    return
                }

                phones.forEach { n ->
                    data = ContentValues()
                    data.put("idContact", id)
                    data.put("number", n)

                    answer = insert?.insert(DB.NUMBERS, "idNumber", data)

                    if (answer?.toInt() == -1)
                    {
                        Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
                        return
                    }
                }
                Utils.showToastMessageLong("Se insertó correctamente", this)
                finish()
            }
        }
        catch(e : SQLiteException)
        {
            Utils.showAlertMessage("Error", "Algo salió mal, vuelva a intentarlo", this)
        }
    }

    @SuppressLint("Recycle")
    private fun getLastContact() : Int
    {
        val select = db?.readableDatabase

        val cursor = select?.query(DB.CONTACTS, arrayOf("MAX(idContact)"), null, null, null, null, null)

        cursor?.let { c ->
            if (c.moveToFirst())
            {
                return c.getInt(0)
            }
        }

        return -1
    }

    private fun initializeSpinner()
    {
        val options = arrayOf("Escoja tipo de mensaje", "Deseado", "Indeseado")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                type = position
            }

            override fun onNothingSelected(parent: AdapterView<*>){
                // Another interface callback
            }
        }
    }
}
