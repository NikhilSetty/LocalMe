package com.fartans.localme.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.fartans.localme.R;

/**
 * Created by sattvamedtech on 10/09/16.
 */
public class VendorFragment extends Fragment {

    private ProgressDialog mProgressDialog;
    private FragmentActivity mParentActivity;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mParentActivity = getActivity();
        final RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.activity_requests_display, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
