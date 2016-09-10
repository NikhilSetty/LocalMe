package com.fartans.localme.rest.service;

import com.fartans.localme.models.Requests;
import com.fartans.localme.models.VendorRequests;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * @author Hitesh sethiya
 * Created by Hitesh Sethiya on 10/09/16.
 */
public interface IVendorService {

    String VENDORS = "Request/getvendorrequests";

    @GET(VENDORS)
    Call<VendorRequests> getVendorRequests(@Query("latitude") String latitude,
                                           @Query("longitude") String longitude,
                                           @Query("lastRequestId") String lastRequestId);
}
