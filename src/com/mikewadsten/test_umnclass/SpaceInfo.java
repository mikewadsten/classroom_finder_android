package com.mikewadsten.test_umnclass;

import org.json.JSONObject;

public class SpaceInfo {
    int mSpaceID, mCapacity;
    String mRoomUrl, mRoomName, mSeatType;
    boolean mChalk, mMarkers;
    
    public SpaceInfo(JSONObject from) throws Exception {
        if (!loadFromJson(from))
            throw new Exception("Could not load space info from JSON");
    }
    
    public boolean loadFromJson(JSONObject in) {
        int id, cap;
        boolean chalk, markers;
        String url, name, type;
        try {
            id = in.getInt("spaceID");
            cap = in.getInt("capacity");
            url = in.getString("room_url");
            name = in.getString("roomname");
            type = in.getString("seat_type");
            chalk = "yes".equals(in.getString("chalk"));
            markers = "yes".equals(in.getString("marker"));
        } catch (Exception e) {
            return false;
        }
        mSpaceID = id;
        mCapacity = cap;
        mChalk = chalk;
        mMarkers = markers;
        mRoomUrl = url;
        mRoomName = name;
        mSeatType = type;
        
        return true;
    }
    
    public int getSpaceId() {
        return mSpaceID;
    }
    public SpaceInfo setSpaceId(int id) {
        mSpaceID = id;
        return this;
    }
    
    public int getCapacity() {
        return mCapacity;
    }
    public SpaceInfo setCapacity(int cap) {
        mCapacity = cap;
        return this;
    }
    
    public boolean hasChalk() {
        return mChalk;
    }
    public SpaceInfo setChalk(boolean has) {
        mChalk = has;
        return this;
    }
    
    public boolean hasMarkers() {
        return mMarkers;
    }
    public SpaceInfo setMarkers(boolean has) {
        mMarkers = has;
        return this;
    }
    
    public String getUrl() {
        return mRoomUrl;
    }
    public SpaceInfo setUrl(String url) {
        mRoomUrl = url;
        return this;
    }
    
    public String getRoomName() {
        return mRoomName;
    }
    public SpaceInfo setRoomName(String num) {
        mRoomName = num;
        return this;
    }
    
    public String getSeatType() {
        return mSeatType;
    }
    public SpaceInfo setSeatType(String type) {
        mSeatType = type;
        return this;
    }
    
    public String toString() {
        return String.format("%d: %s, %s", mSpaceID, mRoomName, mSeatType);
    }
}
