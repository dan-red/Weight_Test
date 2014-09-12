package mdash.cs.swarthmore.edu.weighttest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by mborris1 on 6/3/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DB_NAME = "Withings";
    private static final String TABLE_NAME = "Weight_Data";
    private static final String COL_EPOCH = "Epoch";
    private static final String COL_DATE = "Date";
    private static final String COL_LBS = "Weight";
    private static final String COL_FAT = "Fat";
    private static final String COL_LEAN = "Lean";
    private Context mContext;
    private static String dbPath = "/data/data/edu.swarthmore.cs.mdash.weighttest.app/databases/" + DB_NAME;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.mContext = context;
    }

    // Creating the table
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Place: ", "in onCreate() of database class");

        // Create the "weight_data" table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        String create = "CREATE TABLE " + TABLE_NAME + "("
                + COL_EPOCH + " REAL,"
                + COL_DATE + " TEXT,"
                + COL_LBS + " TEXT,"
                + COL_FAT + " TEXT,"
                + COL_LEAN + " TEXT,"
                + "UNIQUE(" + COL_DATE + "," + COL_LBS + ") ON CONFLICT IGNORE);";
        db.execSQL(create);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        //Create tables again
        onCreate(db);
    }

    /** CRUD operations: create, read, update, and delete entries */
    // Add a new entry manually
    public void addEntry(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COL_EPOCH, entry.getEpoch());
        cv.put(COL_DATE, entry.getDate());
        cv.put(COL_LBS, entry.getWeight());
        cv.put(COL_FAT, entry.getFat());
        cv.put(COL_LEAN, entry.getLean());

        db.insert(TABLE_NAME, null, cv);
        db.close();
    }

    // Get most recent entry (first in database)
    public Entry getFirstEntry() {
        Log.d("Place: ", "in getFirstEntry()");
        // Select query
        String selectQuery = "select * from " + TABLE_NAME + " limit 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Entry entry = new Entry();

        // loop through rows
        if (cursor.moveToFirst()) {
            entry.setEpoch(cursor.getInt(0));
            entry.setDate(cursor.getString(1));
            entry.setWeight(cursor.getString(2));
            entry.setFat(cursor.getString(3));
            entry.setLean(cursor.getString(4));
        }
        return entry;
    }

    // Update a single contact
    public int updateEntry(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COL_EPOCH, entry.getEpoch());
        cv.put(COL_DATE, entry.getDate());
        cv.put(COL_LBS, entry.getWeight());
        cv.put(COL_FAT, entry.getFat());
        cv.put(COL_LEAN, entry.getLean());

        // updating row
        return db.update(TABLE_NAME, cv, COL_EPOCH + " = ?",
                new String[] { String.valueOf(entry.getEpoch()) });
    }

    // Delete a single entry
    public void deleteContact(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_EPOCH + " = ?",
                new String[] { String.valueOf(entry.getEpoch()) });
        db.close();
    }

    // Get the number of rows/entries in database
    public int getRowCount() {
        int count = 1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            count = cursor.getCount();
        }
        return count;
    }

    // Get the most recent timestamp (biggest epoch time value)
    public int getNewestEpoch() {
        int newestEpoch = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "select max(Epoch) from " + TABLE_NAME;
      //  String select = "select Epoch from " + TABLE_NAME + " limit 1";
        Cursor c = db.rawQuery(select, null);
        if (c.moveToFirst()) {
            newestEpoch = c.getInt(0);
        }
        return newestEpoch;
    }

    // Custom method to drop and recreate table (flush out data)
    public void dropAndCreateTable() {
        Log.d("Place: ", "in dropAndCreateTable()");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        String create = "CREATE TABLE " + TABLE_NAME + "("
                + COL_EPOCH + " REAL,"
                + COL_DATE + " TEXT,"
                + COL_LBS + " TEXT,"
                + COL_FAT + " TEXT,"
                + COL_LEAN + " TEXT,"
                + "UNIQUE(" + COL_DATE + "," + COL_LBS + ") ON CONFLICT IGNORE);";
        db.execSQL(create);
    }

    // Check if the table already exists; if so, returns true
    public boolean checkTable(SQLiteDatabase db) {
        if (TABLE_NAME == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", TABLE_NAME});
        if (!cursor.moveToFirst())
        {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    // Check if the db already exists; if so, returns true
    public boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(dbPath + DB_NAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return checkDB != null ? true : false;
    }

    // Returns the db table name
    public String getTableName() {
        return TABLE_NAME;
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query ="SELECT * FROM " + TABLE_NAME;

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null) {
                cursor.moveToNext();
            }
            return cursor;
        } catch (SQLException exception) {
            Log.e("DatabaseHelper.getData(): ", exception.toString());
            throw exception;
        }
    }

    public void parseData(Entry[] newEntries) {
        Cursor cursor = getData();
        Entry entry;
        int counter = 0;

        if (cursor.moveToFirst()) {
            do {
                entry = new Entry();

                try {
                    entry.setEpoch(cursor.getInt(0));
                } catch (Exception e) {
                    Log.d("DatabaseHelper.parseData(): ", "Missing epoch data");
                }

                try {
                    entry.setDate(cursor.getString(1));
                } catch (Exception e) {
                    Log.d("DatabaseHelper.parseData(): ", "Missing date data");
                }

                try {
                    entry.setWeight(cursor.getString(2));
                } catch (Exception e) {
                    Log.d("DatabaseHelper.parseData(): ", "Missing weight data");
                }

                try {
                    entry.setFat(cursor.getString(3));
                } catch (Exception e) {
                    Log.d("DatabaseHelper.parseData(): ", "Missing fat mass data");
                }

                try {
                    entry.setLean(cursor.getString(4));
                } catch (Exception e) {
                    Log.d("DatabaseHelper.parseData(): ", "Missing lean mass data");
                }

                newEntries[counter] = entry;
                counter++;

            } while (cursor.moveToNext());
        }
        else {
            Log.e("DatabaseHelper.parseData(): ", "Could not moveToFirst");
        }
    }


}