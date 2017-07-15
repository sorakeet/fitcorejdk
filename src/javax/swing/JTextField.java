/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class JTextField extends JTextComponent implements SwingConstants{
    // --- variables -------------------------------------------
    public static final String notifyAction="notify-field-accept";
    private static final Action[] defaultActions={
            new NotifyAction()
    };
    private static final String uiClassID="TextFieldUI";
    private Action action;
    private PropertyChangeListener actionPropertyChangeListener;
    private BoundedRangeModel visibility;
    private int horizontalAlignment=LEADING;
    private int columns;
    private int columnWidth;
    private String command;

    public JTextField(){
        this(null,null,0);
    }

    public JTextField(Document doc,String text,int columns){
        if(columns<0){
            throw new IllegalArgumentException("columns less than zero.");
        }
        visibility=new DefaultBoundedRangeModel();
        visibility.addChangeListener(new ScrollRepainter());
        this.columns=columns;
        if(doc==null){
            doc=createDefaultModel();
        }
        setDocument(doc);
        if(text!=null){
            setText(text);
        }
    }

    public void setDocument(Document doc){
        if(doc!=null){
            doc.putProperty("filterNewlines",Boolean.TRUE);
        }
        super.setDocument(doc);
    }

    public Action[] getActions(){
        return TextAction.augmentList(super.getActions(),defaultActions);
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJTextField();
        }
        return accessibleContext;
    }

    protected String paramString(){
        String horizontalAlignmentString;
        if(horizontalAlignment==LEFT){
            horizontalAlignmentString="LEFT";
        }else if(horizontalAlignment==CENTER){
            horizontalAlignmentString="CENTER";
        }else if(horizontalAlignment==RIGHT){
            horizontalAlignmentString="RIGHT";
        }else if(horizontalAlignment==LEADING){
            horizontalAlignmentString="LEADING";
        }else if(horizontalAlignment==TRAILING){
            horizontalAlignmentString="TRAILING";
        }else horizontalAlignmentString="";
        String commandString=(command!=null?
                command:"");
        return super.paramString()+
                ",columns="+columns+
                ",columnWidth="+columnWidth+
                ",command="+commandString+
                ",horizontalAlignment="+horizontalAlignmentString;
    }

    protected Document createDefaultModel(){
        return new PlainDocument();
    }

    public JTextField(String text){
        this(null,text,0);
    }

    public JTextField(int columns){
        this(null,null,columns);
    }

    public JTextField(String text,int columns){
        this(null,text,columns);
    }

    public int getHorizontalAlignment(){
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(int alignment){
        if(alignment==horizontalAlignment) return;
        int oldValue=horizontalAlignment;
        if((alignment==LEFT)||(alignment==CENTER)||
                (alignment==RIGHT)||(alignment==LEADING)||
                (alignment==TRAILING)){
            horizontalAlignment=alignment;
        }else{
            throw new IllegalArgumentException("horizontalAlignment");
        }
        firePropertyChange("horizontalAlignment",oldValue,horizontalAlignment);
        invalidate();
        repaint();
    }

    public int getColumns(){
        return columns;
    }

    public void setColumns(int columns){
        int oldVal=this.columns;
        if(columns<0){
            throw new IllegalArgumentException("columns less than zero.");
        }
        if(columns!=oldVal){
            this.columns=columns;
            invalidate();
        }
    }

    public synchronized void addActionListener(ActionListener l){
        listenerList.add(ActionListener.class,l);
    }

    public synchronized void removeActionListener(ActionListener l){
        if((l!=null)&&(getAction()==l)){
            setAction(null);
        }else{
            listenerList.remove(ActionListener.class,l);
        }
    }

    public synchronized ActionListener[] getActionListeners(){
        return listenerList.getListeners(ActionListener.class);
    }

    private boolean isListener(Class c,ActionListener a){
        boolean isListener=false;
        Object[] listeners=listenerList.getListenerList();
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==c&&listeners[i+1]==a){
                isListener=true;
            }
        }
        return isListener;
    }

    public Action getAction(){
        return action;
    }

    public void setAction(Action a){
        Action oldValue=getAction();
        if(action==null||!action.equals(a)){
            action=a;
            if(oldValue!=null){
                removeActionListener(oldValue);
                oldValue.removePropertyChangeListener(actionPropertyChangeListener);
                actionPropertyChangeListener=null;
            }
            configurePropertiesFromAction(action);
            if(action!=null){
                // Don't add if it is already a listener
                if(!isListener(ActionListener.class,action)){
                    addActionListener(action);
                }
                // Reverse linkage:
                actionPropertyChangeListener=createActionPropertyChangeListener(action);
                action.addPropertyChangeListener(actionPropertyChangeListener);
            }
            firePropertyChange("action",oldValue,action);
        }
    }

    protected void configurePropertiesFromAction(Action a){
        AbstractAction.setEnabledFromAction(this,a);
        AbstractAction.setToolTipTextFromAction(this,a);
        setActionCommandFromAction(a);
    }

    protected void actionPropertyChanged(Action action,String propertyName){
        if(propertyName==Action.ACTION_COMMAND_KEY){
            setActionCommandFromAction(action);
        }else if(propertyName=="enabled"){
            AbstractAction.setEnabledFromAction(this,action);
        }else if(propertyName==Action.SHORT_DESCRIPTION){
            AbstractAction.setToolTipTextFromAction(this,action);
        }
    }

    private void setActionCommandFromAction(Action action){
        setActionCommand((action==null)?null:
                (String)action.getValue(Action.ACTION_COMMAND_KEY));
    }
    // --- Scrolling support -----------------------------------

    public void setActionCommand(String command){
        this.command=command;
    }

    protected PropertyChangeListener createActionPropertyChangeListener(Action a){
        return new TextFieldActionPropertyChangeListener(this,a);
    }

    public void postActionEvent(){
        fireActionPerformed();
    }

    protected void fireActionPerformed(){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        int modifiers=0;
        AWTEvent currentEvent=EventQueue.getCurrentEvent();
        if(currentEvent instanceof InputEvent){
            modifiers=((InputEvent)currentEvent).getModifiers();
        }else if(currentEvent instanceof ActionEvent){
            modifiers=((ActionEvent)currentEvent).getModifiers();
        }
        ActionEvent e=
                new ActionEvent(this,ActionEvent.ACTION_PERFORMED,
                        (command!=null)?command:getText(),
                        EventQueue.getMostRecentEventTime(),modifiers);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ActionListener.class){
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }

    public BoundedRangeModel getHorizontalVisibility(){
        return visibility;
    }

    public int getScrollOffset(){
        return visibility.getValue();
    }

    public void setScrollOffset(int scrollOffset){
        visibility.setValue(scrollOffset);
    }

    boolean hasActionListener(){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ActionListener.class){
                return true;
            }
        }
        return false;
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

    public String getUIClassID(){
        return uiClassID;
    }

    public Dimension getPreferredSize(){
        Dimension size=super.getPreferredSize();
        if(columns!=0){
            Insets insets=getInsets();
            size.width=columns*getColumnWidth()+
                    insets.left+insets.right;
        }
        return size;
    }

    protected int getColumnWidth(){
        if(columnWidth==0){
            FontMetrics metrics=getFontMetrics(getFont());
            columnWidth=metrics.charWidth('m');
        }
        return columnWidth;
    }

    public void setFont(Font f){
        super.setFont(f);
        columnWidth=0;
    }
    // --- Action implementations -----------------------------------

    public void scrollRectToVisible(Rectangle r){
        // convert to coordinate system of the bounded range
        Insets i=getInsets();
        int x0=r.x+visibility.getValue()-i.left;
        int x1=x0+r.width;
        if(x0<visibility.getValue()){
            // Scroll to the left
            visibility.setValue(x0);
        }else if(x1>visibility.getValue()+visibility.getExtent()){
            // Scroll to the right
            visibility.setValue(x1-visibility.getExtent());
        }
    }

    @Override
    public boolean isValidateRoot(){
        return !(SwingUtilities.getUnwrappedParent(this) instanceof JViewport);
    }

    private static class TextFieldActionPropertyChangeListener extends
            ActionPropertyChangeListener<JTextField>{
        TextFieldActionPropertyChangeListener(JTextField tf,Action a){
            super(tf,a);
        }

        protected void actionPropertyChanged(JTextField textField,
                                             Action action,
                                             PropertyChangeEvent e){
            if(AbstractAction.shouldReconfigure(e)){
                textField.configurePropertiesFromAction(action);
            }else{
                textField.actionPropertyChanged(action,e.getPropertyName());
            }
        }
    }

    // Note that JFormattedTextField.CommitAction extends this
    static class NotifyAction extends TextAction{
        NotifyAction(){
            super(notifyAction);
        }

        public void actionPerformed(ActionEvent e){
            JTextComponent target=getFocusedComponent();
            if(target instanceof JTextField){
                JTextField field=(JTextField)target;
                field.postActionEvent();
            }
        }

        public boolean isEnabled(){
            JTextComponent target=getFocusedComponent();
            if(target instanceof JTextField){
                return ((JTextField)target).hasActionListener();
            }
            return false;
        }
    }
/////////////////
// Accessibility support
////////////////

    class ScrollRepainter implements ChangeListener, Serializable{
        public void stateChanged(ChangeEvent e){
            repaint();
        }
    }

    protected class AccessibleJTextField extends AccessibleJTextComponent{
        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            states.add(AccessibleState.SINGLE_LINE);
            return states;
        }
    }
}
