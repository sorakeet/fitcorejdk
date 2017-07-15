/**
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

import sun.awt.image.*;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

public class BufferedImage extends Image
        implements WritableRenderedImage, Transparency{
    public static final int TYPE_CUSTOM=0;
    public static final int TYPE_INT_RGB=1;
    public static final int TYPE_INT_ARGB=2;
    public static final int TYPE_INT_ARGB_PRE=3;
    public static final int TYPE_INT_BGR=4;
    public static final int TYPE_3BYTE_BGR=5;
    public static final int TYPE_4BYTE_ABGR=6;
    public static final int TYPE_4BYTE_ABGR_PRE=7;
    public static final int TYPE_USHORT_565_RGB=8;
    public static final int TYPE_USHORT_555_RGB=9;
    public static final int TYPE_BYTE_GRAY=10;
    public static final int TYPE_USHORT_GRAY=11;
    public static final int TYPE_BYTE_BINARY=12;
    public static final int TYPE_BYTE_INDEXED=13;
    private static final int DCM_RED_MASK=0x00ff0000;
    private static final int DCM_GREEN_MASK=0x0000ff00;
    private static final int DCM_BLUE_MASK=0x000000ff;
    private static final int DCM_ALPHA_MASK=0xff000000;
    private static final int DCM_565_RED_MASK=0xf800;
    private static final int DCM_565_GRN_MASK=0x07E0;
    private static final int DCM_565_BLU_MASK=0x001F;
    private static final int DCM_555_RED_MASK=0x7C00;
    private static final int DCM_555_GRN_MASK=0x03E0;
    private static final int DCM_555_BLU_MASK=0x001F;
    private static final int DCM_BGR_RED_MASK=0x0000ff;
    private static final int DCM_BGR_GRN_MASK=0x00ff00;
    private static final int DCM_BGR_BLU_MASK=0xff0000;

    static{
        ColorModel.loadLibraries();
        initIDs();
    }

    private final WritableRaster raster;
    private int imageType=TYPE_CUSTOM;
    private ColorModel colorModel;
    private OffScreenImageSource osis;
    private Hashtable<String,Object> properties;

    public BufferedImage(int width,
                         int height,
                         int imageType){
        switch(imageType){
            case TYPE_INT_RGB:{
                colorModel=new DirectColorModel(24,
                        0x00ff0000,   // Red
                        0x0000ff00,   // Green
                        0x000000ff,   // Blue
                        0x0           // Alpha
                );
                raster=colorModel.createCompatibleWritableRaster(width,
                        height);
            }
            break;
            case TYPE_INT_ARGB:{
                colorModel=ColorModel.getRGBdefault();
                raster=colorModel.createCompatibleWritableRaster(width,
                        height);
            }
            break;
            case TYPE_INT_ARGB_PRE:{
                colorModel=new
                        DirectColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_sRGB),
                        32,
                        0x00ff0000,// Red
                        0x0000ff00,// Green
                        0x000000ff,// Blue
                        0xff000000,// Alpha
                        true,       // Alpha Premultiplied
                        DataBuffer.TYPE_INT
                );
                raster=colorModel.createCompatibleWritableRaster(width,
                        height);
            }
            break;
            case TYPE_INT_BGR:{
                colorModel=new DirectColorModel(24,
                        0x000000ff,   // Red
                        0x0000ff00,   // Green
                        0x00ff0000    // Blue
                );
                raster=colorModel.createCompatibleWritableRaster(width,
                        height);
            }
            break;
            case TYPE_3BYTE_BGR:{
                ColorSpace cs=ColorSpace.getInstance(ColorSpace.CS_sRGB);
                int[] nBits={8,8,8};
                int[] bOffs={2,1,0};
                colorModel=new ComponentColorModel(cs,nBits,false,false,
                        Transparency.OPAQUE,
                        DataBuffer.TYPE_BYTE);
                raster=Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                        width,height,
                        width*3,3,
                        bOffs,null);
            }
            break;
            case TYPE_4BYTE_ABGR:{
                ColorSpace cs=ColorSpace.getInstance(ColorSpace.CS_sRGB);
                int[] nBits={8,8,8,8};
                int[] bOffs={3,2,1,0};
                colorModel=new ComponentColorModel(cs,nBits,true,false,
                        Transparency.TRANSLUCENT,
                        DataBuffer.TYPE_BYTE);
                raster=Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                        width,height,
                        width*4,4,
                        bOffs,null);
            }
            break;
            case TYPE_4BYTE_ABGR_PRE:{
                ColorSpace cs=ColorSpace.getInstance(ColorSpace.CS_sRGB);
                int[] nBits={8,8,8,8};
                int[] bOffs={3,2,1,0};
                colorModel=new ComponentColorModel(cs,nBits,true,true,
                        Transparency.TRANSLUCENT,
                        DataBuffer.TYPE_BYTE);
                raster=Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                        width,height,
                        width*4,4,
                        bOffs,null);
            }
            break;
            case TYPE_BYTE_GRAY:{
                ColorSpace cs=ColorSpace.getInstance(ColorSpace.CS_GRAY);
                int[] nBits={8};
                colorModel=new ComponentColorModel(cs,nBits,false,true,
                        Transparency.OPAQUE,
                        DataBuffer.TYPE_BYTE);
                raster=colorModel.createCompatibleWritableRaster(width,
                        height);
            }
            break;
            case TYPE_USHORT_GRAY:{
                ColorSpace cs=ColorSpace.getInstance(ColorSpace.CS_GRAY);
                int[] nBits={16};
                colorModel=new ComponentColorModel(cs,nBits,false,true,
                        Transparency.OPAQUE,
                        DataBuffer.TYPE_USHORT);
                raster=colorModel.createCompatibleWritableRaster(width,
                        height);
            }
            break;
            case TYPE_BYTE_BINARY:{
                byte[] arr={(byte)0,(byte)0xff};
                colorModel=new IndexColorModel(1,2,arr,arr,arr);
                raster=Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                        width,height,1,1,null);
            }
            break;
            case TYPE_BYTE_INDEXED:{
                // Create a 6x6x6 color cube
                int[] cmap=new int[256];
                int i=0;
                for(int r=0;r<256;r+=51){
                    for(int g=0;g<256;g+=51){
                        for(int b=0;b<256;b+=51){
                            cmap[i++]=(r<<16)|(g<<8)|b;
                        }
                    }
                }
                // And populate the rest of the cmap with gray values
                int grayIncr=256/(256-i);
                // The gray ramp will be between 18 and 252
                int gray=grayIncr*3;
                for(;i<256;i++){
                    cmap[i]=(gray<<16)|(gray<<8)|gray;
                    gray+=grayIncr;
                }
                colorModel=new IndexColorModel(8,256,cmap,0,false,-1,
                        DataBuffer.TYPE_BYTE);
                raster=Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                        width,height,1,null);
            }
            break;
            case TYPE_USHORT_565_RGB:{
                colorModel=new DirectColorModel(16,
                        DCM_565_RED_MASK,
                        DCM_565_GRN_MASK,
                        DCM_565_BLU_MASK
                );
                raster=colorModel.createCompatibleWritableRaster(width,
                        height);
            }
            break;
            case TYPE_USHORT_555_RGB:{
                colorModel=new DirectColorModel(15,
                        DCM_555_RED_MASK,
                        DCM_555_GRN_MASK,
                        DCM_555_BLU_MASK
                );
                raster=colorModel.createCompatibleWritableRaster(width,
                        height);
            }
            break;
            default:
                throw new IllegalArgumentException("Unknown image type "+
                        imageType);
        }
        this.imageType=imageType;
    }

    public BufferedImage(int width,
                         int height,
                         int imageType,
                         IndexColorModel cm){
        if(cm.hasAlpha()&&cm.isAlphaPremultiplied()){
            throw new IllegalArgumentException("This image types do not have "+
                    "premultiplied alpha.");
        }
        switch(imageType){
            case TYPE_BYTE_BINARY:
                int bits; // Will be set below
                int mapSize=cm.getMapSize();
                if(mapSize<=2){
                    bits=1;
                }else if(mapSize<=4){
                    bits=2;
                }else if(mapSize<=16){
                    bits=4;
                }else{
                    throw new IllegalArgumentException
                            ("Color map for TYPE_BYTE_BINARY "+
                                    "must have no more than 16 entries");
                }
                raster=Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                        width,height,1,bits,null);
                break;
            case TYPE_BYTE_INDEXED:
                raster=Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                        width,height,1,null);
                break;
            default:
                throw new IllegalArgumentException("Invalid image type ("+
                        imageType+").  Image type must"+
                        " be either TYPE_BYTE_BINARY or "+
                        " TYPE_BYTE_INDEXED");
        }
        if(!cm.isCompatibleRaster(raster)){
            throw new IllegalArgumentException("Incompatible image type and IndexColorModel");
        }
        colorModel=cm;
        this.imageType=imageType;
    }

    public BufferedImage(ColorModel cm,
                         WritableRaster raster,
                         boolean isRasterPremultiplied,
                         Hashtable<?,?> properties){
        if(!cm.isCompatibleRaster(raster)){
            throw new
                    IllegalArgumentException("Raster "+raster+
                    " is incompatible with ColorModel "+
                    cm);
        }
        if((raster.minX!=0)||(raster.minY!=0)){
            throw new
                    IllegalArgumentException("Raster "+raster+
                    " has minX or minY not equal to zero: "
                    +raster.minX+" "+raster.minY);
        }
        colorModel=cm;
        this.raster=raster;
        if(properties!=null&&!properties.isEmpty()){
            this.properties=new Hashtable<>();
            for(final Object key : properties.keySet()){
                if(key instanceof String){
                    this.properties.put((String)key,properties.get(key));
                }
            }
        }
        int numBands=raster.getNumBands();
        boolean isAlphaPre=cm.isAlphaPremultiplied();
        final boolean isStandard=isStandard(cm,raster);
        ColorSpace cs;
        // Force the raster data alpha state to match the premultiplied
        // state in the color model
        coerceData(isRasterPremultiplied);
        SampleModel sm=raster.getSampleModel();
        cs=cm.getColorSpace();
        int csType=cs.getType();
        if(csType!=ColorSpace.TYPE_RGB){
            if(csType==ColorSpace.TYPE_GRAY&&
                    isStandard&&
                    cm instanceof ComponentColorModel){
                // Check if this might be a child raster (fix for bug 4240596)
                if(sm instanceof ComponentSampleModel&&
                        ((ComponentSampleModel)sm).getPixelStride()!=numBands){
                    imageType=TYPE_CUSTOM;
                }else if(raster instanceof ByteComponentRaster&&
                        raster.getNumBands()==1&&
                        cm.getComponentSize(0)==8&&
                        ((ByteComponentRaster)raster).getPixelStride()==1){
                    imageType=TYPE_BYTE_GRAY;
                }else if(raster instanceof ShortComponentRaster&&
                        raster.getNumBands()==1&&
                        cm.getComponentSize(0)==16&&
                        ((ShortComponentRaster)raster).getPixelStride()==1){
                    imageType=TYPE_USHORT_GRAY;
                }
            }else{
                imageType=TYPE_CUSTOM;
            }
            return;
        }
        if((raster instanceof IntegerComponentRaster)&&
                (numBands==3||numBands==4)){
            IntegerComponentRaster iraster=
                    (IntegerComponentRaster)raster;
            // Check if the raster params and the color model
            // are correct
            int pixSize=cm.getPixelSize();
            if(iraster.getPixelStride()==1&&
                    isStandard&&
                    cm instanceof DirectColorModel&&
                    (pixSize==32||pixSize==24)){
                // Now check on the DirectColorModel params
                DirectColorModel dcm=(DirectColorModel)cm;
                int rmask=dcm.getRedMask();
                int gmask=dcm.getGreenMask();
                int bmask=dcm.getBlueMask();
                if(rmask==DCM_RED_MASK&&gmask==DCM_GREEN_MASK&&
                        bmask==DCM_BLUE_MASK){
                    if(dcm.getAlphaMask()==DCM_ALPHA_MASK){
                        imageType=(isAlphaPre
                                ?TYPE_INT_ARGB_PRE
                                :TYPE_INT_ARGB);
                    }else{
                        // No Alpha
                        if(!dcm.hasAlpha()){
                            imageType=TYPE_INT_RGB;
                        }
                    }
                }   // if (dcm.getRedMask() == DCM_RED_MASK &&
                else if(rmask==DCM_BGR_RED_MASK&&gmask==DCM_BGR_GRN_MASK
                        &&bmask==DCM_BGR_BLU_MASK){
                    if(!dcm.hasAlpha()){
                        imageType=TYPE_INT_BGR;
                    }
                }  // if (rmask == DCM_BGR_RED_MASK &&
            }   // if (iraster.getPixelStride() == 1
        }   // ((raster instanceof IntegerComponentRaster) &&
        else if((cm instanceof IndexColorModel)&&(numBands==1)&&
                isStandard&&
                (!cm.hasAlpha()||!isAlphaPre)){
            IndexColorModel icm=(IndexColorModel)cm;
            int pixSize=icm.getPixelSize();
            if(raster instanceof BytePackedRaster){
                imageType=TYPE_BYTE_BINARY;
            }   // if (raster instanceof BytePackedRaster)
            else if(raster instanceof ByteComponentRaster){
                ByteComponentRaster braster=(ByteComponentRaster)raster;
                if(braster.getPixelStride()==1&&pixSize<=8){
                    imageType=TYPE_BYTE_INDEXED;
                }
            }
        }   // else if (cm instanceof IndexColorModel) && (numBands == 1))
        else if((raster instanceof ShortComponentRaster)
                &&(cm instanceof DirectColorModel)
                &&isStandard
                &&(numBands==3)
                &&!cm.hasAlpha()){
            DirectColorModel dcm=(DirectColorModel)cm;
            if(dcm.getRedMask()==DCM_565_RED_MASK){
                if(dcm.getGreenMask()==DCM_565_GRN_MASK&&
                        dcm.getBlueMask()==DCM_565_BLU_MASK){
                    imageType=TYPE_USHORT_565_RGB;
                }
            }else if(dcm.getRedMask()==DCM_555_RED_MASK){
                if(dcm.getGreenMask()==DCM_555_GRN_MASK&&
                        dcm.getBlueMask()==DCM_555_BLU_MASK){
                    imageType=TYPE_USHORT_555_RGB;
                }
            }
        }   // else if ((cm instanceof IndexColorModel) && (numBands == 1))
        else if((raster instanceof ByteComponentRaster)
                &&(cm instanceof ComponentColorModel)
                &&isStandard
                &&(raster.getSampleModel() instanceof PixelInterleavedSampleModel)
                &&(numBands==3||numBands==4)){
            ComponentColorModel ccm=(ComponentColorModel)cm;
            PixelInterleavedSampleModel csm=
                    (PixelInterleavedSampleModel)raster.getSampleModel();
            ByteComponentRaster braster=(ByteComponentRaster)raster;
            int[] offs=csm.getBandOffsets();
            if(ccm.getNumComponents()!=numBands){
                throw new RasterFormatException("Number of components in "+
                        "ColorModel ("+
                        ccm.getNumComponents()+
                        ") does not match # in "+
                        " Raster ("+numBands+")");
            }
            int[] nBits=ccm.getComponentSize();
            boolean is8bit=true;
            for(int i=0;i<numBands;i++){
                if(nBits[i]!=8){
                    is8bit=false;
                    break;
                }
            }
            if(is8bit&&
                    braster.getPixelStride()==numBands&&
                    offs[0]==numBands-1&&
                    offs[1]==numBands-2&&
                    offs[2]==numBands-3){
                if(numBands==3&&!ccm.hasAlpha()){
                    imageType=TYPE_3BYTE_BGR;
                }else if(offs[3]==0&&ccm.hasAlpha()){
                    imageType=(isAlphaPre
                            ?TYPE_4BYTE_ABGR_PRE
                            :TYPE_4BYTE_ABGR);
                }
            }
        }   // else if ((raster instanceof ByteComponentRaster) &&
    }

    private static boolean isStandard(ColorModel cm,WritableRaster wr){
        final Class<? extends ColorModel> cmClass=cm.getClass();
        final Class<? extends WritableRaster> wrClass=wr.getClass();
        final Class<? extends SampleModel> smClass=wr.getSampleModel().getClass();
        final PrivilegedAction<Boolean> checkClassLoadersAction=
                new PrivilegedAction<Boolean>(){
                    @Override
                    public Boolean run(){
                        final ClassLoader std=System.class.getClassLoader();
                        return (cmClass.getClassLoader()==std)&&
                                (smClass.getClassLoader()==std)&&
                                (wrClass.getClassLoader()==std);
                    }
                };
        return AccessController.doPrivileged(checkClassLoadersAction);
    }

    public void coerceData(boolean isAlphaPremultiplied){
        if(colorModel.hasAlpha()&&
                colorModel.isAlphaPremultiplied()!=isAlphaPremultiplied){
            // Make the color model do the conversion
            colorModel=colorModel.coerceData(raster,isAlphaPremultiplied);
        }
    }

    static private native void initIDs();

    public int getType(){
        return imageType;
    }

    public WritableRaster getRaster(){
        return raster;
    }

    public WritableRaster getAlphaRaster(){
        return colorModel.getAlphaRaster(raster);
    }

    public int getRGB(int x,int y){
        return colorModel.getRGB(raster.getDataElements(x,y,null));
    }

    public int[] getRGB(int startX,int startY,int w,int h,
                        int[] rgbArray,int offset,int scansize){
        int yoff=offset;
        int off;
        Object data;
        int nbands=raster.getNumBands();
        int dataType=raster.getDataBuffer().getDataType();
        switch(dataType){
            case DataBuffer.TYPE_BYTE:
                data=new byte[nbands];
                break;
            case DataBuffer.TYPE_USHORT:
                data=new short[nbands];
                break;
            case DataBuffer.TYPE_INT:
                data=new int[nbands];
                break;
            case DataBuffer.TYPE_FLOAT:
                data=new float[nbands];
                break;
            case DataBuffer.TYPE_DOUBLE:
                data=new double[nbands];
                break;
            default:
                throw new IllegalArgumentException("Unknown data buffer type: "+
                        dataType);
        }
        if(rgbArray==null){
            rgbArray=new int[offset+h*scansize];
        }
        for(int y=startY;y<startY+h;y++,yoff+=scansize){
            off=yoff;
            for(int x=startX;x<startX+w;x++){
                rgbArray[off++]=colorModel.getRGB(raster.getDataElements(x,
                        y,
                        data));
            }
        }
        return rgbArray;
    }

    public synchronized void setRGB(int x,int y,int rgb){
        raster.setDataElements(x,y,colorModel.getDataElements(rgb,null));
    }

    public void setRGB(int startX,int startY,int w,int h,
                       int[] rgbArray,int offset,int scansize){
        int yoff=offset;
        int off;
        Object pixel=null;
        for(int y=startY;y<startY+h;y++,yoff+=scansize){
            off=yoff;
            for(int x=startX;x<startX+w;x++){
                pixel=colorModel.getDataElements(rgbArray[off++],pixel);
                raster.setDataElements(x,y,pixel);
            }
        }
    }

    public int getWidth(ImageObserver observer){
        return raster.getWidth();
    }

    public int getHeight(ImageObserver observer){
        return raster.getHeight();
    }

    public ImageProducer getSource(){
        if(osis==null){
            if(properties==null){
                properties=new Hashtable();
            }
            osis=new OffScreenImageSource(this,properties);
        }
        return osis;
    }

    public Graphics getGraphics(){
        return createGraphics();
    }

    public Object getProperty(String name,ImageObserver observer){
        return getProperty(name);
    }

    public Graphics2D createGraphics(){
        GraphicsEnvironment env=
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        return env.createGraphics(this);
    }

    public BufferedImage getSubimage(int x,int y,int w,int h){
        return new BufferedImage(colorModel,
                raster.createWritableChild(x,y,w,h,
                        0,0,null),
                colorModel.isAlphaPremultiplied(),
                properties);
    }

    public boolean isAlphaPremultiplied(){
        return colorModel.isAlphaPremultiplied();
    }

    public String toString(){
        return "BufferedImage@"+Integer.toHexString(hashCode())
                +": type = "+imageType
                +" "+colorModel+" "+raster;
    }

    public Vector<RenderedImage> getSources(){
        return null;
    }

    public Object getProperty(String name){
        if(name==null){
            throw new NullPointerException("null property name is not allowed");
        }
        if(properties==null){
            return Image.UndefinedProperty;
        }
        Object o=properties.get(name);
        if(o==null){
            o=Image.UndefinedProperty;
        }
        return o;
    }

    public String[] getPropertyNames(){
        if(properties==null||properties.isEmpty()){
            return null;
        }
        final Set<String> keys=properties.keySet();
        return keys.toArray(new String[keys.size()]);
    }

    public ColorModel getColorModel(){
        return colorModel;
    }

    public SampleModel getSampleModel(){
        return raster.getSampleModel();
    }

    public int getWidth(){
        return raster.getWidth();
    }

    public int getHeight(){
        return raster.getHeight();
    }

    public int getMinX(){
        return raster.getMinX();
    }

    public int getMinY(){
        return raster.getMinY();
    }

    public int getNumXTiles(){
        return 1;
    }

    public int getNumYTiles(){
        return 1;
    }

    public int getMinTileX(){
        return 0;
    }

    public int getMinTileY(){
        return 0;
    }

    public int getTileWidth(){
        return raster.getWidth();
    }

    public int getTileHeight(){
        return raster.getHeight();
    }

    public int getTileGridXOffset(){
        return raster.getSampleModelTranslateX();
    }

    public int getTileGridYOffset(){
        return raster.getSampleModelTranslateY();
    }

    public Raster getTile(int tileX,int tileY){
        if(tileX==0&&tileY==0){
            return raster;
        }
        throw new ArrayIndexOutOfBoundsException("BufferedImages only have"+
                " one tile with index 0,0");
    }

    public Raster getData(){
        // REMIND : this allocates a whole new tile if raster is a
        // subtile.  (It only copies in the requested area)
        // We should do something smarter.
        int width=raster.getWidth();
        int height=raster.getHeight();
        int startX=raster.getMinX();
        int startY=raster.getMinY();
        WritableRaster wr=
                Raster.createWritableRaster(raster.getSampleModel(),
                        new Point(raster.getSampleModelTranslateX(),
                                raster.getSampleModelTranslateY()));
        Object tdata=null;
        for(int i=startY;i<startY+height;i++){
            tdata=raster.getDataElements(startX,i,width,1,tdata);
            wr.setDataElements(startX,i,width,1,tdata);
        }
        return wr;
    }

    public Raster getData(Rectangle rect){
        SampleModel sm=raster.getSampleModel();
        SampleModel nsm=sm.createCompatibleSampleModel(rect.width,
                rect.height);
        WritableRaster wr=Raster.createWritableRaster(nsm,
                rect.getLocation());
        int width=rect.width;
        int height=rect.height;
        int startX=rect.x;
        int startY=rect.y;
        Object tdata=null;
        for(int i=startY;i<startY+height;i++){
            tdata=raster.getDataElements(startX,i,width,1,tdata);
            wr.setDataElements(startX,i,width,1,tdata);
        }
        return wr;
    }

    public WritableRaster copyData(WritableRaster outRaster){
        if(outRaster==null){
            return (WritableRaster)getData();
        }
        int width=outRaster.getWidth();
        int height=outRaster.getHeight();
        int startX=outRaster.getMinX();
        int startY=outRaster.getMinY();
        Object tdata=null;
        for(int i=startY;i<startY+height;i++){
            tdata=raster.getDataElements(startX,i,width,1,tdata);
            outRaster.setDataElements(startX,i,width,1,tdata);
        }
        return outRaster;
    }

    public void setData(Raster r){
        int width=r.getWidth();
        int height=r.getHeight();
        int startX=r.getMinX();
        int startY=r.getMinY();
        int[] tdata=null;
        // Clip to the current Raster
        Rectangle rclip=new Rectangle(startX,startY,width,height);
        Rectangle bclip=new Rectangle(0,0,raster.width,raster.height);
        Rectangle intersect=rclip.intersection(bclip);
        if(intersect.isEmpty()){
            return;
        }
        width=intersect.width;
        height=intersect.height;
        startX=intersect.x;
        startY=intersect.y;
        // remind use get/setDataElements for speed if Rasters are
        // compatible
        for(int i=startY;i<startY+height;i++){
            tdata=r.getPixels(startX,i,width,1,tdata);
            raster.setPixels(startX,i,width,1,tdata);
        }
    }

    public void addTileObserver(TileObserver to){
    }

    public void removeTileObserver(TileObserver to){
    }

    public WritableRaster getWritableTile(int tileX,int tileY){
        return raster;
    }

    public void releaseWritableTile(int tileX,int tileY){
    }

    public boolean isTileWritable(int tileX,int tileY){
        if(tileX==0&&tileY==0){
            return true;
        }
        throw new IllegalArgumentException("Only 1 tile in image");
    }

    public Point[] getWritableTileIndices(){
        Point[] p=new Point[1];
        p[0]=new Point(0,0);
        return p;
    }

    public boolean hasTileWriters(){
        return true;
    }

    public int getTransparency(){
        return colorModel.getTransparency();
    }
}
