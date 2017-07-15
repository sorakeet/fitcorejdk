/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.*;
import javax.swing.plaf.MenuBarUI;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

@SuppressWarnings("serial")
public class JMenuBar extends JComponent implements Accessible, MenuElement{
    private static final String uiClassID="MenuBarUI";
    private static final boolean TRACE=false; // trace creates and disposes
    private static final boolean VERBOSE=false; // show reuse hits/misses
    private static final boolean DEBUG=false;  // show bad params, misc.
    private transient SingleSelectionModel selectionModel;
    private boolean paintBorder=true;
    private Insets margin=null;

    public JMenuBar(){
        super();
        setFocusTraversalKeysEnabled(false);
        setSelectionModel(new DefaultSingleSelectionModel());
        updateUI();
    }

    public void updateUI(){
        setUI((MenuBarUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected void paintBorder(Graphics g){
        if(isBorderPainted()){
            super.paintBorder(g);
        }
    }

    public boolean isBorderPainted(){
        return paintBorder;
    }

    public void setBorderPainted(boolean b){
        boolean oldValue=paintBorder;
        paintBorder=b;
        firePropertyChange("borderPainted",oldValue,paintBorder);
        if(b!=oldValue){
            revalidate();
            repaint();
        }
    }

    protected boolean processKeyBinding(KeyStroke ks,KeyEvent e,
                                        int condition,boolean pressed){
        // See if we have a local binding.
        boolean retValue=super.processKeyBinding(ks,e,condition,pressed);
        if(!retValue){
            MenuElement[] subElements=getSubElements();
            for(MenuElement subElement : subElements){
                if(processBindingForKeyStrokeRecursive(
                        subElement,ks,e,condition,pressed)){
                    return true;
                }
            }
        }
        return retValue;
    }

    public void addNotify(){
        super.addNotify();
        KeyboardManager.getCurrentManager().registerMenuBar(this);
    }

    public void removeNotify(){
        super.removeNotify();
        KeyboardManager.getCurrentManager().unregisterMenuBar(this);
    }

    protected String paramString(){
        String paintBorderString=(paintBorder?
                "true":"false");
        String marginString=(margin!=null?
                margin.toString():"");
        return super.paramString()+
                ",margin="+marginString+
                ",paintBorder="+paintBorderString;
    }

    static boolean processBindingForKeyStrokeRecursive(MenuElement elem,
                                                       KeyStroke ks,KeyEvent e,int condition,boolean pressed){
        if(elem==null){
            return false;
        }
        Component c=elem.getComponent();
        if(!(c.isVisible()||(c instanceof JPopupMenu))||!c.isEnabled()){
            return false;
        }
        if(c!=null&&c instanceof JComponent&&
                ((JComponent)c).processKeyBinding(ks,e,condition,pressed)){
            return true;
        }
        MenuElement[] subElements=elem.getSubElements();
        for(MenuElement subElement : subElements){
            if(processBindingForKeyStrokeRecursive(subElement,ks,e,condition,pressed)){
                return true;
                // We don't, pass along to children JMenu's
            }
        }
        return false;
    }

    public MenuBarUI getUI(){
        return (MenuBarUI)ui;
    }

    public void setUI(MenuBarUI ui){
        super.setUI(ui);
    }

    public JMenu add(JMenu c){
        super.add(c);
        return c;
    }

    public JMenu getMenu(int index){
        Component c=getComponentAtIndex(index);
        if(c instanceof JMenu)
            return (JMenu)c;
        return null;
    }

    @Deprecated
    public Component getComponentAtIndex(int i){
        if(i<0||i>=getComponentCount()){
            return null;
        }
        return getComponent(i);
    }

    public int getMenuCount(){
        return getComponentCount();
    }

    @Transient
    public JMenu getHelpMenu(){
        throw new Error("getHelpMenu() not yet implemented.");
    }

    public void setHelpMenu(JMenu menu){
        throw new Error("setHelpMenu() not yet implemented.");
    }

    public boolean isSelected(){
        return selectionModel.isSelected();
    }

    public void setSelected(Component sel){
        SingleSelectionModel model=getSelectionModel();
        int index=getComponentIndex(sel);
        model.setSelectedIndex(index);
    }

    public SingleSelectionModel getSelectionModel(){
        return selectionModel;
    }

    public void setSelectionModel(SingleSelectionModel model){
        SingleSelectionModel oldValue=selectionModel;
        this.selectionModel=model;
        firePropertyChange("selectionModel",oldValue,selectionModel);
    }

    public int getComponentIndex(Component c){
        int ncomponents=this.getComponentCount();
        Component[] component=this.getComponents();
        for(int i=0;i<ncomponents;i++){
            Component comp=component[i];
            if(comp==c)
                return i;
        }
        return -1;
    }

    public Insets getMargin(){
        if(margin==null){
            return new Insets(0,0,0,0);
        }else{
            return margin;
        }
    }

    public void setMargin(Insets m){
        Insets old=margin;
        this.margin=m;
        firePropertyChange("margin",old,m);
        if(old==null||!old.equals(m)){
            revalidate();
            repaint();
        }
    }

    public void processMouseEvent(MouseEvent event,MenuElement path[],MenuSelectionManager manager){
    }
/////////////////
// Accessibility support
////////////////

    public void processKeyEvent(KeyEvent e,MenuElement path[],MenuSelectionManager manager){
    }

    public void menuSelectionChanged(boolean isIncluded){
    }

    public MenuElement[] getSubElements(){
        MenuElement result[];
        Vector<MenuElement> tmp=new Vector<MenuElement>();
        int c=getComponentCount();
        int i;
        Component m;
        for(i=0;i<c;i++){
            m=getComponent(i);
            if(m instanceof MenuElement)
                tmp.addElement((MenuElement)m);
        }
        result=new MenuElement[tmp.size()];
        for(i=0,c=tmp.size();i<c;i++)
            result[i]=tmp.elementAt(i);
        return result;
    }

    public Component getComponent(){
        return this;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJMenuBar();
        }
        return accessibleContext;
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
        Object[] kvData=new Object[4];
        int n=0;
        if(selectionModel instanceof Serializable){
            kvData[n++]="selectionModel";
            kvData[n++]=selectionModel;
        }
        s.writeObject(kvData);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        Object[] kvData=(Object[])(s.readObject());
        for(int i=0;i<kvData.length;i+=2){
            if(kvData[i]==null){
                break;
            }else if(kvData[i].equals("selectionModel")){
                selectionModel=(SingleSelectionModel)kvData[i+1];
            }
        }
    }

    @SuppressWarnings("serial")
    protected class AccessibleJMenuBar extends AccessibleJComponent
            implements AccessibleSelection{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.MENU_BAR;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            return states;
        }

        public AccessibleSelection getAccessibleSelection(){
            return this;
        }

        public int getAccessibleSelectionCount(){
            if(isSelected()){
                return 1;
            }else{
                return 0;
            }
        }

        public Accessible getAccessibleSelection(int i){
            if(isSelected()){
                if(i!=0){   // single selection model for JMenuBar
                    return null;
                }
                int j=getSelectionModel().getSelectedIndex();
                if(getComponentAtIndex(j) instanceof Accessible){
                    return (Accessible)getComponentAtIndex(j);
                }
            }
            return null;
        }

        public boolean isAccessibleChildSelected(int i){
            return (i==getSelectionModel().getSelectedIndex());
        }

        public void addAccessibleSelection(int i){
            // first close up any open menu
            int j=getSelectionModel().getSelectedIndex();
            if(i==j){
                return;
            }
            if(j>=0&&j<getMenuCount()){
                JMenu menu=getMenu(j);
                if(menu!=null){
                    MenuSelectionManager.defaultManager().setSelectedPath(null);
//                  menu.setPopupMenuVisible(false);
                }
            }
            // now popup the new menu
            getSelectionModel().setSelectedIndex(i);
            JMenu menu=getMenu(i);
            if(menu!=null){
                MenuElement me[]=new MenuElement[3];
                me[0]=JMenuBar.this;
                me[1]=menu;
                me[2]=menu.getPopupMenu();
                MenuSelectionManager.defaultManager().setSelectedPath(me);
//              menu.setPopupMenuVisible(true);
            }
        }

        public void removeAccessibleSelection(int i){
            if(i>=0&&i<getMenuCount()){
                JMenu menu=getMenu(i);
                if(menu!=null){
                    MenuSelectionManager.defaultManager().setSelectedPath(null);
//                  menu.setPopupMenuVisible(false);
                }
                getSelectionModel().setSelectedIndex(-1);
            }
        }

        public void clearAccessibleSelection(){
            int i=getSelectionModel().getSelectedIndex();
            if(i>=0&&i<getMenuCount()){
                JMenu menu=getMenu(i);
                if(menu!=null){
                    MenuSelectionManager.defaultManager().setSelectedPath(null);
//                  menu.setPopupMenuVisible(false);
                }
            }
            getSelectionModel().setSelectedIndex(-1);
        }

        public void selectAllAccessibleSelection(){
        }
    } // internal class AccessibleJMenuBar
}
