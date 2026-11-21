
package in.webcodelab.countdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() == null ||
            !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }

        long targetTime = context.getSharedPreferences(
            "wcl", Context.MODE_PRIVATE
        ).getLong("time", 0);

        if (targetTime <= System.currentTimeMillis()) return;

        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE
            );

        if (Build.VERSION.SDK_INT >= 31 &&
            !alarmManager.canScheduleExactAlarms()) {
            return;
        }

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT |
            PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            targetTime,
            pendingIntent
        );
    }
}
