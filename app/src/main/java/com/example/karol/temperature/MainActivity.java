package com.example.karol.temperature;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Timer;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private int progressStatus = 0;
    boolean stanCold = false;
    boolean stanHot = false;
    boolean stanPerfect = false;
    int licznik = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.textView);

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        new MyDownloadTask(textView).execute("http://192.168.1.14");
                    }
                });
            }
        };
        timer.schedule(task, 0, 100);

        }



    private class MyDownloadTask extends AsyncTask<String,Void,String>
    {
        private TextView textView;
        public MyDownloadTask(TextView textView) {
            this.textView = textView;
        }
        final ProgressBar progressBar= (ProgressBar) findViewById(R.id.progressBar6);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);

        final int red = Color.parseColor("#F20300");
        final int green = Color.parseColor("#00FF00");
        final int blue = Color.parseColor("#0000FF");

        protected void onPreExecute() {
            //display progress dialog.

        }
        String inputLine;
        String nowy ="";
        protected String doInBackground(String... strings) {
            String text="";
            URL oracle = null;

            try {
                    oracle = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) oracle.openConnection();
                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();


                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }
                text = builder.toString();
                urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();

                }

            return text;
        }



        protected void onPostExecute(String result) {
            EditText getTemp = (EditText) findViewById(R.id.editText);
            EditText getTemp2 = (EditText) findViewById(R.id.editText2);
            Log.e("licznik", String.valueOf(licznik));
            if(stanPerfect)
            {
                createNotificationPerfect();
            }
            if(stanCold)
            {
                createNotificationCold();
            }
            if(stanHot)
            {
                createNotificationHot();
            }

            float liczba;
            float ogranicznik;
            float ogranicznik2;
            try {
                ogranicznik = Float.parseFloat((getTemp.getText().toString()));
                ogranicznik2 = Float.parseFloat((getTemp2.getText().toString()));
            }catch (NumberFormatException e)
            {
                ogranicznik = 80f;
                ogranicznik2 = 60f;
            }
            try {
                if (result.equals(null)) {
                    liczba = 0f;
                    textView.setText("Brak połączenia");
                    stanPerfect = false;
                    stanHot = false;
                    stanCold = false;
                } else {
                    liczba = Float.parseFloat(result);
                    textView.setText(result + " °C");

                    progressBar.setProgress((int)Math.round(liczba));
                    if (liczba > ogranicznik2 && liczba < ogranicznik) {
                        stanPerfect = true;
                        stanHot = false;
                        stanCold = false;

                        if(!(licznik>30))
                        {
                            licznik++;

                        }
                        else
                        {
                            licznik=31;
                        }

                        imageView.setColorFilter(green);
                        progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                    } else if (liczba < ogranicznik2) {
                        imageView.setColorFilter(blue);
                        stanPerfect = false;
                        stanHot = false;
                        licznik = 0;
                        stanCold = true;
                        progressBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
                    } else if (liczba > ogranicznik) {
                        stanPerfect = false;
                        stanHot = true;
                        licznik = 0;
                        stanCold = false;
                        imageView.setColorFilter(red);
                        progressBar.setProgressTintList(ColorStateList.valueOf(red));
                    }

                }
            }catch (NumberFormatException e)
            {
                textView.setText("Brak połączenia");
            }
            // dismiss progress dialog and update ui
        }
    }

    private void createNotificationPerfect() {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

       // Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("Yerba Mate")
                .setContentText("Gotowa do zalania!")
                .setTicker("Masz wiadomość")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                //.setLargeIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .build();

        noti.defaults |= Notification.DEFAULT_LIGHTS;


        noti.flags |= Notification.FLAG_SHOW_LIGHTS;
        noti.ledARGB = Color.GREEN;
        noti.ledOffMS = 500;
        noti.ledOnMS = 500;
        long[] vibratePattern = {0, 200, 100, 300};
        long[] vibratePattern2 = {0, 0, 0, 0};
        if(licznik > 0 & licznik < 30) {
            noti.defaults |= Notification.DEFAULT_VIBRATE;
            noti.vibrate = vibratePattern;
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, noti);
    }
    private void createNotificationHot() {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("Yerba Mate")
                .setContentText("Zbyt gorąca!")
                .setTicker("Masz wiadomość")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
               // .setLargeIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, noti);
    }
    private void createNotificationCold() {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

//        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("Yerba Mate")
                .setContentText("Za zimna!")
                .setTicker("Masz wiadomość")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                //.setLargeIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, noti);
    }

}