package com.sosial.sudoers.sosial;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private SharedPreferences sp;
    Intent mServiceIntent;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_login);
        //Remove title bar

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        sp = getSharedPreferences("login", MODE_PRIVATE);
        sp.getString("token", "");
        sp.getString("token2", "");

        if(sp.getBoolean("logged",  false)){
            goToMainActivity();
        }

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
                attemptLogin();
                Snackbar.make(view, "Login Unsuccessful. Try Again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        TextView registerTextField = (TextView) findViewById(R.id.register_text_view);
        registerTextField.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegisterActivity();
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(mEmailView.getText().length() == 0){
            mEmailView.setError("This field is required");
            cancel = true;
        }

        if(mPasswordView.getText().length() == 0){
            mPasswordView.setError(getString(R.string.error_field_required));
            cancel = true;
        }


        if(!cancel){
            String response = sendJson(email, password);
            JSONObject json = null;
            try {
                json = new JSONObject(response);
                response = json.getString("message");
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            if(response.equals("Login Successful.")){
                sp.edit().putBoolean("logged", true).apply();
                goToMainActivity();
            }
        }
    }

    private String sendJson(String user, String password){
        String url = "https://sosial.azurewebsites.net/login";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            postData.put("username", user);
            postData.put("password", password);
            SendRequest sdd =  new SendRequest();
            response  = sdd.execute(url, postData.toString()).get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }


    class SendRequest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();
                String cookie3 = httpURLConnection.getHeaderField(3);
                String cookie = httpURLConnection.getHeaderField("Set-Cookie");
                sp.edit().putString("token", cookie).apply();
                sp.edit().putString("token2", cookie3).apply();

                int response = httpURLConnection.getResponseCode();
                if(response == HttpURLConnection.HTTP_OK){
                    data = "Login Successful.";
                }
                else{
                    data = "Login Unsuccessful.";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    private void showProgress() {
        mLoginFormView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
        mProgressView.animate().setDuration(1000).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(View.GONE);
                if(!sp.getBoolean("logged", false))
                    mLoginFormView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void goToMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void goToRegisterActivity(){
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }
}

