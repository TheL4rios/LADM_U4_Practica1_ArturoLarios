package mx.edu.ittepic.ladm_u4_practica1_arturolarios

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_show_contacts.*
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.DB
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.Utils

class ShowContactsActivity : AppCompatActivity() {

    private val db = DB.getInstance(this)
    private var contacts : ArrayList<ContactsDB>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_contacts)

        fillList()
    }

    private fun fillList()
    {
        contacts = getContacts()

        contacts?.let { c ->
            val data = ArrayList<String>()

            c.forEach { contact ->
                data.add("Nombre: ${contact.name}\nTelefono(s):\n${contact.number.substring(0, contact.number.length - 1)}")
            }

            addContactsList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
            return
        }

        Utils.showToastMessageLong("No hay contactos que mostrar", this)
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
                    contacts.add(ContactsDB(c.getInt(0), c.getString(1), getNumbers(c.getInt(0))))
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
