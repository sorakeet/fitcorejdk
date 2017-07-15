/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.peer.ChoicePeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;
import java.util.Vector;

public class Choice extends Component implements ItemSelectable, Accessible{
    private static final String base="choice";
    private static final long serialVersionUID=-4075310674757313071L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        /** initialize JNI field and method ids */
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    Vector<String> pItems;
    int selectedIndex=-1;
    transient ItemListener itemListener;
    /** Serialization support.
     */
    private int choiceSerializedDataVersion=1;

    public Choice() throws HeadlessException{
        GraphicsEnvironment.checkHeadless();
        pItems=new Vector<>();
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(Choice.class){
            return base+nameCounter++;
        }
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

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=getToolkit().createChoice(this);
            super.addNotify();
        }
    }

    protected String paramString(){
        return super.paramString()+",current="+getSelectedItem();
    }

    public synchronized String getSelectedItem(){
        return (selectedIndex>=0)?getItem(selectedIndex):null;
    }

    public String getItem(int index){
        return getItemImpl(index);
    }

    final String getItemImpl(int index){
        return pItems.elementAt(index);
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTChoice();
        }
        return accessibleContext;
    }

    protected void processItemEvent(ItemEvent e){
        ItemListener listener=itemListener;
        if(listener!=null){
            listener.itemStateChanged(e);
        }
    }

    public int getItemCount(){
        return countItems();
    }

    @Deprecated
    public int countItems(){
        return pItems.size();
    }

    public void add(String item){
        addItem(item);
    }

    public void addItem(String item){
        synchronized(this){
            insertNoInvalidate(item,pItems.size());
        }
        // This could change the preferred size of the Component.
        invalidateIfValid();
    }

    private void insertNoInvalidate(String item,int index){
        if(item==null){
            throw new
                    NullPointerException("cannot add null item to Choice");
        }
        pItems.insertElementAt(item,index);
        ChoicePeer peer=(ChoicePeer)this.peer;
        if(peer!=null){
            peer.add(item,index);
        }
        // no selection or selection shifted up
        if(selectedIndex<0||selectedIndex>=index){
            select(0);
        }
    }

    public synchronized void select(int pos){
        if((pos>=pItems.size())||(pos<0)){
            throw new IllegalArgumentException("illegal Choice item position: "+pos);
        }
        if(pItems.size()>0){
            selectedIndex=pos;
            ChoicePeer peer=(ChoicePeer)this.peer;
            if(peer!=null){
                peer.select(pos);
            }
        }
    }

    public void insert(String item,int index){
        synchronized(this){
            if(index<0){
                throw new IllegalArgumentException("index less than zero.");
            }
            /** if the index greater than item count, add item to the end */
            index=Math.min(index,pItems.size());
            insertNoInvalidate(item,index);
        }
        // This could change the preferred size of the Component.
        invalidateIfValid();
    }

    public void remove(String item){
        synchronized(this){
            int index=pItems.indexOf(item);
            if(index<0){
                throw new IllegalArgumentException("item "+item+
                        " not found in choice");
            }else{
                removeNoInvalidate(index);
            }
        }
        // This could change the preferred size of the Component.
        invalidateIfValid();
    }

    private void removeNoInvalidate(int position){
        pItems.removeElementAt(position);
        ChoicePeer peer=(ChoicePeer)this.peer;
        if(peer!=null){
            peer.remove(position);
        }
        /** Adjust selectedIndex if selected item was removed. */
        if(pItems.size()==0){
            selectedIndex=-1;
        }else if(selectedIndex==position){
            select(0);
        }else if(selectedIndex>position){
            select(selectedIndex-1);
        }
    }

    public void remove(int position){
        synchronized(this){
            removeNoInvalidate(position);
        }
        // This could change the preferred size of the Component.
        invalidateIfValid();
    }

    public void removeAll(){
        synchronized(this){
            if(peer!=null){
                ((ChoicePeer)peer).removeAll();
            }
            pItems.removeAllElements();
            selectedIndex=-1;
        }
        // This could change the preferred size of the Component.
        invalidateIfValid();
    }

    public synchronized Object[] getSelectedObjects(){
        if(selectedIndex>=0){
            Object[] items=new Object[1];
            items[0]=getItem(selectedIndex);
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

    public int getSelectedIndex(){
        return selectedIndex;
    }

    public synchronized void select(String str){
        int index=pItems.indexOf(str);
        if(index>=0){
            select(index);
        }
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
            throws ClassNotFoundException, IOException, HeadlessException{
        GraphicsEnvironment.checkHeadless();
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

    protected class AccessibleAWTChoice extends AccessibleAWTComponent
            implements AccessibleAction{
        private static final long serialVersionUID=7175603582428509322L;

        public AccessibleAWTChoice(){
            super();
        }

        public AccessibleAction getAccessibleAction(){
            return this;
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.COMBO_BOX;
        }

        public int getAccessibleActionCount(){
            return 0;  //  To be fully implemented in a future release
        }

        public String getAccessibleActionDescription(int i){
            return null;  //  To be fully implemented in a future release
        }

        public boolean doAccessibleAction(int i){
            return false;  //  To be fully implemented in a future release
        }
    } // inner class AccessibleAWTChoice
}
