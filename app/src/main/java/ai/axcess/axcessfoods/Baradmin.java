package ai.axcess.axcessfoods;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

public class Baradmin extends AppCompatActivity {
    Button llogout;
    String cunq;
    Handler handler;
    Handler handler2;
    AlertDialog dialog;

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



        Intent i = new Intent(this, Myservice.class);
        this.startService(i);

        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);


        cunq = shared.getString("barowner", "");
        int j = shared.getInt("key", 0);



        llogout = (Button)findViewById(R.id.logout);

        llogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    public void run(){

                        stopService(new Intent(Baradmin.this, Myservice.class));
                        Intent intent = new Intent(Baradmin.this, Loginuser.class);
                        startActivity(intent);

                        dialog.dismiss();

                    }
                }, 1000);


            }

        });


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        WebView webView = (WebView) findViewById(R.id.web);
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