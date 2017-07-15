/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import javax.accessibility.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.peer.CheckboxPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;

public class Checkbox extends Component implements ItemSelectable, Accessible{
    private static final String base="checkbox";
    private static final long serialVersionUID=7270714317450821763L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    String label;
    boolean state;
    CheckboxGroup group;
    transient ItemListener itemListener;
    /** Serialization support.
     */
    private int checkboxSerializedDataVersion=1;

    public Checkbox() throws HeadlessException{
        this("",false,null);
    }

    public Checkbox(String label,boolean state,CheckboxGroup group)
            throws HeadlessException{
        GraphicsEnvironment.checkHeadless();
        this.label=label;
        this.state=state;
        this.group=group;
        if(state&&(group!=null)){
            group.setSelectedCheckbox(this);
        }
    }

    public Checkbox(String label) throws HeadlessException{
        this(label,false,null);
    }

    public Checkbox(String label,boolean state) throws HeadlessException{
        this(label,state,null);
    }

    public Checkbox(String label,CheckboxGroup group,boolean state)
            throws HeadlessException{
        this(label,state,group);
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(Checkbox.class){
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
                peer=getToolkit().createCheckbox(this);
            super.addNotify();
        }
    }

    protected String paramString(){
        String str=super.paramString();
        String label=this.label;
        if(label!=null){
            str+=",label="+label;
        }
        return str+",state="+state;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTCheckbox();
        }
        return accessibleContext;
    }

    protected void processItemEvent(ItemEvent e){
        ItemListener listener=itemListener;
        if(listener!=null){
            listener.itemStateChanged(e);
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
                CheckboxPeer peer=(CheckboxPeer)this.peer;
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

    public Object[] getSelectedObjects(){
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

    public CheckboxGroup getCheckboxGroup(){
        return group;
    }

    public void setCheckboxGroup(CheckboxGroup g){
        CheckboxGroup oldGroup;
        boolean oldState;
        /** Do nothing if this check box has already belonged
         * to the check box group g.
         */
        if(this.group==g){
            return;
        }
        synchronized(this){
            oldGroup=this.group;
            oldState=getState();
            this.group=g;
            CheckboxPeer peer=(CheckboxPeer)this.peer;
            if(peer!=null){
                peer.setCheckboxGroup(g);
            }
            if(this.group!=null&&getState()){
                if(this.group.getSelectedCheckbox()!=null){
                    setState(false);
                }else{
                    this.group.setSelectedCheckbox(this);
                }
            }
        }
        /** Locking check box below could cause deadlock with
         * CheckboxGroup's setSelectedCheckbox method.
         *
         * Fix for 4726853 by kdm@sparc.spb.su
         * Here we should check if this check box was selected
         * in the previous group and set selected check box to
         * null for that group if so.
         */
        if(oldGroup!=null&&oldState){
            oldGroup.setSelectedCheckbox(null);
        }
    }

    public boolean getState(){
        return state;
    }

    public void setState(boolean state){
        /** Cannot hold check box lock when calling group.setSelectedCheckbox. */
        CheckboxGroup group=this.group;
        if(group!=null){
            if(state){
                group.setSelectedCheckbox(this);
            }else if(group.getSelectedCheckbox()==this){
                state=true;
            }
        }
        setStateInternal(state);
    }

    void setStateInternal(boolean state){
        this.state=state;
        CheckboxPeer peer=(CheckboxPeer)this.peer;
        if(peer!=null){
            peer.setState(state);
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

    protected class AccessibleAWTCheckbox extends AccessibleAWTComponent
            implements ItemListener, AccessibleAction, AccessibleValue{
        private static final long serialVersionUID=7881579233144754107L;

        public AccessibleAWTCheckbox(){
            super();
            Checkbox.this.addItemListener(this);
        }

        public void itemStateChanged(ItemEvent e){
            Checkbox cb=(Checkbox)e.getSource();
            if(Checkbox.this.accessibleContext!=null){
                if(cb.getState()){
                    Checkbox.this.accessibleContext.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            null,AccessibleState.CHECKED);
                }else{
                    Checkbox.this.accessibleContext.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            AccessibleState.CHECKED,null);
                }
            }
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

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.CHECK_BOX;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            if(getState()){
                states.add(AccessibleState.CHECKED);
            }
            return states;
        }
    } // inner class AccessibleAWTCheckbox
}
