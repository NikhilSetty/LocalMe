package com.fartans.localme.Firebase;

import android.util.Log;

import com.fartans.localme.TempDataClass;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

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

    }

    public static String getToken(){
        return FirebaseInstanceId.getInstance().getToken();
    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
    }
}
