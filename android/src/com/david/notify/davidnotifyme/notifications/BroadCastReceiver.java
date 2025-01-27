package com.david.notify.davidnotifyme.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import androidx.preference.PreferenceManager;


import com.david.notify.R;
import com.david.notify.davidnotifyme.MainActivity;
import com.david.notify.davidnotifyme.david.David;
import com.david.notify.davidnotifyme.david.DavidClockUtils;
import com.david.notify.davidnotifyme.david.Timetable;
import com.david.notify.davidnotifyme.lunch.LunchActivity;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class BroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        if (extras != null) {
            String notificationType = extras.getString("notificationType");
            Log.d("Received Msg : ", notificationType);

            switch (notificationType) {
                case "update":
                    updateNotification(context);
                    break;
                case "morningMessage":
                    showMorningMessage(context);
                    break;
                case "reminder":
                    extras.getString("message");

                    break;
            }
        }
    }

    public static void updateNotification(Context context) {
        Executor executor = runnable -> new Thread(runnable).start();

        David activeDavid = MainActivity.getDavid();

        David david = activeDavid == null ? new David(context, executor) : activeDavid;

        Log.d("show", shouldShowNotification(context) + "");

        if(shouldShowNotification(context)){
            Timetable timetable = david.ziskajRozvrh();

            timetable.addOnLoadListener(new Timetable.OnLoadListener() {
                @Override
                public void onLoadTimetable(Timetable timetable) {
                    boolean prebiehajucaNotifikacia = david.prebiehaHodina() && !david.bliziSaKoniecHodiny();

                    timetable.getSubjectsToday();

                    Log.d("compare", david.prebiehaHodina() + "." + david.bliziSaKoniecHodiny());

                   // Log.d("minutes", DavidClockUtils.timeToMinutes(timetable.getEndOfCurrentLesson()) + "-" + DavidClockUtils.currentTimeInMinutes());

                    Pair<String, String> updatedNotification = prebiehajucaNotifikacia ? david.ziskajPrebiehajucuHodinu() : david.ziskajDalsiuHodinuEdupage(true);

                    if(updatedNotification.second.equals("Zisťujem čo je na obed...")) {
                        david.zistiNovyObed().nastavObedoveNacuvadlo(new David.OnObedNajdenyNacuvadlo() {
                            @Override
                            public void onObedNajdeny(ArrayList<String> data) {
                                int dayIndex = DavidClockUtils.zistiDen() - 1;
                                String formatLunch;
                                if (dayIndex < data.size()) {
                                    String todayLunch = LunchActivity.formatLunch(data.get(DavidClockUtils.zistiDen() - 1), ", ");
                                    formatLunch = context.getString(R.string.na_obed) + todayLunch;
                                }
                                else formatLunch = "Dneska obed nie je";
                                DavidNotifications.updateNotification(context, updatedNotification.first, formatLunch);
                            }
                        });
                    }

                    DavidNotifications.updateNotification(context, updatedNotification.first,  updatedNotification.second);
                }
            });
        } else {
            DavidNotifications.stopNotification(context, DavidNotifications.LESSON_ONGOING_NOTIFICATION);
        }
    }

    private void showMorningMessage(Context context) {
        if(!DavidClockUtils.jeVikend()) {
            String header = context.getString(R.string.good_morning);
            Timetable timetable = new Timetable(context);
            timetable.addOnLoadListener(new Timetable.OnLoadListener() {
                @Override
                public void onLoadTimetable(Timetable timetable) {
                    String message = David.ziskajRannuSpravu(context, timetable);
                    DavidNotifications.showNotificationMessage(context, header, message, DavidNotifications.MORNING_NOTIFICATION);
                    scheduleNotificationsToday(context, timetable);
                }
            });
        }
    }

    private void scheduleNotificationsToday(Context context, Timetable timetable) {
        Intent notificationIntent = new Intent(context, BroadCastReceiver.class);

        Bundle extras = new Bundle();
        extras.putString("notificationType", "update");
        notificationIntent.putExtras(extras);

        Executor executor = runnable -> new Thread(runnable).start();
        MainActivity.scheduleNotifications(context, notificationIntent, timetable);
    }

    public static boolean shouldShowNotification(Context context) {

        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);

        if(!pref.getBoolean("show_notify",false)) return false;

        int time_on =  DavidClockUtils.timeToMinutes(pref.getString("time_on", "23:00"));
        int time_off =  DavidClockUtils.timeToMinutes(pref.getString("time_off", "00:00"));
        int currentTime =  DavidClockUtils.currentTimeInMinutes();

        Log.d("vikend",  DavidClockUtils.jeVikend() + "");

        return time_on < currentTime && currentTime < time_off && ! DavidClockUtils.jeVikend();
    }
}