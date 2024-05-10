package com.example.wordquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class sqliteDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CSW2021.db";
    public Context con;

    public sqliteDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
        // TODO Auto-generated constructor stub
        con = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table words(word text, length integer, anagram text, definition text, probability real, time real, solved integer, front text, back text)"
        );
        db.execSQL(
                "create table scores(length integer, score integer, counter integer, page integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS words");
        db.execSQL("DROP TABLE IF EXISTS scores");
        onCreate(db);
    }

    public boolean prepareScore()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        for(int i = 2; i <= 15; i++) {
            contentValues.put("length", i);
            contentValues.put("score", 0);
            contentValues.put("counter", 0);
            contentValues.put("page", 0);

            db.insert("scores", null, contentValues);
        }
        return true;
    }

    public boolean insertWord(String word, int length, String anagram, String definition, double probability, String front, String back)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("word", word);
        contentValues.put("length", length);
        contentValues.put("anagram", anagram);
        contentValues.put("definition", definition);
        contentValues.put("probability", probability);
        contentValues.put("time", 0);
        contentValues.put("solved", 0);
        contentValues.put("front", front);
        contentValues.put("back", back);

        db.insert("words", null, contentValues);
        return true;
    }

    public int getScore(int letters)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT score FROM scores WHERE length = " + letters, null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public int getCounter(int letters)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT counter FROM scores WHERE length = " + letters, null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public ArrayList<String> getAllAnagrams(int letters)
    {
        ArrayList<String> anagramList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT(anagram) FROM words WHERE length = " + letters + " ORDER BY probability DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);

                anagramList.add(data);
            } while (cursor.moveToNext());
        }
        return anagramList;
    }

    public HashMap<String, Boolean> getSolvedStatus(int letters)
    {
        HashMap<String, Boolean> statusList = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT anagram, MIN(solved) FROM words WHERE length = " + letters + " GROUP BY anagram", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                int status = Integer.parseInt(cursor.getString(1));

                if(status == 1) {
                    statusList.put(data, true);
                } else {
                    statusList.put(data, false);
                }
            } while (cursor.moveToNext());
        }
        return statusList;
    }

    public ArrayList<String> getSolvedWords(int letters)
    {
        ArrayList<String> wordList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word, definition, time, front, back FROM words WHERE length = " + letters + " AND solved = 1 ORDER BY time DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                String definition = cursor.getString(1);
                String time = cursor.getString(2);
                String front = cursor.getString(3);
                String back = cursor.getString(4);

                wordList.add("<b><small>" + back + "</small> " + data + " <small>" + front + "</small></b> " + definition + " (" + time + " seconds)");
            } while (cursor.moveToNext());
        }
        return wordList;
    }

    public ArrayList<String> getUnsolvedAnswers(String jumble)
    {
        ArrayList<String> answerList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word FROM words WHERE anagram = \"" + jumble + "\" AND solved = 0", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);

                answerList.add(data);
            } while (cursor.moveToNext());
        }
        return answerList;
    }

    public ArrayList<String> getSolvedAnswers(String jumble)
    {
        ArrayList<String> solvedList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word FROM words WHERE anagram = \"" + jumble + "\" AND solved = 1 ORDER BY time", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);

                solvedList.add(data);
            } while (cursor.moveToNext());
        }
        return solvedList;
    }

    public ArrayList<String> getDefinition(String guess)
    {
        ArrayList<String> hookList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT definition, front, back FROM words WHERE word = \"" + guess + "\"", null);

        String meaning = null;
        String front = null;
        String back = null;

        if (cursor.moveToFirst()) {
            do {
                meaning = cursor.getString(0);
                front = cursor.getString(1);
                back = cursor.getString(2);
            } while (cursor.moveToNext());
        }

        hookList.add(meaning);
        hookList.add(front);
        hookList.add(back);

        return hookList;
    }

    public int getPage(int letters)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT page FROM scores WHERE length = " + letters, null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public int updateScore(int letters, int score) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("score", score);

        return db.update("scores", values, "length = ?",
                new String[] {Integer.toString(letters)});
    }

    public int updateCounter(int letters, int counter) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("counter", counter);

        return db.update("scores", values, "length = ?",
                new String[] {Integer.toString(letters)});
    }

    public int updatePage(int letters, int counter) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("page", counter);

        return db.update("scores", values, "length = ?",
                new String[] {Integer.toString(letters)});
    }

    public int updateTime(ArrayList<String> guesses, double time, int solved) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("solved", solved);

        String guess = (((guesses.toString()).replace("[", "(\"")).replace("]", "\")")).replace(", ", "\", \"");
        return db.update("words", values, "word IN " + guess,
                new String[] {});
    }

    public double getTime(String guess)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT time FROM words WHERE word = \"" + guess + "\"", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Double.parseDouble(data);
    }

    public int getNumber(int letters)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM words WHERE length = " + letters, null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
}