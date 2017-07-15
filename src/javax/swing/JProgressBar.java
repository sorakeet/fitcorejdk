/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ProgressBarUI;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.Format;
import java.text.NumberFormat;

public class JProgressBar extends JComponent implements SwingConstants, Accessible{
    private static final String uiClassID="ProgressBarUI";
    static final private int defaultMinimum=0;
    static final private int defaultMaximum=100;
    static final private int defaultOrientation=HORIZONTAL;
    protected int orientation;
    protected boolean paintBorder;
    protected BoundedRangeModel model;
    protected String progressString;
    protected boolean paintString;
    protected transient ChangeEvent changeEvent=null;
    protected ChangeListener changeListener=null;
    private transient Format format;
    private boolean indeterminate;

    public JProgressBar(){
        this(defaultOrientation);
    }

    public JProgressBar(int orient){
        this(orient,defaultMinimum,defaultMaximum);
    }

    public JProgressBar(int orient,int min,int max){
        // Creating the model this way is a bit simplistic, but
        //  I believe that it is the the most common usage of this
        //  component - it's what people will expect.
        setModel(new DefaultBoundedRangeModel(min,0,min,max));
        updateUI();
        setOrientation(orient);      // documented with set/getOrientation()
        setBorderPainted(true);      // documented with is/setBorderPainted()
        setStringPainted(false);     // see setStringPainted
        setString(null);             // see getString
        setIndeterminate(false);     // see setIndeterminate
    }

    public void updateUI(){
        setUI((ProgressBarUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected void paintBorder(Graphics g){
        if(isBorderPainted()){
            super.paintBorder(g);
        }
    }

    public boolean isBorderPainted(){
        return paintBorder;
    }

    public void setBorderPainted(boolean b){
        boolean oldValue=paintBorder;
        paintBorder=b;
        firePropertyChange("borderPainted",oldValue,paintBorder);
        if(paintBorder!=oldValue){
            repaint();
        }
    }

    protected String paramString(){
        String orientationString=(orientation==HORIZONTAL?
                "HORIZONTAL":"VERTICAL");
        String paintBorderString=(paintBorder?
                "true":"false");
        String progressStringString=(progressString!=null?
                progressString:"");
        String paintStringString=(paintString?
                "true":"false");
        String indeterminateString=(indeterminate?
                "true":"false");
        return super.paramString()+
                ",orientation="+orientationString+
                ",paintBorder="+paintBorderString+
                ",paintString="+paintStringString+
                ",progressString="+progressStringString+
                ",indeterminateString="+indeterminateString;
    }

    public JProgressBar(int min,int max){
        this(defaultOrientation,min,max);
    }

    public JProgressBar(BoundedRangeModel newModel){
        setModel(newModel);
        updateUI();
        setOrientation(defaultOrientation);  // see setOrientation()
        setBorderPainted(true);              // see setBorderPainted()
        setStringPainted(false);             // see setStringPainted
        setString(null);                     // see getString
        setIndeterminate(false);             // see setIndeterminate
    }

    public int getOrientation(){
        return orientation;
    }

    public void setOrientation(int newOrientation){
        if(orientation!=newOrientation){
            switch(newOrientation){
                case VERTICAL:
                case HORIZONTAL:
                    int oldOrientation=orientation;
                    orientation=newOrientation;
                    firePropertyChange("orientation",oldOrientation,newOrientation);
                    if(accessibleContext!=null){
                        accessibleContext.firePropertyChange(
                                AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                ((oldOrientation==VERTICAL)
                                        ?AccessibleState.VERTICAL
                                        :AccessibleState.HORIZONTAL),
                                ((orientation==VERTICAL)
                                        ?AccessibleState.VERTICAL
                                        :AccessibleState.HORIZONTAL));
                    }
                    break;
                default:
                    throw new IllegalArgumentException(newOrientation+
                            " is not a legal orientation");
            }
            revalidate();
        }
    }

    public boolean isStringPainted(){
        return paintString;
    }

    public void setStringPainted(boolean b){
        //PENDING: specify that string not painted when in indeterminate mode?
        //         or just leave that to the L&F?
        boolean oldValue=paintString;
        paintString=b;
        firePropertyChange("stringPainted",oldValue,paintString);
        if(paintString!=oldValue){
            revalidate();
            repaint();
        }
    }

    public String getString(){
        if(progressString!=null){
            return progressString;
        }else{
            if(format==null){
                format=NumberFormat.getPercentInstance();
            }
            return format.format(new Double(getPercentComplete()));
        }
    }

    public void setString(String s){
        String oldValue=progressString;
        progressString=s;
        firePropertyChange("string",oldValue,progressString);
        if(progressString==null||oldValue==null||!progressString.equals(oldValue)){
            repaint();
        }
    }

    public double getPercentComplete(){
        long span=model.getMaximum()-model.getMinimum();
        double currentValue=model.getValue();
        double pc=(currentValue-model.getMinimum())/span;
        return pc;
    }

    public ProgressBarUI getUI(){
        return (ProgressBarUI)ui;
    }

    public void setUI(ProgressBarUI ui){
        super.setUI(ui);
    }

    public void addChangeListener(ChangeListener l){
        listenerList.add(ChangeListener.class,l);
    }    protected ChangeListener createChangeListener(){
        return new ModelListener();
    }

    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class,l);
    }

    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged(){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ChangeListener.class){
                // Lazily create the event:
                if(changeEvent==null)
                    changeEvent=new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }

    public int getValue(){
        return getModel().getValue();
    }

    public void setValue(int n){
        BoundedRangeModel brm=getModel();
        int oldValue=brm.getValue();
        brm.setValue(n);
        if(accessibleContext!=null){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    Integer.valueOf(oldValue),
                    Integer.valueOf(brm.getValue()));
        }
    }    public BoundedRangeModel getModel(){
        return model;
    }

    public int getMinimum(){
        return getModel().getMinimum();
    }    public void setModel(BoundedRangeModel newModel){
        // PENDING(???) setting the same model to multiple bars is broken; listeners
        BoundedRangeModel oldModel=getModel();
        if(newModel!=oldModel){
            if(oldModel!=null){
                oldModel.removeChangeListener(changeListener);
                changeListener=null;
            }
            model=newModel;
            if(newModel!=null){
                changeListener=createChangeListener();
                newModel.addChangeListener(changeListener);
            }
            if(accessibleContext!=null){
                accessibleContext.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                        (oldModel==null
                                ?null:Integer.valueOf(oldModel.getValue())),
                        (newModel==null
                                ?null:Integer.valueOf(newModel.getValue())));
            }
            if(model!=null){
                model.setExtent(0);
            }
            repaint();
        }
    }

    public void setMinimum(int n){
        getModel().setMinimum(n);
    }

    public int getMaximum(){
        return getModel().getMaximum();
    }

    public void setMaximum(int n){
        getModel().setMaximum(n);
    }

    public boolean isIndeterminate(){
        return indeterminate;
    }

    public void setIndeterminate(boolean newValue){
        boolean oldValue=indeterminate;
        indeterminate=newValue;
        firePropertyChange("indeterminate",oldValue,indeterminate);
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

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJProgressBar();
        }
        return accessibleContext;
    }

    private class ModelListener implements ChangeListener, Serializable{
        public void stateChanged(ChangeEvent e){
            fireStateChanged();
        }
    }

    protected class AccessibleJProgressBar extends AccessibleJComponent
            implements AccessibleValue{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.PROGRESS_BAR;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            if(getModel().getValueIsAdjusting()){
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
            // TIGER- 4422535
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
            return Integer.valueOf(model.getMaximum()-model.getExtent());
        }
    } // AccessibleJProgressBar


/////////////////
// Accessibility support
////////////////




}
