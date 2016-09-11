package com.fartans.localme.Requests;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fartans.localme.DBHandlers.RequestsDBHandler;
import com.fartans.localme.FragmentTitles;
import com.fartans.localme.MainActivity;
import com.fartans.localme.R;
import com.fartans.localme.Responses.ResponseDisplayActivity;
import com.fartans.localme.TempDataClass;
import com.fartans.localme.models.Requests;
import com.fartans.localme.models.Responses;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


public class ViewResponsesForARequest extends Fragment {

    FragmentActivity activity;

    Requests currentRequest;

    TextView requestString;
    TextView requestTime;

    ListView listViewResponses;
    ListAdapter listAdapter;
    ProgressDialog progressDialog;

    RelativeLayout listViewLayout;
    RelativeLayout errorLayout;

    AlertDialog alertDialog;

    public ViewResponsesForARequest() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        activity = (FragmentActivity) super.getActivity();
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_view_responses_for_arequest, container, false);

        listViewLayout = (RelativeLayout) layout.findViewById(R.id.view_responses_list_view);
        errorLayout = (RelativeLayout) layout.findViewById(R.id.no_response_view);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Looking for Responses...");
        progressDialog.show();

        listViewResponses = (ListView) layout.findViewById(R.id.listViewMyRequestsResponsesDisplay);

        currentRequest = new Requests();

        Bundle args = getArguments();

        currentRequest.RequestID = args.getString("RequestID");
        currentRequest.RequesteUserId = args.getString("RequesteUserId");
        currentRequest.RequestUserName = args.getString("RequestUserName");
        currentRequest.RequestString = args.getString("RequestString");
        currentRequest.RequestUserProfession = args.getString("RequestUserProfession");
        currentRequest.RequestUserProfilePhotoServerPath= args.getString("RequestUserProfilePhotoServerPath");
        currentRequest.RequestTime = args.getString("RequestTime");

        requestString = (TextView) layout.findViewById(R.id.textViewMyRequestString);
        requestTime = (TextView) layout.findViewById(R.id.textViewMyRequestTime);

        requestString.setText(currentRequest.RequestString);
        requestTime.setText(currentRequest.RequestTime);

        HttpGetter getter = new HttpGetter();
        getter.execute(TempDataClass.BASE_URL + "Response/GetAllResponsesForARequest?id="+ currentRequest.RequestID+"&lastResponseId=0");

        return layout;
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
            if(result != null && !result.isEmpty()){
                if(!result.equals("empty")){
                    listViewLayout.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                    List<Responses> list = GetObjectsFromResponse(result);
                    if(list != null){
                        populateListView(list);
                    }
                    progressDialog.dismiss();
                }
            }
            else{
                progressDialog.dismiss();

                listViewLayout.setVisibility(View.INVISIBLE);
                errorLayout.setVisibility(View.VISIBLE);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                builder1.setTitle("Alert!");
                builder1.setMessage("No Responses available!");
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_request_display, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_mark_as_inactive){

            alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Mark Request as Inactive");
            alertDialog.setMessage("Are you sure you want to mark this request as inactive?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setCancelable(false);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setTitle("Deactivating Request!");
                            progressDialog.show();
                            MarkRequestAsInactive inactive = new MarkRequestAsInactive();
                            inactive.execute(TempDataClass.BASE_URL + "Request/DeactivateRequest?id=" + currentRequest.RequestID);
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateListView(List<Responses> list) {

        final Responses[] responsesArray = new Responses[list.size()];
        for(int i = 0; i < list.size(); i++){
            responsesArray[i] = list.get(i);
        }
        listAdapter = new ResponsesArrayAdapter(getActivity(), responsesArray);
        listViewResponses.setAdapter(listAdapter);

        listViewResponses.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View
                    view, int position, long id) {

                try {
                    Bundle i = new Bundle();
                    i.putString("RequestId", responsesArray[position].RequestId);
                    i.putString("RequestString", currentRequest.RequestString);
                    i.putString("RequestTime", currentRequest.RequestTime);
                    i.putString("ResponseId", responsesArray[position].ResponseId);
                    i.putString("ResponseString", responsesArray[position].ResponseString);
                    i.putString("ResponseTime", responsesArray[position].ResponseTime);
                    i.putString("ResponseUserId", responsesArray[position].ResponseUserId);
                    i.putString("ResponseUserName", responsesArray[position].ResponseUserName);
                    i.putString("ResponseUserProfession", responsesArray[position].ResponseUserProfession);
                    i.putString("ResponseUserProfilePhotoServerPath", responsesArray[position].ResponseUserProfilePhotoServerPath);

                    Fragment individualRequestDisplayFragment = new ResponseDisplayActivity();
                    individualRequestDisplayFragment.setArguments(i);

                    Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    TempDataClass.fragmentStack.lastElement().onPause();
                    TempDataClass.fragmentStack.push(currentFragment);
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, individualRequestDisplayFragment)
                            .commit();
                } catch (Exception ex) {
                    //Toast.makeText(getActivity().getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    Log.e("Request", ex.getMessage());
                }

            }

        });

    }

    private List<Responses> GetObjectsFromResponse(String result) {
        try {
            JSONArray contacts = (new JSONObject(result)).getJSONArray("Responses");

            List<Responses> list = new ArrayList<Responses>();

            for(int i = 0; i < contacts.length(); i++){
                Responses response = new Responses();
                JSONObject temp = contacts.getJSONObject(i);

                response.RequestId = currentRequest.RequestID;
                response.ResponseId= temp.getString("ResponseId") != null ? temp.getString("ResponseId"): null;
                response.ResponseString= temp.getString("ResponseString") != null ? temp.getString("ResponseString"): null;
                response.ResponseUserId = temp.getString("ResponseUserId") != null ? temp.getString("ResponseUserId"): null;
                response.ResponseUserName = temp.getString("ResponseUserName") != null ? temp.getString("ResponseUserName"): null;
                response.ResponseUserProfession = temp.getString("ResponseUserProfession") != null ? temp.getString("ResponseUserProfession"): null;
                response.ResponseUserProfilePhotoServerPath = temp.getString("ResponseUserProfilePhotoServerPath") != null ? temp.getString("ResponseUserProfilePhotoServerPath"): null;

                list.add(response);

            }

            return list;
        }
        catch(Exception e){
            //.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Request", e.getMessage());
            return null;
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(FragmentTitles.RESPONSES);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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

    private class MarkRequestAsInactive extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // TODO Auto-generated method stub
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);
            String line = "";
            RequestsDBHandler.DeleteRequest(activity, Integer.parseInt(currentRequest.RequestID) );

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
                if(!(TempDataClass.fragmentStack.size() == 0)) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.container, TempDataClass.fragmentStack.lastElement());
                    TempDataClass.fragmentStack.pop();
                    ft.commit();
                    alertDialog.dismiss();
                    progressDialog.dismiss();
                }
                else{
                    activity.finish();
                }
            }
        }
    }
}
