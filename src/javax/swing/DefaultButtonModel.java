/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.EventListener;

public class DefaultButtonModel implements ButtonModel, Serializable{
    public final static int ARMED=1<<0;
    public final static int SELECTED=1<<1;
    public final static int PRESSED=1<<2;
    public final static int ENABLED=1<<3;
    public final static int ROLLOVER=1<<4;
    protected int stateMask=0;
    protected String actionCommand=null;
    protected ButtonGroup group=null;
    protected int mnemonic=0;
    protected transient ChangeEvent changeEvent=null;
    protected EventListenerList listenerList=new EventListenerList();
    // controls the usage of the MenuItem.disabledAreNavigable UIDefaults
    // property in the setArmed() method
    private boolean menuItem=false;
    public DefaultButtonModel(){
        stateMask=0;
        setEnabled(true);
    }

    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }    public void setActionCommand(String actionCommand){
        this.actionCommand=actionCommand;
    }

    public ActionListener[] getActionListeners(){
        return listenerList.getListeners(ActionListener.class);
    }    public String getActionCommand(){
        return actionCommand;
    }

    public ItemListener[] getItemListeners(){
        return listenerList.getListeners(ItemListener.class);
    }    public boolean isArmed(){
        return (stateMask&ARMED)!=0;
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }    public boolean isSelected(){
        return (stateMask&SELECTED)!=0;
    }

    public Object[] getSelectedObjects(){
        return null;
    }    public boolean isEnabled(){
        return (stateMask&ENABLED)!=0;
    }

    public ButtonGroup getGroup(){
        return group;
    }    public boolean isPressed(){
        return (stateMask&PRESSED)!=0;
    }

    public void setGroup(ButtonGroup group){
        this.group=group;
    }    public boolean isRollover(){
        return (stateMask&ROLLOVER)!=0;
    }

    public void addActionListener(ActionListener l){
        listenerList.add(ActionListener.class,l);
    }    public void setArmed(boolean b){
        if(isMenuItem()&&
                UIManager.getBoolean("MenuItem.disabledAreNavigable")){
            if((isArmed()==b)){
                return;
            }
        }else{
            if((isArmed()==b)||!isEnabled()){
                return;
            }
        }
        if(b){
            stateMask|=ARMED;
        }else{
            stateMask&=~ARMED;
        }
        fireStateChanged();
    }

    public void removeActionListener(ActionListener l){
        listenerList.remove(ActionListener.class,l);
    }    public void setEnabled(boolean b){
        if(isEnabled()==b){
            return;
        }
        if(b){
            stateMask|=ENABLED;
        }else{
            stateMask&=~ENABLED;
            // unarm and unpress, just in case
            stateMask&=~ARMED;
            stateMask&=~PRESSED;
        }
        fireStateChanged();
    }

    public void addItemListener(ItemListener l){
        listenerList.add(ItemListener.class,l);
    }    public void setSelected(boolean b){
        if(this.isSelected()==b){
            return;
        }
        if(b){
            stateMask|=SELECTED;
        }else{
            stateMask&=~SELECTED;
        }
        fireItemStateChanged(
                new ItemEvent(this,
                        ItemEvent.ITEM_STATE_CHANGED,
                        this,
                        b?ItemEvent.SELECTED:ItemEvent.DESELECTED));
        fireStateChanged();
    }

    public void removeItemListener(ItemListener l){
        listenerList.remove(ItemListener.class,l);
    }    public void setPressed(boolean b){
        if((isPressed()==b)||!isEnabled()){
            return;
        }
        if(b){
            stateMask|=PRESSED;
        }else{
            stateMask&=~PRESSED;
        }
        if(!isPressed()&&isArmed()){
            int modifiers=0;
            AWTEvent currentEvent=EventQueue.getCurrentEvent();
            if(currentEvent instanceof InputEvent){
                modifiers=((InputEvent)currentEvent).getModifiers();
            }else if(currentEvent instanceof ActionEvent){
                modifiers=((ActionEvent)currentEvent).getModifiers();
            }
            fireActionPerformed(
                    new ActionEvent(this,ActionEvent.ACTION_PERFORMED,
                            getActionCommand(),
                            EventQueue.getMostRecentEventTime(),
                            modifiers));
        }
        fireStateChanged();
    }

    public void addChangeListener(ChangeListener l){
        listenerList.add(ChangeListener.class,l);
    }    public void setRollover(boolean b){
        if((isRollover()==b)||!isEnabled()){
            return;
        }
        if(b){
            stateMask|=ROLLOVER;
        }else{
            stateMask&=~ROLLOVER;
        }
        fireStateChanged();
    }

    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class,l);
    }    public void setMnemonic(int key){
        mnemonic=key;
        fireStateChanged();
    }

    public int getMnemonic(){
        return mnemonic;
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







    protected void fireActionPerformed(ActionEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ActionListener.class){
                // Lazily create the event:
                // if (changeEvent == null)
                // changeEvent = new ChangeEvent(this);
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }







    protected void fireItemStateChanged(ItemEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ItemListener.class){
                // Lazily create the event:
                // if (changeEvent == null)
                // changeEvent = new ChangeEvent(this);
                ((ItemListener)listeners[i+1]).itemStateChanged(e);
            }
        }
    }









    boolean isMenuItem(){
        return menuItem;
    }

    void setMenuItem(boolean menuItem){
        this.menuItem=menuItem;
    }
}
