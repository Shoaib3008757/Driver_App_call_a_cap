package com.texi;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.JsonObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.texi.Adapter.DriverAllTripAdapter;
import com.texi.utils.Common;
import com.texi.utils.DriverAllTripFeed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.protocol.HTTP;

public class DriverTripActivity extends AppCompatActivity implements DriverAllTripAdapter.OnAllTripClickListener  {


    SlidingMenu slidingMenu;

    RelativeLayout layout_slidemenu;
    TextView txt_all_trip;
    RelativeLayout layout_filter;
    RecyclerView recycle_all_trip;
    SwipeRefreshLayout swipe_refresh_layout;
    RelativeLayout layout_background;
    RelativeLayout layout_no_recourd_found;
    LinearLayout layout_recycleview;

    Typeface OpenSans_Bold,OpenSans_Regular;
    private RecyclerView.LayoutManager AllTripLayoutManager;

    GPSTracker gps;
    double latitude;
    double longitude;
    private static final String SERVER_IP = "http://162.243.225.225:4040";
    private Socket mSocket;
    Switch driver_status;
    TextView switch_driver_status;

    ArrayList<DriverAllTripFeed> DriverAllTripArray;
    DriverAllTripAdapter DrvAllTripAdapter;

    Common common = new Common();
    LoaderView loader;
    Dialog filterDialog;
    String FilterString = "";

    CheckBox chk_all;
    CheckBox chk_pen_book;
    CheckBox chk_acp_book;
    CheckBox chk_drv_can;
    CheckBox chk_com_book;
    boolean checkAllClick = false;

    SharedPreferences userPref;

    static final int DETAIL_REQUEST = 1;

    BroadcastReceiver receiver;
    Bundle savedInstState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstState = savedInstanceState;
        setContentView(R.layout.activity_driver_trip);

        userPref = PreferenceManager.getDefaultSharedPreferences(DriverTripActivity.this);

        layout_slidemenu = (RelativeLayout)findViewById(R.id.layout_slidemenu);
        txt_all_trip = (TextView)findViewById(R.id.txt_all_trip);
        layout_filter = (RelativeLayout)findViewById(R.id.layout_filter);
        recycle_all_trip = (RecyclerView)findViewById(R.id.recycle_all_trip);
        swipe_refresh_layout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        layout_background = (RelativeLayout)findViewById(R.id.layout_background);
        layout_no_recourd_found = (RelativeLayout)findViewById(R.id.layout_no_recourd_found);
        layout_recycleview = (LinearLayout)findViewById(R.id.layout_recycleview);

        loader=new LoaderView(DriverTripActivity.this);

        OpenSans_Bold = Typeface.createFromAsset(getAssets(), getString(R.string.font_bold_opensans));
        OpenSans_Regular = Typeface.createFromAsset(getAssets(), getString(R.string.font_regular_opensans));

        txt_all_trip.setTypeface(OpenSans_Bold);

        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setBehindOffsetRes(R.dimen.slide_menu_width);
        slidingMenu.setFadeDegree(0.20f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.left_menu);

        gps = new GPSTracker(DriverTripActivity.this);
        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }else{
            gps.showSettingsAlert();
        }

        common.SlideMenuDesign(slidingMenu, DriverTripActivity.this,"my trip");

        driver_status = (Switch)slidingMenu.findViewById(R.id.switch_driver_status);
        switch_driver_status = (TextView)slidingMenu.findViewById(R.id.txt_driver_status);
        if(mSocket != null && mSocket.connected() || Common.socket != null && Common.socket.connected()) {
            driver_status.setChecked(true);
            switch_driver_status.setText(getResources().getString(R.string.on_duty));
        }else {
            driver_status.setChecked(false);
            switch_driver_status.setText(getResources().getString(R.string.off_duty));
        }
        driver_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                Log.d("is Checked","is Checked = "+b+"=="+userPref.getBoolean("isBookingAccept",false));
                if(b){
                    switch_driver_status.setText(getResources().getString(R.string.on_duty));
                    if(gps.canGetLocation()) {
                        try {
                            mSocket = IO.socket(SERVER_IP);
                            mSocket.emit(com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT_ERROR, onConnectError);
                            mSocket.connect();
                            Common.socket = mSocket;
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            Log.d("connected ", "connected error = " + e.getMessage());
                        }

                        Common.SocketFunction(DriverTripActivity.this,mSocket,driver_status,latitude,longitude,common,userPref);
                    }else{
                        switch_driver_status.setText(getResources().getString(R.string.off_duty));
                        driver_status.setChecked(false);
                        gps.showSettingsAlert();

                        common.ChangeLocationSocket(DriverTripActivity.this,driver_status);
                    }
                }else{
                    switch_driver_status.setText(getResources().getString(R.string.off_duty));
                    try {
                        JSONArray locAry = new JSONArray();
                        locAry.put(latitude);
                        locAry.put(longitude);
                        JSONObject emitobj = new JSONObject();
                        emitobj.put("coords",locAry);
                        emitobj.put("driver_name",userPref.getString("user_name",""));
                        emitobj.put("driver_id", userPref.getString("id",""));
                        emitobj.put("driver_status", "0");
                        emitobj.put("car_type",userPref.getString("car_type",""));
                        emitobj.put("isdevice","1");
                        emitobj.put("booking_status",userPref.getString("booking_status",""));
                        Log.d("emitobj", "emitobj = " + emitobj);

                        Common.socket.emit("Create Driver Data", emitobj );
                        Common.socket.disconnect();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    common.ChangeLocationSocket(DriverTripActivity.this,driver_status);
                }
            }
        });

        recycle_all_trip = (RecyclerView)findViewById(R.id.recycle_all_trip);
        swipe_refresh_layout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        layout_recycleview = (LinearLayout)findViewById(R.id.layout_recycleview);

        AllTripLayoutManager = new LinearLayoutManager(this);
        recycle_all_trip.setLayoutManager(AllTripLayoutManager);


        layout_slidemenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingMenu.toggle();
            }
        });

        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Common.isNetworkAvailable(DriverTripActivity.this)) {
                    recycle_all_trip.setClickable(false);
                    recycle_all_trip.setEnabled(false);
                    boolean allFilter = false;
                    if (userPref.getBoolean("setFilter", false) == true) {
                        if (userPref.getInt("pending booking", 4) == 0) {
                            FilterString += 0 + ",";
                            allFilter = true;
                        }
                        if (userPref.getInt("accepted booking", 4) == 1) {
                            FilterString += 1 + ",";
                            allFilter = true;
                        }
                        if (userPref.getInt("driver cancel", 4) == 2) {
                            FilterString += 2 + ",";
                            allFilter = true;
                        }
                        if (userPref.getInt("completed booking", 4) == 3) {
                            FilterString += 3 + ",";
                            allFilter = true;
                        }
                        if(FilterString.length() > 0)
                            FilterString = FilterString.substring(0, (FilterString.length() - 1));

                        SharedPreferences.Editor clickfilter = userPref.edit();
                        clickfilter.putBoolean("setFilter", allFilter);
                        clickfilter.commit();

                        if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3){
                            FilterString = "";
                        }
                        FilterAllDriverTrips(0,"",true);
                        FilterString = "";

                    }else {
                        getDriverAllTrip(0,true);
                    }
                } else {
                    recycle_all_trip.setClickable(true);
                    recycle_all_trip.setEnabled(true);
                    //Network is not available
                    Common.showInternetInfo(DriverTripActivity.this, "Network is not available");
                }
            }
        });

        //loader.show();
        if(!userPref.getString("id_device_token","").equals("1")) {
            if(Utility.isNetworkAvailable(DriverTripActivity.this)) {
                new CallUnsubscribe(DriverTripActivity.this, Common.device_token).execute();
            }else {
                Utility.showInternetInfo(DriverTripActivity.this, "Network is not available");
            }
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Common.isNetworkAvailable(DriverTripActivity.this)) {
                        boolean allFilter = false;
                        if (userPref.getBoolean("setFilter", false) == true) {
                            if (userPref.getInt("pending booking", 4) == 0) {
                                FilterString += 0 + ",";
                                allFilter = true;
                            }
                            if (userPref.getInt("accepted booking", 4) == 1) {
                                FilterString += 1 + ",";
                                allFilter = true;
                            }
                            if (userPref.getInt("driver cancel", 4) == 2) {
                                FilterString += 2 + ",";
                                allFilter = true;
                            }
                            if (userPref.getInt("completed booking", 4) == 3) {
                                FilterString += 3 + ",";
                                allFilter = true;
                            }
                            if(FilterString.length() > 0)
                                FilterString = FilterString.substring(0, (FilterString.length() - 1));

                            SharedPreferences.Editor clickfilter = userPref.edit();
                            clickfilter.putBoolean("setFilter", allFilter);
                            clickfilter.commit();

                            if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3){
                                FilterString = "";
                            }
                            FilterAllDriverTrips(0,"filter",false);
                            FilterString = "";

                        }else {
                            getDriverAllTrip(0,false);
                        }
                    } else {
                        Common.showInternetInfo(DriverTripActivity.this, "Network is not available");
                        swipe_refresh_layout.setEnabled(false);
                    }
                }
            }, 500);
        }


        /*Filter Dialog Start*/

        filterDialog = new Dialog(DriverTripActivity.this,R.style.DialogSlideAnim);
        filterDialog.setContentView(R.layout.all_trip_filter_dialog);

        layout_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_background.setVisibility(View.VISIBLE);
                filterDialog.show();
            }
        });

        TextView txt_all = (TextView)filterDialog.findViewById(R.id.txt_all);
        txt_all.setTypeface(OpenSans_Regular);
        TextView txt_pending_booking = (TextView)filterDialog.findViewById(R.id.txt_pending_booking);
        txt_pending_booking.setTypeface(OpenSans_Regular);
        TextView txt_accept_booking = (TextView)filterDialog.findViewById(R.id.txt_accept_booking);
        txt_accept_booking.setTypeface(OpenSans_Regular);
        TextView txt_drv_can = (TextView)filterDialog.findViewById(R.id.txt_drv_can);
        txt_drv_can.setTypeface(OpenSans_Regular);
        TextView txt_com_book = (TextView)filterDialog.findViewById(R.id.txt_com_book);
        txt_com_book.setTypeface(OpenSans_Regular);

        chk_all = (CheckBox)filterDialog.findViewById(R.id.chk_all);
        RelativeLayout layout_all_check = (RelativeLayout)filterDialog.findViewById(R.id.layout_all_check);
        CheckBoxChecked(layout_all_check, chk_all, "all");

        chk_pen_book = (CheckBox)filterDialog.findViewById(R.id.chk_pen_book);
        RelativeLayout layour_pen_book_check = (RelativeLayout)filterDialog.findViewById(R.id.layour_pen_book_check);
        CheckBoxChecked(layour_pen_book_check, chk_pen_book, "pending booking");

        chk_acp_book = (CheckBox)filterDialog.findViewById(R.id.chk_acp_book);
        RelativeLayout layout_acp_book_check = (RelativeLayout)filterDialog.findViewById(R.id.layout_acp_book_check);
        CheckBoxChecked(layout_acp_book_check, chk_acp_book, "accept booking");

        chk_com_book = (CheckBox)filterDialog.findViewById(R.id.chk_com_book);
        RelativeLayout layout_com_book_check = (RelativeLayout)filterDialog.findViewById(R.id.layout_com_book_check);
        CheckBoxChecked(layout_com_book_check, chk_com_book, "completed booking");

        chk_drv_can = (CheckBox)filterDialog.findViewById(R.id.chk_drv_can);
        RelativeLayout layout_drv_reject_check = (RelativeLayout)filterDialog.findViewById(R.id.layout_drv_reject_check);
        CheckBoxChecked(layout_drv_reject_check, chk_drv_can, "driver cancel");

        Log.d("checkbox checked","checkbox checked = "+userPref.getInt("pending booking",4)+"=="+userPref.getInt("accepted booking",4)+"=="+userPref.getInt("driver cancel",4)+"=="+userPref.getInt("completed booking",4));
        if(userPref.getInt("pending booking",4) == 0)
            chk_pen_book.setChecked(true);
        if(userPref.getInt("accepted booking",4) == 1)
            chk_acp_book.setChecked(true);
        if(userPref.getInt("driver cancel",4) == 2)
            chk_drv_can.setChecked(true);
        if(userPref.getInt("completed booking",4) == 3)
            chk_com_book.setChecked(true);

        if(userPref.getInt("pending booking",4) == 0 && userPref.getInt("accepted booking",4) == 1 && userPref.getInt("driver cancel",4) == 2 && userPref.getInt("completed booking",4) == 3){
            chk_all.setChecked(true);
        }

        ImageView img_close_icon = (ImageView)filterDialog.findViewById(R.id.img_close_icon);
        img_close_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layout_background.setVisibility(View.GONE);
                filterDialog.cancel();
            }
        });

        ImageView img_calcel = (ImageView)filterDialog.findViewById(R.id.img_calcel);
        img_calcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_background.setVisibility(View.GONE);
                filterDialog.cancel();
            }
        });

        ImageView img_apply = (ImageView) filterDialog.findViewById(R.id.img_apply);
        img_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_background.setVisibility(View.GONE);
                filterDialog.cancel();
                boolean setFilter = false;

                SharedPreferences.Editor pending_booking = userPref.edit();
                if(chk_pen_book.isChecked()) {
                    FilterString += 0 + ",";
                    pending_booking.putInt("pending booking", 0);
                    setFilter = true;
                }else
                    pending_booking.putInt("pending booking", 4);
                pending_booking.commit();

                SharedPreferences.Editor accepted_booking = userPref.edit();
                if(chk_acp_book.isChecked()){
                    FilterString += 1 + ",";
                    accepted_booking.putInt("accepted booking",1);
                    setFilter = true;
                }else{
                    accepted_booking.putInt("accepted booking", 4);
                }
                accepted_booking.commit();

                SharedPreferences.Editor driver_cancel = userPref.edit();
                if(chk_drv_can.isChecked()){
                    FilterString += 2 + ",";
                    driver_cancel.putInt("driver cancel",2);
                    setFilter = true;
                }else{
                    driver_cancel.putInt("driver cancel", 4);
                }
                driver_cancel.commit();

                SharedPreferences.Editor completed_booking = userPref.edit();
                if(chk_com_book.isChecked()){
                    FilterString += 3 + ",";
                    completed_booking.putInt("completed booking",3);
                    setFilter = true;
                }else{
                    completed_booking.putInt("completed booking", 4);
                }
                completed_booking.commit();

                Log.d("FilterString","FilterString = "+FilterString);
                if(FilterString.length() > 0)
                    FilterString = FilterString.substring(0, (FilterString.length() - 1));

                Log.d("FilterString","FilterString = "+FilterString);
                SharedPreferences.Editor clickfilter = userPref.edit();
                clickfilter.putBoolean("setFilter", setFilter);
                clickfilter.commit();

                if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3){
                    FilterString = "";
                }

                loader.show();
                FilterAllDriverTrips(0,"filter",true);

                FilterString = "";
            }
        });
    }

    @Override
    public void AcceptCabBookin(final int position) {
        if(DriverAllTripArray.size() > 0) {
            loader.show();
            final DriverAllTripFeed driverAllTripFeed = DriverAllTripArray.get(position);
            String DrvBookingUrl = Url.DriverAcceptTripUrl + "?booking_id=" + driverAllTripFeed.getId() + "&driver_id=" + userPref.getString("id", "");
            Log.d("DrvBookingUrl", "DrvBookingUrl =" + DrvBookingUrl);
            Ion.with(DriverTripActivity.this)
                    .load(DrvBookingUrl)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception error, JsonObject result) {
                            // do stuff with the result or error
                            Log.d("load_trips result", "load_trips result = " + result + "==" + error);
                            loader.cancel();
                            if (error == null) {
                                try {
                                    JSONObject resObj = new JSONObject(result.toString());

                                    if(resObj.getString("status").equals("success")) {

                                        SharedPreferences.Editor booking_status = userPref.edit();
                                        booking_status.putString("booking_status","Accepted");
                                        booking_status.commit();

                                        driverAllTripFeed.setDriverFlag("1");
                                        driverAllTripFeed.setStatus("3");
                                        DrvAllTripAdapter.notifyItemChanged(position);
                                    }else if(resObj.getString("status").equals("false")){
                                        Common.showMkError(DriverTripActivity.this,resObj.getString("error code").toString());

                                        if(resObj.has("Isactive") && resObj.getString("Isactive").equals("Inactive")) {

                                            SharedPreferences.Editor editor = userPref.edit();
                                            editor.clear();
                                            editor.commit();

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent = new Intent(DriverTripActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }, 2500);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                Common.ShowHttpErrorMessage(DriverTripActivity.this, error.getMessage());
                            }
                        }
                    });

        }
    }

    @Override
    public void RejectCabBookin(int position, String timerStart) {
        if(DriverAllTripArray.size() > 0) {
            loader.show();
            Log.d("position","position = "+position);
            final DriverAllTripFeed driverAllTripFeed = DriverAllTripArray.get(position);
            final String DrvRejectUrl = Url.DriverRejectTripUrl + "?booking_id=" + driverAllTripFeed.getId() + "&driver_id=" + userPref.getString("id", "");
            Log.d("DrvRejectUrl", "DrvRejectUrl = " + DrvRejectUrl);
            if(timerStart.equals("timer reject")){
                Ion.with(DriverTripActivity.this)
                        .load(DrvRejectUrl)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception error, JsonObject result) {
                                // do stuff with the result or error
                                Log.d("load_trips result", "load_trips result = " + result + "==" + error);
                                loader.cancel();


                                if (error == null) {

                                    try {
                                        JSONObject resObj = new JSONObject(result.toString());

                                        if(resObj.getString("status").equals("success")) {

                                            SharedPreferences.Editor booking_status = userPref.edit();
                                            booking_status.putString("booking_status","Rejected");
                                            booking_status.commit();

                                            driverAllTripFeed.setDriverFlag("2");
                                            driverAllTripFeed.setStatus("5");
                                            DrvAllTripAdapter.updateItems();
                                        }else if(resObj.getString("status").equals("false")){
                                            Common.showMkError(DriverTripActivity.this,resObj.getString("error code").toString());

                                            if(resObj.has("Isactive") && resObj.getString("Isactive").equals("Inactive")) {

                                                SharedPreferences.Editor editor = userPref.edit();
                                                editor.clear();
                                                editor.commit();

                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(DriverTripActivity.this, MainActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }, 2500);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    Common.ShowHttpErrorMessage(DriverTripActivity.this, error.getMessage());
                                }
                            }
                        });
            }else {
                Ion.with(DriverTripActivity.this)
                        .load(DrvRejectUrl)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception error, JsonObject result) {
                                // do stuff with the result or error
                                Log.d("load_trips result", "load_trips result = " + result + "==" + error);
                                loader.cancel();
                                if (error == null) {
                                    driverAllTripFeed.setDriverFlag("2");
                                    driverAllTripFeed.setStatus("5");
                                    DrvAllTripAdapter.updateItems();
                                } else {
                                    Common.ShowHttpErrorMessage(DriverTripActivity.this, error.getMessage());
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void scrollToLoad(int position) {
        boolean allFilter = false;
        if (userPref.getBoolean("setFilter", false) == true) {
            if (userPref.getInt("pending booking", 4) == 0) {
                FilterString += 0 + ",";
                allFilter = true;
            }
            if (userPref.getInt("accepted booking", 4) == 1) {
                FilterString += 1 + ",";
                allFilter = true;
            }
            if (userPref.getInt("driver cancel", 4) == 2) {
                FilterString += 2 + ",";
                allFilter = true;
            }
            if (userPref.getInt("completed booking", 4) == 3) {
                FilterString += 3 + ",";
                allFilter = true;
            }
            if(FilterString.length() > 0)
                FilterString = FilterString.substring(0, (FilterString.length() - 1));

            SharedPreferences.Editor clickfilter = userPref.edit();
            clickfilter.putBoolean("setFilter", allFilter);
            clickfilter.commit();

            if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3){
                FilterString = "";
            }
            FilterAllDriverTrips(position+1,"",true);
            FilterString = "";
        }else {
            getDriverAllTrip(position+1,true);
        }
    }

    @Override
    public void GoTripDetail(int position) {
        if(DriverAllTripArray.size() > 0) {
            Common.driverAllTripFeed = DriverAllTripArray.get(position);
            Intent di = new Intent(DriverTripActivity.this, DriverTripDetailActivity.class);
            //startActivity(di);
            di.putExtra("position", position);
            startActivityForResult(di, DETAIL_REQUEST);
        }

    }

    public void CheckBoxChecked(RelativeLayout relativeLayout, final CheckBox checkBox, final String checkBoxValue){

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("checkAllClick", "checkAllClick = " + checkAllClick + "==" + checkAllClick);
                if (checkBoxValue.equals("all")) {
                    if (checkAllClick) {
                        chk_all.setChecked(false);
                        chk_pen_book.setChecked(false);
                        chk_com_book.setChecked(false);
                        chk_drv_can .setChecked(false);
                        chk_acp_book.setChecked(false);
                        checkAllClick = false;
                    } else {
                        chk_all.setChecked(true);
                        chk_pen_book.setChecked(true);
                        chk_com_book.setChecked(true);
                        chk_drv_can.setChecked(true);
                        chk_acp_book.setChecked(true);
                        checkAllClick = true;
                    }
                } else {
                    if (chk_pen_book.isChecked() && chk_com_book.isChecked() && chk_drv_can.isChecked() && chk_acp_book.isChecked()) {
                        chk_all.setChecked(true);
                        checkAllClick = true;
                    } else {
                        chk_all.setChecked(false);
                        checkAllClick = false;
                    }

                }
            }
        });

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked())
                    checkBox.setChecked(false);
                else
                    checkBox.setChecked(true);
                Log.d("checkAllClick", "checkAllClick = " + checkAllClick + "==" + checkAllClick);
                if (checkBoxValue.equals("all")) {
                    if (checkAllClick) {
                        chk_all.setChecked(false);
                        chk_pen_book.setChecked(false);
                        chk_com_book.setChecked(false);
                        chk_drv_can.setChecked(false);
                        chk_acp_book.setChecked(false);
                        checkAllClick = false;
                    } else {
                        chk_all.setChecked(true);
                        chk_pen_book.setChecked(true);
                        chk_com_book.setChecked(true);
                        chk_drv_can.setChecked(true);
                        chk_acp_book.setChecked(true);
                        checkAllClick = true;
                    }
                } else {
                    if (chk_pen_book.isChecked() && chk_com_book.isChecked() && chk_drv_can.isChecked() && chk_acp_book.isChecked()) {
                        chk_all.setChecked(true);
                        checkAllClick = true;
                    } else {
                        chk_all.setChecked(false);
                        checkAllClick = false;
                    }
                }
            }
        });
    }

    public void FilterAllDriverTrips(final int offset, final String filter,boolean is_pull) {
        if(offset == 0)
            DriverAllTripArray = new ArrayList<>();

        String DriverFilterTripUrl = Url.DriverFilterTripUrl+"?filter="+FilterString+"&off="+offset+"&driver_id="+userPref.getString("id","bgfv");
        Log.d("DriverFilterTripUrl","DriverFilterTripUrl = "+DriverFilterTripUrl);
        Ion.with(DriverTripActivity.this)
                .load(DriverFilterTripUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception error, JsonObject result) {
                        // do stuff with the result or error
                        Log.d("load_trips result", "load_trips result = " + result + "==" + error);
                        loader.cancel();
                        recycle_all_trip.setClickable(true);
                        recycle_all_trip.setEnabled(true);
                        if (error == null) {

                            try {
                                JSONObject resObj = new JSONObject(result.toString());
                                Log.d("loadTripsUrl", "loadTripsUrl two= " + resObj);
                                if (resObj.getString("status").equals("success")) {

                                    JSONArray tripArray = new JSONArray(resObj.getString("all_trip"));
                                    for (int t = 0; t < tripArray.length(); t++) {
                                        JSONObject trpObj = tripArray.getJSONObject(t);
                                        DriverAllTripFeed allTripFeed = new DriverAllTripFeed();
                                        allTripFeed.setId(trpObj.getString("id"));
                                        allTripFeed.setDriverFlag(trpObj.getString("driver_flag"));
                                        allTripFeed.setDropArea(trpObj.getString("drop_area"));
                                        allTripFeed.setPickupArea(trpObj.getString("pickup_area"));
                                        allTripFeed.setCarType(trpObj.getString("car_type"));
                                        allTripFeed.setPickupDateTime(trpObj.getString("pickup_date_time"));
                                        allTripFeed.setAmount(trpObj.getString("amount"));
                                        allTripFeed.setCarIcon(trpObj.getString("icon"));
                                        allTripFeed.setKm(trpObj.getString("km"));
                                        allTripFeed.setUserDetail(trpObj.getString("user_detail"));
                                        allTripFeed.setStatus(trpObj.getString("status"));
                                        allTripFeed.setStartTime(trpObj.getString("start_time"));
                                        allTripFeed.setEndTime(trpObj.getString("end_time"));
                                        allTripFeed.setServerTime(trpObj.getString("server_time"));
                                        allTripFeed.setApproxTime(trpObj.getString("approx_time"));
                                        allTripFeed.setPerMinuteRate(trpObj.getString("per_minute_rate"));
                                        allTripFeed.setPickupLat(trpObj.getString("pickup_lat"));
                                        allTripFeed.setPickupLongs(trpObj.getString("pickup_longs"));
                                        DriverAllTripArray.add(allTripFeed);
                                    }
                                    Log.d("loadTripsUrl", "loadTripsUrl three= " + DriverAllTripArray.size());
                                    if (DriverAllTripArray != null && DriverAllTripArray.size() > 0) {
                                        if (offset == 0) {
                                            layout_recycleview.setVisibility(View.VISIBLE);
                                            layout_no_recourd_found.setVisibility(View.GONE);
                                            DrvAllTripAdapter = new DriverAllTripAdapter(DriverTripActivity.this, DriverAllTripArray,false,savedInstState);
                                            recycle_all_trip.setAdapter(DrvAllTripAdapter);
                                            DrvAllTripAdapter.setOnAllTripItemClickListener(DriverTripActivity.this);
                                            swipe_refresh_layout.setRefreshing(false);
                                        }
                                        DrvAllTripAdapter.updateItems();
                                        swipe_refresh_layout.setEnabled(true);
                                    }
                                }else if(resObj.getString("status").equals("false")){

                                    Common.showMkError(DriverTripActivity.this,resObj.getString("error code").toString());

                                    if(resObj.has("Isactive") && resObj.getString("Isactive").equals("Inactive")) {

                                        SharedPreferences.Editor editor = userPref.edit();
                                        editor.clear();
                                        editor.commit();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(DriverTripActivity.this, LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, 2500);
                                    }
                                }else {
                                    if (offset == 0) {
                                        layout_recycleview.setVisibility(View.GONE);
                                        layout_no_recourd_found.setVisibility(View.VISIBLE);
                                    } else {

                                        Toast.makeText(DriverTripActivity.this, getResources().getString(R.string.data_not_found), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Common.ShowHttpErrorMessage(DriverTripActivity.this, error.getMessage());
                        }
                    }
                });
    }

    /**
     * Listener for socket connection error.. listener registered at the time of socket connection
     */
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSocket != null)
                        if (mSocket.connected() == false) {
                            Log.d("connected", "connected error= " + mSocket.connected());
                            //socketConnection();
                        }else
                        {
                            Log.d("connected", "connected three= " + mSocket.connected());
                        }
                }
            });
        }
    };

    public void getDriverAllTrip(final int offset, final boolean is_pull) {

        if(offset == 0) {
            DriverAllTripArray = new ArrayList<>();
        }
        //String DrvBookingUrl = Url.DriverTripUrl+"?driver_id="+Utility.userDetails.getId()+"&off="+offset;
        String DrvBookingUrl = Url.DriverTripUrl+"?driver_id="+userPref.getString("id","")+"&off="+offset;
        Log.d("loadTripsUrl","loadTripsUrl ="+DrvBookingUrl+"=="+String.valueOf(offset));
        Ion.with(DriverTripActivity.this)
                .load(DrvBookingUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception error, JsonObject result) {
                        // do stuff with the result or error
                        Log.d("load_trips result", "load_trips result = " + result + "==" + error);
                        loader.cancel();
                        recycle_all_trip.setClickable(true);
                        recycle_all_trip.setEnabled(true);
                        if (error == null) {

                            try {
                                JSONObject resObj = new JSONObject(result.toString());
                                Log.d("loadTripsUrl", "loadTripsUrl two= " + resObj);
                                if (resObj.getString("status").equals("success")) {

                                    JSONArray tripArray = new JSONArray(resObj.getString("all_trip"));
                                    for (int t = 0; t < tripArray.length(); t++) {
                                        JSONObject trpObj = tripArray.getJSONObject(t);
                                        DriverAllTripFeed allTripFeed = new DriverAllTripFeed();
                                        allTripFeed.setId(trpObj.getString("id"));
                                        allTripFeed.setDriverFlag(trpObj.getString("driver_flag"));
                                        allTripFeed.setDropArea(trpObj.getString("drop_area"));
                                        allTripFeed.setPickupArea(trpObj.getString("pickup_area"));
                                        allTripFeed.setCarType(trpObj.getString("car_type"));
                                        allTripFeed.setPickupDateTime(trpObj.getString("pickup_date_time"));
                                        allTripFeed.setAmount(trpObj.getString("amount"));
                                        allTripFeed.setCarIcon(trpObj.getString("icon"));
                                        allTripFeed.setKm(trpObj.getString("km"));
                                        allTripFeed.setUserDetail(trpObj.getString("user_detail"));
                                        allTripFeed.setStatus(trpObj.getString("status"));
                                        allTripFeed.setStartTime(trpObj.getString("start_time"));
                                        allTripFeed.setEndTime(trpObj.getString("end_time"));
                                        allTripFeed.setServerTime(trpObj.getString("server_time"));
                                        allTripFeed.setApproxTime(trpObj.getString("approx_time"));
                                        allTripFeed.setPerMinuteRate(trpObj.getString("per_minute_rate"));
                                        allTripFeed.setPickupLat(trpObj.getString("pickup_lat"));
                                        allTripFeed.setPickupLongs(trpObj.getString("pickup_longs"));
                                        DriverAllTripArray.add(allTripFeed);
                                    }
                                    Log.d("loadTripsUrl", "loadTripsUrl three= " + DriverAllTripArray.size());
                                    if (DriverAllTripArray != null && DriverAllTripArray.size() > 0) {
                                        if (offset == 0) {
                                            layout_recycleview.setVisibility(View.VISIBLE);
                                            layout_no_recourd_found.setVisibility(View.GONE);
                                            DrvAllTripAdapter = new DriverAllTripAdapter(DriverTripActivity.this, DriverAllTripArray,is_pull,savedInstState);
                                            recycle_all_trip.setAdapter(DrvAllTripAdapter);
                                            DrvAllTripAdapter.setOnAllTripItemClickListener(DriverTripActivity.this);
                                            DrvAllTripAdapter.updateItems();
                                            swipe_refresh_layout.setRefreshing(false);
                                        }else{
                                            DrvAllTripAdapter.updateItems();
                                            swipe_refresh_layout.setRefreshing(false);
                                        }
                                        swipe_refresh_layout.setEnabled(true);
                                    }
                                }else if(resObj.getString("status").equals("false")){

                                    Common.showMkError(DriverTripActivity.this,resObj.getString("error code").toString());

                                    if(resObj.has("Isactive") && resObj.getString("Isactive").equals("Inactive")) {

                                        SharedPreferences.Editor editor = userPref.edit();
                                        editor.clear();
                                        editor.commit();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(DriverTripActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, 2500);
                                    }
                                }else {
                                    if (offset == 0) {
                                        layout_recycleview.setVisibility(View.GONE);
                                        layout_no_recourd_found.setVisibility(View.VISIBLE);
                                    } else {
                                        //Toast.makeText(DriverTripActivity.this, resObj.getString("message").toString(), Toast.LENGTH_LONG).show();
                                        Toast.makeText(DriverTripActivity.this, getResources().getString(R.string.data_not_found), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Common.ShowHttpErrorMessage(DriverTripActivity.this, error.getMessage());
                        }
                    }
                });

    }

    public class CallUnsubscribe extends AsyncTask<String, Void, String > {


        private SharedPreferences userPref;
        String DeviceToken;
        Activity activity;
        public CallUnsubscribe(Activity activity, String dt){
            DeviceToken = dt;
            userPref = PreferenceManager.getDefaultSharedPreferences(activity);
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... args) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpParams myParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(myParams, 10000);
            HttpConnectionParams.setSoTimeout(myParams, 10000);

            JSONObject JSONResponse = null;
            InputStream contentStream = null;
            String resultString = "";

            try {

                JSONObject passObj = new JSONObject();
                passObj.put("user","d_"+userPref.getString("id",""));
                passObj.put("type","android");
                passObj.put("token",DeviceToken);

                Log.d("passObj","response passObj = "+passObj);

                HttpPost httppost = new HttpPost(Url.unsubscribeUrl);
                httppost.setHeader("Content-Type", "application/json");
                httppost.setHeader("Accept", "application/json");

//                StringEntity se = new StringEntity(passObj.toString());
//                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//                httppost.setEntity(se);
                httppost.setEntity(new StringEntity(passObj.toString(), HTTP.UTF_8));

                for (int i=0;i<httppost.getAllHeaders().length;i++) {
                    Log.v("set header", httppost.getAllHeaders()[i].getValue());
                }

                HttpResponse response = httpclient.execute(httppost);

                // Do some checks to make sure that the request was processed properly
                Header[] headers = response.getAllHeaders();
                HttpEntity entity = response.getEntity();
                contentStream = entity.getContent();

                Log.d("response","response = "+response.toString()+"=="+entity+"=="+contentStream);
                resultString = response.toString();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d("Error","response Error one = "+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Error", "response Error two = " + e.getMessage());
                return e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Error", "response Error three = " + e.getMessage());
                return e.getMessage();
            }


            return resultString;
        }

        @Override
        protected void onPostExecute(String result) {

            if(result.contains("HTTP/1.1 200 OK")){
                new CallSubscribe(activity,Common.device_token).execute();
            }else if(result.contains("failed to connect to")){
                if(loader.isShowing())
                    loader.cancel();
                Common.ShowHttpErrorMessage(activity,"network not available");
            }
        }
    }

    public class CallSubscribe extends AsyncTask<String, Void, String > {


        private SharedPreferences userPref;
        String DeviceToken;
        Activity activity;
        public CallSubscribe(Activity activity, String dt){
            DeviceToken = dt;
            userPref = PreferenceManager.getDefaultSharedPreferences(activity);
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... args) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpParams myParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(myParams, 10000);
            HttpConnectionParams.setSoTimeout(myParams, 10000);

            JSONObject JSONResponse = null;
            InputStream contentStream = null;
            String resultString = "";

            try {

                JSONObject passObj = new JSONObject();
                passObj.put("user","d_"+userPref.getString("id",""));
                passObj.put("type","android");
                passObj.put("token",DeviceToken);

                Log.d("passObj","response passObj = "+passObj);

                HttpPost httppost = new HttpPost(Url.subscribeUrl);
                httppost.setHeader("Content-Type", "application/json");
                httppost.setHeader("Accept", "application/json");

//                StringEntity se = new StringEntity(passObj.toString());
//                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//                httppost.setEntity(se);
                httppost.setEntity(new StringEntity(passObj.toString(), HTTP.UTF_8));

                for (int i=0;i<httppost.getAllHeaders().length;i++) {
                    Log.v("set header", httppost.getAllHeaders()[i].getValue());
                }

                HttpResponse response = httpclient.execute(httppost);

                // Do some checks to make sure that the request was processed properly
                Header[] headers = response.getAllHeaders();
                HttpEntity entity = response.getEntity();
                contentStream = entity.getContent();

                Log.d("response","response = "+response.toString()+"=="+entity+"=="+contentStream);
                resultString = response.toString();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d("Error","response Error one = "+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Error", "response Error two = " + e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Error", "response Error three = " + e.getMessage());
            }


            return resultString;
        }

        @Override
        protected void onPostExecute(String result) {

            if(result.contains("HTTP/1.1 200 OK")){
                SharedPreferences.Editor isDeviceToken = userPref.edit();
                isDeviceToken.putString("id_device_token", "1");
                isDeviceToken.commit();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Common.isNetworkAvailable(DriverTripActivity.this)) {

                            boolean allFilter = false;
                            if (userPref.getBoolean("setFilter", false) == true) {
                                if (userPref.getInt("pending booking", 4) == 0) {
                                    FilterString += 0 + ",";
                                    allFilter = true;
                                }
                                if (userPref.getInt("accepted booking", 4) == 1) {
                                    FilterString += 1 + ",";
                                    allFilter = true;
                                }
                                if (userPref.getInt("driver cancel", 4) == 2) {
                                    FilterString += 2 + ",";
                                    allFilter = true;
                                }
                                if (userPref.getInt("completed booking", 4) == 3) {
                                    FilterString += 3 + ",";
                                    allFilter = true;
                                }
                                if(FilterString.length() > 0)
                                    FilterString = FilterString.substring(0, (FilterString.length() - 1));

                                SharedPreferences.Editor clickfilter = userPref.edit();
                                clickfilter.putBoolean("setFilter", allFilter);
                                clickfilter.commit();

                                if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3){
                                    FilterString = "";
                                }
                                FilterAllDriverTrips(0,"filter",false);
                                FilterString = "";

                            }else {
                                getDriverAllTrip(0,false);
                            }
                        } else {
                            Common.showInternetInfo(DriverTripActivity.this, "Network is not available");
                            swipe_refresh_layout.setEnabled(false);
                        }
                    }
                }, 500);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("DETAIL_REQUEST","DriverTrip DETAIL_REQUEST = "+DETAIL_REQUEST+"=="+requestCode+"=="+data);
        if(requestCode == DETAIL_REQUEST){
            if(data != null) {
                int position = data.getIntExtra("position", 0);
                DriverAllTripFeed driverAllTripFeed = DriverAllTripArray.get(position);
                driverAllTripFeed.setDriverFlag(data.getStringExtra("driver_flage"));
                driverAllTripFeed.setStatus(data.getStringExtra("status"));
                DrvAllTripAdapter.notifyItemChanged(position);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Common.profile_edit == 1){
            Common.showMkSucess(DriverTripActivity.this,getResources().getString(R.string.update_profile),"yes");
            Common.profile_edit = 0;
        }
        if(Common.BookingId != null && !Common.BookingId.equals("")){

            if (Common.isNetworkAvailable(DriverTripActivity.this)) {
                Common.BookingId = "";
                recycle_all_trip.setClickable(false);
                recycle_all_trip.setEnabled(false);
                boolean allFilter = false;
                if (userPref.getBoolean("setFilter", false) == true) {
                    if (userPref.getInt("pending booking", 4) == 0) {
                        FilterString += 0 + ",";
                        allFilter = true;
                    }
                    if (userPref.getInt("accepted booking", 4) == 1) {
                        FilterString += 1 + ",";
                        allFilter = true;
                    }
                    if (userPref.getInt("driver cancel", 4) == 2) {
                        FilterString += 2 + ",";
                        allFilter = true;
                    }
                    if (userPref.getInt("completed booking", 4) == 3) {
                        FilterString += 3 + ",";
                        allFilter = true;
                    }
                    if(FilterString.length() > 0)
                        FilterString = FilterString.substring(0, (FilterString.length() - 1));

                    SharedPreferences.Editor clickfilter = userPref.edit();
                    clickfilter.putBoolean("setFilter", allFilter);
                    clickfilter.commit();

                    if (userPref.getInt("pending booking", 4) == 0 && userPref.getInt("accepted booking", 4) == 1 && userPref.getInt("driver cancel", 4) == 2 && userPref.getInt("completed booking", 4) == 3){
                        FilterString = "";
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FilterAllDriverTrips(0,"",true);
                        }
                    }, 1000);

                    FilterString = "";

                }else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getDriverAllTrip(0,true);
                        }
                    }, 1000);

                }

            } else {
                recycle_all_trip.setClickable(true);
                recycle_all_trip.setEnabled(true);
                //Network is not available
                Common.showInternetInfo(DriverTripActivity.this, "Network is not available");
            }
            Common.ActionClick = "";
            Common.BookingId = "";
        }

    }
}
