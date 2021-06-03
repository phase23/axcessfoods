package ai.axcess.axcessfoods;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import dmax.dialog.SpotsDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Loginuser extends AppCompatActivity {

    EditText pin;
    Button llogin;
    AlertDialog dialog;
    String postaction;
    String cunq;
    String company;
    SharedPreferences sharedpreferences;
    int autoSave;
    Handler handler2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginuser);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedpreferences = getSharedPreferences("autoLogin", Context.MODE_PRIVATE);
        int j = sharedpreferences.getInt("key", 0);
        if(j > 0){
            Intent activity = new Intent(getApplicationContext(), Baradmin.class);
            startActivity(activity);
        }

        handler2 = new Handler(Looper.getMainLooper());
        llogin = (Button)findViewById(R.id.login);
        pin = (EditText)findViewById(R.id.company);



        llogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {







                String thispin = pin.getText().toString();
                if (thispin.matches("")) {
                    Toast.makeText(getApplicationContext(), "Enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }


                dialog = new SpotsDialog.Builder()
                        .setMessage("Please Wait")
                        .setContext(Loginuser.this)
                        .build();
                dialog.show();






                try {
                    doGetRequest("http://axcess.ai/barapp/process_bar.php?&baruser=" + thispin);
                } catch (IOException e) {
                    e.printStackTrace();
                }



                // String body = client.newCall(request).execute( ).body().toString();
                // upate textFields, images etc...
                // postaction = postLogin(thispin);//CallbackFuture future = new CallbackFuture();// client.newCall(request).enqueue(future);
                // Response response = future.get();
                // postaction = getlpost();



            }

        });

    }


    void doGetRequest(String url) throws IOException{
        Log.i("assyn url this",url);
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
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        postaction = response.body().string();
                        Log.i("assyn url",postaction);
                        // Do something with the response


                        Log.i("[print]",postaction);
                        postaction = postaction.trim();



                        String[] separated = postaction.split("~");
                        String dologin = separated[0];
                        cunq = separated[1];

                        if(dologin.equals("noluck")){
                           // Toast.makeText(getApplicationContext(), "Your password is incorrect", Toast.LENGTH_LONG).show();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // For the example, you can show an error dialog or a toast
                                    // on the main UI thread
                                    Toast.makeText(getApplicationContext(), "Your password is incorrect", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            });


                            return;

                        }

                        if(dologin.equals("sucess")){
                            company = separated[2];
                            Log.i("pass:unq -- ",cunq + "name: "+ company);

                            autoSave = 1;
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putInt("key", autoSave);
                            editor.putString("barowner", cunq);
                            editor.putString("company", company);
                            editor.apply();


                            // Toast.makeText(getApplicationContext(), "Success "+ cunq, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Loginuser.this, Baradmin.class);
                            intent.putExtra("barowner",cunq);
                            intent.putExtra("company",company);
                            startActivity(intent);
                            dialog.dismiss();

                        }




                    }
                });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        View focusedView = this.getCurrentFocus();

        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        return true;
    }


}