package com.example.lab4;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.Window;
import android.widget.DatePicker;

import java.util.Calendar;

public class CalendarDialog extends Activity {

    private DatePickerDialog dateDialog;
    private int widgetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        widgetID = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

        Calendar today = Calendar.getInstance();
        dateDialog = new DatePickerDialog(this, dateDialogListener, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        dateDialog.setCancelable(false);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                killP();
            }
        };
        dateDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Отмена", listener);

        dateDialog.show();
    }

    private DatePickerDialog.OnDateSetListener dateDialogListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int month,
                              int day) {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(year, month, day, 9, 0, 0);

            AlarmManager alarmManager = (AlarmManager) CalendarDialog.this.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(CalendarDialog.this, MyWidget.class);
            intent.setAction("Alarm");
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

            PendingIntent pIntent = PendingIntent.getBroadcast(CalendarDialog.this, widgetID, intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, chosenDate.getTime().getTime(), pIntent);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, chosenDate.getTime().getTime(), pIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, chosenDate.getTime().getTime(), pIntent);

            MyWidget.updateW(CalendarDialog.this, widgetID, chosenDate);
            killP();
        }

    };

    private void killP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.finishAndRemoveTask();
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                finishAffinity();
            else
                finish();
        }
        Process.killProcess(Process.myPid());
    }
}
