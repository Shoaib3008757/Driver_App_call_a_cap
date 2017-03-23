package com.texi.utils;

import java.io.Serializable;

/**
 * Created by techintegrity on 11/10/16.
 */
public class DriverAllTripFeed implements Serializable {

    String id;
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    String pickup_area;
    public String getPickupArea(){
        return pickup_area;
    }
    public void setPickupArea(String pickup_area){
        this.pickup_area = pickup_area;
    }

    String drop_area;
    public String getDropArea(){
        return drop_area;
    }
    public void setDropArea(String drop_area){
        this.drop_area = drop_area;
    }

    String status;
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status = status;
    }

    String car_type;
    public String getCarType(){
        return car_type;
    }
    public void setCarType(String car_type){
        this.car_type = car_type;
    }

    String user_detail;
    public String getuserDetail(){
        return user_detail;
    }
    public void setUserDetail(String user_detail){
        this.user_detail = user_detail;
    }

    String pickup_date_time;
    public String getPickupDateTime(){
        return pickup_date_time;
    }
    public void setPickupDateTime(String pickup_date_time){
        this.pickup_date_time = pickup_date_time;
    }

    String driver_flag;
    public String getDriverFlag(){
        return driver_flag;
    }
    public void setDriverFlag(String driver_flag){
        this.driver_flag = driver_flag;
    }

    String amount;
    public String getAmount(){
        return amount;
    }
    public void setAmount(String amount){
        this.amount = amount;
    }

    String car_icon;
    public String getCarIcon(){
        return car_icon;
    }
    public void setCarIcon(String car_icon){
        this.car_icon = car_icon;
    }

    String km;
    public String getKm(){
        return km;
    }
    public void setKm(String km){
        this.km = km;
    }


    String start_time;
    public String getStartTime(){
        return start_time;
    }
    public void setStartTime(String start_time){
        this.start_time = start_time;
    }

    String end_time;
    public String getEndTime(){
        return end_time;
    }
    public void setEndTime(String end_time){
        this.end_time = end_time;
    }

    String Server_time;
    public String getServerTime(){
        return Server_time;
    }
    public void setServerTime(String Server_time){
        this.Server_time = Server_time;
    }

    String strt_ride_time;
    public String getStartRideTime(){
        return strt_ride_time;
    }
    public void setStartRideTime(String strt_ride_time){
        this.strt_ride_time = strt_ride_time;
    }

    String end_ride_time;
    public String getEndRideTime(){
        return end_ride_time;
    }
    public void setEndRideTime(String end_ride_time){
        this.end_ride_time = end_ride_time;
    }

    String approx_time;
    public String getApproxTime(){
        return approx_time;
    }
    public void setApproxTime(String approx_time){
        this.approx_time = approx_time;
    }

    String per_minute_rate;
    public String getPerMinuteRate(){
        return per_minute_rate;
    }
    public void setPerMinuteRate(String per_minute_rate){
        this.per_minute_rate = per_minute_rate;
    }

    String pickup_lat;
    public String getPickupLat(){
        return pickup_lat;
    }
    public void setPickupLat(String pickup_lat){
        this.pickup_lat = pickup_lat;
    }

    String pickup_longs;
    public String getPickupLongs(){
        return pickup_longs;
    }
    public void setPickupLongs(String pickup_longs){
        this.pickup_longs = pickup_longs;
    }

}
