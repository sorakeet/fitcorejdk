/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AWTAccessor;
import sun.awt.SunToolkit;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import java.awt.event.KeyEvent;
import java.awt.peer.FramePeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class Frame extends Window implements MenuContainer{
    @Deprecated
    public static final int DEFAULT_CURSOR=Cursor.DEFAULT_CURSOR;
    @Deprecated
    public static final int CROSSHAIR_CURSOR=Cursor.CROSSHAIR_CURSOR;
    @Deprecated
    public static final int TEXT_CURSOR=Cursor.TEXT_CURSOR;
    @Deprecated
    public static final int WAIT_CURSOR=Cursor.WAIT_CURSOR;
    @Deprecated
    public static final int SW_RESIZE_CURSOR=Cursor.SW_RESIZE_CURSOR;
    @Deprecated
    public static final int SE_RESIZE_CURSOR=Cursor.SE_RESIZE_CURSOR;
    @Deprecated
    public static final int NW_RESIZE_CURSOR=Cursor.NW_RESIZE_CURSOR;
    @Deprecated
    public static final int NE_RESIZE_CURSOR=Cursor.NE_RESIZE_CURSOR;
    @Deprecated
    public static final int N_RESIZE_CURSOR=Cursor.N_RESIZE_CURSOR;
    @Deprecated
    public static final int S_RESIZE_CURSOR=Cursor.S_RESIZE_CURSOR;
    @Deprecated
    public static final int W_RESIZE_CURSOR=Cursor.W_RESIZE_CURSOR;
    @Deprecated
    public static final int E_RESIZE_CURSOR=Cursor.E_RESIZE_CURSOR;
    @Deprecated
    public static final int HAND_CURSOR=Cursor.HAND_CURSOR;
    @Deprecated
    public static final int MOVE_CURSOR=Cursor.MOVE_CURSOR;
    public static final int NORMAL=0;
    public static final int ICONIFIED=1;
    public static final int MAXIMIZED_HORIZ=2;
    public static final int MAXIMIZED_VERT=4;
    public static final int MAXIMIZED_BOTH=MAXIMIZED_VERT|MAXIMIZED_HORIZ;
    private static final String base="frame";
    private static final long serialVersionUID=2673458971256075116L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    static{
        AWTAccessor.setFrameAccessor(
                new AWTAccessor.FrameAccessor(){
                    public void setExtendedState(Frame frame,int state){
                        synchronized(frame.getObjectLock()){
                            frame.state=state;
                        }
                    }

                    public int getExtendedState(Frame frame){
                        synchronized(frame.getObjectLock()){
                            return frame.state;
                        }
                    }

                    public Rectangle getMaximizedBounds(Frame frame){
                        synchronized(frame.getObjectLock()){
                            return frame.maximizedBounds;
                        }
                    }
                }
        );
    }

    Rectangle maximizedBounds;
    String title="Untitled";
    MenuBar menuBar;
    boolean resizable=true;
    boolean undecorated=false;
    boolean mbManagement=false;
    /** used only by the Motif impl. */
    Vector<Window> ownedWindows;
    // XXX: uwe: abuse old field for now
    // will need to take care of serialization
    private int state=NORMAL;
    private int frameSerializedDataVersion=1;

    public Frame() throws HeadlessException{
        this("");
    }

    public Frame(String title) throws HeadlessException{
        init(title,null);
    }

    private void init(String title,GraphicsConfiguration gc){
        this.title=title;
        SunToolkit.checkAndSetPolicy(this);
    }

    public Frame(GraphicsConfiguration gc){
        this("",gc);
    }

    public Frame(String title,GraphicsConfiguration gc){
        super(gc);
        init(title,gc);
    }

    public static Frame[] getFrames(){
        Window[] allWindows=Window.getWindows();
        int frameCount=0;
        for(Window w : allWindows){
            if(w instanceof Frame){
                frameCount++;
            }
        }
        Frame[] frames=new Frame[frameCount];
        int c=0;
        for(Window w : allWindows){
            if(w instanceof Frame){
                frames[c++]=(Frame)w;
            }
        }
        return frames;
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(Frame.class){
            return base+nameCounter++;
        }
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        String oldTitle=this.title;
        if(title==null){
            title="";
        }
        synchronized(this){
            this.title=title;
            FramePeer peer=(FramePeer)this.peer;
            if(peer!=null){
                peer.setTitle(title);
            }
        }
        firePropertyChange("title",oldTitle,title);
    }

    public Image getIconImage(){
        java.util.List<Image> icons=this.icons;
        if(icons!=null){
            if(icons.size()>0){
                return icons.get(0);
            }
        }
        return null;
    }

    public void setIconImage(Image image){
        super.setIconImage(image);
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null){
                peer=getToolkit().createFrame(this);
            }
            FramePeer p=(FramePeer)peer;
            MenuBar menuBar=this.menuBar;
            if(menuBar!=null){
                mbManagement=true;
                menuBar.addNotify();
                p.setMenuBar(menuBar);
            }
            p.setMaximizedBounds(maximizedBounds);
            super.addNotify();
        }
    }

    public void removeNotify(){
        synchronized(getTreeLock()){
            FramePeer peer=(FramePeer)this.peer;
            if(peer!=null){
                // get the latest Frame state before disposing
                getState();
                if(menuBar!=null){
                    mbManagement=true;
                    peer.setMenuBar(null);
                    menuBar.removeNotify();
                }
            }
            super.removeNotify();
        }
    }

    public synchronized int getState(){
        return (getExtendedState()&ICONIFIED)!=0?ICONIFIED:NORMAL;
    }

    public synchronized void setState(int state){
        int current=getExtendedState();
        if(state==ICONIFIED&&(current&ICONIFIED)==0){
            setExtendedState(current|ICONIFIED);
        }else if(state==NORMAL&&(current&ICONIFIED)!=0){
            setExtendedState(current&~ICONIFIED);
        }
    }

    public int getExtendedState(){
        synchronized(getObjectLock()){
            return state;
        }
    }

    public void setExtendedState(int state){
        if(!isFrameStateSupported(state)){
            return;
        }
        synchronized(getObjectLock()){
            this.state=state;
        }
        // peer.setState must be called outside of object lock
        // synchronization block to avoid possible deadlock
        FramePeer peer=(FramePeer)this.peer;
        if(peer!=null){
            peer.setState(state);
        }
    }

    private boolean isFrameStateSupported(int state){
        if(!getToolkit().isFrameStateSupported(state)){
            // * Toolkit.isFrameStateSupported returns always false
            // on compound state even if all parts are supported;
            // * if part of state is not supported, state is not supported;
            // * MAXIMIZED_BOTH is not a compound state.
            if(((state&ICONIFIED)!=0)&&
                    !getToolkit().isFrameStateSupported(ICONIFIED)){
                return false;
            }else{
                state&=~ICONIFIED;
            }
            return getToolkit().isFrameStateSupported(state);
        }
        return true;
    }

    void postProcessKeyEvent(KeyEvent e){
        if(menuBar!=null&&menuBar.handleShortcut(e)){
            e.consume();
            return;
        }
        super.postProcessKeyEvent(e);
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTFrame();
        }
        return accessibleContext;
    }

    @Override
    public void setOpacity(float opacity){
        synchronized(getTreeLock()){
            if((opacity<1.0f)&&!isUndecorated()){
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setOpacity(opacity);
        }
    }

    public boolean isUndecorated(){
        return undecorated;
    }

    public void setUndecorated(boolean undecorated){
        /** Make sure we don't run in the middle of peer creation.*/
        synchronized(getTreeLock()){
            if(isDisplayable()){
                throw new IllegalComponentStateException("The frame is displayable.");
            }
            if(!undecorated){
                if(getOpacity()<1.0f){
                    throw new IllegalComponentStateException("The frame is not opaque");
                }
                if(getShape()!=null){
                    throw new IllegalComponentStateException("The frame does not have a default shape");
                }
                Color bg=getBackground();
                if((bg!=null)&&(bg.getAlpha()<255)){
                    throw new IllegalComponentStateException("The frame background color is not opaque");
                }
            }
            this.undecorated=undecorated;
        }
    }

    @Override
    public void setShape(Shape shape){
        synchronized(getTreeLock()){
            if((shape!=null)&&!isUndecorated()){
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setShape(shape);
        }
    }

    @Override
    public void setBackground(Color bgColor){
        synchronized(getTreeLock()){
            if((bgColor!=null)&&(bgColor.getAlpha()<255)&&!isUndecorated()){
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setBackground(bgColor);
        }
    }

    public MenuBar getMenuBar(){
        return menuBar;
    }

    public void setMenuBar(MenuBar mb){
        synchronized(getTreeLock()){
            if(menuBar==mb){
                return;
            }
            if((mb!=null)&&(mb.parent!=null)){
                mb.parent.remove(mb);
            }
            if(menuBar!=null){
                remove(menuBar);
            }
            menuBar=mb;
            if(menuBar!=null){
                menuBar.parent=this;
                FramePeer peer=(FramePeer)this.peer;
                if(peer!=null){
                    mbManagement=true;
                    menuBar.addNotify();
                    invalidateIfValid();
                    peer.setMenuBar(menuBar);
                }
            }
        }
    }

    public void remove(MenuComponent m){
        if(m==null){
            return;
        }
        synchronized(getTreeLock()){
            if(m==menuBar){
                menuBar=null;
                FramePeer peer=(FramePeer)this.peer;
                if(peer!=null){
                    mbManagement=true;
                    invalidateIfValid();
                    peer.setMenuBar(null);
                    m.removeNotify();
                }
                m.parent=null;
            }else{
                super.remove(m);
            }
        }
    }

    public boolean isResizable(){
        return resizable;
    }

    public void setResizable(boolean resizable){
        boolean oldResizable=this.resizable;
        boolean testvalid=false;
        synchronized(this){
            this.resizable=resizable;
            FramePeer peer=(FramePeer)this.peer;
            if(peer!=null){
                peer.setResizable(resizable);
                testvalid=true;
            }
        }
        // On some platforms, changing the resizable state affects
        // the insets of the Frame. If we could, we'd call invalidate()
        // from the peer, but we need to guarantee that we're not holding
        // the Frame lock when we call invalidate().
        if(testvalid){
            invalidateIfValid();
        }
        firePropertyChange("resizable",oldResizable,resizable);
    }

    public Rectangle getMaximizedBounds(){
        synchronized(getObjectLock()){
            return maximizedBounds;
        }
    }

    public void setMaximizedBounds(Rectangle bounds){
        synchronized(getObjectLock()){
            this.maximizedBounds=bounds;
        }
        FramePeer peer=(FramePeer)this.peer;
        if(peer!=null){
            peer.setMaximizedBounds(bounds);
        }
    }

    protected String paramString(){
        String str=super.paramString();
        if(title!=null){
            str+=",title="+title;
        }
        if(resizable){
            str+=",resizable";
        }
        int state=getExtendedState();
        if(state==NORMAL){
            str+=",normal";
        }else{
            if((state&ICONIFIED)!=0){
                str+=",iconified";
            }
            if((state&MAXIMIZED_BOTH)==MAXIMIZED_BOTH){
                str+=",maximized";
            }else if((state&MAXIMIZED_HORIZ)!=0){
                str+=",maximized_horiz";
            }else if((state&MAXIMIZED_VERT)!=0){
                str+=",maximized_vert";
            }
        }
        return str;
    }

    @Deprecated
    public void setCursor(int cursorType){
        if(cursorType<DEFAULT_CURSOR||cursorType>MOVE_CURSOR){
            throw new IllegalArgumentException("illegal cursor type");
        }
        setCursor(Cursor.getPredefinedCursor(cursorType));
    }

    @Deprecated
    public int getCursorType(){
        return (getCursor().getType());
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
        if(icons!=null&&icons.size()>0){
            Image icon1=icons.get(0);
            if(icon1 instanceof Serializable){
                s.writeObject(icon1);
                return;
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException, HeadlessException{
        // HeadlessException is thrown by Window's readObject
        s.defaultReadObject();
        try{
            Image icon=(Image)s.readObject();
            if(icons==null){
                icons=new ArrayList<Image>();
                icons.add(icon);
            }
        }catch(java.io.OptionalDataException e){
            // pre-1.4 instances will not have this optional data.
            // 1.6 and later instances serialize icons in the Window class
            // e.eof will be true to indicate that there is no more
            // data available for this object.
            // If e.eof is not true, throw the exception as it
            // might have been caused by unrelated reasons.
            if(!e.eof){
                throw (e);
            }
        }
        if(menuBar!=null)
            menuBar.parent=this;
        // Ensure 1.1 serialized Frames can read & hook-up
        // owned windows properly
        //
        if(ownedWindows!=null){
            for(int i=0;i<ownedWindows.size();i++){
                connectOwnedWindow(ownedWindows.elementAt(i));
            }
            ownedWindows=null;
        }
    }

    protected class AccessibleAWTFrame extends AccessibleAWTWindow{
        private static final long serialVersionUID=-6172960752956030250L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.FRAME;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            if(getFocusOwner()!=null){
                states.add(AccessibleState.ACTIVE);
            }
            if(isResizable()){
                states.add(AccessibleState.RESIZABLE);
            }
            return states;
        }
    } // inner class AccessibleAWTFrame
}
