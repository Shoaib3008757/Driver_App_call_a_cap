package com.texi.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.squareup.picasso.Picasso;
import com.texi.CabPopupActivity;
import com.texi.ChangePasswordActivity;
import com.texi.CircleTransformation;
import com.texi.DriverTripActivity;
import com.texi.GPSTracker;
import com.texi.InternetInfoPanel;
import com.texi.LoginActivity;
import com.texi.ProfileEditActivity;
import com.texi.R;
import com.texi.Url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by techintegrity on 08/10/16.
 */
public class Common {

    //public static CustomCountdownTimer customCountdownTimer;
    public static DriverAllTripFeed driverAllTripFeed;
    public static String Currency = "";
    public static String Country = "";
    public static Socket socket;
    public static String BookingId = "";
    public static String ActionClick = "";
    public static CountDownTimer countDownTimer;
    public static String OnTripTime = "";
    public static String FinishedTripTime = "";
    public static String device_token = "";
    public static int profile_edit = 0;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    public static void ValidationGone(final Activity activity, final RelativeLayout rlMainView, TextInputEditText edt_reg_username){
        edt_reg_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("charSequence","charSequence = "+charSequence.length()+"=="+rlMainView.getVisibility()+"=="+ View.VISIBLE);
                if(charSequence.length() > 0 && rlMainView.getVisibility() == View.VISIBLE){
                    if(!activity.isFinishing()){
                        TranslateAnimation slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -100);
                        slideUp.setDuration(10);
                        slideUp.setFillAfter(true);
                        rlMainView.startAnimation(slideUp);
                        slideUp.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                rlMainView.setVisibility(View.GONE);
                            }
                        });

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public static void showMkError(final Activity act,String error_code)
    {
        if(!act.isFinishing()){

            String message = "";
            if(error_code.equals("1")){
                message = act.getResources().getString(R.string.inactive_user);
            }else if(error_code.equals("2")){
                message = act.getResources().getString(R.string.enter_correct_login_detail);
            }else if(error_code.equals("7")){
                message = act.getResources().getString(R.string.email_username_mobile_exit);
            }else if(error_code.equals("8")){
                message = act.getResources().getString(R.string.email_username_exit);
            }else if(error_code.equals("9")){
                message = act.getResources().getString(R.string.email_mobile_exit);
            }else if(error_code.equals("10")){
                message = act.getResources().getString(R.string.mobile_username_exit);
            }else if(error_code.equals("11")){
                message = act.getResources().getString(R.string.email_exit);
            }else if(error_code.equals("12")){
                message = act.getResources().getString(R.string.user_exit);
            }else if(error_code.equals("13")){
                message = act.getResources().getString(R.string.mobile_exit);
            }else if(error_code.equals("14")){
                message = act.getResources().getString(R.string.somthing_worng);
            }else if(error_code.equals("15") || error_code.equals("16")){
                message = act.getResources().getString(R.string.data_not_found);
            }else if(error_code.equals("19")){
                message = act.getResources().getString(R.string.vehicle_numbet_exits);
            }else if(error_code.equals("20")){
                message = act.getResources().getString(R.string.license_numbet_exits);
            }else if(error_code.equals("22")){
                message = act.getResources().getString(R.string.dublicate_booking);
            }

            Animation slideUpAnimation;

            final Dialog MKInfoPanelDialog = new Dialog(act,android.R.style.Theme_Translucent_NoTitleBar);

            MKInfoPanelDialog.setContentView(R.layout.mk_dialog_panel);
            MKInfoPanelDialog.show();
            slideUpAnimation = AnimationUtils.loadAnimation(act.getApplicationContext(),
                    R.anim.slide_up_map);

            RelativeLayout layout_info_panel = (RelativeLayout) MKInfoPanelDialog.findViewById(R.id.layout_info_panel);
            layout_info_panel.startAnimation(slideUpAnimation);

            RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) act.getResources().getDimension(R.dimen.height_40));
            buttonLayoutParams.setMargins(0, (int) act.getResources().getDimension(R.dimen.height_50), 0, 0);
            layout_info_panel.setLayoutParams(buttonLayoutParams);

            TextView subtitle = (TextView)MKInfoPanelDialog.findViewById(R.id.subtitle);
            subtitle.setText(message);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(MKInfoPanelDialog.isShowing() && !act.isFinishing())
                            MKInfoPanelDialog.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 2000);

        }
    }

    public void SlideMenuDesign(final SlidingMenu slidingMenu, final Activity activity, final String clickMenu) {

        Typeface Roboto_Regular =Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.font_regular_roboto));
        Typeface Roboto_Bold =Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.font_bold_roboto));

        final SharedPreferences userPref = PreferenceManager.getDefaultSharedPreferences(activity);

        TextView txt_user_name = (TextView)slidingMenu.findViewById(R.id.txt_user_name);
        txt_user_name.setText(userPref.getString("user_name",""));
        TextView txt_user_number = (TextView)slidingMenu.findViewById(R.id.txt_user_number);
        txt_user_number.setText(userPref.getString("phone",""));
        ImageView img_user = (ImageView)slidingMenu.findViewById(R.id.img_user);
        Picasso.with(activity)
                .load(Uri.parse(Url.imageurl + userPref.getString("image","")))
                .placeholder(R.drawable.user_photo)
                .transform(new CircleTransformation(activity))
                .into(img_user);

        RelativeLayout layout_my_trip = (RelativeLayout)slidingMenu.findViewById(R.id.layout_my_trip);
        layout_my_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingMenu.toggle();
                if(!clickMenu.equals("my trip")){
                    Intent mi = new Intent(activity, DriverTripActivity.class);
                    activity.startActivity(mi);
                    activity.finish();
                }
            }
        });

        RelativeLayout layout_cahnge_password = (RelativeLayout)slidingMenu.findViewById(R.id.layout_cahnge_password);
        layout_cahnge_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingMenu.toggle();
                if(!clickMenu.equals("change password")){
                    Intent mi = new Intent(activity, ChangePasswordActivity.class);
                    activity.startActivity(mi);
                    activity.finish();
                }
            }
        });

        RelativeLayout layout_footer_logout = (RelativeLayout)slidingMenu.findViewById(R.id.layout_footer_logout);
        layout_footer_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingMenu.toggle();
                MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                        .title(R.string.dialog_caption_logout)
                        .content(R.string.dialog_logout_msg)
                        .negativeText(R.string.dialog_cancel)
                        .positiveText(R.string.dialog_ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                if(Common.socket != null){

                                    double latitude = 0;
                                    double longitude = 0;
                                    GPSTracker gps = new GPSTracker(activity);
                                    if(gps.canGetLocation()){
                                        latitude = gps.getLatitude();
                                        longitude = gps.getLongitude();
                                    }else{
                                        gps.showSettingsAlert();
                                    }
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
                                        Log.d("emitobj", "emitobj = " + emitobj);

                                        Common.socket.emit("Create Driver Data", emitobj );
                                        Common.socket.disconnect();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                SharedPreferences.Editor editor = userPref.edit();
                                editor.clear();
                                editor.commit();

                                Intent logInt = new Intent(activity, LoginActivity.class);
                                logInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                logInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                logInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(logInt);
                            }
                        });

                MaterialDialog dialog = builder.build();
                dialog.show();



            }
        });

        RelativeLayout layout_user = (RelativeLayout)slidingMenu.findViewById(R.id.layout_user);
        layout_user.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                slidingMenu.toggle();
                Intent mi = new Intent(activity, ProfileEditActivity.class);
                activity.startActivity(mi);
            }
        });

    }

    public static void SocketFunction(final Activity activity, final Socket mSocket, Switch driver_status, double latitude, double longitude, Common common, SharedPreferences userPref){


        try {
            JSONArray locAry = new JSONArray();
            locAry.put(latitude);
            locAry.put(longitude);
            JSONObject emitobj = new JSONObject();
            emitobj.put("coords",locAry);
            emitobj.put("driver_name",userPref.getString("user_name",""));
            emitobj.put("driver_id", userPref.getString("id",""));
            emitobj.put("driver_status", "1");
            emitobj.put("car_type",userPref.getString("car_type",""));
            emitobj.put("isdevice","1");
            Log.d("emitobj", "emitobj = " + emitobj);

            mSocket.emit("Create Driver Data", emitobj);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        common.ChangeLocationSocket(activity,driver_status);

        Log.d("Socket","Socket = "+Common.socket);
        if(Common.socket != null){
            Common.socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    activity.runOnUiThread(new Runnable() {
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
            });
            Common.SearchedDriverDetail(activity);
        }


    }

    public void ChangeLocationSocket(final Activity activity, final Switch driver_available){
        LocationListener locationListener;
        LocationManager locationManager;
        boolean isPermission = false;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if(driver_available.isChecked()) {
                    SharedPreferences userPref= PreferenceManager.getDefaultSharedPreferences(activity);

                    try {
                        JSONArray locAry = new JSONArray();
                        locAry.put(location.getLatitude());
                        locAry.put(location.getLongitude());
                        JSONObject emitobj = new JSONObject();
                        emitobj.put("coords",locAry);
                        emitobj.put("driver_name",userPref.getString("user_name",""));
                        emitobj.put("driver_id", userPref.getString("id",""));
                        emitobj.put("driver_status", "1");
                        emitobj.put("car_type",userPref.getString("car_type",""));
                        emitobj.put("isdevice","1");
                        emitobj.put("booking_status",userPref.getString("booking_status",""));
                        Log.d("emitobj", "emitobj = " + emitobj);

                        Common.socket.emit("Create Driver Data", emitobj );

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Latitude", "disable");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Latitude","enable");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            isPermission = checkLocationPermission(locationManager,activity);
        }else{

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        }
        if(isPermission) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        }
    }

    public boolean checkLocationPermission(LocationManager locationManager,Activity activity)
    {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else
        {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                showGPSDisabledAlertToUser(activity);
            }

            return true;
        }
    }

    public static void SearchedDriverDetail(final Activity activity){

        Common.socket.on("Searched Driver Detail", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject data = (JSONObject) args[0];
                        Log.d("data", "connected data = " + data);
                        Intent ai = new Intent(activity, CabPopupActivity.class);
                        ai.putExtra("booking_data",data.toString());
                        activity.startActivity(ai);

                    }
                });

            }
        });


    }

    private void showGPSDisabledAlertToUser(final Activity activity)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public static boolean isNetworkAvailable(Activity act){

        ConnectivityManager connMgr = (ConnectivityManager)act.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            return true;
        } else {
            // display error
            return false;
        }

    }
    public static void showInternetInfo(final Activity act,String message)
    {
        if(!act.isFinishing()){
            final InternetInfoPanel mk = new InternetInfoPanel(act, InternetInfoPanel.InternetInfoPanelType.MKInfoPanelTypeInfo, "SUCCESS!",message, 2000);
            mk.show();
            mk.getIv_ok().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        if(mk.isShowing() && !act.isFinishing())
                            mk.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static boolean ShowHttpErrorMessage(Activity activity,String ErrorMessage){

        Log.d("ErrorMessage", "ErrorMessage = " + ErrorMessage);
        boolean Status = true;
        if (ErrorMessage != null && !ErrorMessage.equals("")) {
            if (ErrorMessage.contains("Connect to")) {
                Common.showInternetInfo(activity, "");
                Status = false;
            }else if(ErrorMessage.contains("failed to connect to")){
                Common.showInternetInfo(activity, "network not available");
                Status = false;
            }else if(ErrorMessage.contains("Internal Server Error")){
                Common.showMkError(activity, "Internal Server Error");
                Status = false;
            }else if(ErrorMessage.contains("Request Timeout")){
                Common.showMkError(activity, "Request Timeout");
                Status = false;
            }
        }else{
            Toast.makeText(activity, "Server Time Out", Toast.LENGTH_LONG).show();
            Status = false;
        }
        return Status;
    }

    public static String getCurrentTime(){
        SimpleDateFormat timeFormate = new SimpleDateFormat("HH:mm:ss");
        return timeFormate.format(Calendar.getInstance().getTime());
    }

    public static void showMkSucess(final Activity act,String message,String isHeader)
    {
        if(!act.isFinishing()){

            Animation slideUpAnimation;

            final Dialog MKInfoPanelDialog = new Dialog(act,android.R.style.Theme_Translucent_NoTitleBar);

            MKInfoPanelDialog.setContentView(R.layout.mk_dialog_panel);
            MKInfoPanelDialog.show();
            slideUpAnimation = AnimationUtils.loadAnimation(act.getApplicationContext(),
                    R.anim.slide_up_map);
            slideUpAnimation.setFillAfter(true);
            slideUpAnimation.setDuration(2000);

            RelativeLayout layout_info_panel = (RelativeLayout) MKInfoPanelDialog.findViewById(R.id.layout_info_panel);
            layout_info_panel.setBackgroundResource(R.color.sucess_color);
            layout_info_panel.startAnimation(slideUpAnimation);

            if(isHeader.equals("yes")) {
                RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) act.getResources().getDimension(R.dimen.height_40));
                buttonLayoutParams.setMargins(0, (int) act.getResources().getDimension(R.dimen.height_50), 0, 0);
                layout_info_panel.setLayoutParams(buttonLayoutParams);
            }

            TextView subtitle = (TextView)MKInfoPanelDialog.findViewById(R.id.subtitle);
            subtitle.setText(message);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (MKInfoPanelDialog.isShowing() && !act.isFinishing())
                            MKInfoPanelDialog.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 3000);

        }
    }

    public static CountDownTimer AcceptRejectTimer(final Activity activity, final DonutProgress timmer_progress, long accpet_time, final TextView minutes_value, final String activityName){
        final MediaPlayer mediaPlayer = MediaPlayer.create(activity.getApplicationContext(), R.raw.timmer_mussic);


        CountDownTimer customCountdownTimer = new CountDownTimer(accpet_time, 1000) {

            public void onTick(long millisUntilFinished) {
                timmer_progress.setProgress((int) (millisUntilFinished/1000));
                minutes_value.setText((int) (millisUntilFinished/1000)+" "+activity.getResources().getString(R.string.secound));

                Log.d("mediaPlayer","mediaPlayer = "+mediaPlayer.isPlaying());
                if(!mediaPlayer.isPlaying())
                    mediaPlayer.start();
            }

            public void onFinish() {
                timmer_progress.setProgress(0);
                mediaPlayer.stop();
                if(activityName.equals("cabpopupActivity")) {
                    activity.finish();

                }
            }
        };
        return customCountdownTimer;
    }
}
