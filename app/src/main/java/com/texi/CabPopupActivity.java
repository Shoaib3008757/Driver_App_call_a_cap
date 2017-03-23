package com.texi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.texi.utils.Common;
import com.texi.utils.CustomMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CabPopupActivity extends AppCompatActivity implements OnMapReadyCallback {

    RelativeLayout layout_accept;
    RelativeLayout layout_decline;
    TextView minutes_value,txt_accept,txt_decline;
    DonutProgress timmer_progress;
    TextView txt_address_val;
    CustomMap customMap;

    long accpet_time = 0;

    JSONObject booking_data;
    CountDownTimer customCountdownTimer;

    private GoogleMap mMap;
    LoaderView loader;
    SharedPreferences userPref;

    Typeface Roboto_Bold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cab_popup);

        Display display = getWindowManager().getDefaultDisplay();

        Roboto_Bold = Typeface.createFromAsset(getAssets(), "fonts/roboto_bold.ttf");

        userPref = PreferenceManager.getDefaultSharedPreferences(CabPopupActivity.this);

        loader = new LoaderView(CabPopupActivity.this);

        try {
            booking_data = new JSONObject(getIntent().getStringExtra("booking_data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        customMap = (CustomMap)findViewById(R.id.mapview);
        MapsInitializer.initialize(CabPopupActivity.this);
        customMap.onCreate(savedInstanceState);

        customMap.getMapAsync(CabPopupActivity.this);

        minutes_value = (TextView)findViewById(R.id.minutes_value);
        txt_address_val = (TextView)findViewById(R.id.txt_address_val);
        timmer_progress = (DonutProgress)findViewById(R.id.timmer_progress);
        txt_accept = (TextView)findViewById(R.id.txt_accept);
        txt_accept.setTypeface(Roboto_Bold);
        txt_decline = (TextView)findViewById(R.id.txt_decline);
        txt_decline.setTypeface(Roboto_Bold);

        RelativeLayout layout_main = (RelativeLayout)findViewById(R.id.layout_main);
        layout_main.getLayoutParams().height = (int) (display.getHeight() * 0.72);

        layout_accept = (RelativeLayout) findViewById(R.id.layout_accept);
        layout_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_accept.setClickable(false);
                layout_accept.setEnabled(false);

                AcceptBooking();
            }
        });

        layout_decline = (RelativeLayout) findViewById(R.id.layout_decline);
        layout_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_decline.setEnabled(false);

                RejectBooking();
            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DriverPopup();
            }
        }, 1000);
    }

    public void AcceptBooking(){

        loader.show();

        String DrvBookingUrl = Url.DriverAcceptTripUrl+"?booking_id="+ Common.BookingId+"&driver_id="+userPref.getString("id","");
        Log.d("DrvBookingUrl","DrvBookingUrl ="+DrvBookingUrl);
        Ion.with(CabPopupActivity.this)
                .load(DrvBookingUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception error, JsonObject result) {
                        // do stuff with the result or error
                        Log.d("load_trips result", "load_trips result = " + result + "==" + error);
                        customCountdownTimer.onFinish();

                        loader.cancel();
                        if (error == null) {

                            try {
                                JSONObject resObj = new JSONObject(result.toString());

                                if(resObj.getString("status").equals("success")) {

                                    Common.ActionClick = "accept";

                                    SharedPreferences.Editor booking_status = userPref.edit();
                                    booking_status.putString("booking_status","Accepted");
                                    booking_status.commit();

                                    Intent intent = new Intent(CabPopupActivity.this, DriverTripActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();

                                }else if(resObj.getString("status").equals("false")){
                                    Common.showMkError(CabPopupActivity.this,resObj.getString("error code").toString());

                                    if(resObj.has("Isactive") && resObj.getString("Isactive").equals("Inactive")) {

                                        SharedPreferences.Editor editor = userPref.edit();
                                        editor.clear();
                                        editor.commit();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(CabPopupActivity.this, MainActivity.class);
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
                            layout_accept.setClickable(true);
                            layout_accept.setEnabled(true);
                            Common.ShowHttpErrorMessage(CabPopupActivity.this, error.getMessage());
                        }
                    }
                });
    }

    public void RejectBooking(){

        final String DrvRejectUrl = Url.DriverRejectTripUrl + "?booking_id=" + Common.BookingId + "&driver_id=" + userPref.getString("id", "");
        Log.d("DrvRejectUrl","DrvRejectUrl = "+DrvRejectUrl);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(CabPopupActivity.this)
                .cancelable(false)
                .title(R.string.delete_trip)
                .content(R.string.are_you_sure_delete_trip)
                .negativeText(R.string.dialog_cancel)
                .positiveText(R.string.dialog_ok)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        layout_decline.setEnabled(true);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final LoaderView loader = new LoaderView(CabPopupActivity.this);
                        loader.show();
                        Ion.with(CabPopupActivity.this)
                                .load(DrvRejectUrl)
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception error, JsonObject result) {
                                        // do stuff with the result or error
                                        Log.d("load_trips result", "load_trips result = " + result + "==" + error);
                                        loader.cancel();
                                        customCountdownTimer.onFinish();

                                        if (error == null) {
                                            try {
                                                JSONObject resObj = new JSONObject(result.toString());

                                                if(resObj.getString("status").equals("success")) {

                                                    Common.ActionClick = "reject";

                                                    SharedPreferences.Editor booking_status = userPref.edit();
                                                    booking_status.putString("booking_status","Rejected");
                                                    booking_status.commit();

                                                    Intent intent = new Intent(CabPopupActivity.this, DriverTripActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();

                                                }else if(resObj.getString("status").equals("false")){
                                                    Common.showMkError(CabPopupActivity.this,resObj.getString("error code").toString());

                                                    if(resObj.has("Isactive") && resObj.getString("Isactive").equals("Inactive")) {

                                                        SharedPreferences.Editor editor = userPref.edit();
                                                        editor.clear();
                                                        editor.commit();

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(CabPopupActivity.this, MainActivity.class);
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
                                            Common.ShowHttpErrorMessage(CabPopupActivity.this, error.getMessage());
                                        }
                                    }
                                });
                    }
                });

        MaterialDialog dialog = builder.build();
        dialog.show();

    }

    public void DriverPopup(){

        try {

            //Log.d("dialog dataArray","dialog pickup location = "+ URLDecoder.decode(booking_data.getString("pickup"),"UTF-8"));

            txt_address_val.setText(URLDecoder.decode(booking_data.getString("pickup"),"UTF-8"));

            JSONArray dataArray = new JSONArray(booking_data.getString("data"));
            Log.d("dialog dataArray","dialog dataArray = "+dataArray.length());
            for(int di=0;di<dataArray.length();di++){
                JSONObject dataObj = dataArray.getJSONObject(di);
                String Lotlon = dataObj.getString("loc");
                //JSONArray LotLanArray = new JSONArray(dataObj.getString("loc"));
                String[] SplLotlon = Lotlon.split("\\,");

                String DrvLat = SplLotlon[0].replace("[","");
                String DrvLng = SplLotlon[1].replace("]","");
                LatLng UserLarLng = new LatLng(Double.parseDouble(DrvLat), Double.parseDouble(DrvLng));

                Log.d("Lotlon","dialog Lotlon = "+DrvLng+"=="+DrvLat);

                mMap.addMarker(new MarkerOptions().position(UserLarLng)
                        .title(txt_address_val.getText().toString()));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(UserLarLng)      // Sets the center of the map to location user
                        .zoom(10)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String EndTime = URLDecoder.decode(booking_data.getString("end_time"),"UTF-8");
            String StartTime = URLDecoder.decode(booking_data.getString("start_time"),"UTF-8");
            Log.d("Lotlon","dialog StartTime = "+StartTime+"=="+EndTime);

            try {
                Date date1 = simpleDateFormat.parse(EndTime);
                Date date2 = simpleDateFormat.parse(StartTime);
                accpet_time = date1.getTime() - date2.getTime();

                Log.d("different","different = "+accpet_time);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            customCountdownTimer = Common.AcceptRejectTimer(CabPopupActivity.this,timmer_progress,accpet_time,minutes_value,"cabpopupActivity");
            customCountdownTimer.start();

            Common.BookingId = booking_data.getString("booking_id");

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Error","Error one"+e.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d("Error","Error one"+e.getMessage());
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onResume() {
        customMap.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        customMap.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        customMap.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        customMap.onDestroy();
        super.onDestroy();
        if(customCountdownTimer != null)
            customCountdownTimer.onFinish();
    }

    public void onBackPressed() {
        //super.onBackPressed();
    }
}
