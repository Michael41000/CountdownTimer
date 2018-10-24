package com.example.mroll.countdowntimer;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean timerStarted;
    private long timerTimeMillis;
    private CountDownTimer countdownTimer;
    private TextView timerTextView;
    private Button startStopButton;
    private Button resetButton;
    private long endTime;
    private Spinner hourDropdown;
    private Spinner minuteDropdown;
    private Spinner secondDropdown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerStarted = false;

        startStopButton = findViewById(R.id.startStopButton);
        resetButton = findViewById(R.id.resetButton);

        timerTextView = findViewById(R.id.timerTextView);

        timerTextView.setText(convertTime(timerTimeMillis));

        hourDropdown = findViewById(R.id.hourSpinner);
        minuteDropdown = findViewById(R.id.minuteSpinner);
        secondDropdown = findViewById(R.id.secondSpinner);
        Integer[] hourArray = new Integer[25];
        for (int i = 0; i < hourArray.length; i++) {
            hourArray[i] = i;
        }
        Integer[] minuteArray = new Integer[61];
        Integer[] secondArray = new Integer[61];
        for (int i = 0; i < minuteArray.length; i++)
        {
            minuteArray[i] = i;
            secondArray[i] = i;
        }

        ArrayAdapter<Integer> hourAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, hourArray);
        ArrayAdapter<Integer> minuteAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, minuteArray);
        ArrayAdapter<Integer> secondAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, secondArray);

        hourDropdown.setAdapter(hourAdapter);
        minuteDropdown.setAdapter(minuteAdapter);
        secondDropdown.setAdapter(secondAdapter);

        minuteDropdown.setSelection(10);

        hourDropdown.setOnItemSelectedListener(getSpinnerTime);
        minuteDropdown.setOnItemSelectedListener(getSpinnerTime);
        secondDropdown.setOnItemSelectedListener(getSpinnerTime);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerStarted)
                {
                    pauseTimer();
                }
                else
                {
                    startTimer();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerStarted)
                {
                    pauseTimer();
                }

                Integer hour = (Integer) hourDropdown.getSelectedItem();
                Integer minute = (Integer) minuteDropdown.getSelectedItem();
                Integer second = (Integer) secondDropdown.getSelectedItem();

                timerTimeMillis = second * 1000;
                timerTimeMillis += minute * (60 * 1000);
                timerTimeMillis += hour * (60 * 60 * 1000);
                timerTextView.setText(convertTime(timerTimeMillis));
                startStopButton.setEnabled(true);
                endTime = 0;
            }
        });
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timerTimeMillis;

        countdownTimer = new CountDownTimer(timerTimeMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timerTimeMillis = millisUntilFinished;
                timerTextView.setText(convertTime(timerTimeMillis));
            }

            public void onFinish() {
                timerTimeMillis = 0;
                timerTextView.setText(convertTime(timerTimeMillis));
                timerStarted = false;
                startStopButton.setText("Start");
                startStopButton.setEnabled(false);
            }
        }.start();

        timerStarted = true;
        startStopButton.setText("Pause");
    }

    private void pauseTimer() {
        countdownTimer.cancel();
        endTime = 0;
        timerStarted = false;
        startStopButton.setText("Start");
    }

    private String convertTime(long timeInMills) {
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        // Get the hours out
        while (timeInMills >= 3600000) {
            hours++;
            timeInMills -= 3600000;
        }
        // Get the minutes out
        while (timeInMills >= 60000) {
            minutes++;
            timeInMills -= 60000;
        }
        // Get the seconds out
        while (timeInMills >= 1000)
        {
            seconds++;
            timeInMills -= 1000;
        }
        if (hours > 0)
        {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        else
        {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private AdapterView.OnItemSelectedListener getSpinnerTime = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!timerStarted) {
                Integer hour = (Integer) hourDropdown.getSelectedItem();
                Integer minute = (Integer) minuteDropdown.getSelectedItem();
                Integer second = (Integer) secondDropdown.getSelectedItem();

                timerTimeMillis = second * 1000;
                timerTimeMillis += minute * (60 * 1000);
                timerTimeMillis += hour * (60 * 60 * 1000);
                timerTextView.setText(convertTime(timerTimeMillis));
            }

            if (timerTimeMillis == 0)
            {
                startStopButton.setEnabled(false);
            }
            else
            {
                startStopButton.setEnabled(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("timerTimeMillis", timerTimeMillis);
        editor.putBoolean("timerStarted", timerStarted);
        editor.putLong("endTime", endTime);

        editor.apply();

        if (countdownTimer != null)
        {
            countdownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        timerTimeMillis = prefs.getLong("timerTimeMillis", 0);
        timerStarted = prefs.getBoolean("timerStarted", false);
        timerTextView.setText(convertTime(timerTimeMillis));

        if (timerStarted)
        {
            endTime = prefs.getLong("endTime", 0);
            timerTimeMillis = endTime - System.currentTimeMillis();
            if (timerTimeMillis < 0)
            {
                timerTimeMillis = 0;
                timerTextView.setText(convertTime(timerTimeMillis));
                timerStarted = false;
                startStopButton.setText("Start");
                startStopButton.setEnabled(false);
            }
            else
            {
                startTimer();
            }
        }
    }
}
