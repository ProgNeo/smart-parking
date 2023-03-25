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

        val values = ContentValues().apply {
            put(PARKING_COLUMN_NAME, parking.id)
            put(PARKING_COLUMN_LATITUDE, parking.latitude)
            put(PARKING_COLUMN_LONGITUDE, parking.longitude)
        }

        db.insert(TABLE_NAME_PARKING, null, values)
        db.close()
    }

    fun updateParking(parking: Parking) {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(PARKING_COLUMN_NAME, parking.id)
            put(PARKING_COLUMN_LATITUDE, parking.latitude)
            put(PARKING_COLUMN_LONGITUDE, parking.longitude)
        }

        db.update(TABLE_NAME_PARKING, values,
            "$PARKING_COLUMN_ID=${parking.id}", arrayOf("${parking.id}"))
        db.close()
    }

    fun insertParkingPlace(parkingPlace: ParkingPlace) {
        val db = this.readableDatabase

        val values = ContentValues().apply {
            put(PARKING_PLACE_COLUMN_ID, parkingPlace.id)
            put(PARKING_PLACE_COLUMN_LATITUDE, parkingPlace.latitude)
            put(PARKING_PLACE_COLUMN_LONGITUDE, parkingPlace.longitude)
            put(PARKING_PLACE_COLUMN_EMPLOYED, parkingPlace.employed)
            put(PARKING_PLACE_COLUMN_BOOKED, parkingPlace.booked)
            put(PARKING_PLACE_PARKING_ID, parkingPlace.parkingId)
        }

        db.insert(TABLE_NAME_PARKING_PLACE, null, values)
        db.close()
    }

    fun updateParkingPlace(parkingPlace: ParkingPlace) {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(PARKING_PLACE_COLUMN_ID, parkingPlace.id)
            put(PARKING_PLACE_COLUMN_LATITUDE, parkingPlace.latitude)
            put(PARKING_PLACE_COLUMN_LONGITUDE, parkingPlace.longitude)
            put(PARKING_PLACE_COLUMN_EMPLOYED, parkingPlace.employed)
            put(PARKING_PLACE_COLUMN_BOOKED, parkingPlace.booked)
            put(PARKING_PLACE_PARKING_ID, parkingPlace.parkingId)
        }

        db.update(TABLE_NAME_PARKING_PLACE, values,
            "$PARKING_PLACE_COLUMN_ID=${parkingPlace.id}", arrayOf("${parkingPlace.id}"))
        db.close()
    }

    fun getParkingList(): List<Parking> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME_PARKING"
        val cursor = db.rawQuery(query, null)

        val parkingList = mutableListOf<Parking>()
        with(cursor) {
            while (moveToNext()) {
                val parking = Parking(
                    id = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_ID)),
                    name = getString(cursor.getColumnIndexOrThrow(PARKING_COLUMN_NAME)),
                    latitude = getDouble(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LATITUDE)),
                    longitude = getDouble(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LONGITUDE))
                )
                parkingList.add(parking)
            }
        }
        cursor.close()
        db.close()

        return parkingList
    }

    fun getParking(id: Int): Parking? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME_PARKING WHERE $PARKING_COLUMN_ID = $id"
        val cursor = db.rawQuery(query, null)

        var parking: Parking? = null
        with(cursor) {
            while (moveToNext()) {
                parking = Parking(
                    id = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_ID)),
                    name = getString(cursor.getColumnIndexOrThrow(PARKING_COLUMN_NAME)),
                    latitude = getDouble(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LATITUDE)),
                    longitude = getDouble(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LONGITUDE))
                )
                break
            }
        }
        cursor.close()
        db.close()

        return parking
    }

    fun getParkingPlacesList(): List<ParkingPlace> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME_PARKING_PLACE"
        val cursor = db.rawQuery(query, null)

        val parkingPlacesList = mutableListOf<ParkingPlace>()
        with(cursor) {
            while (moveToNext()) {
                val parkingPlace = ParkingPlace(
                    id = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_ID)),
                    latitude = getDouble(cursor.getColumnIndexOrThrow(PARKING_COLUMN_NAME)),
                    longitude = getDouble(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LATITUDE)),
                    employed = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LONGITUDE)) > 0,
                    booked = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LONGITUDE)) > 0,
                    parkingId = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LONGITUDE))
                )
                parkingPlacesList.add(parkingPlace)
            }
        }
        cursor.close()
        db.close()

        return parkingPlacesList
    }

    fun getParkingPlace(id: Int): ParkingPlace? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME_PARKING_PLACE WHERE $PARKING_PLACE_COLUMN_ID = $id"
        val cursor = db.rawQuery(query, null)

        var parkingPlace: ParkingPlace? = null
        with(cursor) {
            while (moveToNext()) {
                parkingPlace = ParkingPlace(
                    id = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_ID)),
                    latitude = getDouble(cursor.getColumnIndexOrThrow(PARKING_COLUMN_NAME)),
                    longitude = getDouble(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LATITUDE)),
                    employed = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LONGITUDE)) > 0,
                    booked = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LONGITUDE)) > 0,
                    parkingId = getInt(cursor.getColumnIndexOrThrow(PARKING_COLUMN_LONGITUDE))
                )
                break
            }
        }
        cursor.close()
        db.close()

        return parkingPlace
    }
}
