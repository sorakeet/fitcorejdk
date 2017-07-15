/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AppContext;
import sun.awt.SunToolkit;

import java.awt.image.ColorModel;

public abstract class GraphicsDevice{
    public final static int TYPE_RASTER_SCREEN=0;
    public final static int TYPE_PRINTER=1;
    public final static int TYPE_IMAGE_BUFFER=2;
    // created the FS window
    // this lock is used for making synchronous changes to the AppContext's
    // current full screen window
    private final Object fsAppContextLock=new Object();
    private Window fullScreenWindow;
    private AppContext fullScreenAppContext; // tracks which AppContext
    private Rectangle windowedModeBounds;

    protected GraphicsDevice(){
    }

    public abstract int getType();

    public abstract String getIDstring();

    public GraphicsConfiguration
    getBestConfiguration(GraphicsConfigTemplate gct){
        GraphicsConfiguration[] configs=getConfigurations();
        return gct.getBestConfiguration(configs);
    }

    public abstract GraphicsConfiguration[] getConfigurations();

    public boolean isFullScreenSupported(){
        return false;
    }

    public Window getFullScreenWindow(){
        Window returnWindow=null;
        synchronized(fsAppContextLock){
            // Only return a handle to the current fs window if we are in the
            // same AppContext that set the fs window
            if(fullScreenAppContext==AppContext.getAppContext()){
                returnWindow=fullScreenWindow;
            }
        }
        return returnWindow;
    }

    public void setFullScreenWindow(Window w){
        if(w!=null){
            if(w.getShape()!=null){
                w.setShape(null);
            }
            if(w.getOpacity()<1.0f){
                w.setOpacity(1.0f);
            }
            if(!w.isOpaque()){
                Color bgColor=w.getBackground();
                bgColor=new Color(bgColor.getRed(),bgColor.getGreen(),
                        bgColor.getBlue(),255);
                w.setBackground(bgColor);
            }
            // Check if this window is in fullscreen mode on another device.
            final GraphicsConfiguration gc=w.getGraphicsConfiguration();
            if(gc!=null&&gc.getDevice()!=this
                    &&gc.getDevice().getFullScreenWindow()==w){
                gc.getDevice().setFullScreenWindow(null);
            }
        }
        if(fullScreenWindow!=null&&windowedModeBounds!=null){
            // if the window went into fs mode before it was realized it may
            // have (0,0) dimensions
            if(windowedModeBounds.width==0) windowedModeBounds.width=1;
            if(windowedModeBounds.height==0) windowedModeBounds.height=1;
            fullScreenWindow.setBounds(windowedModeBounds);
        }
        // Set the full screen window
        synchronized(fsAppContextLock){
            // Associate fullscreen window with current AppContext
            if(w==null){
                fullScreenAppContext=null;
            }else{
                fullScreenAppContext=AppContext.getAppContext();
            }
            fullScreenWindow=w;
        }
        if(fullScreenWindow!=null){
            windowedModeBounds=fullScreenWindow.getBounds();
            // Note that we use the graphics configuration of the device,
            // not the window's, because we're setting the fs window for
            // this device.
            final GraphicsConfiguration gc=getDefaultConfiguration();
            final Rectangle screenBounds=gc.getBounds();
            if(SunToolkit.isDispatchThreadForAppContext(fullScreenWindow)){
                // Update graphics configuration here directly and do not wait
                // asynchronous notification from the peer. Note that
                // setBounds() will reset a GC, if it was set incorrectly.
                fullScreenWindow.setGraphicsConfiguration(gc);
            }
            fullScreenWindow.setBounds(screenBounds.x,screenBounds.y,
                    screenBounds.width,screenBounds.height);
            fullScreenWindow.setVisible(true);
            fullScreenWindow.toFront();
        }
    }

    public abstract GraphicsConfiguration getDefaultConfiguration();

    public boolean isDisplayChangeSupported(){
        return false;
    }

    public DisplayMode[] getDisplayModes(){
        return new DisplayMode[]{getDisplayMode()};
    }

    public DisplayMode getDisplayMode(){
        GraphicsConfiguration gc=getDefaultConfiguration();
        Rectangle r=gc.getBounds();
        ColorModel cm=gc.getColorModel();
        return new DisplayMode(r.width,r.height,cm.getPixelSize(),0);
    }

    public void setDisplayMode(DisplayMode dm){
        throw new UnsupportedOperationException("Cannot change display mode");
    }

    public int getAvailableAcceleratedMemory(){
        return -1;
    }

    public boolean isWindowTranslucencySupported(WindowTranslucency translucencyKind){
        switch(translucencyKind){
            case PERPIXEL_TRANSPARENT:
                return isWindowShapingSupported();
            case TRANSLUCENT:
                return isWindowOpacitySupported();
            case PERPIXEL_TRANSLUCENT:
                return isWindowPerpixelTranslucencySupported();
        }
        return false;
    }

    static boolean isWindowShapingSupported(){
        Toolkit curToolkit=Toolkit.getDefaultToolkit();
        if(!(curToolkit instanceof SunToolkit)){
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowShapingSupported();
    }

    static boolean isWindowOpacitySupported(){
        Toolkit curToolkit=Toolkit.getDefaultToolkit();
        if(!(curToolkit instanceof SunToolkit)){
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowOpacitySupported();
    }

    boolean isWindowPerpixelTranslucencySupported(){
        /**
         * Per-pixel alpha is supported if all the conditions are TRUE:
         *    1. The toolkit is a sort of SunToolkit
         *    2. The toolkit supports translucency in general
         *        (isWindowTranslucencySupported())
         *    3. There's at least one translucency-capable
         *        GraphicsConfiguration
         */
        Toolkit curToolkit=Toolkit.getDefaultToolkit();
        if(!(curToolkit instanceof SunToolkit)){
            return false;
        }
        if(!((SunToolkit)curToolkit).isWindowTranslucencySupported()){
            return false;
        }
        // TODO: cache translucency capable GC
        return getTranslucencyCapableGC()!=null;
    }

    GraphicsConfiguration getTranslucencyCapableGC(){
        // If the default GC supports translucency return true.
        // It is important to optimize the verification this way,
        // see CR 6661196 for more details.
        GraphicsConfiguration defaultGC=getDefaultConfiguration();
        if(defaultGC.isTranslucencyCapable()){
            return defaultGC;
        }
        // ... otherwise iterate through all the GCs.
        GraphicsConfiguration[] configs=getConfigurations();
        for(int j=0;j<configs.length;j++){
            if(configs[j].isTranslucencyCapable()){
                return configs[j];
            }
        }
        return null;
    }

    public static enum WindowTranslucency{
        PERPIXEL_TRANSPARENT,
        TRANSLUCENT,
        PERPIXEL_TRANSLUCENT;
    }
}
