
package in.webcodelab.countdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String name = context.getSharedPreferences(
            "wcl", Context.MODE_PRIVATE
        ).getString("name", "Your Special Day");

        Intent openApp = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            101,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT |
            PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notification =
            new NotificationCompat.Builder(context, "wcl_countdown")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🎉 Countdown Finished!")
            .setContentText(name + " has arrived. Your wait is over!")
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Your countdown for " + name +
                " is complete. The special moment is here!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        NotificationManager manager =
            (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE
            );

        manager.notify(1001, notification.build());
    }
}
