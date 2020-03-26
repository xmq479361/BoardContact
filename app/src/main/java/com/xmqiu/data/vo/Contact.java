package com.xmqiu.data.vo;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
    public Contact(String id, String name) {

        this.id = id;
        this.name = name;
    }

    /**
     * 本地字段，用于存放本地企业通讯录缓存数据ID及个人通讯录数据ID
     */
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * 姓名
     */
    public String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
    }

    protected Contact(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
