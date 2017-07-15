/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.nimbus;

import sun.reflect.misc.MethodUtil;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.image.VolatileImage;
import java.awt.print.PrinterGraphics;
import java.lang.reflect.Method;

public abstract class AbstractRegionPainter implements Painter<JComponent>{
    private PaintContext ctx;
    private float f;
    private float leftWidth;
    private float topHeight;
    private float centerWidth;
    private float centerHeight;
    private float rightWidth;
    private float bottomHeight;
    private float leftScale;
    private float topScale;
    private float centerHScale;
    private float centerVScale;
    private float rightScale;
    private float bottomScale;

    protected AbstractRegionPainter(){
    }

    @Override
    public final void paint(Graphics2D g,JComponent c,int w,int h){
        //don't render if the width/height are too small
        if(w<=0||h<=0) return;
        Object[] extendedCacheKeys=getExtendedCacheKeys(c);
        ctx=getPaintContext();
        PaintContext.CacheMode cacheMode=ctx==null?PaintContext.CacheMode.NO_CACHING:ctx.cacheMode;
        if(cacheMode==PaintContext.CacheMode.NO_CACHING||
                !ImageCache.getInstance().isImageCachable(w,h)||
                g instanceof PrinterGraphics){
            // no caching so paint directly
            paint0(g,c,w,h,extendedCacheKeys);
        }else if(cacheMode==PaintContext.CacheMode.FIXED_SIZES){
            paintWithFixedSizeCaching(g,c,w,h,extendedCacheKeys);
        }else{
            // 9 Square caching
            paintWith9SquareCaching(g,ctx,c,w,h,extendedCacheKeys);
        }
    }

    protected Object[] getExtendedCacheKeys(JComponent c){
        return null;
    }

    protected abstract PaintContext getPaintContext();

    protected void configureGraphics(Graphics2D g){
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    }

    protected abstract void doPaint(Graphics2D g,JComponent c,int width,
                                    int height,Object[] extendedCacheKeys);

    protected final float decodeAnchorX(float x,float dx){
        if(x>=0&&x<=1){
            return decodeX(x)+(dx*leftScale);
        }else if(x>1&&x<2){
            return decodeX(x)+(dx*centerHScale);
        }else if(x>=2&&x<=3){
            return decodeX(x)+(dx*rightScale);
        }else{
            throw new IllegalArgumentException("Invalid x");
        }
    }

    protected final float decodeX(float x){
        if(x>=0&&x<=1){
            return x*leftWidth;
        }else if(x>1&&x<2){
            return ((x-1)*centerWidth)+leftWidth;
        }else if(x>=2&&x<=3){
            return ((x-2)*rightWidth)+leftWidth+centerWidth;
        }else{
            throw new IllegalArgumentException("Invalid x");
        }
    }

    protected final float decodeAnchorY(float y,float dy){
        if(y>=0&&y<=1){
            return decodeY(y)+(dy*topScale);
        }else if(y>1&&y<2){
            return decodeY(y)+(dy*centerVScale);
        }else if(y>=2&&y<=3){
            return decodeY(y)+(dy*bottomScale);
        }else{
            throw new IllegalArgumentException("Invalid y");
        }
    }

    protected final float decodeY(float y){
        if(y>=0&&y<=1){
            return y*topHeight;
        }else if(y>1&&y<2){
            return ((y-1)*centerHeight)+topHeight;
        }else if(y>=2&&y<=3){
            return ((y-2)*bottomHeight)+topHeight+centerHeight;
        }else{
            throw new IllegalArgumentException("Invalid y");
        }
    }

    protected final Color decodeColor(String key,float hOffset,float sOffset,
                                      float bOffset,int aOffset){
        if(UIManager.getLookAndFeel() instanceof NimbusLookAndFeel){
            NimbusLookAndFeel laf=(NimbusLookAndFeel)UIManager.getLookAndFeel();
            return laf.getDerivedColor(key,hOffset,sOffset,bOffset,aOffset,true);
        }else{
            // can not give a right answer as painter sould not be used outside
            // of nimbus laf but do the best we can
            return Color.getHSBColor(hOffset,sOffset,bOffset);
        }
    }

    protected final Color decodeColor(Color color1,Color color2,
                                      float midPoint){
        return new Color(NimbusLookAndFeel.deriveARGB(color1,color2,midPoint));
    }

    protected final LinearGradientPaint decodeGradient(float x1,float y1,float x2,float y2,float[] midpoints,Color[] colors){
        if(x1==x2&&y1==y2){
            y2+=.00001f;
        }
        return new LinearGradientPaint(x1,y1,x2,y2,midpoints,colors);
    }

    protected final RadialGradientPaint decodeRadialGradient(float x,float y,float r,float[] midpoints,Color[] colors){
        if(r==0f){
            r=.00001f;
        }
        return new RadialGradientPaint(x,y,r,midpoints,colors);
    }

    protected final Color getComponentColor(JComponent c,String property,
                                            Color defaultColor,
                                            float saturationOffset,
                                            float brightnessOffset,
                                            int alphaOffset){
        Color color=null;
        if(c!=null){
            // handle some special cases for performance
            if("background".equals(property)){
                color=c.getBackground();
            }else if("foreground".equals(property)){
                color=c.getForeground();
            }else if(c instanceof JList&&"selectionForeground".equals(property)){
                color=((JList)c).getSelectionForeground();
            }else if(c instanceof JList&&"selectionBackground".equals(property)){
                color=((JList)c).getSelectionBackground();
            }else if(c instanceof JTable&&"selectionForeground".equals(property)){
                color=((JTable)c).getSelectionForeground();
            }else if(c instanceof JTable&&"selectionBackground".equals(property)){
                color=((JTable)c).getSelectionBackground();
            }else{
                String s="get"+Character.toUpperCase(property.charAt(0))+property.substring(1);
                try{
                    Method method=MethodUtil.getMethod(c.getClass(),s,null);
                    color=(Color)MethodUtil.invoke(method,c,null);
                }catch(Exception e){
                    //don't do anything, it just didn't work, that's all.
                    //This could be a normal occurance if you use a property
                    //name referring to a key in clientProperties instead of
                    //a real property
                }
                if(color==null){
                    Object value=c.getClientProperty(property);
                    if(value instanceof Color){
                        color=(Color)value;
                    }
                }
            }
        }
        // we return the defaultColor if the color found is null, or if
        // it is a UIResource. This is done because the color for the
        // ENABLED state is set on the component, but you don't want to use
        // that color for the over state. So we only respect the color
        // specified for the property if it was set by the user, as opposed
        // to set by us.
        if(color==null||color instanceof UIResource){
            return defaultColor;
        }else if(saturationOffset!=0||brightnessOffset!=0||alphaOffset!=0){
            float[] tmp=Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),null);
            tmp[1]=clamp(tmp[1]+saturationOffset);
            tmp[2]=clamp(tmp[2]+brightnessOffset);
            int alpha=clamp(color.getAlpha()+alphaOffset);
            return new Color((Color.HSBtoRGB(tmp[0],tmp[1],tmp[2])&0xFFFFFF)|(alpha<<24));
        }else{
            return color;
        }
    }

    private float clamp(float value){
        if(value<0){
            value=0;
        }else if(value>1){
            value=1;
        }
        return value;
    }
    //---------------------- private methods

    private int clamp(int value){
        if(value<0){
            value=0;
        }else if(value>255){
            value=255;
        }
        return value;
    }

    //initializes the class to prepare it for being able to decode points
    private void prepare(float w,float h){
        //if no PaintContext has been specified, reset the values and bail
        //also bail if the canvasSize was not set (since decoding will not work)
        if(ctx==null||ctx.canvasSize==null){
            f=1f;
            leftWidth=centerWidth=rightWidth=0f;
            topHeight=centerHeight=bottomHeight=0f;
            leftScale=centerHScale=rightScale=0f;
            topScale=centerVScale=bottomScale=0f;
            return;
        }
        //calculate the scaling factor, and the sizes for the various 9-square sections
        Number scale=(Number)UIManager.get("scale");
        f=scale==null?1f:scale.floatValue();
        if(ctx.inverted){
            centerWidth=(ctx.b-ctx.a)*f;
            float availableSpace=w-centerWidth;
            leftWidth=availableSpace*ctx.aPercent;
            rightWidth=availableSpace*ctx.bPercent;
            centerHeight=(ctx.d-ctx.c)*f;
            availableSpace=h-centerHeight;
            topHeight=availableSpace*ctx.cPercent;
            bottomHeight=availableSpace*ctx.dPercent;
        }else{
            leftWidth=ctx.a*f;
            rightWidth=(float)(ctx.canvasSize.getWidth()-ctx.b)*f;
            centerWidth=w-leftWidth-rightWidth;
            topHeight=ctx.c*f;
            bottomHeight=(float)(ctx.canvasSize.getHeight()-ctx.d)*f;
            centerHeight=h-topHeight-bottomHeight;
        }
        leftScale=ctx.a==0f?0f:leftWidth/ctx.a;
        centerHScale=(ctx.b-ctx.a)==0f?0f:centerWidth/(ctx.b-ctx.a);
        rightScale=(ctx.canvasSize.width-ctx.b)==0f?0f:rightWidth/(ctx.canvasSize.width-ctx.b);
        topScale=ctx.c==0f?0f:topHeight/ctx.c;
        centerVScale=(ctx.d-ctx.c)==0f?0f:centerHeight/(ctx.d-ctx.c);
        bottomScale=(ctx.canvasSize.height-ctx.d)==0f?0f:bottomHeight/(ctx.canvasSize.height-ctx.d);
    }

    private void paintWith9SquareCaching(Graphics2D g,PaintContext ctx,
                                         JComponent c,int w,int h,
                                         Object[] extendedCacheKeys){
        // check if we can scale to the requested size
        Dimension canvas=ctx.canvasSize;
        Insets insets=ctx.stretchingInsets;
        if(w<=(canvas.width*ctx.maxHorizontalScaleFactor)&&h<=(canvas.height*ctx.maxVerticalScaleFactor)){
            // get image at canvas size
            VolatileImage img=getImage(g.getDeviceConfiguration(),c,canvas.width,canvas.height,extendedCacheKeys);
            if(img!=null){
                // calculate dst inserts
                // todo: destination inserts need to take into acount scale factor for high dpi. Note: You can use f for this, I think
                Insets dstInsets;
                if(ctx.inverted){
                    int leftRight=(w-(canvas.width-(insets.left+insets.right)))/2;
                    int topBottom=(h-(canvas.height-(insets.top+insets.bottom)))/2;
                    dstInsets=new Insets(topBottom,leftRight,topBottom,leftRight);
                }else{
                    dstInsets=insets;
                }
                // paint 9 square scaled
                Object oldScaleingHints=g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                ImageScalingHelper.paint(g,0,0,w,h,img,insets,dstInsets,
                        ImageScalingHelper.PaintType.PAINT9_STRETCH,ImageScalingHelper.PAINT_ALL);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        oldScaleingHints!=null?oldScaleingHints:RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            }else{
                // render directly
                paint0(g,c,w,h,extendedCacheKeys);
            }
        }else{
            // paint directly
            paint0(g,c,w,h,extendedCacheKeys);
        }
    }

    private void paintWithFixedSizeCaching(Graphics2D g,JComponent c,int w,
                                           int h,Object[] extendedCacheKeys){
        VolatileImage img=getImage(g.getDeviceConfiguration(),c,w,h,extendedCacheKeys);
        if(img!=null){
            //render cached image
            g.drawImage(img,0,0,null);
        }else{
            // render directly
            paint0(g,c,w,h,extendedCacheKeys);
        }
    }

    private VolatileImage getImage(GraphicsConfiguration config,JComponent c,
                                   int w,int h,Object[] extendedCacheKeys){
        ImageCache imageCache=ImageCache.getInstance();
        //get the buffer for this component
        VolatileImage buffer=(VolatileImage)imageCache.getImage(config,w,h,this,extendedCacheKeys);
        int renderCounter=0; //to avoid any potential, though unlikely, infinite loop
        do{
            //validate the buffer so we can check for surface loss
            int bufferStatus=VolatileImage.IMAGE_INCOMPATIBLE;
            if(buffer!=null){
                bufferStatus=buffer.validate(config);
            }
            //If the buffer status is incompatible or restored, then we need to re-render to the volatile image
            if(bufferStatus==VolatileImage.IMAGE_INCOMPATIBLE||bufferStatus==VolatileImage.IMAGE_RESTORED){
                //if the buffer is null (hasn't been created), or isn't the right size, or has lost its contents,
                //then recreate the buffer
                if(buffer==null||buffer.getWidth()!=w||buffer.getHeight()!=h||
                        bufferStatus==VolatileImage.IMAGE_INCOMPATIBLE){
                    //clear any resources related to the old back buffer
                    if(buffer!=null){
                        buffer.flush();
                        buffer=null;
                    }
                    //recreate the buffer
                    buffer=config.createCompatibleVolatileImage(w,h,
                            Transparency.TRANSLUCENT);
                    // put in cache for future
                    imageCache.setImage(buffer,config,w,h,this,extendedCacheKeys);
                }
                //create the graphics context with which to paint to the buffer
                Graphics2D bg=buffer.createGraphics();
                //clear the background before configuring the graphics
                bg.setComposite(AlphaComposite.Clear);
                bg.fillRect(0,0,w,h);
                bg.setComposite(AlphaComposite.SrcOver);
                configureGraphics(bg);
                // paint the painter into buffer
                paint0(bg,c,w,h,extendedCacheKeys);
                //close buffer graphics
                bg.dispose();
            }
        }while(buffer.contentsLost()&&renderCounter++<3);
        // check if we failed
        if(renderCounter==3) return null;
        // return image
        return buffer;
    }

    //convenience method which creates a temporary graphics object by creating a
    //clone of the passed in one, configuring it, drawing with it, disposing it.
    //These steps have to be taken to ensure that any hints set on the graphics
    //are removed subsequent to painting.
    private void paint0(Graphics2D g,JComponent c,int width,int height,
                        Object[] extendedCacheKeys){
        prepare(width,height);
        g=(Graphics2D)g.create();
        configureGraphics(g);
        doPaint(g,c,width,height,extendedCacheKeys);
        g.dispose();
    }

    protected static class PaintContext{
        private static Insets EMPTY_INSETS=new Insets(0,0,0,0);
        private Insets stretchingInsets;
        private Dimension canvasSize;
        private boolean inverted;
        private CacheMode cacheMode;
        private double maxHorizontalScaleFactor;
        private double maxVerticalScaleFactor;
        private float a; // insets.left
        private float b; // canvasSize.width - insets.right
        private float c; // insets.top
        private float d; // canvasSize.height - insets.bottom;
        private float aPercent; // only used if inverted == true
        private float bPercent; // only used if inverted == true
        private float cPercent; // only used if inverted == true
        private float dPercent; // only used if inverted == true
        public PaintContext(Insets insets,Dimension canvasSize,boolean inverted){
            this(insets,canvasSize,inverted,null,1,1);
        }

        public PaintContext(Insets insets,Dimension canvasSize,boolean inverted,
                            CacheMode cacheMode,double maxH,double maxV){
            if(maxH<1||maxH<1){
                throw new IllegalArgumentException("Both maxH and maxV must be >= 1");
            }
            this.stretchingInsets=insets==null?EMPTY_INSETS:insets;
            this.canvasSize=canvasSize;
            this.inverted=inverted;
            this.cacheMode=cacheMode==null?CacheMode.NO_CACHING:cacheMode;
            this.maxHorizontalScaleFactor=maxH;
            this.maxVerticalScaleFactor=maxV;
            if(canvasSize!=null){
                a=stretchingInsets.left;
                b=canvasSize.width-stretchingInsets.right;
                c=stretchingInsets.top;
                d=canvasSize.height-stretchingInsets.bottom;
                this.canvasSize=canvasSize;
                this.inverted=inverted;
                if(inverted){
                    float available=canvasSize.width-(b-a);
                    aPercent=available>0f?a/available:0f;
                    bPercent=available>0f?b/available:0f;
                    available=canvasSize.height-(d-c);
                    cPercent=available>0f?c/available:0f;
                    dPercent=available>0f?d/available:0f;
                }
            }
        }

        protected static enum CacheMode{
            NO_CACHING,FIXED_SIZES,NINE_SQUARE_SCALE
        }
    }
}
