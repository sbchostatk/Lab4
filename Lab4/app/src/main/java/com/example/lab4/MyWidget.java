package com.example.lab4;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MyWidget extends AppWidgetProvider {

    final static String NAME = "widgetInfo";
    final static String COUNT_OF_DAYS = "countOfDays";
    final static String DATE = "date";
    static SharedPreferences sp;

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int i = 0; i < appWidgetIds.length; i++) {
            int widgetID = appWidgetIds[i];

            Intent intent = new Intent(context, CalendarDialog.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, intent, 0);

            RemoteViews widget_v = new RemoteViews(context.getPackageName(), R.layout.widget);
            widget_v.setOnClickPendingIntent(R.id.rl_widget, pIntent);
            appWidgetManager.updateAppWidget(widgetID, widget_v);

            sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
            String date = sp.getString(DATE + widgetID, "-");

            if (!date.equals("-")) {
                Calendar today = Calendar.getInstance();
                Calendar chosenDate = Calendar.getInstance();
                try {
                    chosenDate.clear();
                    chosenDate.setTime(format.parse(date));
                    chosenDate.set(Calendar.HOUR, 9);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (today.after(chosenDate)) {
                    Notification notification = new Notification(context);
                    notification.sendNotification();
                }
                else {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent new_intent = new Intent(context, MyWidget.class);
                    new_intent.setAction("Alarm");
                    new_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context, widgetID, new_intent, 0);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, chosenDate.getTime().getTime(), alarmIntent);
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, chosenDate.getTime().getTime(), alarmIntent);
                    else
                        alarmManager.set(AlarmManager.RTC_WAKEUP, chosenDate.getTime().getTime(), alarmIntent);
                }
                updateW(context, widgetID, chosenDate);

            } else {
                widget_v.setTextViewText(R.id.date, date);
                widget_v.setTextViewText(R.id.number, "-");
                appWidgetManager.updateAppWidget(widgetID, widget_v);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals("Alarm")) {
            Notification notification = new Notification(context);
            notification.sendNotification();

            int widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            widgetUpdateAfterAlarm(context, widgetID);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int i = 0; i < appWidgetIds.length; i++) {
            int widgetID = appWidgetIds[i];
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, MyWidget.class);
            intent.setAction("Alarm");

            PendingIntent pIntent = PendingIntent.getBroadcast(context, widgetID, intent, 0);
            alarmManager.cancel(pIntent);

            sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
            sp.edit().remove(DATE + widgetID).commit();
            sp.edit().remove(COUNT_OF_DAYS + widgetID).commit();
        }
    }

    //считает кол-во дней до даты
    private static int countOfDays(Calendar chooseDate) {
        int countDays = 0;
        Calendar thisDate = Calendar.getInstance();
        if (chooseDate.after(thisDate)) {
            long millis = chooseDate.getTime().getTime() - thisDate.getTime().getTime();
            countDays = (int) ((millis / (24 * 60 * 60 * 1000)) + 1);
        }
        return countDays;
    }

    public static void updateW(Context context, int widgetID, Calendar chooseDate) {
        int countDays = countOfDays(chooseDate);

        sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putString(DATE + widgetID, format.format(chooseDate.getTime())).commit();
        sp.edit().putInt(COUNT_OF_DAYS + widgetID, countDays).commit();

        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
        widgetView.setTextViewText(R.id.date, format.format(chooseDate.getTime()));
        widgetView.setTextViewText(R.id.number, String.valueOf(countDays));

        AppWidgetManager.getInstance(context).updateAppWidget(widgetID, widgetView);
    }

    private void widgetUpdateAfterAlarm(Context context, int widgetID) {
        sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(COUNT_OF_DAYS + widgetID, 0).commit();

        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
        widgetView.setTextViewText(R.id.number, "0");
        AppWidgetManager.getInstance(context).updateAppWidget(widgetID, widgetView);
    }
}