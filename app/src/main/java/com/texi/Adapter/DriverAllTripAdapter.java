package com.texi.Adapter;


import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import com.squareup.picasso.Picasso;
import com.texi.CircleTransformation;
import com.texi.R;
import com.texi.Url;
import com.texi.utils.Common;
import com.texi.utils.CustomMap;
import com.texi.utils.DriverAllTripFeed;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by techintegrity on 11/10/16.
 */
public class DriverAllTripAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener,OnMapReadyCallback {

    Activity activity;
    ArrayList<DriverAllTripFeed> TripArray;
    private int itemsCount = 0;
    private static final int VIEW_TYPE_DEFAULT = 1;
    private static final int VIEW_TYPE_LOADER = 2;
    private boolean showLoadingView = false;

    Typeface OpenSans_Regular,OpenSans_Semi_Bold,OpenSans_Light,Roboto_Bold;

    private OnAllTripClickListener onAllTripClickListener;

    SharedPreferences userPref;

    long accpet_time = 0;
    boolean is_pulltorefresh;
    boolean isAccepted = true;

    Dialog AcceptRejectDialog;

    private GoogleMap mMap;
    CustomMap customMap;
    Bundle savedInstState;
    CountDownTimer customCountdownTimer;
    LatLng UserLarLng;
    String addressTitle;
    MediaPlayer mediaPlayer;
    DonutProgress timmer_progress;
    TextView minutes_value;

    public DriverAllTripAdapter(Activity act, ArrayList<DriverAllTripFeed> trpArray, boolean is_pulltorefresh, Bundle savedInstState){
        activity = act;
        TripArray = trpArray;
        this.is_pulltorefresh = is_pulltorefresh;
        this.savedInstState = savedInstState;
        userPref = PreferenceManager.getDefaultSharedPreferences(activity);

        OpenSans_Regular = Typeface.createFromAsset(activity.getAssets(), "fonts/opensans-regular.ttf");
        OpenSans_Semi_Bold = Typeface.createFromAsset(activity.getAssets(), "fonts/openSans_semibold.ttf");
        OpenSans_Light = Typeface.createFromAsset(activity.getAssets(), "fonts/OpenSans-Light_0.ttf");
        Roboto_Bold = Typeface.createFromAsset(activity.getAssets(), "fonts/roboto_bold.ttf");

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(activity).inflate(R.layout.driver_trip_layout, parent, false);
        AllTripViewHolder allTrpViewHol = new AllTripViewHolder(view);
        allTrpViewHol.layout_footer_detail.setOnClickListener(this);
        allTrpViewHol.layout_all_trip.setOnClickListener(this);
        allTrpViewHol.layout_detail.setOnClickListener(this);

        return allTrpViewHol;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        AllTripViewHolder holder = (AllTripViewHolder) viewHolder;
        if (getItemViewType(position) == VIEW_TYPE_DEFAULT) {
            bindCabDetailFeedItem(position, holder);
        } else if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem(holder);
        }
    }

    private void bindCabDetailFeedItem(final int position, final AllTripViewHolder holder) {

        holder.txt_current_booking.setTypeface(OpenSans_Semi_Bold);
        holder.txt_trip_date.setTypeface(OpenSans_Regular);
        holder.txt_pickup_address.setTypeface(OpenSans_Light);
        holder.txt_drop_address.setTypeface(OpenSans_Light);
        holder.txt_booking_id.setTypeface(OpenSans_Semi_Bold);
        holder.txt_booking_id_val.setTypeface(OpenSans_Regular);

        final DriverAllTripFeed allTripFeed = TripArray.get(position);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.d("Timing","Timing one= "+allTripFeed.getDriverFlag());
//        if(position == 0)
//        {
//            allTripFeed.setDriverFlag("0");
//            allTripFeed.setStatus("1");
//        }
        if(allTripFeed.getDriverFlag().equals("0") && !allTripFeed.getStatus().equals("4")) {

            holder.txt_current_booking.setText(activity.getResources().getString(R.string.pending));
            Picasso.with(activity)
                    .load(R.drawable.status_pending)
                    .into(holder.img_status);
            holder.layout_status_cancle.setVisibility(View.VISIBLE);
            holder.img_user_image.setVisibility(View.GONE);
            holder.layout_detail.setVisibility(View.GONE);

            AcceptRejectDialog = new Dialog(activity,R.style.Theme_AppCompat_Translucent);
            AcceptRejectDialog.setContentView(R.layout.accept_reject_dialog_panel);
            AcceptRejectDialog.setCancelable(false);

            Display display = activity.getWindowManager().getDefaultDisplay();
            RelativeLayout layout_main = (RelativeLayout)AcceptRejectDialog.findViewById(R.id.layout_main);
            layout_main.getLayoutParams().height = (int) (display.getHeight() * 0.72);

            customMap = (CustomMap)AcceptRejectDialog.findViewById(R.id.mapview);
            MapsInitializer.initialize(activity);
            customMap.onCreate(savedInstState);
            customMap.onCreate(null);
            customMap.onResume();
            customMap.getMapAsync(this);

            minutes_value = (TextView)AcceptRejectDialog.findViewById(R.id.minutes_value);
            timmer_progress = (DonutProgress)AcceptRejectDialog.findViewById(R.id.timmer_progress);
            TextView txt_address_val = (TextView)AcceptRejectDialog.findViewById(R.id.txt_address_val);
            txt_address_val.setText(allTripFeed.getPickupArea());

            addressTitle = allTripFeed.getPickupArea();

            Log.d("Lotlon","dialog Lotlon = "+allTripFeed.getPickupLat()+"=="+allTripFeed.getPickupLongs());
            UserLarLng = new LatLng(Double.parseDouble(allTripFeed.getPickupLat()), Double.parseDouble(allTripFeed.getPickupLongs()));

            String EndTime = allTripFeed.getEndTime();
            String ServerTime = allTripFeed.getServerTime();

            try {
                Date date1 = simpleDateFormat.parse(EndTime);
                Date date2 = simpleDateFormat.parse(ServerTime);
                accpet_time = date1.getTime() - date2.getTime();
                Log.d("different","different = "+accpet_time);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            //accpet_time = 60000;

            if (accpet_time > 0 && accpet_time <= 60000) {
                mediaPlayer = MediaPlayer.create(activity.getApplicationContext(), R.raw.timmer_mussic);

                customCountdownTimer = new CountDownTimer(accpet_time, 1000) {

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
                        AcceptRejectDialog.cancel();

                        allTripFeed.setDriverFlag("2");
                        allTripFeed.setStatus("5");
                        // notifyDataSetChanged();
                        if(Common.BookingId != null && !Common.BookingId.equals("")) {
                            notifyItemChanged(position);
                        }
                    }
                };
                //if(!Common.BookingId.equals("")) {
                customCountdownTimer.start();
                // }

                RelativeLayout layout_accept_popup = (RelativeLayout)AcceptRejectDialog.findViewById(R.id.layout_accept_popup);
                layout_accept_popup.setOnClickListener(this);
                layout_accept_popup.setTag(holder);
                RelativeLayout layout_decline_popup = (RelativeLayout)AcceptRejectDialog.findViewById(R.id.layout_decline_popup);
                layout_decline_popup.setOnClickListener(this);
                layout_decline_popup.setTag(holder);

                TextView txt_accept_popup = (TextView)AcceptRejectDialog.findViewById(R.id.txt_accept_popup);
                txt_accept_popup.setTypeface(Roboto_Bold);
                TextView txt_decline_popup = (TextView)AcceptRejectDialog.findViewById(R.id.txt_decline_popup);
                txt_decline_popup.setTypeface(Roboto_Bold);

                AcceptRejectDialog.show();
            } else {
                holder.txt_current_booking.setText(activity.getResources().getString(R.string.user_cancelled));
                Picasso.with(activity)
                        .load(R.drawable.status_user_cancelled)
                        .into(holder.img_status);
                holder.layout_status_cancle.setVisibility(View.VISIBLE);
                holder.img_user_image.setVisibility(View.VISIBLE);

                Picasso.with(activity)
                        .load(R.drawable.cancelled_stemp)
                        .placeholder(R.drawable.cancelled_stemp)
                        .into(holder.img_user_image);

                holder.layout_detail.setVisibility(View.VISIBLE);

            }

        }else if(allTripFeed.getDriverFlag().equals("1") && !allTripFeed.getStatus().equals("4") && !allTripFeed.getStatus().equals("7") && !allTripFeed.getStatus().equals("8")) {
            holder.txt_current_booking.setText(activity.getResources().getString(R.string.accepted));
            Picasso.with(activity)
                    .load(R.drawable.status_accepted)
                    .into(holder.img_status);
            holder.layout_status_cancle.setVisibility(View.VISIBLE);
            holder.img_user_image.setVisibility(View.VISIBLE);
            holder.layout_detail.setVisibility(View.VISIBLE);

            if(allTripFeed.getuserDetail() != null && !allTripFeed.getuserDetail().equals("")) {
                try {
                    JSONObject DrvObj = new JSONObject(allTripFeed.getuserDetail());
                    if(!DrvObj.getString("facebook_id").equals("") && DrvObj.getString("image").equals("")){
                        String facebookImage = Url.FacebookImgUrl + DrvObj.getString("facebook_id").toString() + "/picture?type=large";
                        Log.d("facebookImage","facebookImage = "+facebookImage);
                        Picasso.with(activity)
                                .load(facebookImage)
                                .placeholder(R.drawable.user_photo)
                                .resize(200, 200)
                                .transform(new CircleTransformation(activity))
                                .into(holder.img_user_image);
                    }else {
                        Picasso.with(activity)
                                .load(Uri.parse(Url.userImageUrl + DrvObj.getString("image")))
                                .placeholder(R.drawable.user_photo)
                                .transform(new CircleTransformation(activity))
                                .into(holder.img_user_image);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }else if(allTripFeed.getDriverFlag().equals("2") || allTripFeed.getStatus().equals("4")) {
            holder.txt_current_booking.setText(activity.getResources().getString(R.string.user_cancelled));
            Picasso.with(activity)
                    .load(R.drawable.status_user_cancelled)
                    .into(holder.img_status);
            holder.layout_status_cancle.setVisibility(View.VISIBLE);
            holder.img_user_image.setVisibility(View.VISIBLE);

            Picasso.with(activity)
                    .load(R.drawable.cancelled_stemp)
                    .placeholder(R.drawable.cancelled_stemp)
                    .into(holder.img_user_image);

            holder.layout_detail.setVisibility(View.VISIBLE);

        }else if(allTripFeed.getDriverFlag().equals("3") || allTripFeed.getStatus().equals("9")) {
            holder.txt_current_booking.setText(activity.getResources().getString(R.string.completed));
            Picasso.with(activity)
                    .load(R.drawable.status_completed)
                    .into(holder.img_status);
            holder.layout_status_cancle.setVisibility(View.VISIBLE);
            holder.img_user_image.setVisibility(View.VISIBLE);
            holder.layout_detail.setVisibility(View.VISIBLE);

            if(allTripFeed.getuserDetail() != null && !allTripFeed.getuserDetail().equals("")) {
                try {
                    JSONObject DrvObj = new JSONObject(allTripFeed.getuserDetail());
                    if(!DrvObj.getString("facebook_id").equals("") && DrvObj.getString("image").equals("")){
                        String facebookImage = Url.FacebookImgUrl + DrvObj.getString("facebook_id").toString() + "/picture?type=large";
                        Log.d("facebookImage","facebookImage = "+facebookImage);
                        Picasso.with(activity)
                                .load(facebookImage)
                                .placeholder(R.drawable.user_photo)
                                .resize(200, 200)
                                .transform(new  CircleTransformation(activity))
                                .into(holder.img_user_image);
                    }else {
                        Picasso.with(activity)
                                .load(Uri.parse(Url.userImageUrl + DrvObj.getString("image")))
                                .placeholder(R.drawable.user_photo)
                                .transform(new CircleTransformation(activity))
                                .into(holder.img_user_image);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }else if(allTripFeed.getStatus().equals("7") || allTripFeed.getStatus().equals("8")) {
            int StatusImg = R.drawable.status_on_trip;
            if(allTripFeed.getStatus().equals("8")) {
                holder.txt_current_booking.setText(activity.getResources().getString(R.string.on_trip));
                StatusImg = R.drawable.status_on_trip;

                SharedPreferences.Editor booking_status = userPref.edit();
                booking_status.putString("booking_status","begin trip");
                booking_status.commit();
            }
            else if(allTripFeed.getStatus().equals("7")) {
                holder.txt_current_booking.setText(activity.getResources().getString(R.string.driver_arrived));
                StatusImg = R.drawable.status_driver_arrived;
            }
            Picasso.with(activity)
                    .load(StatusImg)
                    .into(holder.img_status);
            holder.layout_status_cancle.setVisibility(View.VISIBLE);
            holder.img_user_image.setVisibility(View.VISIBLE);

            holder.layout_detail.setVisibility(View.VISIBLE);

            try {
                JSONObject DrvObj = new JSONObject(allTripFeed.getuserDetail());
                if(!DrvObj.getString("facebook_id").equals("") && DrvObj.getString("image").equals("")){
                    String facebookImage = Url.FacebookImgUrl + DrvObj.getString("facebook_id").toString() + "/picture?type=large";
                    Log.d("facebookImage","facebookImage = "+facebookImage);
                    Picasso.with(activity)
                            .load(facebookImage)
                            .placeholder(R.drawable.user_photo)
                            .resize(200, 200)
                            .transform(new  CircleTransformation(activity))
                            .into(holder.img_user_image);
                }else {
                    Picasso.with(activity)
                            .load(Uri.parse(Url.userImageUrl + DrvObj.getString("image")))
                            .placeholder(R.drawable.user_photo)
                            .transform(new CircleTransformation(activity))
                            .into(holder.img_user_image);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String pickup_date_time = "";
        try {
            Date parceDate = simpleDateFormat.parse(allTripFeed.getPickupDateTime());
            SimpleDateFormat parceDateFormat = new SimpleDateFormat("dd MMM yyyy");
            pickup_date_time = parceDateFormat.format(parceDate.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d("Booking id","Booking id = "+allTripFeed.getId());
        holder.txt_trip_date.setText(pickup_date_time);
        holder.txt_pickup_address.setText(allTripFeed.getPickupArea());
        holder.txt_drop_address.setText(allTripFeed.getDropArea());
        holder.txt_booking_id_val.setText(allTripFeed.getId());

        holder.layout_footer_detail.setTag(holder);
        holder.layout_all_trip.setTag(holder);
        holder.layout_detail.setTag(holder);


        if(getItemCount() > 9 && getItemCount() == position+1) {
            if (onAllTripClickListener != null) {
                Log.d("position", "position = " + position+"=="+getItemCount());
                onAllTripClickListener.scrollToLoad(position);
            }
        }
    }

    private void bindLoadingFeedItem(final AllTripViewHolder holder) {
        System.out.println("BindLoadingFeedItem >>>>>");
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoadingView && position == 0) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return TripArray.size();
    }

    public void updateItems() {
        itemsCount = TripArray.size();
        notifyDataSetChanged();
    }

    public void updateItemsFilter(ArrayList<DriverAllTripFeed> allTripArray) {
        TripArray = allTripArray;
        itemsCount = TripArray.size();
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        int viewId = v.getId();
        final AllTripViewHolder holder = (AllTripViewHolder) v.getTag();
        if(viewId == R.id.layout_all_trip || viewId == R.id.layout_footer_detail || viewId == R.id.layout_detail){
            if(this.onAllTripClickListener != null)
                this.onAllTripClickListener.GoTripDetail(holder.getAdapterPosition());
        }else if(viewId == R.id.layout_accept_popup){
            customCountdownTimer.onFinish();
            AcceptRejectDialog.cancel();
            if(this.onAllTripClickListener != null)
                this.onAllTripClickListener.AcceptCabBookin(holder.getPosition());
        }else if(viewId == R.id.layout_decline_popup){


            MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                    .title(R.string.delete_trip)
                    .content(R.string.are_you_sure_delete_trip)
                    .negativeText(R.string.dialog_cancel)
                    .positiveText(R.string.dialog_ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            customCountdownTimer.onFinish();
                            AcceptRejectDialog.cancel();
                            if(onAllTripClickListener != null)
                                onAllTripClickListener.RejectCabBookin(holder.getPosition(),"");
                        }
                    }).onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    });

            MaterialDialog dialog = builder.build();
            dialog.show();


        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.addMarker(new MarkerOptions().position(UserLarLng)
                .title(addressTitle));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(UserLarLng)      // Sets the center of the map to location user
                .zoom(10)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public class AllTripViewHolder extends RecyclerView.ViewHolder{

        TextView txt_current_booking;
        TextView txt_trip_date;
        TextView txt_pickup_address;
        TextView txt_drop_address;
        TextView txt_booking_id;
        TextView txt_booking_id_val;
        RelativeLayout layout_footer_detail;
        LinearLayout layout_all_trip;
        RelativeLayout layout_status_cancle;
        ImageView img_status;
        ImageView img_user_image;
        RelativeLayout layout_detail;


        public AllTripViewHolder(View view) {
            super(view);

            txt_current_booking = (TextView)view.findViewById(R.id.txt_current_booking);
            txt_trip_date = (TextView)view.findViewById(R.id.txt_trip_date);
            txt_pickup_address = (TextView)view.findViewById(R.id.txt_pickup_address);
            txt_drop_address = (TextView)view.findViewById(R.id.txt_drop_address);
            txt_booking_id = (TextView)view.findViewById(R.id.txt_booking_id);
            txt_booking_id_val = (TextView)view.findViewById(R.id.txt_booking_id_val);
            layout_footer_detail = (RelativeLayout)view.findViewById(R.id.layout_footer_detail);
            layout_all_trip = (LinearLayout)view.findViewById(R.id.layout_all_trip);
            layout_status_cancle = (RelativeLayout)view.findViewById(R.id.layout_status_cancle);
            img_status = (ImageView)view.findViewById(R.id.img_status);
            img_user_image = (ImageView)view.findViewById(R.id.img_user_image);
            layout_detail = (RelativeLayout) view.findViewById(R.id.layout_detail);


        }
    }

    public void setOnAllTripItemClickListener(OnAllTripClickListener onAllTripClickListener) {
        this.onAllTripClickListener = onAllTripClickListener;
    }

    public interface OnAllTripClickListener {
        public void AcceptCabBookin(int position);
        public void RejectCabBookin(int position,String timerStart);
        public void scrollToLoad(int position);
        public void GoTripDetail(int position);
    }
}
