/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.TextFieldPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;

public class TextField extends TextComponent{
    private static final String base="textfield";
    private static final long serialVersionUID=-2966288784432217853L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    int columns;
    char echoChar;
    transient ActionListener actionListener;
    private int textFieldSerializedDataVersion=1;

    public TextField() throws HeadlessException{
        this("",0);
    }

    public TextField(String text,int columns) throws HeadlessException{
        super(text);
        this.columns=(columns>=0)?columns:0;
    }

    public TextField(String text) throws HeadlessException{
        this(text,(text!=null)?text.length():0);
    }

    public TextField(int columns) throws HeadlessException{
        this("",columns);
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(TextField.class){
            return base+nameCounter++;
        }
    }

    public Dimension getPreferredSize(){
        return preferredSize();
    }

    @Deprecated
    public Dimension preferredSize(){
        synchronized(getTreeLock()){
            return (columns>0)?
                    preferredSize(columns):
                    super.preferredSize();
        }
    }

    public Dimension getMinimumSize(){
        return minimumSize();
    }

    @Deprecated
    public Dimension minimumSize(){
        synchronized(getTreeLock()){
            return (columns>0)?
                    minimumSize(columns):
                    super.minimumSize();
        }
    }

    @Deprecated
    public Dimension minimumSize(int columns){
        synchronized(getTreeLock()){
            TextFieldPeer peer=(TextFieldPeer)this.peer;
            return (peer!=null)?
                    peer.getMinimumSize(columns):
                    super.minimumSize();
        }
    }

    @Deprecated
    public Dimension preferredSize(int columns){
        synchronized(getTreeLock()){
            TextFieldPeer peer=(TextFieldPeer)this.peer;
            return (peer!=null)?
                    peer.getPreferredSize(columns):
                    super.preferredSize();
        }
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=getToolkit().createTextField(this);
            super.addNotify();
        }
    }

    public void setText(String t){
        super.setText(t);
        // This could change the preferred size of the Component.
        invalidateIfValid();
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

    protected void processEvent(AWTEvent e){
        if(e instanceof ActionEvent){
            processActionEvent((ActionEvent)e);
            return;
        }
        super.processEvent(e);
    }

    protected void processActionEvent(ActionEvent e){
        ActionListener listener=actionListener;
        if(listener!=null){
            listener.actionPerformed(e);
        }
    }

    protected String paramString(){
        String str=super.paramString();
        if(echoChar!=0){
            str+=",echo="+echoChar;
        }
        return str;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTTextField();
        }
        return accessibleContext;
    }

    public char getEchoChar(){
        return echoChar;
    }

    public void setEchoChar(char c){
        setEchoCharacter(c);
    }

    @Deprecated
    public synchronized void setEchoCharacter(char c){
        if(echoChar!=c){
            echoChar=c;
            TextFieldPeer peer=(TextFieldPeer)this.peer;
            if(peer!=null){
                peer.setEchoChar(c);
            }
        }
    }

    public boolean echoCharIsSet(){
        return echoChar!=0;
    }

    public int getColumns(){
        return columns;
    }

    public void setColumns(int columns){
        int oldVal;
        synchronized(this){
            oldVal=this.columns;
            if(columns<0){
                throw new IllegalArgumentException("columns less than zero.");
            }
            if(columns!=oldVal){
                this.columns=columns;
            }
        }
        if(columns!=oldVal){
            invalidate();
        }
    }

    public Dimension getPreferredSize(int columns){
        return preferredSize(columns);
    }

    public Dimension getMinimumSize(int columns){
        return minimumSize(columns);
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
        // HeadlessException will be thrown by TextComponent's readObject
        s.defaultReadObject();
        // Make sure the state we just read in for columns has legal values
        if(columns<0){
            columns=0;
        }
        // Read in listeners, if any
        Object keyOrNull;
        while(null!=(keyOrNull=s.readObject())){
            String key=((String)keyOrNull).intern();
            if(actionListenerK==key){
                addActionListener((ActionListener)(s.readObject()));
            }else{
                // skip value for unrecognized key
                s.readObject();
            }
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

    protected class AccessibleAWTTextField extends AccessibleAWTTextComponent{
        private static final long serialVersionUID=6219164359235943158L;

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            states.add(AccessibleState.SINGLE_LINE);
            return states;
        }
    }
}
