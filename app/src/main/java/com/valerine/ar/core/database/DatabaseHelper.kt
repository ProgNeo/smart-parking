package com.valerine.ar.core.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.valerine.ar.core.database.models.Parking
import com.valerine.ar.core.database.models.ParkingPlace

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SmartParking.db"

        private const val TABLE_NAME_PARKING = "Parking"
        private const val PARKING_COLUMN_ID = "id"
        private const val PARKING_COLUMN_NAME = "name"
        private const val PARKING_COLUMN_LATITUDE = "latitude"
        private const val PARKING_COLUMN_LONGITUDE = "longitude"

        private const val TABLE_NAME_PARKING_PLACE = "ParkingPlace"
        private const val PARKING_PLACE_COLUMN_ID = "id"
        private const val PARKING_PLACE_COLUMN_LATITUDE = "latitude"
        private const val PARKING_PLACE_COLUMN_LONGITUDE = "longitude"
        private const val PARKING_PLACE_COLUMN_EMPLOYED = "employed"
        private const val PARKING_PLACE_COLUMN_BOOKED = "booked"
        private const val PARKING_PLACE_PARKING_ID = "parking_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val parkingTable = """
            CREATE TABLE $TABLE_NAME_PARKING
            (
                $PARKING_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $PARKING_COLUMN_NAME TEXT NOT NULL,
                $PARKING_COLUMN_LATITUDE REAL NOT NULL,
                $PARKING_COLUMN_LONGITUDE REAL NOT NULL
            )
        """.trimIndent()

        val bookingParkingTable = """
            CREATE TABLE $TABLE_NAME_PARKING_PLACE
            (
                $PARKING_PLACE_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $PARKING_PLACE_COLUMN_LATITUDE REAL NOT NULL,
                $PARKING_PLACE_COLUMN_LONGITUDE REAL NOT NULL,
                $PARKING_PLACE_COLUMN_EMPLOYED INTEGER DEFAULT "FALSE" NOT NULL,
                $PARKING_PLACE_COLUMN_BOOKED INTEGER DEFAULT "FALSE" NOT NULL,
                $PARKING_PLACE_PARKING_ID INTEGER NOT NULL,
                FOREIGN KEY ($PARKING_PLACE_PARKING_ID)
                    REFERENCES $TABLE_NAME_PARKING ($PARKING_PLACE_COLUMN_ID)
            )
        """.trimIndent()

        db.execSQL(parkingTable)
        db.execSQL(bookingParkingTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PARKING")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PARKING_PLACE")
        onCreate(db)
    }

    fun insertParking(parking: Parking) {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(PARKING_COLUMN_NAME, parking.id)
        contentValues.put(PARKING_COLUMN_LATITUDE, parking.latitude)
        contentValues.put(PARKING_COLUMN_LONGITUDE, parking.longitude)

        db.insert(TABLE_NAME_PARKING, null, contentValues)
    }

    fun insertParkingPlace(parkingPlace: ParkingPlace) {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(PARKING_PLACE_COLUMN_ID, parkingPlace.id)
        contentValues.put(PARKING_PLACE_COLUMN_LATITUDE, parkingPlace.latitude)
        contentValues.put(PARKING_PLACE_COLUMN_LONGITUDE, parkingPlace.longitude)
        contentValues.put(PARKING_PLACE_COLUMN_EMPLOYED, parkingPlace.employed)
        contentValues.put(PARKING_PLACE_COLUMN_BOOKED, parkingPlace.booked)
        contentValues.put(PARKING_PLACE_PARKING_ID, parkingPlace.parkingId)

        db.insert(TABLE_NAME_PARKING_PLACE, null, contentValues)
    }
}
