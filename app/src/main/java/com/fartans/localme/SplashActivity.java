package com.fartans.localme;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fartans.localme.DBHandlers.DeviceInfoDBHandler;
import com.fartans.localme.DBHandlers.RequestsDBHandler;
import com.fartans.localme.DBHandlers.UserModelDBHandler;
import com.fartans.localme.Firebase.MyFireBaseInstanceIdService;
import com.fartans.localme.models.DeviceInfoKeys;
import com.fartans.localme.models.DeviceInfoModel;
import com.fartans.localme.models.UserModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


public class SplashActivity extends Activity implements LocationListener {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "828188631625";
    TextView mDisplay;
    ImageView mIcon;
    static final String TAG = "GCM";

    String regid;

    AtomicInteger msgId = new AtomicInteger();
    Context context;

    private LocationManager locationManager;
    private String provider;
    public static String lattitude;
    public static String longitude;

    public String[] requestIds;

    private static int SPLASH_TIME_OUT = 3000;

    String registrationId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = getApplicationContext();

        mDisplay = (TextView) findViewById(R.id.textViewRegId);
        mIcon = (ImageView)findViewById(R.id.imageView5);

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            //if(true){
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }

            TempDataClass.deviceRegId = regid;
            TempDataClass.isThroughSplash = true;

            String dbRegID = DeviceInfoDBHandler.GetValueForKey(getApplicationContext(), DeviceInfoKeys.REGISTRATION_ID);

            if (!regid.equals(dbRegID)) {
                //TODO
            }


            getCurrentLocation();

            String profilePhotoPath = DeviceInfoDBHandler.GetValueForKey(getApplicationContext(), DeviceInfoKeys.PROFILE_PHOTO_LOCAL_PATH);
            if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
                TempDataClass.profilePhotoLocalPath = profilePhotoPath;
                TempDataClass.profilePhotoServerPath = TempDataClass.BASE_URL + "MyImages/" + TempDataClass.serverUserId + ".jpg";
            } else {
                TempDataClass.profilePhotoLocalPath = "";
            }

            String profilePhotoServerPath = DeviceInfoDBHandler.GetValueForKey(getApplicationContext(), DeviceInfoKeys.PROFILE_PHOTO_SERVER_PATH);
            if (profilePhotoServerPath != null && !profilePhotoServerPath.isEmpty()) {
                TempDataClass.profilePhotoServerPath = profilePhotoServerPath;
            } else {
                if (TempDataClass.profilePhotoServerPath.isEmpty()) {
                    TempDataClass.profilePhotoServerPath = TempDataClass.BASE_URL + "MyImages/default.jpg";
                }
            }


            if (!UserModelDBHandler.CheckIfUserDataExists(getApplication().getApplicationContext())) {

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity
                        Bundle bundle;
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            bundle = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, mIcon, mIcon.getTransitionName()).toBundle();
                            startActivity(i,bundle);
                        }
                        else {
                            startActivity(i);
                        }

                        // close this activity
                        finish();
                    }
                }, 3000);
            } else {
                requestIds = RequestsDBHandler.GetAllRequestsBeforeThreeDays(getApplicationContext());

                if (requestIds != null) {
/*                    DeleteRequestsOnTheServer delete = new DeleteRequestsOnTheServer();
                    delete.execute(TempDataClass.BASE_URL + "Request/DeleteRequests");*/
                }

                try {
                    UserModel currentUser = UserModelDBHandler.ReturnValue(getApplicationContext());
                    TempDataClass.serverUserId = currentUser.ServerUserId;
                    TempDataClass.userName = currentUser.FirstName + " " + currentUser.LastName;
                    TempDataClass.emailId = currentUser.EmailId;
                } catch (Exception ex) {
                    //Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    //Log.e("DB", ex.getMessage());
                }

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);

                        // close this activity
                        finish();
                    }
                }, SPLASH_TIME_OUT);
            }
            mDisplay.append(regid);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

    }

    private boolean getCurrentLocation() {
        //Get current Location from LocationManager:
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
            return  true;
        } else {
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {


        lattitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());

        TempDataClass.currentLongitude = longitude;
        TempDataClass.currentLattitude = lattitude;

        mDisplay.append("Lattitude:" + lattitude + ", Longitude: " + longitude);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences("TeachMate",
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();

        DeviceInfoModel model = new DeviceInfoModel();
        model.Key = DeviceInfoKeys.REGISTRATION_ID;
        model.Value = regId;

        DeviceInfoDBHandler.InsertDeviceInfo(getApplicationContext(), model);
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    /*if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);*/

                    registrationId = MyFireBaseInstanceIdService.getToken();

                } catch (Exception ex) {
                    Log.e("ERROR", ex.getMessage());
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return registrationId;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + " I saw it here. \n");
                if(msg != null && msg.equals("")) {
                    TempDataClass.deviceRegId = msg;
                }
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            String json = "";
            JSONObject jsonObject = new JSONObject();

            JSONArray jsonArray = new JSONArray();

            for(int i = 0; i< requestIds.length; i++) {
                jsonArray.put(requestIds[i]);
            }

            jsonObject.put("RequestIds", jsonArray);

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
                result = "";

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

    private class DeleteRequestsOnTheServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
