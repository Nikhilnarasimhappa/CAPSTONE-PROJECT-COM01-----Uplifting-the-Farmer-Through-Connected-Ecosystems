package com.example.capestone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "agriculture_db";
    private static final int DATABASE_VERSION = 11; // Increment for schema updates

    // Table names
    public static final String TABLE_CROPS = "crops";
    public static final String TABLE_FERTILIZERS = "fertilizers";
    public static final String TABLE_MACHINERY = "machinery";
    public static final String TABLE_LAND = "land";
    public static final String TABLE_HISTORY = "history"; // New table for tracking activities

    // Common column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_IMAGE_PATH = "imagepath";

    // Land-specific columns
    public static final String COLUMN_SIZE = "size";
    public static final String COLUMN_LOCATION = "location";

    // History table columns
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_ACTION_TYPE = "actionType"; // e.g., add, buy, rent
    public static final String COLUMN_ITEM_TYPE = "itemType"; // e.g., crop, fertilizer, machinery, land
    public static final String COLUMN_ITEM_NAME = "itemName";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // SQL commands to create tables
    private static final String TABLE_CROPS_CREATE =
            "CREATE TABLE " + TABLE_CROPS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_QUANTITY + " TEXT NOT NULL, " +
                    COLUMN_PRICE + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_PATH + " TEXT);";

    private static final String TABLE_FERTILIZERS_CREATE =
            "CREATE TABLE " + TABLE_FERTILIZERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_QUANTITY + " TEXT NOT NULL, " +
                    COLUMN_PRICE + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_PATH + " TEXT);";

    private static final String TABLE_MACHINERY_CREATE =
            "CREATE TABLE " + TABLE_MACHINERY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_QUANTITY + " TEXT NOT NULL, " +
                    COLUMN_PRICE + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_PATH + " TEXT);";

    private static final String TABLE_LAND_CREATE =
            "CREATE TABLE " + TABLE_LAND + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SIZE + " TEXT NOT NULL, " +
                    COLUMN_PRICE + " TEXT NOT NULL, " +
                    COLUMN_LOCATION + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_PATH + " TEXT);";

    private static final String TABLE_HISTORY_CREATE =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT NOT NULL, " +
                    COLUMN_ACTION_TYPE + " TEXT NOT NULL, " +
                    COLUMN_ITEM_TYPE + " TEXT NOT NULL, " +
                    COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CROPS_CREATE);
        db.execSQL(TABLE_FERTILIZERS_CREATE);
        db.execSQL(TABLE_MACHINERY_CREATE);
        db.execSQL(TABLE_LAND_CREATE);
        db.execSQL(TABLE_HISTORY_CREATE); // Create history table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CROPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FERTILIZERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MACHINERY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAND);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY); // Drop history table
        onCreate(db);
    }

    // Add crop
    public long addCrop(String name, String quantity, String price, String imagePath) {
        return insertData(TABLE_CROPS, name, quantity, price, imagePath);
    }

    // Add fertilizer
    public long addFertilizer(String name, String quantity, String price, String imagePath) {
        return insertData(TABLE_FERTILIZERS, name, quantity, price, imagePath);
    }

    // Add machinery
    public long addMachinery(String name, String quantity, String price, String imagePath) {
        return insertData(TABLE_MACHINERY, name, quantity, price, imagePath);
    }

    // Add land
    public long addLand(String size, String price, String location, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SIZE, size);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_IMAGE_PATH, imagePath);

        return db.insert(TABLE_LAND, null, values);
    }

    // General method to insert common data
    private long insertData(String tableName, String name, String quantity, String price, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_IMAGE_PATH, imagePath);

        return db.insert(tableName, null, values);
    }

    // Log an activity in history
    public long logHistory(String userId, String actionType, String itemType, String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_ACTION_TYPE, actionType); // "add", "buy", etc.
        values.put(COLUMN_ITEM_TYPE, itemType); // "crop", "fertilizer", "land", etc.
        values.put(COLUMN_ITEM_NAME, itemName); // e.g., "Wheat", "Tractor", etc.
        return db.insert(TABLE_HISTORY, null, values);
    }

    // Fetch history for a specific user
    public Cursor getHistory(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_HISTORY, null, COLUMN_USER_ID + "=?", new String[]{userId}, null, null, COLUMN_TIMESTAMP + " DESC");
    }

    // Fetch all land data
    public Cursor getAllLand() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LAND, null, null, null, null, null, null);
    }

    // Fetch all items from a specific table
    public Cursor getAllItems(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(tableName, null, null, null, null, null, null);
    }
}
