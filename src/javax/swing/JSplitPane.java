/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.*;
import javax.swing.plaf.SplitPaneUI;
import java.awt.*;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JSplitPane extends JComponent implements Accessible{
    public final static int VERTICAL_SPLIT=0;
    public final static int HORIZONTAL_SPLIT=1;
    public final static String LEFT="left";
    public final static String RIGHT="right";
    public final static String TOP="top";
    public final static String BOTTOM="bottom";
    public final static String DIVIDER="divider";
    public final static String ORIENTATION_PROPERTY="orientation";
    public final static String CONTINUOUS_LAYOUT_PROPERTY="continuousLayout";
    public final static String DIVIDER_SIZE_PROPERTY="dividerSize";
    public final static String ONE_TOUCH_EXPANDABLE_PROPERTY=
            "oneTouchExpandable";
    public final static String LAST_DIVIDER_LOCATION_PROPERTY=
            "lastDividerLocation";
    public final static String DIVIDER_LOCATION_PROPERTY="dividerLocation";
    public final static String RESIZE_WEIGHT_PROPERTY="resizeWeight";
    private static final String uiClassID="SplitPaneUI";
    protected int orientation;
    protected boolean continuousLayout;
    protected Component leftComponent;
    protected Component rightComponent;
    protected int dividerSize;
    protected boolean oneTouchExpandable;
    protected int lastDividerLocation;
    private boolean dividerSizeSet=false;
    private boolean oneTouchExpandableSet;
    private double resizeWeight;
    private int dividerLocation;

    public JSplitPane(){
        this(JSplitPane.HORIZONTAL_SPLIT,
                UIManager.getBoolean("SplitPane.continuousLayout"),
                new JButton(UIManager.getString("SplitPane.leftButtonText")),
                new JButton(UIManager.getString("SplitPane.rightButtonText")));
    }

    public JSplitPane(int newOrientation,
                      boolean newContinuousLayout,
                      Component newLeftComponent,
                      Component newRightComponent){
        super();
        dividerLocation=-1;
        setLayout(null);
        setUIProperty("opaque",Boolean.TRUE);
        orientation=newOrientation;
        if(orientation!=HORIZONTAL_SPLIT&&orientation!=VERTICAL_SPLIT)
            throw new IllegalArgumentException("cannot create JSplitPane, "+
                    "orientation must be one of "+
                    "JSplitPane.HORIZONTAL_SPLIT "+
                    "or JSplitPane.VERTICAL_SPLIT");
        continuousLayout=newContinuousLayout;
        if(newLeftComponent!=null)
            setLeftComponent(newLeftComponent);
        if(newRightComponent!=null)
            setRightComponent(newRightComponent);
        updateUI();
    }

    public void updateUI(){
        setUI((SplitPaneUI)UIManager.getUI(this));
        revalidate();
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected void paintChildren(Graphics g){
        super.paintChildren(g);
        SplitPaneUI ui=getUI();
        if(ui!=null){
            Graphics tempG=g.create();
            ui.finishedPaintingChildren(this,tempG);
            tempG.dispose();
        }
    }

    void setUIProperty(String propertyName,Object value){
        if(propertyName=="dividerSize"){
            if(!dividerSizeSet){
                setDividerSize(((Number)value).intValue());
                dividerSizeSet=false;
            }
        }else if(propertyName=="oneTouchExpandable"){
            if(!oneTouchExpandableSet){
                setOneTouchExpandable(((Boolean)value).booleanValue());
                oneTouchExpandableSet=false;
            }
        }else{
            super.setUIProperty(propertyName,value);
        }
    }

    @Override
    public boolean isValidateRoot(){
        return true;
    }

    protected String paramString(){
        String orientationString=(orientation==HORIZONTAL_SPLIT?
                "HORIZONTAL_SPLIT":"VERTICAL_SPLIT");
        String continuousLayoutString=(continuousLayout?
                "true":"false");
        String oneTouchExpandableString=(oneTouchExpandable?
                "true":"false");
        return super.paramString()+
                ",continuousLayout="+continuousLayoutString+
                ",dividerSize="+dividerSize+
                ",lastDividerLocation="+lastDividerLocation+
                ",oneTouchExpandable="+oneTouchExpandableString+
                ",orientation="+orientationString;
    }

    @ConstructorProperties({"orientation"})
    public JSplitPane(int newOrientation){
        this(newOrientation,
                UIManager.getBoolean("SplitPane.continuousLayout"));
    }

    public JSplitPane(int newOrientation,
                      boolean newContinuousLayout){
        this(newOrientation,newContinuousLayout,null,null);
    }

    public JSplitPane(int newOrientation,
                      Component newLeftComponent,
                      Component newRightComponent){
        this(newOrientation,
                UIManager.getBoolean("SplitPane.continuousLayout"),
                newLeftComponent,newRightComponent);
    }

    public Component getTopComponent(){
        return leftComponent;
    }

    public void setTopComponent(Component comp){
        setLeftComponent(comp);
    }

    public Component getBottomComponent(){
        return rightComponent;
    }

    public void setBottomComponent(Component comp){
        setRightComponent(comp);
    }

    public boolean isOneTouchExpandable(){
        return oneTouchExpandable;
    }

    public void setOneTouchExpandable(boolean newValue){
        boolean oldValue=oneTouchExpandable;
        oneTouchExpandable=newValue;
        oneTouchExpandableSet=true;
        firePropertyChange(ONE_TOUCH_EXPANDABLE_PROPERTY,oldValue,newValue);
        repaint();
    }

    public int getLastDividerLocation(){
        return lastDividerLocation;
    }

    public void setLastDividerLocation(int newLastLocation){
        int oldLocation=lastDividerLocation;
        lastDividerLocation=newLastLocation;
        firePropertyChange(LAST_DIVIDER_LOCATION_PROPERTY,oldLocation,
                newLastLocation);
    }

    public boolean isContinuousLayout(){
        return continuousLayout;
    }

    public void setContinuousLayout(boolean newContinuousLayout){
        boolean oldCD=continuousLayout;
        continuousLayout=newContinuousLayout;
        firePropertyChange(CONTINUOUS_LAYOUT_PROPERTY,oldCD,
                newContinuousLayout);
    }

    public double getResizeWeight(){
        return resizeWeight;
    }

    public void setResizeWeight(double value){
        if(value<0||value>1){
            throw new IllegalArgumentException("JSplitPane weight must be between 0 and 1");
        }
        double oldWeight=resizeWeight;
        resizeWeight=value;
        firePropertyChange(RESIZE_WEIGHT_PROPERTY,oldWeight,value);
    }

    public void resetToPreferredSizes(){
        SplitPaneUI ui=getUI();
        if(ui!=null){
            ui.resetToPreferredSizes(this);
        }
    }

    public SplitPaneUI getUI(){
        return (SplitPaneUI)ui;
    }

    public void setUI(SplitPaneUI ui){
        if((SplitPaneUI)this.ui!=ui){
            super.setUI(ui);
            revalidate();
        }
    }

    public void setDividerLocation(double proportionalLocation){
        if(proportionalLocation<0.0||
                proportionalLocation>1.0){
            throw new IllegalArgumentException("proportional location must "+
                    "be between 0.0 and 1.0.");
        }
        if(getOrientation()==VERTICAL_SPLIT){
            setDividerLocation((int)((double)(getHeight()-getDividerSize())*
                    proportionalLocation));
        }else{
            setDividerLocation((int)((double)(getWidth()-getDividerSize())*
                    proportionalLocation));
        }
    }

    public int getDividerSize(){
        return dividerSize;
    }

    public void setDividerSize(int newSize){
        int oldSize=dividerSize;
        dividerSizeSet=true;
        if(oldSize!=newSize){
            dividerSize=newSize;
            firePropertyChange(DIVIDER_SIZE_PROPERTY,oldSize,newSize);
        }
    }

    public int getOrientation(){
        return orientation;
    }

    public void setOrientation(int orientation){
        if((orientation!=VERTICAL_SPLIT)&&
                (orientation!=HORIZONTAL_SPLIT)){
            throw new IllegalArgumentException("JSplitPane: orientation must "+
                    "be one of "+
                    "JSplitPane.VERTICAL_SPLIT or "+
                    "JSplitPane.HORIZONTAL_SPLIT");
        }
        int oldOrientation=this.orientation;
        this.orientation=orientation;
        firePropertyChange(ORIENTATION_PROPERTY,oldOrientation,orientation);
    }

    public int getDividerLocation(){
        return dividerLocation;
    }

    public void setDividerLocation(int location){
        int oldValue=dividerLocation;
        dividerLocation=location;
        // Notify UI.
        SplitPaneUI ui=getUI();
        if(ui!=null){
            ui.setDividerLocation(this,location);
        }
        // Then listeners
        firePropertyChange(DIVIDER_LOCATION_PROPERTY,oldValue,location);
        // And update the last divider location.
        setLastDividerLocation(oldValue);
    }

    public int getMinimumDividerLocation(){
        SplitPaneUI ui=getUI();
        if(ui!=null){
            return ui.getMinimumDividerLocation(this);
        }
        return -1;
    }

    public int getMaximumDividerLocation(){
        SplitPaneUI ui=getUI();
        if(ui!=null){
            return ui.getMaximumDividerLocation(this);
        }
        return -1;
    }

    protected void addImpl(Component comp,Object constraints,int index){
        Component toRemove;
        if(constraints!=null&&!(constraints instanceof String)){
            throw new IllegalArgumentException("cannot add to layout: "+
                    "constraint must be a string "+
                    "(or null)");
        }
        /** If the constraints are null and the left/right component is
         invalid, add it at the left/right component. */
        if(constraints==null){
            if(getLeftComponent()==null){
                constraints=JSplitPane.LEFT;
            }else if(getRightComponent()==null){
                constraints=JSplitPane.RIGHT;
            }
        }
        /** Find the Component that already exists and remove it. */
        if(constraints!=null&&(constraints.equals(JSplitPane.LEFT)||
                constraints.equals(JSplitPane.TOP))){
            toRemove=getLeftComponent();
            if(toRemove!=null){
                remove(toRemove);
            }
            leftComponent=comp;
            index=-1;
        }else if(constraints!=null&&
                (constraints.equals(JSplitPane.RIGHT)||
                        constraints.equals(JSplitPane.BOTTOM))){
            toRemove=getRightComponent();
            if(toRemove!=null){
                remove(toRemove);
            }
            rightComponent=comp;
            index=-1;
        }else if(constraints!=null&&
                constraints.equals(JSplitPane.DIVIDER)){
            index=-1;
        }
        /** LayoutManager should raise for else condition here. */
        super.addImpl(comp,constraints,index);
        // Update the JSplitPane on the screen
        revalidate();
        repaint();
    }

    public Component getLeftComponent(){
        return leftComponent;
    }

    public void setLeftComponent(Component comp){
        if(comp==null){
            if(leftComponent!=null){
                remove(leftComponent);
                leftComponent=null;
            }
        }else{
            add(comp,JSplitPane.LEFT);
        }
    }

    public Component getRightComponent(){
        return rightComponent;
    }

    public void setRightComponent(Component comp){
        if(comp==null){
            if(rightComponent!=null){
                remove(rightComponent);
                rightComponent=null;
            }
        }else{
            add(comp,JSplitPane.RIGHT);
        }
    }

    public void remove(int index){
        Component comp=getComponent(index);
        if(comp==leftComponent){
            leftComponent=null;
        }else if(comp==rightComponent){
            rightComponent=null;
        }
        super.remove(index);
        // Update the JSplitPane on the screen
        revalidate();
        repaint();
    }

    public void remove(Component component){
        if(component==leftComponent){
            leftComponent=null;
        }else if(component==rightComponent){
            rightComponent=null;
        }
        super.remove(component);
        // Update the JSplitPane on the screen
        revalidate();
        repaint();
    }

    public void removeAll(){
        leftComponent=rightComponent=null;
        super.removeAll();
        // Update the JSplitPane on the screen
        revalidate();
        repaint();
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                ui.installUI(this);
            }
        }
    }
    ///////////////////////////
    // Accessibility support //
    ///////////////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJSplitPane();
        }
        return accessibleContext;
    }

    protected class AccessibleJSplitPane extends AccessibleJComponent
            implements AccessibleValue{
        public AccessibleValue getAccessibleValue(){
            return this;
        }

        public Number getCurrentAccessibleValue(){
            return Integer.valueOf(getDividerLocation());
        }

        public boolean setCurrentAccessibleValue(Number n){
            // TIGER - 4422535
            if(n==null){
                return false;
            }
            setDividerLocation(n.intValue());
            return true;
        }

        public Number getMinimumAccessibleValue(){
            return Integer.valueOf(getUI().getMinimumDividerLocation(
                    JSplitPane.this));
        }

        public Number getMaximumAccessibleValue(){
            return Integer.valueOf(getUI().getMaximumDividerLocation(
                    JSplitPane.this));
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.SPLIT_PANE;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            // FIXME: [[[WDW - Should also add BUSY if this implements
            // Adjustable at some point.  If this happens, we probably
            // should also add actions.]]]
            if(getOrientation()==VERTICAL_SPLIT){
                states.add(AccessibleState.VERTICAL);
            }else{
                states.add(AccessibleState.HORIZONTAL);
            }
            return states;
        }
    } // inner class AccessibleJSplitPane
}
