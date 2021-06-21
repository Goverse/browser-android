package com.goverse.browser.process;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

public class BrowserEvent implements Parcelable {

    public String fromObj;

    public int event;

    public String url;

    public String[] params;

    private int paramsLength;

    public ParcelFileDescriptor fileDescriptor;

    public BrowserEvent(int event, String url) {
        this.event = event;
        this.url = url;
    }

    public BrowserEvent(int event, String url, String[] params) {
        this.event = event;
        this.url = url;
        this.params = params;
    }

    public BrowserEvent(int event, String url, String[] params, String fromObj) {
        this.event = event;
        this.url = url;
        this.params = params;
        this.fromObj = fromObj;
    }

    protected BrowserEvent(Parcel in) {

        this.fromObj = in.readString();
        this.event = in.readInt();
        this.url = in.readString();
        this.paramsLength = in.readInt();
        if (paramsLength != 0) {
            this.params = new String[paramsLength];
            in.readStringArray(this.params);
        }
        this.fileDescriptor = in.readParcelable(ParcelFileDescriptor.class.getClassLoader());
    }

    public static final Creator<BrowserEvent> CREATOR = new Creator<BrowserEvent>() {
        @Override
        public BrowserEvent createFromParcel(Parcel in) {
            return new BrowserEvent(in);
        }

        @Override
        public BrowserEvent[] newArray(int size) {
            return new BrowserEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        this.paramsLength = params == null ? 0 : params.length;
        dest.writeString(this.fromObj);
        dest.writeInt(this.event);
        dest.writeString(this.url);
        dest.writeInt(this.paramsLength);
        if (this.paramsLength != 0) dest.writeStringArray(this.params);
        dest.writeParcelable(this.fileDescriptor, PARCELABLE_WRITE_RETURN_VALUE);
    }
}
