package com.tamarin.nextstep;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class DasReminderReceiver extends BroadcastReceiver {

    static final String CHANNEL_ID = "das_reminder_channel";
    static final int NOTIF_ID_15 = 1001;
    static final int NOTIF_ID_19 = 1002;

    @Override
    public void onReceive(Context context, Intent intent) {
        int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        if (dayOfMonth == 15) {
            showNotification(context,
                    "DAS MEI vence em 5 dias",
                    "Seu DAS vence dia 20. Pague agora para evitar multa e juros.",
                    NOTIF_ID_15);
        } else if (dayOfMonth == 19) {
            showNotification(context,
                    "DAS MEI vence amanhã!",
                    "Pague seu DAS hoje para não pagar multa por atraso.",
                    NOTIF_ID_19);
        }

        scheduleNext(context);
    }

    private void showNotification(Context context, String title, String body, int notifId) {
        createChannel(context);

        Intent openApp = new Intent(context, DashboardActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, notifId, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_home)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(notifId, builder.build());
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lembretes DAS MEI",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Avisos de vencimento do DAS MEI");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    static void schedule(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, DasReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 2001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pi);
    }

    private void scheduleNext(Context context) {
        schedule(context);
    }
}
