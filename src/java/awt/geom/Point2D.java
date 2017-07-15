/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.geom;

import java.io.Serializable;

public abstract class Point2D implements Cloneable{
    protected Point2D(){
    }

    public static double distanceSq(double x1,double y1,
                                    double x2,double y2){
        x1-=x2;
        y1-=y2;
        return (x1*x1+y1*y1);
    }

    public static double distance(double x1,double y1,
                                  double x2,double y2){
        x1-=x2;
        y1-=y2;
        return Math.sqrt(x1*x1+y1*y1);
    }

    public void setLocation(Point2D p){
        setLocation(p.getX(),p.getY());
    }

    public abstract void setLocation(double x,double y);

    public double distanceSq(double px,double py){
        px-=getX();
        py-=getY();
        return (px*px+py*py);
    }

    public abstract double getX();

    public abstract double getY();

    public double distanceSq(Point2D pt){
        double px=pt.getX()-this.getX();
        double py=pt.getY()-this.getY();
        return (px*px+py*py);
    }

    public double distance(double px,double py){
        px-=getX();
        py-=getY();
        return Math.sqrt(px*px+py*py);
    }

    public double distance(Point2D pt){
        double px=pt.getX()-this.getX();
        double py=pt.getY()-this.getY();
        return Math.sqrt(px*px+py*py);
    }

    public int hashCode(){
        long bits=java.lang.Double.doubleToLongBits(getX());
        bits^=java.lang.Double.doubleToLongBits(getY())*31;
        return (((int)bits)^((int)(bits>>32)));
    }

    public boolean equals(Object obj){
        if(obj instanceof Point2D){
            Point2D p2d=(Point2D)obj;
            return (getX()==p2d.getX())&&(getY()==p2d.getY());
        }
        return super.equals(obj);
    }

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    public static class Float extends Point2D implements Serializable{
        private static final long serialVersionUID=-2870572449815403710L;
        public float x;
        public float y;

        public Float(){
        }

        public Float(float x,float y){
            this.x=x;
            this.y=y;
        }

        public double getX(){
            return (double)x;
        }

        public void setLocation(float x,float y){
            this.x=x;
            this.y=y;
        }        public double getY(){
            return (double)y;
        }



        public void setLocation(double x,double y){
            this.x=(float)x;
            this.y=(float)y;
        }

        public String toString(){
            return "Point2D.Float["+x+", "+y+"]";
        }
    }

    public static class Double extends Point2D implements Serializable{
        private static final long serialVersionUID=6150783262733311327L;
        public double x;
        public double y;

        public Double(){
        }

        public Double(double x,double y){
            this.x=x;
            this.y=y;
        }

        public double getX(){
            return x;
        }

        public double getY(){
            return y;
        }

        public void setLocation(double x,double y){
            this.x=x;
            this.y=y;
        }

        public String toString(){
            return "Point2D.Double["+x+", "+y+"]";
        }
    }
}
