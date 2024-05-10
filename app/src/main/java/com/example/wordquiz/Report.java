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

        db = new sqliteDB(Report.this);

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        getWordLength();
    }

    public void getWordLength()
    {
        LayoutInflater inflater = LayoutInflater.from(Report.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);

        AlertDialog dialog = new AlertDialog.Builder(Report.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        letters = Integer.parseInt((e1.getText()).toString());
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
    }
}