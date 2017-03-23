package com.texi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import com.texi.utils.Common;
import com.texi.utils.DriverAllTripFeed;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DriverTripDetailActivity extends AppCompatActivity {

    RelativeLayout layout_back_arrow;

    ImageView img_user_image;

    TextView txt_booking_detail,txt_payment_detial,txt_vehicle_detail,txt_to;
    TextView txt_booking_id;
    TextView txt_booking_id_val;
    TextView txt_pickup_point;
    TextView txt_pickup_point_val;
    TextView txt_booking_date;
    TextView txt_drop_point;
    TextView txt_drop_point_val;
    TextView txt_user_name;
    TextView txt_user_email;
    TextView txt_mobile_num;
    TextView txt_distance;
    TextView txt_distance_val;
    TextView txt_distance_km;
    TextView txt_total_price;
    TextView txt_total_price_dol;
    TextView txt_total_price_val;
    TextView txt_payment_type;
    TextView txt_payment_type_val;
    TextView txt_approx_time;
    TextView txt_approx_time_val;

    LinearLayout layout_accepted;
    RelativeLayout layout_arrived;
    RelativeLayout layout_begin_trip;
    RelativeLayout layout_user_call;
    RelativeLayout layout_finished;

    String UserMobileNu;

    Typeface OpenSans_Regular,Roboto_Regular,Roboto_Medium,Roboto_Bold,OpenSans_Semibold;

    SharedPreferences userPref;

    LoaderView loader;
    DriverAllTripFeed driverAllTripFeed;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_trip_detail);

        userPref = PreferenceManager.getDefaultSharedPreferences(DriverTripDetailActivity.this);

        position = getIntent().getIntExtra("position",0);

        loader=new LoaderView(DriverTripDetailActivity.this);

        OpenSans_Regular = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.font_regular_opensans));
        Roboto_Regular = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.font_regular_roboto));
        Roboto_Medium = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.font_medium_roboto));
        Roboto_Bold = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.font_bold_roboto));
        OpenSans_Semibold = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.font_semibold_opensans));

        driverAllTripFeed = Common.driverAllTripFeed;

        layout_back_arrow = (RelativeLayout)findViewById(R.id.layout_back_arrow);
        layout_back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent di = new Intent();
                di.putExtra("position", position);
                di.putExtra("status", driverAllTripFeed.getStatus());
                di.putExtra("driver_flage", driverAllTripFeed.getDriverFlag());
                setResult(1, di);
                finish();
            }
        });

        txt_booking_detail = (TextView)findViewById(R.id.txt_booking_detail);
        txt_booking_id = (TextView)findViewById(R.id.txt_booking_id);
        txt_booking_id_val = (TextView)findViewById(R.id.txt_booking_id_val);
        txt_pickup_point = (TextView)findViewById(R.id.txt_pickup_point);
        txt_pickup_point_val = (TextView)findViewById(R.id.txt_pickup_point_val);
        txt_booking_date = (TextView)findViewById(R.id.txt_booking_date);
        txt_drop_point = (TextView)findViewById(R.id.txt_booking_date);
        txt_drop_point_val = (TextView)findViewById(R.id.txt_drop_point_val);
        txt_user_name = (TextView)findViewById(R.id.txt_user_name);
        txt_user_email = (TextView)findViewById(R.id.txt_user_email);
        txt_mobile_num = (TextView)findViewById(R.id.txt_mobile_num);
        txt_distance = (TextView)findViewById(R.id.txt_distance);
        txt_distance_val = (TextView)findViewById(R.id.txt_distance_val);
        txt_distance_km = (TextView)findViewById(R.id.txt_distance_km);
        txt_total_price = (TextView)findViewById(R.id.txt_total_price);
        txt_total_price_dol = (TextView)findViewById(R.id.txt_total_price_dol);
        txt_total_price_val = (TextView)findViewById(R.id.txt_total_price_val);
        txt_payment_type = (TextView)findViewById(R.id.txt_payment_type);
        txt_payment_type_val = (TextView)findViewById(R.id.txt_payment_type_val);
        txt_approx_time = (TextView)findViewById(R.id.txt_approx_time);
        txt_approx_time_val = (TextView)findViewById(R.id.txt_approx_time_val);
        txt_payment_detial = (TextView)findViewById(R.id.txt_payment_detial);
        txt_vehicle_detail = (TextView)findViewById(R.id.txt_vehicle_detail);
        txt_to = (TextView)findViewById(R.id.txt_to);

        img_user_image = (ImageView)findViewById(R.id.img_user_image);

        txt_booking_detail.setTypeface(OpenSans_Regular);

        txt_booking_id.setTypeface(Roboto_Regular);
        txt_pickup_point.setTypeface(Roboto_Regular);
        txt_drop_point.setTypeface(Roboto_Regular);
        txt_distance_km.setTypeface(Roboto_Regular);
        txt_total_price_dol.setTypeface(Roboto_Regular);
        txt_total_price_dol.setText(userPref.getString("currency",""));

        txt_user_name.setTypeface(Roboto_Regular);
        txt_user_email.setTypeface(Roboto_Regular);
        txt_mobile_num.setTypeface(Roboto_Regular);

        txt_pickup_point_val.setTypeface(OpenSans_Regular);
        txt_booking_date.setTypeface(OpenSans_Regular);
        txt_drop_point_val.setTypeface(OpenSans_Regular);
        txt_distance.setTypeface(OpenSans_Regular);
        txt_distance_val.setTypeface(OpenSans_Regular);
        txt_total_price.setTypeface(OpenSans_Regular);
        txt_total_price_val.setTypeface(OpenSans_Regular);
        txt_payment_type.setTypeface(OpenSans_Regular);
        txt_payment_type_val.setTypeface(OpenSans_Regular);
        txt_approx_time.setTypeface(OpenSans_Regular);
        txt_approx_time_val.setTypeface(OpenSans_Regular);
        txt_payment_detial.setTypeface(Roboto_Bold);
        txt_vehicle_detail.setTypeface(Roboto_Bold);
        txt_to.setTypeface(Roboto_Bold);

        txt_booking_id_val.setText(driverAllTripFeed.getId());
        txt_pickup_point_val.setText(driverAllTripFeed.getPickupArea());
        txt_drop_point_val.setText(driverAllTripFeed.getDropArea());
        txt_distance_val.setText(driverAllTripFeed.getKm());
        txt_total_price_val.setText(driverAllTripFeed.getAmount());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String pickup_date_time = "";
        try {
            Date parceDate = simpleDateFormat.parse(driverAllTripFeed.getPickupDateTime());
            SimpleDateFormat parceDateFormat = new SimpleDateFormat("h:mm a,dd,MMM yyyy");
            pickup_date_time = parceDateFormat.format(parceDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        txt_booking_date.setText(pickup_date_time);

        txt_approx_time_val.setText(driverAllTripFeed.getApproxTime());

        if(driverAllTripFeed.getuserDetail() != null && !driverAllTripFeed.getuserDetail().equals("")) {
            try {
                JSONObject userObj = new JSONObject(driverAllTripFeed.getuserDetail());

                if(!userObj.getString("facebook_id").equals("") && userObj.getString("image").equals("")){
                    String facebookImage = Url.FacebookImgUrl + userObj.getString("facebook_id").toString() + "/picture?type=large";
                    Log.d("facebookImage","facebookImage = "+facebookImage);
                    Picasso.with(DriverTripDetailActivity.this)
                            .load(facebookImage)
                            .placeholder(R.drawable.user_photo)
                            .resize(200, 200)
                            .transform(new  CircleTransformation(DriverTripDetailActivity.this))
                            .into(img_user_image);
                }else {
                    Picasso.with(DriverTripDetailActivity.this)
                            .load(Uri.parse(Url.userImageUrl + userObj.getString("image")))
                            .placeholder(R.drawable.user_photo)
                            .transform(new CircleTransformation(DriverTripDetailActivity.this))
                            .into(img_user_image);
                }

                txt_user_name.setText(userObj.getString("username"));
                txt_user_email.setText(userObj.getString("email"));
                UserMobileNu = userObj.getString("mobile");
                txt_mobile_num.setText(UserMobileNu);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        layout_accepted = (LinearLayout)findViewById(R.id.layout_accepted);

        layout_arrived = (RelativeLayout) findViewById(R.id.layout_arrived);
        layout_arrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MaterialDialog.Builder builder = new MaterialDialog.Builder(DriverTripDetailActivity.this)
                        .content(R.string.arrived_message)
                        .negativeText(R.string.dialog_cancel)
                        .positiveText(R.string.dialog_ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.cancel();
                                loader.show();
                                String ArrivedUrl = Url.DriverArrivedTripUrl+"?booking_id="+driverAllTripFeed.getId()+"&driver_id="+userPref.getString("id","");
                                Log.d("ArrivedUrl","ArrivedUrl = "+ArrivedUrl);
                                DriverCall(ArrivedUrl,"7");
                            }
                        });

                MaterialDialog dialog = builder.build();
                dialog.show();

            }
        });
        layout_begin_trip = (RelativeLayout)findViewById(R.id.layout_begin_trip);
        layout_begin_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MaterialDialog.Builder builder = new MaterialDialog.Builder(DriverTripDetailActivity.this)
                        .content(R.string.begin_message)
                        .negativeText(R.string.dialog_cancel)
                        .positiveText(R.string.dialog_ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.cancel();
                                Common.OnTripTime = Common.getCurrentTime();
                                driverAllTripFeed.setStartRideTime(Common.getCurrentTime());

                                loader.show();
                                String BeginUrl = Url.DriverOnTripUrl+"?booking_id="+driverAllTripFeed.getId()+"&driver_id="+userPref.getString("id","");
                                Log.d("ArrivedUrl","ArrivedUrl = "+BeginUrl);
                                DriverCall(BeginUrl,"8");
                            }
                        });

                MaterialDialog dialog = builder.build();
                dialog.show();

            }
        });

        layout_user_call = (RelativeLayout)findViewById(R.id.layout_user_call);
        layout_user_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:"+UserMobileNu));
                        startActivity(callIntent);
                    }
                }, 100);
            }
        });

        layout_finished = (RelativeLayout)findViewById(R.id.layout_finished);
        layout_finished.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                driverAllTripFeed.setEndRideTime(Common.getCurrentTime());
                Common.FinishedTripTime = Common.getCurrentTime();
                Common.driverAllTripFeed = driverAllTripFeed;
                Intent fi = new Intent(DriverTripDetailActivity.this, FinishTripActivity.class);
                fi.putExtra("position",position);
                startActivityForResult(fi,1);
            }
        });

        if(driverAllTripFeed.getStatus().equals("1")){
            layout_accepted.setVisibility(View.GONE);
        }else if(driverAllTripFeed.getStatus().equals("3")){
            layout_accepted.setVisibility(View.VISIBLE);
            layout_arrived.setVisibility(View.VISIBLE);
            layout_user_call.setVisibility(View.VISIBLE);
            // layout_begin_trip.setVisibility(View.GONE);
        }else if(driverAllTripFeed.getStatus().equals("7")){
            layout_accepted.setVisibility(View.VISIBLE);
            layout_arrived.setVisibility(View.GONE);
            layout_user_call.setVisibility(View.VISIBLE);
            layout_begin_trip.setVisibility(View.VISIBLE);
        }else if(driverAllTripFeed.getStatus().equals("8")){
            layout_accepted.setVisibility(View.GONE);
            layout_arrived.setVisibility(View.GONE);
            layout_user_call.setVisibility(View.GONE);
            layout_begin_trip.setVisibility(View.GONE);
            layout_finished.setVisibility(View.VISIBLE);
        }else if(driverAllTripFeed.getStatus().equals("9")){
            layout_accepted.setVisibility(View.GONE);
            layout_arrived.setVisibility(View.GONE);
            layout_user_call.setVisibility(View.GONE);
            layout_begin_trip.setVisibility(View.GONE);
            layout_finished.setVisibility(View.GONE);
        }else if(driverAllTripFeed.getStatus().equals("6")){
            layout_accepted.setVisibility(View.GONE);
            layout_arrived.setVisibility(View.GONE);
            layout_user_call.setVisibility(View.GONE);
            layout_begin_trip.setVisibility(View.GONE);
            layout_finished.setVisibility(View.GONE);
        }
    }

    /*Driver status change call*/
    public void DriverCall(String callFun, final String DriverStatus){

        Ion.with(DriverTripDetailActivity.this)
                .load(callFun)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        loader.loaderObject().stop();
                        loader.loaderDismiss();
                        // do stuff with the result or error
                        if (e != null) {
                            Toast.makeText(DriverTripDetailActivity.this, "Login Error" + e, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            if (jsonObject.has("status") && jsonObject.getString("status").equals("success")) {

                                driverAllTripFeed.setStatus(DriverStatus);
                                if(DriverStatus.equals("7")){

                                    SharedPreferences.Editor booking_status = userPref.edit();
                                    booking_status.putString("booking_status","i have arrived");
                                    booking_status.commit();

                                    layout_accepted.setVisibility(View.VISIBLE);
                                    layout_arrived.setVisibility(View.GONE);
                                    layout_user_call.setVisibility(View.VISIBLE);
                                    layout_begin_trip.setVisibility(View.VISIBLE);
                                }else if(DriverStatus.equals("8")){

                                    SharedPreferences.Editor booking_status = userPref.edit();
                                    booking_status.putString("booking_status","begin trip");
                                    booking_status.commit();

                                    layout_accepted.setVisibility(View.GONE);
                                    layout_arrived.setVisibility(View.GONE);
                                    layout_user_call.setVisibility(View.GONE);
                                    layout_begin_trip.setVisibility(View.GONE);
                                    layout_finished.setVisibility(View.VISIBLE);
                                    driverAllTripFeed.setStartRideTime(Common.getCurrentTime());
                                }
                            }else if(jsonObject.getString("status").equals("false")){

                                Common.showMkError(DriverTripDetailActivity.this,jsonObject.getString("error code").toString());

                                if(jsonObject.has("Isactive") && jsonObject.getString("Isactive").equals("Inactive")) {

                                    SharedPreferences.Editor editor = userPref.edit();
                                    editor.clear();
                                    editor.commit();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(DriverTripDetailActivity.this, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 2500);
                                }
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }


                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("requestCode","DETAIL_REQUEST DriverDetail = "+requestCode+"=="+resultCode);
        if(requestCode == 1){
            if(data != null) {
                Intent di = new Intent();
                di.putExtra("position", position);
                di.putExtra("status", driverAllTripFeed.getStatus());
                di.putExtra("driver_flage", driverAllTripFeed.getDriverFlag());
                setResult(1, di);
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        layout_back_arrow = null;
        img_user_image = null;
        txt_booking_detail = null;
        txt_booking_id = null;
        txt_booking_id_val = null;
        txt_pickup_point = null;
        txt_pickup_point_val = null;
        txt_booking_date = null;
        txt_drop_point = null;
        txt_drop_point_val = null;
        txt_user_name = null;
        txt_user_email = null;
        txt_mobile_num = null;
        txt_distance = null;
        txt_distance_val = null;
        txt_distance_km = null;
        txt_total_price = null;
        txt_total_price_dol = null;
        txt_total_price_val = null;
        layout_accepted = null;
        layout_arrived = null;
        layout_begin_trip = null;
        layout_user_call = null;
        layout_finished = null;
    }
}
