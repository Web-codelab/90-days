
package in.webcodelab.countdown;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class MainActivity extends AppCompatActivity {

    TextView timer, title;
    EditText eventName;
    Button dateButton, saveButton;
    Calendar target = Calendar.getInstance();
    Handler handler = new Handler();
    Runnable ticker;
    long targetTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(16,43,80));
        getWindow().setNavigationBarColor(Color.rgb(16,43,80));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(24,40,24,24);
        root.setBackgroundColor(Color.rgb(245,248,255));

        title = new TextView(this);
        title.setText("WCL COUNTDOWN");
        title.setTextSize(26);
        title.setTextColor(Color.rgb(16,43,80));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        eventName = new EditText(this);
        eventName.setHint("Enter Event Name");
        root.addView(eventName);

        dateButton = new Button(this);
        dateButton.setText("SELECT DATE & TIME");
        root.addView(dateButton);

        timer = new TextView(this);
        timer.setText("00 Days : 00 Hours : 00 Minutes : 00 Seconds");
        timer.setTextSize(20);
        timer.setTextColor(Color.rgb(16,43,80));
        timer.setGravity(Gravity.CENTER);
        timer.setPadding(0,40,0,40);
        root.addView(timer);

        saveButton = new Button(this);
        saveButton.setText("START COUNTDOWN");
        root.addView(saveButton);

        setContentView(root);

        dateButton.setOnClickListener(v -> pickDate());

        saveButton.setOnClickListener(v -> {
            if(targetTime <= System.currentTimeMillis()) {
                Toast.makeText(this,"Select a future date and time",Toast.LENGTH_LONG).show();
                return;
            }
            getSharedPreferences("wcl",MODE_PRIVATE)
                .edit()
                .putString("name",eventName.getText().toString())
                .putLong("time",targetTime)
                .apply();

            scheduleAlarm();
            startTimer();
            Toast.makeText(this,"Countdown Started!",Toast.LENGTH_SHORT).show();
        });

        createChannel();

        SharedPreferences prefs = getSharedPreferences("wcl",MODE_PRIVATE);
        targetTime = prefs.getLong("time",0);
        eventName.setText(prefs.getString("name",""));

        if(targetTime > 0) {
            target.setTimeInMillis(targetTime);
            startTimer();
            if(targetTime > System.currentTimeMillis()) scheduleAlarm();
        }

        if(Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{"android.permission.POST_NOTIFICATIONS"},101);
        }
    }

    void pickDate() {
        Calendar now = Calendar.getInstance();

        new DatePickerDialog(this,(view,year,month,day) -> {
            target.set(year,month,day);
            new TimePickerDialog(this,(timeView,hour,minute) -> {
                target.set(Calendar.HOUR_OF_DAY,hour);
                target.set(Calendar.MINUTE,minute);
                target.set(Calendar.SECOND,0);
                targetTime = target.getTimeInMillis();
                dateButton.setText(new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",Locale.getDefault())
                    .format(target.getTime()));
            },now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),false).show();
        },now.get(Calendar.YEAR),now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)).show();
    }

    void startTimer() {
        if(ticker != null) handler.removeCallbacks(ticker);

        ticker = new Runnable() {
            @Override
            public void run() {
                long diff = targetTime-System.currentTimeMillis();

                if(diff <= 0) {
                    timer.setText("🎉 COUNTDOWN FINISHED!");
                    handler.removeCallbacks(this);
                    return;
                }

                long days = diff/86400000;
                long hours = (diff/3600000)%24;
                long mins = (diff/60000)%60;
                long secs = (diff/1000)%60;

                timer.setText(String.format(Locale.getDefault(),
                    "%02d Days : %02d Hours : %02d Minutes : %02d Seconds",
                    days,hours,mins,secs));

                handler.postDelayed(this,1000);
            }
        };
        handler.post(ticker);
    }

    void createChannel() {
        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                "wcl_countdown","WCL Countdown",
                NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Countdown completion alerts");
            getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
        }
    }

    void scheduleAlarm() {
        if(Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission("android.permission.POST_NOTIFICATIONS")
            != android.content.pm.PackageManager.PERMISSION_GRANTED) return;

        Intent intent = new Intent(this,AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
            this,100,intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

        if(Build.VERSION.SDK_INT >= 31 &&
            !am.canScheduleExactAlarms()) {
            Toast.makeText(this,"Allow Alarms & Reminders permission",Toast.LENGTH_LONG).show();
            return;
        }

        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,targetTime,pi);
    }
}
