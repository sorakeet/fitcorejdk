/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import java.awt.*;
import java.awt.event.WindowEvent;

public class JFrame extends Frame implements WindowConstants,
        Accessible,
        RootPaneContainer,
        TransferHandler.HasGetTransferHandler{
    public static final int EXIT_ON_CLOSE=3;
    private static final Object defaultLookAndFeelDecoratedKey=
            new StringBuffer("JFrame.defaultLookAndFeelDecorated");
    protected JRootPane rootPane;
    protected boolean rootPaneCheckingEnabled=false;
/////////////////
// Accessibility support
////////////////
    protected AccessibleContext accessibleContext=null;
    private int defaultCloseOperation=HIDE_ON_CLOSE;
    private TransferHandler transferHandler;

    public JFrame() throws HeadlessException{
        super();
        frameInit();
    }

    protected void frameInit(){
        enableEvents(AWTEvent.KEY_EVENT_MASK|AWTEvent.WINDOW_EVENT_MASK);
        setLocale(JComponent.getDefaultLocale());
        setRootPane(createRootPane());
        setBackground(UIManager.getColor("control"));
        setRootPaneCheckingEnabled(true);
        if(JFrame.isDefaultLookAndFeelDecorated()){
            boolean supportsWindowDecorations=
                    UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if(supportsWindowDecorations){
                setUndecorated(true);
                getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
            }
        }
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

    public static boolean isDefaultLookAndFeelDecorated(){
        Boolean defaultLookAndFeelDecorated=
                (Boolean)SwingUtilities.appContextGet(defaultLookAndFeelDecoratedKey);
        if(defaultLookAndFeelDecorated==null){
            defaultLookAndFeelDecorated=Boolean.FALSE;
        }
        return defaultLookAndFeelDecorated.booleanValue();
    }

    public static void setDefaultLookAndFeelDecorated(boolean defaultLookAndFeelDecorated){
        if(defaultLookAndFeelDecorated){
            SwingUtilities.appContextPut(defaultLookAndFeelDecoratedKey,Boolean.TRUE);
        }else{
            SwingUtilities.appContextPut(defaultLookAndFeelDecoratedKey,Boolean.FALSE);
        }
    }

    public JFrame(GraphicsConfiguration gc){
        super(gc);
        frameInit();
    }

    public JFrame(String title) throws HeadlessException{
        super(title);
        frameInit();
    }

    public JFrame(String title,GraphicsConfiguration gc){
        super(title,gc);
        frameInit();
    }

    protected void processWindowEvent(final WindowEvent e){
        super.processWindowEvent(e);
        if(e.getID()==WindowEvent.WINDOW_CLOSING){
            switch(defaultCloseOperation){
                case HIDE_ON_CLOSE:
                    setVisible(false);
                    break;
                case DISPOSE_ON_CLOSE:
                    dispose();
                    break;
                case EXIT_ON_CLOSE:
                    // This needs to match the checkExit call in
                    // setDefaultCloseOperation
                    System.exit(0);
                    break;
                case DO_NOTHING_ON_CLOSE:
                default:
            }
        }
    }

    public int getDefaultCloseOperation(){
        return defaultCloseOperation;
    }

    public void setDefaultCloseOperation(int operation){
        if(operation!=DO_NOTHING_ON_CLOSE&&
                operation!=HIDE_ON_CLOSE&&
                operation!=DISPOSE_ON_CLOSE&&
                operation!=EXIT_ON_CLOSE){
            throw new IllegalArgumentException("defaultCloseOperation must be one of: DO_NOTHING_ON_CLOSE, HIDE_ON_CLOSE, DISPOSE_ON_CLOSE, or EXIT_ON_CLOSE");
        }
        if(operation==EXIT_ON_CLOSE){
            SecurityManager security=System.getSecurityManager();
            if(security!=null){
                security.checkExit(0);
            }
        }
        if(this.defaultCloseOperation!=operation){
            int oldValue=this.defaultCloseOperation;
            this.defaultCloseOperation=operation;
            firePropertyChange("defaultCloseOperation",oldValue,operation);
        }
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
    }    protected boolean isRootPaneCheckingEnabled(){
        return rootPaneCheckingEnabled;
    }

    public void setJMenuBar(JMenuBar menubar){
        getRootPane().setMenuBar(menubar);
    }    protected void setRootPaneCheckingEnabled(boolean enabled){
        rootPaneCheckingEnabled=enabled;
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
    }    public JRootPane getRootPane(){
        return rootPane;
    }

    public void setIconImage(Image image){
        super.setIconImage(image);
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

    protected String paramString(){
        String defaultCloseOperationString;
        if(defaultCloseOperation==HIDE_ON_CLOSE){
            defaultCloseOperationString="HIDE_ON_CLOSE";
        }else if(defaultCloseOperation==DISPOSE_ON_CLOSE){
            defaultCloseOperationString="DISPOSE_ON_CLOSE";
        }else if(defaultCloseOperation==DO_NOTHING_ON_CLOSE){
            defaultCloseOperationString="DO_NOTHING_ON_CLOSE";
        }else if(defaultCloseOperation==3){
            defaultCloseOperationString="EXIT_ON_CLOSE";
        }else defaultCloseOperationString="";
        String rootPaneString=(rootPane!=null?
                rootPane.toString():"");
        String rootPaneCheckingEnabledString=(rootPaneCheckingEnabled?
                "true":"false");
        return super.paramString()+
                ",defaultCloseOperation="+defaultCloseOperationString+
                ",rootPane="+rootPaneString+
                ",rootPaneCheckingEnabled="+rootPaneCheckingEnabledString;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJFrame();
        }
        return accessibleContext;
    }    public Container getContentPane(){
        return getRootPane().getContentPane();
    }

    public Graphics getGraphics(){
        JComponent.getGraphicsInvoked(this);
        return super.getGraphics();
    }    public void setContentPane(Container contentPane){
        getRootPane().setContentPane(contentPane);
    }

    public void repaint(long time,int x,int y,int width,int height){
        if(RepaintManager.HANDLE_TOP_LEVEL_PAINT){
            RepaintManager.currentManager(this).addDirtyRegion(
                    this,x,y,width,height);
        }else{
            super.repaint(time,x,y,width,height);
        }
    }    public JLayeredPane getLayeredPane(){
        return getRootPane().getLayeredPane();
    }

    protected class AccessibleJFrame extends AccessibleAWTFrame{
        // AccessibleContext methods
        public String getAccessibleName(){
            if(accessibleName!=null){
                return accessibleName;
            }else{
                if(getTitle()==null){
                    return super.getAccessibleName();
                }else{
                    return getTitle();
                }
            }
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            if(isResizable()){
                states.add(AccessibleState.RESIZABLE);
            }
            if(getFocusOwner()!=null){
                states.add(AccessibleState.ACTIVE);
            }
            // FIXME:  [[[WDW - should also return ICONIFIED and ICONIFIABLE
            // if we can ever figure these out]]]
            return states;
        }
    } // inner class AccessibleJFrame    public void setLayeredPane(JLayeredPane layeredPane){
        getRootPane().setLayeredPane(layeredPane);
    }

    public Component getGlassPane(){
        return getRootPane().getGlassPane();
    }

    public void setGlassPane(Component glassPane){
        getRootPane().setGlassPane(glassPane);
    }















}
