package com.fartans.localme.Firebase;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fartans.localme.SignUp.NewSignUpActicity;
import com.fartans.localme.TempDataClass;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

/**
 * Created by nravishankar on 8/29/2016.
 */
public class MyFireBaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFireBaseIDService";

    @Override
    public void onTokenRefresh() {
        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        TempDataClass.deviceRegId = refreshedToken;
        sendRegistrationToServer(refreshedToken);
    }

    public static String getToken(){
        return FirebaseInstanceId.getInstance().getToken();
    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
        if(!TempDataClass.serverUserId.equals("")) {
            HttpPostRegIdToServer post = new HttpPostRegIdToServer();
            post.execute(TempDataClass.BASE_URL + "User/UpdateRegId");
        }
    }


    private class HttpPostRegIdToServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return POSTRegID(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("OK")){
            }
        }
    }

    public String POSTRegID(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Id", TempDataClass.serverUserId);
            if(TempDataClass.deviceRegId == null || TempDataClass.deviceRegId.equals("")){
                TempDataClass.deviceRegId = MyFireBaseInstanceIdService.getToken();
            }
            jsonObject.put("RegistrationId", TempDataClass.deviceRegId);
            json = jsonObject.toString();
            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");


            HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            return result;

        } catch (Exception e) {
            Log.v("Getter", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
