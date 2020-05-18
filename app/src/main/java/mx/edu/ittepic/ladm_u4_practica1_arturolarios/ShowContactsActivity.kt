package mx.edu.ittepic.ladm_u4_practica1_arturolarios

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_show_contacts.*
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.DB
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.Utils

class ShowContactsActivity : AppCompatActivity() {

    private val db = DB.getInstance(this)
    private var contacts : ArrayList<ContactsDB>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_contacts)
        this.title = "Contactos Agregados"
    }

    override fun onResume() {
        super.onResume()
        fillList()
    }

    private fun fillList()
    {
        contacts = getContacts()

        contacts?.let { c ->
            val data = ArrayList<String>()

            c.forEach { contact ->
                val type = if (contact.type == 0) "Indeseado" else "Deseado"

                data.add("Nombre: ${contact.name}\nTelefono(s):\n${contact.number.substring(0, contact.number.length - 1)}\n" +
                        "Tipo de contacto: $type")
            }

            addContactsList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)

            addContactsList.setOnItemClickListener { _, _, position, _ ->
                AlertDialog.Builder(this)
                    .setTitle("¿Qué deseas hacer con este contacto?")
                    .setMessage("${data[position]}\n")
                    .setPositiveButton("Eliminar"){_, _ ->
                        deleteContact(c[position])
                    }
                    .setNegativeButton("Modificar"){_, _ ->
                        val modifyContact = Intent(this, AddContactActivity :: class.java)

                        modifyContact.putExtra("id", c[position].id)
                        modifyContact.putExtra("name", c[position].name)
                        modifyContact.putExtra("number", c[position].number)
                        modifyContact.putExtra("type", c[position].type)

                        startActivity(modifyContact)
                    }
                    .setNeutralButton("Cancelar"){_, _ ->}
                    .show()
            }

            return
        }

        Utils.showToastMessageLong("No hay contactos que mostrar", this)
    }

    private fun deleteContact(contact: ContactsDB)
    {
        val delete = db?.writableDatabase

        var answer = delete?.delete(DB.NUMBERS, "idContact=?", arrayOf(contact.id.toString()))

        if (answer == 0)
        {
            Utils.showAlertMessage("Atención", "Algo salió mal, vuelva a intentarlo", this)
            return
        }

        answer = delete?.delete(DB.CONTACTS, "idContact=?", arrayOf(contact.id.toString()))

        if (answer == 0)
        {
            Utils.showAlertMessage("Atención", "Algo salió mal, vuelva a intentarlo", this)
            return
        }

        Utils.showToastMessageLong("Se elimicó con éxito", this)
        fillList()
    }

    @SuppressLint("Recycle")
    private fun getContacts() : ArrayList<ContactsDB>?
    {
        val select = db?.readableDatabase

        val cursor = select?.query(DB.CONTACTS, arrayOf("*"), null, null, null, null, null)

        cursor?.let { c ->
            val contacts = ArrayList<ContactsDB>()

            if (c.moveToFirst())
            {
                do {
                    contacts.add(ContactsDB(c.getInt(0), c.getString(1), getNumbers(c.getInt(0)), c.getInt(2)))
                } while (c.moveToNext())
            }
            return contacts
        }

        return null
    }

    @SuppressLint("Recycle")
    private fun getNumbers(id: Int): String
    {
        val select = db?.readableDatabase

        val cursor = select?.query(DB.NUMBERS, arrayOf("*"), "idContact=?", arrayOf(id.toString()), null, null, null)

        cursor?.let { c ->
            var tel = ""
            if (c.moveToFirst())
            {
                do {
                    tel += c.getString(2) + "\n"
                }while (c.moveToNext())
            }

            return tel
        }
        return ""
    }
}
