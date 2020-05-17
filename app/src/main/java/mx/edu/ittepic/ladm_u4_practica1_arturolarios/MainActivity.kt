package mx.edu.ittepic.ladm_u4_practica1_arturolarios

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils.Utils

class MainActivity : AppCompatActivity() {

    object Constants {
        const val READ_CONTACTS_SUCCESS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.title = "Lista de Contactos"

        grantPermission()
    }

    override fun onResume() {
        super.onResume()
        fillList()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_CONTACTS_SUCCESS)
        {
            fillList()
            return
        }

        Utils.showToastMessageLong("Algó salió mal, por favor vuelva a intentarlo", this)
    }

    @SuppressLint("Recycle")
    private fun fillList()
    {
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        cursor?.let { c ->
            if (c.moveToFirst())
            {
                val phones = ArrayList<Contacts>()

                do {
                    val id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    var phone = ""

                    if (c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                    {
                        val numbers = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                                            arrayOf(id), null)

                        numbers?.let { n ->
                            while (n.moveToNext())
                            {
                                phone += n.getString(n.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + "\n"
                            }
                            n.close()
                        }

                        phones.add(Contacts(name, phone))
                    }
                } while (c.moveToNext())

                showList(phones)
            }
        }
    }

    private fun showList(phones : ArrayList<Contacts>)
    {
        val data = ArrayList<String>()

        phones.forEach { p ->
            data.add("Nombre: ${p.name}\nTelefono(s):\n${p.number}")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
        contactsList.adapter = adapter

        contactsList.setOnItemClickListener { _, _, position, _ ->
            AlertDialog.Builder(this)
                .setTitle("Contacto")
                .setMessage("${data[position]}\n¿Desea agregar este contacto a la lista?")
                .setPositiveButton("SI"){_, _ ->

                }
                .setNegativeButton("NO"){_, _ ->}
                .show()
        }
    }

    private fun grantPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), Constants.READ_CONTACTS_SUCCESS)
        }
    }
}