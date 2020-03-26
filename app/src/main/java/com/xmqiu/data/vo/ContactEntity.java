package com.xmqiu.data.vo;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.xmqiu.ui.mailcreate.IMailCreateView.EXTRA_TAG_RECV;

public class ContactEntity implements Parcelable/*,Serializable*/ {
    /**
     * 收件人
     */
    public List<Contact> ciList = new CopyOnWriteArrayList<Contact>();
    /**
     * 抄送人
     */
    public List<Contact> copyciList = new CopyOnWriteArrayList<Contact>();
    /**
     * 密送人
     */
    public List<Contact> secret_ciList = new CopyOnWriteArrayList<Contact>();


    public Bundle attachContactToBundle(Bundle b) {
        b.putParcelable(EXTRA_TAG_RECV, this);
        return b;
    }

    //
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.ciList);
        dest.writeList(this.copyciList);
        dest.writeList(this.secret_ciList);
    }

    public ContactEntity() {
    }

    protected ContactEntity(Parcel in) {
        this.ciList = new ArrayList<Contact>();
        in.readList(this.ciList, Contact.class.getClassLoader());
        this.copyciList = new ArrayList<Contact>();
        in.readList(this.copyciList, Contact.class.getClassLoader());
        this.secret_ciList = new ArrayList<Contact>();
        in.readList(this.secret_ciList, Contact.class.getClassLoader());
    }

    public static final Parcelable.Creator<ContactEntity> CREATOR = new Parcelable.Creator<ContactEntity>() {
        @Override
        public ContactEntity createFromParcel(Parcel source) {
            return new ContactEntity(source);
        }

        @Override
        public ContactEntity[] newArray(int size) {
            return new ContactEntity[size];
        }
    };

    public boolean isEmpty() {
        return this.ciList.size() == 1 && this.copyciList.size() == 1 && this.secret_ciList.size() == 1;
    }
}