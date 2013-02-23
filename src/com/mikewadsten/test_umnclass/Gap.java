package com.mikewadsten.test_umnclass;

import org.json.JSONObject;

public class Gap {
    private int mSpaceId, mLength;
    private String mBuilding, mRoomNum;
    private String mStart, mEnd;
    
    public Gap() {
        mSpaceId = 0;
        mBuilding = "";
        mRoomNum = "";
        mStart = mEnd = "";
        mLength = 0;
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
        String bldg, num;
        String startTime, endTime;
        try {
            // Pull out fields
            id = in.getInt("spaceID");
            bldg = in.getString("building");
            num = in.getString("roomnum");
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
        mBuilding = bldg;
        mLength = len;
        mRoomNum = num;
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
    
    /**
     * Get the fully qualified room name
     * @return the concatenation of the building and room number
     */
    public String getFullName() {
        return String.format("%s, Room %s", mBuilding, mRoomNum);
    }
    
    /**
     * Like getFullName, just a little shorter
     * @return something
     */
    public String getAbbrev() {
        return String.format("%s %s", mBuilding, mRoomNum);
    }
    
    public String getReversedName() {
        return String.format("%s %s", mRoomNum, mBuilding);
    }
    
    public String getBuilding() {
        return mBuilding;
    }
    public void setBuilding(String building) {
        mBuilding = building;
    }
    
    public String getRoomNumber() {
        return mRoomNum;
    }
    public void setRoomNumber(String num) {
        mRoomNum = num;
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
