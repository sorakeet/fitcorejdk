/**
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.geom.Dimension2D;
import java.beans.Transient;

public class Dimension extends Dimension2D implements java.io.Serializable{
    private static final long serialVersionUID=4723952579491349524L;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    public int width;
    public int height;

    public Dimension(){
        this(0,0);
    }

    public Dimension(int width,int height){
        this.width=width;
        this.height=height;
    }

    public Dimension(Dimension d){
        this(d.width,d.height);
    }

    private static native void initIDs();

    public double getWidth(){
        return width;
    }

    public double getHeight(){
        return height;
    }

    public void setSize(double width,double height){
        this.width=(int)Math.ceil(width);
        this.height=(int)Math.ceil(height);
    }

    @Transient
    public Dimension getSize(){
        return new Dimension(width,height);
    }

    public void setSize(Dimension d){
        setSize(d.width,d.height);
    }

    public void setSize(int width,int height){
        this.width=width;
        this.height=height;
    }

    public int hashCode(){
        int sum=width+height;
        return sum*(sum+1)/2+width;
    }

    public boolean equals(Object obj){
        if(obj instanceof Dimension){
            Dimension d=(Dimension)obj;
            return (width==d.width)&&(height==d.height);
        }
        return false;
    }

    public String toString(){
        return getClass().getName()+"[width="+width+",height="+height+"]";
    }
}
