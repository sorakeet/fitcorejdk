/**
 * Copyright (c) 1995, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AWTAccessor;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.event.KeyEvent;
import java.awt.peer.MenuBarPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Vector;

public class MenuBar extends MenuComponent implements MenuContainer, Accessible{
    private static final String base="menubar";
    private static final long serialVersionUID=-4930327919388951260L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
        AWTAccessor.setMenuBarAccessor(
                new AWTAccessor.MenuBarAccessor(){
                    public Menu getHelpMenu(MenuBar menuBar){
                        return menuBar.helpMenu;
                    }

                    public Vector<Menu> getMenus(MenuBar menuBar){
                        return menuBar.menus;
                    }
                });
    }

    Vector<Menu> menus=new Vector<>();
    Menu helpMenu;
    private int menuBarSerializedDataVersion=1;

    public MenuBar() throws HeadlessException{
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(MenuBar.class){
            return base+nameCounter++;
        }
    }

    public void removeNotify(){
        synchronized(getTreeLock()){
            int nmenus=getMenuCount();
            for(int i=0;i<nmenus;i++){
                getMenu(i).removeNotify();
            }
            super.removeNotify();
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTMenuBar();
        }
        return accessibleContext;
    }

    int getAccessibleChildIndex(MenuComponent child){
        return menus.indexOf(child);
    }

    public int getMenuCount(){
        return countMenus();
    }

    @Deprecated
    public int countMenus(){
        return getMenuCountImpl();
    }

    final int getMenuCountImpl(){
        return menus.size();
    }

    public Menu getMenu(int i){
        return getMenuImpl(i);
    }

    final Menu getMenuImpl(int i){
        return menus.elementAt(i);
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=Toolkit.getDefaultToolkit().createMenuBar(this);
            int nmenus=getMenuCount();
            for(int i=0;i<nmenus;i++){
                getMenu(i).addNotify();
            }
        }
    }

    public Menu getHelpMenu(){
        return helpMenu;
    }

    public void setHelpMenu(final Menu m){
        synchronized(getTreeLock()){
            if(helpMenu==m){
                return;
            }
            if(helpMenu!=null){
                remove(helpMenu);
            }
            helpMenu=m;
            if(m!=null){
                if(m.parent!=this){
                    add(m);
                }
                m.isHelpMenu=true;
                m.parent=this;
                MenuBarPeer peer=(MenuBarPeer)this.peer;
                if(peer!=null){
                    if(m.peer==null){
                        m.addNotify();
                    }
                    peer.addHelpMenu(m);
                }
            }
        }
    }

    public Menu add(Menu m){
        synchronized(getTreeLock()){
            if(m.parent!=null){
                m.parent.remove(m);
            }
            m.parent=this;
            MenuBarPeer peer=(MenuBarPeer)this.peer;
            if(peer!=null){
                if(m.peer==null){
                    m.addNotify();
                }
                menus.addElement(m);
                peer.addMenu(m);
            }else{
                menus.addElement(m);
            }
            return m;
        }
    }

    public void remove(MenuComponent m){
        synchronized(getTreeLock()){
            int index=menus.indexOf(m);
            if(index>=0){
                remove(index);
            }
        }
    }

    public void remove(final int index){
        synchronized(getTreeLock()){
            Menu m=getMenu(index);
            menus.removeElementAt(index);
            MenuBarPeer peer=(MenuBarPeer)this.peer;
            if(peer!=null){
                peer.delMenu(index);
                m.removeNotify();
                m.parent=null;
            }
            if(helpMenu==m){
                helpMenu=null;
                m.isHelpMenu=false;
            }
        }
    }

    public synchronized Enumeration<MenuShortcut> shortcuts(){
        Vector<MenuShortcut> shortcuts=new Vector<>();
        int nmenus=getMenuCount();
        for(int i=0;i<nmenus;i++){
            Enumeration<MenuShortcut> e=getMenu(i).shortcuts();
            while(e.hasMoreElements()){
                shortcuts.addElement(e.nextElement());
            }
        }
        return shortcuts.elements();
    }

    public MenuItem getShortcutMenuItem(MenuShortcut s){
        int nmenus=getMenuCount();
        for(int i=0;i<nmenus;i++){
            MenuItem mi=getMenu(i).getShortcutMenuItem(s);
            if(mi!=null){
                return mi;
            }
        }
        return null;  // MenuShortcut wasn't found
    }

    boolean handleShortcut(KeyEvent e){
        // Is it a key event?
        int id=e.getID();
        if(id!=KeyEvent.KEY_PRESSED&&id!=KeyEvent.KEY_RELEASED){
            return false;
        }
        // Is the accelerator modifier key pressed?
        int accelKey=Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if((e.getModifiers()&accelKey)==0){
            return false;
        }
        // Pass MenuShortcut on to child menus.
        int nmenus=getMenuCount();
        for(int i=0;i<nmenus;i++){
            Menu m=getMenu(i);
            if(m.handleShortcut(e)){
                return true;
            }
        }
        return false;
    }

    public void deleteShortcut(MenuShortcut s){
        int nmenus=getMenuCount();
        for(int i=0;i<nmenus;i++){
            getMenu(i).deleteShortcut(s);
        }
    }
/////////////////
// Accessibility support
////////////////

    private void writeObject(java.io.ObjectOutputStream s)
            throws ClassNotFoundException,
            IOException{
        s.defaultWriteObject();
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException, HeadlessException{
        // HeadlessException will be thrown from MenuComponent's readObject
        s.defaultReadObject();
        for(int i=0;i<menus.size();i++){
            Menu m=menus.elementAt(i);
            m.parent=this;
        }
    }

    protected class AccessibleAWTMenuBar extends AccessibleAWTMenuComponent{
        private static final long serialVersionUID=-8577604491830083815L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.MENU_BAR;
        }
    } // class AccessibleAWTMenuBar
}
