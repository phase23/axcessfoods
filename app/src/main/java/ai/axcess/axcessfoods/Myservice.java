package ai.axcess.axcessfoods;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Myservice extends Service  {
    Context mContext;
    String responseBody;
    String responseLocation;
    private final int TWENTY_SECONDS = 2000;
    public Handler handler;
    MediaPlayer player;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                //.setSmallIcon(R.mipmap.app_icon)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startMyOwnForeground();
        }else {
            startForeground(1337, notification);
        }

        mContext=this;

        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onStart(Intent intent, int startid) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {



                isneworder();

                // this method will contain your almost-finished HTTP calls
                handler.postDelayed(this, TWENTY_SECONDS);
            }
        }, TWENTY_SECONDS);



    }




    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)

                .setContentTitle("ReadID")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }



    public void isneworder() {


        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = "http://axcess.ai/barapp/isneworder.php?action=checkdevice&token="+thisdevice;
        Log.i("action url",url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("deviceid",thisdevice )
                .build();
        Request request = new Request.Builder()
                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.i("SUCC",""+response.message());
            }
            String resp = response.message();
            responseLocation =  response.body().string();
            Log.i("respBody:main",responseLocation);
            Log.i("MSG",resp);

            String outputthis = responseLocation.trim();

            int myNum = 0;
            try {
                myNum = Integer.parseInt(outputthis);
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            if(myNum == 0){
            if(player != null){
                player.stop();
            }

            }else {


                player = MediaPlayer.create(this, R.raw.beep08b);
                player.setVolume(20, 20);
                player.start();
            }


            Log.i("respBody:outthis",outputthis);

        } catch (IOException e) {
            e.printStackTrace();
        }






    }//emd




}
