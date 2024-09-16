package com.example.wordquiz;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
                "create table words(word text, length integer, anagram text, definition text, probability real, time real, solved integer, back text, front text)"
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

    public ArrayList<String> getTableNames()
    {
        ArrayList<String> tableList = new ArrayList<>();
        int idx = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);

                if(idx > 0) {
                    tableList.add(data);
                }
                idx++;
            } while(cursor.moveToNext());
        }
        return tableList;
    }

    public void exportDB(Context situation)
    {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        ArrayList<String> tables = getTableNames();
        for(String table : tables)
        {
            File file = new File(exportDir, "Android/data/com.example.wordquiz/files/" + table + ".csv");
            try
            {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = this.getReadableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM " + table,null);
                String columnsList[] = curCSV.getColumnNames();
                csvWrite.writeNext(columnsList);
                while(curCSV.moveToNext())
                {
                    String arrStr[] = new String[columnsList.length];
                    for(int index = 0; index < columnsList.length; index++)
                    {
                        arrStr[index] = curCSV.getString(index);
                    }
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
                alertBox("Export CSV", "Export CSV complete.", situation);
            }
            catch(Exception sqlEx)
            {
                alertBox("Export CSV", sqlEx.toString(), situation);
            }
        }
    }

    public void importDB(Context situation)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        String path = "Android/data/com.example.wordquiz/files/words.csv";
        String database = "words";

        LayoutInflater inflater = LayoutInflater.from(situation);
        final View yourCustomView = inflater.inflate(R.layout.path, null);

        TextView t3 = yourCustomView.findViewById(R.id.textview10);
        EditText e2 = yourCustomView.findViewById(R.id.edittext3);
        EditText e3 = yourCustomView.findViewById(R.id.edittext4);

        t3.setText(exportDir.toString() + "/");
        e2.setText(path);
        e3.setText(database);

        AlertDialog dialog = new AlertDialog.Builder(situation)
                .setTitle("File name")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String databaseName = (e3.getText()).toString();
                        ArrayList<String> databases = getTableNames();
                        if(databases.contains(databaseName))
                        {
                            File file = new File(exportDir, (e2.getText()).toString());
                            try
                            {
                                CSVReader csvRead = new CSVReader(new FileReader(file));
                                try {
                                    String columns[] = csvRead.readNext();
                                    String nextLine[] = csvRead.readNext();
                                    do {
                                        ContentValues contentValues = new ContentValues();
                                        for(int column = 0; column < columns.length; column++) {
                                            contentValues.put(columns[column], nextLine[column]);
                                        }
                                        db.insert(databaseName, null, contentValues);
                                        nextLine = csvRead.readNext();
                                    } while (nextLine != null);
                                    csvRead.close();
                                    alertBox("Import CSV", "Import CSV complete.", situation);
                                }
                                catch(IOException e)
                                {
                                    alertBox("Import CSV", e.toString(), situation);
                                }
                            }
                            catch(FileNotFoundException e)
                            {
                                alertBox("Import CSV", e.toString(), situation);
                            }
                        }
                        else
                        {
                            alertBox("Import CSV", "Table not found. Create a new table with the name '" + databaseName + "' first.", situation);
                        }
                    }
                }).create();
        dialog.show();
    }

    public void exportLabels(Context situation)
    {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "Android/data/com.example.wordquiz/files/labels.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT word, solved, time FROM words WHERE time > 0",null);
            String columnsList[] = curCSV.getColumnNames();
            csvWrite.writeNext(columnsList);
            while(curCSV.moveToNext())
            {
                String arrStr[] = new String[columnsList.length];
                for(int index = 0; index < columnsList.length; index++)
                {
                    arrStr[index] = curCSV.getString(index);
                }
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            alertBox("Export labels", "Export labels complete.", situation);
        }
        catch(Exception sqlEx)
        {
            alertBox("Export labels", sqlEx.toString(), situation);
        }
    }

    public void importLabels(Context situation)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        String path = "Android/data/com.example.wordquiz/files/labels.csv";

        LayoutInflater inflater = LayoutInflater.from(situation);
        final View yourCustomView = inflater.inflate(R.layout.message, null);

        TextView t2 = yourCustomView.findViewById(R.id.textview11);
        EditText e1 = yourCustomView.findViewById(R.id.edittext5);

        t2.setText(exportDir.toString() + "/");
        e1.setText(path);

        AlertDialog dialog = new AlertDialog.Builder(situation)
                .setTitle("File name")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File file = new File(exportDir, (e1.getText()).toString());
                        try
                        {
                            CSVReader csvRead = new CSVReader(new FileReader(file));
                            try {
                                String columns[] = csvRead.readNext();
                                String nextLine[] = csvRead.readNext();
                                do {
                                    ContentValues contentValues = new ContentValues();
                                    int wordIndex = 0;
                                    for(int column = 0; column < columns.length; column++) {
                                        if(columns[column].equals("word"))
                                        {
                                            wordIndex = column;
                                        }
                                        else
                                        {
                                            contentValues.put(columns[column], nextLine[column]);
                                        }
                                    }
                                    db.update("words", contentValues, "word = ?",
                                            new String[] {columns[wordIndex]});
                                    nextLine = csvRead.readNext();
                                } while (nextLine != null);
                                csvRead.close();
                                alertBox("Import labels", "Import labels complete.", situation);
                            }
                            catch(IOException e)
                            {
                                alertBox("Import labels", e.toString(), situation);
                            }
                        }
                        catch(FileNotFoundException e)
                        {
                            alertBox("Import labels", e.toString(), situation);
                        }
                    }
                }).create();
        dialog.show();
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

    public boolean insertWord(String word, int length, String anagram, String definition, double probability, String back, String front)
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
        contentValues.put("back", back);
        contentValues.put("front", front);

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
        Cursor cursor = db.rawQuery("SELECT word, definition, time, back, front FROM words WHERE length = " + letters + " AND solved = 1 ORDER BY time DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                String definition = cursor.getString(1);
                String time = cursor.getString(2);
                String back = cursor.getString(3);
                String front = cursor.getString(4);

                wordList.add("<b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + definition + " (" + time + " seconds)");
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
        Cursor cursor = db.rawQuery("SELECT definition, back, front FROM words WHERE word = \"" + guess + "\"", null);

        String meaning = null;
        String back = null;
        String front = null;

        if (cursor.moveToFirst()) {
            do {
                meaning = cursor.getString(0);
                back = cursor.getString(1);
                front = cursor.getString(2);
            } while (cursor.moveToNext());
        }

        hookList.add(meaning);
        hookList.add(back);
        hookList.add(front);

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

    public void alertBox(String title, String message, Context location)
    {
        LayoutInflater inflater = LayoutInflater.from(location);
        final View yourCustomView = inflater.inflate(R.layout.display, null);

        TextView t1 = yourCustomView.findViewById(R.id.textview8);
        t1.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(location)
            .setTitle(title)
            .setView(yourCustomView)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).create();
        dialog.show();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
}