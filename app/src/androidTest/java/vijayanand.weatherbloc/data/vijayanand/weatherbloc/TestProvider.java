package vijayanand.weatherbloc;


import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.test.AndroidTestCase;

import vijayanand.weatherbloc.WeatherContract.LocationEntry;
import vijayanand.weatherbloc.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {

        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();
        cursor = mContext.getContentResolver().query(

                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecordsFromDB() {

        vijayanand.weatherbloc.WeatherDbHelper dbHelper = new vijayanand.weatherbloc.WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(WeatherEntry.TABLE_NAME, null, null);
        db.delete(LocationEntry.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecords() {

        deleteAllRecordsFromDB();
    }

    @Override
    protected void setUp() throws Exception {

        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {

        PackageManager pm = mContext.getPackageManager();
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                vijayanand.weatherbloc.WeatherProvider.class.getName());
        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + vijayanand.weatherbloc.WeatherContract.CONTENT_AUTHORITY,
                    providerInfo.authority, vijayanand.weatherbloc.WeatherContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {

            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {

        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                WeatherEntry.CONTENT_TYPE, type);
        String testLocation = "1264527";
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        assertEquals("Error: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE",
                WeatherEntry.CONTENT_TYPE, type);
        long testDate = 1419120000L;
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        assertEquals("Error: the WeatherEntry CONTENT_URI with location and date should return WeatherEntry.CONTENT_ITEM_TYPE",
                WeatherEntry.CONTENT_ITEM_TYPE, type);
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        assertEquals("Error: the LocationEntry CONTENT_URI should return LocationEntry.CONTENT_TYPE",
                LocationEntry.CONTENT_TYPE, type);
    }

    public void testBasicWeatherQuery() {

        vijayanand.weatherbloc.WeatherDbHelper dbHelper = new vijayanand.weatherbloc.WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = vijayanand.weatherbloc.TestUtilities.createNorthPoleLocationValues();
        long locationRowId = vijayanand.weatherbloc.TestUtilities.insertNorthPoleLocationValues(mContext);
        ContentValues weatherValues = vijayanand.weatherbloc.TestUtilities.createWeatherValues(locationRowId);
        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue("Unable to Insert WeatherEntry into the Database", weatherRowId != -1);
        db.close();

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        vijayanand.weatherbloc.TestUtilities.validateCursor("testBasicWeatherQuery", weatherCursor, weatherValues);
    }

    public void testBasicLocationQueries() {

        vijayanand.weatherbloc.WeatherDbHelper dbHelper = new vijayanand.weatherbloc.WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = vijayanand.weatherbloc.TestUtilities.createNorthPoleLocationValues();
        long locationRowId = vijayanand.weatherbloc.TestUtilities.insertNorthPoleLocationValues(mContext);

        Cursor locationCursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        vijayanand.weatherbloc.TestUtilities.validateCursor("testBasicLocationQueries, location query", locationCursor, testValues);
        // level 19 or greater because getNotificationUri was added in API level 19.
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    locationCursor.getNotificationUri(), LocationEntry.CONTENT_URI);
        }
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertWeatherValues(long locationRowId) {

        long currentTestDate = vijayanand.weatherbloc.TestUtilities.TEST_DATE;
        long millisecondsInADay = 1000 * 60 * 60 * 24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate += millisecondsInADay) {

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_DATE, currentTestDate);
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2 + 0.01 * (float) i);
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3 - 0.01 * (float) i);
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75 + i);
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65 - i);
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5 + 0.2 * (float) i);
            weatherValues.put(vijayanand.weatherbloc.WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);

            returnContentValues[i] = weatherValues;
        }
        return returnContentValues;
    }

}