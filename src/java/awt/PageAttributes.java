/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.util.Locale;

public final class PageAttributes implements Cloneable{
    private ColorType color;
    private MediaType media;
    private OrientationRequestedType orientationRequested;
    private OriginType origin;
    private PrintQualityType printQuality;
    private int[] printerResolution;

    public PageAttributes(){
        setColor(ColorType.MONOCHROME);
        setMediaToDefault();
        setOrientationRequestedToDefault();
        setOrigin(OriginType.PHYSICAL);
        setPrintQualityToDefault();
        setPrinterResolutionToDefault();
    }

    public void setMediaToDefault(){
        String defaultCountry=Locale.getDefault().getCountry();
        if(defaultCountry!=null&&
                (defaultCountry.equals(Locale.US.getCountry())||
                        defaultCountry.equals(Locale.CANADA.getCountry()))){
            setMedia(MediaType.NA_LETTER);
        }else{
            setMedia(MediaType.ISO_A4);
        }
    }

    public void setOrientationRequestedToDefault(){
        setOrientationRequested(OrientationRequestedType.PORTRAIT);
    }

    public void setOrientationRequested(OrientationRequestedType
                                                orientationRequested){
        if(orientationRequested==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "orientationRequested");
        }
        this.orientationRequested=orientationRequested;
    }

    public void setPrintQualityToDefault(){
        setPrintQuality(PrintQualityType.NORMAL);
    }

    public void setPrintQuality(PrintQualityType printQuality){
        if(printQuality==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "printQuality");
        }
        this.printQuality=printQuality;
    }

    public void setPrinterResolutionToDefault(){
        setPrinterResolution(72);
    }

    public void setPrinterResolution(int printerResolution){
        setPrinterResolution(new int[]{printerResolution,printerResolution,
                3});
    }

    public PageAttributes(PageAttributes obj){
        set(obj);
    }

    public void set(PageAttributes obj){
        color=obj.color;
        media=obj.media;
        orientationRequested=obj.orientationRequested;
        origin=obj.origin;
        printQuality=obj.printQuality;
        // okay because we never modify the contents of printerResolution
        printerResolution=obj.printerResolution;
    }

    public PageAttributes(ColorType color,MediaType media,
                          OrientationRequestedType orientationRequested,
                          OriginType origin,PrintQualityType printQuality,
                          int[] printerResolution){
        setColor(color);
        setMedia(media);
        setOrientationRequested(orientationRequested);
        setOrigin(origin);
        setPrintQuality(printQuality);
        setPrinterResolution(printerResolution);
    }

    public int[] getPrinterResolution(){
        // Return a copy because otherwise client code could circumvent the
        // the checks made in setPrinterResolution by modifying the
        // returned array.
        int[] copy=new int[3];
        copy[0]=printerResolution[0];
        copy[1]=printerResolution[1];
        copy[2]=printerResolution[2];
        return copy;
    }

    public void setPrinterResolution(int[] printerResolution){
        if(printerResolution==null||
                printerResolution.length!=3||
                printerResolution[0]<=0||
                printerResolution[1]<=0||
                (printerResolution[2]!=3&&printerResolution[2]!=4)){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "printerResolution");
        }
        // Store a copy because otherwise client code could circumvent the
        // the checks made above by holding a reference to the array and
        // modifying it after calling setPrinterResolution.
        int[] copy=new int[3];
        copy[0]=printerResolution[0];
        copy[1]=printerResolution[1];
        copy[2]=printerResolution[2];
        this.printerResolution=copy;
    }

    public int hashCode(){
        return (color.hashCode()<<31^
                media.hashCode()<<24^
                orientationRequested.hashCode()<<23^
                origin.hashCode()<<22^
                printQuality.hashCode()<<20^
                printerResolution[2]>>2<<19^
                printerResolution[1]<<10^
                printerResolution[0]);
    }

    public boolean equals(Object obj){
        if(!(obj instanceof PageAttributes)){
            return false;
        }
        PageAttributes rhs=(PageAttributes)obj;
        return (color==rhs.color&&
                media==rhs.media&&
                orientationRequested==rhs.orientationRequested&&
                origin==rhs.origin&&
                printQuality==rhs.printQuality&&
                printerResolution[0]==rhs.printerResolution[0]&&
                printerResolution[1]==rhs.printerResolution[1]&&
                printerResolution[2]==rhs.printerResolution[2]);
    }

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // Since we implement Cloneable, this should never happen
            throw new InternalError(e);
        }
    }

    public String toString(){
        // int[] printerResolution = getPrinterResolution();
        return "color="+getColor()+",media="+getMedia()+
                ",orientation-requested="+getOrientationRequested()+
                ",origin="+getOrigin()+",print-quality="+getPrintQuality()+
                ",printer-resolution=["+printerResolution[0]+","+
                printerResolution[1]+","+printerResolution[2]+"]";
    }

    public ColorType getColor(){
        return color;
    }

    public void setColor(ColorType color){
        if(color==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "color");
        }
        this.color=color;
    }

    public MediaType getMedia(){
        return media;
    }

    public void setMedia(MediaType media){
        if(media==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "media");
        }
        this.media=media;
    }

    public OrientationRequestedType getOrientationRequested(){
        return orientationRequested;
    }

    public void setOrientationRequested(int orientationRequested){
        switch(orientationRequested){
            case 3:
                setOrientationRequested(OrientationRequestedType.PORTRAIT);
                break;
            case 4:
                setOrientationRequested(OrientationRequestedType.LANDSCAPE);
                break;
            default:
                // This will throw an IllegalArgumentException
                setOrientationRequested(null);
                break;
        }
    }

    public OriginType getOrigin(){
        return origin;
    }

    public void setOrigin(OriginType origin){
        if(origin==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "origin");
        }
        this.origin=origin;
    }

    public PrintQualityType getPrintQuality(){
        return printQuality;
    }

    public void setPrintQuality(int printQuality){
        switch(printQuality){
            case 3:
                setPrintQuality(PrintQualityType.DRAFT);
                break;
            case 4:
                setPrintQuality(PrintQualityType.NORMAL);
                break;
            case 5:
                setPrintQuality(PrintQualityType.HIGH);
                break;
            default:
                // This will throw an IllegalArgumentException
                setPrintQuality(null);
                break;
        }
    }

    public static final class ColorType extends AttributeValue{
        private static final int I_COLOR=0;
        public static final ColorType COLOR=new ColorType(I_COLOR);
        private static final int I_MONOCHROME=1;
        public static final ColorType MONOCHROME=new ColorType(I_MONOCHROME);
        private static final String NAMES[]={
                "color","monochrome"
        };

        private ColorType(int type){
            super(type,NAMES);
        }
    }

    public static final class MediaType extends AttributeValue{
        private static final int I_ISO_4A0=0;
        public static final MediaType ISO_4A0=new MediaType(I_ISO_4A0);
        private static final int I_ISO_2A0=1;
        public static final MediaType ISO_2A0=new MediaType(I_ISO_2A0);
        private static final int I_ISO_A0=2;
        public static final MediaType ISO_A0=new MediaType(I_ISO_A0);
        public static final MediaType A0=ISO_A0;
        private static final int I_ISO_A1=3;
        public static final MediaType ISO_A1=new MediaType(I_ISO_A1);
        public static final MediaType A1=ISO_A1;
        private static final int I_ISO_A2=4;
        public static final MediaType ISO_A2=new MediaType(I_ISO_A2);
        public static final MediaType A2=ISO_A2;
        private static final int I_ISO_A3=5;
        public static final MediaType ISO_A3=new MediaType(I_ISO_A3);
        public static final MediaType A3=ISO_A3;
        private static final int I_ISO_A4=6;
        public static final MediaType ISO_A4=new MediaType(I_ISO_A4);
        public static final MediaType A4=ISO_A4;
        private static final int I_ISO_A5=7;
        public static final MediaType ISO_A5=new MediaType(I_ISO_A5);
        public static final MediaType A5=ISO_A5;
        private static final int I_ISO_A6=8;
        public static final MediaType ISO_A6=new MediaType(I_ISO_A6);
        public static final MediaType A6=ISO_A6;
        private static final int I_ISO_A7=9;
        public static final MediaType ISO_A7=new MediaType(I_ISO_A7);
        public static final MediaType A7=ISO_A7;
        private static final int I_ISO_A8=10;
        public static final MediaType ISO_A8=new MediaType(I_ISO_A8);
        public static final MediaType A8=ISO_A8;
        private static final int I_ISO_A9=11;
        public static final MediaType ISO_A9=new MediaType(I_ISO_A9);
        public static final MediaType A9=ISO_A9;
        private static final int I_ISO_A10=12;
        public static final MediaType ISO_A10=new MediaType(I_ISO_A10);
        public static final MediaType A10=ISO_A10;
        private static final int I_ISO_B0=13;
        public static final MediaType ISO_B0=new MediaType(I_ISO_B0);
        public static final MediaType B0=ISO_B0;
        private static final int I_ISO_B1=14;
        public static final MediaType ISO_B1=new MediaType(I_ISO_B1);
        public static final MediaType B1=ISO_B1;
        private static final int I_ISO_B2=15;
        public static final MediaType ISO_B2=new MediaType(I_ISO_B2);
        public static final MediaType B2=ISO_B2;
        private static final int I_ISO_B3=16;
        public static final MediaType ISO_B3=new MediaType(I_ISO_B3);
        public static final MediaType B3=ISO_B3;
        private static final int I_ISO_B4=17;
        public static final MediaType ISO_B4=new MediaType(I_ISO_B4);
        public static final MediaType B4=ISO_B4;
        public static final MediaType ISO_B4_ENVELOPE=ISO_B4;
        private static final int I_ISO_B5=18;
        public static final MediaType ISO_B5=new MediaType(I_ISO_B5);
        public static final MediaType B5=ISO_B5;
        public static final MediaType ISO_B5_ENVELOPE=ISO_B5;
        private static final int I_ISO_B6=19;
        public static final MediaType ISO_B6=new MediaType(I_ISO_B6);
        public static final MediaType B6=ISO_B6;
        private static final int I_ISO_B7=20;
        public static final MediaType ISO_B7=new MediaType(I_ISO_B7);
        public static final MediaType B7=ISO_B7;
        private static final int I_ISO_B8=21;
        public static final MediaType ISO_B8=new MediaType(I_ISO_B8);
        public static final MediaType B8=ISO_B8;
        private static final int I_ISO_B9=22;
        public static final MediaType ISO_B9=new MediaType(I_ISO_B9);
        public static final MediaType B9=ISO_B9;
        private static final int I_ISO_B10=23;
        public static final MediaType ISO_B10=new MediaType(I_ISO_B10);
        public static final MediaType B10=ISO_B10;
        private static final int I_JIS_B0=24;
        public static final MediaType JIS_B0=new MediaType(I_JIS_B0);
        private static final int I_JIS_B1=25;
        public static final MediaType JIS_B1=new MediaType(I_JIS_B1);
        private static final int I_JIS_B2=26;
        public static final MediaType JIS_B2=new MediaType(I_JIS_B2);
        private static final int I_JIS_B3=27;
        public static final MediaType JIS_B3=new MediaType(I_JIS_B3);
        private static final int I_JIS_B4=28;
        public static final MediaType JIS_B4=new MediaType(I_JIS_B4);
        private static final int I_JIS_B5=29;
        public static final MediaType JIS_B5=new MediaType(I_JIS_B5);
        private static final int I_JIS_B6=30;
        public static final MediaType JIS_B6=new MediaType(I_JIS_B6);
        private static final int I_JIS_B7=31;
        public static final MediaType JIS_B7=new MediaType(I_JIS_B7);
        private static final int I_JIS_B8=32;
        public static final MediaType JIS_B8=new MediaType(I_JIS_B8);
        private static final int I_JIS_B9=33;
        public static final MediaType JIS_B9=new MediaType(I_JIS_B9);
        private static final int I_JIS_B10=34;
        public static final MediaType JIS_B10=new MediaType(I_JIS_B10);
        private static final int I_ISO_C0=35;
        public static final MediaType ISO_C0=new MediaType(I_ISO_C0);
        public static final MediaType C0=ISO_C0;
        public static final MediaType ISO_C0_ENVELOPE=ISO_C0;
        private static final int I_ISO_C1=36;
        public static final MediaType ISO_C1=new MediaType(I_ISO_C1);
        public static final MediaType C1=ISO_C1;
        public static final MediaType ISO_C1_ENVELOPE=ISO_C1;
        private static final int I_ISO_C2=37;
        public static final MediaType ISO_C2=new MediaType(I_ISO_C2);
        public static final MediaType C2=ISO_C2;
        public static final MediaType ISO_C2_ENVELOPE=ISO_C2;
        private static final int I_ISO_C3=38;
        public static final MediaType ISO_C3=new MediaType(I_ISO_C3);
        public static final MediaType C3=ISO_C3;
        public static final MediaType ISO_C3_ENVELOPE=ISO_C3;
        private static final int I_ISO_C4=39;
        public static final MediaType ISO_C4=new MediaType(I_ISO_C4);
        public static final MediaType C4=ISO_C4;
        public static final MediaType ISO_C4_ENVELOPE=ISO_C4;
        private static final int I_ISO_C5=40;
        public static final MediaType ISO_C5=new MediaType(I_ISO_C5);
        public static final MediaType C5=ISO_C5;
        public static final MediaType ISO_C5_ENVELOPE=ISO_C5;
        private static final int I_ISO_C6=41;
        public static final MediaType ISO_C6=new MediaType(I_ISO_C6);
        public static final MediaType C6=ISO_C6;
        public static final MediaType ISO_C6_ENVELOPE=ISO_C6;
        private static final int I_ISO_C7=42;
        public static final MediaType ISO_C7=new MediaType(I_ISO_C7);
        public static final MediaType C7=ISO_C7;
        public static final MediaType ISO_C7_ENVELOPE=ISO_C7;
        private static final int I_ISO_C8=43;
        public static final MediaType ISO_C8=new MediaType(I_ISO_C8);
        public static final MediaType C8=ISO_C8;
        public static final MediaType ISO_C8_ENVELOPE=ISO_C8;
        private static final int I_ISO_C9=44;
        public static final MediaType ISO_C9=new MediaType(I_ISO_C9);
        public static final MediaType C9=ISO_C9;
        public static final MediaType ISO_C9_ENVELOPE=ISO_C9;
        private static final int I_ISO_C10=45;
        public static final MediaType ISO_C10=new MediaType(I_ISO_C10);
        public static final MediaType C10=ISO_C10;
        public static final MediaType ISO_C10_ENVELOPE=ISO_C10;
        private static final int I_ISO_DESIGNATED_LONG=46;
        public static final MediaType ISO_DESIGNATED_LONG=
                new MediaType(I_ISO_DESIGNATED_LONG);
        public static final MediaType ISO_DESIGNATED_LONG_ENVELOPE=
                ISO_DESIGNATED_LONG;
        private static final int I_EXECUTIVE=47;
        public static final MediaType EXECUTIVE=new MediaType(I_EXECUTIVE);
        private static final int I_FOLIO=48;
        public static final MediaType FOLIO=new MediaType(I_FOLIO);
        private static final int I_INVOICE=49;
        public static final MediaType INVOICE=new MediaType(I_INVOICE);
        public static final MediaType STATEMENT=INVOICE;
        private static final int I_LEDGER=50;
        public static final MediaType LEDGER=new MediaType(I_LEDGER);
        public static final MediaType TABLOID=LEDGER;
        private static final int I_NA_LETTER=51;
        public static final MediaType NA_LETTER=new MediaType(I_NA_LETTER);
        public static final MediaType LETTER=NA_LETTER;
        public static final MediaType NOTE=NA_LETTER;
        private static final int I_NA_LEGAL=52;
        public static final MediaType NA_LEGAL=new MediaType(I_NA_LEGAL);
        public static final MediaType LEGAL=NA_LEGAL;
        private static final int I_QUARTO=53;
        public static final MediaType QUARTO=new MediaType(I_QUARTO);
        private static final int I_A=54;
        public static final MediaType A=new MediaType(I_A);
        private static final int I_B=55;
        public static final MediaType B=new MediaType(I_B);
        private static final int I_C=56;
        public static final MediaType C=new MediaType(I_C);
        private static final int I_D=57;
        public static final MediaType D=new MediaType(I_D);
        private static final int I_E=58;
        public static final MediaType E=new MediaType(I_E);
        private static final int I_NA_10X15_ENVELOPE=59;
        public static final MediaType NA_10X15_ENVELOPE=
                new MediaType(I_NA_10X15_ENVELOPE);
        public static final MediaType ENV_10X15=NA_10X15_ENVELOPE;
        private static final int I_NA_10X14_ENVELOPE=60;
        public static final MediaType NA_10X14_ENVELOPE=
                new MediaType(I_NA_10X14_ENVELOPE);
        public static final MediaType ENV_10X14=NA_10X14_ENVELOPE;
        private static final int I_NA_10X13_ENVELOPE=61;
        public static final MediaType NA_10X13_ENVELOPE=
                new MediaType(I_NA_10X13_ENVELOPE);
        public static final MediaType ENV_10X13=NA_10X13_ENVELOPE;
        private static final int I_NA_9X12_ENVELOPE=62;
        public static final MediaType NA_9X12_ENVELOPE=
                new MediaType(I_NA_9X12_ENVELOPE);
        public static final MediaType ENV_9X12=NA_9X12_ENVELOPE;
        private static final int I_NA_9X11_ENVELOPE=63;
        public static final MediaType NA_9X11_ENVELOPE=
                new MediaType(I_NA_9X11_ENVELOPE);
        public static final MediaType ENV_9X11=NA_9X11_ENVELOPE;
        private static final int I_NA_7X9_ENVELOPE=64;
        public static final MediaType NA_7X9_ENVELOPE=
                new MediaType(I_NA_7X9_ENVELOPE);
        public static final MediaType ENV_7X9=NA_7X9_ENVELOPE;
        private static final int I_NA_6X9_ENVELOPE=65;
        public static final MediaType NA_6X9_ENVELOPE=
                new MediaType(I_NA_6X9_ENVELOPE);
        public static final MediaType ENV_6X9=NA_6X9_ENVELOPE;
        private static final int I_NA_NUMBER_9_ENVELOPE=66;
        public static final MediaType NA_NUMBER_9_ENVELOPE=
                new MediaType(I_NA_NUMBER_9_ENVELOPE);
        public static final MediaType ENV_9=NA_NUMBER_9_ENVELOPE;
        private static final int I_NA_NUMBER_10_ENVELOPE=67;
        public static final MediaType NA_NUMBER_10_ENVELOPE=
                new MediaType(I_NA_NUMBER_10_ENVELOPE);
        public static final MediaType ENV_10=NA_NUMBER_10_ENVELOPE;
        private static final int I_NA_NUMBER_11_ENVELOPE=68;
        public static final MediaType NA_NUMBER_11_ENVELOPE=
                new MediaType(I_NA_NUMBER_11_ENVELOPE);
        public static final MediaType ENV_11=NA_NUMBER_11_ENVELOPE;
        private static final int I_NA_NUMBER_12_ENVELOPE=69;
        public static final MediaType NA_NUMBER_12_ENVELOPE=
                new MediaType(I_NA_NUMBER_12_ENVELOPE);
        public static final MediaType ENV_12=NA_NUMBER_12_ENVELOPE;
        private static final int I_NA_NUMBER_14_ENVELOPE=70;
        public static final MediaType NA_NUMBER_14_ENVELOPE=
                new MediaType(I_NA_NUMBER_14_ENVELOPE);
        public static final MediaType ENV_14=NA_NUMBER_14_ENVELOPE;
        private static final int I_INVITE_ENVELOPE=71;
        public static final MediaType INVITE_ENVELOPE=
                new MediaType(I_INVITE_ENVELOPE);
        public static final MediaType ENV_INVITE=INVITE_ENVELOPE;
        public static final MediaType INVITE=INVITE_ENVELOPE;
        private static final int I_ITALY_ENVELOPE=72;
        public static final MediaType ITALY_ENVELOPE=
                new MediaType(I_ITALY_ENVELOPE);
        public static final MediaType ENV_ITALY=ITALY_ENVELOPE;
        public static final MediaType ITALY=ITALY_ENVELOPE;
        private static final int I_MONARCH_ENVELOPE=73;
        public static final MediaType MONARCH_ENVELOPE=
                new MediaType(I_MONARCH_ENVELOPE);
        public static final MediaType ENV_MONARCH=MONARCH_ENVELOPE;
        public static final MediaType MONARCH=MONARCH_ENVELOPE;
        private static final int I_PERSONAL_ENVELOPE=74;
        public static final MediaType PERSONAL_ENVELOPE=
                new MediaType(I_PERSONAL_ENVELOPE);
        public static final MediaType ENV_PERSONAL=PERSONAL_ENVELOPE;
        public static final MediaType PERSONAL=PERSONAL_ENVELOPE;
        private static final String NAMES[]={
                "iso-4a0","iso-2a0","iso-a0","iso-a1","iso-a2","iso-a3",
                "iso-a4","iso-a5","iso-a6","iso-a7","iso-a8","iso-a9",
                "iso-a10","iso-b0","iso-b1","iso-b2","iso-b3","iso-b4",
                "iso-b5","iso-b6","iso-b7","iso-b8","iso-b9","iso-b10",
                "jis-b0","jis-b1","jis-b2","jis-b3","jis-b4","jis-b5",
                "jis-b6","jis-b7","jis-b8","jis-b9","jis-b10","iso-c0",
                "iso-c1","iso-c2","iso-c3","iso-c4","iso-c5","iso-c6",
                "iso-c7","iso-c8","iso-c9","iso-c10","iso-designated-long",
                "executive","folio","invoice","ledger","na-letter","na-legal",
                "quarto","a","b","c","d","e","na-10x15-envelope",
                "na-10x14-envelope","na-10x13-envelope","na-9x12-envelope",
                "na-9x11-envelope","na-7x9-envelope","na-6x9-envelope",
                "na-number-9-envelope","na-number-10-envelope",
                "na-number-11-envelope","na-number-12-envelope",
                "na-number-14-envelope","invite-envelope","italy-envelope",
                "monarch-envelope","personal-envelope"
        };

        private MediaType(int type){
            super(type,NAMES);
        }
    }

    public static final class OrientationRequestedType extends AttributeValue{
        private static final int I_PORTRAIT=0;
        public static final OrientationRequestedType PORTRAIT=
                new OrientationRequestedType(I_PORTRAIT);
        private static final int I_LANDSCAPE=1;
        public static final OrientationRequestedType LANDSCAPE=
                new OrientationRequestedType(I_LANDSCAPE);
        private static final String NAMES[]={
                "portrait","landscape"
        };

        private OrientationRequestedType(int type){
            super(type,NAMES);
        }
    }

    public static final class OriginType extends AttributeValue{
        private static final int I_PHYSICAL=0;
        public static final OriginType PHYSICAL=new OriginType(I_PHYSICAL);
        private static final int I_PRINTABLE=1;
        public static final OriginType PRINTABLE=new OriginType(I_PRINTABLE);
        private static final String NAMES[]={
                "physical","printable"
        };

        private OriginType(int type){
            super(type,NAMES);
        }
    }

    public static final class PrintQualityType extends AttributeValue{
        private static final int I_HIGH=0;
        public static final PrintQualityType HIGH=
                new PrintQualityType(I_HIGH);
        private static final int I_NORMAL=1;
        public static final PrintQualityType NORMAL=
                new PrintQualityType(I_NORMAL);
        private static final int I_DRAFT=2;
        public static final PrintQualityType DRAFT=
                new PrintQualityType(I_DRAFT);
        private static final String NAMES[]={
                "high","normal","draft"
        };

        private PrintQualityType(int type){
            super(type,NAMES);
        }
    }
}
