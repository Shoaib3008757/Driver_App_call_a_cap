package com.texi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.texi.utils.Common;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    Typeface regularRoboto,boldRoboto,regularOpenSans,boldOpenSans;
    TextInputEditText edt_reg_username,edt_reg_password;
    TextView caption_signin,tv_signin,tv_forgot_password;

    //Error Alert
    RelativeLayout rlMainView;
    TextView tvTitle;

    LoaderView loader;

    SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        userPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

        //Font
        regularRoboto = Typeface.createFromAsset(getAssets(), getString(R.string.font_regular_roboto));
        boldRoboto=Typeface.createFromAsset(getAssets(), getString(R.string.font_bold_roboto));

        regularOpenSans = Typeface.createFromAsset(getAssets(), getString(R.string.font_regular_opensans));
        boldOpenSans=Typeface.createFromAsset(getAssets(), getString(R.string.font_bold_opensans));

        //Error Alert
        rlMainView=(RelativeLayout)findViewById(R.id.rlMainView);
        tvTitle=(TextView)findViewById(R.id.tvTitle);

        //SIGNIN
        caption_signin=(TextView)findViewById(R.id.caption_signin);
        caption_signin.setTypeface(boldOpenSans);

        //UserName & Password
        edt_reg_username=(TextInputEditText)findViewById(R.id.edt_reg_username);
        edt_reg_username.setTypeface(regularOpenSans);

        edt_reg_password=(TextInputEditText)findViewById(R.id.edt_reg_password);
        edt_reg_password.setTypeface(regularOpenSans);
        edt_reg_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b){
                if(b)
                    Utility.showMKPanelInfo(LoginActivity.this, getResources().getString(R.string.hint_password_msg),rlMainView,tvTitle,regularRoboto);
            }
        });

        //SignIn
        tv_signin=(TextView)findViewById(R.id.tv_signin);
        tv_signin.setTypeface(boldRoboto);
        tv_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isValidLogin()){

                    View focusCurrent = getCurrentFocus();
                    if (focusCurrent != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    loader=new LoaderView(LoginActivity.this);
                    loader.show();
                    callLoginWebservice();

                }

            }
        });

        //ForgotPassword
        tv_forgot_password=(TextView)findViewById(R.id.tv_forgot_password);
        tv_forgot_password.setTypeface(regularOpenSans);
        tv_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotActivity.class));
            }
        });

        Common.ValidationGone(LoginActivity.this,rlMainView,edt_reg_username);
        Common.ValidationGone(LoginActivity.this,rlMainView,edt_reg_password);
    }

    public boolean isValidLogin(){
        boolean isvalid_details=true;
        if(edt_reg_username.getText().toString().trim().equalsIgnoreCase("") || edt_reg_username.getText().toString().trim().length()==0) {
            isvalid_details=false;
            Utility.showMKPanelError(LoginActivity.this, getResources().getString(R.string.please_enter_username),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_username.getText().toString().length() != 0 && edt_reg_username.getText().toString().length() < 4){
            isvalid_details=false;
            Utility.showMKPanelError(LoginActivity.this, getResources().getString(R.string.minimum_user_charactor),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_username.getText().toString().length() != 0 && edt_reg_username.getText().toString().length() > 30){
            isvalid_details=false;
            Utility.showMKPanelError(LoginActivity.this, getResources().getString(R.string.maximum_user_charactor),rlMainView,tvTitle,regularRoboto);
        }
        else if(!Utility.isValidUserName(edt_reg_username.getText().toString())){
            isvalid_details=false;
            Utility.showMKPanelError(LoginActivity.this, getResources().getString(R.string.username_error),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_password.getText().toString().trim().length() == 0) {
            isvalid_details=false;
            Utility.showMKPanelError(LoginActivity.this, getResources().getString(R.string.please_enter_password),rlMainView,tvTitle,regularRoboto);
        }
        else if(!edt_reg_password.getText().toString().trim().matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$")) {
            isvalid_details=false;
            Utility.showMKPanelError(LoginActivity.this, getResources().getString(R.string.hint_password_msg),rlMainView,tvTitle,regularRoboto);
        }
        else if (edt_reg_password.getText().toString().trim().length() < 6 || edt_reg_password.getText().toString().trim().length() > 32) {
            isvalid_details=false;
            Utility.showMKPanelError(LoginActivity.this, getResources().getString(R.string.password_length),rlMainView,tvTitle,regularRoboto);
        }else if(edt_reg_password.getText().toString().trim().length() > 32) {
            isvalid_details=false;
            Utility.showMKPanelError(LoginActivity.this, getResources().getString(R.string.large_password),rlMainView,tvTitle,regularRoboto);
        }

        return isvalid_details;
    }


    public void callLoginWebservice(){

        String urlLogin=Url.driver_login+"username="+edt_reg_username.getText().toString().trim()+"&password="+edt_reg_password.getText().toString().trim()+"&isdevice=1";
        Log.d("urlLogin","urlLogin = "+urlLogin);
        Ion.with(LoginActivity.this)
                .load(urlLogin)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result){
                        loader.loaderObject().stop();
                        loader.loaderDismiss();
                        // do stuff with the result or error
                        if(e != null){
                            // Toast.makeText(LoginActivity.this, "Login Error"+e, Toast.LENGTH_LONG).show();
                            Common.showMkError(LoginActivity.this, e.getMessage());
                            return;
                        }
                        try {

                            JSONObject jsonObject = new JSONObject(result.toString());
                            if(jsonObject.has("status") && jsonObject.getString("status").equals("success")){

                        /*set Start Currency*/

                                JSONArray currencyArray = new JSONArray(jsonObject.getString("country_detail"));
                                for (int ci = 0; ci < currencyArray.length(); ci++) {
                                    JSONObject startEndTimeObj = currencyArray.getJSONObject(ci);
                                    Common.Currency = startEndTimeObj.getString("currency");
                                    Common.Country = startEndTimeObj.getString("country");

                                    SharedPreferences.Editor currency = userPref.edit();
                                    currency.putString("currency",startEndTimeObj.getString("currency"));
                                    currency.commit();
                                }

                                JSONArray jsonArray = jsonObject.getJSONArray("Driver_detail");
                                JSONObject jsonObjDriver=jsonArray.getJSONObject(0);
                                System.out.println("Driver Response >>>"+jsonObjDriver);

                                SharedPreferences.Editor id = userPref.edit();
                                id.putString("id",jsonObjDriver.getString("id"));
                                id.commit();

                                SharedPreferences.Editor name = userPref.edit();
                                name.putString("name",jsonObjDriver.getString("name"));
                                name.commit();

                                SharedPreferences.Editor user_name = userPref.edit();
                                user_name.putString("user_name",jsonObjDriver.getString("user_name"));
                                user_name.commit();

                                SharedPreferences.Editor email = userPref.edit();
                                email.putString("email",jsonObjDriver.getString("email"));
                                email.commit();

                                SharedPreferences.Editor password = userPref.edit();
                                password.putString("password",edt_reg_password.getText().toString().trim());
                                password.commit();

                                SharedPreferences.Editor gender = userPref.edit();
                                gender.putString("gender",jsonObjDriver.getString("gender"));
                                gender.commit();

                                SharedPreferences.Editor phone = userPref.edit();
                                phone.putString("phone",jsonObjDriver.getString("phone"));
                                phone.commit();

                                SharedPreferences.Editor dob = userPref.edit();
                                dob.putString("dob",jsonObjDriver.getString("dob"));
                                dob.commit();

                                SharedPreferences.Editor address = userPref.edit();
                                address.putString("address",jsonObjDriver.getString("address"));
                                address.commit();

                                SharedPreferences.Editor license_no = userPref.edit();
                                license_no.putString("license_no",jsonObjDriver.getString("license_no"));
                                license_no.commit();

                                SharedPreferences.Editor Lieasence_Expiry_Date = userPref.edit();
                                Lieasence_Expiry_Date.putString("Lieasence_Expiry_Date",jsonObjDriver.getString("Lieasence_Expiry_Date"));
                                Lieasence_Expiry_Date.commit();

                                SharedPreferences.Editor license_plate = userPref.edit();
                                license_plate.putString("license_plate",jsonObjDriver.getString("license_plate"));
                                license_plate.commit();

                                SharedPreferences.Editor Insurance = userPref.edit();
                                Insurance.putString("Insurance",jsonObjDriver.getString("Insurance"));
                                Insurance.commit();

                                SharedPreferences.Editor Car_Model = userPref.edit();
                                Car_Model.putString("Car_Model",jsonObjDriver.getString("Car_Model"));
                                Car_Model.commit();

                                SharedPreferences.Editor Car_Make = userPref.edit();
                                Car_Make.putString("Car_Make",jsonObjDriver.getString("Car_Make"));
                                Car_Make.commit();

                                SharedPreferences.Editor car_type = userPref.edit();
                                car_type.putString("car_type",jsonObjDriver.getString("car_type"));
                                car_type.commit();

                                SharedPreferences.Editor car_no = userPref.edit();
                                car_no.putString("car_no",jsonObjDriver.getString("car_no"));
                                car_no.commit();

                                SharedPreferences.Editor Seating_Capacity = userPref.edit();
                                Seating_Capacity.putString("Seating_Capacity",jsonObjDriver.getString("Seating_Capacity"));
                                Seating_Capacity.commit();

                                SharedPreferences.Editor image = userPref.edit();
                                image.putString("image",jsonObjDriver.getString("image"));
                                image.commit();

                                SharedPreferences.Editor status = userPref.edit();
                                status.putString("status",jsonObjDriver.getString("status"));
                                status.commit();

                                SharedPreferences.Editor isLogin = userPref.edit();
                                isLogin.putBoolean("is_login",true);
                                isLogin.commit();

                                Intent intent = new Intent(LoginActivity.this, DriverTripActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }else if(jsonObject.has("status") && jsonObject.getString("status").equals("failed")){
                                Utility.showMKPanelErrorServer(LoginActivity.this, jsonObject.getString("error code").toString(),rlMainView,tvTitle,regularRoboto);
                            }else if(jsonObject.has("status") && jsonObject.getString("status").equals("failed"))
                            {
                                Utility.showMKPanelErrorServer(LoginActivity.this, jsonObject.getString("error code").toString(),rlMainView,tvTitle,regularRoboto);
                            }else{
                                Utility.showMKPanelErrorServer(LoginActivity.this, jsonObject.getString("error code").toString(),rlMainView,tvTitle,regularRoboto);
                            }
                        }catch (Exception e1){
                            e1.printStackTrace();
                        }
                    }
                });
    }
}
