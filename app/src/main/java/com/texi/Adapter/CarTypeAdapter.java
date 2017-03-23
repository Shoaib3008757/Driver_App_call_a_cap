package com.texi.Adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.texi.CircleTransformation;
import com.texi.R;
import com.texi.Url;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by techintegrity on 12/10/16.
 */
public class CarTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    Activity activity;
    ArrayList<HashMap<String,String>> carTypeArray;
    Typeface OpenSans_Regular;
    private int itemsCount = 0;
    private static final int VIEW_TYPE_DEFAULT = 1;
    private static final int VIEW_TYPE_LOADER = 2;
    private boolean showLoadingView = false;

    private OnCarTypeClickListener onCarTypeClickListener;

    public CarTypeAdapter(Activity activity, ArrayList<HashMap<String,String>> carTypeArray){

        this.activity = activity;
        this.carTypeArray = carTypeArray;
        OpenSans_Regular = Typeface.createFromAsset(activity.getAssets(), "fonts/opensans-regular.ttf");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(activity).inflate(R.layout.car_type_layout, parent, false);
        CarTypeViewHolder carTypeViewHolder = new CarTypeViewHolder(view);
        carTypeViewHolder.layout_car_type_main.setOnClickListener(this);
        return carTypeViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        CarTypeViewHolder holder = (CarTypeViewHolder) viewHolder;
        if (getItemViewType(position) == VIEW_TYPE_DEFAULT) {
            bindCarTypeFeedItem(position, holder);
        } else if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem(holder);
        }
    }

    private void bindCarTypeFeedItem(int position, CarTypeViewHolder holder) {

        HashMap<String,String> carTypeHasMap = carTypeArray.get(position);
        Log.d("Car Image","Car Image = "+ Url.carImageUrl + carTypeHasMap.get("icon")+"=="+carTypeHasMap.get("is_selected"));
        Picasso.with(activity)
                .load(Uri.parse(Url.carImageUrl + carTypeHasMap.get("icon")))
                .placeholder(R.drawable.car_icon)
                .transform(new CircleTransformation(activity))
                .into(holder.img_car_image);

        holder.txt_car_type.setText(carTypeHasMap.get("car_type"));

        if(carTypeHasMap.get("is_selected").equals("1"))
            holder.layout_car_type_main.setBackgroundColor(activity.getResources().getColor(R.color.border_color));
        else
            holder.layout_car_type_main.setBackgroundColor(0);

        holder.layout_car_type_main.setTag(holder);


    }


    private void bindLoadingFeedItem(CarTypeViewHolder holder) {
        System.out.println("BindLoadingFeedItem >>>>>");
    }

    @Override
    public int getItemCount() {
        return carTypeArray.size();
    }

    @Override
    public void onClick(View view) {

        int viewId = view.getId();
        CarTypeViewHolder holder = (CarTypeViewHolder) view.getTag();

        holder.layout_car_type_main.setBackgroundColor(activity.getResources().getColor(R.color.border_color));
        if(viewId == R.id.layout_car_type_main){
            if(onCarTypeClickListener != null)
                onCarTypeClickListener.SelectCarType(holder.getPosition());
        }

    }

    public void updateItems() {
        itemsCount = carTypeArray.size();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoadingView && position == 0) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    public class CarTypeViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout_car_type_main;
        ImageView img_car_image;
        TextView txt_car_type;

        public CarTypeViewHolder(View view) {
            super(view);

            layout_car_type_main = (RelativeLayout)view.findViewById(R.id.layout_car_type_main);
            img_car_image = (ImageView)view.findViewById(R.id.img_car_image);
            txt_car_type = (TextView)view.findViewById(R.id.txt_car_type);
        }
    }

    public void setOnCarTypeItemClickListener(OnCarTypeClickListener onCarTypeClickListener) {
        this.onCarTypeClickListener = onCarTypeClickListener;
    }

    public interface OnCarTypeClickListener {

        public void SelectCarType(int position);
    }
}
