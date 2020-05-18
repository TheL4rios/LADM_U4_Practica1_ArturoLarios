package mx.edu.ittepic.ladm_u3_practica1_arturolarios.Utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DB(context : Context) : SQLiteOpenHelper(context, "MESSAGES", null, 1)
{
    companion object
    {
        const val MESSAGES = "MESSAGES"
        const val CONTACTS = "CONTACTS"
        const val NUMBERS = "NUMBERS"

        private var instance : DB?= null

        fun getInstance(context : Context) : DB?
        {
            instance.let {
                instance =
                    DB(context)
            }

            return instance
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // 1 -> good message
        // 0 -> bad message

        var sql = "CREATE TABLE MESSAGES(" +
                      "idMessage INTEGER PRIMARY KEY AUTOINCREMENT," +
                      "message VARCHAR(2000)," +
                      "type INTEGER" +
                  ")"
        db?.execSQL(sql)

        sql = "CREATE TABLE CONTACTS(" +
                    "idContact INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name VARCHAR(200)," +
                    "type INTEGER" +
              ")"
        db?.execSQL(sql)

        sql = "CREATE TABLE NUMBERS(" +
                    "idNumber INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "idContact INTEGER," +
                    "number VARCHAR(50)," +
                    "FOREIGN KEY(idContact) REFERENCES CONTACTS(idContact)" +
                ")"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS MESSAGES")
        db?.execSQL("DROP TABLE IF EXISTS CONTACTS")
        db?.execSQL("DROP TABLE IF EXISTS NUMBERS")
        onCreate(db)
    }
}