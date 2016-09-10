package com.fartans.localme.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.fartans.localme.Base64;
import com.fartans.localme.DBHandlers.RequestsDBHandler;
import com.fartans.localme.HomeFragment;
import com.fartans.localme.R;
import com.fartans.localme.Requests.RequestsDisplayActivity;
import com.fartans.localme.TempDataClass;
import com.fartans.localme.enums.RequestType;
import com.fartans.localme.models.Requests;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

/**
 *
 * Parent fragment for tabs in a tab host.
 * @author Hitesh Sethiya
 * Created by HiteshSethiya on 10/09/16.
 */
public class MainTabHosts extends Fragment {

    private FragmentTabHost mTabHost;
    private RequestType mTabTobeOpened;

    private ProgressDialog mProgressDialog;

    private Requests newRequest;

    private boolean isCurrentLocation;

    private String newRequestString;
    private static int RESULT_LOAD_IMAGE = 2;

    public static String RequestImagePath = "";


    /**
     * Create a new MainTabHosts fragment.
     * @param requestType to decide which tab should be selected.
     * @return new instance of @MainTabHosts
     */
    public static Fragment newInstance(RequestType requestType) {
        MainTabHosts mainTabHosts = new MainTabHosts();
        mainTabHosts.mTabTobeOpened = requestType;
        Bundle args = new Bundle();
        mainTabHosts.setArguments(args);
        return mainTabHosts;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        newRequest = new Requests();
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.fragment_tab_hosts);

        mTabHost.addTab(mTabHost.newTabSpec("simple").setIndicator("Local"),
                RequestsDisplayActivity.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("contacts").setIndicator("Vendor"),
                HomeFragment.class, null);

        mTabHost.setCurrentTab(getTabIndex(mTabTobeOpened));
        return mTabHost;
    }

    private Integer getTabIndex(RequestType requestType) {
        return RequestType.valueOf(requestType.name()).ordinal();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_requests_display, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_request) {
            GenerateNewRequest();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void GenerateNewRequest() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.alert_prompt_new_request, null);
        RequestImagePath = "";

        ArrayList<String> array = new ArrayList<String>();
        array.add("Registered Locations");
        array.add("Current Locations");
        final Spinner spinner1;
        ArrayAdapter<String> mAdapter;
        //spinner1= (Spinner) promptsView.findViewById(R.id.spinnerLocationSelector);
        final EditText requestEditText = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        //mAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.spinner_item, array);
        //mAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        //spinner1.setAdapter(mAdapter);

        final Switch locationSwicth = (Switch) promptsView.findViewById(R.id.switch1);

        final Button buttonUploadImage = (Button) promptsView.findViewById(R.id.buttonUploadImage);

        buttonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, 2);
            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setMessage("Generate New Request!");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if(requestEditText.getText().toString().equals("") || requestEditText.getText() == null){
                            Toast.makeText(getActivity(), "Please enter a Request Message!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            newRequestString = requestEditText.getText().toString();
                            //if(spinner1.getSelectedItem().toString().equals("Registered Locations")){
                            if(locationSwicth.isChecked()){
                                isCurrentLocation = true;
                            }
                            else{
                                isCurrentLocation = false;
                            }
                            showProgress(true,getString(R.string.generating_requests));
                            HttpAsyncTaskPOST newPost = new HttpAsyncTaskPOST();
                            newPost.execute(TempDataClass.BASE_URL + "Request/SendRequestNotification");
                        }
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void showProgress(final boolean show,final String message) {
        if(mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        if(show && isAdded()) {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }

    }

    private class HttpAsyncTaskPOST extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            newRequest.RequestID = result;
            newRequest.RequesteUserId = TempDataClass.serverUserId;
            newRequest.RequestUserName = TempDataClass.userName;
            newRequest.RequestUserProfession = TempDataClass.userProfession;
            newRequest.ImagePath = RequestImagePath;
            RequestsDBHandler.InsertRequests(getActivity().getApplicationContext(), newRequest);
            Toast.makeText(getActivity().getApplicationContext(), "Request Generated Successfully!", Toast.LENGTH_LONG).show();
            showProgress(false,null);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == -1 && null != data) {
            Uri SelectedImage = data.getData();
            String[] FilePathColumn = {MediaStore.Images.Media.DATA };

            Cursor SelectedCursor = getActivity().getContentResolver().query(SelectedImage, FilePathColumn, null, null, null);
            SelectedCursor.moveToFirst();

            int columnIndex = SelectedCursor.getColumnIndex(FilePathColumn[0]);
            String picturePath = SelectedCursor.getString(columnIndex);
            RequestImagePath = picturePath;
            SelectedCursor.close();

            // image.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            //CommonMethods.scaleImage(getActivity().getApplicationContext(), image, 100);
            //Toast.makeText(getApplicationContext(), picturePath, Toast.LENGTH_SHORT).show();

        }
    }

    public String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("UserId", TempDataClass.serverUserId);
            newRequest.RequesteUserId = TempDataClass.serverUserId;
            //TODO
            if(!RequestImagePath.equals("")) {
                Bitmap bitmap = BitmapFactory.decodeFile(RequestImagePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream); //compress to which format you want.
                final byte [] byte_arr = stream.toByteArray();
                final String image_str = Base64.encodeBytes(byte_arr);
                jsonObject.put("ImageArray", image_str);
            }
            jsonObject.put("RequestMessage", newRequestString);
            newRequest.RequestString = newRequestString;
            if(isCurrentLocation){
                jsonObject.put("IsCurrent", "true");
                jsonObject.put("Latitude", TempDataClass.currentLattitude);
                jsonObject.put("Longitude", TempDataClass.currentLongitude);
            }
            else {
                jsonObject.put("IsCurrent", "false");
                jsonObject.put("Longitude", 0);
                jsonObject.put("Longitude", 0);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            newRequest.RequestTime = currentDateandTime;
            jsonObject.put("TimeOfRequest", currentDateandTime);



            //Code to get current date and month
            Calendar calendar = Calendar.getInstance();
            int cYear = calendar.get(Calendar.YEAR);
            int cDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

            newRequest.requestYear = cYear;
            newRequest.requestDayOfTheYear = cDayOfYear;
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
