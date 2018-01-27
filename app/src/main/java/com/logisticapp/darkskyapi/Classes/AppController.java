package com.logisticapp.darkskyapi.Classes;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by daniel on 23/01/18.
 */

public class AppController {

    private static AppController mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;

    private AppController(Context context){
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    //Si una instancia ya esta creada, se regresara.
    //En caso contrario, se creara una y se retornará.
    public static synchronized  AppController getInstance(Context context){
        if (mInstance == null){
            mInstance = new AppController(context);
        }

        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public void addToRequestQueue(@NonNull final Request request){
        getRequestQueue().add(request);
    }

    public void addToRequestQueueWithTag(@NonNull final Request request, String tag){
        request.setTag(tag);
        getRequestQueue().add(request);
    }
}
