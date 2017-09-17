package com.adamprogrammer.cslendar;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    static int DATE_ACTUAL_SERVER_YEAR;
    static int DATE_ACTUAL_SERVER_MONTH; // for a moment, i know its messy
    static int DATE_ACTUAL_SERVER_DAY;
    static int DATE_MONTH_SELECTED;
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
                    DATE_ACTUAL_SERVER_YEAR = response.getInt("year");
                    DATE_ACTUAL_SERVER_MONTH = response.getInt("month");
                    DATE_ACTUAL_SERVER_DAY= response.getInt("day");
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

    private void createCalendar (int future) {
        calendar = Calendar.getInstance(Locale.US);
        calendarCountDays = Calendar.getInstance(Locale.US);
        if (future == 1) {
            calendar.set(DATE_ACTUAL_SERVER_YEAR, DATE_ACTUAL_SERVER_MONTH+1, 1);
            calendarCountDays.set(DATE_ACTUAL_SERVER_YEAR, DATE_ACTUAL_SERVER_MONTH+1, 0);
            DATE_MONTH_SELECTED = 1;
        } else if (future == 2) {
            calendar.set(DATE_ACTUAL_SERVER_YEAR, DATE_ACTUAL_SERVER_MONTH+2, 1);
            calendarCountDays.set(DATE_ACTUAL_SERVER_YEAR, DATE_ACTUAL_SERVER_MONTH+2, 0);
            DATE_MONTH_SELECTED = 2;
        } else {
            calendar.set(DATE_ACTUAL_SERVER_YEAR, DATE_ACTUAL_SERVER_MONTH, DATE_ACTUAL_SERVER_DAY);
            calendarCountDays.set(DATE_ACTUAL_SERVER_YEAR, DATE_ACTUAL_SERVER_MONTH, 0);
            DATE_MONTH_SELECTED = 0;
        }

        for (int i=0; i<8; i++) { // rows
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT);

            row.setLayoutParams(lp);
            row.setBackgroundColor(Color.LTGRAY);

            for (int x=0; x<7; x++) { // columns
                if (i==0)  { // setting month
                    inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    tr = inflater.from(this).inflate(R.layout.calendar_header, null);
                    btnNext = (Button) tr.findViewById(R.id.next_month);
                    btnNext.setOnClickListener(this);
                    btnPrev = (Button) tr.findViewById(R.id.prev_month);
                    btnPrev.setOnClickListener(this);
                    TextView tv_cm = (TextView) tr.findViewById(R.id.calendar_month);
                    tv_cm.setText(calendar.get(Calendar.MONTH)+", day:"+calendar.get(Calendar.DAY_OF_MONTH)
                    +", year: "+calendar.get(Calendar.YEAR));
                    mainTable.addView(tr);
                    break;
                } else if (i==1) { // setting days of week names
                    TextView tv = new TextView(this);
                    ViewGroup.LayoutParams lpp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                            0, 1f);
                    row.setLayoutParams(lpp);
                    tv.setPadding(5,5,5,5);
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

                    TextView tv = new TextView(this);
                    ViewGroup.LayoutParams lpp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                            0, 1f);
                    row.setLayoutParams(lpp);
                    tv.setPadding(5,5,5,5);
                    int[] dayOptions = setDayOptions((x+1+counter));
                    if (dayOptions[0] == 1) { // correct day
                        tv.setFocusable(true);
                        tv.setClickable(true);
                        tv.setEnabled(true);
                        //tv.setBackground(ContextCompat.getDrawable(this, R.drawable.calendar_item));
                        //tv.setBackgroundResource(R.drawable.calendar_item);
                        tv.setBackgroundColor(dayOptions[1]);
                        tv.setText(dayOptions[2]+"");
                    } else {
                        tv.setText("");
                    }
                    row.addView(tv);
                }
            }
            mainTable.addView(row);
        }
    }
    private int[] setDayOptions(int day) {
        int color = 0;
        int exists = 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int calendarPadding = calendarCountDays.get(Calendar.DAY_OF_WEEK);

        if (day <= calendarPadding) { // setting "calendar padding"

            return new int[] {0, color, day};
        }

        if (day < dayOfMonth+calendarPadding) { // filtering passed days and
            color = Color.LTGRAY;
        } else if(day > daysInMonth+calendarPadding) { // dont display those not existing in month
            exists = 0;
        } else { // correct days
            color = Color.YELLOW;
        }
        return new int[] {exists, color, day-calendarPadding};
    }

    private int[] getDateFromServer() {

        return new int[] {2017,8,17};
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.prev_month) {

            if (DATE_MONTH_SELECTED == 1) {
                mainTable.removeAllViews();
                createCalendar(0);
            } else if (DATE_MONTH_SELECTED == 2) {
                mainTable.removeAllViews();
                createCalendar(1);
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Cannot do this", Snackbar.LENGTH_LONG).show();
            }

        } else {
            if (DATE_MONTH_SELECTED == 0) {
                mainTable.removeAllViews();
                createCalendar(1);
            } else if (DATE_MONTH_SELECTED == 1) {
                mainTable.removeAllViews();
                createCalendar(2);
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Cannot do this", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
