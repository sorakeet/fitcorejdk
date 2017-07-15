/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import java.awt.*;
import java.awt.event.WindowListener;

@SuppressWarnings("serial")
public class JWindow extends Window implements Accessible,
        RootPaneContainer,
        TransferHandler.HasGetTransferHandler{
    protected JRootPane rootPane;
    protected boolean rootPaneCheckingEnabled=false;
/////////////////
// Accessibility support
////////////////
    protected AccessibleContext accessibleContext=null;
    private TransferHandler transferHandler;

    public JWindow(){
        this((Frame)null);
    }

    public JWindow(Frame owner){
        super(owner==null?SwingUtilities.getSharedOwnerFrame():owner);
        if(owner==null){
            WindowListener ownerShutdownListener=
                    SwingUtilities.getSharedOwnerFrameShutdownListener();
            addWindowListener(ownerShutdownListener);
        }
        windowInit();
    }

    protected void windowInit(){
        setLocale(JComponent.getDefaultLocale());
        setRootPane(createRootPane());
        setRootPaneCheckingEnabled(true);
        sun.awt.SunToolkit.checkAndSetPolicy(this);
    }

    protected JRootPane createRootPane(){
        JRootPane rp=new JRootPane();
        // NOTE: this uses setOpaque vs LookAndFeel.installProperty as there
        // is NO reason for the RootPane not to be opaque. For painting to
        // work the contentPane must be opaque, therefor the RootPane can
        // also be opaque.
        rp.setOpaque(true);
        return rp;
    }

    public JWindow(GraphicsConfiguration gc){
        this(null,gc);
        super.setFocusableWindowState(false);
    }

    public JWindow(Window owner,GraphicsConfiguration gc){
        super(owner==null?(Window)SwingUtilities.getSharedOwnerFrame():
                owner,gc);
        if(owner==null){
            WindowListener ownerShutdownListener=
                    SwingUtilities.getSharedOwnerFrameShutdownListener();
            addWindowListener(ownerShutdownListener);
        }
        windowInit();
    }

    public JWindow(Window owner){
        super(owner==null?(Window)SwingUtilities.getSharedOwnerFrame():
                owner);
        if(owner==null){
            WindowListener ownerShutdownListener=
                    SwingUtilities.getSharedOwnerFrameShutdownListener();
            addWindowListener(ownerShutdownListener);
        }
        windowInit();
    }    protected boolean isRootPaneCheckingEnabled(){
        return rootPaneCheckingEnabled;
    }

    public TransferHandler getTransferHandler(){
        return transferHandler;
    }

    public void setTransferHandler(TransferHandler newHandler){
        TransferHandler oldHandler=transferHandler;
        transferHandler=newHandler;
        SwingUtilities.installSwingDropTargetAsNecessary(this,transferHandler);
        firePropertyChange("transferHandler",oldHandler,newHandler);
    }

    protected void addImpl(Component comp,Object constraints,int index){
        if(isRootPaneCheckingEnabled()){
            getContentPane().add(comp,constraints,index);
        }else{
            super.addImpl(comp,constraints,index);
        }
    }

    public void remove(Component comp){
        if(comp==rootPane){
            super.remove(comp);
        }else{
            getContentPane().remove(comp);
        }
    }    protected void setRootPaneCheckingEnabled(boolean enabled){
        rootPaneCheckingEnabled=enabled;
    }

    public void setLayout(LayoutManager manager){
        if(isRootPaneCheckingEnabled()){
            getContentPane().setLayout(manager);
        }else{
            super.setLayout(manager);
        }
    }

    public void update(Graphics g){
        paint(g);
    }

    protected String paramString(){
        String rootPaneCheckingEnabledString=(rootPaneCheckingEnabled?
                "true":"false");
        return super.paramString()+
                ",rootPaneCheckingEnabled="+rootPaneCheckingEnabledString;
    }

    public Graphics getGraphics(){
        JComponent.getGraphicsInvoked(this);
        return super.getGraphics();
    }    public JRootPane getRootPane(){
        return rootPane;
    }

    public void repaint(long time,int x,int y,int width,int height){
        if(RepaintManager.HANDLE_TOP_LEVEL_PAINT){
            RepaintManager.currentManager(this).addDirtyRegion(
                    this,x,y,width,height);
        }else{
            super.repaint(time,x,y,width,height);
        }
    }    protected void setRootPane(JRootPane root){
        if(rootPane!=null){
            remove(rootPane);
        }
        rootPane=root;
        if(rootPane!=null){
            boolean checkingEnabled=isRootPaneCheckingEnabled();
            try{
                setRootPaneCheckingEnabled(false);
                add(rootPane,BorderLayout.CENTER);
            }finally{
                setRootPaneCheckingEnabled(checkingEnabled);
            }
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJWindow();
        }
        return accessibleContext;
    }    public Container getContentPane(){
        return getRootPane().getContentPane();
    }

    @SuppressWarnings("serial")
    protected class AccessibleJWindow extends AccessibleAWTWindow{
        // everything is in the new parent, AccessibleAWTWindow
    }    public void setContentPane(Container contentPane){
        getRootPane().setContentPane(contentPane);
    }

    public JLayeredPane getLayeredPane(){
        return getRootPane().getLayeredPane();
    }

    public void setLayeredPane(JLayeredPane layeredPane){
        getRootPane().setLayeredPane(layeredPane);
    }

    public Component getGlassPane(){
        return getRootPane().getGlassPane();
    }

    public void setGlassPane(Component glassPane){
        getRootPane().setGlassPane(glassPane);
    }











}
