package com.mikewadsten.test_umnclass;

import org.json.JSONObject;

public class Gap {
    // gapID is the real uniqueness identifier
    private int mSpaceId, mLength, mGapId;
    private String mBuilding, mRoomNum;
    private String mStart, mEnd;
    
    public Gap() {
        mSpaceId = 0;
        mBuilding = "";
        mRoomNum = "";
        mStart = mEnd = "";
        mLength = 0;
        mGapId = -1;
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
        int id, len, gid;
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
            gid = in.getInt("gapID");
        } catch (Exception e) {
//            Log.e("Gap.loadFromJson",
//                    String.format("Failed to load from JSON... %s",
//                                    e.getMessage()));
            return false;
        }
        mSpaceId = id;
        mGapId = gid;
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
    public Gap setSpaceId(int id) {
        mSpaceId = id;
        return this;
    }
    
    public int getGapId() {
        return mGapId;
    }
    public Gap setGapId(int gid) {
        mGapId = gid;
        return this;
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
    public Gap setBuilding(String building) {
        mBuilding = building;
        return this;
    }
    
    public String getRoomNumber() {
        return mRoomNum;
    }
    public Gap setRoomNumber(String num) {
        mRoomNum = num;
        return this;
    }
    
    public String getStartTime() {
        return mStart;
    }
    public Gap setStartTime(String start) {
        mStart = start;
        return this;
    }
    
    public String getEndTime() {
        return mEnd;
    }
    public Gap setEndTime(String end) {
        mEnd = end;
        return this;
    }
    
    public int getGapLength() {
        return mLength;
    }
    public Gap setGapLength(int length) {
        mLength = length;
        return this;
    }
    
    public String toString() {
        return String.format("%d (%d): %s %s, %s to %s (%d)", mSpaceId, mGapId,
                mBuilding, mRoomNum, mStart, mEnd, mLength);
    }
}
