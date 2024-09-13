package com.example.wordquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Report extends AppCompatActivity {
    sqliteDB db;
    int letters = 0;

    TextView t1;
    TextView t2;
    Button b1;
    Button b2;
    Button b3;
    Button b4;
    Button b5;

    ArrayList<String> anagrams;
    int words;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        t1 = findViewById(R.id.textview6);
        t2 = findViewById(R.id.textview7);
        b1 = findViewById(R.id.button6);
        b2 = findViewById(R.id.button7);
        b3 = findViewById(R.id.button8);
        b4 = findViewById(R.id.button9);
        b5 = findViewById(R.id.button17);

        db = new sqliteDB(Report.this);

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(Report.this, MainActivity.class);
                startActivity(intent2);
                finish();
            }
        });

        getWordLength();
    }

    public void getWordLength()
    {
        LayoutInflater inflater = LayoutInflater.from(Report.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);
        e1.setHint("Enter a value between 2 and 15");

        AlertDialog dialog = new AlertDialog.Builder(Report.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String alphabet = (e1.getText()).toString();
                        letters = alphabet.length() == 0 ? 0 : Integer.parseInt(alphabet);
                        if(letters < 2 || letters > 15)
                        {
                            Toast.makeText(Report.this, "Enter a value between 2 and 15", Toast.LENGTH_LONG).show();
                            getWordLength();
                        }
                        else
                        {
                            start();
                        }
                    }
                }).create();
        dialog.show();
    }

    public void start()
    {
        anagrams = db.getSolvedWords(letters);
        words = anagrams.size();
        counter = db.getPage(letters);

        nextWord();
    }

    public void nextWord()
    {
        b1.setEnabled(true);
        b2.setEnabled(true);
        b5.setEnabled(true);

        if(words > 0) {
            t1.setText("Page " + (counter + 1) + " out of " + (((words - 1) / 100) + 1));
        }
        else {
            t1.setText("Page " + (counter + 1) + " out of 1");
        }
        t2.setText("");

        for(int i = 0; i < 100; i++)
        {
            int position = (counter * 100) + i;
            if(position >= words)
            {
                break;
            }
            String jumble = anagrams.get(position);
            if(i == 0)
            {
                t2.setText((position + 1) + ". " + jumble);
            }
            else
            {
                t2.setText(t2.getText() + "<br>" + (position + 1) + ". " + jumble);
            }
        }

        t2.setText(Html.fromHtml((t2.getText()).toString()));

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(words > 100) {
                    counter--;
                    if (counter < 0) {
                        counter = (words - 1) / 100;
                    }
                    db.updatePage(letters, counter);
                    nextWord();
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(words > 100) {
                    counter++;
                    if (counter == ((words - 1) / 100) + 1) {
                        counter = 0;
                    }
                    db.updatePage(letters, counter);
                    nextWord();
                }
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(Report.this);
                final View yourCustomView = inflater.inflate(R.layout.input, null);

                EditText e2 = yourCustomView.findViewById(R.id.edittext1);
                int maximum = ((words - 1) / 100) + 1;
                e2.setHint("Enter a value between 1 and " + maximum);

                AlertDialog dialog = new AlertDialog.Builder(Report.this)
                        .setTitle("Go to page")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String pages = (e2.getText()).toString();
                                int page = pages.length() == 0 ? 0 : Integer.parseInt(pages);
                                if(page < 1 || page > maximum)
                                {
                                    Toast.makeText(Report.this, "Enter a value between 1 and " + maximum, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    if(words > 100) {
                                        counter = page - 1;
                                        db.updatePage(letters, counter);
                                        nextWord();
                                    }
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }
}