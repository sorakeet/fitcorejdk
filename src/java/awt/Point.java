/**
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.geom.Point2D;
import java.beans.Transient;

public class Point extends Point2D implements java.io.Serializable{
    private static final long serialVersionUID=-5276940640259749850L;
    public int x;
    public int y;

    public Point(){
        this(0,0);
    }

    public Point(int x,int y){
        this.x=x;
        this.y=y;
    }

    public Point(Point p){
        this(p.x,p.y);
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public void setLocation(double x,double y){
        this.x=(int)Math.floor(x+0.5);
        this.y=(int)Math.floor(y+0.5);
    }

    public boolean equals(Object obj){
        if(obj instanceof Point){
            Point pt=(Point)obj;
            return (x==pt.x)&&(y==pt.y);
        }
        return super.equals(obj);
    }

    @Transient
    public Point getLocation(){
        return new Point(x,y);
    }

    public void setLocation(Point p){
        setLocation(p.x,p.y);
    }

    public void setLocation(int x,int y){
        move(x,y);
    }

    public void move(int x,int y){
        this.x=x;
        this.y=y;
    }

    public void translate(int dx,int dy){
        this.x+=dx;
        this.y+=dy;
    }

    public String toString(){
        return getClass().getName()+"[x="+x+",y="+y+"]";
    }
}
