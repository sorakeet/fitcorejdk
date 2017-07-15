/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import javax.accessibility.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.ButtonPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;

public class Button extends Component implements Accessible{
    private static final String base="button";
    private static final long serialVersionUID=-8774683716313001058L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    String label;
    String actionCommand;
    transient ActionListener actionListener;
    /** Serialization support.
     */
    private int buttonSerializedDataVersion=1;

    public Button() throws HeadlessException{
        this("");
    }

    public Button(String label) throws HeadlessException{
        GraphicsEnvironment.checkHeadless();
        this.label=label;
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(Button.class){
            return base+nameCounter++;
        }
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e){
        if(e.id==ActionEvent.ACTION_PERFORMED){
            if((eventMask&AWTEvent.ACTION_EVENT_MASK)!=0||
                    actionListener!=null){
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        EventListener l=null;
        if(listenerType==ActionListener.class){
            l=actionListener;
        }else{
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l,listenerType);
    }

    protected void processEvent(AWTEvent e){
        if(e instanceof ActionEvent){
            processActionEvent((ActionEvent)e);
            return;
        }
        super.processEvent(e);
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=getToolkit().createButton(this);
            super.addNotify();
        }
    }

    protected String paramString(){
        return super.paramString()+",label="+label;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTButton();
        }
        return accessibleContext;
    }

    protected void processActionEvent(ActionEvent e){
        ActionListener listener=actionListener;
        if(listener!=null){
            listener.actionPerformed(e);
        }
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label){
        boolean testvalid=false;
        synchronized(this){
            if(label!=this.label&&(this.label==null||
                    !this.label.equals(label))){
                this.label=label;
                ButtonPeer peer=(ButtonPeer)this.peer;
                if(peer!=null){
                    peer.setLabel(label);
                }
                testvalid=true;
            }
        }
        // This could change the preferred size of the Component.
        if(testvalid){
            invalidateIfValid();
        }
    }

    public String getActionCommand(){
        return (actionCommand==null?label:actionCommand);
    }

    public void setActionCommand(String command){
        actionCommand=command;
    }

    public synchronized void removeActionListener(ActionListener l){
        if(l==null){
            return;
        }
        actionListener=AWTEventMulticaster.remove(actionListener,l);
    }

    public synchronized ActionListener[] getActionListeners(){
        return getListeners(ActionListener.class);
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
        AWTEventMulticaster.save(s,actionListenerK,actionListener);
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException, HeadlessException{
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();
        Object keyOrNull;
        while(null!=(keyOrNull=s.readObject())){
            String key=((String)keyOrNull).intern();
            if(actionListenerK==key)
                addActionListener((ActionListener)(s.readObject()));
            else // skip value for unrecognized key
                s.readObject();
        }
    }
/////////////////
// Accessibility support
////////////////

    public synchronized void addActionListener(ActionListener l){
        if(l==null){
            return;
        }
        actionListener=AWTEventMulticaster.add(actionListener,l);
        newEventsOnly=true;
    }

    protected class AccessibleAWTButton extends AccessibleAWTComponent
            implements AccessibleAction, AccessibleValue{
        private static final long serialVersionUID=-5932203980244017102L;

        public String getAccessibleName(){
            if(accessibleName!=null){
                return accessibleName;
            }else{
                if(getLabel()==null){
                    return super.getAccessibleName();
                }else{
                    return getLabel();
                }
            }
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.PUSH_BUTTON;
        }

        public AccessibleAction getAccessibleAction(){
            return this;
        }

        public AccessibleValue getAccessibleValue(){
            return this;
        }

        public int getAccessibleActionCount(){
            return 1;
        }

        public String getAccessibleActionDescription(int i){
            if(i==0){
                // [[[PENDING:  WDW -- need to provide a localized string]]]
                return "click";
            }else{
                return null;
            }
        }

        public boolean doAccessibleAction(int i){
            if(i==0){
                // Simulate a button click
                Toolkit.getEventQueue().postEvent(
                        new ActionEvent(Button.this,
                                ActionEvent.ACTION_PERFORMED,
                                Button.this.getActionCommand()));
                return true;
            }else{
                return false;
            }
        }

        public Number getCurrentAccessibleValue(){
            return Integer.valueOf(0);
        }

        public boolean setCurrentAccessibleValue(Number n){
            return false;
        }

        public Number getMinimumAccessibleValue(){
            return Integer.valueOf(0);
        }

        public Number getMaximumAccessibleValue(){
            return Integer.valueOf(0);
        }
    } // inner class AccessibleAWTButton
}
