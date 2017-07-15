/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.swing.DefaultLookup;
import sun.swing.UIAction;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolBarUI;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Hashtable;

public class BasicToolBarUI extends ToolBarUI implements SwingConstants{
    // Rollover button implementation.
    private static String IS_ROLLOVER="JToolBar.isRollover";
    private static Border rolloverBorder;
    private static Border nonRolloverBorder;
    private static Border nonRolloverToggleBorder;
    private static String FOCUSED_COMP_INDEX="JToolBar.focusedCompIndex";
    protected JToolBar toolBar;
    protected DragWindow dragWindow;
    protected int focusedCompIndex=-1;
    protected Color dockingColor=null;
    protected Color floatingColor=null;
    protected Color dockingBorderColor=null;
    protected Color floatingBorderColor=null;
    protected MouseInputListener dockingListener;
    protected PropertyChangeListener propertyListener;
    protected ContainerListener toolBarContListener;
    protected FocusListener toolBarFocusListener;
    protected String constraintBeforeFloating=BorderLayout.NORTH;
    @Deprecated
    protected KeyStroke upKey;
    @Deprecated
    protected KeyStroke downKey;
    @Deprecated
    protected KeyStroke leftKey;
    @Deprecated
    protected KeyStroke rightKey;
    private boolean floating;
    private int floatingX;
    private int floatingY;
    private JFrame floatingFrame;
    private RootPaneContainer floatingToolBar;
    private Container dockingSource;
    private int dockingSensitivity=0;
    private Handler handler;
    private boolean rolloverBorders=false;
    private HashMap<AbstractButton,Border> borderTable=new HashMap<AbstractButton,Border>();
    private Hashtable<AbstractButton,Boolean> rolloverTable=new Hashtable<AbstractButton,Boolean>();

    public static ComponentUI createUI(JComponent c){
        return new BasicToolBarUI();
    }

    static void loadActionMap(LazyActionMap map){
        map.put(new Actions(Actions.NAVIGATE_RIGHT));
        map.put(new Actions(Actions.NAVIGATE_LEFT));
        map.put(new Actions(Actions.NAVIGATE_UP));
        map.put(new Actions(Actions.NAVIGATE_DOWN));
    }

    public void installUI(JComponent c){
        toolBar=(JToolBar)c;
        // Set defaults
        installDefaults();
        installComponents();
        installListeners();
        installKeyboardActions();
        // Initialize instance vars
        dockingSensitivity=0;
        floating=false;
        floatingX=floatingY=0;
        floatingToolBar=null;
        setOrientation(toolBar.getOrientation());
        LookAndFeel.installProperty(c,"opaque",Boolean.TRUE);
        if(c.getClientProperty(FOCUSED_COMP_INDEX)!=null){
            focusedCompIndex=((Integer)(c.getClientProperty(FOCUSED_COMP_INDEX))).intValue();
        }
    }

    public void uninstallUI(JComponent c){
        // Clear defaults
        uninstallDefaults();
        uninstallComponents();
        uninstallListeners();
        uninstallKeyboardActions();
        // Clear instance vars
        if(isFloating())
            setFloating(false,null);
        floatingToolBar=null;
        dragWindow=null;
        dockingSource=null;
        c.putClientProperty(FOCUSED_COMP_INDEX,Integer.valueOf(focusedCompIndex));
    }

    protected void uninstallDefaults(){
        LookAndFeel.uninstallBorder(toolBar);
        dockingColor=null;
        floatingColor=null;
        dockingBorderColor=null;
        floatingBorderColor=null;
        installNormalBorders(toolBar);
        rolloverBorder=null;
        nonRolloverBorder=null;
        nonRolloverToggleBorder=null;
    }

    protected void installNormalBorders(JComponent c){
        // Put back the normal borders on buttons
        Component[] components=c.getComponents();
        for(Component component : components){
            setBorderToNormal(component);
        }
    }

    protected void setBorderToNormal(Component c){
        if(c instanceof AbstractButton){
            AbstractButton b=(AbstractButton)c;
            Border border=borderTable.remove(b);
            b.setBorder(border);
            Boolean value=rolloverTable.remove(b);
            if(value!=null){
                b.setRolloverEnabled(value.booleanValue());
            }
        }
    }

    protected void uninstallComponents(){
    }

    protected void uninstallListeners(){
        if(dockingListener!=null){
            toolBar.removeMouseMotionListener(dockingListener);
            toolBar.removeMouseListener(dockingListener);
            dockingListener=null;
        }
        if(propertyListener!=null){
            toolBar.removePropertyChangeListener(propertyListener);
            propertyListener=null;  // removed in setFloating
        }
        if(toolBarContListener!=null){
            toolBar.removeContainerListener(toolBarContListener);
            toolBarContListener=null;
        }
        if(toolBarFocusListener!=null){
            // Remove focus listener from all components in toolbar
            Component[] components=toolBar.getComponents();
            for(Component component : components){
                component.removeFocusListener(toolBarFocusListener);
            }
            toolBarFocusListener=null;
        }
        handler=null;
    }

    protected void uninstallKeyboardActions(){
        SwingUtilities.replaceUIActionMap(toolBar,null);
        SwingUtilities.replaceUIInputMap(toolBar,JComponent.
                        WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                null);
    }

    public boolean isFloating(){
        return floating;
    }

    public void setFloating(boolean b,Point p){
        if(toolBar.isFloatable()){
            boolean visible=false;
            Window ancestor=SwingUtilities.getWindowAncestor(toolBar);
            if(ancestor!=null){
                visible=ancestor.isVisible();
            }
            if(dragWindow!=null)
                dragWindow.setVisible(false);
            this.floating=b;
            if(floatingToolBar==null){
                floatingToolBar=createFloatingWindow(toolBar);
            }
            if(b==true){
                if(dockingSource==null){
                    dockingSource=toolBar.getParent();
                    dockingSource.remove(toolBar);
                }
                constraintBeforeFloating=calculateConstraint();
                if(propertyListener!=null)
                    UIManager.addPropertyChangeListener(propertyListener);
                floatingToolBar.getContentPane().add(toolBar,BorderLayout.CENTER);
                if(floatingToolBar instanceof Window){
                    ((Window)floatingToolBar).pack();
                    ((Window)floatingToolBar).setLocation(floatingX,floatingY);
                    if(visible){
                        ((Window)floatingToolBar).show();
                    }else{
                        ancestor.addWindowListener(new WindowAdapter(){
                            public void windowOpened(WindowEvent e){
                                ((Window)floatingToolBar).show();
                            }
                        });
                    }
                }
            }else{
                if(floatingToolBar==null)
                    floatingToolBar=createFloatingWindow(toolBar);
                if(floatingToolBar instanceof Window) ((Window)floatingToolBar).setVisible(false);
                floatingToolBar.getContentPane().remove(toolBar);
                String constraint=getDockingConstraint(dockingSource,
                        p);
                if(constraint==null){
                    constraint=BorderLayout.NORTH;
                }
                int orientation=mapConstraintToOrientation(constraint);
                setOrientation(orientation);
                if(dockingSource==null)
                    dockingSource=toolBar.getParent();
                if(propertyListener!=null)
                    UIManager.removePropertyChangeListener(propertyListener);
                dockingSource.add(constraint,toolBar);
            }
            dockingSource.invalidate();
            Container dockingSourceParent=dockingSource.getParent();
            if(dockingSourceParent!=null)
                dockingSourceParent.validate();
            dockingSource.repaint();
        }
    }

    protected RootPaneContainer createFloatingWindow(JToolBar toolbar){
        class ToolBarDialog extends JDialog{
            public ToolBarDialog(Frame owner,String title,boolean modal){
                super(owner,title,modal);
            }

            public ToolBarDialog(Dialog owner,String title,boolean modal){
                super(owner,title,modal);
            }

            // Override createRootPane() to automatically resize
            // the frame when contents change
            protected JRootPane createRootPane(){
                JRootPane rootPane=new JRootPane(){
                    private boolean packing=false;

                    public void validate(){
                        super.validate();
                        if(!packing){
                            packing=true;
                            pack();
                            packing=false;
                        }
                    }
                };
                rootPane.setOpaque(true);
                return rootPane;
            }
        }
        JDialog dialog;
        Window window=SwingUtilities.getWindowAncestor(toolbar);
        if(window instanceof Frame){
            dialog=new ToolBarDialog((Frame)window,toolbar.getName(),false);
        }else if(window instanceof Dialog){
            dialog=new ToolBarDialog((Dialog)window,toolbar.getName(),false);
        }else{
            dialog=new ToolBarDialog((Frame)null,toolbar.getName(),false);
        }
        dialog.getRootPane().setName("ToolBar.FloatingWindow");
        dialog.setTitle(toolbar.getName());
        dialog.setResizable(false);
        WindowListener wl=createFrameListener();
        dialog.addWindowListener(wl);
        return dialog;
    }

    protected WindowListener createFrameListener(){
        return new FrameListener();
    }

    private int mapConstraintToOrientation(String constraint){
        int orientation=toolBar.getOrientation();
        if(constraint!=null){
            if(constraint.equals(BorderLayout.EAST)||constraint.equals(BorderLayout.WEST))
                orientation=JToolBar.VERTICAL;
            else if(constraint.equals(BorderLayout.NORTH)||constraint.equals(BorderLayout.SOUTH))
                orientation=JToolBar.HORIZONTAL;
        }
        return orientation;
    }

    private String calculateConstraint(){
        String constraint=null;
        LayoutManager lm=dockingSource.getLayout();
        if(lm instanceof BorderLayout){
            constraint=(String)((BorderLayout)lm).getConstraints(toolBar);
        }
        return (constraint!=null)?constraint:constraintBeforeFloating;
    }

    private String getDockingConstraint(Component c,Point p){
        if(p==null) return constraintBeforeFloating;
        if(c.contains(p)){
            dockingSensitivity=(toolBar.getOrientation()==JToolBar.HORIZONTAL)
                    ?toolBar.getSize().height
                    :toolBar.getSize().width;
            // North  (Base distance on height for now!)
            if(p.y<dockingSensitivity&&!isBlocked(c,BorderLayout.NORTH)){
                return BorderLayout.NORTH;
            }
            // East  (Base distance on height for now!)
            if(p.x>=c.getWidth()-dockingSensitivity&&!isBlocked(c,BorderLayout.EAST)){
                return BorderLayout.EAST;
            }
            // West  (Base distance on height for now!)
            if(p.x<dockingSensitivity&&!isBlocked(c,BorderLayout.WEST)){
                return BorderLayout.WEST;
            }
            if(p.y>=c.getHeight()-dockingSensitivity&&!isBlocked(c,BorderLayout.SOUTH)){
                return BorderLayout.SOUTH;
            }
        }
        return null;
    }

    private boolean isBlocked(Component comp,Object constraint){
        if(comp instanceof Container){
            Container cont=(Container)comp;
            LayoutManager lm=cont.getLayout();
            if(lm instanceof BorderLayout){
                BorderLayout blm=(BorderLayout)lm;
                Component c=blm.getLayoutComponent(cont,constraint);
                return (c!=null&&c!=toolBar);
            }
        }
        return false;
    }

    protected void installDefaults(){
        LookAndFeel.installBorder(toolBar,"ToolBar.border");
        LookAndFeel.installColorsAndFont(toolBar,
                "ToolBar.background",
                "ToolBar.foreground",
                "ToolBar.font");
        // Toolbar specific defaults
        if(dockingColor==null||dockingColor instanceof UIResource)
            dockingColor=UIManager.getColor("ToolBar.dockingBackground");
        if(floatingColor==null||floatingColor instanceof UIResource)
            floatingColor=UIManager.getColor("ToolBar.floatingBackground");
        if(dockingBorderColor==null||
                dockingBorderColor instanceof UIResource)
            dockingBorderColor=UIManager.getColor("ToolBar.dockingForeground");
        if(floatingBorderColor==null||
                floatingBorderColor instanceof UIResource)
            floatingBorderColor=UIManager.getColor("ToolBar.floatingForeground");
        // ToolBar rollover button borders
        Object rolloverProp=toolBar.getClientProperty(IS_ROLLOVER);
        if(rolloverProp==null){
            rolloverProp=UIManager.get("ToolBar.isRollover");
        }
        if(rolloverProp!=null){
            rolloverBorders=((Boolean)rolloverProp).booleanValue();
        }
        if(rolloverBorder==null){
            rolloverBorder=createRolloverBorder();
        }
        if(nonRolloverBorder==null){
            nonRolloverBorder=createNonRolloverBorder();
        }
        if(nonRolloverToggleBorder==null){
            nonRolloverToggleBorder=createNonRolloverToggleBorder();
        }
        setRolloverBorders(isRolloverBorders());
    }

    protected Border createRolloverBorder(){
        Object border=UIManager.get("ToolBar.rolloverBorder");
        if(border!=null){
            return (Border)border;
        }
        UIDefaults table=UIManager.getLookAndFeelDefaults();
        return new CompoundBorder(new BasicBorders.RolloverButtonBorder(
                table.getColor("controlShadow"),
                table.getColor("controlDkShadow"),
                table.getColor("controlHighlight"),
                table.getColor("controlLtHighlight")),
                new BasicBorders.RolloverMarginBorder());
    }

    protected Border createNonRolloverBorder(){
        Object border=UIManager.get("ToolBar.nonrolloverBorder");
        if(border!=null){
            return (Border)border;
        }
        UIDefaults table=UIManager.getLookAndFeelDefaults();
        return new CompoundBorder(new BasicBorders.ButtonBorder(
                table.getColor("Button.shadow"),
                table.getColor("Button.darkShadow"),
                table.getColor("Button.light"),
                table.getColor("Button.highlight")),
                new BasicBorders.RolloverMarginBorder());
    }

    private Border createNonRolloverToggleBorder(){
        UIDefaults table=UIManager.getLookAndFeelDefaults();
        return new CompoundBorder(new BasicBorders.RadioButtonBorder(
                table.getColor("ToggleButton.shadow"),
                table.getColor("ToggleButton.darkShadow"),
                table.getColor("ToggleButton.light"),
                table.getColor("ToggleButton.highlight")),
                new BasicBorders.RolloverMarginBorder());
    }

    public boolean isRolloverBorders(){
        return rolloverBorders;
    }

    public void setRolloverBorders(boolean rollover){
        rolloverBorders=rollover;
        if(rolloverBorders){
            installRolloverBorders(toolBar);
        }else{
            installNonRolloverBorders(toolBar);
        }
    }

    protected void installRolloverBorders(JComponent c){
        // Put rollover borders on buttons
        Component[] components=c.getComponents();
        for(Component component : components){
            if(component instanceof JComponent){
                ((JComponent)component).updateUI();
                setBorderToRollover(component);
            }
        }
    }

    protected void setBorderToRollover(Component c){
        if(c instanceof AbstractButton){
            AbstractButton b=(AbstractButton)c;
            Border border=borderTable.get(b);
            if(border==null||border instanceof UIResource){
                borderTable.put(b,b.getBorder());
            }
            // Only set the border if its the default border
            if(b.getBorder() instanceof UIResource){
                b.setBorder(getRolloverBorder(b));
            }
            rolloverTable.put(b,b.isRolloverEnabled()?
                    Boolean.TRUE:Boolean.FALSE);
            b.setRolloverEnabled(true);
        }
    }

    protected Border getRolloverBorder(AbstractButton b){
        return rolloverBorder;
    }

    protected void installNonRolloverBorders(JComponent c){
        // Put non-rollover borders on buttons. These borders reduce the margin.
        Component[] components=c.getComponents();
        for(Component component : components){
            if(component instanceof JComponent){
                ((JComponent)component).updateUI();
                setBorderToNonRollover(component);
            }
        }
    }

    protected void setBorderToNonRollover(Component c){
        if(c instanceof AbstractButton){
            AbstractButton b=(AbstractButton)c;
            Border border=borderTable.get(b);
            if(border==null||border instanceof UIResource){
                borderTable.put(b,b.getBorder());
            }
            // Only set the border if its the default border
            if(b.getBorder() instanceof UIResource){
                b.setBorder(getNonRolloverBorder(b));
            }
            rolloverTable.put(b,b.isRolloverEnabled()?
                    Boolean.TRUE:Boolean.FALSE);
            b.setRolloverEnabled(false);
        }
    }

    protected Border getNonRolloverBorder(AbstractButton b){
        if(b instanceof JToggleButton){
            return nonRolloverToggleBorder;
        }else{
            return nonRolloverBorder;
        }
    }

    protected void installComponents(){
    }

    protected void installListeners(){
        dockingListener=createDockingListener();
        if(dockingListener!=null){
            toolBar.addMouseMotionListener(dockingListener);
            toolBar.addMouseListener(dockingListener);
        }
        propertyListener=createPropertyListener();  // added in setFloating
        if(propertyListener!=null){
            toolBar.addPropertyChangeListener(propertyListener);
        }
        toolBarContListener=createToolBarContListener();
        if(toolBarContListener!=null){
            toolBar.addContainerListener(toolBarContListener);
        }
        toolBarFocusListener=createToolBarFocusListener();
        if(toolBarFocusListener!=null){
            // Put focus listener on all components in toolbar
            Component[] components=toolBar.getComponents();
            for(Component component : components){
                component.addFocusListener(toolBarFocusListener);
            }
        }
    }

    protected ContainerListener createToolBarContListener(){
        return getHandler();
    }

    protected FocusListener createToolBarFocusListener(){
        return getHandler();
    }

    protected PropertyChangeListener createPropertyListener(){
        return getHandler();
    }

    protected MouseInputListener createDockingListener(){
        getHandler().tb=toolBar;
        return getHandler();
    }

    private Handler getHandler(){
        if(handler==null){
            handler=new Handler();
        }
        return handler;
    }

    protected void installKeyboardActions(){
        InputMap km=getInputMap(JComponent.
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(toolBar,JComponent.
                        WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                km);
        LazyActionMap.installLazyActionMap(toolBar,BasicToolBarUI.class,
                "ToolBar.actionMap");
    }

    InputMap getInputMap(int condition){
        if(condition==JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT){
            return (InputMap)DefaultLookup.get(toolBar,this,
                    "ToolBar.ancestorInputMap");
        }
        return null;
    }

    public void setOrientation(int orientation){
        toolBar.setOrientation(orientation);
        if(dragWindow!=null)
            dragWindow.setOrientation(orientation);
    }

    protected void navigateFocusedComp(int direction){
        int nComp=toolBar.getComponentCount();
        int j;
        switch(direction){
            case EAST:
            case SOUTH:
                if(focusedCompIndex<0||focusedCompIndex>=nComp) break;
                j=focusedCompIndex+1;
                while(j!=focusedCompIndex){
                    if(j>=nComp) j=0;
                    Component comp=toolBar.getComponentAtIndex(j++);
                    if(comp!=null&&comp.isFocusTraversable()&&comp.isEnabled()){
                        comp.requestFocus();
                        break;
                    }
                }
                break;
            case WEST:
            case NORTH:
                if(focusedCompIndex<0||focusedCompIndex>=nComp) break;
                j=focusedCompIndex-1;
                while(j!=focusedCompIndex){
                    if(j<0) j=nComp-1;
                    Component comp=toolBar.getComponentAtIndex(j--);
                    if(comp!=null&&comp.isFocusTraversable()&&comp.isEnabled()){
                        comp.requestFocus();
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }

    protected JFrame createFloatingFrame(JToolBar toolbar){
        Window window=SwingUtilities.getWindowAncestor(toolbar);
        JFrame frame=new JFrame(toolbar.getName(),
                (window!=null)?window.getGraphicsConfiguration():null){
            // Override createRootPane() to automatically resize
            // the frame when contents change
            protected JRootPane createRootPane(){
                JRootPane rootPane=new JRootPane(){
                    private boolean packing=false;

                    public void validate(){
                        super.validate();
                        if(!packing){
                            packing=true;
                            pack();
                            packing=false;
                        }
                    }
                };
                rootPane.setOpaque(true);
                return rootPane;
            }
        };
        frame.getRootPane().setName("ToolBar.FloatingFrame");
        frame.setResizable(false);
        WindowListener wl=createFrameListener();
        frame.addWindowListener(wl);
        return frame;
    }

    protected DragWindow createDragWindow(JToolBar toolbar){
        Window frame=null;
        if(toolBar!=null){
            Container p;
            for(p=toolBar.getParent();p!=null&&!(p instanceof Window);
                p=p.getParent())
                ;
            if(p!=null&&p instanceof Window)
                frame=(Window)p;
        }
        if(floatingToolBar==null){
            floatingToolBar=createFloatingWindow(toolBar);
        }
        if(floatingToolBar instanceof Window) frame=(Window)floatingToolBar;
        DragWindow dragWindow=new DragWindow(frame);
        return dragWindow;
    }

    public void setFloatingLocation(int x,int y){
        floatingX=x;
        floatingY=y;
    }

    public Color getDockingColor(){
        return dockingColor;
    }

    public void setDockingColor(Color c){
        this.dockingColor=c;
    }

    public Color getFloatingColor(){
        return floatingColor;
    }

    public void setFloatingColor(Color c){
        this.floatingColor=c;
    }

    public boolean canDock(Component c,Point p){
        return (p!=null&&getDockingConstraint(c,p)!=null);
    }

    protected void dragTo(Point position,Point origin){
        if(toolBar.isFloatable()){
            try{
                if(dragWindow==null)
                    dragWindow=createDragWindow(toolBar);
                Point offset=dragWindow.getOffset();
                if(offset==null){
                    Dimension size=toolBar.getPreferredSize();
                    offset=new Point(size.width/2,size.height/2);
                    dragWindow.setOffset(offset);
                }
                Point global=new Point(origin.x+position.x,
                        origin.y+position.y);
                Point dragPoint=new Point(global.x-offset.x,
                        global.y-offset.y);
                if(dockingSource==null)
                    dockingSource=toolBar.getParent();
                constraintBeforeFloating=calculateConstraint();
                Point dockingPosition=dockingSource.getLocationOnScreen();
                Point comparisonPoint=new Point(global.x-dockingPosition.x,
                        global.y-dockingPosition.y);
                if(canDock(dockingSource,comparisonPoint)){
                    dragWindow.setBackground(getDockingColor());
                    String constraint=getDockingConstraint(dockingSource,
                            comparisonPoint);
                    int orientation=mapConstraintToOrientation(constraint);
                    dragWindow.setOrientation(orientation);
                    dragWindow.setBorderColor(dockingBorderColor);
                }else{
                    dragWindow.setBackground(getFloatingColor());
                    dragWindow.setBorderColor(floatingBorderColor);
                    dragWindow.setOrientation(toolBar.getOrientation());
                }
                dragWindow.setLocation(dragPoint.x,dragPoint.y);
                if(dragWindow.isVisible()==false){
                    Dimension size=toolBar.getPreferredSize();
                    dragWindow.setSize(size.width,size.height);
                    dragWindow.show();
                }
            }catch(IllegalComponentStateException e){
            }
        }
    }

    protected void floatAt(Point position,Point origin){
        if(toolBar.isFloatable()){
            try{
                Point offset=dragWindow.getOffset();
                if(offset==null){
                    offset=position;
                    dragWindow.setOffset(offset);
                }
                Point global=new Point(origin.x+position.x,
                        origin.y+position.y);
                setFloatingLocation(global.x-offset.x,
                        global.y-offset.y);
                if(dockingSource!=null){
                    Point dockingPosition=dockingSource.getLocationOnScreen();
                    Point comparisonPoint=new Point(global.x-dockingPosition.x,
                            global.y-dockingPosition.y);
                    if(canDock(dockingSource,comparisonPoint)){
                        setFloating(false,comparisonPoint);
                    }else{
                        setFloating(true,null);
                    }
                }else{
                    setFloating(true,null);
                }
                dragWindow.setOffset(null);
            }catch(IllegalComponentStateException e){
            }
        }
    }

    protected void paintDragWindow(Graphics g){
        g.setColor(dragWindow.getBackground());
        int w=dragWindow.getWidth();
        int h=dragWindow.getHeight();
        g.fillRect(0,0,w,h);
        g.setColor(dragWindow.getBorderColor());
        g.drawRect(0,0,w-1,h-1);
    }

    private static class Actions extends UIAction{
        private static final String NAVIGATE_RIGHT="navigateRight";
        private static final String NAVIGATE_LEFT="navigateLeft";
        private static final String NAVIGATE_UP="navigateUp";
        private static final String NAVIGATE_DOWN="navigateDown";

        public Actions(String name){
            super(name);
        }

        public void actionPerformed(ActionEvent evt){
            String key=getName();
            JToolBar toolBar=(JToolBar)evt.getSource();
            BasicToolBarUI ui=(BasicToolBarUI)BasicLookAndFeel.getUIOfType(
                    toolBar.getUI(),BasicToolBarUI.class);
            if(NAVIGATE_RIGHT==key){
                ui.navigateFocusedComp(EAST);
            }else if(NAVIGATE_LEFT==key){
                ui.navigateFocusedComp(WEST);
            }else if(NAVIGATE_UP==key){
                ui.navigateFocusedComp(NORTH);
            }else if(NAVIGATE_DOWN==key){
                ui.navigateFocusedComp(SOUTH);
            }
        }
    }

    private class Handler implements ContainerListener,
            FocusListener, MouseInputListener, PropertyChangeListener{
        //
        // MouseInputListener (DockingListener)
        //
        JToolBar tb;
        boolean isDragging=false;        public void componentRemoved(ContainerEvent evt){
            Component c=evt.getChild();
            if(toolBarFocusListener!=null){
                c.removeFocusListener(toolBarFocusListener);
            }
            // Revert the button border
            setBorderToNormal(c);
        }
        Point origin=null;

        //
        // ContainerListener
        //
        public void componentAdded(ContainerEvent evt){
            Component c=evt.getChild();
            if(toolBarFocusListener!=null){
                c.addFocusListener(toolBarFocusListener);
            }
            if(isRolloverBorders()){
                setBorderToRollover(c);
            }else{
                setBorderToNonRollover(c);
            }
        }        public void focusLost(FocusEvent evt){
        }

        //
        // FocusListener
        //
        public void focusGained(FocusEvent evt){
            Component c=evt.getComponent();
            focusedCompIndex=toolBar.getComponentIndex(c);
        }

        public void mouseDragged(MouseEvent evt){
            if(!tb.isEnabled()){
                return;
            }
            isDragging=true;
            Point position=evt.getPoint();
            if(origin==null){
                origin=evt.getComponent().getLocationOnScreen();
            }
            dragTo(position,origin);
        }

        public void mouseClicked(MouseEvent evt){
        }

        //
        // PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent evt){
            String propertyName=evt.getPropertyName();
            if(propertyName=="lookAndFeel"){
                toolBar.updateUI();
            }else if(propertyName=="orientation"){
                // Search for JSeparator components and change it's orientation
                // to match the toolbar and flip it's orientation.
                Component[] components=toolBar.getComponents();
                int orientation=((Integer)evt.getNewValue()).intValue();
                JToolBar.Separator separator;
                for(int i=0;i<components.length;++i){
                    if(components[i] instanceof JToolBar.Separator){
                        separator=(JToolBar.Separator)components[i];
                        if((orientation==JToolBar.HORIZONTAL)){
                            separator.setOrientation(JSeparator.VERTICAL);
                        }else{
                            separator.setOrientation(JSeparator.HORIZONTAL);
                        }
                        Dimension size=separator.getSeparatorSize();
                        if(size!=null&&size.width!=size.height){
                            // Flip the orientation.
                            Dimension newSize=
                                    new Dimension(size.height,size.width);
                            separator.setSeparatorSize(newSize);
                        }
                    }
                }
            }else if(propertyName==IS_ROLLOVER){
                installNormalBorders(toolBar);
                setRolloverBorders(((Boolean)evt.getNewValue()).booleanValue());
            }
        }        public void mousePressed(MouseEvent evt){
            if(!tb.isEnabled()){
                return;
            }
            isDragging=false;
        }

        public void mouseReleased(MouseEvent evt){
            if(!tb.isEnabled()){
                return;
            }
            if(isDragging){
                Point position=evt.getPoint();
                if(origin==null)
                    origin=evt.getComponent().getLocationOnScreen();
                floatAt(position,origin);
            }
            origin=null;
            isDragging=false;
        }





        public void mouseEntered(MouseEvent evt){
        }

        public void mouseExited(MouseEvent evt){
        }

        public void mouseMoved(MouseEvent evt){
        }


    }

    protected class FrameListener extends WindowAdapter{
        public void windowClosing(WindowEvent w){
            if(toolBar.isFloatable()){
                if(dragWindow!=null)
                    dragWindow.setVisible(false);
                floating=false;
                if(floatingToolBar==null)
                    floatingToolBar=createFloatingWindow(toolBar);
                if(floatingToolBar instanceof Window) ((Window)floatingToolBar).setVisible(false);
                floatingToolBar.getContentPane().remove(toolBar);
                String constraint=constraintBeforeFloating;
                if(toolBar.getOrientation()==JToolBar.HORIZONTAL){
                    if(constraint=="West"||constraint=="East"){
                        constraint="North";
                    }
                }else{
                    if(constraint=="North"||constraint=="South"){
                        constraint="West";
                    }
                }
                if(dockingSource==null)
                    dockingSource=toolBar.getParent();
                if(propertyListener!=null)
                    UIManager.removePropertyChangeListener(propertyListener);
                dockingSource.add(toolBar,constraint);
                dockingSource.invalidate();
                Container dockingSourceParent=dockingSource.getParent();
                if(dockingSourceParent!=null)
                    dockingSourceParent.validate();
                dockingSource.repaint();
            }
        }
    }

    protected class ToolBarContListener implements ContainerListener{
        // NOTE: This class exists only for backward compatibility. All
        // its functionality has been moved into Handler. If you need to add
        // new functionality add it to the Handler, but make sure this
        // class calls into the Handler.
        public void componentAdded(ContainerEvent e){
            getHandler().componentAdded(e);
        }

        public void componentRemoved(ContainerEvent e){
            getHandler().componentRemoved(e);
        }
    }

    protected class ToolBarFocusListener implements FocusListener{
        // NOTE: This class exists only for backward compatibility. All
        // its functionality has been moved into Handler. If you need to add
        // new functionality add it to the Handler, but make sure this
        // class calls into the Handler.
        public void focusGained(FocusEvent e){
            getHandler().focusGained(e);
        }

        public void focusLost(FocusEvent e){
            getHandler().focusLost(e);
        }
    }

    protected class PropertyListener implements PropertyChangeListener{
        // NOTE: This class exists only for backward compatibility. All
        // its functionality has been moved into Handler. If you need to add
        // new functionality add it to the Handler, but make sure this
        // class calls into the Handler.
        public void propertyChange(PropertyChangeEvent e){
            getHandler().propertyChange(e);
        }
    }

    public class DockingListener implements MouseInputListener{
        // NOTE: This class exists only for backward compatibility. All
        // its functionality has been moved into Handler. If you need to add
        // new functionality add it to the Handler, but make sure this
        // class calls into the Handler.
        protected JToolBar toolBar;
        protected boolean isDragging=false;
        protected Point origin=null;

        public DockingListener(JToolBar t){
            this.toolBar=t;
            getHandler().tb=t;
        }

        public void mouseClicked(MouseEvent e){
            getHandler().mouseClicked(e);
        }

        public void mousePressed(MouseEvent e){
            getHandler().tb=toolBar;
            getHandler().mousePressed(e);
            isDragging=getHandler().isDragging;
        }

        public void mouseReleased(MouseEvent e){
            getHandler().tb=toolBar;
            getHandler().isDragging=isDragging;
            getHandler().origin=origin;
            getHandler().mouseReleased(e);
            isDragging=getHandler().isDragging;
            origin=getHandler().origin;
        }

        public void mouseEntered(MouseEvent e){
            getHandler().mouseEntered(e);
        }

        public void mouseExited(MouseEvent e){
            getHandler().mouseExited(e);
        }

        public void mouseDragged(MouseEvent e){
            getHandler().tb=toolBar;
            getHandler().origin=origin;
            getHandler().mouseDragged(e);
            isDragging=getHandler().isDragging;
            origin=getHandler().origin;
        }

        public void mouseMoved(MouseEvent e){
            getHandler().mouseMoved(e);
        }
    }

    protected class DragWindow extends Window{
        Color borderColor=Color.gray;
        int orientation=toolBar.getOrientation();
        Point offset; // offset of the mouse cursor inside the DragWindow

        DragWindow(Window w){
            super(w);
        }

        public int getOrientation(){
            return orientation;
        }

        public void setOrientation(int o){
            if(isShowing()){
                if(o==this.orientation)
                    return;
                this.orientation=o;
                Dimension size=getSize();
                setSize(new Dimension(size.height,size.width));
                if(offset!=null){
                    if(BasicGraphicsUtils.isLeftToRight(toolBar)){
                        setOffset(new Point(offset.y,offset.x));
                    }else if(o==JToolBar.HORIZONTAL){
                        setOffset(new Point(size.height-offset.y,offset.x));
                    }else{
                        setOffset(new Point(offset.y,size.width-offset.x));
                    }
                }
                repaint();
            }
        }

        public Point getOffset(){
            return offset;
        }

        public void setOffset(Point p){
            this.offset=p;
        }

        public Color getBorderColor(){
            return this.borderColor;
        }

        public void setBorderColor(Color c){
            if(this.borderColor==c)
                return;
            this.borderColor=c;
            repaint();
        }

        public void paint(Graphics g){
            paintDragWindow(g);
            // Paint the children
            super.paint(g);
        }

        public Insets getInsets(){
            return new Insets(1,1,1,1);
        }
    }
}
