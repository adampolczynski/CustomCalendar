package com.adamprogrammer.cslendar;

import android.content.Context;
import android.graphics.Color;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TableLayout mainTable;
    private LayoutInflater inflater;
    Calendar calendar;
    String[] days;
    Calendar calendarTemp;
    View tr;
    Button btnNext;
    Button btnPrev;

    final int DATE_ACTUAL_SERVER_MONTH = 8; // for a moment, i know its messy
    final int DATE_ACTUAL_SERVER_DAY = 17;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTable = (TableLayout) findViewById(R.id.main_table);
        days = new String[] { "Sun", "Mon", "Tue", "Wed",
                "Thu", "Fri", "Sat" };
        createCalendar(getDateFromServer());
    }


    private void createCalendar (int[] date) {
        calendar = Calendar.getInstance(Locale.US);
        calendar.set(date[0], date[1], date[2]);

        calendarTemp = Calendar.getInstance(Locale.US);
        calendarTemp.set(date[0], date[1], 0); // check first day in a month
        //Log.e("TAG", "createCalendar(TEMP): "+days[calendarTemp.get(Calendar.DAY_OF_WEEK)]);

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
        int calendarPadding = calendarTemp.get(Calendar.DAY_OF_WEEK);

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
            mainTable.removeAllViews();
            int[] month = getDateFromServer(); // need to replace it with some constant defined after connecting
            month[1]--;
            month[2] = 1;
            if (month[1]==DATE_ACTUAL_SERVER_MONTH) {
                month[2] = DATE_ACTUAL_SERVER_DAY;
            }
            createCalendar(month);
        } else {
            mainTable.removeAllViews();
            int[] month = getDateFromServer(); // the same with this
            month[1]++;
            month[2] = 1;
            if (month[1]==DATE_ACTUAL_SERVER_MONTH) {
                month[2] = DATE_ACTUAL_SERVER_DAY;
            }
            createCalendar(month);
        }
    }
}
