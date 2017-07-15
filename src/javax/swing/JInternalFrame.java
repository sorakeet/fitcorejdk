/**
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.AppContext;
import sun.swing.SwingUtilities2;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.DesktopIconUI;
import javax.swing.plaf.InternalFrameUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JInternalFrame extends JComponent implements
        Accessible, WindowConstants,
        RootPaneContainer{
    public final static String CONTENT_PANE_PROPERTY="contentPane";
    public final static String MENU_BAR_PROPERTY="JMenuBar";
    public final static String TITLE_PROPERTY="title";
    public final static String LAYERED_PANE_PROPERTY="layeredPane";
    public final static String ROOT_PANE_PROPERTY="rootPane";
    public final static String GLASS_PANE_PROPERTY="glassPane";
    public final static String FRAME_ICON_PROPERTY="frameIcon";
    public final static String IS_SELECTED_PROPERTY="selected";
    public final static String IS_CLOSED_PROPERTY="closed";
    public final static String IS_MAXIMUM_PROPERTY="maximum";
    public final static String IS_ICON_PROPERTY="icon";
    private static final String uiClassID="InternalFrameUI";
    private static final Object PROPERTY_CHANGE_LISTENER_KEY=
            new StringBuilder("InternalFramePropertyChangeListener");
    protected JRootPane rootPane;
    protected boolean rootPaneCheckingEnabled=false;
    protected boolean closable;
    protected boolean isClosed;
    protected boolean maximizable;
    protected boolean isMaximum;
    protected boolean iconable;
    protected boolean isIcon;
    protected boolean resizable;
    protected boolean isSelected;
    protected Icon frameIcon;
    protected String title;
    protected JDesktopIcon desktopIcon;
    // ======= begin optimized frame dragging defence code ==============
    boolean isDragging=false;
    boolean danger=false;
    private Cursor lastCursor;
    private boolean opened;
    private Rectangle normalBounds=null;
    private int defaultCloseOperation=DISPOSE_ON_CLOSE;
    private Component lastFocusOwner;

    public JInternalFrame(){
        this("",false,false,false,false);
    }

    public JInternalFrame(String title){
        this(title,false,false,false,false);
    }

    public JInternalFrame(String title,boolean resizable){
        this(title,resizable,false,false,false);
    }

    public JInternalFrame(String title,boolean resizable,boolean closable){
        this(title,resizable,closable,false,false);
    }

    public JInternalFrame(String title,boolean resizable,boolean closable,
                          boolean maximizable){
        this(title,resizable,closable,maximizable,false);
    }

    public JInternalFrame(String title,boolean resizable,boolean closable,
                          boolean maximizable,boolean iconifiable){
        setRootPane(createRootPane());
        setLayout(new BorderLayout());
        this.title=title;
        this.resizable=resizable;
        this.closable=closable;
        this.maximizable=maximizable;
        isMaximum=false;
        this.iconable=iconifiable;
        isIcon=false;
        setVisible(false);
        setRootPaneCheckingEnabled(true);
        desktopIcon=new JDesktopIcon(this);
        updateUI();
        sun.awt.SunToolkit.checkAndSetPolicy(this);
        addPropertyChangeListenerIfNecessary();
    }

    private static void addPropertyChangeListenerIfNecessary(){
        if(AppContext.getAppContext().get(PROPERTY_CHANGE_LISTENER_KEY)==
                null){
            PropertyChangeListener focusListener=
                    new FocusPropertyChangeListener();
            AppContext.getAppContext().put(PROPERTY_CHANGE_LISTENER_KEY,
                    focusListener);
            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    addPropertyChangeListener(focusListener);
        }
    }

    private static void updateLastFocusOwner(Component component){
        if(component!=null){
            Component parent=component;
            while(parent!=null&&!(parent instanceof Window)){
                if(parent instanceof JInternalFrame){
                    // Update lastFocusOwner for parent.
                    ((JInternalFrame)parent).setLastFocusOwner(component);
                }
                parent=parent.getParent();
            }
        }
    }

    private void setLastFocusOwner(Component component){
        lastFocusOwner=component;
    }

    protected JRootPane createRootPane(){
        return new JRootPane();
    }

    public InternalFrameUI getUI(){
        return (InternalFrameUI)ui;
    }

    public void setUI(InternalFrameUI ui){
        boolean checkingEnabled=isRootPaneCheckingEnabled();
        try{
            setRootPaneCheckingEnabled(false);
            super.setUI(ui);
        }finally{
            setRootPaneCheckingEnabled(checkingEnabled);
        }
    }

    protected boolean isRootPaneCheckingEnabled(){
        return rootPaneCheckingEnabled;
    }    public String getUIClassID(){
        return uiClassID;
    }

    protected void setRootPaneCheckingEnabled(boolean enabled){
        rootPaneCheckingEnabled=enabled;
    }

    public void updateUI(){
        setUI((InternalFrameUI)UIManager.getUI(this));
        invalidate();
        if(desktopIcon!=null){
            desktopIcon.updateUIWhenHidden();
        }
    }

    void updateUIWhenHidden(){
        setUI((InternalFrameUI)UIManager.getUI(this));
        invalidate();
        Component[] children=getComponents();
        if(children!=null){
            for(Component child : children){
                SwingUtilities.updateComponentTreeUI(child);
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

    public void remove(Component comp){
        int oldCount=getComponentCount();
        super.remove(comp);
        if(oldCount==getComponentCount()){
            getContentPane().remove(comp);
        }
    }
//////////////////////////////////////////////////////////////////////////
/// Property Methods
//////////////////////////////////////////////////////////////////////////

    public void setLayout(LayoutManager manager){
        if(isRootPaneCheckingEnabled()){
            getContentPane().setLayout(manager);
        }else{
            super.setLayout(manager);
        }
    }    @Deprecated
    public JMenuBar getMenuBar(){
        return getRootPane().getMenuBar();
    }

    public JMenuBar getJMenuBar(){
        return getRootPane().getJMenuBar();
    }

    public void setJMenuBar(JMenuBar m){
        JMenuBar oldValue=getMenuBar();
        getRootPane().setJMenuBar(m);
        firePropertyChange(MENU_BAR_PROPERTY,oldValue,m);
    }    @Deprecated
    public void setMenuBar(JMenuBar m){
        JMenuBar oldValue=getMenuBar();
        getRootPane().setJMenuBar(m);
        firePropertyChange(MENU_BAR_PROPERTY,oldValue,m);
    }

    public boolean isClosable(){
        return closable;
    }

    public void setClosable(boolean b){
        Boolean oldValue=closable?Boolean.TRUE:Boolean.FALSE;
        Boolean newValue=b?Boolean.TRUE:Boolean.FALSE;
        closable=b;
        firePropertyChange("closable",oldValue,newValue);
    }    // implements javax.swing.RootPaneContainer
    public Container getContentPane(){
        return getRootPane().getContentPane();
    }

    public boolean isClosed(){
        return isClosed;
    }    public void setContentPane(Container c){
        Container oldValue=getContentPane();
        getRootPane().setContentPane(c);
        firePropertyChange(CONTENT_PANE_PROPERTY,oldValue,c);
    }

    public void setClosed(boolean b) throws PropertyVetoException{
        if(isClosed==b){
            return;
        }
        Boolean oldValue=isClosed?Boolean.TRUE:Boolean.FALSE;
        Boolean newValue=b?Boolean.TRUE:Boolean.FALSE;
        if(b){
            fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSING);
        }
        fireVetoableChange(IS_CLOSED_PROPERTY,oldValue,newValue);
        isClosed=b;
        if(isClosed){
            setVisible(false);
        }
        firePropertyChange(IS_CLOSED_PROPERTY,oldValue,newValue);
        if(isClosed){
            dispose();
        }else if(!opened){
            /** this bogus -- we haven't defined what
             setClosed(false) means. */
            //        fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_OPENED);
            //            opened = true;
        }
    }    public JLayeredPane getLayeredPane(){
        return getRootPane().getLayeredPane();
    }

    public boolean isResizable(){
        // don't allow resizing when maximized.
        return isMaximum?false:resizable;
    }    public void setLayeredPane(JLayeredPane layered){
        JLayeredPane oldValue=getLayeredPane();
        getRootPane().setLayeredPane(layered);
        firePropertyChange(LAYERED_PANE_PROPERTY,oldValue,layered);
    }

    public void setResizable(boolean b){
        Boolean oldValue=resizable?Boolean.TRUE:Boolean.FALSE;
        Boolean newValue=b?Boolean.TRUE:Boolean.FALSE;
        resizable=b;
        firePropertyChange("resizable",oldValue,newValue);
    }    public Component getGlassPane(){
        return getRootPane().getGlassPane();
    }

    public boolean isIconifiable(){
        return iconable;
    }    public void setGlassPane(Component glass){
        Component oldValue=getGlassPane();
        getRootPane().setGlassPane(glass);
        firePropertyChange(GLASS_PANE_PROPERTY,oldValue,glass);
    }

    public void setIconifiable(boolean b){
        Boolean oldValue=iconable?Boolean.TRUE:Boolean.FALSE;
        Boolean newValue=b?Boolean.TRUE:Boolean.FALSE;
        iconable=b;
        firePropertyChange("iconable",oldValue,newValue);
    }    public JRootPane getRootPane(){
        return rootPane;
    }

    public boolean isMaximizable(){
        return maximizable;
    }    protected void setRootPane(JRootPane root){
        if(rootPane!=null){
            remove(rootPane);
        }
        JRootPane oldValue=getRootPane();
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
        firePropertyChange(ROOT_PANE_PROPERTY,oldValue,root);
    }

    public void setMaximizable(boolean b){
        Boolean oldValue=maximizable?Boolean.TRUE:Boolean.FALSE;
        Boolean newValue=b?Boolean.TRUE:Boolean.FALSE;
        maximizable=b;
        firePropertyChange("maximizable",oldValue,newValue);
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        String oldValue=this.title;
        this.title=title;
        firePropertyChange(TITLE_PROPERTY,oldValue,title);
    }

    public boolean isSelected(){
        return isSelected;
    }

    public void setSelected(boolean selected) throws PropertyVetoException{
        // The InternalFrame may already be selected, but the focus
        // may be outside it, so restore the focus to the subcomponent
        // which previously had it. See Bug 4302764.
        if(selected&&isSelected){
            restoreSubcomponentFocus();
            return;
        }
        // The internal frame or the desktop icon must be showing to allow
        // selection.  We may deselect even if neither is showing.
        if((isSelected==selected)||(selected&&
                (isIcon?!desktopIcon.isShowing():!isShowing()))){
            return;
        }
        Boolean oldValue=isSelected?Boolean.TRUE:Boolean.FALSE;
        Boolean newValue=selected?Boolean.TRUE:Boolean.FALSE;
        fireVetoableChange(IS_SELECTED_PROPERTY,oldValue,newValue);
        /** We don't want to leave focus in the previously selected
         frame, so we have to set it to *something* in case it
         doesn't get set in some other way (as if a user clicked on
         a component that doesn't request focus).  If this call is
         happening because the user clicked on a component that will
         want focus, then it will get transfered there later.

         We test for parent.isShowing() above, because AWT throws a
         NPE if you try to request focus on a lightweight before its
         parent has been made visible */
        if(selected){
            restoreSubcomponentFocus();
        }
        isSelected=selected;
        firePropertyChange(IS_SELECTED_PROPERTY,oldValue,newValue);
        if(isSelected)
            fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_ACTIVATED);
        else
            fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED);
        repaint();
    }

    public Icon getFrameIcon(){
        return frameIcon;
    }

    public void setFrameIcon(Icon icon){
        Icon oldIcon=frameIcon;
        frameIcon=icon;
        firePropertyChange(FRAME_ICON_PROPERTY,oldIcon,icon);
    }

    public void moveToFront(){
        if(isIcon()){
            if(getDesktopIcon().getParent() instanceof JLayeredPane){
                ((JLayeredPane)getDesktopIcon().getParent()).
                        moveToFront(getDesktopIcon());
            }
        }else if(getParent() instanceof JLayeredPane){
            ((JLayeredPane)getParent()).moveToFront(this);
        }
    }

    public Cursor getLastCursor(){
        return lastCursor;
    }

    public int getLayer(){
        return JLayeredPane.getLayer(this);
    }

    public void setLayer(int layer){
        this.setLayer(Integer.valueOf(layer));
    }

    public void setLayer(Integer layer){
        if(getParent()!=null&&getParent() instanceof JLayeredPane){
            // Normally we want to do this, as it causes the LayeredPane
            // to draw properly.
            JLayeredPane p=(JLayeredPane)getParent();
            p.setLayer(this,layer.intValue(),p.getPosition(this));
        }else{
            // Try to do the right thing
            JLayeredPane.putLayer(this,layer.intValue());
            if(getParent()!=null)
                getParent().repaint(getX(),getY(),getWidth(),getHeight());
        }
    }

    public JDesktopPane getDesktopPane(){
        Container p;
        // Search upward for desktop
        p=getParent();
        while(p!=null&&!(p instanceof JDesktopPane))
            p=p.getParent();
        if(p==null){
            // search its icon parent for desktop
            p=getDesktopIcon().getParent();
            while(p!=null&&!(p instanceof JDesktopPane))
                p=p.getParent();
        }
        return (JDesktopPane)p;
    }

    public JDesktopIcon getDesktopIcon(){
        return desktopIcon;
    }

    public void setDesktopIcon(JDesktopIcon d){
        JDesktopIcon oldValue=getDesktopIcon();
        desktopIcon=d;
        firePropertyChange("desktopIcon",oldValue,d);
    }

    public Rectangle getNormalBounds(){
        /** we used to test (!isMaximum) here, but since this
         method is used by the property listener for the
         IS_MAXIMUM_PROPERTY, it ended up getting the wrong
         answer... Since normalBounds get set to null when the
         frame is restored, this should work better */
        if(normalBounds!=null){
            return normalBounds;
        }else{
            return getBounds();
        }
    }

    public void setNormalBounds(Rectangle r){
        normalBounds=r;
    }

    public Component getFocusOwner(){
        if(isSelected()){
            return lastFocusOwner;
        }
        return null;
    }

    public Component getMostRecentFocusOwner(){
        if(isSelected()){
            return getFocusOwner();
        }
        if(lastFocusOwner!=null){
            return lastFocusOwner;
        }
        FocusTraversalPolicy policy=getFocusTraversalPolicy();
        if(policy instanceof InternalFrameFocusTraversalPolicy){
            return ((InternalFrameFocusTraversalPolicy)policy).
                    getInitialComponent(this);
        }
        Component toFocus=policy.getDefaultComponent(this);
        if(toFocus!=null){
            return toFocus;
        }
        return getContentPane();
    }

    public void restoreSubcomponentFocus(){
        if(isIcon()){
            SwingUtilities2.compositeRequestFocus(getDesktopIcon());
        }else{
            Component component=KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
            if((component==null)||!SwingUtilities.isDescendingFrom(component,this)){
                // FocusPropertyChangeListener will eventually update
                // lastFocusOwner. As focus requests are asynchronous
                // lastFocusOwner may be accessed before it has been correctly
                // updated. To avoid any problems, lastFocusOwner is immediately
                // set, assuming the request will succeed.
                setLastFocusOwner(getMostRecentFocusOwner());
                if(lastFocusOwner==null){
                    // Make sure focus is restored somewhere, so that
                    // we don't leave a focused component in another frame while
                    // this frame is selected.
                    setLastFocusOwner(getContentPane());
                }
                lastFocusOwner.requestFocus();
            }
        }
    }

    public void addInternalFrameListener(InternalFrameListener l){  // remind: sync ??
        listenerList.add(InternalFrameListener.class,l);
        // remind: needed?
        enableEvents(0);   // turn on the newEventsOnly flag in Component.
    }

    public void removeInternalFrameListener(InternalFrameListener l){  // remind: sync??
        listenerList.remove(InternalFrameListener.class,l);
    }

    public InternalFrameListener[] getInternalFrameListeners(){
        return listenerList.getListeners(InternalFrameListener.class);
    }

    public void doDefaultCloseAction(){
        fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSING);
        switch(defaultCloseOperation){
            case DO_NOTHING_ON_CLOSE:
                break;
            case HIDE_ON_CLOSE:
                setVisible(false);
                if(isSelected())
                    try{
                        setSelected(false);
                    }catch(PropertyVetoException pve){
                    }
                /** should this activate the next frame? that's really
                 desktopmanager's policy... */
                break;
            case DISPOSE_ON_CLOSE:
                try{
                    fireVetoableChange(IS_CLOSED_PROPERTY,Boolean.FALSE,
                            Boolean.TRUE);
                    isClosed=true;
                    setVisible(false);
                    firePropertyChange(IS_CLOSED_PROPERTY,Boolean.FALSE,
                            Boolean.TRUE);
                    dispose();
                }catch(PropertyVetoException pve){
                }
                break;
            default:
                break;
        }
    }

    public int getDefaultCloseOperation(){
        return defaultCloseOperation;
    }

    public void setDefaultCloseOperation(int operation){
        this.defaultCloseOperation=operation;
    }

    public void pack(){
        try{
            if(isIcon()){
                setIcon(false);
            }else if(isMaximum()){
                setMaximum(false);
            }
        }catch(PropertyVetoException e){
            return;
        }
        setSize(getPreferredSize());
        validate();
    }

    public boolean isIcon(){
        return isIcon;
    }

    public void setIcon(boolean b) throws PropertyVetoException{
        if(isIcon==b){
            return;
        }
        /** If an internal frame is being iconified before it has a
         parent, (e.g., client wants it to start iconic), create the
         parent if possible so that we can place the icon in its
         proper place on the desktop. I am not sure the call to
         validate() is necessary, since we are not going to display
         this frame yet */
        firePropertyChange("ancestor",null,getParent());
        Boolean oldValue=isIcon?Boolean.TRUE:Boolean.FALSE;
        Boolean newValue=b?Boolean.TRUE:Boolean.FALSE;
        fireVetoableChange(IS_ICON_PROPERTY,oldValue,newValue);
        isIcon=b;
        firePropertyChange(IS_ICON_PROPERTY,oldValue,newValue);
        if(b)
            fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_ICONIFIED);
        else
            fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED);
    }

    // remind: name ok? all one method ok? need to be synchronized?
    protected void fireInternalFrameEvent(int id){
        Object[] listeners=listenerList.getListenerList();
        InternalFrameEvent e=null;
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==InternalFrameListener.class){
                if(e==null){
                    e=new InternalFrameEvent(this,id);
                    //      System.out.println("InternalFrameEvent: " + e.paramString());
                }
                switch(e.getID()){
                    case InternalFrameEvent.INTERNAL_FRAME_OPENED:
                        ((InternalFrameListener)listeners[i+1]).internalFrameOpened(e);
                        break;
                    case InternalFrameEvent.INTERNAL_FRAME_CLOSING:
                        ((InternalFrameListener)listeners[i+1]).internalFrameClosing(e);
                        break;
                    case InternalFrameEvent.INTERNAL_FRAME_CLOSED:
                        ((InternalFrameListener)listeners[i+1]).internalFrameClosed(e);
                        break;
                    case InternalFrameEvent.INTERNAL_FRAME_ICONIFIED:
                        ((InternalFrameListener)listeners[i+1]).internalFrameIconified(e);
                        break;
                    case InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED:
                        ((InternalFrameListener)listeners[i+1]).internalFrameDeiconified(e);
                        break;
                    case InternalFrameEvent.INTERNAL_FRAME_ACTIVATED:
                        ((InternalFrameListener)listeners[i+1]).internalFrameActivated(e);
                        break;
                    case InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED:
                        ((InternalFrameListener)listeners[i+1]).internalFrameDeactivated(e);
                        break;
                    default:
                        break;
                }
            }
        }
        /** we could do it off the event, but at the moment, that's not how
         I'm implementing it */
        //      if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING) {
        //          doDefaultCloseAction();
        //      }
    }

    public boolean isMaximum(){
        return isMaximum;
    }

    public void setMaximum(boolean b) throws PropertyVetoException{
        if(isMaximum==b){
            return;
        }
        Boolean oldValue=isMaximum?Boolean.TRUE:Boolean.FALSE;
        Boolean newValue=b?Boolean.TRUE:Boolean.FALSE;
        fireVetoableChange(IS_MAXIMUM_PROPERTY,oldValue,newValue);
        /** setting isMaximum above the event firing means that
         property listeners that, for some reason, test it will
         get it wrong... See, for example, getNormalBounds() */
        isMaximum=b;
        firePropertyChange(IS_MAXIMUM_PROPERTY,oldValue,newValue);
    }

    public void show(){
        // bug 4312922
        if(isVisible()){
            //match the behavior of setVisible(true): do nothing
            return;
        }
        // bug 4149505
        if(!opened){
            fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_OPENED);
            opened=true;
        }
        /** icon default visibility is false; set it to true so that it shows
         up when user iconifies frame */
        getDesktopIcon().setVisible(true);
        toFront();
        super.show();
        if(isIcon){
            return;
        }
        if(!isSelected()){
            try{
                setSelected(true);
            }catch(PropertyVetoException pve){
            }
        }
    }

    public void setCursor(Cursor cursor){
        if(cursor==null){
            lastCursor=null;
            super.setCursor(cursor);
            return;
        }
        int type=cursor.getType();
        if(!(type==Cursor.SW_RESIZE_CURSOR||
                type==Cursor.SE_RESIZE_CURSOR||
                type==Cursor.NW_RESIZE_CURSOR||
                type==Cursor.NE_RESIZE_CURSOR||
                type==Cursor.N_RESIZE_CURSOR||
                type==Cursor.S_RESIZE_CURSOR||
                type==Cursor.W_RESIZE_CURSOR||
                type==Cursor.E_RESIZE_CURSOR)){
            lastCursor=cursor;
        }
        super.setCursor(cursor);
    }

    public final Container getFocusCycleRootAncestor(){
        return null;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJInternalFrame();
        }
        return accessibleContext;
    }

    public void dispose(){
        if(isVisible()){
            setVisible(false);
        }
        if(isSelected()){
            try{
                setSelected(false);
            }catch(PropertyVetoException pve){
            }
        }
        if(!isClosed){
            firePropertyChange(IS_CLOSED_PROPERTY,Boolean.FALSE,Boolean.TRUE);
            isClosed=true;
        }
        fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSED);
    }    public void reshape(int x,int y,int width,int height){
        super.reshape(x,y,width,height);
        validate();
        repaint();
    }
///////////////////////////
// Frame/Window equivalents
///////////////////////////

    public void toFront(){
        moveToFront();
    }

    public void toBack(){
        moveToBack();
    }

    public void moveToBack(){
        if(isIcon()){
            if(getDesktopIcon().getParent() instanceof JLayeredPane){
                ((JLayeredPane)getDesktopIcon().getParent()).
                        moveToBack(getDesktopIcon());
            }
        }else if(getParent() instanceof JLayeredPane){
            ((JLayeredPane)getParent()).moveToBack(this);
        }
    }

    public final String getWarningString(){
        return null;
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                boolean old=isRootPaneCheckingEnabled();
                try{
                    setRootPaneCheckingEnabled(false);
                    ui.installUI(this);
                }finally{
                    setRootPaneCheckingEnabled(old);
                }
            }
        }
    }

    private static class FocusPropertyChangeListener implements
            PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent e){
            if(e.getPropertyName()=="permanentFocusOwner"){
                updateLastFocusOwner((Component)e.getNewValue());
            }
        }
    }

    static public class JDesktopIcon extends JComponent implements Accessible{
        JInternalFrame internalFrame;

        public JDesktopIcon(JInternalFrame f){
            setVisible(false);
            setInternalFrame(f);
            updateUI();
        }

        public DesktopIconUI getUI(){
            return (DesktopIconUI)ui;
        }

        public void setUI(DesktopIconUI ui){
            super.setUI(ui);
        }

        public JDesktopPane getDesktopPane(){
            if(getInternalFrame()!=null)
                return getInternalFrame().getDesktopPane();
            return null;
        }

        public JInternalFrame getInternalFrame(){
            return internalFrame;
        }

        public void setInternalFrame(JInternalFrame f){
            internalFrame=f;
        }

        void updateUIWhenHidden(){
            /** Update this UI and any associated internal frame */
            setUI((DesktopIconUI)UIManager.getUI(this));
            Dimension r=getPreferredSize();
            setSize(r.width,r.height);
            invalidate();
            Component[] children=getComponents();
            if(children!=null){
                for(Component child : children){
                    SwingUtilities.updateComponentTreeUI(child);
                }
            }
        }        public void updateUI(){
            boolean hadUI=(ui!=null);
            setUI((DesktopIconUI)UIManager.getUI(this));
            invalidate();
            Dimension r=getPreferredSize();
            setSize(r.width,r.height);
            if(internalFrame!=null&&internalFrame.getUI()!=null){  // don't do this if UI not created yet
                SwingUtilities.updateComponentTreeUI(internalFrame);
            }
        }

        ////////////////
        // Serialization support
        ////////////////
        private void writeObject(ObjectOutputStream s) throws IOException{
            s.defaultWriteObject();
            if(getUIClassID().equals("DesktopIconUI")){
                byte count=JComponent.getWriteObjCounter(this);
                JComponent.setWriteObjCounter(this,--count);
                if(count==0&&ui!=null){
                    ui.installUI(this);
                }
            }
        }

        protected class AccessibleJDesktopIcon extends AccessibleJComponent
                implements AccessibleValue{
            public AccessibleRole getAccessibleRole(){
                return AccessibleRole.DESKTOP_ICON;
            }

            public AccessibleValue getAccessibleValue(){
                return this;
            }
            //
            // AccessibleValue methods
            //

            public Number getCurrentAccessibleValue(){
                AccessibleContext a=JDesktopIcon.this.getInternalFrame().getAccessibleContext();
                AccessibleValue v=a.getAccessibleValue();
                if(v!=null){
                    return v.getCurrentAccessibleValue();
                }else{
                    return null;
                }
            }

            public boolean setCurrentAccessibleValue(Number n){
                // TIGER - 4422535
                if(n==null){
                    return false;
                }
                AccessibleContext a=JDesktopIcon.this.getInternalFrame().getAccessibleContext();
                AccessibleValue v=a.getAccessibleValue();
                if(v!=null){
                    return v.setCurrentAccessibleValue(n);
                }else{
                    return false;
                }
            }

            public Number getMinimumAccessibleValue(){
                AccessibleContext a=JDesktopIcon.this.getInternalFrame().getAccessibleContext();
                if(a instanceof AccessibleValue){
                    return ((AccessibleValue)a).getMinimumAccessibleValue();
                }else{
                    return null;
                }
            }

            public Number getMaximumAccessibleValue(){
                AccessibleContext a=JDesktopIcon.this.getInternalFrame().getAccessibleContext();
                if(a instanceof AccessibleValue){
                    return ((AccessibleValue)a).getMaximumAccessibleValue();
                }else{
                    return null;
                }
            }
        } // AccessibleJDesktopIcon        public String getUIClassID(){
            return "DesktopIconUI";
        }


        /////////////////
        // Accessibility support
        ////////////////

        public AccessibleContext getAccessibleContext(){
            if(accessibleContext==null){
                accessibleContext=new AccessibleJDesktopIcon();
            }
            return accessibleContext;
        }


    }

    protected class AccessibleJInternalFrame extends AccessibleJComponent
            implements AccessibleValue{
        public String getAccessibleName(){
            String name=accessibleName;
            if(name==null){
                name=(String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
            }
            if(name==null){
                name=getTitle();
            }
            return name;
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.INTERNAL_FRAME;
        }

        public AccessibleValue getAccessibleValue(){
            return this;
        }
        //
        // AccessibleValue methods
        //

        public Number getCurrentAccessibleValue(){
            return Integer.valueOf(getLayer());
        }

        public boolean setCurrentAccessibleValue(Number n){
            // TIGER - 4422535
            if(n==null){
                return false;
            }
            setLayer(new Integer(n.intValue()));
            return true;
        }

        public Number getMinimumAccessibleValue(){
            return Integer.MIN_VALUE;
        }

        public Number getMaximumAccessibleValue(){
            return Integer.MAX_VALUE;
        }
    } // AccessibleJInternalFrame



    public void hide(){
        if(isIcon()){
            getDesktopIcon().setVisible(false);
        }
        super.hide();
    }







    public final void setFocusCycleRoot(boolean focusCycleRoot){
    }

    public final boolean isFocusCycleRoot(){
        return true;
    }







    void compWriteObjectNotify(){
        // need to disable rootpane checking for InternalFrame: 4172083
        boolean old=isRootPaneCheckingEnabled();
        try{
            setRootPaneCheckingEnabled(false);
            super.compWriteObjectNotify();
        }finally{
            setRootPaneCheckingEnabled(old);
        }
    }

    protected String paramString(){
        String rootPaneString=(rootPane!=null?
                rootPane.toString():"");
        String rootPaneCheckingEnabledString=(rootPaneCheckingEnabled?
                "true":"false");
        String closableString=(closable?"true":"false");
        String isClosedString=(isClosed?"true":"false");
        String maximizableString=(maximizable?"true":"false");
        String isMaximumString=(isMaximum?"true":"false");
        String iconableString=(iconable?"true":"false");
        String isIconString=(isIcon?"true":"false");
        String resizableString=(resizable?"true":"false");
        String isSelectedString=(isSelected?"true":"false");
        String frameIconString=(frameIcon!=null?
                frameIcon.toString():"");
        String titleString=(title!=null?
                title:"");
        String desktopIconString=(desktopIcon!=null?
                desktopIcon.toString():"");
        String openedString=(opened?"true":"false");
        String defaultCloseOperationString;
        if(defaultCloseOperation==HIDE_ON_CLOSE){
            defaultCloseOperationString="HIDE_ON_CLOSE";
        }else if(defaultCloseOperation==DISPOSE_ON_CLOSE){
            defaultCloseOperationString="DISPOSE_ON_CLOSE";
        }else if(defaultCloseOperation==DO_NOTHING_ON_CLOSE){
            defaultCloseOperationString="DO_NOTHING_ON_CLOSE";
        }else defaultCloseOperationString="";
        return super.paramString()+
                ",closable="+closableString+
                ",defaultCloseOperation="+defaultCloseOperationString+
                ",desktopIcon="+desktopIconString+
                ",frameIcon="+frameIconString+
                ",iconable="+iconableString+
                ",isClosed="+isClosedString+
                ",isIcon="+isIconString+
                ",isMaximum="+isMaximumString+
                ",isSelected="+isSelectedString+
                ",maximizable="+maximizableString+
                ",opened="+openedString+
                ",resizable="+resizableString+
                ",rootPane="+rootPaneString+
                ",rootPaneCheckingEnabled="+rootPaneCheckingEnabledString+
                ",title="+titleString;
    }



    protected void paintComponent(Graphics g){
        if(isDragging){
            //         System.out.println("ouch");
            danger=true;
        }
        super.paintComponent(g);
    }
    // ======= end optimized frame dragging defence code ==============
/////////////////
// Accessibility support
////////////////






}
