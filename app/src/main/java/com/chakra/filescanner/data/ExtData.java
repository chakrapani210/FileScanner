package com.chakra.filescanner.data;

import com.chakra.filescanner.Utils;

/**
 * Created by f68dpim on 9/30/16.
 */
public class ExtData implements Comparable<ExtData> {
    public String ext;
    public int count;

    public ExtData(String ext, int count) {
        this.ext = ext;
        this.count = count;
    }

    public ExtData(String ext) {
        this.ext = ext;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExtData) {
            return ext.equals(((ExtData) obj).ext);
        } else if(obj instanceof String){
            return ext.equals((String) obj);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return ext + ":" + count;
    }

    @Override
    public int hashCode() {
        return ext.hashCode();
    }

    @Override
    public int compareTo(ExtData another) {
        return another.count - count;
    }
}
