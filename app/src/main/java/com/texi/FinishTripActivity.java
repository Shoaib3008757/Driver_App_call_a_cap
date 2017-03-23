package com.texi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import com.texi.utils.Common;
import com.texi.utils.DriverAllTripFeed;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FinishTripActivity extends AppCompatActivity {

    RelativeLayout layout_finished;
    RelativeLayout layout_back_arrow;
    TextView txt_booking_detail;
    ImageView img_user;
    TextView txt_user_name;
    TextView txt_booking_date;
    TextView txt_pickup_location;
    TextView txt_pickup_time;
    TextView txt_pickup_location_val;
    TextView txt_drop_location;
    TextView txt_drop_time;
    TextView txt_drop_location_val;
    TextView txt_distance;
    TextView txt_distance_val;
    TextView txt_total_time;
    TextView txt_approc_price;
    TextView txt_approc_price_val;
    TextView txt_final_price;
    TextView txt_final_price_val;
    TextView txt_appr_currence;
    TextView txt_final_currence;
    EditText edt_final_amount;
    EditText edt_reason_ride;

    DriverAllTripFeed driverAllTripFeed;

    SharedPreferences userPref;
    LoaderView loader;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_trip);

        userPref = PreferenceManager.getDefaultSharedPreferences(FinishTripActivity.this);
        loader=new LoaderView(FinishTripActivity.this);

        position = getIntent().getIntExtra("position",0);

        driverAllTripFeed = Common.driverAllTripFeed;

        txt_booking_detail = (TextView)findViewById(R.id.txt_booking_detail);
        img_user = (ImageView)findViewById(R.id.img_user);
        txt_user_name = (TextView)findViewById(R.id.txt_user_name);
        txt_booking_date = (TextView)findViewById(R.id.txt_booking_date);
        txt_pickup_location = (TextView)findViewById(R.id.txt_pickup_location);
        txt_pickup_time = (TextView)findViewById(R.id.txt_pickup_time);
        txt_pickup_location_val = (TextView)findViewById(R.id.txt_pickup_location_val);
        txt_drop_location = (TextView)findViewById(R.id.txt_drop_location);
        txt_drop_time = (TextView)findViewById(R.id.txt_drop_time);
        txt_drop_location_val = (TextView)findViewById(R.id.txt_drop_location_val);
        layout_finished = (RelativeLayout)findViewById(R.id.layout_finished);
        txt_distance = (TextView) findViewById(R.id.txt_distance);
        txt_distance_val = (TextView) findViewById(R.id.txt_distance_val);
        txt_total_time = (TextView) findViewById(R.id.txt_total_time);
        txt_approc_price = (TextView) findViewById(R.id.txt_approc_price);
        txt_approc_price_val = (TextView) findViewById(R.id.txt_approc_price_val);
        txt_final_price = (TextView) findViewById(R.id.txt_final_price);
        txt_final_price_val = (TextView) findViewById(R.id.txt_final_price_val);
        txt_appr_currence = (TextView) findViewById(R.id.txt_appr_currence);
        txt_final_currence = (TextView) findViewById(R.id.txt_final_currence);
        edt_final_amount = (EditText) findViewById(R.id.edt_final_amount);
        edt_reason_ride = (EditText) findViewById(R.id.edt_reason_ride);

        txt_appr_currence.setText(userPref.getString("currency",""));
        txt_final_currence.setText(userPref.getString("currency",""));
        if(Common.OnTripTime != "")
            txt_pickup_time.setText(Common.OnTripTime);
        if(Common.FinishedTripTime != "")
            txt_drop_time.setText(Common.FinishedTripTime);
        txt_approc_price_val.setText(driverAllTripFeed.getAmount());


        SimpleDateFormat timeDifFormate = new SimpleDateFormat("HH:mm:ss");
        String TotalTime = "";
        try {
            Date starttime = timeDifFormate.parse(Common.OnTripTime);
            Date endtime = timeDifFormate.parse(Common.FinishedTripTime);

            long TwoTimeDif = endtime.getTime() - starttime.getTime();

            long mills = Math.abs(TwoTimeDif);

            int Hours = (int) (mills/(1000 * 60 * 60));
            int Mins = (int) (mills/(1000*60)) % 60;
            long Secs = (int) (mills / 1000) % 60;

            Log.d("TwoTimeDif","TwoTimeDif = "+Common.FinishedTripTime+"=="+Common.OnTripTime);
            Log.d("TwoTimeDif","TwoTimeDif = "+endtime.getTime()+"=="+starttime.getTime());
            Log.d("TwoTimeDif","TwoTimeDif = "+TwoTimeDif+"=="+Hours+"=="+Mins+"=="+Secs);

            if(Hours > 0)
                TotalTime += Hours+" hr ";
            if(Mins > 0)
                TotalTime += Mins+" min ";

            txt_total_time.setText(TotalTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        txt_distance_val.setText(driverAllTripFeed.getKm() + " km");

        if(driverAllTripFeed.getuserDetail() != null && !driverAllTripFeed.getuserDetail().equals("")) {
            try {
                JSONObject DrvObj = new JSONObject(driverAllTripFeed.getuserDetail());
                Picasso.with(FinishTripActivity.this)
                        .load(Uri.parse(Url.userImageUrl + DrvObj.getString("image")))
                        .placeholder(R.drawable.user_photo)
                        .transform(new CircleTransformation(FinishTripActivity.this))
                        .into(img_user);
                txt_user_name.setText(DrvObj.getString("username"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String pickup_date_time = "";
        try {
            Date parceDate = simpleDateFormat.parse(driverAllTripFeed.getPickupDateTime());
            SimpleDateFormat parceDateFormat = new SimpleDateFormat("dd MMM yyyy");
            pickup_date_time = parceDateFormat.format(parceDate.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        txt_booking_date.setText(pickup_date_time);

        txt_pickup_location_val.setText(driverAllTripFeed.getPickupArea());
        txt_drop_location_val.setText(driverAllTripFeed.getDropArea());

        /*Final amount caculation*/

        String approxTime[] = driverAllTripFeed.getApproxTime().split(" ");
        int hours = 0;
        int mintus = 0;
        if (approxTime.length == 4) {
            hours = Integer.parseInt(approxTime[0]) * 60;
            mintus = Integer.parseInt(approxTime[2]);
        } else if (approxTime.length == 2) {
            if (approxTime[1].contains("mins"))
                mintus = Integer.parseInt(approxTime[0]);
            else
                mintus = Integer.parseInt(approxTime[0]) * 3600;
        }

        Float aprTime = (hours+mintus) *  Float.parseFloat(driverAllTripFeed.getPerMinuteRate());
        Log.d("Driver Price","Driver Price = "+aprTime+"=="+hours+"=="+mintus);
        Float RideAmount = Integer.parseInt(driverAllTripFeed.getAmount()) - aprTime;

        int final_hours = 0;
        int final_mintus = 0;
        String timeSplite[] = TotalTime.split(" ");
        if(timeSplite.length == 2){
            final_mintus = Integer.parseInt(timeSplite[0]);
        }else if(timeSplite.length == 4){
            final_hours = Integer.parseInt(timeSplite[0]) * 60;
            final_mintus = Integer.parseInt(timeSplite[2]);
        }

        float DriverAmount = (final_hours+final_mintus)*Float.parseFloat(driverAllTripFeed.getPerMinuteRate());
        int finalPrice = (int) (DriverAmount+RideAmount);
        txt_final_price_val.setText(finalPrice+"");
        edt_final_amount.setText(finalPrice+"");
        Log.d("Driver Price","Driver Price = "+aprTime+"=="+RideAmount+"=="+final_mintus+"=="+final_hours+"=="+DriverAmount);

        layout_finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edt_final_amount.getText().toString().length() == 0){
                    Common.showMkError(FinishTripActivity.this,getResources().getString(R.string.please_enter_amount));
                    return;
                }

                loader.show();
                String FinishUrl = "";
                try {
                    FinishUrl = Url.DriverCompletedTripUrl+"?booking_id="+driverAllTripFeed.getId()+"&driver_id="+userPref.getString("id","")+"&final_amount="+edt_final_amount.getText().toString()+"&delay_reason="+ URLEncoder.encode(edt_reason_ride.getText().toString(),"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("FinishUrl","FinishUrl = "+FinishUrl);
                Ion.with(FinishTripActivity.this)
                        .load(FinishUrl)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                loader.loaderObject().stop();
                                loader.loaderDismiss();



                                // do stuff with the result or error
                                if (e != null) {
                                    //Toast.makeText(FinishTripActivity.this, "Login Error" + e, Toast.LENGTH_LONG).show();
                                    Common.showMkError(FinishTripActivity.this,e.getMessage());
                                    return;
                                }
                                layout_finished.setVisibility(View.GONE);
                                try {
                                    JSONObject jsonObject = new JSONObject(result.toString());
                                    if (jsonObject.has("status") && jsonObject.getString("status").equals("success")) {

                                        layout_finished.setVisibility(View.GONE);

                                        SharedPreferences.Editor isBookingAccept = userPref.edit();
                                        isBookingAccept.putBoolean("isBookingAccept",false);
                                        isBookingAccept.commit();

                                        SharedPreferences.Editor booking_status = userPref.edit();
                                        booking_status.putString("booking_status","finished");                                      booking_status.commit();

                                        driverAllTripFeed.setStatus("9");
                                        driverAllTripFeed.setDriverFlag("3");

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent di = new Intent();
                                                di.putExtra("position", position);
                                                di.putExtra("status", driverAllTripFeed.getStatus());
                                                di.putExtra("driver_flage", driverAllTripFeed.getDriverFlag());
                                                setResult(1, di);
                                                finish();
                                            }
                                        }, 500);

                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }


                            }
                        });

            }
        });

        layout_back_arrow = (RelativeLayout)findViewById(R.id.layout_back_arrow);
        layout_back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
