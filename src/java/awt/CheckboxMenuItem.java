/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AWTAccessor;

import javax.accessibility.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.peer.CheckboxMenuItemPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;

public class CheckboxMenuItem extends MenuItem implements ItemSelectable, Accessible{
    private static final String base="chkmenuitem";
    private static final long serialVersionUID=6190621106981774043L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
        AWTAccessor.setCheckboxMenuItemAccessor(
                new AWTAccessor.CheckboxMenuItemAccessor(){
                    public boolean getState(CheckboxMenuItem cmi){
                        return cmi.state;
                    }
                });
    }

    boolean state=false;
    transient ItemListener itemListener;
    /** Serialization support.
     */
    private int checkboxMenuItemSerializedDataVersion=1;

    public CheckboxMenuItem() throws HeadlessException{
        this("",false);
    }

    public CheckboxMenuItem(String label,boolean state)
            throws HeadlessException{
        super(label);
        this.state=state;
    }

    public CheckboxMenuItem(String label) throws HeadlessException{
        this(label,false);
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(CheckboxMenuItem.class){
            return base+nameCounter++;
        }
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=Toolkit.getDefaultToolkit().createCheckboxMenuItem(this);
            super.addNotify();
        }
    }

    void doMenuEvent(long when,int modifiers){
        setState(!state);
        Toolkit.getEventQueue().postEvent(
                new ItemEvent(this,ItemEvent.ITEM_STATE_CHANGED,
                        getLabel(),
                        state?ItemEvent.SELECTED:
                                ItemEvent.DESELECTED));
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        EventListener l=null;
        if(listenerType==ItemListener.class){
            l=itemListener;
        }else{
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l,listenerType);
    }

    protected void processEvent(AWTEvent e){
        if(e instanceof ItemEvent){
            processItemEvent((ItemEvent)e);
            return;
        }
        super.processEvent(e);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e){
        if(e.id==ItemEvent.ITEM_STATE_CHANGED){
            if((eventMask&AWTEvent.ITEM_EVENT_MASK)!=0||
                    itemListener!=null){
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    public String paramString(){
        return super.paramString()+",state="+state;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTCheckboxMenuItem();
        }
        return accessibleContext;
    }

    protected void processItemEvent(ItemEvent e){
        ItemListener listener=itemListener;
        if(listener!=null){
            listener.itemStateChanged(e);
        }
    }

    public boolean getState(){
        return state;
    }

    public synchronized void setState(boolean b){
        state=b;
        CheckboxMenuItemPeer peer=(CheckboxMenuItemPeer)this.peer;
        if(peer!=null){
            peer.setState(b);
        }
    }

    public synchronized Object[] getSelectedObjects(){
        if(state){
            Object[] items=new Object[1];
            items[0]=label;
            return items;
        }
        return null;
    }

    public synchronized void addItemListener(ItemListener l){
        if(l==null){
            return;
        }
        itemListener=AWTEventMulticaster.add(itemListener,l);
        newEventsOnly=true;
    }

    public synchronized void removeItemListener(ItemListener l){
        if(l==null){
            return;
        }
        itemListener=AWTEventMulticaster.remove(itemListener,l);
    }

    public synchronized ItemListener[] getItemListeners(){
        return getListeners(ItemListener.class);
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
        AWTEventMulticaster.save(s,itemListenerK,itemListener);
        s.writeObject(null);
    }
/////////////////
// Accessibility support
////////////////

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        s.defaultReadObject();
        Object keyOrNull;
        while(null!=(keyOrNull=s.readObject())){
            String key=((String)keyOrNull).intern();
            if(itemListenerK==key)
                addItemListener((ItemListener)(s.readObject()));
            else // skip value for unrecognized key
                s.readObject();
        }
    }

    protected class AccessibleAWTCheckboxMenuItem extends AccessibleAWTMenuItem
            implements AccessibleAction, AccessibleValue{
        private static final long serialVersionUID=-1122642964303476L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.CHECK_BOX;
        }

        public AccessibleAction getAccessibleAction(){
            return this;
        }

        public AccessibleValue getAccessibleValue(){
            return this;
        }

        public int getAccessibleActionCount(){
            return 0;  //  To be fully implemented in a future release
        }

        public String getAccessibleActionDescription(int i){
            return null;  //  To be fully implemented in a future release
        }

        public boolean doAccessibleAction(int i){
            return false;    //  To be fully implemented in a future release
        }

        public Number getCurrentAccessibleValue(){
            return null;  //  To be fully implemented in a future release
        }

        public boolean setCurrentAccessibleValue(Number n){
            return false;  //  To be fully implemented in a future release
        }

        public Number getMinimumAccessibleValue(){
            return null;  //  To be fully implemented in a future release
        }

        public Number getMaximumAccessibleValue(){
            return null;  //  To be fully implemented in a future release
        }
    } // class AccessibleAWTMenuItem
}
