package com.texi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by techintegrity on 08/10/16.
 */
public class Utility {
    public static ArrayList<HashMap<String,String>> arrayCarTypeList;

    public static UserDetails userDetails = new UserDetails();

    /* To restrict Space Bar in Keyboard */
    public static InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidUserName(String str)
    {
        boolean isValid = false;
        String expression = "^[a-z_A-Z0-9]*$";
        CharSequence inputStr = str;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if(matcher.matches())
        {
            isValid = true;
        }
        return isValid;
    }


    public static void  showMKPanelErrorServer(final Activity act, String error_code, final RelativeLayout rlMainView, TextView tvTitle, Typeface typeface){
        if(!act.isFinishing() && (rlMainView.getVisibility() == View.GONE)){

            Log.d("rlMainView","rlMainView = "+rlMainView.getVisibility()+"=="+View.GONE);
            if((rlMainView.getVisibility() == View.GONE)) {
                rlMainView.setVisibility(View.VISIBLE);
            }

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

            rlMainView.setBackgroundResource(R.color.mk_error);
            tvTitle.setText(message);

            tvTitle.setTypeface(typeface);
            Animation slideUpAnimation = AnimationUtils.loadAnimation(act.getApplicationContext(),R.anim.slide_up_map);
            rlMainView.startAnimation(slideUpAnimation);

        }
    }

    public static void showMKPanelError(final Activity act, String message, final RelativeLayout rlMainView, TextView tvTitle, Typeface typeface){
        if(!act.isFinishing() && (rlMainView.getVisibility() == View.GONE)){

            Log.d("rlMainView","rlMainView = "+rlMainView.getVisibility()+"=="+View.GONE);
            if((rlMainView.getVisibility() == View.GONE)) {
                rlMainView.setVisibility(View.VISIBLE);
            }

            rlMainView.setBackgroundResource(R.color.mk_error);
            tvTitle.setText(message);

            tvTitle.setTypeface(typeface);
            Animation slideUpAnimation = AnimationUtils.loadAnimation(act.getApplicationContext(),R.anim.slide_up_map);
            rlMainView.startAnimation(slideUpAnimation);

        }
    }


    public static void showMKPanelInfo(final Activity act, String message,final RelativeLayout rlMainView,TextView tvTitle,Typeface typeface){
        if(!act.isFinishing() && (rlMainView.getVisibility() == View.GONE)){

            rlMainView.setBackgroundResource(R.color.mk_info);
            if((rlMainView.getVisibility() == View.GONE)) {
                rlMainView.setVisibility(View.VISIBLE);
            }
            tvTitle.setText(message);
            tvTitle.setTypeface(typeface);
            Animation slideUpAnimation = AnimationUtils.loadAnimation(act.getApplicationContext(),R.anim.slide_up_map);
            rlMainView.startAnimation(slideUpAnimation);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!act.isFinishing()){
                            TranslateAnimation slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -100);
                            slideUp.setDuration(2000);
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

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 3000);
        }
    }

//    public static void showMkError(final Activity act,String message)
//    {
//        if(!act.isFinishing()){
//            final MKInfoPanel mk=new MKInfoPanel(act, MKInfoPanel.MKInfoPanelType.MKInfoPanelTypeError, "",message, 2000);
//            mk.show();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        if(mk.isShowing() && !act.isFinishing())
//                            mk.cancel();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 4000);
//        }
//    }

    public static void showInternetInfo(final Activity act,String message)
    {
        if(!act.isFinishing()){
            final InternetInfoPanel mk = new InternetInfoPanel(act, InternetInfoPanel.InternetInfoPanelType.MKInfoPanelTypeInfo, "SUCCESS!",message, 2000);
            mk.show();
            mk.getIv_ok().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        if (mk.isShowing() && !act.isFinishing())
                            mk.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

//    public static void showMkInfo(final Activity act,String message)
//    {
//        if(!act.isFinishing()){
//            final MKInfoPanel mk=new MKInfoPanel(act, MKInfoPanel.MKInfoPanelType.MKInfoPanelTypeInfo, "SUCCESS!",message, 2000);
//            mk.show();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        if(mk.isShowing() && !act.isFinishing())
//                            mk.cancel();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 4000);
//        }
//    }


    public static void showDialogOK(Activity activity,String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(activity.getResources().getString(R.string.dialog_ok), okListener)
                .setNegativeButton(activity.getResources().getString(R.string.dialog_cancel), okListener)
                .create()
                .show();
    }


    public static boolean isNetworkAvailable(Activity act){
        ConnectivityManager connMgr = (ConnectivityManager)act.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            return true;
        }else{
            // display error
            return false;
        }
    }



    public static boolean checkAndRequestPermissionsGallery(Activity activity,int REQUEST_GALLERY_PERMISSION) {
        int permissionSendMessage = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_GALLERY_PERMISSION);
            return false;
        }
        return true;
    }

    public static boolean checkAndRequestPermissions(Activity activity,int REQUEST_ID_MULTIPLE_PERMISSIONS) {
        int permissionSendMessage = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}
