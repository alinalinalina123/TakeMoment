package takeMoment.alina;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import takeMoment.alina.database.MyDAO;
import takeMoment.alina.image.PhotoUtils;
import takeMoment.alina.image.RoundImageView;
import takeMoment.alina.model.User;

public class SignUpActivity extends AppCompatActivity {
    private Context context;
    private EditText mName;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mEmail;
    private RoundImageView mProfilePic;
public static String name ;
    public static String    username;
    public static String       password;
    public static String email;
    private String mFilename = "user";
    private File mPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Регистрация");
        setContentView(R.layout.activity_sign_up);
        context = this;

        mName = (EditText) findViewById(R.id.signup_name);
        mUsername = (EditText) findViewById(R.id.signup_username);
        mPassword = (EditText) findViewById(R.id.signup_password);
        mProfilePic = (RoundImageView) findViewById(R.id.signup_profile);
         mEmail=(EditText)findViewById(R.id.signup_email);
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                mFilename = "IMG_" + UUID.randomUUID().toString() + ".jpg";
                File picturesDir =
                        SignUpActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                mPhotoFile = new File(picturesDir, mFilename);

                Uri photoUri = Uri.fromFile(mPhotoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(intent, 0);
            }
        });

        Button mSignUp = (Button) findViewById(R.id.signup_button);
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 name = mName.getText().toString();
                 username = mUsername.getText().toString();
                 password = mPassword.getText().toString();
                email=mEmail.getText().toString();


                if (username.trim().length() == 0){
                    Toast.makeText(SignUpActivity.this, "Введите логин.",Toast.LENGTH_SHORT ).show();
                }else if (password.trim().length() == 0){
                    Toast.makeText(SignUpActivity.this, "Введите пароль.",Toast.LENGTH_SHORT ).show();
                }else if(MyDAO.get(context).getUser(username)!=null){
                    Toast.makeText(SignUpActivity.this, "Такой логин уже существует.",Toast.LENGTH_SHORT ).show();
                }else{
                    User user = new User(username,password);
                    user.setName(name);
                    user.setProfilePic(mFilename);
                    SaveUserTask sav_us=new SaveUserTask();
                    sav_us.execute();
                    MyDAO.get(context).addUser(user);///ДОБАВЛЕНИЕ ЮЗЕРА В ЛОКАЛЬНУЮ БД
                    Intent intent1 = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent1);
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            Bitmap photo = PhotoUtils.getScaledBitmap(mPhotoFile.getPath(),
                    mProfilePic.getWidth(), mProfilePic.getHeight());
            mProfilePic.setImageBitmap(photo);

        }
    }
    class SaveUserTask extends AsyncTask<Void,Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("login", username);
                jsonObject.accumulate("password", password);
                jsonObject.accumulate("name", name);
                jsonObject.accumulate("email", email);


                String data = jsonObject.toString();
                Log.d("json data", data);
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost("http://192.168.43.66:8080/UC/saveUser");
                StringEntity se = new StringEntity(data);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPost);

                // 9. receive response as inputStream
                Log.d("----!!!!_----", httpResponse.getEntity().getContent().toString());


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;}

    }
}

