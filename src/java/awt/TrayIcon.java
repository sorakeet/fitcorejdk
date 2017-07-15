/**
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.HeadlessToolkit;
import sun.awt.SunToolkit;

import java.awt.event.*;
import java.awt.peer.TrayIconPeer;
import java.security.AccessControlContext;
import java.security.AccessController;

public class TrayIcon{
    static{
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
        AWTAccessor.setTrayIconAccessor(
                new AWTAccessor.TrayIconAccessor(){
                    public void addNotify(TrayIcon trayIcon) throws AWTException{
                        trayIcon.addNotify();
                    }

                    public void removeNotify(TrayIcon trayIcon){
                        trayIcon.removeNotify();
                    }
                });
    }

    private final AccessControlContext acc=AccessController.getContext();
    transient MouseListener mouseListener;
    transient MouseMotionListener mouseMotionListener;
    transient ActionListener actionListener;
    private Image image;
    private String tooltip;
    private PopupMenu popup;
    private boolean autosize;
    private int id;
    private String actionCommand;
    transient private TrayIconPeer peer;

    public TrayIcon(Image image,String tooltip,PopupMenu popup){
        this(image,tooltip);
        setPopupMenu(popup);
    }

    public TrayIcon(Image image,String tooltip){
        this(image);
        setToolTip(tooltip);
    }

    public TrayIcon(Image image){
        this();
        if(image==null){
            throw new IllegalArgumentException("creating TrayIcon with null Image");
        }
        setImage(image);
    }

    private TrayIcon()
            throws UnsupportedOperationException, HeadlessException, SecurityException{
        SystemTray.checkSystemTrayAllowed();
        if(GraphicsEnvironment.isHeadless()){
            throw new HeadlessException();
        }
        if(!SystemTray.isSupported()){
            throw new UnsupportedOperationException();
        }
        SunToolkit.insertTargetMapping(this,AppContext.getAppContext());
    }

    private static native void initIDs();

    final AccessControlContext getAccessControlContext(){
        if(acc==null){
            throw new SecurityException("TrayIcon is missing AccessControlContext");
        }
        return acc;
    }

    public Image getImage(){
        return image;
    }

    public void setImage(Image image){
        if(image==null){
            throw new NullPointerException("setting null Image");
        }
        this.image=image;
        TrayIconPeer peer=this.peer;
        if(peer!=null){
            peer.updateImage();
        }
    }

    public PopupMenu getPopupMenu(){
        return popup;
    }

    public void setPopupMenu(PopupMenu popup){
        if(popup==this.popup){
            return;
        }
        synchronized(TrayIcon.class){
            if(popup!=null){
                if(popup.isTrayIconPopup){
                    throw new IllegalArgumentException("the PopupMenu is already set for another TrayIcon");
                }
                popup.isTrayIconPopup=true;
            }
            if(this.popup!=null){
                this.popup.isTrayIconPopup=false;
            }
            this.popup=popup;
        }
    }

    public String getToolTip(){
        return tooltip;
    }

    public void setToolTip(String tooltip){
        this.tooltip=tooltip;
        TrayIconPeer peer=this.peer;
        if(peer!=null){
            peer.setToolTip(tooltip);
        }
    }

    public boolean isImageAutoSize(){
        return autosize;
    }

    public void setImageAutoSize(boolean autosize){
        this.autosize=autosize;
        TrayIconPeer peer=this.peer;
        if(peer!=null){
            peer.updateImage();
        }
    }

    public synchronized void addMouseListener(MouseListener listener){
        if(listener==null){
            return;
        }
        mouseListener=AWTEventMulticaster.add(mouseListener,listener);
    }

    public synchronized void removeMouseListener(MouseListener listener){
        if(listener==null){
            return;
        }
        mouseListener=AWTEventMulticaster.remove(mouseListener,listener);
    }

    public synchronized MouseListener[] getMouseListeners(){
        return AWTEventMulticaster.getListeners(mouseListener,MouseListener.class);
    }

    public synchronized void addMouseMotionListener(MouseMotionListener listener){
        if(listener==null){
            return;
        }
        mouseMotionListener=AWTEventMulticaster.add(mouseMotionListener,listener);
    }

    public synchronized void removeMouseMotionListener(MouseMotionListener listener){
        if(listener==null){
            return;
        }
        mouseMotionListener=AWTEventMulticaster.remove(mouseMotionListener,listener);
    }

    public synchronized MouseMotionListener[] getMouseMotionListeners(){
        return AWTEventMulticaster.getListeners(mouseMotionListener,MouseMotionListener.class);
    }

    public String getActionCommand(){
        return actionCommand;
    }

    public void setActionCommand(String command){
        actionCommand=command;
    }

    public synchronized void addActionListener(ActionListener listener){
        if(listener==null){
            return;
        }
        actionListener=AWTEventMulticaster.add(actionListener,listener);
    }

    public synchronized void removeActionListener(ActionListener listener){
        if(listener==null){
            return;
        }
        actionListener=AWTEventMulticaster.remove(actionListener,listener);
    }

    public synchronized ActionListener[] getActionListeners(){
        return AWTEventMulticaster.getListeners(actionListener,ActionListener.class);
    }

    ;

    public void displayMessage(String caption,String text,MessageType messageType){
        if(caption==null&&text==null){
            throw new NullPointerException("displaying the message with both caption and text being null");
        }
        TrayIconPeer peer=this.peer;
        if(peer!=null){
            peer.displayMessage(caption,text,messageType.name());
        }
    }

    public Dimension getSize(){
        return SystemTray.getSystemTray().getTrayIconSize();
    }
    // ****************************************************************
    // ****************************************************************

    void addNotify()
            throws AWTException{
        synchronized(this){
            if(peer==null){
                Toolkit toolkit=Toolkit.getDefaultToolkit();
                if(toolkit instanceof SunToolkit){
                    peer=((SunToolkit)Toolkit.getDefaultToolkit()).createTrayIcon(this);
                }else if(toolkit instanceof HeadlessToolkit){
                    peer=((HeadlessToolkit)Toolkit.getDefaultToolkit()).createTrayIcon(this);
                }
            }
        }
        peer.setToolTip(tooltip);
    }

    void removeNotify(){
        TrayIconPeer p=null;
        synchronized(this){
            p=peer;
            peer=null;
        }
        if(p!=null){
            p.dispose();
        }
    }

    int getID(){
        return id;
    }

    void setID(int id){
        this.id=id;
    }

    void dispatchEvent(AWTEvent e){
        EventQueue.setCurrentEventAndMostRecentTime(e);
        Toolkit.getDefaultToolkit().notifyAWTEventListeners(e);
        processEvent(e);
    }

    void processEvent(AWTEvent e){
        if(e instanceof MouseEvent){
            switch(e.getID()){
                case MouseEvent.MOUSE_PRESSED:
                case MouseEvent.MOUSE_RELEASED:
                case MouseEvent.MOUSE_CLICKED:
                    processMouseEvent((MouseEvent)e);
                    break;
                case MouseEvent.MOUSE_MOVED:
                    processMouseMotionEvent((MouseEvent)e);
                    break;
                default:
                    return;
            }
        }else if(e instanceof ActionEvent){
            processActionEvent((ActionEvent)e);
        }
    }

    void processMouseEvent(MouseEvent e){
        MouseListener listener=mouseListener;
        if(listener!=null){
            int id=e.getID();
            switch(id){
                case MouseEvent.MOUSE_PRESSED:
                    listener.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    listener.mouseReleased(e);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    listener.mouseClicked(e);
                    break;
                default:
                    return;
            }
        }
    }

    void processMouseMotionEvent(MouseEvent e){
        MouseMotionListener listener=mouseMotionListener;
        if(listener!=null&&
                e.getID()==MouseEvent.MOUSE_MOVED){
            listener.mouseMoved(e);
        }
    }

    void processActionEvent(ActionEvent e){
        ActionListener listener=actionListener;
        if(listener!=null){
            listener.actionPerformed(e);
        }
    }

    public enum MessageType{
        ERROR,
        WARNING,
        INFO,
        NONE
    }
}
