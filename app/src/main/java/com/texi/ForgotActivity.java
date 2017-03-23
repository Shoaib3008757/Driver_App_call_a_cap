package com.texi;

import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.texi.utils.Common;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotActivity extends AppCompatActivity {

    Typeface regularRoboto,boldRoboto,regularOpenSans,boldOpenSans;
    TextView tv_retrive_your_password;
    TextInputEditText edt_reg_email;

    //Error Alert
    RelativeLayout rlMainView;
    TextView tvTitle;

    LoaderView loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        loader=new LoaderView(ForgotActivity.this);


        //Font
        regularRoboto = Typeface.createFromAsset(getAssets(), getString(R.string.font_regular_roboto));
        boldRoboto=Typeface.createFromAsset(getAssets(), getString(R.string.font_bold_roboto));

        regularOpenSans = Typeface.createFromAsset(getAssets(), getString(R.string.font_regular_opensans));
        boldOpenSans=Typeface.createFromAsset(getAssets(), getString(R.string.font_bold_opensans));


        //Error Alert
        rlMainView=(RelativeLayout)findViewById(R.id.rlMainView);
        tvTitle=(TextView)findViewById(R.id.tvTitle);

        //Email
        edt_reg_email=(TextInputEditText)findViewById(R.id.edt_reg_email);
        edt_reg_email.setTypeface(regularOpenSans);

        //Retrive your password
        tv_retrive_your_password=(TextView)findViewById(R.id.tv_retrive_your_password);
        tv_retrive_your_password.setTypeface(boldRoboto);
        tv_retrive_your_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidForgot()){
                    callForgotPassword();
                }
            }
        });

        Common.ValidationGone(ForgotActivity.this,rlMainView,edt_reg_email);
    }

    public boolean isValidForgot(){
        boolean isvalid_details=true;
        if(edt_reg_email.getText().toString().trim().equalsIgnoreCase("") || edt_reg_email.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ForgotActivity.this, getResources().getString(R.string.please_enter_email),rlMainView,tvTitle,regularRoboto);
        }
        else if(!Utility.isValidEmail(edt_reg_email.getText().toString())) {
            isvalid_details=false;
            Utility.showMKPanelError(ForgotActivity.this, getResources().getString(R.string.please_enter_valid_email),rlMainView,tvTitle,regularRoboto);
        }
        return isvalid_details;
    }

    public void callForgotPassword(){
        loader.show();
        String DriverForPass = Url.driver_forgot_password+"email="+edt_reg_email.getText().toString().trim()+"&isdevice=1";
        Log.d("DriverForPass","DriverForPass = "+DriverForPass);
        Ion.with(ForgotActivity.this)
                .load(DriverForPass)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        System.out.println("Forgot Response >>>"+result);
                        System.out.println("Forgot Response >>>"+e);
                        loader.cancel();
                        if (e != null) {
                            //Toast.makeText(ForgotActivity.this, "Forgot Error"+e, Toast.LENGTH_LONG).show();
                            Common.ShowHttpErrorMessage(ForgotActivity.this, e.getMessage());
                            return;
                        }

                        try {
                            JSONObject JsonRes = new JSONObject(result.toString());
                            if(JsonRes.getString("status").equals("success")) {
                                Toast.makeText(ForgotActivity.this, JsonRes.getString("message").toString(), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }


                    }
                });


    }
}
