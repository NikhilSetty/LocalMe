package com.fartans.localme.rest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.fartans.localme.TempDataClass;
import com.fartans.localme.rest.service.IVendorService;
import com.squareup.okhttp.Request;

import retrofit.Retrofit;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;

import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by HiteshSethiya on 10/09/16.
 */
public class RestClient {

    private static final String TAG = RestClient.class.getName();
    private static final String acceptHeader = "application/json";
    private static final String USER_AGENT = "User-Agent";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ACCEPT_CONTENT = "Accept";
    private static String AGENT_HEADER;
    private IVendorService iVendorService;

    private static RestClient restClient;
    private Retrofit retrofit;
    private Context mContext;

    /**
     * It is not a good idea to make an instance of RestClient everytime. So making this class a singleton.
     * Good practice: Use dependency Injection to make an object of RestClient.
     * Creating an object of RestClient is an EXTREMELY EXPENSIVE OPERATION
     */
    private RestClient(Context context) {
        mContext = context;
        String androidOS = Build.VERSION.RELEASE;
        try {
            AGENT_HEADER = "PakettsApp/"+ context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName +" (ANDROID/"+androidOS+")";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,"PackageManager.NameNotFoundException - setting AGENT HEADER to default.",e);
            AGENT_HEADER = "PakettsApp/1.0.0 "+"(ANDROID/"+androidOS+")";
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        //Follow all redirects
        okHttpClient.setFollowRedirects(true);
        okHttpClient.setFollowSslRedirects(true);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        okHttpClient.interceptors().add(requestInterceptor);

        //TODO intercept response

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient.interceptors().add(logging);

        this.retrofit = new Retrofit.Builder()
                .baseUrl(TempDataClass.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        iVendorService = getRetrofit().create(IVendorService.class);
    }

    Interceptor requestInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder requestBuilder = chain.request().newBuilder();
/*
            final String authToken = sharedPreference.getAuthToken();
            final String cookie = sharedPreferences.getString(Constants.COOKIE, null);
            if(authToken != null) {
                requestBuilder
                        .addHeader(BaseActivity.AUTHORIZATION,authToken);
            }*/
            requestBuilder.addHeader(USER_AGENT,AGENT_HEADER);
            Request newRequest = requestBuilder
                    //Retrofit by default adds accept encoding gzip
                    //.addHeader(ACCEPT_ENCODING,"gzip, deflate")
                    .addHeader(ACCEPT_CONTENT, acceptHeader).build();
            return chain.proceed(newRequest);
        }
    };

    public static synchronized RestClient getRestClient(Context context) {
        if(restClient == null) {
            restClient = new RestClient(context);
        }
        return restClient;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public Context getContext() {
        return mContext;
    }

    public IVendorService getVendorService() {
        return iVendorService;
    }
}
