package com.adamprogrammer.cslendar;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TableLayout mainTable;
    private LayoutInflater inflater;
    Calendar calendar;
    Calendar calendarCountDays;
    String[] days;
    View tr;
    Button btnNext;
    Button btnPrev;
    static int ACTUAL_SERVER_YEAR;
    static int ACTUAL_SERVER_MONTH; // for a moment, i know its messy
    static int ACTUAL_SERVER_DAY;
    static int MONTH_SELECTED_REL;
    static int leftORRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTable = (TableLayout) findViewById(R.id.main_table);
        days = new String[] { "Sun", "Mon", "Tue", "Wed",
                "Thu", "Fri", "Sat" };

        //createCalendar(getDateFromServer());
        getServerData();
    }
    private void getServerData() {
        String url = "http://adampol.scienceontheweb.net/dates.php";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,  new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    ACTUAL_SERVER_YEAR = response.getInt("year");
                    ACTUAL_SERVER_MONTH = response.getInt("month");
                    ACTUAL_SERVER_DAY= response.getInt("day");
                    createCalendar(0);
                } catch (JSONException e) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), e+"", Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", "onErrorResponse: "+error);
            }
        });
        Singleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void createCalendar (int leftOrRight) {
        calendar = Calendar.getInstance(Locale.US);
        calendarCountDays = Calendar.getInstance(Locale.US);
        if (MONTH_SELECTED_REL + leftOrRight == 0) {
            calendar.set(ACTUAL_SERVER_YEAR, ACTUAL_SERVER_MONTH, ACTUAL_SERVER_DAY);
            calendarCountDays.set(ACTUAL_SERVER_YEAR, ACTUAL_SERVER_MONTH, 0);
            MONTH_SELECTED_REL = 0 ;// aktualny
        } else if (leftOrRight == 1) { // do przodu
            MONTH_SELECTED_REL++;
            calendar.set(ACTUAL_SERVER_YEAR, ACTUAL_SERVER_MONTH + MONTH_SELECTED_REL, 1);
            calendarCountDays.set(ACTUAL_SERVER_YEAR, ACTUAL_SERVER_MONTH + MONTH_SELECTED_REL, 0);
        } else { // do tylu
            MONTH_SELECTED_REL--;
            calendar.set(ACTUAL_SERVER_YEAR, ACTUAL_SERVER_MONTH + MONTH_SELECTED_REL, 1);
            calendarCountDays.set(ACTUAL_SERVER_YEAR, ACTUAL_SERVER_MONTH + MONTH_SELECTED_REL, 0);
        }

        for (int i = 0; i < 8; i++) { // rows
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

            row.setLayoutParams(lp);

            for (int x = 0; x < 7; x++) { // columns
                if (i == 0) { // setting month
                    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    tr = inflater.from(this).inflate(R.layout.calendar_header, null);
                    btnNext = (Button) tr.findViewById(R.id.next_month);
                    btnNext.setOnClickListener(this);
                    btnPrev = (Button) tr.findViewById(R.id.prev_month);
                    btnPrev.setOnClickListener(this);
                    TextView tv_cm = (TextView) tr.findViewById(R.id.calendar_month);
                    tv_cm.setText(String.format(Locale.getDefault(), "%tB", calendar));
                    mainTable.addView(tr);
                    break;
                } else if (i == 1) { // setting days of week names
                    Button tv = new Button(this);
                    ViewGroup.LayoutParams lpp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                    lpp.height = 40;
                    tv.setTextColor(Color.BLACK);
                    tv.setLayoutParams(lpp);
                    tv.setPadding(0, 0, 0, 0);
                    tv.setTextSize(10);
                    tv.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.calendar_item));
                    tv.setEnabled(false);
                    //tv.setPadding(5,5,5,5);

                    tv.setText(days[x]);
                    row.addView(tv);
                } else { // settings days of month
                    int counter = 0;
                    switch (i) {
                        case 3:
                            counter = 7;
                            break;
                        case 4:
                            counter = 14;
                            break;
                        case 5:
                            counter = 21;
                            break;
                        case 6:
                            counter = 28;
                            break;
                        case 7:
                            counter = 35;
                            break;
                    }

                    Button tv = new Button(this);
                    tv.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.calendar_item));
                    tv.setEnabled(false);
                    tv.setPadding(0, 0, 0, 0);
                    tv.setTextSize(10);
                    tv.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
                    int[] dayOptions = getDayOptions((x + 1 + counter));
                    if (dayOptions[0] == 1) { // correct day in month
                        tv.setText(String.format(Locale.getDefault(),"%d", dayOptions[2]));
                        if (dayOptions[1] == 0) { // day in the past

                        } else { // day available
                            tv.setEnabled(true);
                            tv.setOnClickListener(this);
                        }

                    } else {
                        tv.setText("");
                    }
                    row.addView(tv);
                }
            }
            mainTable.addView(row);
        }
    }
    private int[] getDayOptions(int day) {
        int color = 0;
        int exists = 1;
        int type = 0; // 0-day in the past, 1- day available, 2- day off, 3- day busy
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int calendarPadding = calendarCountDays.get(Calendar.DAY_OF_WEEK);

        if (day <= calendarPadding) { // setting "calendar padding"

            return new int[] {0, color, day};
        }

        if (day < dayOfMonth+calendarPadding) { // filtering passed days and
            type = 0;
        } else if(day > daysInMonth+calendarPadding) { // dont display those not existing in month
            exists = 0;
        } else { // correct days
            type = 1;
        }
        return new int[] {exists, type, day-calendarPadding};
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.prev_month) { // here a lil bit chaotic, need to organize later
            if (MONTH_SELECTED_REL == 1) {
                mainTable.removeAllViews();
                createCalendar(0);
            } else if (MONTH_SELECTED_REL == 2) {
                mainTable.removeAllViews();
                createCalendar(1);
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Cannot do this", Snackbar.LENGTH_LONG).show();
            }
        } else if (v.getId() == R.id.next_month){
            if (MONTH_SELECTED_REL == 0) {
                mainTable.removeAllViews();
                createCalendar(1);
            } else if (MONTH_SELECTED_REL == 1) {
                mainTable.removeAllViews();
                createCalendar(2);
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Cannot do this", Snackbar.LENGTH_LONG).show();
            }
        } else {
            Button b = (Button) v;
            Snackbar.make(getWindow().getDecorView().getRootView(), "Clicked calendar: "+b.getText().toString(),
                    Snackbar.LENGTH_LONG).show();
            b.setSelected(true);
        }
    }
}
