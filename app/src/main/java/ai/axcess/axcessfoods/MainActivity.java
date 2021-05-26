package ai.axcess.axcessfoods;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.onesignal.OneSignal;


public class MainActivity extends AppCompatActivity {
    private static final String ONESIGNAL_APP_ID = "070e3946-98cd-4d8e-b3f6-ce12c3257c72";
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        String deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.i("log device", deviceId);


        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        Intent i = new Intent(this, Myservice.class);
        this.startService(i);

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
        webView.loadUrl("https://axcess.ai/barapp/");

        webView.setWebViewClient(new WebViewClient() {

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){

                handler.proceed();

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
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