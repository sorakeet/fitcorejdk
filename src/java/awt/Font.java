/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.font.*;

import java.awt.font.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.peer.FontPeer;
import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.CharacterIterator;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import static sun.font.EAttribute.*;

public class Font implements Serializable{
    public static final String DIALOG="Dialog";
    public static final String DIALOG_INPUT="DialogInput";
    public static final String SANS_SERIF="SansSerif";
    public static final String SERIF="Serif";
    public static final String MONOSPACED="Monospaced";
    public static final int PLAIN=0;
    public static final int BOLD=1;
    public static final int ITALIC=2;
    public static final int ROMAN_BASELINE=0;
    public static final int CENTER_BASELINE=1;
    public static final int HANGING_BASELINE=2;
    public static final int TRUETYPE_FONT=0;
    public static final int TYPE1_FONT=1;
    public static final int LAYOUT_LEFT_TO_RIGHT=0;
    public static final int LAYOUT_RIGHT_TO_LEFT=1;
    public static final int LAYOUT_NO_START_CONTEXT=2;
    public static final int LAYOUT_NO_LIMIT_CONTEXT=4;
    private static final AffineTransform identityTx=new AffineTransform();
    private static final long serialVersionUID=-4206021311591459213L;
    private static final int RECOGNIZED_MASK=AttributeValues.MASK_ALL
            &~AttributeValues.getMask(EFONT);
    private static final int PRIMARY_MASK=
            AttributeValues.getMask(EFAMILY,EWEIGHT,EWIDTH,EPOSTURE,ESIZE,
                    ETRANSFORM,ESUPERSCRIPT,ETRACKING);
    private static final int SECONDARY_MASK=
            RECOGNIZED_MASK&~PRIMARY_MASK;
    private static final int LAYOUT_MASK=
            AttributeValues.getMask(ECHAR_REPLACEMENT,EFOREGROUND,EBACKGROUND,
                    EUNDERLINE,ESTRIKETHROUGH,ERUN_DIRECTION,
                    EBIDI_EMBEDDING,EJUSTIFICATION,
                    EINPUT_METHOD_HIGHLIGHT,EINPUT_METHOD_UNDERLINE,
                    ESWAP_COLORS,ENUMERIC_SHAPING,EKERNING,
                    ELIGATURES,ETRACKING,ESUPERSCRIPT);
    private static final int EXTRA_MASK=
            AttributeValues.getMask(ETRANSFORM,ESUPERSCRIPT,EWIDTH);
    // x = r^0 + r^1 + r^2... r^n
    // rx = r^1 + r^2 + r^3... r^(n+1)
    // x - rx = r^0 - r^(n+1)
    // x (1 - r) = r^0 - r^(n+1)
    // x = (r^0 - r^(n+1)) / (1 - r)
    // x = (1 - r^(n+1)) / (1 - r)
    // scale ratio is 2/3
    // trans = 1/2 of ascent * x
    // assume ascent is 3/4 of point size
    private static final float[] ssinfo={
            0.0f,
            0.375f,
            0.625f,
            0.7916667f,
            0.9027778f,
            0.9768519f,
            1.0262346f,
            1.0591564f,
    };

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        initIDs();
        FontAccess.setFontAccess(new FontAccessImpl());
    }

    protected String name;
    protected int style;
    protected int size;
    protected float pointSize;
    transient int hash;
    private Hashtable<Object,Object> fRequestedAttributes;
    private transient FontPeer peer;
    private transient long pData;       // native JDK1.1 font pointer
    private transient Font2DHandle font2DHandle;
    private transient AttributeValues values;
    private transient boolean hasLayoutAttributes;
    private transient boolean createdFont=false;
    private transient boolean nonIdentityTx;
    private int fontSerializedDataVersion=1;
    private transient SoftReference<FontLineMetrics> flmref;

    public Font(String name,int style,int size){
        this.name=(name!=null)?name:"Default";
        this.style=(style&~0x03)==0?style:0;
        this.size=size;
        this.pointSize=size;
    }

    private Font(String name,int style,float sizePts,
                 boolean created,Font2DHandle handle){
        this(name,style,sizePts);
        this.createdFont=created;
        /** Fonts created from a stream will use the same font2D instance
         * as the parent.
         * One exception is that if the derived font is requested to be
         * in a different style, then also check if its a CompositeFont
         * and if so build a new CompositeFont from components of that style.
         * CompositeFonts can only be marked as "created" if they are used
         * to add fall backs to a physical font. And non-composites are
         * always from "Font.createFont()" and shouldn't get this treatment.
         */
        if(created){
            if(handle.font2D instanceof CompositeFont&&
                    handle.font2D.getStyle()!=style){
                FontManager fm=FontManagerFactory.getInstance();
                this.font2DHandle=fm.getNewComposite(null,style,handle);
            }else{
                this.font2DHandle=handle;
            }
        }
    }

    private Font(String name,int style,float sizePts){
        this.name=(name!=null)?name:"Default";
        this.style=(style&~0x03)==0?style:0;
        this.size=(int)(sizePts+0.5);
        this.pointSize=sizePts;
    }

    private Font(File fontFile,int fontFormat,
                 boolean isCopy,CreatedFontTracker tracker)
            throws FontFormatException{
        this.createdFont=true;
        /** Font2D instances created by this method track their font file
         * so that when the Font2D is GC'd it can also remove the file.
         */
        FontManager fm=FontManagerFactory.getInstance();
        this.font2DHandle=fm.createFont2D(fontFile,fontFormat,isCopy,
                tracker).handle;
        this.name=this.font2DHandle.font2D.getFontName(Locale.getDefault());
        this.style=Font.PLAIN;
        this.size=1;
        this.pointSize=1f;
    }

    private Font(AttributeValues values,String oldName,int oldStyle,
                 boolean created,Font2DHandle handle){
        this.createdFont=created;
        if(created){
            this.font2DHandle=handle;
            String newName=null;
            if(oldName!=null){
                newName=values.getFamily();
                if(oldName.equals(newName)) newName=null;
            }
            int newStyle=0;
            if(oldStyle==-1){
                newStyle=-1;
            }else{
                if(values.getWeight()>=2f) newStyle=BOLD;
                if(values.getPosture()>=.2f) newStyle|=ITALIC;
                if(oldStyle==newStyle) newStyle=-1;
            }
            if(handle.font2D instanceof CompositeFont){
                if(newStyle!=-1||newName!=null){
                    FontManager fm=FontManagerFactory.getInstance();
                    this.font2DHandle=
                            fm.getNewComposite(newName,newStyle,handle);
                }
            }else if(newName!=null){
                this.createdFont=false;
                this.font2DHandle=null;
            }
        }
        initFromValues(values);
    }

    private void initFromValues(AttributeValues values){
        this.values=values;
        values.defineAll(PRIMARY_MASK); // for 1.5 streaming compatibility
        this.name=values.getFamily();
        this.pointSize=values.getSize();
        this.size=(int)(values.getSize()+0.5);
        if(values.getWeight()>=2f) this.style|=BOLD; // not == 2f
        if(values.getPosture()>=.2f) this.style|=ITALIC; // not  == .2f
        this.nonIdentityTx=values.anyNonDefault(EXTRA_MASK);
        this.hasLayoutAttributes=values.anyNonDefault(LAYOUT_MASK);
    }

    public Font(Map<? extends Attribute,?> attributes){
        initFromValues(AttributeValues.fromMap(attributes,RECOGNIZED_MASK));
    }

    protected Font(Font font){
        if(font.values!=null){
            initFromValues(font.getAttributeValues().clone());
        }else{
            this.name=font.name;
            this.style=font.style;
            this.size=font.size;
            this.pointSize=font.pointSize;
        }
        this.font2DHandle=font.font2DHandle;
        this.createdFont=font.createdFont;
    }

    public static Font getFont(Map<? extends Attribute,?> attributes){
        // optimize for two cases:
        // 1) FONT attribute, and nothing else
        // 2) attributes, but no FONT
        // avoid turning the attributemap into a regular map for no reason
        if(attributes instanceof AttributeMap&&
                ((AttributeMap)attributes).getValues()!=null){
            AttributeValues values=((AttributeMap)attributes).getValues();
            if(values.isNonDefault(EFONT)){
                Font font=values.getFont();
                if(!values.anyDefined(SECONDARY_MASK)){
                    return font;
                }
                // merge
                values=font.getAttributeValues().clone();
                values.merge(attributes,SECONDARY_MASK);
                return new Font(values,font.name,font.style,
                        font.createdFont,font.font2DHandle);
            }
            return new Font(attributes);
        }
        Font font=(Font)attributes.get(TextAttribute.FONT);
        if(font!=null){
            if(attributes.size()>1){ // oh well, check for anything else
                AttributeValues values=font.getAttributeValues().clone();
                values.merge(attributes,SECONDARY_MASK);
                return new Font(values,font.name,font.style,
                        font.createdFont,font.font2DHandle);
            }
            return font;
        }
        return new Font(attributes);
    }

    public static Font createFont(int fontFormat,InputStream fontStream)
            throws FontFormatException, IOException{
        if(hasTempPermission()){
            return createFont0(fontFormat,fontStream,null);
        }
        // Otherwise, be extra conscious of pending temp file creation and
        // resourcefully handle the temp file resources, among other things.
        CreatedFontTracker tracker=CreatedFontTracker.getTracker();
        boolean acquired=false;
        try{
            acquired=tracker.acquirePermit();
            if(!acquired){
                throw new IOException("Timed out waiting for resources.");
            }
            return createFont0(fontFormat,fontStream,tracker);
        }catch(InterruptedException e){
            throw new IOException("Problem reading font data.");
        }finally{
            if(acquired){
                tracker.releasePermit();
            }
        }
    }

    private static boolean hasTempPermission(){
        if(System.getSecurityManager()==null){
            return true;
        }
        File f=null;
        boolean hasPerm=false;
        try{
            f=Files.createTempFile("+~JT",".tmp").toFile();
            f.delete();
            f=null;
            hasPerm=true;
        }catch(Throwable t){
            /** inc. any kind of SecurityException */
        }
        return hasPerm;
    }

    private static Font createFont0(int fontFormat,InputStream fontStream,
                                    CreatedFontTracker tracker)
            throws FontFormatException, IOException{
        if(fontFormat!=Font.TRUETYPE_FONT&&
                fontFormat!=Font.TYPE1_FONT){
            throw new IllegalArgumentException("font format not recognized");
        }
        boolean copiedFontData=false;
        try{
            final File tFile=AccessController.doPrivileged(
                    new PrivilegedExceptionAction<File>(){
                        public File run() throws IOException{
                            return Files.createTempFile("+~JF",".tmp").toFile();
                        }
                    }
            );
            if(tracker!=null){
                tracker.add(tFile);
            }
            int totalSize=0;
            try{
                final OutputStream outStream=
                        AccessController.doPrivileged(
                                new PrivilegedExceptionAction<OutputStream>(){
                                    public OutputStream run() throws IOException{
                                        return new FileOutputStream(tFile);
                                    }
                                }
                        );
                if(tracker!=null){
                    tracker.set(tFile,outStream);
                }
                try{
                    byte[] buf=new byte[8192];
                    for(;;){
                        int bytesRead=fontStream.read(buf);
                        if(bytesRead<0){
                            break;
                        }
                        if(tracker!=null){
                            if(totalSize+bytesRead>CreatedFontTracker.MAX_FILE_SIZE){
                                throw new IOException("File too big.");
                            }
                            if(totalSize+tracker.getNumBytes()>
                                    CreatedFontTracker.MAX_TOTAL_BYTES){
                                throw new IOException("Total files too big.");
                            }
                            totalSize+=bytesRead;
                            tracker.addBytes(bytesRead);
                        }
                        outStream.write(buf,0,bytesRead);
                    }
                    /** don't close the input stream */
                }finally{
                    outStream.close();
                }
                /** After all references to a Font2D are dropped, the file
                 * will be removed. To support long-lived AppContexts,
                 * we need to then decrement the byte count by the size
                 * of the file.
                 * If the data isn't a valid font, the implementation will
                 * delete the tmp file and decrement the byte count
                 * in the tracker object before returning from the
                 * constructor, so we can set 'copiedFontData' to true here
                 * without waiting for the results of that constructor.
                 */
                copiedFontData=true;
                Font font=new Font(tFile,fontFormat,true,tracker);
                return font;
            }finally{
                if(tracker!=null){
                    tracker.remove(tFile);
                }
                if(!copiedFontData){
                    if(tracker!=null){
                        tracker.subBytes(totalSize);
                    }
                    AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Void>(){
                                public Void run(){
                                    tFile.delete();
                                    return null;
                                }
                            }
                    );
                }
            }
        }catch(Throwable t){
            if(t instanceof FontFormatException){
                throw (FontFormatException)t;
            }
            if(t instanceof IOException){
                throw (IOException)t;
            }
            Throwable cause=t.getCause();
            if(cause instanceof FontFormatException){
                throw (FontFormatException)cause;
            }
            throw new IOException("Problem reading font data.");
        }
    }

    public static Font createFont(int fontFormat,File fontFile)
            throws FontFormatException, IOException{
        fontFile=new File(fontFile.getPath());
        if(fontFormat!=Font.TRUETYPE_FONT&&
                fontFormat!=Font.TYPE1_FONT){
            throw new IllegalArgumentException("font format not recognized");
        }
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            FilePermission filePermission=
                    new FilePermission(fontFile.getPath(),"read");
            sm.checkPermission(filePermission);
        }
        if(!fontFile.canRead()){
            throw new IOException("Can't read "+fontFile);
        }
        return new Font(fontFile,fontFormat,false,null);
    }

    public static Font getFont(String nm){
        return getFont(nm,null);
    }

    public static Font getFont(String nm,Font font){
        String str=null;
        try{
            str=System.getProperty(nm);
        }catch(SecurityException e){
        }
        if(str==null){
            return font;
        }
        return decode(str);
    }

    public static Font decode(String str){
        String fontName=str;
        String styleName="";
        int fontSize=12;
        int fontStyle=Font.PLAIN;
        if(str==null){
            return new Font(DIALOG,fontStyle,fontSize);
        }
        int lastHyphen=str.lastIndexOf('-');
        int lastSpace=str.lastIndexOf(' ');
        char sepChar=(lastHyphen>lastSpace)?'-':' ';
        int sizeIndex=str.lastIndexOf(sepChar);
        int styleIndex=str.lastIndexOf(sepChar,sizeIndex-1);
        int strlen=str.length();
        if(sizeIndex>0&&sizeIndex+1<strlen){
            try{
                fontSize=
                        Integer.valueOf(str.substring(sizeIndex+1)).intValue();
                if(fontSize<=0){
                    fontSize=12;
                }
            }catch(NumberFormatException e){
                /** It wasn't a valid size, if we didn't also find the
                 * start of the style string perhaps this is the style */
                styleIndex=sizeIndex;
                sizeIndex=strlen;
                if(str.charAt(sizeIndex-1)==sepChar){
                    sizeIndex--;
                }
            }
        }
        if(styleIndex>=0&&styleIndex+1<strlen){
            styleName=str.substring(styleIndex+1,sizeIndex);
            styleName=styleName.toLowerCase(Locale.ENGLISH);
            if(styleName.equals("bolditalic")){
                fontStyle=Font.BOLD|Font.ITALIC;
            }else if(styleName.equals("italic")){
                fontStyle=Font.ITALIC;
            }else if(styleName.equals("bold")){
                fontStyle=Font.BOLD;
            }else if(styleName.equals("plain")){
                fontStyle=Font.PLAIN;
            }else{
                /** this string isn't any of the expected styles, so
                 * assume its part of the font name
                 */
                styleIndex=sizeIndex;
                if(str.charAt(styleIndex-1)==sepChar){
                    styleIndex--;
                }
            }
            fontName=str.substring(0,styleIndex);
        }else{
            int fontEnd=strlen;
            if(styleIndex>0){
                fontEnd=styleIndex;
            }else if(sizeIndex>0){
                fontEnd=sizeIndex;
            }
            if(fontEnd>0&&str.charAt(fontEnd-1)==sepChar){
                fontEnd--;
            }
            fontName=str.substring(0,fontEnd);
        }
        return new Font(fontName,fontStyle,fontSize);
    }

    private static native void initIDs();

    @Deprecated
    public FontPeer getPeer(){
        return getPeer_NoClientCode();
    }

    // NOTE: This method is called by privileged threads.
    //       We implement this functionality in a package-private method
    //       to insure that it cannot be overridden by client subclasses.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    @SuppressWarnings("deprecation")
    final FontPeer getPeer_NoClientCode(){
        if(peer==null){
            Toolkit tk=Toolkit.getDefaultToolkit();
            this.peer=tk.getFontPeer(name,style);
        }
        return peer;
    }

    public String getPSName(){
        return getFont2D().getPostscriptName();
    }

    private Font2D getFont2D(){
        FontManager fm=FontManagerFactory.getInstance();
        if(fm.usingPerAppContextComposites()&&
                font2DHandle!=null&&
                font2DHandle.font2D instanceof CompositeFont&&
                ((CompositeFont)(font2DHandle.font2D)).isStdComposite()){
            return fm.findFont2D(name,style,
                    FontManager.LOGICAL_FALLBACK);
        }else if(font2DHandle==null){
            font2DHandle=
                    fm.findFont2D(name,style,
                            FontManager.LOGICAL_FALLBACK).handle;
        }
        /** Do not cache the de-referenced font2D. It must be explicitly
         * de-referenced to pick up a valid font in the event that the
         * original one is marked invalid
         */
        return font2DHandle.font2D;
    }

    public String getName(){
        return name;
    }

    public String getFontName(){
        return getFontName(Locale.getDefault());
    }

    public String getFontName(Locale l){
        if(l==null){
            throw new NullPointerException("null locale doesn't mean default");
        }
        return getFont2D().getFontName(l);
    }

    public int getStyle(){
        return style;
    }

    public int getSize(){
        return size;
    }

    public float getSize2D(){
        return pointSize;
    }

    public boolean isPlain(){
        return style==0;
    }

    public boolean hasLayoutAttributes(){
        return hasLayoutAttributes;
    }

    public int hashCode(){
        if(hash==0){
            hash=name.hashCode()^style^size;
            /** It is possible many fonts differ only in transform.
             * So include the transform in the hash calculation.
             * nonIdentityTx is set whenever there is a transform in
             * 'values'. The tests for null are required because it can
             * also be set for other reasons.
             */
            if(nonIdentityTx&&
                    values!=null&&values.getTransform()!=null){
                hash^=values.getTransform().hashCode();
            }
        }
        return hash;
    }

    public boolean equals(Object obj){
        if(obj==this){
            return true;
        }
        if(obj!=null){
            try{
                Font font=(Font)obj;
                if(size==font.size&&
                        style==font.style&&
                        nonIdentityTx==font.nonIdentityTx&&
                        hasLayoutAttributes==font.hasLayoutAttributes&&
                        pointSize==font.pointSize&&
                        name.equals(font.name)){
                    /** 'values' is usually initialized lazily, except when
                     * the font is constructed from a Map, or derived using
                     * a Map or other values. So if only one font has
                     * the field initialized we need to initialize it in
                     * the other instance and compare.
                     */
                    if(values==null){
                        if(font.values==null){
                            return true;
                        }else{
                            return getAttributeValues().equals(font.values);
                        }
                    }else{
                        return values.equals(font.getAttributeValues());
                    }
                }
            }catch(ClassCastException e){
            }
        }
        return false;
    }

    private AttributeValues getAttributeValues(){
        if(values==null){
            AttributeValues valuesTmp=new AttributeValues();
            valuesTmp.setFamily(name);
            valuesTmp.setSize(pointSize); // expects the float value.
            if((style&BOLD)!=0){
                valuesTmp.setWeight(2); // WEIGHT_BOLD
            }
            if((style&ITALIC)!=0){
                valuesTmp.setPosture(.2f); // POSTURE_OBLIQUE
            }
            valuesTmp.defineAll(PRIMARY_MASK); // for streaming compatibility
            values=valuesTmp;
        }
        return values;
    }

    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public String toString(){
        String strStyle;
        if(isBold()){
            strStyle=isItalic()?"bolditalic":"bold";
        }else{
            strStyle=isItalic()?"italic":"plain";
        }
        return getClass().getName()+"[family="+getFamily()+",name="+name+",style="+
                strStyle+",size="+size+"]";
    } // toString()

    public String getFamily(){
        return getFamily_NoClientCode();
    }

    // NOTE: This method is called by privileged threads.
    //       We implement this functionality in a package-private
    //       method to insure that it cannot be overridden by client
    //       subclasses.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    final String getFamily_NoClientCode(){
        return getFamily(Locale.getDefault());
    }

    public String getFamily(Locale l){
        if(l==null){
            throw new NullPointerException("null locale doesn't mean default");
        }
        return getFont2D().getFamilyName(l);
    }

    public boolean isBold(){
        return (style&BOLD)!=0;
    }

    public boolean isItalic(){
        return (style&ITALIC)!=0;
    }

    private void writeObject(ObjectOutputStream s)
            throws ClassNotFoundException,
            IOException{
        if(values!=null){
            synchronized(values){
                // transient
                fRequestedAttributes=values.toSerializableHashtable();
                s.defaultWriteObject();
                fRequestedAttributes=null;
            }
        }else{
            s.defaultWriteObject();
        }
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException,
            IOException{
        s.defaultReadObject();
        if(pointSize==0){
            pointSize=(float)size;
        }
        // Handle fRequestedAttributes.
        // in 1.5, we always streamed out the font values plus
        // TRANSFORM, SUPERSCRIPT, and WIDTH, regardless of whether the
        // values were default or not.  In 1.6 we only stream out
        // defined values.  So, 1.6 streams in from a 1.5 stream,
        // it check each of these values and 'undefines' it if the
        // value is the default.
        if(fRequestedAttributes!=null){
            values=getAttributeValues(); // init
            AttributeValues extras=
                    AttributeValues.fromSerializableHashtable(fRequestedAttributes);
            if(!AttributeValues.is16Hashtable(fRequestedAttributes)){
                extras.unsetDefault(); // if legacy stream, undefine these
            }
            values=getAttributeValues().merge(extras);
            this.nonIdentityTx=values.anyNonDefault(EXTRA_MASK);
            this.hasLayoutAttributes=values.anyNonDefault(LAYOUT_MASK);
            fRequestedAttributes=null; // don't need it any more
        }
    }

    public int getNumGlyphs(){
        return getFont2D().getNumGlyphs();
    }

    public int getMissingGlyphCode(){
        return getFont2D().getMissingGlyphCode();
    }

    public byte getBaselineFor(char c){
        return getFont2D().getBaselineFor(c);
    }

    public Map<TextAttribute,?> getAttributes(){
        return new AttributeMap(getAttributeValues());
    }

    public Attribute[] getAvailableAttributes(){
        // FONT is not supported by Font
        Attribute attributes[]={
                TextAttribute.FAMILY,
                TextAttribute.WEIGHT,
                TextAttribute.WIDTH,
                TextAttribute.POSTURE,
                TextAttribute.SIZE,
                TextAttribute.TRANSFORM,
                TextAttribute.SUPERSCRIPT,
                TextAttribute.CHAR_REPLACEMENT,
                TextAttribute.FOREGROUND,
                TextAttribute.BACKGROUND,
                TextAttribute.UNDERLINE,
                TextAttribute.STRIKETHROUGH,
                TextAttribute.RUN_DIRECTION,
                TextAttribute.BIDI_EMBEDDING,
                TextAttribute.JUSTIFICATION,
                TextAttribute.INPUT_METHOD_HIGHLIGHT,
                TextAttribute.INPUT_METHOD_UNDERLINE,
                TextAttribute.SWAP_COLORS,
                TextAttribute.NUMERIC_SHAPING,
                TextAttribute.KERNING,
                TextAttribute.LIGATURES,
                TextAttribute.TRACKING,
        };
        return attributes;
    }

    public Font deriveFont(int style,float size){
        if(values==null){
            return new Font(name,style,size,createdFont,font2DHandle);
        }
        AttributeValues newValues=getAttributeValues().clone();
        int oldStyle=(this.style!=style)?this.style:-1;
        applyStyle(style,newValues);
        newValues.setSize(size);
        return new Font(newValues,null,oldStyle,createdFont,font2DHandle);
    }

    private static void applyStyle(int style,AttributeValues values){
        // WEIGHT_BOLD, WEIGHT_REGULAR
        values.setWeight((style&BOLD)!=0?2f:1f);
        // POSTURE_OBLIQUE, POSTURE_REGULAR
        values.setPosture((style&ITALIC)!=0?.2f:0f);
    }

    public Font deriveFont(int style,AffineTransform trans){
        AttributeValues newValues=getAttributeValues().clone();
        int oldStyle=(this.style!=style)?this.style:-1;
        applyStyle(style,newValues);
        applyTransform(trans,newValues);
        return new Font(newValues,null,oldStyle,createdFont,font2DHandle);
    }

    private static void applyTransform(AffineTransform trans,AttributeValues values){
        if(trans==null){
            throw new IllegalArgumentException("transform must not be null");
        }
        values.setTransform(trans);
    }

    public Font deriveFont(float size){
        if(values==null){
            return new Font(name,style,size,createdFont,font2DHandle);
        }
        AttributeValues newValues=getAttributeValues().clone();
        newValues.setSize(size);
        return new Font(newValues,null,-1,createdFont,font2DHandle);
    }

    public Font deriveFont(AffineTransform trans){
        AttributeValues newValues=getAttributeValues().clone();
        applyTransform(trans,newValues);
        return new Font(newValues,null,-1,createdFont,font2DHandle);
    }

    public Font deriveFont(int style){
        if(values==null){
            return new Font(name,style,size,createdFont,font2DHandle);
        }
        AttributeValues newValues=getAttributeValues().clone();
        int oldStyle=(this.style!=style)?this.style:-1;
        applyStyle(style,newValues);
        return new Font(newValues,null,oldStyle,createdFont,font2DHandle);
    }

    public Font deriveFont(Map<? extends Attribute,?> attributes){
        if(attributes==null){
            return this;
        }
        AttributeValues newValues=getAttributeValues().clone();
        newValues.merge(attributes,RECOGNIZED_MASK);
        return new Font(newValues,name,style,createdFont,font2DHandle);
    }

    public boolean canDisplay(char c){
        return getFont2D().canDisplay(c);
    }

    public boolean canDisplay(int codePoint){
        if(!Character.isValidCodePoint(codePoint)){
            throw new IllegalArgumentException("invalid code point: "+
                    Integer.toHexString(codePoint));
        }
        return getFont2D().canDisplay(codePoint);
    }

    public int canDisplayUpTo(String str){
        Font2D font2d=getFont2D();
        int len=str.length();
        for(int i=0;i<len;i++){
            char c=str.charAt(i);
            if(font2d.canDisplay(c)){
                continue;
            }
            if(!Character.isHighSurrogate(c)){
                return i;
            }
            if(!font2d.canDisplay(str.codePointAt(i))){
                return i;
            }
            i++;
        }
        return -1;
    }

    public int canDisplayUpTo(char[] text,int start,int limit){
        Font2D font2d=getFont2D();
        for(int i=start;i<limit;i++){
            char c=text[i];
            if(font2d.canDisplay(c)){
                continue;
            }
            if(!Character.isHighSurrogate(c)){
                return i;
            }
            if(!font2d.canDisplay(Character.codePointAt(text,i,limit))){
                return i;
            }
            i++;
        }
        return -1;
    }

    public int canDisplayUpTo(CharacterIterator iter,int start,int limit){
        Font2D font2d=getFont2D();
        char c=iter.setIndex(start);
        for(int i=start;i<limit;i++,c=iter.next()){
            if(font2d.canDisplay(c)){
                continue;
            }
            if(!Character.isHighSurrogate(c)){
                return i;
            }
            char c2=iter.next();
            // c2 could be CharacterIterator.DONE which is not a low surrogate.
            if(!Character.isLowSurrogate(c2)){
                return i;
            }
            if(!font2d.canDisplay(Character.toCodePoint(c,c2))){
                return i;
            }
            i++;
        }
        return -1;
    }

    public float getItalicAngle(){
        return getItalicAngle(null);
    }

    private float getItalicAngle(FontRenderContext frc){
        Object aa, fm;
        if(frc==null){
            aa=RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
            fm=RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
        }else{
            aa=frc.getAntiAliasingHint();
            fm=frc.getFractionalMetricsHint();
        }
        return getFont2D().getItalicAngle(this,identityTx,aa,fm);
    }

    public boolean hasUniformLineMetrics(){
        return false;   // REMIND always safe, but prevents caller optimize
    }

    public LineMetrics getLineMetrics(String str,FontRenderContext frc){
        FontLineMetrics flm=defaultLineMetrics(frc);
        flm.numchars=str.length();
        return flm;
    }

    private FontLineMetrics defaultLineMetrics(FontRenderContext frc){
        FontLineMetrics flm=null;
        if(flmref==null
                ||(flm=flmref.get())==null
                ||!flm.frc.equals(frc)){
            /** The device transform in the frc is not used in obtaining line
             * metrics, although it probably should be: REMIND find why not?
             * The font transform is used but its applied in getFontMetrics, so
             * just pass identity here
             */
            float[] metrics=new float[8];
            getFont2D().getFontMetrics(this,identityTx,
                    frc.getAntiAliasingHint(),
                    frc.getFractionalMetricsHint(),
                    metrics);
            float ascent=metrics[0];
            float descent=metrics[1];
            float leading=metrics[2];
            float ssOffset=0;
            if(values!=null&&values.getSuperscript()!=0){
                ssOffset=(float)getTransform().getTranslateY();
                ascent-=ssOffset;
                descent+=ssOffset;
            }
            float height=ascent+descent+leading;
            int baselineIndex=0; // need real index, assumes roman for everything
            // need real baselines eventually
            float[] baselineOffsets={0,(descent/2f-ascent)/2f,-ascent};
            float strikethroughOffset=metrics[4];
            float strikethroughThickness=metrics[5];
            float underlineOffset=metrics[6];
            float underlineThickness=metrics[7];
            float italicAngle=getItalicAngle(frc);
            if(isTransformed()){
                AffineTransform ctx=values.getCharTransform(); // extract rotation
                if(ctx!=null){
                    Point2D.Float pt=new Point2D.Float();
                    pt.setLocation(0,strikethroughOffset);
                    ctx.deltaTransform(pt,pt);
                    strikethroughOffset=pt.y;
                    pt.setLocation(0,strikethroughThickness);
                    ctx.deltaTransform(pt,pt);
                    strikethroughThickness=pt.y;
                    pt.setLocation(0,underlineOffset);
                    ctx.deltaTransform(pt,pt);
                    underlineOffset=pt.y;
                    pt.setLocation(0,underlineThickness);
                    ctx.deltaTransform(pt,pt);
                    underlineThickness=pt.y;
                }
            }
            strikethroughOffset+=ssOffset;
            underlineOffset+=ssOffset;
            CoreMetrics cm=new CoreMetrics(ascent,descent,leading,height,
                    baselineIndex,baselineOffsets,
                    strikethroughOffset,strikethroughThickness,
                    underlineOffset,underlineThickness,
                    ssOffset,italicAngle);
            flm=new FontLineMetrics(0,cm,frc);
            flmref=new SoftReference<FontLineMetrics>(flm);
        }
        return (FontLineMetrics)flm.clone();
    }

    public AffineTransform getTransform(){
        /** The most common case is the identity transform.  Most callers
         * should call isTransformed() first, to decide if they need to
         * get the transform, but some may not.  Here we check to see
         * if we have a nonidentity transform, and only do the work to
         * fetch and/or compute it if so, otherwise we return a new
         * identity transform.
         *
         * Note that the transform is _not_ necessarily the same as
         * the transform passed in as an Attribute in a Map, as the
         * transform returned will also reflect the effects of WIDTH and
         * SUPERSCRIPT attributes.  Clients who want the actual transform
         * need to call getRequestedAttributes.
         */
        if(nonIdentityTx){
            AttributeValues values=getAttributeValues();
            AffineTransform at=values.isNonDefault(ETRANSFORM)
                    ?new AffineTransform(values.getTransform())
                    :new AffineTransform();
            if(values.getSuperscript()!=0){
                // can't get ascent and descent here, recursive call to this fn,
                // so use pointsize
                // let users combine super- and sub-scripting
                int superscript=values.getSuperscript();
                double trans=0;
                int n=0;
                boolean up=superscript>0;
                int sign=up?-1:1;
                int ss=up?superscript:-superscript;
                while((ss&7)>n){
                    int newn=ss&7;
                    trans+=sign*(ssinfo[newn]-ssinfo[n]);
                    ss>>=3;
                    sign=-sign;
                    n=newn;
                }
                trans*=pointSize;
                double scale=Math.pow(2./3.,n);
                at.preConcatenate(AffineTransform.getTranslateInstance(0,trans));
                at.scale(scale,scale);
                // note on placement and italics
                // We preconcatenate the transform because we don't want to translate along
                // the italic angle, but purely perpendicular to the baseline.  While this
                // looks ok for superscripts, it can lead subscripts to stack on each other
                // and bring the following text too close.  The way we deal with potential
                // collisions that can occur in the case of italics is by adjusting the
                // horizontal spacing of the adjacent glyphvectors.  Examine the italic
                // angle of both vectors, if one is non-zero, compute the minimum ascent
                // and descent, and then the x position at each for each vector along its
                // italic angle starting from its (offset) baseline.  Compute the difference
                // between the x positions and use the maximum difference to adjust the
                // position of the right gv.
            }
            if(values.isNonDefault(EWIDTH)){
                at.scale(values.getWidth(),1f);
            }
            return at;
        }
        return new AffineTransform();
    }

    public boolean isTransformed(){
        return nonIdentityTx;
    }

    public LineMetrics getLineMetrics(String str,
                                      int beginIndex,int limit,
                                      FontRenderContext frc){
        FontLineMetrics flm=defaultLineMetrics(frc);
        int numChars=limit-beginIndex;
        flm.numchars=(numChars<0)?0:numChars;
        return flm;
    }

    public LineMetrics getLineMetrics(char[] chars,
                                      int beginIndex,int limit,
                                      FontRenderContext frc){
        FontLineMetrics flm=defaultLineMetrics(frc);
        int numChars=limit-beginIndex;
        flm.numchars=(numChars<0)?0:numChars;
        return flm;
    }

    public LineMetrics getLineMetrics(CharacterIterator ci,
                                      int beginIndex,int limit,
                                      FontRenderContext frc){
        FontLineMetrics flm=defaultLineMetrics(frc);
        int numChars=limit-beginIndex;
        flm.numchars=(numChars<0)?0:numChars;
        return flm;
    }

    public Rectangle2D getStringBounds(String str,
                                       int beginIndex,int limit,
                                       FontRenderContext frc){
        String substr=str.substring(beginIndex,limit);
        return getStringBounds(substr,frc);
    }

    public Rectangle2D getStringBounds(String str,FontRenderContext frc){
        char[] array=str.toCharArray();
        return getStringBounds(array,0,array.length,frc);
    }

    public Rectangle2D getStringBounds(char[] chars,
                                       int beginIndex,int limit,
                                       FontRenderContext frc){
        if(beginIndex<0){
            throw new IndexOutOfBoundsException("beginIndex: "+beginIndex);
        }
        if(limit>chars.length){
            throw new IndexOutOfBoundsException("limit: "+limit);
        }
        if(beginIndex>limit){
            throw new IndexOutOfBoundsException("range length: "+
                    (limit-beginIndex));
        }
        // this code should be in textlayout
        // quick check for simple text, assume GV ok to use if simple
        boolean simple=values==null||
                (values.getKerning()==0&&values.getLigatures()==0&&
                        values.getBaselineTransform()==null);
        if(simple){
            simple=!FontUtilities.isComplexText(chars,beginIndex,limit);
        }
        if(simple){
            GlyphVector gv=new StandardGlyphVector(this,chars,beginIndex,
                    limit-beginIndex,frc);
            return gv.getLogicalBounds();
        }else{
            // need char array constructor on textlayout
            String str=new String(chars,beginIndex,limit-beginIndex);
            TextLayout tl=new TextLayout(str,this,frc);
            return new Rectangle2D.Float(0,-tl.getAscent(),tl.getAdvance(),
                    tl.getAscent()+tl.getDescent()+
                            tl.getLeading());
        }
    }

    public Rectangle2D getStringBounds(CharacterIterator ci,
                                       int beginIndex,int limit,
                                       FontRenderContext frc){
        int start=ci.getBeginIndex();
        int end=ci.getEndIndex();
        if(beginIndex<start){
            throw new IndexOutOfBoundsException("beginIndex: "+beginIndex);
        }
        if(limit>end){
            throw new IndexOutOfBoundsException("limit: "+limit);
        }
        if(beginIndex>limit){
            throw new IndexOutOfBoundsException("range length: "+
                    (limit-beginIndex));
        }
        char[] arr=new char[limit-beginIndex];
        ci.setIndex(beginIndex);
        for(int idx=0;idx<arr.length;idx++){
            arr[idx]=ci.current();
            ci.next();
        }
        return getStringBounds(arr,0,arr.length,frc);
    }

    public Rectangle2D getMaxCharBounds(FontRenderContext frc){
        float[] metrics=new float[4];
        getFont2D().getFontMetrics(this,frc,metrics);
        return new Rectangle2D.Float(0,-metrics[0],
                metrics[3],
                metrics[0]+metrics[1]+metrics[2]);
    }

    public GlyphVector createGlyphVector(FontRenderContext frc,String str){
        return (GlyphVector)new StandardGlyphVector(this,str,frc);
    }

    public GlyphVector createGlyphVector(FontRenderContext frc,char[] chars){
        return (GlyphVector)new StandardGlyphVector(this,chars,frc);
    }

    public GlyphVector createGlyphVector(FontRenderContext frc,
                                         CharacterIterator ci){
        return (GlyphVector)new StandardGlyphVector(this,ci,frc);
    }

    public GlyphVector createGlyphVector(FontRenderContext frc,
                                         int[] glyphCodes){
        return (GlyphVector)new StandardGlyphVector(this,glyphCodes,frc);
    }

    public GlyphVector layoutGlyphVector(FontRenderContext frc,
                                         char[] text,
                                         int start,
                                         int limit,
                                         int flags){
        GlyphLayout gl=GlyphLayout.get(null); // !!! no custom layout engines
        StandardGlyphVector gv=gl.layout(this,frc,text,
                start,limit-start,flags,null);
        GlyphLayout.done(gl);
        return gv;
    }

    private static class FontAccessImpl extends FontAccess{
        public Font2D getFont2D(Font font){
            return font.getFont2D();
        }

        public void setFont2D(Font font,Font2DHandle handle){
            font.font2DHandle=handle;
        }

        public void setCreatedFont(Font font){
            font.createdFont=true;
        }

        public boolean isCreatedFont(Font font){
            return font.createdFont;
        }
    }
}
