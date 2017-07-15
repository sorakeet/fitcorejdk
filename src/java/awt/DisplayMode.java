/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.lang.annotation.Native;

public final class DisplayMode{
    @Native
    public final static int BIT_DEPTH_MULTI=-1;
    @Native
    public final static int REFRESH_RATE_UNKNOWN=0;
    private Dimension size;
    private int bitDepth;
    private int refreshRate;

    public DisplayMode(int width,int height,int bitDepth,int refreshRate){
        this.size=new Dimension(width,height);
        this.bitDepth=bitDepth;
        this.refreshRate=refreshRate;
    }

    public int hashCode(){
        return getWidth()+getHeight()+getBitDepth()*7
                +getRefreshRate()*13;
    }

    public boolean equals(Object dm){
        if(dm instanceof DisplayMode){
            return equals((DisplayMode)dm);
        }else{
            return false;
        }
    }

    public boolean equals(DisplayMode dm){
        if(dm==null){
            return false;
        }
        return (getHeight()==dm.getHeight()
                &&getWidth()==dm.getWidth()
                &&getBitDepth()==dm.getBitDepth()
                &&getRefreshRate()==dm.getRefreshRate());
    }

    public int getHeight(){
        return size.height;
    }

    public int getWidth(){
        return size.width;
    }

    public int getBitDepth(){
        return bitDepth;
    }

    public int getRefreshRate(){
        return refreshRate;
    }
}
