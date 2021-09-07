package ai.axcess.axcessfoods;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static android.graphics.Color.RED;

public class Baradmin extends AppCompatActivity {
    Button llogout;
    Button transactions;
    String cunq;
    String whichsection;
    String getsection;
    Handler handler;
    Handler handler2;
    AlertDialog dialog;
    WebView webView;
    Boolean isRunning = false;
    MediaPlayer player;
    TextView setdevice;
    String cookieval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baradmin);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = shared.edit();



        dialog = new SpotsDialog.Builder()
                .setMessage("Please Wait Loading...")
                .setContext(Baradmin.this)
                .build();
        dialog.show();



        //Intent i = new Intent(this, Myservice.class);
        //this.startService(i);

        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);


        cunq = shared.getString("barowner", "");
        int j = shared.getInt("key", 0);


        llogout = (Button)findViewById(R.id.logout);
        llogout.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_exit_to_app_24, 0, 0, 0);

        llogout.setTag("Logout");
        transactions = (Button)findViewById(R.id.transactions);



        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        setdevice = (TextView)findViewById(R.id.deviceid);
        setdevice.setText(thisdevice);


        FirebaseDatabase database = FirebaseDatabase.getInstance("https://axcessdrivers-default-rtdb.firebaseio.com/");
        //DatabaseReference restaurant = database.getReference(thisdevice); // yourwebisteurl/rootNode if it exist otherwise don't pass any string to it.
        //restaurant.child("waitingstatus").setValue("waiting");


        DatabaseReference restaurant = FirebaseDatabase.getInstance().getReference(thisdevice);
        restaurant.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("waitingstatus")) {
                    // Exist! Do whatever.
                } else {
                    // Don't exist! Do something.
                    restaurant.child("waitingstatus").setValue("waiting");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed, how to handle?

            }

        });



        restaurant.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                String alert = dataSnapshot.child("waitingstatus").getValue(String.class);
                //boolean isSeen = ds.child("isSeen").getValue(Boolean.class);
                //Log.d(TAG, "Value is: " + value);
                // Toast.makeText(getApplicationContext(), "Value is:" + value + " Alert: " + alert, Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), "changed : " + value, Toast.LENGTH_LONG).show();

                if(alert.equals("alert")){
                    if(isRunning) {

                        // Toast.makeText(getApplicationContext(), "Runing already Alert: " + alert, Toast.LENGTH_LONG).show();

                    }else {

                       // Toast.makeText(getApplicationContext(), "Not Runing already Alert: " + alert, Toast.LENGTH_LONG).show();

                        if( !am.isMusicActive()) {

                            isRunning = true;
                            startplayer();
                            // Toast.makeText(getApplicationContext(), "Alert on : " + alert, Toast.LENGTH_LONG).show();

                        }



                    }


                }else{


                    if(isRunning) {
                        isRunning = false;
                    }

                    if(player != null){
                        player.stop();
                        player.release();
                        player = null;
                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "AxcessEats Welcome.", error.toException());
            }
        });





        transactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                setSection("Settings");
                transactions.setVisibility(View.INVISIBLE);
                llogout.setText("Dashboad");
                llogout.setTag("Return");
                llogout.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_transit_enterexit_24, 0, 0, 0);



                dialog = new SpotsDialog.Builder()
                        .setMessage("Please Wait")
                        .setContext(Baradmin.this)
                        .build();
                dialog.show();

                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run(){

                Baradmin.this.webView.loadUrl("https://axcess.ai/bar/app.php");
                       // dialog.dismiss();
                    }
                }, 3000);

            }

        });

        llogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String tag = (String) llogout.getTag();

                if(tag.equals("Return")) {
                    llogout.setText("Logout");
                    llogout.setTag("Logout");
                    transactions.setVisibility(View.VISIBLE);
                    Baradmin.this.webView.loadUrl("http://axcess.ai/barapp/loadurl.php?barownerid=" + cunq);
                    llogout.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_exit_to_app_24, 0, 0, 0);

                }else {

                    shared.edit().clear().commit();

                    Intent offintent = new Intent("stopchecks");
                    offintent.putExtra("send", "off");
                    offintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().sendBroadcast(offintent);


                    dialog = new SpotsDialog.Builder()
                            .setMessage("Please Wait")
                            .setContext(Baradmin.this)
                            .build();
                    dialog.show();

                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            stopService(new Intent(Baradmin.this, Myservice.class));
                            Intent intent = new Intent(Baradmin.this, Loginuser.class);
                            startActivity(intent);

                            dialog.dismiss();

                        }
                    }, 1000);

                }//emd if
            }

        });


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String thissection = getSection();



        webView = (WebView) findViewById(R.id.web);
        webView.addJavascriptInterface(new WebAppInterface(this), "android");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //webView.setWebViewClient(new WebViewClient());
        //WebView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://axcess.ai/barapp/loadurl.php?barownerid=" + cunq);


        webView.setWebViewClient(new WebViewClient() {

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){

                handler.proceed();

            }


            @Override
            public void onPageFinished(WebView view, String url) {
                //Toast.makeText(getApplicationContext(), url + "\n\n", Toast.LENGTH_LONG).show();
                Log.d("WebView", url);

                if (url.equals("https://axcess.ai/barapp/andriodapp.php")) {
                    Toast.makeText(getApplicationContext(), "Preparing your dasboard", Toast.LENGTH_LONG).show();
                    dialog.dismiss();


                    String cookies = CookieManager.getInstance().getCookie(url);
                    Log.d(TAG, "All the cookies in a string:" + cookies);
                    cookieval = getCookie(url ,"cbarunq");
                    Log.d(TAG, "my cookies: " + cookieval);


                    if(cookieval != null){


                        try {
                            senddeviceid("https://axcess.ai/barapp/senddeviceid.php?cbarunq="+cookieval + "&device="+thisdevice);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        try {
                            senddeviceid("http://axcess.ai/barapp/senddeviceid.php?cbarunq="+cookieval + "&device="+thisdevice);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }




                }

                if (url.equals("https://axcess.ai/bar/app.php")) {
                    Toast.makeText(getApplicationContext(), "Loading", Toast.LENGTH_LONG).show();
                    dialog.dismiss();

                }

                if (url.equals("https://axcess.ai/barapp/restart.php")) {
                    Toast.makeText(getApplicationContext(), "App timeout- Restarting", Toast.LENGTH_LONG).show();

                    shared.edit().clear().commit();

                    Intent offintent = new Intent("stopchecks");
                    offintent.putExtra("send", "off");
                    offintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().sendBroadcast(offintent);


                    dialog = new SpotsDialog.Builder()
                            .setMessage("Please Wait")
                            .setContext(Baradmin.this)
                            .build();
                    dialog.show();

                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            stopService(new Intent(Baradmin.this, Myservice.class));
                            Intent intent = new Intent(Baradmin.this, Loginuser.class);
                            startActivity(intent);

                            dialog.dismiss();

                        }
                    }, 1000);


                }






                super.onPageFinished(view, url);


            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                //Toast.makeText(getApplicationContext(), "AJAX" + url, Toast.LENGTH_LONG).show();
                return null;
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });



    }

    public void startplayer(){

        player = MediaPlayer.create(this, R.raw.beep6);
        player.setVolume(20, 20);
        player.start();
        //player.stop();
        //player.release();
    }



    public String getSection() {

        return whichsection;
    }

    public void setSection(String newName) {
        this.whichsection = newName;
    }



    void senddeviceid(String url) throws IOException {
        Log.d(TAG, "url:" + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                               // Toast.makeText(getApplicationContext(), "Device status: " + e, Toast.LENGTH_LONG).show();

                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {

                        String resulting = response.body().string();
                        //Toast.makeText(getApplicationContext(), "Device status: " + resulting, Toast.LENGTH_LONG).show();


                    }//end void

                });
    }




    public String getCookie(String siteName,String CookieName){
        String CookieValue = null;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if(cookies != null){
            String[] temp=cookies.split(";");
            for (String ar1 : temp ){
                if(ar1.contains(CookieName)){
                    String[] temp1=ar1.split("=");
                    CookieValue = temp1[1];
                }
            }
        }
        return CookieValue;
    }




    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

}