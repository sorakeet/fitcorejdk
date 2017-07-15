/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.geom;

public abstract class Dimension2D implements Cloneable{
    protected Dimension2D(){
    }

    public abstract double getWidth();

    public abstract double getHeight();

    public void setSize(Dimension2D d){
        setSize(d.getWidth(),d.getHeight());
    }

    public abstract void setSize(double width,double height);

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }
}
