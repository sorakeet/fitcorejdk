/**
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.peer.LabelPeer;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Label extends Component implements Accessible{
    public static final int LEFT=0;
    public static final int CENTER=1;
    public static final int RIGHT=2;
    private static final String base="label";
    private static final long serialVersionUID=3094126758329070636L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    String text;
    int alignment=LEFT;

    public Label() throws HeadlessException{
        this("",LEFT);
    }

    public Label(String text,int alignment) throws HeadlessException{
        GraphicsEnvironment.checkHeadless();
        this.text=text;
        setAlignment(alignment);
    }

    public Label(String text) throws HeadlessException{
        this(text,LEFT);
    }

    private static native void initIDs();

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException, HeadlessException{
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();
    }

    String constructComponentName(){
        synchronized(Label.class){
            return base+nameCounter++;
        }
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=getToolkit().createLabel(this);
            super.addNotify();
        }
    }

    protected String paramString(){
        String align="";
        switch(alignment){
            case LEFT:
                align="left";
                break;
            case CENTER:
                align="center";
                break;
            case RIGHT:
                align="right";
                break;
        }
        return super.paramString()+",align="+align+",text="+text;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTLabel();
        }
        return accessibleContext;
    }

    public int getAlignment(){
        return alignment;
    }

    public synchronized void setAlignment(int alignment){
        switch(alignment){
            case LEFT:
            case CENTER:
            case RIGHT:
                this.alignment=alignment;
                LabelPeer peer=(LabelPeer)this.peer;
                if(peer!=null){
                    peer.setAlignment(alignment);
                }
                return;
        }
        throw new IllegalArgumentException("improper alignment: "+alignment);
    }

    public String getText(){
        return text;
    }
/////////////////
// Accessibility support
////////////////

    public void setText(String text){
        boolean testvalid=false;
        synchronized(this){
            if(text!=this.text&&(this.text==null||
                    !this.text.equals(text))){
                this.text=text;
                LabelPeer peer=(LabelPeer)this.peer;
                if(peer!=null){
                    peer.setText(text);
                }
                testvalid=true;
            }
        }
        // This could change the preferred size of the Component.
        if(testvalid){
            invalidateIfValid();
        }
    }

    protected class AccessibleAWTLabel extends AccessibleAWTComponent{
        private static final long serialVersionUID=-3568967560160480438L;

        public AccessibleAWTLabel(){
            super();
        }

        public String getAccessibleName(){
            if(accessibleName!=null){
                return accessibleName;
            }else{
                if(getText()==null){
                    return super.getAccessibleName();
                }else{
                    return getText();
                }
            }
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.LABEL;
        }
    } // inner class AccessibleAWTLabel
}
