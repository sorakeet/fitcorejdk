/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AWTAccessor;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.peer.PopupMenuPeer;

public class PopupMenu extends Menu{
    private static final String base="popup";
    private static final long serialVersionUID=-4620452533522760060L;
    static int nameCounter=0;

    static{
        AWTAccessor.setPopupMenuAccessor(
                new AWTAccessor.PopupMenuAccessor(){
                    public boolean isTrayIconPopup(PopupMenu popupMenu){
                        return popupMenu.isTrayIconPopup;
                    }
                });
    }

    transient boolean isTrayIconPopup=false;

    public PopupMenu() throws HeadlessException{
        this("");
    }

    public PopupMenu(String label) throws HeadlessException{
        super(label);
    }

    public MenuContainer getParent(){
        if(isTrayIconPopup){
            return null;
        }
        return super.getParent();
    }

    String constructComponentName(){
        synchronized(PopupMenu.class){
            return base+nameCounter++;
        }
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            // If our parent is not a Component, then this PopupMenu is
            // really just a plain, old Menu.
            if(parent!=null&&!(parent instanceof Component)){
                super.addNotify();
            }else{
                if(peer==null)
                    peer=Toolkit.getDefaultToolkit().createPopupMenu(this);
                int nitems=getItemCount();
                for(int i=0;i<nitems;i++){
                    MenuItem mi=getItem(i);
                    mi.parent=this;
                    mi.addNotify();
                }
            }
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTPopupMenu();
        }
        return accessibleContext;
    }
/////////////////
// Accessibility support
////////////////

    public void show(Component origin,int x,int y){
        // Use localParent for thread safety.
        MenuContainer localParent=parent;
        if(localParent==null){
            throw new NullPointerException("parent is null");
        }
        if(!(localParent instanceof Component)){
            throw new IllegalArgumentException(
                    "PopupMenus with non-Component parents cannot be shown");
        }
        Component compParent=(Component)localParent;
        //Fixed 6278745: Incorrect exception throwing in PopupMenu.show() method
        //Exception was not thrown if compParent was not equal to origin and
        //was not Container
        if(compParent!=origin){
            if(compParent instanceof Container){
                if(!((Container)compParent).isAncestorOf(origin)){
                    throw new IllegalArgumentException("origin not in parent's hierarchy");
                }
            }else{
                throw new IllegalArgumentException("origin not in parent's hierarchy");
            }
        }
        if(compParent.getPeer()==null||!compParent.isShowing()){
            throw new RuntimeException("parent not showing on screen");
        }
        if(peer==null){
            addNotify();
        }
        synchronized(getTreeLock()){
            if(peer!=null){
                ((PopupMenuPeer)peer).show(
                        new Event(origin,0,Event.MOUSE_DOWN,x,y,0,0));
            }
        }
    }

    protected class AccessibleAWTPopupMenu extends AccessibleAWTMenu{
        private static final long serialVersionUID=-4282044795947239955L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.POPUP_MENU;
        }
    } // class AccessibleAWTPopupMenu
}
