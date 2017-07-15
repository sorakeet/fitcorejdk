/**
 * Copyright (c) 1999, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.ComponentFactory;
import sun.awt.SunToolkit;
import sun.awt.image.SunWritableRaster;
import sun.security.util.SecurityConstants;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.awt.peer.RobotPeer;
import java.lang.reflect.InvocationTargetException;

public class Robot{
    private static final int MAX_DELAY=60000;
    private static int LEGAL_BUTTON_MASK=0;
    private RobotPeer peer;
    private boolean isAutoWaitForIdle=false;
    private int autoDelay=0;
    private DirectColorModel screenCapCM=null;
    private transient Object anchor=new Object();
    private transient RobotDisposer disposer;

    public Robot() throws AWTException{
        if(GraphicsEnvironment.isHeadless()){
            throw new AWTException("headless environment");
        }
        init(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice());
    }

    private void init(GraphicsDevice screen) throws AWTException{
        checkRobotAllowed();
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        if(toolkit instanceof ComponentFactory){
            peer=((ComponentFactory)toolkit).createRobot(this,screen);
            disposer=new RobotDisposer(peer);
            sun.java2d.Disposer.addRecord(anchor,disposer);
        }
        initLegalButtonMask();
    }

    private static synchronized void initLegalButtonMask(){
        if(LEGAL_BUTTON_MASK!=0) return;
        int tmpMask=0;
        if(Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled()){
            if(Toolkit.getDefaultToolkit() instanceof SunToolkit){
                final int buttonsNumber=((SunToolkit)(Toolkit.getDefaultToolkit())).getNumberOfButtons();
                for(int i=0;i<buttonsNumber;i++){
                    tmpMask|=InputEvent.getMaskForButton(i+1);
                }
            }
        }
        tmpMask|=InputEvent.BUTTON1_MASK|
                InputEvent.BUTTON2_MASK|
                InputEvent.BUTTON3_MASK|
                InputEvent.BUTTON1_DOWN_MASK|
                InputEvent.BUTTON2_DOWN_MASK|
                InputEvent.BUTTON3_DOWN_MASK;
        LEGAL_BUTTON_MASK=tmpMask;
    }

    private void checkRobotAllowed(){
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkPermission(SecurityConstants.AWT.CREATE_ROBOT_PERMISSION);
        }
    }

    public Robot(GraphicsDevice screen) throws AWTException{
        checkIsScreenDevice(screen);
        init(screen);
    }

    private void checkIsScreenDevice(GraphicsDevice device){
        if(device==null||device.getType()!=GraphicsDevice.TYPE_RASTER_SCREEN){
            throw new IllegalArgumentException("not a valid screen device");
        }
    }

    public synchronized void mouseMove(int x,int y){
        peer.mouseMove(x,y);
        afterEvent();
    }

    private void afterEvent(){
        autoWaitForIdle();
        autoDelay();
    }

    private void autoWaitForIdle(){
        if(isAutoWaitForIdle){
            waitForIdle();
        }
    }

    public synchronized void waitForIdle(){
        checkNotDispatchThread();
        // post a dummy event to the queue so we know when
        // all the events before it have been processed
        try{
            SunToolkit.flushPendingEvents();
            EventQueue.invokeAndWait(new Runnable(){
                public void run(){
                    // dummy implementation
                }
            });
        }catch(InterruptedException ite){
            System.err.println("Robot.waitForIdle, non-fatal exception caught:");
            ite.printStackTrace();
        }catch(InvocationTargetException ine){
            System.err.println("Robot.waitForIdle, non-fatal exception caught:");
            ine.printStackTrace();
        }
    }

    private void checkNotDispatchThread(){
        if(EventQueue.isDispatchThread()){
            throw new IllegalThreadStateException("Cannot call method from the event dispatcher thread");
        }
    }

    private void autoDelay(){
        delay(autoDelay);
    }

    public synchronized void delay(int ms){
        checkDelayArgument(ms);
        try{
            Thread.sleep(ms);
        }catch(InterruptedException ite){
            ite.printStackTrace();
        }
    }

    private void checkDelayArgument(int ms){
        if(ms<0||ms>MAX_DELAY){
            throw new IllegalArgumentException("Delay must be to 0 to 60,000ms");
        }
    }

    public synchronized void mousePress(int buttons){
        checkButtonsArgument(buttons);
        peer.mousePress(buttons);
        afterEvent();
    }

    private void checkButtonsArgument(int buttons){
        if((buttons|LEGAL_BUTTON_MASK)!=LEGAL_BUTTON_MASK){
            throw new IllegalArgumentException("Invalid combination of button flags");
        }
    }

    public synchronized void mouseRelease(int buttons){
        checkButtonsArgument(buttons);
        peer.mouseRelease(buttons);
        afterEvent();
    }

    public synchronized void mouseWheel(int wheelAmt){
        peer.mouseWheel(wheelAmt);
        afterEvent();
    }

    public synchronized void keyPress(int keycode){
        checkKeycodeArgument(keycode);
        peer.keyPress(keycode);
        afterEvent();
    }

    private void checkKeycodeArgument(int keycode){
        // rather than build a big table or switch statement here, we'll
        // just check that the key isn't VK_UNDEFINED and assume that the
        // peer implementations will throw an exception for other bogus
        // values e.g. -1, 999999
        if(keycode==KeyEvent.VK_UNDEFINED){
            throw new IllegalArgumentException("Invalid key code");
        }
    }

    public synchronized void keyRelease(int keycode){
        checkKeycodeArgument(keycode);
        peer.keyRelease(keycode);
        afterEvent();
    }

    public synchronized Color getPixelColor(int x,int y){
        Color color=new Color(peer.getRGBPixel(x,y));
        return color;
    }

    public synchronized BufferedImage createScreenCapture(Rectangle screenRect){
        checkScreenCaptureAllowed();
        checkValidRect(screenRect);
        BufferedImage image;
        DataBufferInt buffer;
        WritableRaster raster;
        if(screenCapCM==null){
            /**
             * Fix for 4285201
             * Create a DirectColorModel equivalent to the default RGB ColorModel,
             * except with no Alpha component.
             */
            screenCapCM=new DirectColorModel(24,
                    /** red mask */0x00FF0000,
                    /** green mask */0x0000FF00,
                    /** blue mask */0x000000FF);
        }
        // need to sync the toolkit prior to grabbing the pixels since in some
        // cases rendering to the screen may be delayed
        Toolkit.getDefaultToolkit().sync();
        int pixels[];
        int[] bandmasks=new int[3];
        pixels=peer.getRGBPixels(screenRect);
        buffer=new DataBufferInt(pixels,pixels.length);
        bandmasks[0]=screenCapCM.getRedMask();
        bandmasks[1]=screenCapCM.getGreenMask();
        bandmasks[2]=screenCapCM.getBlueMask();
        raster=Raster.createPackedRaster(buffer,screenRect.width,screenRect.height,screenRect.width,bandmasks,null);
        SunWritableRaster.makeTrackable(buffer);
        image=new BufferedImage(screenCapCM,raster,false,null);
        return image;
    }

    private static void checkValidRect(Rectangle rect){
        if(rect.width<=0||rect.height<=0){
            throw new IllegalArgumentException("Rectangle width and height must be > 0");
        }
    }

    private static void checkScreenCaptureAllowed(){
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkPermission(
                    SecurityConstants.AWT.READ_DISPLAY_PIXELS_PERMISSION);
        }
    }

    public synchronized String toString(){
        String params="autoDelay = "+getAutoDelay()+", "+"autoWaitForIdle = "+isAutoWaitForIdle();
        return getClass().getName()+"[ "+params+" ]";
    }

    public synchronized boolean isAutoWaitForIdle(){
        return isAutoWaitForIdle;
    }

    public synchronized void setAutoWaitForIdle(boolean isOn){
        isAutoWaitForIdle=isOn;
    }

    public synchronized int getAutoDelay(){
        return autoDelay;
    }

    public synchronized void setAutoDelay(int ms){
        checkDelayArgument(ms);
        autoDelay=ms;
    }

    static class RobotDisposer implements sun.java2d.DisposerRecord{
        private final RobotPeer peer;

        public RobotDisposer(RobotPeer peer){
            this.peer=peer;
        }

        public void dispose(){
            if(peer!=null){
                peer.dispose();
            }
        }
    }
}
