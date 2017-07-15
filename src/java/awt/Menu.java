/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AWTAccessor;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.event.KeyEvent;
import java.awt.peer.MenuPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Vector;

public class Menu extends MenuItem implements MenuContainer, Accessible{
    private static final String base="menu";
    private static final long serialVersionUID=-8809584163345499784L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
        AWTAccessor.setMenuAccessor(
                new AWTAccessor.MenuAccessor(){
                    public Vector<MenuComponent> getItems(Menu menu){
                        return menu.items;
                    }
                });
    }

    Vector<MenuComponent> items=new Vector<>();
    boolean tearOff;
    boolean isHelpMenu;
    private int menuSerializedDataVersion=1;

    public Menu() throws HeadlessException{
        this("",false);
    }

    public Menu(String label,boolean tearOff) throws HeadlessException{
        super(label);
        this.tearOff=tearOff;
    }

    public Menu(String label) throws HeadlessException{
        this(label,false);
    }

    private static native void initIDs();

    String constructComponentName(){
        synchronized(Menu.class){
            return base+nameCounter++;
        }
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=Toolkit.getDefaultToolkit().createMenu(this);
            int nitems=getItemCount();
            for(int i=0;i<nitems;i++){
                MenuItem mi=getItem(i);
                mi.parent=this;
                mi.addNotify();
            }
        }
    }

    public int getItemCount(){
        return countItems();
    }

    @Deprecated
    public int countItems(){
        return countItemsImpl();
    }

    final int countItemsImpl(){
        return items.size();
    }

    public MenuItem getItem(int index){
        return getItemImpl(index);
    }

    final MenuItem getItemImpl(int index){
        return (MenuItem)items.elementAt(index);
    }

    void deleteShortcut(MenuShortcut s){
        int nitems=getItemCount();
        for(int i=0;i<nitems;i++){
            getItem(i).deleteShortcut(s);
        }
    }

    boolean handleShortcut(KeyEvent e){
        int nitems=getItemCount();
        for(int i=0;i<nitems;i++){
            MenuItem mi=getItem(i);
            if(mi.handleShortcut(e)){
                return true;
            }
        }
        return false;
    }

    MenuItem getShortcutMenuItem(MenuShortcut s){
        int nitems=getItemCount();
        for(int i=0;i<nitems;i++){
            MenuItem mi=getItem(i).getShortcutMenuItem(s);
            if(mi!=null){
                return mi;
            }
        }
        return null;
    }

    public String paramString(){
        String str=",tearOff="+tearOff+",isHelpMenu="+isHelpMenu;
        return super.paramString()+str;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTMenu();
        }
        return accessibleContext;
    }

    public void removeNotify(){
        synchronized(getTreeLock()){
            int nitems=getItemCount();
            for(int i=0;i<nitems;i++){
                getItem(i).removeNotify();
            }
            super.removeNotify();
        }
    }

    int getAccessibleChildIndex(MenuComponent child){
        return items.indexOf(child);
    }

    public boolean isTearOff(){
        return tearOff;
    }

    public void insert(String label,int index){
        insert(new MenuItem(label),index);
    }

    public void insert(MenuItem menuitem,int index){
        synchronized(getTreeLock()){
            if(index<0){
                throw new IllegalArgumentException("index less than zero.");
            }
            int nitems=getItemCount();
            Vector<MenuItem> tempItems=new Vector<>();
            /** Remove the item at index, nitems-index times
             storing them in a temporary vector in the
             order they appear on the menu.
             */
            for(int i=index;i<nitems;i++){
                tempItems.addElement(getItem(index));
                remove(index);
            }
            add(menuitem);
            /** Add the removed items back to the menu, they are
             already in the correct order in the temp vector.
             */
            for(int i=0;i<tempItems.size();i++){
                add(tempItems.elementAt(i));
            }
        }
    }

    public MenuItem add(MenuItem mi){
        synchronized(getTreeLock()){
            if(mi.parent!=null){
                mi.parent.remove(mi);
            }
            items.addElement(mi);
            mi.parent=this;
            MenuPeer peer=(MenuPeer)this.peer;
            if(peer!=null){
                mi.addNotify();
                peer.addItem(mi);
            }
            return mi;
        }
    }

    public void remove(int index){
        synchronized(getTreeLock()){
            MenuItem mi=getItem(index);
            items.removeElementAt(index);
            MenuPeer peer=(MenuPeer)this.peer;
            if(peer!=null){
                peer.delItem(index);
                mi.removeNotify();
                mi.parent=null;
            }
        }
    }

    public void insertSeparator(int index){
        synchronized(getTreeLock()){
            if(index<0){
                throw new IllegalArgumentException("index less than zero.");
            }
            int nitems=getItemCount();
            Vector<MenuItem> tempItems=new Vector<>();
            /** Remove the item at index, nitems-index times
             storing them in a temporary vector in the
             order they appear on the menu.
             */
            for(int i=index;i<nitems;i++){
                tempItems.addElement(getItem(index));
                remove(index);
            }
            addSeparator();
            /** Add the removed items back to the menu, they are
             already in the correct order in the temp vector.
             */
            for(int i=0;i<tempItems.size();i++){
                add(tempItems.elementAt(i));
            }
        }
    }

    public void addSeparator(){
        add("-");
    }

    public void add(String label){
        add(new MenuItem(label));
    }

    public void remove(MenuComponent item){
        synchronized(getTreeLock()){
            int index=items.indexOf(item);
            if(index>=0){
                remove(index);
            }
        }
    }

    public void removeAll(){
        synchronized(getTreeLock()){
            int nitems=getItemCount();
            for(int i=nitems-1;i>=0;i--){
                remove(i);
            }
        }
    }

    synchronized Enumeration<MenuShortcut> shortcuts(){
        Vector<MenuShortcut> shortcuts=new Vector<>();
        int nitems=getItemCount();
        for(int i=0;i<nitems;i++){
            MenuItem mi=getItem(i);
            if(mi instanceof Menu){
                Enumeration<MenuShortcut> e=((Menu)mi).shortcuts();
                while(e.hasMoreElements()){
                    shortcuts.addElement(e.nextElement());
                }
            }else{
                MenuShortcut ms=mi.getShortcut();
                if(ms!=null){
                    shortcuts.addElement(ms);
                }
            }
        }
        return shortcuts.elements();
    }
/////////////////
// Accessibility support
////////////////

    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException, HeadlessException{
        // HeadlessException will be thrown from MenuComponent's readObject
        s.defaultReadObject();
        for(int i=0;i<items.size();i++){
            MenuItem item=(MenuItem)items.elementAt(i);
            item.parent=this;
        }
    }

    protected class AccessibleAWTMenu extends AccessibleAWTMenuItem{
        private static final long serialVersionUID=5228160894980069094L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.MENU;
        }
    } // class AccessibleAWTMenu
}
