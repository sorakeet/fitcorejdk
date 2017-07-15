/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.swing.DefaultLookup;
import sun.swing.UIAction;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

public class BasicMenuUI extends BasicMenuItemUI{
    private static final boolean TRACE=false; // trace creates and disposes
    private static final boolean VERBOSE=false; // show reuse hits/misses
    private static final boolean DEBUG=false;  // show bad params, misc.
    private static boolean crossMenuMnemonic=true;
    protected ChangeListener changeListener;
    protected MenuListener menuListener;
    private int lastMnemonic=0;
    private InputMap selectedWindowInputMap;

    public static ComponentUI createUI(JComponent x){
        return new BasicMenuUI();
    }

    static void loadActionMap(LazyActionMap map){
        BasicMenuItemUI.loadActionMap(map);
        map.put(new Actions(Actions.SELECT,null,true));
    }

    private static void appendPath(MenuElement[] path,MenuElement elem){
        MenuElement newPath[]=new MenuElement[path.length+1];
        System.arraycopy(path,0,newPath,0,path.length);
        newPath[path.length]=elem;
        MenuSelectionManager.defaultManager().setSelectedPath(newPath);
    }

    protected void installDefaults(){
        super.installDefaults();
        updateDefaultBackgroundColor();
        ((JMenu)menuItem).setDelay(200);
        crossMenuMnemonic=UIManager.getBoolean("Menu.crossMenuMnemonic");
    }

    protected String getPropertyPrefix(){
        return "Menu";
    }

    protected void installListeners(){
        super.installListeners();
        if(changeListener==null)
            changeListener=createChangeListener(menuItem);
        if(changeListener!=null)
            menuItem.addChangeListener(changeListener);
        if(menuListener==null)
            menuListener=createMenuListener(menuItem);
        if(menuListener!=null)
            ((JMenu)menuItem).addMenuListener(menuListener);
    }

    protected void installKeyboardActions(){
        super.installKeyboardActions();
        updateMnemonicBinding();
    }

    void installLazyActionMap(){
        LazyActionMap.installLazyActionMap(menuItem,BasicMenuUI.class,
                getPropertyPrefix()+".actionMap");
    }

    protected void uninstallDefaults(){
        menuItem.setArmed(false);
        menuItem.setSelected(false);
        menuItem.resetKeyboardActions();
        super.uninstallDefaults();
    }

    protected void uninstallListeners(){
        super.uninstallListeners();
        if(changeListener!=null)
            menuItem.removeChangeListener(changeListener);
        if(menuListener!=null)
            ((JMenu)menuItem).removeMenuListener(menuListener);
        changeListener=null;
        menuListener=null;
        handler=null;
    }

    protected void uninstallKeyboardActions(){
        super.uninstallKeyboardActions();
        lastMnemonic=0;
    }

    protected MouseInputListener createMouseInputListener(JComponent c){
        return getHandler();
    }

    protected MenuDragMouseListener createMenuDragMouseListener(JComponent c){
        return getHandler();
    }

    protected MenuKeyListener createMenuKeyListener(JComponent c){
        return (MenuKeyListener)getHandler();
    }

    protected PropertyChangeListener createPropertyChangeListener(JComponent c){
        return getHandler();
    }

    BasicMenuItemUI.Handler getHandler(){
        if(handler==null){
            handler=new Handler();
        }
        return handler;
    }

    public Dimension getMaximumSize(JComponent c){
        if(((JMenu)menuItem).isTopLevelMenu()==true){
            Dimension d=c.getPreferredSize();
            return new Dimension(d.width,Short.MAX_VALUE);
        }
        return null;
    }

    void updateMnemonicBinding(){
        int mnemonic=menuItem.getModel().getMnemonic();
        int[] shortcutKeys=(int[])DefaultLookup.get(menuItem,this,
                "Menu.shortcutKeys");
        if(shortcutKeys==null){
            shortcutKeys=new int[]{KeyEvent.ALT_MASK};
        }
        if(mnemonic==lastMnemonic){
            return;
        }
        InputMap windowInputMap=SwingUtilities.getUIInputMap(
                menuItem,JComponent.WHEN_IN_FOCUSED_WINDOW);
        if(lastMnemonic!=0&&windowInputMap!=null){
            for(int shortcutKey : shortcutKeys){
                windowInputMap.remove(KeyStroke.getKeyStroke
                        (lastMnemonic,shortcutKey,false));
            }
        }
        if(mnemonic!=0){
            if(windowInputMap==null){
                windowInputMap=createInputMap(JComponent.
                        WHEN_IN_FOCUSED_WINDOW);
                SwingUtilities.replaceUIInputMap(menuItem,JComponent.
                        WHEN_IN_FOCUSED_WINDOW,windowInputMap);
            }
            for(int shortcutKey : shortcutKeys){
                windowInputMap.put(KeyStroke.getKeyStroke(mnemonic,
                        shortcutKey,false),"selectMenu");
            }
        }
        lastMnemonic=mnemonic;
    }

    protected MenuListener createMenuListener(JComponent c){
        return null;
    }

    protected ChangeListener createChangeListener(JComponent c){
        return null;
    }

    private void updateDefaultBackgroundColor(){
        if(!UIManager.getBoolean("Menu.useMenuBarBackgroundForTopLevel")){
            return;
        }
        JMenu menu=(JMenu)menuItem;
        if(menu.getBackground() instanceof UIResource){
            if(menu.isTopLevelMenu()){
                menu.setBackground(UIManager.getColor("MenuBar.background"));
            }else{
                menu.setBackground(UIManager.getColor(getPropertyPrefix()+".background"));
            }
        }
    }

    protected void setupPostTimer(JMenu menu){
        Timer timer=new Timer(menu.getDelay(),new Actions(
                Actions.SELECT,menu,false));
        timer.setRepeats(false);
        timer.start();
    }

    private static class Actions extends UIAction{
        private static final String SELECT="selectMenu";
        // NOTE: This will be null if the action is registered in the
        // ActionMap. For the timer use it will be non-null.
        private JMenu menu;
        private boolean force=false;

        Actions(String key,JMenu menu,boolean shouldForce){
            super(key);
            this.menu=menu;
            this.force=shouldForce;
        }

        public void actionPerformed(ActionEvent e){
            JMenu menu=getMenu(e);
            if(!crossMenuMnemonic){
                JPopupMenu pm=BasicPopupMenuUI.getLastPopup();
                if(pm!=null&&pm!=menu.getParent()){
                    return;
                }
            }
            final MenuSelectionManager defaultManager=MenuSelectionManager.defaultManager();
            if(force){
                Container cnt=menu.getParent();
                if(cnt!=null&&cnt instanceof JMenuBar){
                    MenuElement me[];
                    MenuElement subElements[];
                    subElements=menu.getPopupMenu().getSubElements();
                    if(subElements.length>0){
                        me=new MenuElement[4];
                        me[0]=(MenuElement)cnt;
                        me[1]=menu;
                        me[2]=menu.getPopupMenu();
                        me[3]=subElements[0];
                    }else{
                        me=new MenuElement[3];
                        me[0]=(MenuElement)cnt;
                        me[1]=menu;
                        me[2]=menu.getPopupMenu();
                    }
                    defaultManager.setSelectedPath(me);
                }
            }else{
                MenuElement path[]=defaultManager.getSelectedPath();
                if(path.length>0&&path[path.length-1]==menu){
                    appendPath(path,menu.getPopupMenu());
                }
            }
        }

        private JMenu getMenu(ActionEvent e){
            if(e.getSource() instanceof JMenu){
                return (JMenu)e.getSource();
            }
            return menu;
        }

        public boolean isEnabled(Object c){
            if(c instanceof JMenu){
                return ((JMenu)c).isEnabled();
            }
            return true;
        }
    }

    protected class MouseInputHandler implements MouseInputListener{
        // NOTE: This class exists only for backward compatibility. All
        // its functionality has been moved into Handler. If you need to add
        // new functionality add it to the Handler, but make sure this
        // class calls into the Handler.

        public void mouseClicked(MouseEvent e){
            getHandler().mouseClicked(e);
        }

        public void mousePressed(MouseEvent e){
            getHandler().mousePressed(e);
        }

        public void mouseReleased(MouseEvent e){
            getHandler().mouseReleased(e);
        }

        public void mouseEntered(MouseEvent e){
            getHandler().mouseEntered(e);
        }

        public void mouseExited(MouseEvent e){
            getHandler().mouseExited(e);
        }

        public void mouseDragged(MouseEvent e){
            getHandler().mouseDragged(e);
        }

        public void mouseMoved(MouseEvent e){
            getHandler().mouseMoved(e);
        }
    }

    public class ChangeHandler implements ChangeListener{
        public JMenu menu;
        public BasicMenuUI ui;
        public boolean isSelected=false;
        public Component wasFocused;

        public ChangeHandler(JMenu m,BasicMenuUI ui){
            menu=m;
            this.ui=ui;
        }

        public void stateChanged(ChangeEvent e){
        }
    }

    private class Handler extends BasicMenuItemUI.Handler implements MenuKeyListener{
        //
        // MouseInputListener
        //
        public void mouseClicked(MouseEvent e){
        }

        public void mousePressed(MouseEvent e){
            JMenu menu=(JMenu)menuItem;
            if(!menu.isEnabled())
                return;
            MenuSelectionManager manager=
                    MenuSelectionManager.defaultManager();
            if(menu.isTopLevelMenu()){
                if(menu.isSelected()&&menu.getPopupMenu().isShowing()){
                    manager.clearSelectedPath();
                }else{
                    Container cnt=menu.getParent();
                    if(cnt!=null&&cnt instanceof JMenuBar){
                        MenuElement me[]=new MenuElement[2];
                        me[0]=(MenuElement)cnt;
                        me[1]=menu;
                        manager.setSelectedPath(me);
                    }
                }
            }
            MenuElement selectedPath[]=manager.getSelectedPath();
            if(selectedPath.length>0&&
                    selectedPath[selectedPath.length-1]!=menu.getPopupMenu()){
                if(menu.isTopLevelMenu()||
                        menu.getDelay()==0){
                    appendPath(selectedPath,menu.getPopupMenu());
                }else{
                    setupPostTimer(menu);
                }
            }
        }

        public void mouseReleased(MouseEvent e){
            JMenu menu=(JMenu)menuItem;
            if(!menu.isEnabled())
                return;
            MenuSelectionManager manager=
                    MenuSelectionManager.defaultManager();
            manager.processMouseEvent(e);
            if(!e.isConsumed())
                manager.clearSelectedPath();
        }

        public void mouseEntered(MouseEvent e){
            JMenu menu=(JMenu)menuItem;
            // only disable the menu highlighting if it's disabled and the property isn't
            // true. This allows disabled rollovers to work in WinL&F
            if(!menu.isEnabled()&&!UIManager.getBoolean("MenuItem.disabledAreNavigable")){
                return;
            }
            MenuSelectionManager manager=
                    MenuSelectionManager.defaultManager();
            MenuElement selectedPath[]=manager.getSelectedPath();
            if(!menu.isTopLevelMenu()){
                if(!(selectedPath.length>0&&
                        selectedPath[selectedPath.length-1]==
                                menu.getPopupMenu())){
                    if(menu.getDelay()==0){
                        appendPath(getPath(),menu.getPopupMenu());
                    }else{
                        manager.setSelectedPath(getPath());
                        setupPostTimer(menu);
                    }
                }
            }else{
                if(selectedPath.length>0&&
                        selectedPath[0]==menu.getParent()){
                    MenuElement newPath[]=new MenuElement[3];
                    // A top level menu's parent is by definition
                    // a JMenuBar
                    newPath[0]=(MenuElement)menu.getParent();
                    newPath[1]=menu;
                    if(BasicPopupMenuUI.getLastPopup()!=null){
                        newPath[2]=menu.getPopupMenu();
                    }
                    manager.setSelectedPath(newPath);
                }
            }
        }

        public void mouseExited(MouseEvent e){
        }

        public void mouseDragged(MouseEvent e){
            JMenu menu=(JMenu)menuItem;
            if(!menu.isEnabled())
                return;
            MenuSelectionManager.defaultManager().processMouseEvent(e);
        }

        public void mouseMoved(MouseEvent e){
        }

        //
        // MenuDragHandler
        //
        public void menuDragMouseEntered(MenuDragMouseEvent e){
        }

        public void menuDragMouseDragged(MenuDragMouseEvent e){
            if(menuItem.isEnabled()==false)
                return;
            MenuSelectionManager manager=e.getMenuSelectionManager();
            MenuElement path[]=e.getPath();
            Point p=e.getPoint();
            if(p.x>=0&&p.x<menuItem.getWidth()&&
                    p.y>=0&&p.y<menuItem.getHeight()){
                JMenu menu=(JMenu)menuItem;
                MenuElement selectedPath[]=manager.getSelectedPath();
                if(!(selectedPath.length>0&&
                        selectedPath[selectedPath.length-1]==
                                menu.getPopupMenu())){
                    if(menu.isTopLevelMenu()||
                            menu.getDelay()==0||
                            e.getID()==MouseEvent.MOUSE_DRAGGED){
                        appendPath(path,menu.getPopupMenu());
                    }else{
                        manager.setSelectedPath(path);
                        setupPostTimer(menu);
                    }
                }
            }else if(e.getID()==MouseEvent.MOUSE_RELEASED){
                Component comp=manager.componentForPoint(e.getComponent(),e.getPoint());
                if(comp==null)
                    manager.clearSelectedPath();
            }
        }

        public void menuDragMouseExited(MenuDragMouseEvent e){
        }

        public void menuDragMouseReleased(MenuDragMouseEvent e){
        }

        //
        // PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent e){
            if(e.getPropertyName()==AbstractButton.
                    MNEMONIC_CHANGED_PROPERTY){
                updateMnemonicBinding();
            }else{
                if(e.getPropertyName().equals("ancestor")){
                    updateDefaultBackgroundColor();
                }
                super.propertyChange(e);
            }
        }

        //
        // MenuKeyListener
        //
        public void menuKeyTyped(MenuKeyEvent e){
            if(!crossMenuMnemonic&&BasicPopupMenuUI.getLastPopup()!=null){
                // when crossMenuMnemonic is not set, we don't open a toplevel
                // menu if another toplevel menu is already open
                return;
            }
            if(BasicPopupMenuUI.getPopups().size()!=0){
                //Fix 6939261: to return in case not on the main menu
                //and has a pop-up.
                //after return code will be handled in BasicPopupMenuUI.java
                return;
            }
            char key=Character.toLowerCase((char)menuItem.getMnemonic());
            MenuElement path[]=e.getPath();
            if(key==Character.toLowerCase(e.getKeyChar())){
                JPopupMenu popupMenu=((JMenu)menuItem).getPopupMenu();
                ArrayList newList=new ArrayList(Arrays.asList(path));
                newList.add(popupMenu);
                MenuElement subs[]=popupMenu.getSubElements();
                MenuElement sub=
                        BasicPopupMenuUI.findEnabledChild(subs,-1,true);
                if(sub!=null){
                    newList.add(sub);
                }
                MenuSelectionManager manager=e.getMenuSelectionManager();
                MenuElement newPath[]=new MenuElement[0];
                ;
                newPath=(MenuElement[])newList.toArray(newPath);
                manager.setSelectedPath(newPath);
                e.consume();
            }
        }

        public void menuKeyPressed(MenuKeyEvent e){
        }

        public void menuKeyReleased(MenuKeyEvent e){
        }
    }
}
