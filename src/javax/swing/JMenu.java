/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.PopupMenuUI;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

@SuppressWarnings("serial")
public class JMenu extends JMenuItem implements Accessible, MenuElement{
    private static final String uiClassID="MenuUI";
    private static final boolean TRACE=false; // trace creates and disposes
    private static final boolean VERBOSE=false; // show reuse hits/misses
    private static final boolean DEBUG=false;  // show bad params, misc.
    protected WinListener popupListener;
    private JPopupMenu popupMenu;
    private ChangeListener menuChangeListener=null;
    private MenuEvent menuEvent=null;
    private int delay;
    private Point customMenuLocation=null;

    public JMenu(Action a){
        this();
        setAction(a);
    }

    public JMenu(){
        this("");
    }

    public JMenu(String s){
        super(s);
    }

    public JMenu(String s,boolean b){
        this(s);
    }

    public void setModel(ButtonModel newModel){
        ButtonModel oldModel=getModel();
        super.setModel(newModel);
        if(oldModel!=null&&menuChangeListener!=null){
            oldModel.removeChangeListener(menuChangeListener);
            menuChangeListener=null;
        }
        model=newModel;
        if(newModel!=null){
            menuChangeListener=createMenuChangeListener();
            newModel.addChangeListener(menuChangeListener);
        }
    }

    void initFocusability(){
    }
    //    public void repaint(long tm, int x, int y, int width, int height) {
    //        Thread.currentThread().dumpStack();
    //        super.repaint(tm,x,y,width,height);
    //    }

    public void updateUI(){
        setUI((MenuItemUI)UIManager.getUI(this));
        if(popupMenu!=null){
            popupMenu.setUI((PopupMenuUI)UIManager.getUI(popupMenu));
        }
    }

    public String getUIClassID(){
        return uiClassID;
    }

    public void setAccelerator(KeyStroke keyStroke){
        throw new Error("setAccelerator() is not defined for JMenu.  Use setMnemonic() instead.");
    }

    // Overriden to do nothing, JMenu doesn't support an accelerator
    void configureAcceleratorFromAction(Action a){
    }

    public void menuSelectionChanged(boolean isIncluded){
        if(DEBUG){
            System.out.println("In JMenu.menuSelectionChanged to "+isIncluded);
        }
        setSelected(isIncluded);
    }

    public MenuElement[] getSubElements(){
        if(popupMenu==null)
            return new MenuElement[0];
        else{
            MenuElement result[]=new MenuElement[1];
            result[0]=popupMenu;
            return result;
        }
    }

    // implements javax.swing.MenuElement
    public Component getComponent(){
        return this;
    }

    protected String paramString(){
        return super.paramString();
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJMenu();
        }
        return accessibleContext;
    }

    private ChangeListener createMenuChangeListener(){
        return new MenuChangeListener();
    }

    public boolean isSelected(){
        return getModel().isSelected();
    }

    public void setSelected(boolean b){
        ButtonModel model=getModel();
        boolean oldValue=model.isSelected();
        // TIGER - 4840653
        // Removed code which fired an AccessibleState.SELECTED
        // PropertyChangeEvent since this resulted in two
        // identical events being fired since
        // AbstractButton.fireItemStateChanged also fires the
        // same event. This caused screen readers to speak the
        // name of the item twice.
        if(b!=model.isSelected()){
            getModel().setSelected(b);
        }
    }

    public void doClick(int pressTime){
        MenuElement me[]=buildMenuElementArray(this);
        MenuSelectionManager.defaultManager().setSelectedPath(me);
    }

    private MenuElement[] buildMenuElementArray(JMenu leaf){
        Vector<MenuElement> elements=new Vector<MenuElement>();
        Component current=leaf.getPopupMenu();
        JPopupMenu pop;
        JMenu menu;
        JMenuBar bar;
        while(true){
            if(current instanceof JPopupMenu){
                pop=(JPopupMenu)current;
                elements.insertElementAt(pop,0);
                current=pop.getInvoker();
            }else if(current instanceof JMenu){
                menu=(JMenu)current;
                elements.insertElementAt(menu,0);
                current=menu.getParent();
            }else if(current instanceof JMenuBar){
                bar=(JMenuBar)current;
                elements.insertElementAt(bar,0);
                MenuElement me[]=new MenuElement[elements.size()];
                elements.copyInto(me);
                return me;
            }
        }
    }

    public boolean isPopupMenuVisible(){
        ensurePopupMenuCreated();
        return popupMenu.isVisible();
    }

    public void setPopupMenuVisible(boolean b){
        if(DEBUG){
            System.out.println("in JMenu.setPopupMenuVisible "+b);
            // Thread.dumpStack();
        }
        boolean isVisible=isPopupMenuVisible();
        if(b!=isVisible&&(isEnabled()||!b)){
            ensurePopupMenuCreated();
            if((b==true)&&isShowing()){
                // Set location of popupMenu (pulldown or pullright)
                Point p=getCustomMenuLocation();
                if(p==null){
                    p=getPopupMenuOrigin();
                }
                getPopupMenu().show(this,p.x,p.y);
            }else{
                getPopupMenu().setVisible(false);
            }
        }
    }

    protected Point getPopupMenuOrigin(){
        int x;
        int y;
        JPopupMenu pm=getPopupMenu();
        // Figure out the sizes needed to caclulate the menu position
        Dimension s=getSize();
        Dimension pmSize=pm.getSize();
        // For the first time the menu is popped up,
        // the size has not yet been initiated
        if(pmSize.width==0){
            pmSize=pm.getPreferredSize();
        }
        Point position=getLocationOnScreen();
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        GraphicsConfiguration gc=getGraphicsConfiguration();
        Rectangle screenBounds=new Rectangle(toolkit.getScreenSize());
        GraphicsEnvironment ge=
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd=ge.getScreenDevices();
        for(int i=0;i<gd.length;i++){
            if(gd[i].getType()==GraphicsDevice.TYPE_RASTER_SCREEN){
                GraphicsConfiguration dgc=
                        gd[i].getDefaultConfiguration();
                if(dgc.getBounds().contains(position)){
                    gc=dgc;
                    break;
                }
            }
        }
        if(gc!=null){
            screenBounds=gc.getBounds();
            // take screen insets (e.g. taskbar) into account
            Insets screenInsets=toolkit.getScreenInsets(gc);
            screenBounds.width-=
                    Math.abs(screenInsets.left+screenInsets.right);
            screenBounds.height-=
                    Math.abs(screenInsets.top+screenInsets.bottom);
            position.x-=Math.abs(screenInsets.left);
            position.y-=Math.abs(screenInsets.top);
        }
        Container parent=getParent();
        if(parent instanceof JPopupMenu){
            // We are a submenu (pull-right)
            int xOffset=UIManager.getInt("Menu.submenuPopupOffsetX");
            int yOffset=UIManager.getInt("Menu.submenuPopupOffsetY");
            if(SwingUtilities.isLeftToRight(this)){
                // First determine x:
                x=s.width+xOffset;   // Prefer placement to the right
                if(position.x+x+pmSize.width>=screenBounds.width
                        +screenBounds.x&&
                        // popup doesn't fit - place it wherever there's more room
                        screenBounds.width-s.width<2*(position.x
                                -screenBounds.x)){
                    x=0-xOffset-pmSize.width;
                }
            }else{
                // First determine x:
                x=0-xOffset-pmSize.width; // Prefer placement to the left
                if(position.x+x<screenBounds.x&&
                        // popup doesn't fit - place it wherever there's more room
                        screenBounds.width-s.width>2*(position.x-
                                screenBounds.x)){
                    x=s.width+xOffset;
                }
            }
            // Then the y:
            y=yOffset;                     // Prefer dropping down
            if(position.y+y+pmSize.height>=screenBounds.height
                    +screenBounds.y&&
                    // popup doesn't fit - place it wherever there's more room
                    screenBounds.height-s.height<2*(position.y
                            -screenBounds.y)){
                y=s.height-yOffset-pmSize.height;
            }
        }else{
            // We are a toplevel menu (pull-down)
            int xOffset=UIManager.getInt("Menu.menuPopupOffsetX");
            int yOffset=UIManager.getInt("Menu.menuPopupOffsetY");
            if(SwingUtilities.isLeftToRight(this)){
                // First determine the x:
                x=xOffset;                   // Extend to the right
                if(position.x+x+pmSize.width>=screenBounds.width
                        +screenBounds.x&&
                        // popup doesn't fit - place it wherever there's more room
                        screenBounds.width-s.width<2*(position.x
                                -screenBounds.x)){
                    x=s.width-xOffset-pmSize.width;
                }
            }else{
                // First determine the x:
                x=s.width-xOffset-pmSize.width; // Extend to the left
                if(position.x+x<screenBounds.x&&
                        // popup doesn't fit - place it wherever there's more room
                        screenBounds.width-s.width>2*(position.x
                                -screenBounds.x)){
                    x=xOffset;
                }
            }
            // Then the y:
            y=s.height+yOffset;    // Prefer dropping down
            if(position.y+y+pmSize.height>=screenBounds.height
                    +screenBounds.y&&
                    // popup doesn't fit - place it wherever there's more room
                    screenBounds.height-s.height<2*(position.y
                            -screenBounds.y)){
                y=0-yOffset-pmSize.height;   // Otherwise drop 'up'
            }
        }
        return new Point(x,y);
    }

    public int getDelay(){
        return delay;
    }

    public void setDelay(int d){
        if(d<0)
            throw new IllegalArgumentException("Delay must be a positive integer");
        delay=d;
    }

    private Point getCustomMenuLocation(){
        return customMenuLocation;
    }

    public void setMenuLocation(int x,int y){
        customMenuLocation=new Point(x,y);
        if(popupMenu!=null)
            popupMenu.setLocation(x,y);
    }

    public Component add(Component c){
        ensurePopupMenuCreated();
        popupMenu.add(c);
        return c;
    }

    private void ensurePopupMenuCreated(){
        if(popupMenu==null){
            final JMenu thisMenu=this;
            this.popupMenu=new JPopupMenu();
            popupMenu.setInvoker(this);
            popupListener=createWinListener(popupMenu);
        }
    }

    protected WinListener createWinListener(JPopupMenu p){
        return new WinListener(p);
    }

    public Component add(Component c,int index){
        ensurePopupMenuCreated();
        popupMenu.add(c,index);
        return c;
    }

    public void remove(int pos){
        if(pos<0){
            throw new IllegalArgumentException("index less than zero.");
        }
        if(pos>getItemCount()){
            throw new IllegalArgumentException("index greater than the number of items.");
        }
        if(popupMenu!=null)
            popupMenu.remove(pos);
    }

    public int getItemCount(){
        return getMenuComponentCount();
    }

    public int getMenuComponentCount(){
        int componentCount=0;
        if(popupMenu!=null)
            componentCount=popupMenu.getComponentCount();
        return componentCount;
    }

    public void remove(Component c){
        if(popupMenu!=null)
            popupMenu.remove(c);
    }

    public void removeAll(){
        if(popupMenu!=null)
            popupMenu.removeAll();
    }

    public void applyComponentOrientation(ComponentOrientation o){
        super.applyComponentOrientation(o);
        if(popupMenu!=null){
            int ncomponents=getMenuComponentCount();
            for(int i=0;i<ncomponents;++i){
                getMenuComponent(i).applyComponentOrientation(o);
            }
            popupMenu.setComponentOrientation(o);
        }
    }

    public JMenuItem add(String s){
        return add(new JMenuItem(s));
    }

    public JMenuItem add(JMenuItem menuItem){
        ensurePopupMenuCreated();
        return popupMenu.add(menuItem);
    }

    public JMenuItem add(Action a){
        JMenuItem mi=createActionComponent(a);
        mi.setAction(a);
        add(mi);
        return mi;
    }

    protected JMenuItem createActionComponent(Action a){
        JMenuItem mi=new JMenuItem(){
            protected PropertyChangeListener createActionPropertyChangeListener(Action a){
                PropertyChangeListener pcl=createActionChangeListener(this);
                if(pcl==null){
                    pcl=super.createActionPropertyChangeListener(a);
                }
                return pcl;
            }
        };
        mi.setHorizontalTextPosition(JButton.TRAILING);
        mi.setVerticalTextPosition(JButton.CENTER);
        return mi;
    }

    protected PropertyChangeListener createActionChangeListener(JMenuItem b){
        return b.createActionPropertyChangeListener0(b.getAction());
    }

    public void addSeparator(){
        ensurePopupMenuCreated();
        popupMenu.addSeparator();
    }

    public void insert(String s,int pos){
        if(pos<0){
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(new JMenuItem(s),pos);
    }

    public JMenuItem insert(JMenuItem mi,int pos){
        if(pos<0){
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(mi,pos);
        return mi;
    }

    public JMenuItem insert(Action a,int pos){
        if(pos<0){
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        JMenuItem mi=new JMenuItem(a);
        mi.setHorizontalTextPosition(JButton.TRAILING);
        mi.setVerticalTextPosition(JButton.CENTER);
        popupMenu.insert(mi,pos);
        return mi;
    }

    public void insertSeparator(int index){
        if(index<0){
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(new JPopupMenu.Separator(),index);
    }

    public JMenuItem getItem(int pos){
        if(pos<0){
            throw new IllegalArgumentException("index less than zero.");
        }
        Component c=getMenuComponent(pos);
        if(c instanceof JMenuItem){
            JMenuItem mi=(JMenuItem)c;
            return mi;
        }
        // 4173633
        return null;
    }

    public Component getMenuComponent(int n){
        if(popupMenu!=null)
            return popupMenu.getComponent(n);
        return null;
    }

    public boolean isTearOff(){
        throw new Error("boolean isTearOff() {} not yet implemented");
    }

    public void remove(JMenuItem item){
        if(popupMenu!=null)
            popupMenu.remove(item);
    }

    public boolean isTopLevelMenu(){
        return getParent() instanceof JMenuBar;
    }

    public boolean isMenuComponent(Component c){
        // Are we in the MenuItem part of the menu
        if(c==this)
            return true;
        // Are we in the PopupMenu?
        if(c instanceof JPopupMenu){
            JPopupMenu comp=(JPopupMenu)c;
            if(comp==this.getPopupMenu())
                return true;
        }
        // Are we in a Component on the PopupMenu
        int ncomponents=this.getMenuComponentCount();
        Component[] component=this.getMenuComponents();
        for(int i=0;i<ncomponents;i++){
            Component comp=component[i];
            // Are we in the current component?
            if(comp==c)
                return true;
            // Hmmm, what about Non-menu containers?
            // Recursive call for the Menu case
            if(comp instanceof JMenu){
                JMenu subMenu=(JMenu)comp;
                if(subMenu.isMenuComponent(c))
                    return true;
            }
        }
        return false;
    }

    public Component[] getMenuComponents(){
        if(popupMenu!=null)
            return popupMenu.getComponents();
        return new Component[0];
    }

    public JPopupMenu getPopupMenu(){
        ensurePopupMenuCreated();
        return popupMenu;
    }

    private Point translateToPopupMenu(Point p){
        return translateToPopupMenu(p.x,p.y);
    }

    private Point translateToPopupMenu(int x,int y){
        int newX;
        int newY;
        if(getParent() instanceof JPopupMenu){
            newX=x-getSize().width;
            newY=y;
        }else{
            newX=x;
            newY=y-getSize().height;
        }
        return new Point(newX,newY);
    }

    public void addMenuListener(MenuListener l){
        listenerList.add(MenuListener.class,l);
    }

    public void removeMenuListener(MenuListener l){
        listenerList.remove(MenuListener.class,l);
    }

    public MenuListener[] getMenuListeners(){
        return listenerList.getListeners(MenuListener.class);
    }

    protected void fireMenuSelected(){
        if(DEBUG){
            System.out.println("In JMenu.fireMenuSelected");
        }
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==MenuListener.class){
                if(listeners[i+1]==null){
                    throw new Error(getText()+" has a NULL Listener!! "+i);
                }else{
                    // Lazily create the event:
                    if(menuEvent==null)
                        menuEvent=new MenuEvent(this);
                    ((MenuListener)listeners[i+1]).menuSelected(menuEvent);
                }
            }
        }
    }

    protected void fireMenuDeselected(){
        if(DEBUG){
            System.out.println("In JMenu.fireMenuDeselected");
        }
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==MenuListener.class){
                if(listeners[i+1]==null){
                    throw new Error(getText()+" has a NULL Listener!! "+i);
                }else{
                    // Lazily create the event:
                    if(menuEvent==null)
                        menuEvent=new MenuEvent(this);
                    ((MenuListener)listeners[i+1]).menuDeselected(menuEvent);
                }
            }
        }
    }

    protected void fireMenuCanceled(){
        if(DEBUG){
            System.out.println("In JMenu.fireMenuCanceled");
        }
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==MenuListener.class){
                if(listeners[i+1]==null){
                    throw new Error(getText()+" has a NULL Listener!! "
                            +i);
                }else{
                    // Lazily create the event:
                    if(menuEvent==null)
                        menuEvent=new MenuEvent(this);
                    ((MenuListener)listeners[i+1]).menuCanceled(menuEvent);
                }
            }
        }
    }

    public void setComponentOrientation(ComponentOrientation o){
        super.setComponentOrientation(o);
        if(popupMenu!=null){
            popupMenu.setComponentOrientation(o);
        }
    }

    protected void processKeyEvent(KeyEvent evt){
        MenuSelectionManager.defaultManager().processKeyEvent(evt);
        if(evt.isConsumed())
            return;
        super.processKeyEvent(evt);
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                ui.installUI(this);
            }
        }
    }

    @SuppressWarnings("serial")
    class MenuChangeListener implements ChangeListener, Serializable{
        boolean isSelected=false;

        public void stateChanged(ChangeEvent e){
            ButtonModel model=(ButtonModel)e.getSource();
            boolean modelSelected=model.isSelected();
            if(modelSelected!=isSelected){
                if(modelSelected==true){
                    fireMenuSelected();
                }else{
                    fireMenuDeselected();
                }
                isSelected=modelSelected;
            }
        }
    }
/////////////////
// Accessibility support
////////////////

    @SuppressWarnings("serial")
    protected class WinListener extends WindowAdapter implements Serializable{
        JPopupMenu popupMenu;

        public WinListener(JPopupMenu p){
            this.popupMenu=p;
        }

        public void windowClosing(WindowEvent e){
            setSelected(false);
        }
    }

    @SuppressWarnings("serial")
    protected class AccessibleJMenu extends AccessibleJMenuItem
            implements AccessibleSelection{
        public int getAccessibleChildrenCount(){
            Component[] children=getMenuComponents();
            int count=0;
            for(Component child : children){
                if(child instanceof Accessible){
                    count++;
                }
            }
            return count;
        }

        public Accessible getAccessibleChild(int i){
            Component[] children=getMenuComponents();
            int count=0;
            for(Component child : children){
                if(child instanceof Accessible){
                    if(count==i){
                        if(child instanceof JComponent){
                            // FIXME:  [[[WDW - probably should set this when
                            // the component is added to the menu.  I tried
                            // to do this in most cases, but the separators
                            // added by addSeparator are hard to get to.]]]
                            AccessibleContext ac=child.getAccessibleContext();
                            ac.setAccessibleParent(JMenu.this);
                        }
                        return (Accessible)child;
                    }else{
                        count++;
                    }
                }
            }
            return null;
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.MENU;
        }

        public AccessibleSelection getAccessibleSelection(){
            return this;
        }

        public int getAccessibleSelectionCount(){
            MenuElement me[]=
                    MenuSelectionManager.defaultManager().getSelectedPath();
            if(me!=null){
                for(int i=0;i<me.length;i++){
                    if(me[i]==JMenu.this){   // this menu is selected
                        if(i+1<me.length){
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }

        public Accessible getAccessibleSelection(int i){
            // if i is a sub-menu & popped, return it
            if(i<0||i>=getItemCount()){
                return null;
            }
            MenuElement me[]=
                    MenuSelectionManager.defaultManager().getSelectedPath();
            if(me!=null){
                for(int j=0;j<me.length;j++){
                    if(me[j]==JMenu.this){   // this menu is selected
                        // so find the next JMenuItem in the MenuElement
                        // array, and return it!
                        while(++j<me.length){
                            if(me[j] instanceof JMenuItem){
                                return (Accessible)me[j];
                            }
                        }
                    }
                }
            }
            return null;
        }

        public boolean isAccessibleChildSelected(int i){
            // if i is a sub-menu and is pop-ed up, return true, else false
            MenuElement me[]=
                    MenuSelectionManager.defaultManager().getSelectedPath();
            if(me!=null){
                JMenuItem mi=JMenu.this.getItem(i);
                for(int j=0;j<me.length;j++){
                    if(me[j]==mi){
                        return true;
                    }
                }
            }
            return false;
        }

        public void addAccessibleSelection(int i){
            if(i<0||i>=getItemCount()){
                return;
            }
            JMenuItem mi=getItem(i);
            if(mi!=null){
                if(mi instanceof JMenu){
                    MenuElement me[]=buildMenuElementArray((JMenu)mi);
                    MenuSelectionManager.defaultManager().setSelectedPath(me);
                }else{
                    MenuSelectionManager.defaultManager().setSelectedPath(null);
                }
            }
        }

        public void removeAccessibleSelection(int i){
            if(i<0||i>=getItemCount()){
                return;
            }
            JMenuItem mi=getItem(i);
            if(mi!=null&&mi instanceof JMenu){
                if(mi.isSelected()){
                    MenuElement old[]=
                            MenuSelectionManager.defaultManager().getSelectedPath();
                    MenuElement me[]=new MenuElement[old.length-2];
                    for(int j=0;j<old.length-2;j++){
                        me[j]=old[j];
                    }
                    MenuSelectionManager.defaultManager().setSelectedPath(me);
                }
            }
        }

        public void clearAccessibleSelection(){
            // if this menu is selected, reset selection to only go
            // to this menu; else do nothing
            MenuElement old[]=
                    MenuSelectionManager.defaultManager().getSelectedPath();
            if(old!=null){
                for(int j=0;j<old.length;j++){
                    if(old[j]==JMenu.this){  // menu is in the selection!
                        MenuElement me[]=new MenuElement[j+1];
                        System.arraycopy(old,0,me,0,j);
                        me[j]=JMenu.this.getPopupMenu();
                        MenuSelectionManager.defaultManager().setSelectedPath(me);
                    }
                }
            }
        }

        public void selectAllAccessibleSelection(){
        }
    } // inner class AccessibleJMenu
}
