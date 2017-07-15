/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import java.applet.Applet;
import java.awt.*;

public class JApplet extends Applet implements Accessible,
        RootPaneContainer,
        TransferHandler.HasGetTransferHandler{
    protected JRootPane rootPane;
    protected boolean rootPaneCheckingEnabled=false;
/////////////////
// Accessibility support
////////////////
    protected AccessibleContext accessibleContext=null;
    private TransferHandler transferHandler;

    public JApplet() throws HeadlessException{
        super();
        // Check the timerQ and restart if necessary.
        TimerQueue q=TimerQueue.sharedInstance();
        if(q!=null){
            q.startIfNeeded();
        }
        /** Workaround for bug 4155072.  The shared double buffer image
         * may hang on to a reference to this applet; unfortunately
         * Image.getGraphics() will continue to call JApplet.getForeground()
         * and getBackground() even after this applet has been destroyed.
         * So we ensure that these properties are non-null here.
         */
        setForeground(Color.black);
        setBackground(Color.white);
        setLocale(JComponent.getDefaultLocale());
        setLayout(new BorderLayout());
        setRootPane(createRootPane());
        setRootPaneCheckingEnabled(true);
        setFocusTraversalPolicyProvider(true);
        sun.awt.SunToolkit.checkAndSetPolicy(this);
        enableEvents(AWTEvent.KEY_EVENT_MASK);
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

    public TransferHandler getTransferHandler(){
        return transferHandler;
    }

    public void setTransferHandler(TransferHandler newHandler){
        TransferHandler oldHandler=transferHandler;
        transferHandler=newHandler;
        SwingUtilities.installSwingDropTargetAsNecessary(this,transferHandler);
        firePropertyChange("transferHandler",oldHandler,newHandler);
    }

    public JMenuBar getJMenuBar(){
        return getRootPane().getMenuBar();
    }

    public void setJMenuBar(JMenuBar menuBar){
        getRootPane().setMenuBar(menuBar);
    }

    public JRootPane getRootPane(){
        return rootPane;
    }

    protected void setRootPane(JRootPane root){
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

    protected void addImpl(Component comp,Object constraints,int index){
        if(isRootPaneCheckingEnabled()){
            getContentPane().add(comp,constraints,index);
        }else{
            super.addImpl(comp,constraints,index);
        }
    }

    protected boolean isRootPaneCheckingEnabled(){
        return rootPaneCheckingEnabled;
    }

    protected void setRootPaneCheckingEnabled(boolean enabled){
        rootPaneCheckingEnabled=enabled;
    }

    public void remove(Component comp){
        if(comp==rootPane){
            super.remove(comp);
        }else{
            getContentPane().remove(comp);
        }
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
    }    public Container getContentPane(){
        return getRootPane().getContentPane();
    }

    protected String paramString(){
        String rootPaneString=(rootPane!=null?
                rootPane.toString():"");
        String rootPaneCheckingEnabledString=(rootPaneCheckingEnabled?
                "true":"false");
        return super.paramString()+
                ",rootPane="+rootPaneString+
                ",rootPaneCheckingEnabled="+rootPaneCheckingEnabledString;
    }    public void setContentPane(Container contentPane){
        getRootPane().setContentPane(contentPane);
    }

    public Graphics getGraphics(){
        JComponent.getGraphicsInvoked(this);
        return super.getGraphics();
    }    public JLayeredPane getLayeredPane(){
        return getRootPane().getLayeredPane();
    }

    public void repaint(long time,int x,int y,int width,int height){
        if(RepaintManager.HANDLE_TOP_LEVEL_PAINT){
            RepaintManager.currentManager(this).addDirtyRegion(
                    this,x,y,width,height);
        }else{
            super.repaint(time,x,y,width,height);
        }
    }    public void setLayeredPane(JLayeredPane layeredPane){
        getRootPane().setLayeredPane(layeredPane);
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJApplet();
        }
        return accessibleContext;
    }    public Component getGlassPane(){
        return getRootPane().getGlassPane();
    }

    protected class AccessibleJApplet extends AccessibleApplet{
        // everything moved to new parent, AccessibleApplet
    }    public void setGlassPane(Component glassPane){
        getRootPane().setGlassPane(glassPane);
    }











}
