package mdash.cs.swarthmore.edu.weighttest;

import java.io.Serializable;

/**
 * Created by mborris1 on 6/6/14.
 */
public class Entry implements Serializable {
    private Integer mEpoch;
    private String mDate;
    private String mWeight;
    private String mFat;
    private String mLean;

    public Entry() {
        // empty constructor
    }

    public Entry(Integer epoch, String date, String weight, String fat, String lean){
        this.mEpoch = epoch;
        this.mDate = date;
        this.mWeight = weight;
        this.mFat = fat;
        this.mLean = lean;
    }

    public Integer getEpoch() {
        return mEpoch;
    }

    public void setEpoch(Integer epoch) {
        mEpoch = epoch;
    }

    public String getWeight() {
        return mWeight;
    }

    public void setWeight(String weight) {
        mWeight = weight;
    }

    public String getFat() {
        return mFat;
    }

    public void setFat(String fat) {
        mFat = fat;
    }

    public String getLean() {
        return mLean;
    }

    public void setLean(String lean) {
        mLean = lean;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

}