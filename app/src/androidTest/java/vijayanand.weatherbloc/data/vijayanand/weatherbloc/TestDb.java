package vijayanand.weatherbloc;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(vijayanand.weatherbloc.WeatherDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(vijayanand.weatherbloc.WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(vijayanand.weatherbloc.WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(vijayanand.weatherbloc.WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new vijayanand.weatherbloc.WeatherDbHelper(
                this.mContext).getWritableDatabase();

        assertEquals(true, db.isOpen());
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        do {
            tableNameHashSet.remove(c.getString(0));
        } while(c.moveToNext());

        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());
        c = db.rawQuery("PRAGMA table_info(" + vijayanand.weatherbloc.WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        final HashSet<String> locationColumnHashSet = new HashSet<String>();

        locationColumnHashSet.add(vijayanand.weatherbloc.WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(vijayanand.weatherbloc.WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(vijayanand.weatherbloc.WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(vijayanand.weatherbloc.WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(vijayanand.weatherbloc.WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testLocationTable() {

        insertLocation();
    }

    public void testWeatherTable() {

        long locationRowId = insertLocation();
        assertFalse("Error: Location Not Inserted Correctly", locationRowId == -1L);
        vijayanand.weatherbloc.WeatherDbHelper dbHelper = new vijayanand.weatherbloc.WeatherDbHelper(mContext);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues weatherValues = vijayanand.weatherbloc.TestUtilities.createWeatherValues(locationRowId);
        long weatherRowId = db.insert(vijayanand.weatherbloc.WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);

        assertTrue(weatherRowId != -1);
        Cursor weatherCursor = db.query(
                vijayanand.weatherbloc.WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
    public long insertLocation() {

        vijayanand.weatherbloc.WeatherDbHelper dbHelper = new vijayanand.weatherbloc.WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = vijayanand.weatherbloc.TestUtilities.createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = db.insert(vijayanand.weatherbloc.WeatherContract.LocationEntry.TABLE_NAME, null, testValues);
        assertTrue(locationRowId != -1);

        Cursor cursor = db.query(
                vijayanand.weatherbloc.WeatherContract.LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );
        vijayanand.weatherbloc.TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext());

        cursor.close();
        db.close();
        return locationRowId;
    }
}