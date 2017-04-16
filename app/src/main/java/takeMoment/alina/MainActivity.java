package takeMoment.alina;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;

import java.util.concurrent.ExecutionException;

import takeMoment.alina.database.MyDAO;
import takeMoment.alina.homepage.HomepageActivity;
import takeMoment.alina.model.User;

import static takeMoment.alina.Constants.URL.HOST;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_USERNAME = "username";
    private Context context;
    private String username;
    private String password;
    private User mUser;
    private String mFilename = "user";
    public static String str;
    public static boolean flag=false;
    public static JSONObject jsobj;
 public static  boolean response_log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);



        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        context = this;

        final EditText mUsername = (EditText) findViewById(R.id.main_username);
        final EditText mPassword = (EditText) findViewById(R.id.main_password);


        Button mSignUp = (Button) findViewById(R.id.main_signup);
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });


        //log in
        Button mLogIn = (Button) findViewById(R.id.main_log_in);
        mLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTask mt = new MyTask();
                str=mUsername.getText().toString().trim();

                    mt.execute();
                try {
                    String str_result= new MyTask().execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if(response_log) {
                    username = mUsername.getText().toString();
                    password = mPassword.getText().toString();
                    mUser = MyDAO.get(context).getUser(username);
                    if (mUser != null) {
                        if (mUser.getPassword().equals(password)) {
                            Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
                            intent.putExtra(KEY_USERNAME, username);
                            startActivityForResult(intent, 0);
                        } else {

                            Toast.makeText(context, "Не верный пароль.", Toast.LENGTH_SHORT).show();
                        }

                    } else {

                        try {
                            if(jsobj!=null) {
                                String Name = jsobj.getString("name");
                                String Login = jsobj.getString("login");
                                String password = jsobj.getString("password");
                                String Email = jsobj.getString("email");
                                User user = new User(Login,password);
                                user.setName(Name);
                                user.setProfilePic(mFilename);
                                MyDAO.get(context).addUser(user);
                                Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
                                intent.putExtra(KEY_USERNAME, username);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                        // ТУТ НАПИСАТЬ ФУНКЦИЮ ДЛЯ ЗАПОЛНЕНИЯ БД ДАННЫМИ О ВЫБРАННОМ ПОЛЬЗОВАТЕЛЕ НАПИСАТЬ ЗАПРОС К СЕРВИСУ GetUser.
                    }
                }
                else Toast.makeText(context, "Такого пользователя не существует.", Toast.LENGTH_SHORT).show();

                }
        });

        ImageView mInsta = (ImageView)findViewById(R.id.main_insta);
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_view);
        mInsta.startAnimation(hyperspaceJumpAnimation);



    }

    class MyTask extends AsyncTask<Void,Void, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPostExecute(String result)
        {

            progressDialog.cancel();

            //Call your method that checks if the pictures were downloaded

        }

        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(
                    MainActivity.this);
            progressDialog.setMessage("Downloading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // Do nothing
        }

        @Override
        protected String doInBackground(Void... params) {

                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(HOST+"UC/login/"+str);
                HttpResponse response;
                try {
                    response = client.execute(request);
                    if(response.getStatusLine().getStatusCode()==200)
                    {
                        try
                        {

                            String resp_body = EntityUtils.toString(response.getEntity());
                            Log.v("resp_body", resp_body.toString());
                                response_log=Boolean.valueOf(resp_body.toString());

                            if(response_log)
                            {
                                HttpClient client2 = new DefaultHttpClient();
                                HttpGet request2 = new HttpGet(HOST+"UC/userData/"+str);
                                HttpResponse response2;
                                try {
                                    response2 = client2.execute(request2);
                                    if(response2.getStatusLine().getStatusCode()==200)
                                    {
                                        try
                                        {
                                            String resp = EntityUtils.toString(response2.getEntity());
                                            Log.v("resp_body", resp.toString());
                                             jsobj = new JSONObject(resp);


                                        }
                                        catch(Exception e)
                                        {

                                            Log.e("sometag",e.getMessage());
                                        }
                                    }

                                } catch (ClientProtocolException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace(); return null;
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();return null;
                                }
                            }
                        }
                        catch(Exception e)
                        {

                            Log.e("sometag",e.getMessage());
                        }
                    }
                    return response.toString();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace(); return null;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();return null;
                }
            }



    }
}
