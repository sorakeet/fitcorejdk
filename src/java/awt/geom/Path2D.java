/**
 * Copyright (c) 2006, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.geom;

import sun.awt.geom.Curve;

import java.awt.*;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Arrays;

public abstract class Path2D implements Shape, Cloneable{
    public static final int WIND_EVEN_ODD=PathIterator.WIND_EVEN_ODD;
    public static final int WIND_NON_ZERO=PathIterator.WIND_NON_ZERO;
    static final int INIT_SIZE=20;
    static final int EXPAND_MAX=500;
    static final int EXPAND_MAX_COORDS=EXPAND_MAX*2;
    static final int EXPAND_MIN=10; // ensure > 6 (cubics)
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO=(byte)PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO=(byte)PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO=(byte)PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO=(byte)PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE=(byte)PathIterator.SEG_CLOSE;
    // Note: It would be nice to have this return Path2D
    // but one of our subclasses (GeneralPath) needs to
    // offer "public Object clone()" for backwards
    // compatibility so we cannot restrict it further.
    // REMIND: Can we do both somehow?
    private static final byte SERIAL_STORAGE_FLT_ARRAY=0x30;
    private static final byte SERIAL_STORAGE_DBL_ARRAY=0x31;
    private static final byte SERIAL_SEG_FLT_MOVETO=0x40;
    private static final byte SERIAL_SEG_FLT_LINETO=0x41;
    private static final byte SERIAL_SEG_FLT_QUADTO=0x42;
    private static final byte SERIAL_SEG_FLT_CUBICTO=0x43;
    private static final byte SERIAL_SEG_DBL_MOVETO=0x50;
    private static final byte SERIAL_SEG_DBL_LINETO=0x51;
    private static final byte SERIAL_SEG_DBL_QUADTO=0x52;
    private static final byte SERIAL_SEG_DBL_CUBICTO=0x53;
    private static final byte SERIAL_SEG_CLOSE=0x60;
    private static final byte SERIAL_PATH_END=0x61;
    transient byte[] pointTypes;
    transient int numTypes;    abstract int pointCrossings(double px,double py);
    transient int numCoords;
    transient int windingRule;    abstract int rectCrossings(double rxmin,double rymin,
                               double rxmax,double rymax);

    Path2D(){
    }
    Path2D(int rule,int initialTypes){
        setWindingRule(rule);
        this.pointTypes=new byte[initialTypes];
    }

    static byte[] expandPointTypes(byte[] oldPointTypes,int needed){
        final int oldSize=oldPointTypes.length;
        final int newSizeMin=oldSize+needed;
        if(newSizeMin<oldSize){
            // hard overflow failure - we can't even accommodate
            // new items without overflowing
            throw new ArrayIndexOutOfBoundsException(
                    "pointTypes exceeds maximum capacity !");
        }
        // growth algorithm computation
        int grow=oldSize;
        if(grow>EXPAND_MAX){
            grow=Math.max(EXPAND_MAX,oldSize>>3); // 1/8th min
        }else if(grow<EXPAND_MIN){
            grow=EXPAND_MIN;
        }
        assert grow>0;
        int newSize=oldSize+grow;
        if(newSize<newSizeMin){
            // overflow in growth algorithm computation
            newSize=Integer.MAX_VALUE;
        }
        while(true){
            try{
                // try allocating the larger array
                return Arrays.copyOf(oldPointTypes,newSize);
            }catch(OutOfMemoryError oome){
                if(newSize==newSizeMin){
                    throw oome;
                }
            }
            newSize=newSizeMin+(newSize-newSizeMin)/2;
        }
    }

    public static boolean contains(PathIterator pi,Point2D p){
        return contains(pi,p.getX(),p.getY());
    }

    public static boolean contains(PathIterator pi,double x,double y){
        if(x*0.0+y*0.0==0.0){
            /** N * 0.0 is 0.0 only if N is finite.
             * Here we know that both x and y are finite.
             */
            int mask=(pi.getWindingRule()==WIND_NON_ZERO?-1:1);
            int cross=Curve.pointCrossingsForPath(pi,x,y);
            return ((cross&mask)!=0);
        }else{
            /** Either x or y was infinite or NaN.
             * A NaN always produces a negative response to any test
             * and Infinity values cannot be "inside" any path so
             * they should return false as well.
             */
            return false;
        }
    }

    public static boolean contains(PathIterator pi,Rectangle2D r){
        return contains(pi,r.getX(),r.getY(),r.getWidth(),r.getHeight());
    }

    public static boolean contains(PathIterator pi,
                                   double x,double y,double w,double h){
        if(java.lang.Double.isNaN(x+w)||java.lang.Double.isNaN(y+h)){
            /** [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinte values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false;
        }
        if(w<=0||h<=0){
            return false;
        }
        int mask=(pi.getWindingRule()==WIND_NON_ZERO?-1:2);
        int crossings=Curve.rectCrossingsForPath(pi,x,y,x+w,y+h);
        return (crossings!=Curve.RECT_INTERSECTS&&
                (crossings&mask)!=0);
    }

    public static boolean intersects(PathIterator pi,Rectangle2D r){
        return intersects(pi,r.getX(),r.getY(),r.getWidth(),r.getHeight());
    }

    public static boolean intersects(PathIterator pi,
                                     double x,double y,double w,double h){
        if(java.lang.Double.isNaN(x+w)||java.lang.Double.isNaN(y+h)){
            /** [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinte values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false;
        }
        if(w<=0||h<=0){
            return false;
        }
        int mask=(pi.getWindingRule()==WIND_NON_ZERO?-1:2);
        int crossings=Curve.rectCrossingsForPath(pi,x,y,x+w,y+h);
        return (crossings==Curve.RECT_INTERSECTS||
                (crossings&mask)!=0);
    }

    abstract float[] cloneCoordsFloat(AffineTransform at);

    abstract double[] cloneCoordsDouble(AffineTransform at);

    public abstract void moveTo(double x,double y);

    public abstract void lineTo(double x,double y);

    public abstract void quadTo(double x1,double y1,
                                double x2,double y2);

    public abstract void curveTo(double x1,double y1,
                                 double x2,double y2,
                                 double x3,double y3);

    public final synchronized void closePath(){
        if(numTypes==0||pointTypes[numTypes-1]!=SEG_CLOSE){
            needRoom(true,0);
            pointTypes[numTypes++]=SEG_CLOSE;
        }
    }

    abstract void needRoom(boolean needMove,int newCoords);

    public final void append(Shape s,boolean connect){
        append(s.getPathIterator(null),connect);
    }

    public abstract void append(PathIterator pi,boolean connect);

    public final synchronized int getWindingRule(){
        return windingRule;
    }

    public final void setWindingRule(int rule){
        if(rule!=WIND_EVEN_ODD&&rule!=WIND_NON_ZERO){
            throw new IllegalArgumentException("winding rule must be "+
                    "WIND_EVEN_ODD or "+
                    "WIND_NON_ZERO");
        }
        windingRule=rule;
    }    public final boolean contains(double x,double y){
        if(x*0.0+y*0.0==0.0){
            /** N * 0.0 is 0.0 only if N is finite.
             * Here we know that both x and y are finite.
             */
            if(numTypes<2){
                return false;
            }
            int mask=(windingRule==WIND_NON_ZERO?-1:1);
            return ((pointCrossings(x,y)&mask)!=0);
        }else{
            /** Either x or y was infinite or NaN.
             * A NaN always produces a negative response to any test
             * and Infinity values cannot be "inside" any path so
             * they should return false as well.
             */
            return false;
        }
    }

    public final synchronized Point2D getCurrentPoint(){
        int index=numCoords;
        if(numTypes<1||index<1){
            return null;
        }
        if(pointTypes[numTypes-1]==SEG_CLOSE){
            loop:
            for(int i=numTypes-2;i>0;i--){
                switch(pointTypes[i]){
                    case SEG_MOVETO:
                        break loop;
                    case SEG_LINETO:
                        index-=2;
                        break;
                    case SEG_QUADTO:
                        index-=4;
                        break;
                    case SEG_CUBICTO:
                        index-=6;
                        break;
                    case SEG_CLOSE:
                        break;
                }
            }
        }
        return getPoint(index-2);
    }

    abstract Point2D getPoint(int coordindex);    public final boolean contains(Point2D p){
        return contains(p.getX(),p.getY());
    }

    public final synchronized void reset(){
        numTypes=numCoords=0;
    }

    public abstract void transform(AffineTransform at);

    public final synchronized Shape createTransformedShape(AffineTransform at){
        Path2D p2d=(Path2D)clone();
        if(at!=null){
            p2d.transform(at);
        }
        return p2d;
    }

    public abstract Object clone();    public final boolean contains(double x,double y,double w,double h){
        if(java.lang.Double.isNaN(x+w)||java.lang.Double.isNaN(y+h)){
            /** [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinte values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false;
        }
        if(w<=0||h<=0){
            return false;
        }
        int mask=(windingRule==WIND_NON_ZERO?-1:2);
        int crossings=rectCrossings(x,y,x+w,y+h);
        return (crossings!=Curve.RECT_INTERSECTS&&
                (crossings&mask)!=0);
    }

    public final Rectangle getBounds(){
        return getBounds2D().getBounds();
    }

    final void writeObject(java.io.ObjectOutputStream s,boolean isdbl)
            throws java.io.IOException{
        s.defaultWriteObject();
        float fCoords[];
        double dCoords[];
        if(isdbl){
            dCoords=((Double)this).doubleCoords;
            fCoords=null;
        }else{
            fCoords=((Float)this).floatCoords;
            dCoords=null;
        }
        int numTypes=this.numTypes;
        s.writeByte(isdbl
                ?SERIAL_STORAGE_DBL_ARRAY
                :SERIAL_STORAGE_FLT_ARRAY);
        s.writeInt(numTypes);
        s.writeInt(numCoords);
        s.writeByte((byte)windingRule);
        int cindex=0;
        for(int i=0;i<numTypes;i++){
            int npoints;
            byte serialtype;
            switch(pointTypes[i]){
                case SEG_MOVETO:
                    npoints=1;
                    serialtype=(isdbl
                            ?SERIAL_SEG_DBL_MOVETO
                            :SERIAL_SEG_FLT_MOVETO);
                    break;
                case SEG_LINETO:
                    npoints=1;
                    serialtype=(isdbl
                            ?SERIAL_SEG_DBL_LINETO
                            :SERIAL_SEG_FLT_LINETO);
                    break;
                case SEG_QUADTO:
                    npoints=2;
                    serialtype=(isdbl
                            ?SERIAL_SEG_DBL_QUADTO
                            :SERIAL_SEG_FLT_QUADTO);
                    break;
                case SEG_CUBICTO:
                    npoints=3;
                    serialtype=(isdbl
                            ?SERIAL_SEG_DBL_CUBICTO
                            :SERIAL_SEG_FLT_CUBICTO);
                    break;
                case SEG_CLOSE:
                    npoints=0;
                    serialtype=SERIAL_SEG_CLOSE;
                    break;
                default:
                    // Should never happen
                    throw new InternalError("unrecognized path type");
            }
            s.writeByte(serialtype);
            while(--npoints>=0){
                if(isdbl){
                    s.writeDouble(dCoords[cindex++]);
                    s.writeDouble(dCoords[cindex++]);
                }else{
                    s.writeFloat(fCoords[cindex++]);
                    s.writeFloat(fCoords[cindex++]);
                }
            }
        }
        s.writeByte(SERIAL_PATH_END);
    }    public final boolean contains(Rectangle2D r){
        return contains(r.getX(),r.getY(),r.getWidth(),r.getHeight());
    }

    final void readObject(java.io.ObjectInputStream s,boolean storedbl)
            throws ClassNotFoundException, java.io.IOException{
        s.defaultReadObject();
        // The subclass calls this method with the storage type that
        // they want us to use (storedbl) so we ignore the storage
        // method hint from the stream.
        s.readByte();
        int nT=s.readInt();
        int nC=s.readInt();
        try{
            setWindingRule(s.readByte());
        }catch(IllegalArgumentException iae){
            throw new java.io.InvalidObjectException(iae.getMessage());
        }
        pointTypes=new byte[(nT<0)?INIT_SIZE:nT];
        if(nC<0){
            nC=INIT_SIZE*2;
        }
        if(storedbl){
            ((Double)this).doubleCoords=new double[nC];
        }else{
            ((Float)this).floatCoords=new float[nC];
        }
        PATHDONE:
        for(int i=0;nT<0||i<nT;i++){
            boolean isdbl;
            int npoints;
            byte segtype;
            byte serialtype=s.readByte();
            switch(serialtype){
                case SERIAL_SEG_FLT_MOVETO:
                    isdbl=false;
                    npoints=1;
                    segtype=SEG_MOVETO;
                    break;
                case SERIAL_SEG_FLT_LINETO:
                    isdbl=false;
                    npoints=1;
                    segtype=SEG_LINETO;
                    break;
                case SERIAL_SEG_FLT_QUADTO:
                    isdbl=false;
                    npoints=2;
                    segtype=SEG_QUADTO;
                    break;
                case SERIAL_SEG_FLT_CUBICTO:
                    isdbl=false;
                    npoints=3;
                    segtype=SEG_CUBICTO;
                    break;
                case SERIAL_SEG_DBL_MOVETO:
                    isdbl=true;
                    npoints=1;
                    segtype=SEG_MOVETO;
                    break;
                case SERIAL_SEG_DBL_LINETO:
                    isdbl=true;
                    npoints=1;
                    segtype=SEG_LINETO;
                    break;
                case SERIAL_SEG_DBL_QUADTO:
                    isdbl=true;
                    npoints=2;
                    segtype=SEG_QUADTO;
                    break;
                case SERIAL_SEG_DBL_CUBICTO:
                    isdbl=true;
                    npoints=3;
                    segtype=SEG_CUBICTO;
                    break;
                case SERIAL_SEG_CLOSE:
                    isdbl=false;
                    npoints=0;
                    segtype=SEG_CLOSE;
                    break;
                case SERIAL_PATH_END:
                    if(nT<0){
                        break PATHDONE;
                    }
                    throw new StreamCorruptedException("unexpected PATH_END");
                default:
                    throw new StreamCorruptedException("unrecognized path type");
            }
            needRoom(segtype!=SEG_MOVETO,npoints*2);
            if(isdbl){
                while(--npoints>=0){
                    append(s.readDouble(),s.readDouble());
                }
            }else{
                while(--npoints>=0){
                    append(s.readFloat(),s.readFloat());
                }
            }
            pointTypes[numTypes++]=segtype;
        }
        if(nT>=0&&s.readByte()!=SERIAL_PATH_END){
            throw new StreamCorruptedException("missing PATH_END");
        }
    }

    abstract void append(float x,float y);

    abstract void append(double x,double y);

    public static class Float extends Path2D implements Serializable{
        private static final long serialVersionUID=6990832515060788886L;
        transient float floatCoords[];

        public Float(){
            this(WIND_NON_ZERO,INIT_SIZE);
        }

        public Float(int rule,int initialCapacity){
            super(rule,initialCapacity);
            floatCoords=new float[initialCapacity*2];
        }

        public Float(int rule){
            this(rule,INIT_SIZE);
        }

        public Float(Shape s){
            this(s,null);
        }

        public Float(Shape s,AffineTransform at){
            if(s instanceof Path2D){
                Path2D p2d=(Path2D)s;
                setWindingRule(p2d.windingRule);
                this.numTypes=p2d.numTypes;
                // trim arrays:
                this.pointTypes=Arrays.copyOf(p2d.pointTypes,p2d.numTypes);
                this.numCoords=p2d.numCoords;
                this.floatCoords=p2d.cloneCoordsFloat(at);
            }else{
                PathIterator pi=s.getPathIterator(at);
                setWindingRule(pi.getWindingRule());
                this.pointTypes=new byte[INIT_SIZE];
                this.floatCoords=new float[INIT_SIZE*2];
                append(pi,false);
            }
        }

        @Override
        float[] cloneCoordsFloat(AffineTransform at){
            // trim arrays:
            float ret[];
            if(at==null){
                ret=Arrays.copyOf(floatCoords,numCoords);
            }else{
                ret=new float[numCoords];
                at.transform(floatCoords,0,ret,0,numCoords/2);
            }
            return ret;
        }

        public final synchronized void moveTo(float x,float y){
            if(numTypes>0&&pointTypes[numTypes-1]==SEG_MOVETO){
                floatCoords[numCoords-2]=x;
                floatCoords[numCoords-1]=y;
            }else{
                needRoom(false,2);
                pointTypes[numTypes++]=SEG_MOVETO;
                floatCoords[numCoords++]=x;
                floatCoords[numCoords++]=y;
            }
        }        @Override
        double[] cloneCoordsDouble(AffineTransform at){
            // trim arrays:
            double ret[]=new double[numCoords];
            if(at==null){
                for(int i=0;i<numCoords;i++){
                    ret[i]=floatCoords[i];
                }
            }else{
                at.transform(floatCoords,0,ret,0,numCoords/2);
            }
            return ret;
        }

        public final synchronized void lineTo(float x,float y){
            needRoom(true,2);
            pointTypes[numTypes++]=SEG_LINETO;
            floatCoords[numCoords++]=x;
            floatCoords[numCoords++]=y;
        }

        public final synchronized void quadTo(float x1,float y1,
                                              float x2,float y2){
            needRoom(true,4);
            pointTypes[numTypes++]=SEG_QUADTO;
            floatCoords[numCoords++]=x1;
            floatCoords[numCoords++]=y1;
            floatCoords[numCoords++]=x2;
            floatCoords[numCoords++]=y2;
        }        void append(float x,float y){
            floatCoords[numCoords++]=x;
            floatCoords[numCoords++]=y;
        }

        public final synchronized void curveTo(float x1,float y1,
                                               float x2,float y2,
                                               float x3,float y3){
            needRoom(true,6);
            pointTypes[numTypes++]=SEG_CUBICTO;
            floatCoords[numCoords++]=x1;
            floatCoords[numCoords++]=y1;
            floatCoords[numCoords++]=x2;
            floatCoords[numCoords++]=y2;
            floatCoords[numCoords++]=x3;
            floatCoords[numCoords++]=y3;
        }

        private void writeObject(java.io.ObjectOutputStream s)
                throws java.io.IOException{
            super.writeObject(s,false);
        }        void append(double x,double y){
            floatCoords[numCoords++]=(float)x;
            floatCoords[numCoords++]=(float)y;
        }

        private void readObject(java.io.ObjectInputStream s)
                throws ClassNotFoundException, java.io.IOException{
            super.readObject(s,false);
        }

        static class CopyIterator extends Iterator{
            float floatCoords[];

            CopyIterator(Float p2df){
                super(p2df);
                this.floatCoords=p2df.floatCoords;
            }

            public int currentSegment(float[] coords){
                int type=path.pointTypes[typeIdx];
                int numCoords=curvecoords[type];
                if(numCoords>0){
                    System.arraycopy(floatCoords,pointIdx,
                            coords,0,numCoords);
                }
                return type;
            }

            public int currentSegment(double[] coords){
                int type=path.pointTypes[typeIdx];
                int numCoords=curvecoords[type];
                if(numCoords>0){
                    for(int i=0;i<numCoords;i++){
                        coords[i]=floatCoords[pointIdx+i];
                    }
                }
                return type;
            }
        }        Point2D getPoint(int coordindex){
            return new Point2D.Float(floatCoords[coordindex],
                    floatCoords[coordindex+1]);
        }

        static class TxIterator extends Iterator{
            float floatCoords[];
            AffineTransform affine;

            TxIterator(Float p2df,AffineTransform at){
                super(p2df);
                this.floatCoords=p2df.floatCoords;
                this.affine=at;
            }

            public int currentSegment(float[] coords){
                int type=path.pointTypes[typeIdx];
                int numCoords=curvecoords[type];
                if(numCoords>0){
                    affine.transform(floatCoords,pointIdx,
                            coords,0,numCoords/2);
                }
                return type;
            }

            public int currentSegment(double[] coords){
                int type=path.pointTypes[typeIdx];
                int numCoords=curvecoords[type];
                if(numCoords>0){
                    affine.transform(floatCoords,pointIdx,
                            coords,0,numCoords/2);
                }
                return type;
            }
        }

        @Override
        void needRoom(boolean needMove,int newCoords){
            if((numTypes==0)&&needMove){
                throw new IllegalPathStateException("missing initial moveto "+
                        "in path definition");
            }
            if(numTypes>=pointTypes.length){
                pointTypes=expandPointTypes(pointTypes,1);
            }
            if(numCoords>(floatCoords.length-newCoords)){
                floatCoords=expandCoords(floatCoords,newCoords);
            }
        }



        static float[] expandCoords(float[] oldCoords,int needed){
            final int oldSize=oldCoords.length;
            final int newSizeMin=oldSize+needed;
            if(newSizeMin<oldSize){
                // hard overflow failure - we can't even accommodate
                // new items without overflowing
                throw new ArrayIndexOutOfBoundsException(
                        "coords exceeds maximum capacity !");
            }
            // growth algorithm computation
            int grow=oldSize;
            if(grow>EXPAND_MAX_COORDS){
                grow=Math.max(EXPAND_MAX_COORDS,oldSize>>3); // 1/8th min
            }else if(grow<EXPAND_MIN){
                grow=EXPAND_MIN;
            }
            assert grow>needed;
            int newSize=oldSize+grow;
            if(newSize<newSizeMin){
                // overflow in growth algorithm computation
                newSize=Integer.MAX_VALUE;
            }
            while(true){
                try{
                    // try allocating the larger array
                    return Arrays.copyOf(oldCoords,newSize);
                }catch(OutOfMemoryError oome){
                    if(newSize==newSizeMin){
                        throw oome;
                    }
                }
                newSize=newSizeMin+(newSize-newSizeMin)/2;
            }
        }



        public final synchronized void moveTo(double x,double y){
            if(numTypes>0&&pointTypes[numTypes-1]==SEG_MOVETO){
                floatCoords[numCoords-2]=(float)x;
                floatCoords[numCoords-1]=(float)y;
            }else{
                needRoom(false,2);
                pointTypes[numTypes++]=SEG_MOVETO;
                floatCoords[numCoords++]=(float)x;
                floatCoords[numCoords++]=(float)y;
            }
        }





        public final synchronized void lineTo(double x,double y){
            needRoom(true,2);
            pointTypes[numTypes++]=SEG_LINETO;
            floatCoords[numCoords++]=(float)x;
            floatCoords[numCoords++]=(float)y;
        }

        public final synchronized void quadTo(double x1,double y1,
                                              double x2,double y2){
            needRoom(true,4);
            pointTypes[numTypes++]=SEG_QUADTO;
            floatCoords[numCoords++]=(float)x1;
            floatCoords[numCoords++]=(float)y1;
            floatCoords[numCoords++]=(float)x2;
            floatCoords[numCoords++]=(float)y2;
        }

        public final synchronized void curveTo(double x1,double y1,
                                               double x2,double y2,
                                               double x3,double y3){
            needRoom(true,6);
            pointTypes[numTypes++]=SEG_CUBICTO;
            floatCoords[numCoords++]=(float)x1;
            floatCoords[numCoords++]=(float)y1;
            floatCoords[numCoords++]=(float)x2;
            floatCoords[numCoords++]=(float)y2;
            floatCoords[numCoords++]=(float)x3;
            floatCoords[numCoords++]=(float)y3;
        }

        int pointCrossings(double px,double py){
            if(numTypes==0){
                return 0;
            }
            double movx, movy, curx, cury, endx, endy;
            float coords[]=floatCoords;
            curx=movx=coords[0];
            cury=movy=coords[1];
            int crossings=0;
            int ci=2;
            for(int i=1;i<numTypes;i++){
                switch(pointTypes[i]){
                    case PathIterator.SEG_MOVETO:
                        if(cury!=movy){
                            crossings+=
                                    Curve.pointCrossingsForLine(px,py,
                                            curx,cury,
                                            movx,movy);
                        }
                        movx=curx=coords[ci++];
                        movy=cury=coords[ci++];
                        break;
                    case PathIterator.SEG_LINETO:
                        crossings+=
                                Curve.pointCrossingsForLine(px,py,
                                        curx,cury,
                                        endx=coords[ci++],
                                        endy=coords[ci++]);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_QUADTO:
                        crossings+=
                                Curve.pointCrossingsForQuad(px,py,
                                        curx,cury,
                                        coords[ci++],
                                        coords[ci++],
                                        endx=coords[ci++],
                                        endy=coords[ci++],
                                        0);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_CUBICTO:
                        crossings+=
                                Curve.pointCrossingsForCubic(px,py,
                                        curx,cury,
                                        coords[ci++],
                                        coords[ci++],
                                        coords[ci++],
                                        coords[ci++],
                                        endx=coords[ci++],
                                        endy=coords[ci++],
                                        0);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_CLOSE:
                        if(cury!=movy){
                            crossings+=
                                    Curve.pointCrossingsForLine(px,py,
                                            curx,cury,
                                            movx,movy);
                        }
                        curx=movx;
                        cury=movy;
                        break;
                }
            }
            if(cury!=movy){
                crossings+=
                        Curve.pointCrossingsForLine(px,py,
                                curx,cury,
                                movx,movy);
            }
            return crossings;
        }

        int rectCrossings(double rxmin,double rymin,
                          double rxmax,double rymax){
            if(numTypes==0){
                return 0;
            }
            float coords[]=floatCoords;
            double curx, cury, movx, movy, endx, endy;
            curx=movx=coords[0];
            cury=movy=coords[1];
            int crossings=0;
            int ci=2;
            for(int i=1;
                crossings!=Curve.RECT_INTERSECTS&&i<numTypes;
                i++){
                switch(pointTypes[i]){
                    case PathIterator.SEG_MOVETO:
                        if(curx!=movx||cury!=movy){
                            crossings=
                                    Curve.rectCrossingsForLine(crossings,
                                            rxmin,rymin,
                                            rxmax,rymax,
                                            curx,cury,
                                            movx,movy);
                        }
                        // Count should always be a multiple of 2 here.
                        // assert((crossings & 1) != 0);
                        movx=curx=coords[ci++];
                        movy=cury=coords[ci++];
                        break;
                    case PathIterator.SEG_LINETO:
                        crossings=
                                Curve.rectCrossingsForLine(crossings,
                                        rxmin,rymin,
                                        rxmax,rymax,
                                        curx,cury,
                                        endx=coords[ci++],
                                        endy=coords[ci++]);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_QUADTO:
                        crossings=
                                Curve.rectCrossingsForQuad(crossings,
                                        rxmin,rymin,
                                        rxmax,rymax,
                                        curx,cury,
                                        coords[ci++],
                                        coords[ci++],
                                        endx=coords[ci++],
                                        endy=coords[ci++],
                                        0);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_CUBICTO:
                        crossings=
                                Curve.rectCrossingsForCubic(crossings,
                                        rxmin,rymin,
                                        rxmax,rymax,
                                        curx,cury,
                                        coords[ci++],
                                        coords[ci++],
                                        coords[ci++],
                                        coords[ci++],
                                        endx=coords[ci++],
                                        endy=coords[ci++],
                                        0);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_CLOSE:
                        if(curx!=movx||cury!=movy){
                            crossings=
                                    Curve.rectCrossingsForLine(crossings,
                                            rxmin,rymin,
                                            rxmax,rymax,
                                            curx,cury,
                                            movx,movy);
                        }
                        curx=movx;
                        cury=movy;
                        // Count should always be a multiple of 2 here.
                        // assert((crossings & 1) != 0);
                        break;
                }
            }
            if(crossings!=Curve.RECT_INTERSECTS&&
                    (curx!=movx||cury!=movy)){
                crossings=
                        Curve.rectCrossingsForLine(crossings,
                                rxmin,rymin,
                                rxmax,rymax,
                                curx,cury,
                                movx,movy);
            }
            // Count should always be a multiple of 2 here.
            // assert((crossings & 1) != 0);
            return crossings;
        }

        public final void append(PathIterator pi,boolean connect){
            float coords[]=new float[6];
            while(!pi.isDone()){
                switch(pi.currentSegment(coords)){
                    case SEG_MOVETO:
                        if(!connect||numTypes<1||numCoords<1){
                            moveTo(coords[0],coords[1]);
                            break;
                        }
                        if(pointTypes[numTypes-1]!=SEG_CLOSE&&
                                floatCoords[numCoords-2]==coords[0]&&
                                floatCoords[numCoords-1]==coords[1]){
                            // Collapse out initial moveto/lineto
                            break;
                        }
                        lineTo(coords[0],coords[1]);
                        break;
                    case SEG_LINETO:
                        lineTo(coords[0],coords[1]);
                        break;
                    case SEG_QUADTO:
                        quadTo(coords[0],coords[1],
                                coords[2],coords[3]);
                        break;
                    case SEG_CUBICTO:
                        curveTo(coords[0],coords[1],
                                coords[2],coords[3],
                                coords[4],coords[5]);
                        break;
                    case SEG_CLOSE:
                        closePath();
                        break;
                }
                pi.next();
                connect=false;
            }
        }

        public final void transform(AffineTransform at){
            at.transform(floatCoords,0,floatCoords,0,numCoords/2);
        }

        public final synchronized Rectangle2D getBounds2D(){
            float x1, y1, x2, y2;
            int i=numCoords;
            if(i>0){
                y1=y2=floatCoords[--i];
                x1=x2=floatCoords[--i];
                while(i>0){
                    float y=floatCoords[--i];
                    float x=floatCoords[--i];
                    if(x<x1) x1=x;
                    if(y<y1) y1=y;
                    if(x>x2) x2=x;
                    if(y>y2) y2=y;
                }
            }else{
                x1=y1=x2=y2=0.0f;
            }
            return new Rectangle2D.Float(x1,y1,x2-x1,y2-y1);
        }

        public final PathIterator getPathIterator(AffineTransform at){
            if(at==null){
                return new CopyIterator(this);
            }else{
                return new TxIterator(this,at);
            }
        }

        public final Object clone(){
            // Note: It would be nice to have this return Path2D
            // but one of our subclasses (GeneralPath) needs to
            // offer "public Object clone()" for backwards
            // compatibility so we cannot restrict it further.
            // REMIND: Can we do both somehow?
            if(this instanceof GeneralPath){
                return new GeneralPath(this);
            }else{
                return new Float(this);
            }
        }
    }    public final boolean intersects(double x,double y,double w,double h){
        if(java.lang.Double.isNaN(x+w)||java.lang.Double.isNaN(y+h)){
            /** [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinte values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false;
        }
        if(w<=0||h<=0){
            return false;
        }
        int mask=(windingRule==WIND_NON_ZERO?-1:2);
        int crossings=rectCrossings(x,y,x+w,y+h);
        return (crossings==Curve.RECT_INTERSECTS||
                (crossings&mask)!=0);
    }

    public static class Double extends Path2D implements Serializable{
        private static final long serialVersionUID=1826762518450014216L;
        transient double doubleCoords[];

        public Double(){
            this(WIND_NON_ZERO,INIT_SIZE);
        }

        public Double(int rule,int initialCapacity){
            super(rule,initialCapacity);
            doubleCoords=new double[initialCapacity*2];
        }

        public Double(int rule){
            this(rule,INIT_SIZE);
        }

        public Double(Shape s){
            this(s,null);
        }

        public Double(Shape s,AffineTransform at){
            if(s instanceof Path2D){
                Path2D p2d=(Path2D)s;
                setWindingRule(p2d.windingRule);
                this.numTypes=p2d.numTypes;
                // trim arrays:
                this.pointTypes=Arrays.copyOf(p2d.pointTypes,p2d.numTypes);
                this.numCoords=p2d.numCoords;
                this.doubleCoords=p2d.cloneCoordsDouble(at);
            }else{
                PathIterator pi=s.getPathIterator(at);
                setWindingRule(pi.getWindingRule());
                this.pointTypes=new byte[INIT_SIZE];
                this.doubleCoords=new double[INIT_SIZE*2];
                append(pi,false);
            }
        }

        static double[] expandCoords(double[] oldCoords,int needed){
            final int oldSize=oldCoords.length;
            final int newSizeMin=oldSize+needed;
            if(newSizeMin<oldSize){
                // hard overflow failure - we can't even accommodate
                // new items without overflowing
                throw new ArrayIndexOutOfBoundsException(
                        "coords exceeds maximum capacity !");
            }
            // growth algorithm computation
            int grow=oldSize;
            if(grow>EXPAND_MAX_COORDS){
                grow=Math.max(EXPAND_MAX_COORDS,oldSize>>3); // 1/8th min
            }else if(grow<EXPAND_MIN){
                grow=EXPAND_MIN;
            }
            assert grow>needed;
            int newSize=oldSize+grow;
            if(newSize<newSizeMin){
                // overflow in growth algorithm computation
                newSize=Integer.MAX_VALUE;
            }
            while(true){
                try{
                    // try allocating the larger array
                    return Arrays.copyOf(oldCoords,newSize);
                }catch(OutOfMemoryError oome){
                    if(newSize==newSizeMin){
                        throw oome;
                    }
                }
                newSize=newSizeMin+(newSize-newSizeMin)/2;
            }
        }        @Override
        float[] cloneCoordsFloat(AffineTransform at){
            // trim arrays:
            float ret[]=new float[numCoords];
            if(at==null){
                for(int i=0;i<numCoords;i++){
                    ret[i]=(float)doubleCoords[i];
                }
            }else{
                at.transform(doubleCoords,0,ret,0,numCoords/2);
            }
            return ret;
        }

        private void writeObject(java.io.ObjectOutputStream s)
                throws java.io.IOException{
            super.writeObject(s,true);
        }

        private void readObject(java.io.ObjectInputStream s)
                throws ClassNotFoundException, java.io.IOException{
            super.readObject(s,true);
        }        @Override
        double[] cloneCoordsDouble(AffineTransform at){
            // trim arrays:
            double ret[];
            if(at==null){
                ret=Arrays.copyOf(doubleCoords,numCoords);
            }else{
                ret=new double[numCoords];
                at.transform(doubleCoords,0,ret,0,numCoords/2);
            }
            return ret;
        }

        static class CopyIterator extends Iterator{
            double doubleCoords[];

            CopyIterator(Double p2dd){
                super(p2dd);
                this.doubleCoords=p2dd.doubleCoords;
            }

            public int currentSegment(float[] coords){
                int type=path.pointTypes[typeIdx];
                int numCoords=curvecoords[type];
                if(numCoords>0){
                    for(int i=0;i<numCoords;i++){
                        coords[i]=(float)doubleCoords[pointIdx+i];
                    }
                }
                return type;
            }

            public int currentSegment(double[] coords){
                int type=path.pointTypes[typeIdx];
                int numCoords=curvecoords[type];
                if(numCoords>0){
                    System.arraycopy(doubleCoords,pointIdx,
                            coords,0,numCoords);
                }
                return type;
            }
        }

        static class TxIterator extends Iterator{
            double doubleCoords[];
            AffineTransform affine;

            TxIterator(Double p2dd,AffineTransform at){
                super(p2dd);
                this.doubleCoords=p2dd.doubleCoords;
                this.affine=at;
            }

            public int currentSegment(float[] coords){
                int type=path.pointTypes[typeIdx];
                int numCoords=curvecoords[type];
                if(numCoords>0){
                    affine.transform(doubleCoords,pointIdx,
                            coords,0,numCoords/2);
                }
                return type;
            }

            public int currentSegment(double[] coords){
                int type=path.pointTypes[typeIdx];
                int numCoords=curvecoords[type];
                if(numCoords>0){
                    affine.transform(doubleCoords,pointIdx,
                            coords,0,numCoords/2);
                }
                return type;
            }
        }        void append(float x,float y){
            doubleCoords[numCoords++]=x;
            doubleCoords[numCoords++]=y;
        }



        void append(double x,double y){
            doubleCoords[numCoords++]=x;
            doubleCoords[numCoords++]=y;
        }



        Point2D getPoint(int coordindex){
            return new Point2D.Double(doubleCoords[coordindex],
                    doubleCoords[coordindex+1]);
        }



        @Override
        void needRoom(boolean needMove,int newCoords){
            if((numTypes==0)&&needMove){
                throw new IllegalPathStateException("missing initial moveto "+
                        "in path definition");
            }
            if(numTypes>=pointTypes.length){
                pointTypes=expandPointTypes(pointTypes,1);
            }
            if(numCoords>(doubleCoords.length-newCoords)){
                doubleCoords=expandCoords(doubleCoords,newCoords);
            }
        }

        public final synchronized void moveTo(double x,double y){
            if(numTypes>0&&pointTypes[numTypes-1]==SEG_MOVETO){
                doubleCoords[numCoords-2]=x;
                doubleCoords[numCoords-1]=y;
            }else{
                needRoom(false,2);
                pointTypes[numTypes++]=SEG_MOVETO;
                doubleCoords[numCoords++]=x;
                doubleCoords[numCoords++]=y;
            }
        }

        public final synchronized void lineTo(double x,double y){
            needRoom(true,2);
            pointTypes[numTypes++]=SEG_LINETO;
            doubleCoords[numCoords++]=x;
            doubleCoords[numCoords++]=y;
        }

        public final synchronized void quadTo(double x1,double y1,
                                              double x2,double y2){
            needRoom(true,4);
            pointTypes[numTypes++]=SEG_QUADTO;
            doubleCoords[numCoords++]=x1;
            doubleCoords[numCoords++]=y1;
            doubleCoords[numCoords++]=x2;
            doubleCoords[numCoords++]=y2;
        }

        public final synchronized void curveTo(double x1,double y1,
                                               double x2,double y2,
                                               double x3,double y3){
            needRoom(true,6);
            pointTypes[numTypes++]=SEG_CUBICTO;
            doubleCoords[numCoords++]=x1;
            doubleCoords[numCoords++]=y1;
            doubleCoords[numCoords++]=x2;
            doubleCoords[numCoords++]=y2;
            doubleCoords[numCoords++]=x3;
            doubleCoords[numCoords++]=y3;
        }

        int pointCrossings(double px,double py){
            if(numTypes==0){
                return 0;
            }
            double movx, movy, curx, cury, endx, endy;
            double coords[]=doubleCoords;
            curx=movx=coords[0];
            cury=movy=coords[1];
            int crossings=0;
            int ci=2;
            for(int i=1;i<numTypes;i++){
                switch(pointTypes[i]){
                    case PathIterator.SEG_MOVETO:
                        if(cury!=movy){
                            crossings+=
                                    Curve.pointCrossingsForLine(px,py,
                                            curx,cury,
                                            movx,movy);
                        }
                        movx=curx=coords[ci++];
                        movy=cury=coords[ci++];
                        break;
                    case PathIterator.SEG_LINETO:
                        crossings+=
                                Curve.pointCrossingsForLine(px,py,
                                        curx,cury,
                                        endx=coords[ci++],
                                        endy=coords[ci++]);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_QUADTO:
                        crossings+=
                                Curve.pointCrossingsForQuad(px,py,
                                        curx,cury,
                                        coords[ci++],
                                        coords[ci++],
                                        endx=coords[ci++],
                                        endy=coords[ci++],
                                        0);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_CUBICTO:
                        crossings+=
                                Curve.pointCrossingsForCubic(px,py,
                                        curx,cury,
                                        coords[ci++],
                                        coords[ci++],
                                        coords[ci++],
                                        coords[ci++],
                                        endx=coords[ci++],
                                        endy=coords[ci++],
                                        0);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_CLOSE:
                        if(cury!=movy){
                            crossings+=
                                    Curve.pointCrossingsForLine(px,py,
                                            curx,cury,
                                            movx,movy);
                        }
                        curx=movx;
                        cury=movy;
                        break;
                }
            }
            if(cury!=movy){
                crossings+=
                        Curve.pointCrossingsForLine(px,py,
                                curx,cury,
                                movx,movy);
            }
            return crossings;
        }

        int rectCrossings(double rxmin,double rymin,
                          double rxmax,double rymax){
            if(numTypes==0){
                return 0;
            }
            double coords[]=doubleCoords;
            double curx, cury, movx, movy, endx, endy;
            curx=movx=coords[0];
            cury=movy=coords[1];
            int crossings=0;
            int ci=2;
            for(int i=1;
                crossings!=Curve.RECT_INTERSECTS&&i<numTypes;
                i++){
                switch(pointTypes[i]){
                    case PathIterator.SEG_MOVETO:
                        if(curx!=movx||cury!=movy){
                            crossings=
                                    Curve.rectCrossingsForLine(crossings,
                                            rxmin,rymin,
                                            rxmax,rymax,
                                            curx,cury,
                                            movx,movy);
                        }
                        // Count should always be a multiple of 2 here.
                        // assert((crossings & 1) != 0);
                        movx=curx=coords[ci++];
                        movy=cury=coords[ci++];
                        break;
                    case PathIterator.SEG_LINETO:
                        endx=coords[ci++];
                        endy=coords[ci++];
                        crossings=
                                Curve.rectCrossingsForLine(crossings,
                                        rxmin,rymin,
                                        rxmax,rymax,
                                        curx,cury,
                                        endx,endy);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_QUADTO:
                        crossings=
                                Curve.rectCrossingsForQuad(crossings,
                                        rxmin,rymin,
                                        rxmax,rymax,
                                        curx,cury,
                                        coords[ci++],
                                        coords[ci++],
                                        endx=coords[ci++],
                                        endy=coords[ci++],
                                        0);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_CUBICTO:
                        crossings=
                                Curve.rectCrossingsForCubic(crossings,
                                        rxmin,rymin,
                                        rxmax,rymax,
                                        curx,cury,
                                        coords[ci++],
                                        coords[ci++],
                                        coords[ci++],
                                        coords[ci++],
                                        endx=coords[ci++],
                                        endy=coords[ci++],
                                        0);
                        curx=endx;
                        cury=endy;
                        break;
                    case PathIterator.SEG_CLOSE:
                        if(curx!=movx||cury!=movy){
                            crossings=
                                    Curve.rectCrossingsForLine(crossings,
                                            rxmin,rymin,
                                            rxmax,rymax,
                                            curx,cury,
                                            movx,movy);
                        }
                        curx=movx;
                        cury=movy;
                        // Count should always be a multiple of 2 here.
                        // assert((crossings & 1) != 0);
                        break;
                }
            }
            if(crossings!=Curve.RECT_INTERSECTS&&
                    (curx!=movx||cury!=movy)){
                crossings=
                        Curve.rectCrossingsForLine(crossings,
                                rxmin,rymin,
                                rxmax,rymax,
                                curx,cury,
                                movx,movy);
            }
            // Count should always be a multiple of 2 here.
            // assert((crossings & 1) != 0);
            return crossings;
        }

        public final void append(PathIterator pi,boolean connect){
            double coords[]=new double[6];
            while(!pi.isDone()){
                switch(pi.currentSegment(coords)){
                    case SEG_MOVETO:
                        if(!connect||numTypes<1||numCoords<1){
                            moveTo(coords[0],coords[1]);
                            break;
                        }
                        if(pointTypes[numTypes-1]!=SEG_CLOSE&&
                                doubleCoords[numCoords-2]==coords[0]&&
                                doubleCoords[numCoords-1]==coords[1]){
                            // Collapse out initial moveto/lineto
                            break;
                        }
                        lineTo(coords[0],coords[1]);
                        break;
                    case SEG_LINETO:
                        lineTo(coords[0],coords[1]);
                        break;
                    case SEG_QUADTO:
                        quadTo(coords[0],coords[1],
                                coords[2],coords[3]);
                        break;
                    case SEG_CUBICTO:
                        curveTo(coords[0],coords[1],
                                coords[2],coords[3],
                                coords[4],coords[5]);
                        break;
                    case SEG_CLOSE:
                        closePath();
                        break;
                }
                pi.next();
                connect=false;
            }
        }

        public final void transform(AffineTransform at){
            at.transform(doubleCoords,0,doubleCoords,0,numCoords/2);
        }

        public final synchronized Rectangle2D getBounds2D(){
            double x1, y1, x2, y2;
            int i=numCoords;
            if(i>0){
                y1=y2=doubleCoords[--i];
                x1=x2=doubleCoords[--i];
                while(i>0){
                    double y=doubleCoords[--i];
                    double x=doubleCoords[--i];
                    if(x<x1) x1=x;
                    if(y<y1) y1=y;
                    if(x>x2) x2=x;
                    if(y>y2) y2=y;
                }
            }else{
                x1=y1=x2=y2=0.0;
            }
            return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
        }

        public final PathIterator getPathIterator(AffineTransform at){
            if(at==null){
                return new CopyIterator(this);
            }else{
                return new TxIterator(this,at);
            }
        }

        public final Object clone(){
            // Note: It would be nice to have this return Path2D
            // but one of our subclasses (GeneralPath) needs to
            // offer "public Object clone()" for backwards
            // compatibility so we cannot restrict it further.
            // REMIND: Can we do both somehow?
            return new Double(this);
        }
    }

    static abstract class Iterator implements PathIterator{
        static final int curvecoords[]={2,2,4,6,0};
        int typeIdx;
        int pointIdx;
        Path2D path;

        Iterator(Path2D path){
            this.path=path;
        }

        public int getWindingRule(){
            return path.getWindingRule();
        }

        public boolean isDone(){
            return (typeIdx>=path.numTypes);
        }

        public void next(){
            int type=path.pointTypes[typeIdx++];
            pointIdx+=curvecoords[type];
        }
    }    public final boolean intersects(Rectangle2D r){
        return intersects(r.getX(),r.getY(),r.getWidth(),r.getHeight());
    }



    public final PathIterator getPathIterator(AffineTransform at,
                                              double flatness){
        return new FlatteningPathIterator(getPathIterator(at),flatness);
    }














}
