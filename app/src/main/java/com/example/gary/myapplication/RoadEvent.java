package com.example.gary.myapplication;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**Road Event object
 * Created by gary on 17/02/2016.
 */
public class RoadEvent implements Parcelable{

    private String message, address;
    private Location location;
    private int likes;









    public RoadEvent(){
        location = null;
    }

    protected RoadEvent(Parcel in) {
        message = in.readString();
        address = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        likes = in.readInt();
    }

    public static final Creator<RoadEvent> CREATOR = new Creator<RoadEvent>() {
        @Override
        public RoadEvent createFromParcel(Parcel in) {
            return new RoadEvent(in);
        }

        @Override
        public RoadEvent[] newArray(int size) {
            return new RoadEvent[size];
        }
    };

    public void setMessage(String msg){
        message = msg;
    }

    public String getMessage(){
        return message;
    }

    public void setAddress(String add){
        address = add;
    }

    public String getAddress(){
        return address;
    }

    public void setLocation(Location loc){
        location = loc;
    }

    public Location getLocation(){
        return location;
    }

    public Boolean isLoc(){
        return location != null;
    }

    public void setLikes(int like_count){
        if(like_count > 40){
            likes = 40;
        }else{
            likes = like_count;
        }
    }

    public int getLikes(){
        return likes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(address);
        dest.writeParcelable(location, flags);
        dest.writeInt(likes);
    }
}
