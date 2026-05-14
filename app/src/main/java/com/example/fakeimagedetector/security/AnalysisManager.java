package com.example.fakeimagedetector.security;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AnalysisManager {
    private final DatabaseHelper dbHelper;
    private static final int MAX_ANALYSES = 5;

    public AnalysisManager(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public void addAnalysis(int userId, double probability, String resultText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_PROBABILITY, probability);
        values.put(DatabaseHelper.COLUMN_RESULT_TEXT, resultText);
        db.insert(DatabaseHelper.TABLE_ANALYSES, null, values);

        cleanupOldAnalyses(db, userId);
    }

    private void cleanupOldAnalyses(SQLiteDatabase db, int userId) {
        String query = "SELECT " + DatabaseHelper.COLUMN_ID +
                " FROM " + DatabaseHelper.TABLE_ANALYSES +
                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?" +
                " ORDER BY " + DatabaseHelper.COLUMN_TIMESTAMP + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.getCount() > MAX_ANALYSES) {
            int toDelete = cursor.getCount() - MAX_ANALYSES;
            for (int i = 0; i < toDelete; i++) {
                if (cursor.moveToNext()) {
                    int idToDelete = cursor.getInt(0);
                    db.delete(DatabaseHelper.TABLE_ANALYSES,
                            DatabaseHelper.COLUMN_ID + " = ?",
                            new String[]{String.valueOf(idToDelete)});
                }
            }
        }
        cursor.close();
    }

    public Cursor getHistoryCursor(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_ANALYSES +
                        " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?" +
                        " ORDER BY " + DatabaseHelper.COLUMN_TIMESTAMP + " DESC",
                new String[]{String.valueOf(userId)});
    }
}