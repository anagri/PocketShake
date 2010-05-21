package com.barefoot.pocketshake.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.barefoot.pocketshake.R;
import com.barefoot.pocketshake.data.EarthQuake;
import com.barefoot.pocketshake.exceptions.InvalidFeedException;

public class EarthQuakeDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "PocketShake";
	private static final int DATABASE_VERSION = 1;
	private static final String LOG_TAG = "EarthQuakeDatabase";
	private final Context mContext;

	public EarthQuakeDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
	}

	public static class EarthquakeCursor extends SQLiteCursor {
		/** The query for this cursor */
		private static final String QUERY = "SELECT _id, location, intensity, longitude, latitude, date, time "
				+ "FROM earthquakes " + "ORDER BY date";

		/** Cursor constructor */
		private EarthquakeCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
				String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		/** Private factory class necessary for rawQueryWithFactory() call */
		private static class Factory implements SQLiteDatabase.CursorFactory {
			@Override
			public Cursor newCursor(SQLiteDatabase db,
					SQLiteCursorDriver driver, String editTable,
					SQLiteQuery query) {
				return new EarthquakeCursor(db, driver, editTable, query);
			}
		}

		// accessor methods for each column.
		public String getEarthquakeId() {
			return getString(getColumnIndexOrThrow("_id"));
		}

		public String getLocation() {
			return getString(getColumnIndexOrThrow("location"));
		}

		public String getIntensity() {
			return getString(getColumnIndexOrThrow("intensity"));
		}

		public String getLongitude() {
			return getString(getColumnIndexOrThrow("longitude"));
		}

		public String getLatitude() {
			return getString(getColumnIndexOrThrow("latitude"));
		}

		public String getDate() {
			return getString(getColumnIndexOrThrow("date"));
		}

		public String getTime() {
			return getString(getColumnIndexOrThrow("time"));
		}

		public EarthQuake getEarthQuake() {
			EarthQuake earthquake = null;
			try {
				earthquake = new EarthQuake(
						getString(getColumnIndexOrThrow("_id")), "M"
								+ getString(getColumnIndexOrThrow("intensity"))
								+ ", "
								+ getString(getColumnIndexOrThrow("location")),
						getString(getColumnIndexOrThrow("longitude")) + " "
								+ getString(getColumnIndexOrThrow("latitude")),
						getString(getColumnIndexOrThrow("date")) + "T"
								+ getString(getColumnIndexOrThrow("time"))
								+ "Z");
			} catch (InvalidFeedException e) {
				Log.e("Trying to return earthquake object", e.getMessage());
			}
			return earthquake;
		}
	}

	private void executeManySqlStatements(SQLiteDatabase db, String[] sqls) {
		for (String eachSql : sqls) {
			if (eachSql.trim().length() > 0)
				db.execSQL(eachSql);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String[] createSqls = mContext.getString(R.string.create_db_sql).split(
				"\n");
		db.beginTransaction();
		try {
			executeManySqlStatements(db, createSqls);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("Error creating tables and debug data", e.toString());
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("EarthQuakeDBUpgrade", "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");

		String[] createSqls = mContext.getString(R.string.create_db_sql).split(
				"\n");
		db.beginTransaction();
		try {
			executeManySqlStatements(db, createSqls);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("Error creating tables and debug data", e.toString());
		} finally {
			db.endTransaction();
		}

		// recreate database from scratch once again.
		onCreate(db);
	}

	public EarthquakeCursor getEarthquakes() {
		Log.i(LOG_TAG, "Fetching earthquakes from database");
		SQLiteDatabase db = getReadableDatabase();

		return (EarthquakeCursor) db.rawQueryWithFactory(
				new EarthquakeCursor.Factory(), EarthquakeCursor.QUERY, null,
				null);
	}

	public void saveNewEarthquakesOnly(EarthQuake[] earthqaukeFeed) {
		for (EarthQuake eachEarthQuake : earthqaukeFeed) {
			if (!exists(eachEarthQuake)) {
				create(eachEarthQuake);
			}
		}
	}

	protected void create(EarthQuake eachEarthQuake) {
		if (eachEarthQuake != null) {
			try {
				getWritableDatabase().execSQL(getInsertQuery(eachEarthQuake));
			} catch (SQLException e) {
				Log.e("Creating new earthquake", e.getMessage());
			}
		}

	}

	private String getInsertQuery(EarthQuake eachEarthQuake) {
		StringBuffer insertQuery = new StringBuffer(
				"Insert into earthquakes (intensity, location, longitude, latitude, time, date, _id) values (");
		insertQuery.append(databaseValue(eachEarthQuake.getIntensity()));
		insertQuery.append(databaseValue(eachEarthQuake.getLocation()));
		insertQuery.append(databaseValue(eachEarthQuake.getLongitude()));
		insertQuery.append(databaseValue(eachEarthQuake.getLatitude()));
		insertQuery.append(databaseValue(eachEarthQuake.getTime()));
		insertQuery.append(databaseValue(eachEarthQuake.getDate()));
		insertQuery.append("'" + eachEarthQuake.getId() + "'");
		insertQuery.append(")");

		return insertQuery.toString();
	}

	private String databaseValue(String value) {
		return value == null ? "null, " : "'" + value + "', ";
	}

	public boolean exists(EarthQuake eachEarthQuake) {
		if (eachEarthQuake != null) {
			Cursor c = null;
			String count_query = "Select count(*) from earthquakes where _id = ?";
			try {
				c = getReadableDatabase().rawQuery(count_query,
						new String[] { eachEarthQuake.getId() });
				if (c != null && c.moveToFirst() && c.getInt(0) > 0)
					return true;

			} catch (Exception e) {
				Log.e("Running Count Query", e.getMessage());
			} finally {
				if (c != null) {
					try {
						c.close();
					} catch (SQLException e) {
					}
				}
			}
		}
		return false;
	}
}
