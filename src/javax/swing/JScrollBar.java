/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollBarUI;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class JScrollBar extends JComponent implements Adjustable, Accessible{
    private static final String uiClassID="ScrollBarUI";
    protected BoundedRangeModel model;
    protected int orientation;
    protected int unitIncrement;
    protected int blockIncrement;
    private ChangeListener fwdAdjustmentEvents=new ModelListener();

    public JScrollBar(){
        this(VERTICAL);
    }

    public JScrollBar(int orientation){
        this(orientation,0,10,0,100);
    }

    public JScrollBar(int orientation,int value,int extent,int min,int max){
        checkOrientation(orientation);
        this.unitIncrement=1;
        this.blockIncrement=(extent==0)?1:extent;
        this.orientation=orientation;
        this.model=new DefaultBoundedRangeModel(value,extent,min,max);
        this.model.addChangeListener(fwdAdjustmentEvents);
        setRequestFocusEnabled(false);
        updateUI();
    }

    private void checkOrientation(int orientation){
        switch(orientation){
            case VERTICAL:
            case HORIZONTAL:
                break;
            default:
                throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
        }
    }

    public void updateUI(){
        setUI((ScrollBarUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    public Dimension getMaximumSize(){
        Dimension pref=getPreferredSize();
        if(getOrientation()==VERTICAL){
            return new Dimension(pref.width,Short.MAX_VALUE);
        }else{
            return new Dimension(Short.MAX_VALUE,pref.height);
        }
    }

    public int getOrientation(){
        return orientation;
    }

    public void setOrientation(int orientation){
        checkOrientation(orientation);
        int oldValue=this.orientation;
        this.orientation=orientation;
        firePropertyChange("orientation",oldValue,orientation);
        if((oldValue!=orientation)&&(accessibleContext!=null)){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    ((oldValue==VERTICAL)
                            ?AccessibleState.VERTICAL:AccessibleState.HORIZONTAL),
                    ((orientation==VERTICAL)
                            ?AccessibleState.VERTICAL:AccessibleState.HORIZONTAL));
        }
        if(orientation!=oldValue){
            revalidate();
        }
    }

    public Dimension getMinimumSize(){
        Dimension pref=getPreferredSize();
        if(orientation==VERTICAL){
            return new Dimension(pref.width,5);
        }else{
            return new Dimension(5,pref.height);
        }
    }

    public void setEnabled(boolean x){
        super.setEnabled(x);
        Component[] children=getComponents();
        for(Component child : children){
            child.setEnabled(x);
        }
    }    public BoundedRangeModel getModel(){
        return model;
    }

    protected String paramString(){
        String orientationString=(orientation==HORIZONTAL?
                "HORIZONTAL":"VERTICAL");
        return super.paramString()+
                ",blockIncrement="+blockIncrement+
                ",orientation="+orientationString+
                ",unitIncrement="+unitIncrement;
    }    public void setModel(BoundedRangeModel newModel){
        Integer oldValue=null;
        BoundedRangeModel oldModel=model;
        if(model!=null){
            model.removeChangeListener(fwdAdjustmentEvents);
            oldValue=Integer.valueOf(model.getValue());
        }
        model=newModel;
        if(model!=null){
            model.addChangeListener(fwdAdjustmentEvents);
        }
        firePropertyChange("model",oldModel,model);
        if(accessibleContext!=null){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    oldValue,new Integer(model.getValue()));
        }
    }

    public ScrollBarUI getUI(){
        return (ScrollBarUI)ui;
    }

    public void setUI(ScrollBarUI ui){
        super.setUI(ui);
    }    public void setUnitIncrement(int unitIncrement){
        int oldValue=this.unitIncrement;
        this.unitIncrement=unitIncrement;
        firePropertyChange("unitIncrement",oldValue,unitIncrement);
    }

    public int getUnitIncrement(int direction){
        return unitIncrement;
    }

    public int getBlockIncrement(int direction){
        return blockIncrement;
    }    public void setBlockIncrement(int blockIncrement){
        int oldValue=this.blockIncrement;
        this.blockIncrement=blockIncrement;
        firePropertyChange("blockIncrement",oldValue,blockIncrement);
    }

    public void setValues(int newValue,int newExtent,int newMin,int newMax){
        BoundedRangeModel m=getModel();
        int oldValue=m.getValue();
        m.setRangeProperties(newValue,newExtent,newMin,newMax,m.getValueIsAdjusting());
        if(accessibleContext!=null){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    Integer.valueOf(oldValue),
                    Integer.valueOf(m.getValue()));
        }
    }    public int getUnitIncrement(){
        return unitIncrement;
    }

    public AdjustmentListener[] getAdjustmentListeners(){
        return listenerList.getListeners(AdjustmentListener.class);
    }    public int getBlockIncrement(){
        return blockIncrement;
    }

    protected void fireAdjustmentValueChanged(int id,int type,int value){
        fireAdjustmentValueChanged(id,type,value,getValueIsAdjusting());
    }    public int getValue(){
        return getModel().getValue();
    }

    public boolean getValueIsAdjusting(){
        return getModel().getValueIsAdjusting();
    }    public void setValue(int value){
        BoundedRangeModel m=getModel();
        int oldValue=m.getValue();
        m.setValue(value);
        if(accessibleContext!=null){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    Integer.valueOf(oldValue),
                    Integer.valueOf(m.getValue()));
        }
    }

    public void setValueIsAdjusting(boolean b){
        BoundedRangeModel m=getModel();
        boolean oldValue=m.getValueIsAdjusting();
        m.setValueIsAdjusting(b);
        if((oldValue!=b)&&(accessibleContext!=null)){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    ((oldValue)?AccessibleState.BUSY:null),
                    ((b)?AccessibleState.BUSY:null));
        }
    }    public int getVisibleAmount(){
        return getModel().getExtent();
    }

    private void fireAdjustmentValueChanged(int id,int type,int value,
                                            boolean isAdjusting){
        Object[] listeners=listenerList.getListenerList();
        AdjustmentEvent e=null;
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==AdjustmentListener.class){
                if(e==null){
                    e=new AdjustmentEvent(this,id,type,value,isAdjusting);
                }
                ((AdjustmentListener)listeners[i+1]).adjustmentValueChanged(e);
            }
        }
    }    public void setVisibleAmount(int extent){
        getModel().setExtent(extent);
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
    }    public int getMinimum(){
        return getModel().getMinimum();
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJScrollBar();
        }
        return accessibleContext;
    }    public void setMinimum(int minimum){
        getModel().setMinimum(minimum);
    }

    private class ModelListener implements ChangeListener, Serializable{
        public void stateChanged(ChangeEvent e){
            Object obj=e.getSource();
            if(obj instanceof BoundedRangeModel){
                int id=AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED;
                int type=AdjustmentEvent.TRACK;
                BoundedRangeModel model=(BoundedRangeModel)obj;
                int value=model.getValue();
                boolean isAdjusting=model.getValueIsAdjusting();
                fireAdjustmentValueChanged(id,type,value,isAdjusting);
            }
        }
    }    public int getMaximum(){
        return getModel().getMaximum();
    }

    protected class AccessibleJScrollBar extends AccessibleJComponent
            implements AccessibleValue{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.SCROLL_BAR;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            if(getValueIsAdjusting()){
                states.add(AccessibleState.BUSY);
            }
            if(getOrientation()==VERTICAL){
                states.add(AccessibleState.VERTICAL);
            }else{
                states.add(AccessibleState.HORIZONTAL);
            }
            return states;
        }

        public AccessibleValue getAccessibleValue(){
            return this;
        }

        public Number getCurrentAccessibleValue(){
            return Integer.valueOf(getValue());
        }

        public boolean setCurrentAccessibleValue(Number n){
            // TIGER - 4422535
            if(n==null){
                return false;
            }
            setValue(n.intValue());
            return true;
        }

        public Number getMinimumAccessibleValue(){
            return Integer.valueOf(getMinimum());
        }

        public Number getMaximumAccessibleValue(){
            // TIGER - 4422362
            return new Integer(model.getMaximum()-model.getExtent());
        }
    } // AccessibleJScrollBar    public void setMaximum(int maximum){
        getModel().setMaximum(maximum);
    }







    public void addAdjustmentListener(AdjustmentListener l){
        listenerList.add(AdjustmentListener.class,l);
    }

    public void removeAdjustmentListener(AdjustmentListener l){
        listenerList.remove(AdjustmentListener.class,l);
    }








    // PENDING(hmuller) - the next three methods should be removed










/////////////////
// Accessibility support
////////////////




}
