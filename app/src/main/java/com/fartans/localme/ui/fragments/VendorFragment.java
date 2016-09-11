package com.fartans.localme.ui.fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fartans.localme.R;
import com.fartans.localme.Requests.RequestDisplayActivity;
import com.fartans.localme.Requests.RequestsArrayAdapter;
import com.fartans.localme.TempDataClass;
import com.fartans.localme.models.Requests;
import com.fartans.localme.models.VendorRequests;
import com.fartans.localme.rest.RestClient;
import com.fasterxml.jackson.core.JsonParseException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * @author Hitesh Sethiya
 * Created by Hitesh Sethiya on 10/09/16.
 */
public class VendorFragment extends Fragment {

    private ListView mVendorRequestsListView;
    private RequestsArrayAdapter mRequestsAdapter;

    private Button mRetryButton;
    private RelativeLayout mConnectionLostLayout;
    private TextView mErrorView;

    private ProgressDialog mProgressDialog;
    private FragmentActivity mParentActivity;
    private static String mLastRequestId;
    public int listCurrentPosition;
    public int lastviewposition;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mParentActivity = getActivity();
        final RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.activity_requests_display, container, false);

        mVendorRequestsListView = (ListView) view.findViewById(R.id.listViewRequests);
        mRetryButton = (Button) view.findViewById(R.id.buttonRetry);
        mErrorView = (TextView) view.findViewById(R.id.textView6);
        mConnectionLostLayout = (RelativeLayout) view.findViewById(R.id.layout_connectionLost);

        final Button loadMore = new Button(getActivity());
        loadMore.setBackgroundColor(Color.WHITE);
        loadMore.setTextColor(Color.BLACK);
        loadMore.setText(R.string.load_more_items);
        mVendorRequestsListView.addFooterView(loadMore);
        mVendorRequestsListView.setOnItemClickListener(mVendorListItemClick);

        mRetryButton.setOnClickListener(mOnRetryClickListener);

        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true,getString(R.string.loading_requests));
                listCurrentPosition = mVendorRequestsListView.getFirstVisiblePosition();
                lastviewposition = mVendorRequestsListView.getCount()-1;
                getVendorRequests(mLastRequestId);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLastRequestId = "0";
        getVendorRequests(mLastRequestId);
    }

    private View.OnClickListener mOnRetryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getVendorRequests(mLastRequestId);
        }
    };

    private void getVendorRequests(final String lastRequestId) {
        if(!isAdded()) {
            return;
        }
        showProgress(true,"Loading Vendor Requests...");
        final String latitude = "".equals(TempDataClass.currentLattitude) ? "0" : TempDataClass.currentLattitude;
        final String longitude  = "".equals(TempDataClass.currentLongitude) ? "0" : TempDataClass.currentLongitude;
        final RestClient restClient = RestClient.getRestClient(getContext());
        restClient.getVendorService()
                .getVendorRequests(latitude,longitude,lastRequestId)
                .enqueue(mgetVendorsCallBack);
    }

    private Callback<VendorRequests> mgetVendorsCallBack = new Callback<VendorRequests>() {
        /**
         * {"Requests":[{"RequestUserName":"Pavan","RequesteUserId":1,"RequestMessage":"Need O positive blood","RequestedTime":"12:13:2 20/2/2016","RequestUserProfilePhotoServerPath":"http://teach-mate.azurewebsites.net/MyImages/default.jpg","RequestId":"1","RequestUserProfession":"malleswaram"}]}}
         */
        @Override
        public void onResponse(Response<VendorRequests> response, Retrofit retrofit) {
            showProgress(false,null);
            if(response.isSuccess() && response.body().getRequests() != null && !response.body().getRequests().isEmpty()) {
                appendToListAdapter(response.body().getRequests().toArray(new Requests[response.body().getRequests().size()]));
            } else if(response.body().getRequests() == null || response.body().getRequests().isEmpty()){
                showError(true,"No requests to view right now!.",true);
            } else {
                showError(true,"Something went wrong! Please try again.",true);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            showProgress(false,null);
            if(t instanceof JsonParseException) {
                Toast.makeText(getContext(),"No more requests to load!",Toast.LENGTH_SHORT).show();
            } else if(t instanceof SocketTimeoutException) {
                showError(true,"Slow or no internet connection!",true);
            } else {
                showError(true,"Something went wrong! Please try again.",true);
            }
        }
    };


    private void appendToListAdapter(Requests[] requestsArray) {
        if(mRequestsAdapter == null) {
            mRequestsAdapter = new RequestsArrayAdapter(getActivity(), requestsArray);
            mVendorRequestsListView.setAdapter(mRequestsAdapter);
            mLastRequestId = requestsArray[requestsArray.length - 1].RequestID;
        } else {
            List<Requests> appendList = new ArrayList<>(Arrays.asList(mRequestsAdapter.getValues()));
            for(int i = 0; i < requestsArray.length; ++i) {
                appendList.add(requestsArray[i]);
            }
            mRequestsAdapter = new RequestsArrayAdapter(getActivity(), appendList.toArray(new Requests[appendList.size()]));
            mVendorRequestsListView.setAdapter(mRequestsAdapter);
            mLastRequestId = appendList.get(appendList.size() - 1).RequestID;
        }
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

    private void showError(final boolean show, final String message, final boolean retry) {
        if(retry) {
            mRetryButton.setVisibility(View.VISIBLE);
        } else {
            mRetryButton.setVisibility(View.GONE);
        }

        if(show) {
            mVendorRequestsListView.setVisibility(View.GONE);
            mErrorView.setText(message);
            mConnectionLostLayout.setVisibility(View.VISIBLE);
        } else {
            mConnectionLostLayout.setVisibility(View.GONE);
            mVendorRequestsListView.setVisibility(View.VISIBLE);
        }
        showProgress(false,null);
    }

    private AdapterView.OnItemClickListener mVendorListItemClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View
        view, int position, long id) {

            try {
                Bundle i = new Bundle();
                i.putString("RequestID", mRequestsAdapter.getValues()[position].RequestID);
                i.putString("RequesteUserId", mRequestsAdapter.getValues()[position].RequesteUserId);
                i.putString("RequestUserName", mRequestsAdapter.getValues()[position].RequestUserName);
                i.putString("RequestString", mRequestsAdapter.getValues()[position].RequestString);
                i.putString("RequestUserProfession", mRequestsAdapter.getValues()[position].RequestUserProfession);
                i.putString("RequestUserProfilePhotoServerPath", mRequestsAdapter.getValues()[position].RequestUserProfilePhotoServerPath);
                i.putString("RequestTime", mRequestsAdapter.getValues()[position].RequestTime);
                i.putString("RequestImage", mRequestsAdapter.getValues()[position].ImagePath);

                Fragment individualRequestDisplayFragment = new RequestDisplayActivity();
                individualRequestDisplayFragment.setArguments(i);

                Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);

                FragmentManager fragmentManager = mParentActivity.getSupportFragmentManager();
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

    };

}
