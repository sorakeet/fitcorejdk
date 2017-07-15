/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.geom;

import java.awt.*;
import java.io.Serializable;

public abstract class Line2D implements Shape, Cloneable{
    protected Line2D(){
    }

    public abstract Point2D getP1();

    public abstract Point2D getP2();

    public void setLine(Point2D p1,Point2D p2){
        setLine(p1.getX(),p1.getY(),p2.getX(),p2.getY());
    }

    public abstract void setLine(double x1,double y1,double x2,double y2);

    public void setLine(Line2D l){
        setLine(l.getX1(),l.getY1(),l.getX2(),l.getY2());
    }

    public int relativeCCW(double px,double py){
        return relativeCCW(getX1(),getY1(),getX2(),getY2(),px,py);
    }

    public abstract double getX1();

    public abstract double getY1();

    public abstract double getX2();

    public abstract double getY2();

    public static int relativeCCW(double x1,double y1,
                                  double x2,double y2,
                                  double px,double py){
        x2-=x1;
        y2-=y1;
        px-=x1;
        py-=y1;
        double ccw=px*y2-py*x2;
        if(ccw==0.0){
            // The point is colinear, classify based on which side of
            // the segment the point falls on.  We can calculate a
            // relative value using the projection of px,py onto the
            // segment - a negative value indicates the point projects
            // outside of the segment in the direction of the particular
            // endpoint used as the origin for the projection.
            ccw=px*x2+py*y2;
            if(ccw>0.0){
                // Reverse the projection to be relative to the original x2,y2
                // x2 and y2 are simply negated.
                // px and py need to have (x2 - x1) or (y2 - y1) subtracted
                //    from them (based on the original values)
                // Since we really want to get a positive answer when the
                //    point is "beyond (x2,y2)", then we want to calculate
                //    the inverse anyway - thus we leave x2 & y2 negated.
                px-=x2;
                py-=y2;
                ccw=px*x2+py*y2;
                if(ccw<0.0){
                    ccw=0.0;
                }
            }
        }
        return (ccw<0.0)?-1:((ccw>0.0)?1:0);
    }

    public int relativeCCW(Point2D p){
        return relativeCCW(getX1(),getY1(),getX2(),getY2(),
                p.getX(),p.getY());
    }

    public boolean intersectsLine(double x1,double y1,double x2,double y2){
        return linesIntersect(x1,y1,x2,y2,
                getX1(),getY1(),getX2(),getY2());
    }

    public static boolean linesIntersect(double x1,double y1,
                                         double x2,double y2,
                                         double x3,double y3,
                                         double x4,double y4){
        return ((relativeCCW(x1,y1,x2,y2,x3,y3)*
                relativeCCW(x1,y1,x2,y2,x4,y4)<=0)
                &&(relativeCCW(x3,y3,x4,y4,x1,y1)*
                relativeCCW(x3,y3,x4,y4,x2,y2)<=0));
    }

    public boolean intersectsLine(Line2D l){
        return linesIntersect(l.getX1(),l.getY1(),l.getX2(),l.getY2(),
                getX1(),getY1(),getX2(),getY2());
    }

    public double ptSegDistSq(double px,double py){
        return ptSegDistSq(getX1(),getY1(),getX2(),getY2(),px,py);
    }

    public static double ptSegDistSq(double x1,double y1,
                                     double x2,double y2,
                                     double px,double py){
        // Adjust vectors relative to x1,y1
        // x2,y2 becomes relative vector from x1,y1 to end of segment
        x2-=x1;
        y2-=y1;
        // px,py becomes relative vector from x1,y1 to test point
        px-=x1;
        py-=y1;
        double dotprod=px*x2+py*y2;
        double projlenSq;
        if(dotprod<=0.0){
            // px,py is on the side of x1,y1 away from x2,y2
            // distance to segment is length of px,py vector
            // "length of its (clipped) projection" is now 0.0
            projlenSq=0.0;
        }else{
            // switch to backwards vectors relative to x2,y2
            // x2,y2 are already the negative of x1,y1=>x2,y2
            // to get px,py to be the negative of px,py=>x2,y2
            // the dot product of two negated vectors is the same
            // as the dot product of the two normal vectors
            px=x2-px;
            py=y2-py;
            dotprod=px*x2+py*y2;
            if(dotprod<=0.0){
                // px,py is on the side of x2,y2 away from x1,y1
                // distance to segment is length of (backwards) px,py vector
                // "length of its (clipped) projection" is now 0.0
                projlenSq=0.0;
            }else{
                // px,py is between x1,y1 and x2,y2
                // dotprod is the length of the px,py vector
                // projected on the x2,y2=>x1,y1 vector times the
                // length of the x2,y2=>x1,y1 vector
                projlenSq=dotprod*dotprod/(x2*x2+y2*y2);
            }
        }
        // Distance to line is now the length of the relative point
        // vector minus the length of its projection onto the line
        // (which is zero if the projection falls outside the range
        //  of the line segment).
        double lenSq=px*px+py*py-projlenSq;
        if(lenSq<0){
            lenSq=0;
        }
        return lenSq;
    }

    public double ptSegDistSq(Point2D pt){
        return ptSegDistSq(getX1(),getY1(),getX2(),getY2(),
                pt.getX(),pt.getY());
    }

    public double ptSegDist(double px,double py){
        return ptSegDist(getX1(),getY1(),getX2(),getY2(),px,py);
    }

    public static double ptSegDist(double x1,double y1,
                                   double x2,double y2,
                                   double px,double py){
        return Math.sqrt(ptSegDistSq(x1,y1,x2,y2,px,py));
    }

    public double ptSegDist(Point2D pt){
        return ptSegDist(getX1(),getY1(),getX2(),getY2(),
                pt.getX(),pt.getY());
    }

    public double ptLineDistSq(double px,double py){
        return ptLineDistSq(getX1(),getY1(),getX2(),getY2(),px,py);
    }

    public static double ptLineDistSq(double x1,double y1,
                                      double x2,double y2,
                                      double px,double py){
        // Adjust vectors relative to x1,y1
        // x2,y2 becomes relative vector from x1,y1 to end of segment
        x2-=x1;
        y2-=y1;
        // px,py becomes relative vector from x1,y1 to test point
        px-=x1;
        py-=y1;
        double dotprod=px*x2+py*y2;
        // dotprod is the length of the px,py vector
        // projected on the x1,y1=>x2,y2 vector times the
        // length of the x1,y1=>x2,y2 vector
        double projlenSq=dotprod*dotprod/(x2*x2+y2*y2);
        // Distance to line is now the length of the relative point
        // vector minus the length of its projection onto the line
        double lenSq=px*px+py*py-projlenSq;
        if(lenSq<0){
            lenSq=0;
        }
        return lenSq;
    }

    public double ptLineDistSq(Point2D pt){
        return ptLineDistSq(getX1(),getY1(),getX2(),getY2(),
                pt.getX(),pt.getY());
    }

    public double ptLineDist(double px,double py){
        return ptLineDist(getX1(),getY1(),getX2(),getY2(),px,py);
    }

    public static double ptLineDist(double x1,double y1,
                                    double x2,double y2,
                                    double px,double py){
        return Math.sqrt(ptLineDistSq(x1,y1,x2,y2,px,py));
    }

    public double ptLineDist(Point2D pt){
        return ptLineDist(getX1(),getY1(),getX2(),getY2(),
                pt.getX(),pt.getY());
    }

    public Rectangle getBounds(){
        return getBounds2D().getBounds();
    }

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    public static class Float extends Line2D implements Serializable{
        private static final long serialVersionUID=6161772511649436349L;
        public float x1;
        public float y1;
        public float x2;
        public float y2;

        public Float(){
        }

        public Float(float x1,float y1,float x2,float y2){
            setLine(x1,y1,x2,y2);
        }

        public void setLine(float x1,float y1,float x2,float y2){
            this.x1=x1;
            this.y1=y1;
            this.x2=x2;
            this.y2=y2;
        }

        public Float(Point2D p1,Point2D p2){
            setLine(p1,p2);
        }

        public double getX1(){
            return (double)x1;
        }        public double getY1(){
            return (double)y1;
        }



        public Point2D getP1(){
            return new Point2D.Float(x1,y1);
        }

        public double getX2(){
            return (double)x2;
        }

        public double getY2(){
            return (double)y2;
        }

        public Point2D getP2(){
            return new Point2D.Float(x2,y2);
        }

        public void setLine(double x1,double y1,double x2,double y2){
            this.x1=(float)x1;
            this.y1=(float)y1;
            this.x2=(float)x2;
            this.y2=(float)y2;
        }

        public Rectangle2D getBounds2D(){
            float x, y, w, h;
            if(x1<x2){
                x=x1;
                w=x2-x1;
            }else{
                x=x2;
                w=x1-x2;
            }
            if(y1<y2){
                y=y1;
                h=y2-y1;
            }else{
                y=y2;
                h=y1-y2;
            }
            return new Rectangle2D.Float(x,y,w,h);
        }
    }

    public static class Double extends Line2D implements Serializable{
        private static final long serialVersionUID=7979627399746467499L;
        public double x1;
        public double y1;
        public double x2;
        public double y2;

        public Double(){
        }

        public Double(double x1,double y1,double x2,double y2){
            setLine(x1,y1,x2,y2);
        }

        public Double(Point2D p1,Point2D p2){
            setLine(p1,p2);
        }

        public double getX1(){
            return x1;
        }

        public double getY1(){
            return y1;
        }

        public Point2D getP1(){
            return new Point2D.Double(x1,y1);
        }

        public double getX2(){
            return x2;
        }

        public double getY2(){
            return y2;
        }

        public Point2D getP2(){
            return new Point2D.Double(x2,y2);
        }

        public void setLine(double x1,double y1,double x2,double y2){
            this.x1=x1;
            this.y1=y1;
            this.x2=x2;
            this.y2=y2;
        }

        public Rectangle2D getBounds2D(){
            double x, y, w, h;
            if(x1<x2){
                x=x1;
                w=x2-x1;
            }else{
                x=x2;
                w=x1-x2;
            }
            if(y1<y2){
                y=y1;
                h=y2-y1;
            }else{
                y=y2;
                h=y1-y2;
            }
            return new Rectangle2D.Double(x,y,w,h);
        }
    }    public boolean contains(double x,double y){
        return false;
    }



    public boolean contains(Point2D p){
        return false;
    }

    public boolean intersects(double x,double y,double w,double h){
        return intersects(new Rectangle2D.Double(x,y,w,h));
    }

    public boolean intersects(Rectangle2D r){
        return r.intersectsLine(getX1(),getY1(),getX2(),getY2());
    }

    public boolean contains(double x,double y,double w,double h){
        return false;
    }

    public boolean contains(Rectangle2D r){
        return false;
    }

    public PathIterator getPathIterator(AffineTransform at){
        return new LineIterator(this,at);
    }

    public PathIterator getPathIterator(AffineTransform at,double flatness){
        return new LineIterator(this,at);
    }
}
