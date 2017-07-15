/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.Size2DSyntax;
import java.util.HashMap;
import java.util.Vector;

public class MediaSize extends Size2DSyntax implements Attribute{
    private static final long serialVersionUID=-1967958664615414771L;
    private static HashMap mediaMap=new HashMap(100,10);
    private static Vector sizeVector=new Vector(100,10);

    /** force loading of all the subclasses so that the instances
     * are created and inserted into the hashmap.
     */
    static{
        MediaSize ISOA4=ISO.A4;
        MediaSize JISB5=JIS.B5;
        MediaSize NALETTER=NA.LETTER;
        MediaSize EngineeringC=Engineering.C;
        MediaSize OtherEXECUTIVE=Other.EXECUTIVE;
    }

    private MediaSizeName mediaName;

    public MediaSize(float x,float y,int units){
        super(x,y,units);
        if(x>y){
            throw new IllegalArgumentException("X dimension > Y dimension");
        }
        sizeVector.add(this);
    }

    public MediaSize(int x,int y,int units){
        super(x,y,units);
        if(x>y){
            throw new IllegalArgumentException("X dimension > Y dimension");
        }
        sizeVector.add(this);
    }

    public MediaSize(float x,float y,int units,MediaSizeName media){
        super(x,y,units);
        if(x>y){
            throw new IllegalArgumentException("X dimension > Y dimension");
        }
        if(media!=null&&mediaMap.get(media)==null){
            mediaName=media;
            mediaMap.put(mediaName,this);
        }
        sizeVector.add(this);
    }

    public MediaSize(int x,int y,int units,MediaSizeName media){
        super(x,y,units);
        if(x>y){
            throw new IllegalArgumentException("X dimension > Y dimension");
        }
        if(media!=null&&mediaMap.get(media)==null){
            mediaName=media;
            mediaMap.put(mediaName,this);
        }
        sizeVector.add(this);
    }

    public static MediaSize getMediaSizeForName(MediaSizeName media){
        return (MediaSize)mediaMap.get(media);
    }

    public static MediaSizeName findMedia(float x,float y,int units){
        MediaSize match=ISO.A4;
        if(x<=0.0f||y<=0.0f||units<1){
            throw new IllegalArgumentException("args must be +ve values");
        }
        double ls=x*x+y*y;
        double tmp_ls;
        float[] dim;
        float diffx=x;
        float diffy=y;
        for(int i=0;i<sizeVector.size();i++){
            MediaSize mediaSize=(MediaSize)sizeVector.elementAt(i);
            dim=mediaSize.getSize(units);
            if(x==dim[0]&&y==dim[1]){
                match=mediaSize;
                break;
            }else{
                diffx=x-dim[0];
                diffy=y-dim[1];
                tmp_ls=diffx*diffx+diffy*diffy;
                if(tmp_ls<ls){
                    ls=tmp_ls;
                    match=mediaSize;
                }
            }
        }
        return match.getMediaSizeName();
    }

    public MediaSizeName getMediaSizeName(){
        return mediaName;
    }

    public boolean equals(Object object){
        return (super.equals(object)&&object instanceof MediaSize);
    }

    public final Class<? extends Attribute> getCategory(){
        return MediaSize.class;
    }

    public final String getName(){
        return "media-size";
    }

    public final static class ISO{
        public static final MediaSize
                A0=new MediaSize(841,1189,Size2DSyntax.MM,MediaSizeName.ISO_A0);
        public static final MediaSize
                A1=new MediaSize(594,841,Size2DSyntax.MM,MediaSizeName.ISO_A1);
        public static final MediaSize
                A2=new MediaSize(420,594,Size2DSyntax.MM,MediaSizeName.ISO_A2);
        public static final MediaSize
                A3=new MediaSize(297,420,Size2DSyntax.MM,MediaSizeName.ISO_A3);
        public static final MediaSize
                A4=new MediaSize(210,297,Size2DSyntax.MM,MediaSizeName.ISO_A4);
        public static final MediaSize
                A5=new MediaSize(148,210,Size2DSyntax.MM,MediaSizeName.ISO_A5);
        public static final MediaSize
                A6=new MediaSize(105,148,Size2DSyntax.MM,MediaSizeName.ISO_A6);
        public static final MediaSize
                A7=new MediaSize(74,105,Size2DSyntax.MM,MediaSizeName.ISO_A7);
        public static final MediaSize
                A8=new MediaSize(52,74,Size2DSyntax.MM,MediaSizeName.ISO_A8);
        public static final MediaSize
                A9=new MediaSize(37,52,Size2DSyntax.MM,MediaSizeName.ISO_A9);
        public static final MediaSize
                A10=new MediaSize(26,37,Size2DSyntax.MM,MediaSizeName.ISO_A10);
        public static final MediaSize
                B0=new MediaSize(1000,1414,Size2DSyntax.MM,MediaSizeName.ISO_B0);
        public static final MediaSize
                B1=new MediaSize(707,1000,Size2DSyntax.MM,MediaSizeName.ISO_B1);
        public static final MediaSize
                B2=new MediaSize(500,707,Size2DSyntax.MM,MediaSizeName.ISO_B2);
        public static final MediaSize
                B3=new MediaSize(353,500,Size2DSyntax.MM,MediaSizeName.ISO_B3);
        public static final MediaSize
                B4=new MediaSize(250,353,Size2DSyntax.MM,MediaSizeName.ISO_B4);
        public static final MediaSize
                B5=new MediaSize(176,250,Size2DSyntax.MM,MediaSizeName.ISO_B5);
        public static final MediaSize
                B6=new MediaSize(125,176,Size2DSyntax.MM,MediaSizeName.ISO_B6);
        public static final MediaSize
                B7=new MediaSize(88,125,Size2DSyntax.MM,MediaSizeName.ISO_B7);
        public static final MediaSize
                B8=new MediaSize(62,88,Size2DSyntax.MM,MediaSizeName.ISO_B8);
        public static final MediaSize
                B9=new MediaSize(44,62,Size2DSyntax.MM,MediaSizeName.ISO_B9);
        public static final MediaSize
                B10=new MediaSize(31,44,Size2DSyntax.MM,MediaSizeName.ISO_B10);
        public static final MediaSize
                C3=new MediaSize(324,458,Size2DSyntax.MM,MediaSizeName.ISO_C3);
        public static final MediaSize
                C4=new MediaSize(229,324,Size2DSyntax.MM,MediaSizeName.ISO_C4);
        public static final MediaSize
                C5=new MediaSize(162,229,Size2DSyntax.MM,MediaSizeName.ISO_C5);
        public static final MediaSize
                C6=new MediaSize(114,162,Size2DSyntax.MM,MediaSizeName.ISO_C6);
        public static final MediaSize
                DESIGNATED_LONG=new MediaSize(110,220,Size2DSyntax.MM,
                MediaSizeName.ISO_DESIGNATED_LONG);

        private ISO(){
        }
    }

    public final static class JIS{
        public static final MediaSize
                B0=new MediaSize(1030,1456,Size2DSyntax.MM,MediaSizeName.JIS_B0);
        public static final MediaSize
                B1=new MediaSize(728,1030,Size2DSyntax.MM,MediaSizeName.JIS_B1);
        public static final MediaSize
                B2=new MediaSize(515,728,Size2DSyntax.MM,MediaSizeName.JIS_B2);
        public static final MediaSize
                B3=new MediaSize(364,515,Size2DSyntax.MM,MediaSizeName.JIS_B3);
        public static final MediaSize
                B4=new MediaSize(257,364,Size2DSyntax.MM,MediaSizeName.JIS_B4);
        public static final MediaSize
                B5=new MediaSize(182,257,Size2DSyntax.MM,MediaSizeName.JIS_B5);
        public static final MediaSize
                B6=new MediaSize(128,182,Size2DSyntax.MM,MediaSizeName.JIS_B6);
        public static final MediaSize
                B7=new MediaSize(91,128,Size2DSyntax.MM,MediaSizeName.JIS_B7);
        public static final MediaSize
                B8=new MediaSize(64,91,Size2DSyntax.MM,MediaSizeName.JIS_B8);
        public static final MediaSize
                B9=new MediaSize(45,64,Size2DSyntax.MM,MediaSizeName.JIS_B9);
        public static final MediaSize
                B10=new MediaSize(32,45,Size2DSyntax.MM,MediaSizeName.JIS_B10);
        public static final MediaSize CHOU_1=new MediaSize(142,332,Size2DSyntax.MM);
        public static final MediaSize CHOU_2=new MediaSize(119,277,Size2DSyntax.MM);
        public static final MediaSize CHOU_3=new MediaSize(120,235,Size2DSyntax.MM);
        public static final MediaSize CHOU_4=new MediaSize(90,205,Size2DSyntax.MM);
        public static final MediaSize CHOU_30=new MediaSize(92,235,Size2DSyntax.MM);
        public static final MediaSize CHOU_40=new MediaSize(90,225,Size2DSyntax.MM);
        public static final MediaSize KAKU_0=new MediaSize(287,382,Size2DSyntax.MM);
        public static final MediaSize KAKU_1=new MediaSize(270,382,Size2DSyntax.MM);
        public static final MediaSize KAKU_2=new MediaSize(240,332,Size2DSyntax.MM);
        public static final MediaSize KAKU_3=new MediaSize(216,277,Size2DSyntax.MM);
        public static final MediaSize KAKU_4=new MediaSize(197,267,Size2DSyntax.MM);
        public static final MediaSize KAKU_5=new MediaSize(190,240,Size2DSyntax.MM);
        public static final MediaSize KAKU_6=new MediaSize(162,229,Size2DSyntax.MM);
        public static final MediaSize KAKU_7=new MediaSize(142,205,Size2DSyntax.MM);
        public static final MediaSize KAKU_8=new MediaSize(119,197,Size2DSyntax.MM);
        public static final MediaSize KAKU_20=new MediaSize(229,324,Size2DSyntax.MM);
        public static final MediaSize KAKU_A4=new MediaSize(228,312,Size2DSyntax.MM);
        public static final MediaSize YOU_1=new MediaSize(120,176,Size2DSyntax.MM);
        public static final MediaSize YOU_2=new MediaSize(114,162,Size2DSyntax.MM);
        public static final MediaSize YOU_3=new MediaSize(98,148,Size2DSyntax.MM);
        public static final MediaSize YOU_4=new MediaSize(105,235,Size2DSyntax.MM);
        public static final MediaSize YOU_5=new MediaSize(95,217,Size2DSyntax.MM);
        public static final MediaSize YOU_6=new MediaSize(98,190,Size2DSyntax.MM);
        public static final MediaSize YOU_7=new MediaSize(92,165,Size2DSyntax.MM);

        private JIS(){
        }
    }

    public final static class NA{
        public static final MediaSize
                LETTER=new MediaSize(8.5f,11.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_LETTER);
        public static final MediaSize
                LEGAL=new MediaSize(8.5f,14.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_LEGAL);
        public static final MediaSize
                NA_5X7=new MediaSize(5,7,Size2DSyntax.INCH,
                MediaSizeName.NA_5X7);
        public static final MediaSize
                NA_8X10=new MediaSize(8,10,Size2DSyntax.INCH,
                MediaSizeName.NA_8X10);
        public static final MediaSize
                NA_NUMBER_9_ENVELOPE=
                new MediaSize(3.875f,8.875f,Size2DSyntax.INCH,
                        MediaSizeName.NA_NUMBER_9_ENVELOPE);
        public static final MediaSize
                NA_NUMBER_10_ENVELOPE=
                new MediaSize(4.125f,9.5f,Size2DSyntax.INCH,
                        MediaSizeName.NA_NUMBER_10_ENVELOPE);
        public static final MediaSize
                NA_NUMBER_11_ENVELOPE=
                new MediaSize(4.5f,10.375f,Size2DSyntax.INCH,
                        MediaSizeName.NA_NUMBER_11_ENVELOPE);
        public static final MediaSize
                NA_NUMBER_12_ENVELOPE=
                new MediaSize(4.75f,11.0f,Size2DSyntax.INCH,
                        MediaSizeName.NA_NUMBER_12_ENVELOPE);
        public static final MediaSize
                NA_NUMBER_14_ENVELOPE=
                new MediaSize(5.0f,11.5f,Size2DSyntax.INCH,
                        MediaSizeName.NA_NUMBER_14_ENVELOPE);
        public static final MediaSize
                NA_6X9_ENVELOPE=new MediaSize(6.0f,9.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_6X9_ENVELOPE);
        public static final MediaSize
                NA_7X9_ENVELOPE=new MediaSize(7.0f,9.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_7X9_ENVELOPE);
        public static final MediaSize
                NA_9x11_ENVELOPE=new MediaSize(9.0f,11.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_9X11_ENVELOPE);
        public static final MediaSize
                NA_9x12_ENVELOPE=new MediaSize(9.0f,12.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_9X12_ENVELOPE);
        public static final MediaSize
                NA_10x13_ENVELOPE=new MediaSize(10.0f,13.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_10X13_ENVELOPE);
        public static final MediaSize
                NA_10x14_ENVELOPE=new MediaSize(10.0f,14.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_10X14_ENVELOPE);
        public static final MediaSize
                NA_10X15_ENVELOPE=new MediaSize(10.0f,15.0f,Size2DSyntax.INCH,
                MediaSizeName.NA_10X15_ENVELOPE);

        private NA(){
        }
    }

    public final static class Engineering{
        public static final MediaSize
                A=new MediaSize(8.5f,11.0f,Size2DSyntax.INCH,
                MediaSizeName.A);
        public static final MediaSize
                B=new MediaSize(11.0f,17.0f,Size2DSyntax.INCH,
                MediaSizeName.B);
        public static final MediaSize
                C=new MediaSize(17.0f,22.0f,Size2DSyntax.INCH,
                MediaSizeName.C);
        public static final MediaSize
                D=new MediaSize(22.0f,34.0f,Size2DSyntax.INCH,
                MediaSizeName.D);
        public static final MediaSize
                E=new MediaSize(34.0f,44.0f,Size2DSyntax.INCH,
                MediaSizeName.E);

        private Engineering(){
        }
    }

    public final static class Other{
        public static final MediaSize
                EXECUTIVE=new MediaSize(7.25f,10.5f,Size2DSyntax.INCH,
                MediaSizeName.EXECUTIVE);
        public static final MediaSize
                LEDGER=new MediaSize(11.0f,17.0f,Size2DSyntax.INCH,
                MediaSizeName.LEDGER);
        public static final MediaSize
                TABLOID=new MediaSize(11.0f,17.0f,Size2DSyntax.INCH,
                MediaSizeName.TABLOID);
        public static final MediaSize
                INVOICE=new MediaSize(5.5f,8.5f,Size2DSyntax.INCH,
                MediaSizeName.INVOICE);
        public static final MediaSize
                FOLIO=new MediaSize(8.5f,13.0f,Size2DSyntax.INCH,
                MediaSizeName.FOLIO);
        public static final MediaSize
                QUARTO=new MediaSize(8.5f,10.83f,Size2DSyntax.INCH,
                MediaSizeName.QUARTO);
        public static final MediaSize
                ITALY_ENVELOPE=new MediaSize(110,230,Size2DSyntax.MM,
                MediaSizeName.ITALY_ENVELOPE);
        public static final MediaSize
                MONARCH_ENVELOPE=new MediaSize(3.87f,7.5f,Size2DSyntax.INCH,
                MediaSizeName.MONARCH_ENVELOPE);
        public static final MediaSize
                PERSONAL_ENVELOPE=new MediaSize(3.625f,6.5f,Size2DSyntax.INCH,
                MediaSizeName.PERSONAL_ENVELOPE);
        public static final MediaSize
                JAPANESE_POSTCARD=new MediaSize(100,148,Size2DSyntax.MM,
                MediaSizeName.JAPANESE_POSTCARD);
        public static final MediaSize
                JAPANESE_DOUBLE_POSTCARD=new MediaSize(148,200,Size2DSyntax.MM,
                MediaSizeName.JAPANESE_DOUBLE_POSTCARD);

        private Other(){
        }
    }
}
