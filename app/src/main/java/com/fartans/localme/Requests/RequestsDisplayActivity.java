package com.fartans.localme.Requests;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.fartans.localme.Base64;
import com.fartans.localme.DBHandlers.RequestsDBHandler;
import com.fartans.localme.FragmentTitles;
import com.fartans.localme.MainActivity;
import com.fartans.localme.R;
import com.fartans.localme.TempDataClass;
import com.fartans.localme.models.Requests;

import org.json.JSONArray;
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
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


public class RequestsDisplayActivity extends Fragment {

    private ListView listViewRequests;

    private ListAdapter listAdapter;

    private String newRequestString;

    private boolean isCurrentLocation;

    private ProgressDialog mProgressDialog;

    private Requests newRequest;

    private FragmentActivity activity;

    private RelativeLayout connectionLostLayout;

    private Button retryButton;

    private int index;

    private static List<Requests> resumeList = new ArrayList<Requests>();

    private boolean isFromOnResume = false;

    public int lastviewposition;
    public int listCurrentPosition;
    Button buttonUploadImage;

    private static int RESULT_LOAD_IMAGE = 2;

    public static String RequestImagePath = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (FragmentActivity) super.getActivity();
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.activity_requests_display, container, false);

        newRequest = new Requests();
        listViewRequests = (ListView) layout.findViewById(R.id.listViewRequests);
        retryButton = (Button) layout.findViewById(R.id.buttonRetry);
        connectionLostLayout = (RelativeLayout) layout.findViewById(R.id.layout_connectionLost);

        final Button loadMore = new Button(getActivity());
        loadMore.setBackgroundColor(Color.WHITE);
        loadMore.setTextColor(Color.BLACK);
        loadMore.setText(R.string.load_more_items);
        listViewRequests.addFooterView(loadMore);

        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true,getString(R.string.loading_requests));
                listCurrentPosition = listViewRequests.getFirstVisiblePosition();
                lastviewposition = listViewRequests.getCount()-1;
                String lastRequestId;
                try {
                    lastRequestId = resumeList.get(listViewRequests.getCount() - 2).RequestID;
                    new LoadmoreAPIAsyncTask().execute(TempDataClass.BASE_URL + "Request/GetAllRequestsAssigned?id=" + TempDataClass.serverUserId + "&lastRequestId=" + lastRequestId);
                }catch(Exception ex){
                    showProgress(false,null);
                    //Log.e("Request", ex.getMessage());
                }



                //Toast.makeText(getActivity().getApplicationContext(), "" + listViewRequests.getCount(), Toast.LENGTH_LONG).show();
            }
        });

        if(!isFromOnResume) {
            showProgress(true,getString(R.string.loading_requests));


            // if(new CommonMethods().hasActiveInternetConnection(activity)){
            if(true){
                HttpGetter getter = new HttpGetter();
                getter.execute(TempDataClass.BASE_URL + "Request/GetAllRequestsAssigned?id=" + TempDataClass.serverUserId + "&lastRequestId=0");
            }
            else{
                showProgress(false,null);
                listViewRequests.setVisibility(View.INVISIBLE);
                connectionLostLayout.setVisibility(View.VISIBLE);
            }
        }else{
            isFromOnResume = false;
        }

        /*        //Debug Code
        String result = "{'UserId':1,'Requests':[{'RequestId':1,'RequesteUserId':'2', 'RequestUserName':'Umang', 'RequestMessage':'Help me, baby!', 'RequestUserProfession':'Software Engineer', 'RequestUserProfilePhotoServerPath':'C:/profile.png', 'RequestedTime':'12/13/14 9.48 a.m.'},{'RequestId':2,'RequesteUserId':3, 'RequestUserName':'Anuj', 'RequestMessage':'Get me out of here', 'RequestUserProfession':'Priest', 'RequestUserProfilePhotoServerPath':'C:/profile.png', 'RequestedTime':'12/14/14 8.48 a.m.'}]}";
        List<Requests> list = GetObjectsFromResponse(result);
        if(list != null){
            populateListView(list);
            showProgress(false,null);
        }*/

        retryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new RequestsDisplayActivity())
                        .commit();
            }
        });

        return layout;

    }

    @Override
    public void onResume(){
        super.onResume();
        isFromOnResume = true;
        populateListView(resumeList);
        listViewRequests.setSelectionFromTop(index, 0);
    }

    @Override
    public void onPause(){
        super.onPause();
        index = listViewRequests.getFirstVisiblePosition();
    }

    private void populateListView(List<Requests> list) {

        final Requests[] requestsArray = new Requests[list.size()];
        for(int i = 0; i < list.size(); i++){
            requestsArray[i] = list.get(i);
        }
        listAdapter = new RequestsArrayAdapter(getActivity(), requestsArray);
        listViewRequests.setAdapter(listAdapter);

        listViewRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View
                    view, int position, long id) {

                try {
                    Bundle i = new Bundle();
                    i.putString("RequestID", requestsArray[position].RequestID);
                    i.putString("RequesteUserId", requestsArray[position].RequesteUserId);
                    i.putString("RequestUserName", requestsArray[position].RequestUserName);
                    i.putString("RequestString", requestsArray[position].RequestString);
                    i.putString("RequestUserProfession", requestsArray[position].RequestUserProfession);
                    i.putString("RequestUserProfilePhotoServerPath", requestsArray[position].RequestUserProfilePhotoServerPath);
                    i.putString("RequestTime", requestsArray[position].RequestTime);
                    i.putString("RequestImage", requestsArray[position].ImagePath);

                    Fragment individualRequestDisplayFragment = new RequestDisplayActivity();
                    individualRequestDisplayFragment.setArguments(i);

                    Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    TempDataClass.fragmentStack.lastElement().onPause();
                    TempDataClass.fragmentStack.push(currentFragment);
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, individualRequestDisplayFragment)
                            .commit();
                }
                catch(Exception ex){
                    //.makeText(getActivity().getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    Log.e("Request", ex.getMessage());
                }

            }

        });

    }

    private List<Requests> GetObjectsFromResponse(String response) {
        try {

            //JSONObject employee =(new JSONObject(response)).getJSONObject("Requests");
            JSONArray contacts = (new JSONObject(response)).getJSONArray("Requests");

            List<Requests> list = new ArrayList<Requests>();


            for(int i = 0; i < contacts.length(); i++){
                Requests request = new Requests();
                JSONObject temp = contacts.getJSONObject(i);

                request.RequestID = temp.getString("RequestId") != null ? temp.getString("RequestId") : null;
                request.RequestUserName = temp.getString("RequestUserName") != null ? temp.getString("RequestUserName"): null;
                request.RequestString = temp.getString("RequestMessage") != null ? temp.getString("RequestMessage"): null;
                request.RequesteUserId = temp.getString("RequesteUserId") != null ? temp.getString("RequesteUserId"): null;
                request.RequestTime = temp.getString("RequestedTime") != null ? temp.getString("RequestedTime"): null;
                request.RequestUserProfession = temp.getString("RequestUserProfession") != null ? temp.getString("RequestUserProfession"): null;
                request.RequestUserProfilePhotoServerPath = temp.getString("RequestUserProfilePhotoServerPath") != null ? temp.getString("RequestUserProfilePhotoServerPath"): null;
                request.ImagePath = temp.getString("RequestImageUrl") != null ? temp.getString("RequestImageUrl"): null;
                String isActive = temp.getString("IsActive") != null ? temp.getString("IsActive"): null;
                if(isActive != null && isActive.equals("True")){
                    request.IsActive = true;
                    list.add(request);
                }else{
                    request.IsActive = false;
                }


            }

            return list;
        }
        catch(Exception e){
            //Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Request", e.getMessage());
            return null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(FragmentTitles.REQUESTS);
        //TODO
    }

    /*@Override
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
    }*/

    public void GenerateNewRequest() {
        RequestImagePath = "";
        LayoutInflater li = LayoutInflater.from(getActivity());
        final View promptsView = li.inflate(R.layout.alert_prompt_new_request, null);

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
        //alertDialogBuilder.setMessage("Generate New Request!");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.custom_title_view, null);
        alertDialogBuilder.setCustomTitle(view);
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
                                isCurrentLocation = false;
                            }
                            else{
                                isCurrentLocation = true;
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
            if(!TextUtils.isEmpty(RequestImagePath)){
                buttonUploadImage.setText("Uploaded");
            }

                SelectedCursor.close();


            // image.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            //CommonMethods.scaleImage(getActivity().getApplicationContext(), image, 100);
            //Toast.makeText(getApplicationContext(), picturePath, Toast.LENGTH_SHORT).show();

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

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    private class HttpGetter extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // TODO Auto-generated method stub
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);
            String line = "";

            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(content));

                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    Log.v("Getter", "Your data: " + builder.toString()); //response data
                } else {
                    Log.e("Getter", "Failed to get data");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null && !result.isEmpty()) {
                if (!result.equals("Empty")) {
                    List<Requests> list = GetObjectsFromResponse(result);
                    resumeList = list;
                    if (list != null && list.size()!=0) {
                        populateListView(list);
                    }
                    showProgress(false,null);
                } else {
                    showProgress(false,null);
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                    builder1.setTitle("Alert!");
                    builder1.setMessage("No New Requests found in Server!");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }else{
                showProgress(false,null);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                builder1.setTitle("Alert!");
                builder1.setMessage("No New Requests found in Server!");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
    }

    public class LoadmoreAPIAsyncTask extends AsyncTask<String ,Void,String>{
        int current_position = listViewRequests.getFirstVisiblePosition();
        int lastpostiton = listViewRequests.getCount()-1;


        @Override
        protected String doInBackground(String... params) {
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(params[0]);
            String line = "";

            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(content));

                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    Log.d("Getter", "Your data: " + builder.toString()); //response data
                } else {
                    Log.e("Getter", "Failed to get data");
                }
            } catch (ClientProtocolException e) {
                Log.e("ClientProtocolException","ClientProtocolException in LoadmoreAPIAsyncTask requestdisplayactivity",e);
            } catch (IOException e) {
                Log.e("IOException","IOException in LoadmoreAPIAsyncTask requestdisplayactivity",e);
            }

            return builder.toString();
        }
        protected void onPostExecute(String result) {
            if(result != null && !result.isEmpty()) {
                if (!result.equals("Empty")) {
                    List<Requests> list = GetObjectsFromResponse(result);
                    if (list != null) {
                        AddToListView(list);
                    }
                    showProgress(false,null);
                } else {
                    showProgress(false,null);
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                    builder1.setTitle("Alert!");
                    builder1.setMessage("No more Requests found in Server!");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }else{
                showProgress(false,null);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                builder1.setTitle("Alert!");
                builder1.setMessage("No more Requests found in Server!");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
    }

    private void AddToListView(List<Requests> list) {
        resumeList.addAll(list);
        final Requests[] requestsArray = new Requests[resumeList.size()];
        for(int i = 0; i < resumeList.size(); i++){
            requestsArray[i] = resumeList.get(i);
        }
        listAdapter = new RequestsArrayAdapter(getActivity(), requestsArray);
        listViewRequests.setAdapter(listAdapter);

        listViewRequests.setSelectionFromTop(listCurrentPosition, 0);

        listViewRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View
                    view, int position, long id) {

                try {
                    Bundle i = new Bundle();
                    i.putString("RequestID", requestsArray[position].RequestID);
                    i.putString("RequesteUserId", requestsArray[position].RequesteUserId);
                    i.putString("RequestUserName", requestsArray[position].RequestUserName);
                    i.putString("RequestString", requestsArray[position].RequestString);
                    i.putString("RequestUserProfession", requestsArray[position].RequestUserProfession);
                    i.putString("RequestUserProfilePhotoServerPath", requestsArray[position].RequestUserProfilePhotoServerPath);
                    i.putString("RequestTime", requestsArray[position].RequestTime);
                    i.putString("RequestImage", requestsArray[position].ImagePath);

                    Fragment individualRequestDisplayFragment = new RequestDisplayActivity();
                    individualRequestDisplayFragment.setArguments(i);

                    Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    TempDataClass.fragmentStack.lastElement().onPause();
                    TempDataClass.fragmentStack.push(currentFragment);
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, individualRequestDisplayFragment)
                            .commit();
                }
                catch(Exception ex){
                    //Toast.makeText(getActivity().getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    Log.e("Request", ex.getMessage());
                }

            }

        });
    }

    private void showProgress(final boolean show,final String message) {
        if(mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        if(show) {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }

    }

}
