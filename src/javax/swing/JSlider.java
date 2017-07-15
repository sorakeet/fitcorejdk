/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class JSlider extends JComponent implements SwingConstants, Accessible{
    private static final String uiClassID="SliderUI";
    protected BoundedRangeModel sliderModel;
    protected int majorTickSpacing;
    protected int minorTickSpacing;
    protected boolean snapToTicks=false;
    protected int orientation;
    protected ChangeListener changeListener=createChangeListener();
    protected transient ChangeEvent changeEvent=null;
    boolean snapToValue=true;
    private boolean paintTicks=false;
    private boolean paintTrack=true;
    private boolean paintLabels=false;
    private boolean isInverted=false;
    private Dictionary labelTable;

    public JSlider(){
        this(HORIZONTAL,0,100,50);
    }

    public JSlider(int orientation,int min,int max,int value){
        checkOrientation(orientation);
        this.orientation=orientation;
        setModel(new DefaultBoundedRangeModel(value,0,min,max));
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
        setUI((SliderUI)UIManager.getUI(this));
        // The labels preferred size may be derived from the font
        // of the slider, so we must update the UI of the slider first, then
        // that of labels.  This way when setSize is called the right
        // font is used.
        updateLabelUIs();
    }

    public String getUIClassID(){
        return uiClassID;
    }

    public void setFont(Font font){
        super.setFont(font);
        updateLabelSizes();
    }

    private void updateLabelSizes(){
        Dictionary labelTable=getLabelTable();
        if(labelTable!=null){
            Enumeration labels=labelTable.elements();
            while(labels.hasMoreElements()){
                JComponent component=(JComponent)labels.nextElement();
                component.setSize(component.getPreferredSize());
            }
        }
    }

    protected String paramString(){
        String paintTicksString=(paintTicks?
                "true":"false");
        String paintTrackString=(paintTrack?
                "true":"false");
        String paintLabelsString=(paintLabels?
                "true":"false");
        String isInvertedString=(isInverted?
                "true":"false");
        String snapToTicksString=(snapToTicks?
                "true":"false");
        String snapToValueString=(snapToValue?
                "true":"false");
        String orientationString=(orientation==HORIZONTAL?
                "HORIZONTAL":"VERTICAL");
        return super.paramString()+
                ",isInverted="+isInvertedString+
                ",majorTickSpacing="+majorTickSpacing+
                ",minorTickSpacing="+minorTickSpacing+
                ",orientation="+orientationString+
                ",paintLabels="+paintLabelsString+
                ",paintTicks="+paintTicksString+
                ",paintTrack="+paintTrackString+
                ",snapToTicks="+snapToTicksString+
                ",snapToValue="+snapToValueString;
    }

    protected void updateLabelUIs(){
        Dictionary labelTable=getLabelTable();
        if(labelTable==null){
            return;
        }
        Enumeration labels=labelTable.keys();
        while(labels.hasMoreElements()){
            JComponent component=(JComponent)labelTable.get(labels.nextElement());
            component.updateUI();
            component.setSize(component.getPreferredSize());
        }
    }

    public Dictionary getLabelTable(){
/**
 if ( labelTable == null && getMajorTickSpacing() > 0 ) {
 setLabelTable( createStandardLabels( getMajorTickSpacing() ) );
 }
 */
        return labelTable;
    }

    public void setLabelTable(Dictionary labels){
        Dictionary oldTable=labelTable;
        labelTable=labels;
        updateLabelUIs();
        firePropertyChange("labelTable",oldTable,labelTable);
        if(labels!=oldTable){
            revalidate();
            repaint();
        }
    }

    public JSlider(int orientation){
        this(orientation,0,100,50);
    }

    public JSlider(int min,int max){
        this(HORIZONTAL,min,max,(min+max)/2);
    }

    public JSlider(int min,int max,int value){
        this(HORIZONTAL,min,max,value);
    }

    public JSlider(BoundedRangeModel brm){
        this.orientation=JSlider.HORIZONTAL;
        setModel(brm);
        updateUI();
    }

    public SliderUI getUI(){
        return (SliderUI)ui;
    }

    public void setUI(SliderUI ui){
        super.setUI(ui);
    }

    protected ChangeListener createChangeListener(){
        return new ModelListener();
    }    public BoundedRangeModel getModel(){
        return sliderModel;
    }

    public void addChangeListener(ChangeListener l){
        listenerList.add(ChangeListener.class,l);
    }    public void setModel(BoundedRangeModel newModel){
        BoundedRangeModel oldModel=getModel();
        if(oldModel!=null){
            oldModel.removeChangeListener(changeListener);
        }
        sliderModel=newModel;
        if(newModel!=null){
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
        firePropertyChange("model",oldModel,sliderModel);
    }

    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class,l);
    }

    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged(){
        Object[] listeners=listenerList.getListenerList();
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ChangeListener.class){
                if(changeEvent==null){
                    changeEvent=new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }

    public int getValue(){
        return getModel().getValue();
    }

    public void setValue(int n){
        BoundedRangeModel m=getModel();
        int oldValue=m.getValue();
        if(oldValue==n){
            return;
        }
        m.setValue(n);
        if(accessibleContext!=null){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    Integer.valueOf(oldValue),
                    Integer.valueOf(m.getValue()));
        }
    }

    public int getMinimum(){
        return getModel().getMinimum();
    }

    public void setMinimum(int minimum){
        int oldMin=getModel().getMinimum();
        getModel().setMinimum(minimum);
        firePropertyChange("minimum",Integer.valueOf(oldMin),Integer.valueOf(minimum));
    }

    public int getMaximum(){
        return getModel().getMaximum();
    }

    public void setMaximum(int maximum){
        int oldMax=getModel().getMaximum();
        getModel().setMaximum(maximum);
        firePropertyChange("maximum",Integer.valueOf(oldMax),Integer.valueOf(maximum));
    }

    public boolean getValueIsAdjusting(){
        return getModel().getValueIsAdjusting();
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
    }

    public int getExtent(){
        return getModel().getExtent();
    }

    public void setExtent(int extent){
        getModel().setExtent(extent);
    }

    public int getOrientation(){
        return orientation;
    }    public boolean imageUpdate(Image img,int infoflags,int x,int y,int w,int h){
        if(!isShowing()){
            return false;
        }
        // Check that there is a label with such image
        Enumeration elements=labelTable.elements();
        while(elements.hasMoreElements()){
            Component component=(Component)elements.nextElement();
            if(component instanceof JLabel){
                JLabel label=(JLabel)component;
                if(SwingUtilities.doesIconReferenceImage(label.getIcon(),img)||
                        SwingUtilities.doesIconReferenceImage(label.getDisabledIcon(),img)){
                    return super.imageUpdate(img,infoflags,x,y,w,h);
                }
            }
        }
        return false;
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

    public Hashtable createStandardLabels(int increment){
        return createStandardLabels(increment,getMinimum());
    }

    public Hashtable createStandardLabels(int increment,int start){
        if(start>getMaximum()||start<getMinimum()){
            throw new IllegalArgumentException("Slider label start point out of range.");
        }
        if(increment<=0){
            throw new IllegalArgumentException("Label incremement must be > 0");
        }
        class SmartHashtable extends Hashtable<Object,Object> implements PropertyChangeListener{
            int increment=0;
            int start=0;
            boolean startAtMin=false;

            public SmartHashtable(int increment,int start){
                super();
                this.increment=increment;
                this.start=start;
                startAtMin=start==getMinimum();
                createLabels();
            }

            void createLabels(){
                for(int labelIndex=start;labelIndex<=getMaximum();labelIndex+=increment){
                    put(Integer.valueOf(labelIndex),new LabelUIResource(""+labelIndex,JLabel.CENTER));
                }
            }

            public void propertyChange(PropertyChangeEvent e){
                if(e.getPropertyName().equals("minimum")&&startAtMin){
                    start=getMinimum();
                }
                if(e.getPropertyName().equals("minimum")||
                        e.getPropertyName().equals("maximum")){
                    Enumeration keys=getLabelTable().keys();
                    Hashtable<Object,Object> hashtable=new Hashtable<Object,Object>();
                    // Save the labels that were added by the developer
                    while(keys.hasMoreElements()){
                        Object key=keys.nextElement();
                        Object value=labelTable.get(key);
                        if(!(value instanceof LabelUIResource)){
                            hashtable.put(key,value);
                        }
                    }
                    clear();
                    createLabels();
                    // Add the saved labels
                    keys=hashtable.keys();
                    while(keys.hasMoreElements()){
                        Object key=keys.nextElement();
                        put(key,hashtable.get(key));
                    }
                    ((JSlider)e.getSource()).setLabelTable(this);
                }
            }

            class LabelUIResource extends JLabel implements UIResource{
                public LabelUIResource(String text,int alignment){
                    super(text,alignment);
                    setName("Slider.label");
                }

                public Color getForeground(){
                    Color fg=super.getForeground();
                    if(fg!=null&&!(fg instanceof UIResource)){
                        return fg;
                    }
                    if(!(JSlider.this.getForeground() instanceof UIResource)){
                        return JSlider.this.getForeground();
                    }
                    return fg;
                }

                public Font getFont(){
                    Font font=super.getFont();
                    if(font!=null&&!(font instanceof UIResource)){
                        return font;
                    }
                    return JSlider.this.getFont();
                }
            }
        }
        SmartHashtable table=new SmartHashtable(increment,start);
        Dictionary labelTable=getLabelTable();
        if(labelTable!=null&&(labelTable instanceof PropertyChangeListener)){
            removePropertyChangeListener((PropertyChangeListener)labelTable);
        }
        addPropertyChangeListener(table);
        return table;
    }

    public boolean getInverted(){
        return isInverted;
    }

    public void setInverted(boolean b){
        boolean oldValue=isInverted;
        isInverted=b;
        firePropertyChange("inverted",oldValue,isInverted);
        if(b!=oldValue){
            repaint();
        }
    }

    public int getMajorTickSpacing(){
        return majorTickSpacing;
    }

    public void setMajorTickSpacing(int n){
        int oldValue=majorTickSpacing;
        majorTickSpacing=n;
        if(labelTable==null&&getMajorTickSpacing()>0&&getPaintLabels()){
            setLabelTable(createStandardLabels(getMajorTickSpacing()));
        }
        firePropertyChange("majorTickSpacing",oldValue,majorTickSpacing);
        if(majorTickSpacing!=oldValue&&getPaintTicks()){
            repaint();
        }
    }

    public int getMinorTickSpacing(){
        return minorTickSpacing;
    }

    public void setMinorTickSpacing(int n){
        int oldValue=minorTickSpacing;
        minorTickSpacing=n;
        firePropertyChange("minorTickSpacing",oldValue,minorTickSpacing);
        if(minorTickSpacing!=oldValue&&getPaintTicks()){
            repaint();
        }
    }

    public boolean getPaintTicks(){
        return paintTicks;
    }

    public void setPaintTicks(boolean b){
        boolean oldValue=paintTicks;
        paintTicks=b;
        firePropertyChange("paintTicks",oldValue,paintTicks);
        if(paintTicks!=oldValue){
            revalidate();
            repaint();
        }
    }

    public boolean getSnapToTicks(){
        return snapToTicks;
    }

    public void setSnapToTicks(boolean b){
        boolean oldValue=snapToTicks;
        snapToTicks=b;
        firePropertyChange("snapToTicks",oldValue,snapToTicks);
    }

    boolean getSnapToValue(){
        return snapToValue;
    }

    void setSnapToValue(boolean b){
        boolean oldValue=snapToValue;
        snapToValue=b;
        firePropertyChange("snapToValue",oldValue,snapToValue);
    }

    public boolean getPaintTrack(){
        return paintTrack;
    }

    public void setPaintTrack(boolean b){
        boolean oldValue=paintTrack;
        paintTrack=b;
        firePropertyChange("paintTrack",oldValue,paintTrack);
        if(paintTrack!=oldValue){
            repaint();
        }
    }

    public boolean getPaintLabels(){
        return paintLabels;
    }

    public void setPaintLabels(boolean b){
        boolean oldValue=paintLabels;
        paintLabels=b;
        if(labelTable==null&&getMajorTickSpacing()>0){
            setLabelTable(createStandardLabels(getMajorTickSpacing()));
        }
        firePropertyChange("paintLabels",oldValue,paintLabels);
        if(paintLabels!=oldValue){
            revalidate();
            repaint();
        }
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

    private class ModelListener implements ChangeListener, Serializable{
        public void stateChanged(ChangeEvent e){
            fireStateChanged();
        }
    }

    protected class AccessibleJSlider extends AccessibleJComponent
            implements AccessibleValue{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.SLIDER;
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
            BoundedRangeModel model=JSlider.this.getModel();
            return Integer.valueOf(model.getMaximum()-model.getExtent());
        }
    } // AccessibleJSlider




/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJSlider();
        }
        return accessibleContext;
    }


}
