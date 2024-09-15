package com.example.wordquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    sqliteDB db;
    int letters = 0;
    HashMap<String, String> dictionary;

    TextView t1;
    TextView t2;
    TextView t3;
    TextView t4;
    TextView t5;
    TextView t9;
    EditText e2;
    Button b1;
    Button b2;
    Button b3;
    Button b4;
    Button b5;
    Button b6;
    Button b7;
    Button b8;
    Button b9;
    Button b10;
    Button b11;
    Button b12;
    Button b13;

    ArrayList<String> anagrams;
    HashMap<String, Boolean> solvedStatus;
    int words;
    int score;
    int counter;
    int number;
    boolean show = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1 = findViewById(R.id.textview1);
        t2 = findViewById(R.id.textview2);
        t3 = findViewById(R.id.textview3);
        t4 = findViewById(R.id.textview4);
        t5 = findViewById(R.id.textview5);
        t9 = findViewById(R.id.textview9);
        e2 = findViewById(R.id.edittext2);
        b1 = findViewById(R.id.button1);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        b5 = findViewById(R.id.button5);
        b6 = findViewById(R.id.button10);
        b7 = findViewById(R.id.button11);
        b8 = findViewById(R.id.button13);
        b9 = findViewById(R.id.button12);
        b10 = findViewById(R.id.button14);
        b11 = findViewById(R.id.button15);
        b12 = findViewById(R.id.button16);
        b13 = findViewById(R.id.button18);

        db = new sqliteDB(MainActivity.this);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        boolean prepared = pref.getBoolean("prepared", false);

        if(prepared) {
            getWordLength();
        } else {
            Toast.makeText(MainActivity.this, "Please give 1 hour to prepare database of dictionary words. Only when opening this mobile app for the first time.", Toast.LENGTH_LONG).show();
            db.prepareScore();
            prepareDictionary();
        }

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(MainActivity.this, Report.class);
                startActivity(intent1);
                finish();
            }
        });

        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show = !show;
                if(show)
                {
                    b8.setText("Keep Hiding Answers");
                }
                else
                {
                    b8.setText("Keep Showing Answers");
                }
            }
        });

        b13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                e2.setText("");
            }
        });
    }

    public void prepareDictionary()
    {
        dictionary = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("CSW2021.txt"), "UTF-8"));
            while(true)
            {
                String s = reader.readLine();
                if(s == null)
                {
                    break;
                }
                else
                {
                    String t[] = s.split("=");
                    dictionary.put(t[0], t[1]);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        prepareDatabase();
    }

    public void prepareDatabase()
    {
        Iterator<Map.Entry<String, String>> itr = dictionary.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String word = entry.getKey();
            char c[] = word.toCharArray();
            Arrays.sort(c);
            String anagram = new String(c);
            String definition = entry.getValue();
            StringBuilder back = new StringBuilder();
            StringBuilder front = new StringBuilder();
            for(char letter = 'A'; letter <= 'Z'; letter++)
            {
                if(dictionary.containsKey(word + letter))
                {
                    back.append(letter);
                }
                if(dictionary.containsKey(letter + word))
                {
                    front.append(letter);
                }
            }
            boolean q = db.insertWord(word, word.length(), anagram, definition, probability(word), new String(back), new String(front));
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("prepared", true);
        editor.commit();

        getWordLength();
    }

    public void getWordLength()
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);
        e1.setHint("Enter a value between 2 and 15");

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String alphabet = (e1.getText()).toString();
                        letters = alphabet.length() == 0 ? 0 : Integer.parseInt(alphabet);
                        if(letters < 2 || letters > 15)
                        {
                            Toast.makeText(MainActivity.this, "Enter a value between 2 and 15", Toast.LENGTH_LONG).show();
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

    public void wordLength(long begin, double delay, ArrayList<String> answers)
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);
        e1.setHint("Enter a value between 2 and 15");

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        letters = Integer.parseInt((e1.getText()).toString());
                        if(letters < 2 || letters > 15)
                        {
                            Toast.makeText(MainActivity.this, "Enter a value between 2 and 15", Toast.LENGTH_LONG).show();
                            wordLength(begin, delay, answers);
                        }
                        else
                        {
                            cumulativeTime(begin, delay, answers);
                            start();
                        }
                    }
                }).create();
        dialog.show();
    }

    public void start()
    {
        anagrams = db.getAllAnagrams(letters);
        solvedStatus = db.getSolvedStatus(letters);
        words = anagrams.size();
        score = db.getScore(letters);
        counter = db.getCounter(letters);
        number = db.getNumber(letters);

        nextWord();
    }

    public void nextWord()
    {
        String jumble = anagrams.get(counter);
        ArrayList<String> answers = db.getUnsolvedAnswers(jumble);
        ArrayList<String> solved = db.getSolvedAnswers(jumble);
        int total = answers.size() + solved.size();
        long begin = System.currentTimeMillis();
        final double[] delay = {answers.size() == 0 ? 0 : db.getTime(answers.get(0))};

        ArrayList<String> replies = new ArrayList<>();
        replies.addAll(answers);
        replies.addAll(solved);

        b1.setEnabled(true);
        b2.setEnabled(true);
        b4.setEnabled(true);
        b6.setEnabled(true);
        b7.setEnabled(true);
        b9.setEnabled(true);
        b10.setEnabled(true);
        b11.setEnabled(true);
        b12.setEnabled(true);

        t1.setText("Word " + (counter + 1) + " out of " + words);
        t2.setText("Number of answers: " + total);
        t3.setText(jumble);
        t4.setText("Score: " + score + "/" + number);
        t5.setText("");
        if(show) {
            t9.setText(replies.toString());
        }
        e2.setText("");

        final String[] amount = {new String()};

        for(int k = 0; k < solved.size(); k++)
        {
            String sum = solved.get(k);
            ArrayList<String> hooks = db.getDefinition(sum);
            String overall = hooks.get(0);
            String ahead = hooks.get(1);
            String behind = hooks.get(2);

            if(k == 0)
            {
                amount[0] += ((k + 1) + ". <b><small>" + behind + "</small> " + sum + " <small>" + ahead + "</small></b> " + overall);
            }
            else
            {
                amount[0] += ("<br>" + (k + 1) + ". <b><small>" + behind + "</small> " + sum + " <small>" + ahead + "</small></b> " + overall);
            }

            t5.setText(Html.fromHtml(amount[0]));
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String guess = (((e2.getText()).toString()).trim()).toUpperCase();
                if(answers.contains(guess))
                {
                    long stop = System.currentTimeMillis();
                    double time = stop - begin;
                    time /= 1000;
                    time += delay[0];
                    ArrayList<String> guesses = new ArrayList<>();
                    guesses.add(guess);
                    db.updateTime(guesses, time, 1);
                    ArrayList<String> hook = db.getDefinition(guess);
                    String meaning = hook.get(0);
                    String back = hook.get(1);
                    String front = hook.get(2);
                    if(solved.size() == 0)
                    {
                        amount[0] += ((solved.size() + 1) + ". <b><small>" + front + "</small> " + guess + " <small>" + back + "</small></b> " + meaning);
                    }
                    else
                    {
                        amount[0] += ("<br>" + (solved.size() + 1) + ". <b><small>" + front + "</small> " + guess + " <small>" + back + "</small></b> " + meaning);
                    }
                    t5.setText(Html.fromHtml(amount[0]));
                    answers.remove(guess);
                    solved.add(guess);
                    score++;
                    db.updateScore(letters, score);
                    if(answers.size() == 0) {
                        solvedStatus.put(jumble, true);
                    }
                    t4.setText("Score: " + score + "/" + number);
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(counter == words - 1) {
                    counter = 0;
                }
                else {
                    counter++;
                }
                db.updateCounter(letters, counter);
                cumulativeTime(begin, delay[0], answers);
                nextWord();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wordLength(begin, delay[0], answers);
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(counter == 0) {
                    counter = words - 1;
                }
                else {
                    counter--;
                }
                db.updateCounter(letters, counter);
                cumulativeTime(begin, delay[0], answers);
                nextWord();
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cumulativeTime(begin, delay[0], answers);
                Intent intent1 = new Intent(MainActivity.this, Report.class);
                startActivity(intent1);
                finish();
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (score < number) {
                    do {
                        if (counter == 0) {
                            counter = words - 1;
                        } else {
                            counter--;
                        }
                    } while(solvedStatus.get(anagrams.get(counter)));
                    db.updateCounter(letters, counter);
                    cumulativeTime(begin, delay[0], answers);
                    nextWord();
                }
            }
        });

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (score < number) {
                    do {
                        if (counter == words - 1) {
                            counter = 0;
                        } else {
                            counter++;
                        }
                    } while(solvedStatus.get(anagrams.get(counter)));
                    db.updateCounter(letters, counter);
                    cumulativeTime(begin, delay[0], answers);
                    nextWord();
                }
            }
        });

        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delay[0] += 10;
            }
        });

        b10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.updateTime(solved, 0, 0);
                t5.setText("");
                amount[0] = new String();
                score -= solved.size();
                db.updateScore(letters, score);
                if(answers.size() == 0) {
                    solvedStatus.put(jumble, false);
                }
                t4.setText("Score: " + score + "/" + number);
                answers.addAll(solved);
                solved.clear();
            }
        });

        b11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cumulativeTime(begin, delay[0], answers);
                finish();
            }
        });

        b12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.input, null);

                EditText e3 = yourCustomView.findViewById(R.id.edittext1);
                e3.setHint("Enter a value between 1 and " + words);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Go to page")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String pages = (e3.getText()).toString();
                                int page = pages.length() == 0 ? 0 : Integer.parseInt(pages);
                                if(page < 1 || page > words)
                                {
                                    Toast.makeText(MainActivity.this, "Enter a value between 1 and " + words, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    counter = page - 1;
                                    db.updateCounter(letters, counter);
                                    cumulativeTime(begin, delay[0], answers);
                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    public void cumulativeTime(long begin, double delay, ArrayList<String> answers)
    {
        long stop = System.currentTimeMillis();
        double time = stop - begin;
        time /= 1000;
        time += delay;
        db.updateTime(answers, time, 0);
    }

    public double probability(String st)
    {
        int frequency[] = new int[] {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
        int count = 100;
        double chance = 1;
        for(int j = 0; j < st.length(); j++)
        {
            char ch = st.charAt(j);
            int ord = ((int) ch) - 65;
            chance *= frequency[ord];
            chance /= count;
            if(frequency[ord] > 0) {
                frequency[ord]--;
            }
            count--;
        }
        return chance;
    }
}