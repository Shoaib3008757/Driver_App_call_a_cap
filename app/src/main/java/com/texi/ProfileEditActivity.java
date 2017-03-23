package com.texi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.squareup.picasso.Picasso;
import com.texi.Adapter.CarTypeAdapter;
import com.texi.utils.Common;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

public class ProfileEditActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener,CarTypeAdapter.OnCarTypeClickListener{

    Typeface regularRoboto,boldRoboto,regularOpenSans,boldOpenSans;

    RelativeLayout layout_next,layout_back;
    LinearLayout layout_header_step_first,layout_header_step_secound,layout_header_step_thd;
    TextInputEditText edt_reg_name,edt_reg_username,edt_reg_mobile,edt_reg_email,edt_reg_password,edt_reg_confirmpassword,edt_reg_dob,edt_reg_gender,edt_reg_address;
    TextInputEditText edt_reg_carmake,edt_reg_camodel,edt_reg_cartype,edt_reg_carnumber,edt_reg_seating_capacity;
    TextInputEditText edt_reg_driving_license,edt_reg_license_exp_date,edt_reg_license_plate,edt_reg_insuarance;
    View register_step_one,register_step_two,register_step_three;
    ImageView icon_pending_first,icon_pending_fir_thd,icon_pending_sec_thd,iv_user_photo;
    TextView tv_signin_register;

    int RegisterStep = 0;

    //Error Alert
    RelativeLayout rlMainView;
    TextView tvTitle;
    String CarTypeId;

    boolean isPersonalDetailsSelected=false,isVehicleDetailsSelected=false,isLegalDetailsSelected=false;
    Boolean SecoundStepValidation = false;
    Boolean ThrdStepValidation = false;
    Dialog Cameradialog;

    //Photos
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 3;
    public static final int REQUEST_GALLERY_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Uri mCapturedImageURI;
    public int isSelectPhoto=0;
    File photoFile;

    LoaderView loader;
    SharedPreferences userPref;
    CarTypeAdapter carTypeAdapter;
    Dialog CarTypeDialog;
    RecyclerView recycle_car_type;
    private RecyclerView.LayoutManager CarTypeLayoutManager;

    Calendar myCalendar;
    android.app.DatePickerDialog.OnDateSetListener BirthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        userPref = PreferenceManager.getDefaultSharedPreferences(ProfileEditActivity.this);

        layout_header_step_first = (LinearLayout)findViewById(R.id.layout_header_step_first);
        layout_header_step_secound = (LinearLayout)findViewById(R.id.layout_header_step_secound);
        layout_header_step_thd = (LinearLayout)findViewById(R.id.layout_header_step_thd);

        //Font
        regularRoboto = Typeface.createFromAsset(getAssets(), getString(R.string.font_regular_roboto));
        boldRoboto=Typeface.createFromAsset(getAssets(), getString(R.string.font_bold_roboto));

        regularOpenSans = Typeface.createFromAsset(getAssets(), getString(R.string.font_regular_opensans));
        boldOpenSans=Typeface.createFromAsset(getAssets(), getString(R.string.font_bold_opensans));

        register_step_one = (View) findViewById(R.id.register_step_one);
        register_step_two = (View) findViewById(R.id.register_step_two);
        register_step_three = (View) findViewById(R.id.register_step_three);

        if(Utility.arrayCarTypeList!=null && Utility.arrayCarTypeList.size() > 0){

            for(int i=0;i<Utility.arrayCarTypeList.size();i++){
                HashMap<String,String> hashMap=Utility.arrayCarTypeList.get(i);
                String strCarType=hashMap.get("car_type").toString();
                System.out.println("Selected CarType >>"+strCarType);
                System.out.println("Selected CarID >>"+hashMap.get("car_id").toString());
                System.out.println("Selected seating capacity >>"+hashMap.get("seating_capecity").toString());
                System.out.println("Selected car_rate >>"+hashMap.get("car_rate").toString());

            }
        }
        Log.d("CarType","CarType = "+Utility.arrayCarTypeList);

        //Error Alert
        rlMainView=(RelativeLayout)findViewById(R.id.rlMainView);
        tvTitle=(TextView)findViewById(R.id.tvTitle);

        isPersonalDetailsSelected=true;
        isVehicleDetailsSelected=false;
        isLegalDetailsSelected=false;

        iv_user_photo=(ImageView)findViewById(R.id.iv_user_photo);
        iv_user_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Cameradialog = new Dialog(ProfileEditActivity.this,android.R.style.Theme_Translucent_NoTitleBar);
                Cameradialog.setContentView(R.layout.custom_dialog);

                TextView tv_gallery =(TextView)Cameradialog.findViewById(R.id.tv_gallery);
                tv_gallery.setTypeface(boldRoboto);
                RelativeLayout galRelLayout = (RelativeLayout)Cameradialog.findViewById(R.id.gallery_layout);
                galRelLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Utility.checkAndRequestPermissionsGallery(ProfileEditActivity.this,REQUEST_GALLERY_PERMISSION)) {
                            launchGallery();
                        }
                    }
                });

                TextView tv_camera =(TextView)Cameradialog.findViewById(R.id.tv_camera);
                tv_camera.setTypeface(boldRoboto);
                RelativeLayout cameraRelLayout = (RelativeLayout)Cameradialog.findViewById(R.id.camera_layout);
                cameraRelLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Utility.checkAndRequestPermissions(ProfileEditActivity.this,REQUEST_ID_MULTIPLE_PERMISSIONS)) {
                            // carry on the normal flow, as the case of  permissions  granted.
                            requestForCameraPermission();
                        }
                    }
                });

                RelativeLayout dialogMainLayout = (RelativeLayout)Cameradialog.findViewById(R.id.dialog_main_layout);
                dialogMainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cameradialog.dismiss();
                    }
                });

                ImageView CancelImage = (ImageView)Cameradialog.findViewById(R.id.img_cancel);
                CancelImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cameradialog.dismiss();
                    }
                });

                Cameradialog.show();

            }
        });

        edt_reg_name=(TextInputEditText)findViewById(R.id.edt_reg_name);
        edt_reg_name.setTypeface(regularOpenSans);
        edt_reg_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    String strName=edt_reg_name.getText().toString();
                    edt_reg_name.setText(WordUtils.capitalize(strName));
                }

            }
        });
        edt_reg_username=(TextInputEditText)findViewById(R.id.edt_reg_username);
        edt_reg_username.setTypeface(regularOpenSans);
        edt_reg_mobile=(TextInputEditText)findViewById(R.id.edt_reg_mobile);
        edt_reg_mobile.setTypeface(regularOpenSans);
        edt_reg_email=(TextInputEditText)findViewById(R.id.edt_reg_email);
        edt_reg_email.setTypeface(regularOpenSans);
        edt_reg_password=(TextInputEditText)findViewById(R.id.edt_reg_password);
        edt_reg_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b){
                if(b)
                    Utility.showMKPanelInfo(ProfileEditActivity.this, getResources().getString(R.string.hint_password_msg),rlMainView,tvTitle,regularRoboto);
            }
        });
        edt_reg_password.setTypeface(regularOpenSans);
        edt_reg_confirmpassword=(TextInputEditText)findViewById(R.id.edt_reg_confirmpassword);
        edt_reg_confirmpassword.setTypeface(regularOpenSans);
        edt_reg_dob=(TextInputEditText)findViewById(R.id.edt_reg_dob);
        edt_reg_dob.setTypeface(regularOpenSans);
        edt_reg_dob.setText(userPref.getString("dob",""));

        edt_reg_confirmpassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    edt_reg_dob.performClick();
                    return true;
                }
                return false;
            }
        });

        final String splitDate[] = edt_reg_dob.getText().toString().split("-");
        Log.d("splitDate","splitDate = "+splitDate[0]+"=="+splitDate[1]+"=="+splitDate[2]);
        final Calendar defDate = Calendar.getInstance();
        defDate.set(Calendar.YEAR, Integer.parseInt(splitDate[2]));
        defDate.set(Calendar.MONTH, 8);
        defDate.set(Calendar.DAY_OF_MONTH, 11);

        myCalendar = Calendar.getInstance();
        BirthDate = new android.app.DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                String date = dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
                Calendar userAge = new GregorianCalendar(year,monthOfYear,dayOfMonth);
                Calendar minAdultAge = new GregorianCalendar();
                minAdultAge.add(Calendar.YEAR, -18);
                if(minAdultAge.before(userAge)){
                    edt_reg_dob.setText(userPref.getString("dob",""));
                    Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.dob_error),rlMainView,tvTitle,regularRoboto);
                }else{

                    if(!isFinishing() && rlMainView.getVisibility() == View.VISIBLE){
                        TranslateAnimation slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -100);
                        slideUp.setDuration(100);
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

                    edt_reg_dob.setText(date);
                    edt_reg_gender.performClick();
                }
            }
        };

        edt_reg_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                android.app.DatePickerDialog dpd = new android.app.DatePickerDialog(ProfileEditActivity.this, BirthDate, Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[0]), Integer.parseInt(splitDate[1]));
                Calendar minCal = Calendar.getInstance();
                minCal.add(Calendar.YEAR, -100);
                long hundredYearsAgo = minCal.getTimeInMillis();
                dpd.getDatePicker().setMinDate(hundredYearsAgo);
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                dpd.show();
            }
        });

        //Gender
        edt_reg_gender=(TextInputEditText)findViewById(R.id.edt_reg_gender);
        edt_reg_gender.setTypeface(regularOpenSans);
        edt_reg_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();

                int selectGender=0;
                if(edt_reg_gender.getText().toString().trim().length()>0 && edt_reg_gender.getText().toString().trim().equals("Male"))
                    selectGender=0;
                else if(edt_reg_gender.getText().toString().trim().length()>0 && edt_reg_gender.getText().toString().trim().equals("Female")){
                    selectGender=1;
                }

                MaterialDialog mdGender=new MaterialDialog.Builder(ProfileEditActivity.this)
                        .title(R.string.hint_gender)
                        .items(R.array.gender)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence gender) {
                                edt_reg_gender.setText(gender);
                                return true;
                            }
                        })
                        .positiveText(R.string.dialog_choose)
                        .show();

                if(edt_reg_gender.getText().toString().trim().length()>0)
                    mdGender.setSelectedIndex(selectGender);

            }
        });
        edt_reg_address=(TextInputEditText)findViewById(R.id.edt_reg_address);
        edt_reg_address.setTypeface(regularOpenSans);

        edt_reg_name.setText(userPref.getString("name",""));
        edt_reg_username.setText(userPref.getString("user_name",""));
        edt_reg_mobile.setText(userPref.getString("phone",""));
        edt_reg_email.setText(userPref.getString("email",""));
        edt_reg_password.setText(userPref.getString("password",""));
        edt_reg_confirmpassword.setText(userPref.getString("password",""));

        edt_reg_gender.setText(userPref.getString("gender",""));
        edt_reg_address.setText(userPref.getString("address",""));

        String driverImg = Url.imageurl+userPref.getString("image","");
        if(!userPref.getString("image","").equals(""))
            isSelectPhoto = 1;
        else
            isSelectPhoto = 0;
        Log.d("driverImg","driverImg = "+driverImg);
        Picasso.with(ProfileEditActivity.this)
                .load(Uri.parse(driverImg))
                .resize(250,250).centerCrop()
                .transform(new CircleTransformation(this))
                .into(iv_user_photo);

        //Personal Details End

        //Vehicle Details Start

        edt_reg_carmake=(TextInputEditText)findViewById(R.id.edt_reg_carmake);
        edt_reg_carmake.setTypeface(regularOpenSans);
        edt_reg_camodel=(TextInputEditText)findViewById(R.id.edt_reg_camodel);
        edt_reg_camodel.setTypeface(regularOpenSans);
        //Car Type
        edt_reg_cartype=(TextInputEditText)findViewById(R.id.edt_reg_cartype);
        edt_reg_cartype.setTypeface(regularOpenSans);

        edt_reg_carmake.setText(userPref.getString("Car_Make",""));
        edt_reg_camodel.setText(userPref.getString("Car_Model",""));


        if(Utility.arrayCarTypeList!=null && Utility.arrayCarTypeList.size() > 0){
            for(int i=0;i<Utility.arrayCarTypeList.size();i++){
                HashMap<String,String> hashMap=Utility.arrayCarTypeList.get(i);
                Log.d("hashMap","Selected hashMap = "+hashMap);
                String strCarType=hashMap.get("car_id").toString();

                if(strCarType.equals(userPref.getString("car_type",""))){
                    Log.d("CarTypeList","CarTypeList ="+strCarType+"=="+userPref.getString("car_type",""));
                    CarTypeId = hashMap.get("car_id").toString();
                    hashMap.put("is_selected","1");
                    hashMap.put("car_id",hashMap.get("car_id"));
                    hashMap.put("car_type",hashMap.get("car_type"));
                    hashMap.put("icon",hashMap.get("icon"));
                    hashMap.put("car_rate",hashMap.get("car_rate"));
                    hashMap.put("seating_capecity",hashMap.get("seating_capecity"));

                    Utility.arrayCarTypeList.set(i,hashMap);
                    edt_reg_cartype.setText(hashMap.get("car_type").toString());
                }
            }
            Log.d("CarTypeId","CarTypeId = "+CarTypeId);
        }


        edt_reg_cartype.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                view.clearFocus();

                int selectCarType=0;
                if(edt_reg_cartype.getText().toString().trim().length()>0 && edt_reg_cartype.getText().toString().trim().equals("Hatchback"))
                    selectCarType=0;
                else if(edt_reg_cartype.getText().toString().trim().length()>0 && edt_reg_cartype.getText().toString().trim().equals("Sedans")){
                    selectCarType=1;
                }else if(edt_reg_cartype.getText().toString().trim().length()>0 && edt_reg_cartype.getText().toString().trim().equals("Suv")){
                    selectCarType=2;
                }else if(edt_reg_cartype.getText().toString().trim().length()>0 && edt_reg_cartype.getText().toString().trim().equals("Truck")){
                    selectCarType=3;
                }else if(edt_reg_cartype.getText().toString().trim().length()>0 && edt_reg_cartype.getText().toString().trim().equals("Van")){
                    selectCarType=4;
                }else if(edt_reg_cartype.getText().toString().trim().length()>0 && edt_reg_cartype.getText().toString().trim().equals("Zeep")){
                    selectCarType=5;
                }

                Log.d("arrayCarTypeList","arrayCarTypeList = "+Utility.arrayCarTypeList);
                 /*Car Type Dialog Start*/
                CarTypeDialog = new Dialog(ProfileEditActivity.this,android.R.style.Theme_Translucent_NoTitleBar);
                CarTypeDialog.setContentView(R.layout.cartype_dialog);
                recycle_car_type = (RecyclerView)CarTypeDialog.findViewById(R.id.recycle_car_type);

                CarTypeLayoutManager = new LinearLayoutManager(ProfileEditActivity.this);
                recycle_car_type.setLayoutManager(CarTypeLayoutManager);

                carTypeAdapter = new CarTypeAdapter(ProfileEditActivity.this,Utility.arrayCarTypeList);
                carTypeAdapter.updateItems();
                carTypeAdapter.setOnCarTypeItemClickListener(ProfileEditActivity.this);
                recycle_car_type.setAdapter(carTypeAdapter);

                CarTypeDialog.show();
                /*Car Type Dialog End*/

            }
        });


        edt_reg_carnumber=(TextInputEditText)findViewById(R.id.edt_reg_carnumber);
        edt_reg_carnumber.setTypeface(regularOpenSans);
        edt_reg_carnumber.setText(userPref.getString("car_no",""));
        edt_reg_seating_capacity=(TextInputEditText)findViewById(R.id.edt_reg_seating_capacity);
        edt_reg_seating_capacity.setTypeface(regularOpenSans);
        edt_reg_seating_capacity.setText(userPref.getString("Seating_Capacity",""));
        icon_pending_first = (ImageView)findViewById(R.id.icon_pending_first);
        icon_pending_fir_thd = (ImageView)findViewById(R.id.icon_pending_fir_thd);
        icon_pending_sec_thd = (ImageView)findViewById(R.id.icon_pending_sec_thd);

        edt_reg_driving_license=(TextInputEditText)findViewById(R.id.edt_reg_driving_license);
        edt_reg_driving_license.setTypeface(regularOpenSans);
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for(int i = start; i < end; i++) {
                    if(!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        edt_reg_driving_license.setFilters(new InputFilter[]{filter});
        edt_reg_driving_license.setText(userPref.getString("license_no",""));

        edt_reg_license_exp_date=(TextInputEditText)findViewById(R.id.edt_reg_license_exp_date);
        edt_reg_license_exp_date.setTypeface(regularOpenSans);
        edt_reg_license_exp_date.setText(userPref.getString("Lieasence_Expiry_Date",""));
        edt_reg_license_exp_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                                                                        @Override
                                                                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                                                            String expiryDate = dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
                                                                            edt_reg_license_exp_date.setText(expiryDate);
                                                                        }
                                                                    },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setMinDate(now);
                Calendar cMax = Calendar.getInstance();
                cMax.set(Calendar.YEAR, 2050);
                cMax.set(Calendar.MONTH, Calendar.DECEMBER);
                cMax.set(Calendar.DAY_OF_MONTH, 31);
                dpd.setMaxDate(cMax);
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        edt_reg_license_plate=(TextInputEditText)findViewById(R.id.edt_reg_license_plate);
        edt_reg_license_plate.setTypeface(regularOpenSans);
        edt_reg_license_plate.setText(userPref.getString("license_plate",""));
        edt_reg_insuarance=(TextInputEditText)findViewById(R.id.edt_reg_insuarance);
        edt_reg_insuarance.setTypeface(regularOpenSans);
        edt_reg_insuarance.setText(userPref.getString("Insurance",""));

        layout_back = (RelativeLayout)findViewById(R.id.layout_back);
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int height = (int) getResources().getDimension(R.dimen.height_25);
                isLegalDetailsSelected=false;

                RegisterStep = RegisterStep - 1;
                Log.d("RegisterStep","RegisterStep = "+RegisterStep+"=="+SecoundStepValidation);
                if(RegisterStep == 0) {
                    layout_header_step_first.setVisibility(View.VISIBLE);
                    layout_header_step_secound.setVisibility(View.GONE);
                    layout_header_step_thd.setVisibility(View.GONE);

                    register_step_one.setVisibility(View.VISIBLE);
                    register_step_two.setVisibility(View.GONE);
                    register_step_three.setVisibility(View.GONE);
                    layout_next.setVisibility(View.VISIBLE);
                    layout_back.setVisibility(View.GONE);


                    if(SecoundStepValidation){
                        Picasso.with(ProfileEditActivity.this)
                                .load(R.drawable.icon_done)
                                .resize(height, height)
                                .into(icon_pending_first);
                    }else{
                        Picasso.with(ProfileEditActivity.this)
                                .load(R.drawable.icon_pending)
                                .resize(height, height)
                                .into(icon_pending_first);
                    }
                    if(ThrdStepValidation){
                        Picasso.with(ProfileEditActivity.this)
                                .load(R.drawable.icon_done)
                                .resize(height, height)
                                .into(icon_pending_fir_thd);
                    }else{
                        Picasso.with(ProfileEditActivity.this)
                                .load(R.drawable.icon_pending)
                                .resize(height, height)
                                .into(icon_pending_fir_thd);
                    }

                }else if(RegisterStep == 1){
                    layout_header_step_first.setVisibility(View.GONE);
                    layout_header_step_secound.setVisibility(View.VISIBLE);
                    layout_header_step_thd.setVisibility(View.GONE);

                    register_step_one.setVisibility(View.GONE);
                    register_step_two.setVisibility(View.VISIBLE);
                    register_step_three.setVisibility(View.GONE);
                    layout_next.setVisibility(View.VISIBLE);
                    layout_back.setVisibility(View.VISIBLE);

                    if(ThrdStepValidation){
                        Picasso.with(ProfileEditActivity.this)
                                .load(R.drawable.icon_done)
                                .resize(height, height)
                                .into(icon_pending_sec_thd);
                    }else{
                        Picasso.with(ProfileEditActivity.this)
                                .load(R.drawable.icon_pending)
                                .resize(height, height)
                                .into(icon_pending_sec_thd);
                    }

                }else if(RegisterStep == 2){
                    layout_header_step_first.setVisibility(View.GONE);
                    layout_header_step_secound.setVisibility(View.GONE);
                    layout_header_step_thd.setVisibility(View.VISIBLE);

                    register_step_one.setVisibility(View.GONE);
                    register_step_two.setVisibility(View.GONE);
                    register_step_three.setVisibility(View.VISIBLE);
                    layout_next.setVisibility(View.GONE);
                    layout_back.setVisibility(View.VISIBLE);

                }

                //realViewSwitcher.setCurrentScreen(RegisterStep,true);
            }
        });

        layout_next = (RelativeLayout)findViewById(R.id.layout_next);
        layout_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean firstStep = PersonalDetailValidation();
                boolean secoundStep = VehicleDetailValidation();
                boolean thdStep = isValidLegalDetails();
                isLegalDetailsSelected=true;
                Log.d("firstStap","firstStap = "+firstStep+"=="+secoundStep+"=="+RegisterStep);
                if(RegisterStep == 0) {
                    if (firstStep) {
                        layout_header_step_first.setVisibility(View.GONE);
                        layout_header_step_secound.setVisibility(View.VISIBLE);
                        layout_header_step_thd.setVisibility(View.GONE);

                        register_step_one.setVisibility(View.GONE);
                        register_step_two.setVisibility(View.VISIBLE);
                        register_step_three.setVisibility(View.GONE);
                        layout_next.setVisibility(View.VISIBLE);
                        layout_back.setVisibility(View.VISIBLE);
                        RegisterStep = RegisterStep + 1;
                        if(secoundStep){
                            Picasso.with(ProfileEditActivity.this)
                                    .load(R.drawable.icon_done)
                                    .into(icon_pending_first);
                        }else{
                            Picasso.with(ProfileEditActivity.this)
                                    .load(R.drawable.icon_pending)
                                    .into(icon_pending_first);
                        }
                        if(thdStep){
                            Picasso.with(ProfileEditActivity.this)
                                    .load(R.drawable.icon_done)
                                    .into(icon_pending_fir_thd);
                        }else{
                            Picasso.with(ProfileEditActivity.this)
                                    .load(R.drawable.icon_pending)
                                    .into(icon_pending_fir_thd);
                        }
                    }
                }else if(RegisterStep == 1) {
                    if (firstStep && secoundStep) {
                        RegisterStep = RegisterStep + 1;
                        layout_header_step_first.setVisibility(View.GONE);
                        layout_header_step_secound.setVisibility(View.GONE);
                        layout_header_step_thd.setVisibility(View.VISIBLE);

                        register_step_one.setVisibility(View.GONE);
                        register_step_two.setVisibility(View.GONE);
                        register_step_three.setVisibility(View.VISIBLE);

                        layout_back.setVisibility(View.VISIBLE);
                        layout_next.setVisibility(View.GONE);
                        if(thdStep){
                            Picasso.with(ProfileEditActivity.this)
                                    .load(R.drawable.icon_done)
                                    .into(icon_pending_fir_thd);
                        }else{
                            Picasso.with(ProfileEditActivity.this)
                                    .load(R.drawable.icon_pending)
                                    .into(icon_pending_fir_thd);
                        }
                    }
                }
            }
        });

        tv_signin_register=(TextView)findViewById(R.id.tv_signin_register);
        tv_signin_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean firstStep = PersonalDetailValidation();
                boolean secoundStep = VehicleDetailValidation();
                boolean thdStep = isValidLegalDetails();

                if(firstStep && secoundStep && thdStep){
                    if(Utility.arrayCarTypeList!=null && Utility.arrayCarTypeList.size() > 0){
                        for(int i=0;i<Utility.arrayCarTypeList.size();i++){
                            HashMap<String,String> hashMap=Utility.arrayCarTypeList.get(i);
                            String strCarType=hashMap.get("car_type").toString();
                            if(strCarType.equals(edt_reg_cartype.getText().toString().trim())){
                                System.out.println("Selected CarType >>"+strCarType);
                            }
                        }
                    }

                    loader=new LoaderView(ProfileEditActivity.this);
                    loader.show();
                    postEditProfile();
                }
            }
        });

        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_name);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_username);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_mobile);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_email);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_password);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_confirmpassword);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_dob);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_gender);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_address);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_carmake);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_camodel);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_cartype);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_carnumber);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_seating_capacity);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_driving_license);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_license_exp_date);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_license_plate);
        Common.ValidationGone(ProfileEditActivity.this,rlMainView,edt_reg_insuarance);
    }

    public void postEditProfile(){
        Log.d("CarTypeId","CarTypeId = "+CarTypeId+"=="+edt_reg_address.getText().toString().trim());
        Builders.Any.B DriverIonProfile = Ion.with(ProfileEditActivity.this).load(Url.driver_edit_profile);
        DriverIonProfile.setMultipartParameter("uid", userPref.getString("id",""))
                .setMultipartParameter("name", edt_reg_name.getText().toString().trim())
                .setMultipartParameter("username", edt_reg_username.getText().toString().trim())
                .setMultipartParameter("phone", edt_reg_mobile.getText().toString().trim())
                .setMultipartParameter("email", edt_reg_email.getText().toString().trim())
                .setMultipartParameter("password", edt_reg_password.getText().toString().trim())
                .setMultipartParameter("dob", edt_reg_dob.getText().toString().trim())
                .setMultipartParameter("gender", edt_reg_gender.getText().toString().trim())
                .setMultipartParameter("address", edt_reg_address.getText().toString().trim())
                .setMultipartParameter("Car_Make", edt_reg_carmake.getText().toString().trim())
                .setMultipartParameter("Car_Model", edt_reg_camodel.getText().toString().trim())
                .setMultipartParameter("car_type", CarTypeId)
                .setMultipartParameter("Car_no", edt_reg_carnumber.getText().toString().trim())
                .setMultipartParameter("Seating_Capacity", edt_reg_seating_capacity.getText().toString().trim())
                .setMultipartParameter("license_no", edt_reg_driving_license.getText().toString().trim())
                .setMultipartParameter("Lieasence_Expiry_Date",edt_reg_license_exp_date.getText().toString().trim())
                .setMultipartParameter("license_plate", edt_reg_license_plate.getText().toString().trim())
                .setMultipartParameter("Insurance", edt_reg_insuarance.getText().toString().trim())
                .setMultipartParameter("isdevice", "1");
        if(photoFile != null)
            DriverIonProfile.setMultipartFile("image",photoFile);
        else
            DriverIonProfile.setMultipartParameter("image","");
        DriverIonProfile.asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        System.out.println("Profile Response Car No >>>"+edt_reg_carnumber.getText().toString().trim());
                        System.out.println("Profile Response >>>"+result);
                        System.out.println("Profile Response >>>"+e);

                        loader.loaderObject().stop();
                        loader.loaderDismiss();

                        if(e != null){
                            //Toast.makeText(ProfileEditActivity.this, "Register Error"+e, Toast.LENGTH_LONG).show();
                            Common.showMkError(ProfileEditActivity.this,e.getMessage());
                            return;
                        }

                        try {

                            JSONObject jsonObject = new JSONObject(result.toString());
                            Log.d("jsonObject","jsonObject profile= "+jsonObject);
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

                                Common.profile_edit = 1;

                                finish();
                            }else if(jsonObject.has("status") && jsonObject.getString("status").equals("false")){

                                Utility.showMKPanelErrorServer(ProfileEditActivity.this, jsonObject.getString("error code").toString(),rlMainView,tvTitle,regularRoboto);

                                if(jsonObject.has("Isactive") && jsonObject.getString("Isactive").equals("Inactive")) {

                                    SharedPreferences.Editor editor = userPref.edit();
                                    editor.clear();
                                    editor.commit();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(ProfileEditActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 2500);
                                }
                            }else if(jsonObject.has("status") && jsonObject.getString("status").equals("failed")){
                                Utility.showMKPanelErrorServer(ProfileEditActivity.this, jsonObject.getString("error code").toString(),rlMainView,tvTitle,regularRoboto);
                            }
                        }catch (Exception e1){
                            e1.printStackTrace();
                        }



                    }
                });

    }

    private void launchGallery(){
        Intent libraryIntent = new Intent(Intent.ACTION_PICK);
        libraryIntent.setType("image/*");
        startActivityForResult(libraryIntent, 2);
    }

    public void setImage(Uri imagePath)
    {
        try{

            File file = new File(imagePath.getPath());
            ExifInterface exif = new ExifInterface(file.getPath());
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            Bitmap bitmap = resizeBitMapImage(imagePath.getPath(), 200, 200);
            Bitmap RotateBitmap = RotateBitmap(bitmap,rotationAngle);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentDateandTime = sdf.format(new Date());
            String myFinalImagePath = saveToInternalSorage(RotateBitmap, currentDateandTime);
            File fileimg=new File(myFinalImagePath);
            photoFile=fileimg;
            Picasso.with(ProfileEditActivity.this).load(fileimg).resize(250,250).centerCrop().transform(new CircleTransformation(this)).into(iv_user_photo);
            isSelectPhoto=1;
            Cameradialog.cancel();

        }
        catch(Exception es)
        {	es.printStackTrace();
            System.out.println("==== exceptin in setimage : "+es);
        }
    }

    public static Bitmap resizeBitMapImage(String filePath, int targetWidth, int targetHeight) {
        Bitmap bitMapImage = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            double sampleSize = 0;
            Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math.abs(options.outWidth
                    - targetWidth);
            if (options.outHeight * options.outWidth * 2 >= 1638) {
                sampleSize = scaleByHeight ? options.outHeight / targetHeight : options.outWidth / targetWidth;
                sampleSize = (int) Math.pow(2d, Math.floor(Math.log(sampleSize) / Math.log(2d)));
            }
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[128];
            while (true) {
                try {
                    options.inSampleSize = (int) sampleSize;
                    bitMapImage = BitmapFactory.decodeFile(filePath, options);
                    break;
                } catch (Exception ex) {
                    try {
                        sampleSize = sampleSize * 2;
                    } catch (Exception ex1) {

                    }
                }
            }
        } catch (Exception ex) {

        }
        return bitMapImage;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private Uri getImageUri()
    {
        File file1 = new File(Environment.getExternalStorageDirectory() + "/Texi");
        if (!file1.exists())
        {
            file1.mkdirs();
        }
        File file2 = new File(Environment.getExternalStorageDirectory() + "/Texi/Camera");
        if (!file2.exists())
        {
            file2.mkdirs();
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/Texi/Camera/"+System.currentTimeMillis()+".jpg");
        Uri imgUri = Uri.fromFile(file);
        return imgUri;
    }

    private String saveToInternalSorage(Bitmap bitmapImage,String imageName){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,imageName+".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath()+"/"+imageName+".jpg";
    }

    public void requestForCameraPermission() {
        final String permission = android.Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(ProfileEditActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileEditActivity.this, permission)) {
                requestForPermission(permission);
            } else {
                requestForPermission(permission);
            }
        } else {
            launchCamera();
        }
    }

    private void requestForPermission(final String permission) {
        ActivityCompat.requestPermissions(ProfileEditActivity.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    private void launchCamera() {

        String storageState = Environment.getExternalStorageState();

        if(storageState.equals(Environment.MEDIA_MOUNTED)){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mCapturedImageURI=getImageUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            startActivityForResult(intent, 1);
        }
        else
        {
            new AlertDialog.Builder(ProfileEditActivity.this).setMessage("External Storeage (SD Card) is required.\n\nCurrent state: "+ storageState) .setCancelable(true).create().show();
        }


    }

    public boolean PersonalDetailValidation(){

        boolean isvalid_details=true;
        if(isSelectPhoto==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_select_image),rlMainView,tvTitle,regularRoboto);
        }else if(edt_reg_name.getText().toString().trim().equalsIgnoreCase("") || edt_reg_name.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this,getResources().getString(R.string.enter_name),rlMainView,tvTitle,regularRoboto);
        } else if(edt_reg_name.getText().toString().length() != 0 && edt_reg_name.getText().toString().length() < 4){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.minimum_name_charactor),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_name.getText().toString().length() != 0 && edt_reg_name.getText().toString().length() > 30){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.maximum_name_charactor),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_username.getText().toString().trim().equalsIgnoreCase("") || edt_reg_username.getText().toString().trim().length()==0) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_username),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_username.getText().toString().length() != 0 && edt_reg_username.getText().toString().length() < 4){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.minimum_user_charactor),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_username.getText().toString().length() != 0 && edt_reg_username.getText().toString().length() > 30){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.maximum_user_charactor),rlMainView,tvTitle,regularRoboto);
        }
        else if(!Utility.isValidUserName(edt_reg_username.getText().toString())){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.username_error),rlMainView,tvTitle,regularRoboto);
        }
//        else if(isSelectPhoto==0){
//            isvalid_details=false;
//            Utility.showMKPanelError(RegisterActivity.this, getResources().getString(R.string.please_select_image),rlMainView,tvTitle,regularRoboto);
//        }
        else if(edt_reg_mobile.getText().toString().trim().equalsIgnoreCase("") || edt_reg_mobile.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_mobileno),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_mobile.getText().toString().trim().length() < 10) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.min_mobile_number),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_email.getText().toString().trim().equalsIgnoreCase("") || edt_reg_email.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_email),rlMainView,tvTitle,regularRoboto);
        }
        else if(!Utility.isValidEmail(edt_reg_email.getText().toString())) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_valid_email),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_password.getText().toString().trim().length() == 0) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_password),rlMainView,tvTitle,regularRoboto);
        }else if(!edt_reg_password.getText().toString().trim().matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$")) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.hint_password_msg),rlMainView,tvTitle,regularRoboto);
        }
        else if (edt_reg_password.getText().toString().trim().length() < 6 || edt_reg_password.getText().toString().trim().length() > 32) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.password_length),rlMainView,tvTitle,regularRoboto);
        }else if(edt_reg_password.getText().toString().trim().length() > 32) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.large_password),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_confirmpassword.getText().toString().trim().length() == 0) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_confirm_password),rlMainView,tvTitle,regularRoboto);
        }
        else if(!edt_reg_confirmpassword.getText().toString().trim().matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$")){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.hint_password_msg),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_confirmpassword.getText().toString().trim().length() < 6 || edt_reg_confirmpassword.getText().toString().trim().length() > 32) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.password_length),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_confirmpassword.getText().toString().trim().length() > 32) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.large_password),rlMainView,tvTitle,regularRoboto);
        }
        else if(!edt_reg_confirmpassword.getText().toString().trim().equals(edt_reg_password.getText().toString().trim())) {
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.password_does_not_match),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_dob.getText().toString().trim().equalsIgnoreCase("")||edt_reg_dob.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.enter_dob),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_gender.getText().toString().trim().equalsIgnoreCase("")||edt_reg_gender.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.select_gender),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_address.getText().toString().trim().equalsIgnoreCase("")||edt_reg_address.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_address),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_address.getText().toString().trim().matches("^[0-9]*$")){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_address_number),rlMainView,tvTitle,regularRoboto);
        }
        return isvalid_details;
    }

    public boolean VehicleDetailValidation(){

        boolean isvalid_details=true;
        if(edt_reg_carmake.getText().toString().trim().equalsIgnoreCase("") || edt_reg_carmake.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.enter_carmake),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_carmake.getText().toString().trim().matches("^[0-9]*$")){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.please_enter_carmake_number),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_camodel.getText().toString().trim().equalsIgnoreCase("") || edt_reg_camodel.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.enter_carmodel),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_cartype.getText().toString().trim().equalsIgnoreCase("") || edt_reg_cartype.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.enter_cartype),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_carnumber.getText().toString().trim().equalsIgnoreCase("") || edt_reg_carnumber.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.enter_carnumber),rlMainView,tvTitle,regularRoboto);
        }
        else if(!Utility.isValidUserName(edt_reg_carnumber.getText().toString())){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.carnumber_error),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_seating_capacity.getText().toString().trim().equalsIgnoreCase("")||edt_reg_seating_capacity.getText().toString().trim().length()==0){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.enter_seating_capacity),rlMainView,tvTitle,regularRoboto);
        }else if(!edt_reg_seating_capacity.getText().toString().trim().matches("^[0-9]*$")){
            isvalid_details=false;
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.seating_capacity_error),rlMainView,tvTitle,regularRoboto);
        }
        SecoundStepValidation = isvalid_details;
        return isvalid_details;
    }

    public boolean isValidLegalDetails(){
        boolean isvalid_details=true;
        if(edt_reg_driving_license.getText().toString().trim().equalsIgnoreCase("") || edt_reg_driving_license.getText().toString().trim().length()==0){
            isvalid_details=false;
            if(isLegalDetailsSelected)
                Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.enter_driving_license),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_driving_license.getText().toString().trim().length()>16){
            isvalid_details=false;
            if(isLegalDetailsSelected)
                Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.driving_license_length),rlMainView,tvTitle,regularRoboto);
        }else if(edt_reg_license_plate.getText().toString().trim().equalsIgnoreCase("") || edt_reg_license_plate.getText().toString().trim().length()==0){
            isvalid_details=false;
            if(isLegalDetailsSelected)
                Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.enter_license_plate),rlMainView,tvTitle,regularRoboto);
        }
        else if(!Utility.isValidUserName(edt_reg_license_plate.getText().toString())){
            isvalid_details=false;
            if(isLegalDetailsSelected)
                Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.license_plate_error),rlMainView,tvTitle,regularRoboto);
        }
        else if(edt_reg_insuarance.getText().toString().trim().equalsIgnoreCase("")||edt_reg_insuarance.getText().toString().trim().length()==0){
            isvalid_details=false;
            if(isLegalDetailsSelected)
                Utility.showMKPanelError(ProfileEditActivity.this,getResources().getString(R.string.enter_insuarance),rlMainView,tvTitle,regularRoboto);
        }
        else if(!Utility.isValidUserName(edt_reg_insuarance.getText().toString())){
            isvalid_details=false;
            if(isLegalDetailsSelected)
                Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.insuarance_error),rlMainView,tvTitle,regularRoboto);
        }
        ThrdStepValidation = isvalid_details;
        return isvalid_details;
    }


    @Override
    public void SelectCarType(int position) {
        if(Utility.arrayCarTypeList.size() > 0){

            for(int i=0;i<Utility.arrayCarTypeList.size();i++){
                Log.d("Car Image ","Car Image "+position+"=="+i);
                HashMap<String,String> cartypHasMap = Utility.arrayCarTypeList.get(i);
                cartypHasMap.put("is_selected","0");
                if(position == i) {
                    cartypHasMap.put("is_selected","1");
                    cartypHasMap.put("is_selected","1");
                    cartypHasMap.put("car_id",cartypHasMap.get("car_id"));
                    cartypHasMap.put("car_type",cartypHasMap.get("car_type"));
                    cartypHasMap.put("icon",cartypHasMap.get("icon"));
                    cartypHasMap.put("car_rate",cartypHasMap.get("car_rate"));
                    cartypHasMap.put("seating_capecity",cartypHasMap.get("seating_capecity"));
                    edt_reg_cartype.setText(cartypHasMap.get("car_type"));
                    CarTypeId = cartypHasMap.get("car_id").toString();
                }
                Utility.arrayCarTypeList.set(i,cartypHasMap);
            }

            CarTypeDialog.cancel();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
        Calendar userAge = new GregorianCalendar(year,monthOfYear,dayOfMonth);
        Calendar minAdultAge = new GregorianCalendar();
        minAdultAge.add(Calendar.YEAR, -18);
        if(minAdultAge.before(userAge)){
            edt_reg_dob.setText("");
            Utility.showMKPanelError(ProfileEditActivity.this, getResources().getString(R.string.dob_error),rlMainView,tvTitle,regularRoboto);
        }else{
            edt_reg_dob.setText(date);
        }
    }
}
