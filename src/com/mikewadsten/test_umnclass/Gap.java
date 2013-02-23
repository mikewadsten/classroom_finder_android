package com.mikewadsten.test_umnclass;

import org.json.JSONObject;

public class Gap {
    private int mSpaceId, mLength;
    private String mRoomname;
    private String mStart, mEnd;
    
    public Gap() {
        mSpaceId = 0;
        mRoomname = "";
        mStart = mEnd = "";
        mLength = 0;
    }
    
    public Gap(int spaceId, String roomname,
                String start, String end, int length) {
        mSpaceId = spaceId;
        mRoomname = roomname;
        mStart = start;
        mEnd = end;
        mLength = length;
    }
    
    /**
     * Get a Gap object from the given JSON object.
     * @param from JSON object to parse
     * @throws Exception if 'from' does not correspond to a Gap
     */
    public Gap(JSONObject from) throws Exception {
        if (!loadFromJson(from))
            throw new Exception("Could not get gap from JSON");
        
//        Log.d("Gap from JSON", String.format("%d: %s, %s-%s, %d",
//                mSpaceId, mRoomname, getStartAsString(), getEndAsString(), mLength));
    }
    
    /**
     * Given a JSON object, try to pull in the Gap data from it.
     * @param in JSON object to possibly load as a Gap
     * @return true if the parameter was loaded, false otherwise
     */
    public boolean loadFromJson(JSONObject in) {
        int id, len;
        String name;
        String startTime, endTime;
        try {
            // Pull out fields
            id = in.getInt("spaceID");
            name = in.getString("roomname");
            len = in.getInt("length");
            startTime = in.getString("start");
            endTime = in.getString("end");
        } catch (Exception e) {
//            Log.e("Gap.loadFromJson",
//                    String.format("Failed to load from JSON... %s",
//                                    e.getMessage()));
            return false;
        }
        mSpaceId = id;
        mLength = len;
        mRoomname = name;
        mStart = startTime;
        mEnd = endTime;
        
        return true;
    }
    
    public int getSpaceId() {
        return mSpaceId;
    }
    public void setSpaceId(int id) {
        mSpaceId = id;
    }
    
    public String getRoomName() {
        return mRoomname;
    }
    public void setRoomName(String name) {
        mRoomname = name;
    }
    
    public String getStartTime() {
        return mStart;
    }
    public void setStartTime(String start) {
        mStart = start;
    }
    
    public String getEndTime() {
        return mEnd;
    }
    public void setEndTime(String end) {
        mEnd = end;
    }
    
    public int getGapLength() {
        return mLength;
    }
    public void setGapLength(int length) {
        mLength = length;
    }
}
