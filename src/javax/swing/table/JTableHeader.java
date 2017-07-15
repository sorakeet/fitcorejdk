/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.accessibility.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.TableHeaderUI;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Locale;

public class JTableHeader extends JComponent implements TableColumnModelListener, Accessible{
    private static final String uiClassID="TableHeaderUI";
    //
// Instance Variables
//
    protected JTable table;
    protected TableColumnModel columnModel;
    protected boolean reorderingAllowed;
    protected boolean resizingAllowed;
    protected boolean updateTableInRealTime;
    transient protected TableColumn resizingColumn;
    transient protected TableColumn draggedColumn;
    transient protected int draggedDistance;
    private TableCellRenderer defaultRenderer;
//
// Constructors
//

    public JTableHeader(){
        this(null);
    }

    public JTableHeader(TableColumnModel cm){
        super();
        //setFocusable(false); // for strict win/mac compatibility mode,
        // this method should be invoked
        if(cm==null)
            cm=createDefaultColumnModel();
        setColumnModel(cm);
        // Initialize local ivars
        initializeLocalVars();
        // Get UI going
        updateUI();
    }
//
// Local behavior attributes
//

    public void updateUI(){
        setUI((TableHeaderUI)UIManager.getUI(this));
        TableCellRenderer renderer=getDefaultRenderer();
        if(renderer instanceof Component){
            SwingUtilities.updateComponentTreeUI((Component)renderer);
        }
    }

    @Transient
    public TableCellRenderer getDefaultRenderer(){
        return defaultRenderer;
    }

    public void setDefaultRenderer(TableCellRenderer defaultRenderer){
        this.defaultRenderer=defaultRenderer;
    }

    public String getUIClassID(){
        return uiClassID;
    }

    public String getToolTipText(MouseEvent event){
        String tip=null;
        Point p=event.getPoint();
        int column;
        // Locate the renderer under the event location
        if((column=columnAtPoint(p))!=-1){
            TableColumn aColumn=columnModel.getColumn(column);
            TableCellRenderer renderer=aColumn.getHeaderRenderer();
            if(renderer==null){
                renderer=defaultRenderer;
            }
            Component component=renderer.getTableCellRendererComponent(
                    getTable(),aColumn.getHeaderValue(),false,false,
                    -1,column);
            // Now have to see if the component is a JComponent before
            // getting the tip
            if(component instanceof JComponent){
                // Convert the event to the renderer's coordinate system
                MouseEvent newEvent;
                Rectangle cellRect=getHeaderRect(column);
                p.translate(-cellRect.x,-cellRect.y);
                newEvent=new MouseEvent(component,event.getID(),
                        event.getWhen(),event.getModifiers(),
                        p.x,p.y,event.getXOnScreen(),event.getYOnScreen(),
                        event.getClickCount(),
                        event.isPopupTrigger(),MouseEvent.NOBUTTON);
                tip=((JComponent)component).getToolTipText(newEvent);
            }
        }
        // No tip from the renderer get our own tip
        if(tip==null)
            tip=getToolTipText();
        return tip;
    }

    public JTable getTable(){
        return table;
    }

    public void setTable(JTable table){
        JTable old=this.table;
        this.table=table;
        firePropertyChange("table",old,table);
    }

    public int columnAtPoint(Point point){
        int x=point.x;
        if(!getComponentOrientation().isLeftToRight()){
            x=getWidthInRightToLeft()-x-1;
        }
        return getColumnModel().getColumnIndexAtX(x);
    }

    public TableColumnModel getColumnModel(){
        return columnModel;
    }

    public void setColumnModel(TableColumnModel columnModel){
        if(columnModel==null){
            throw new IllegalArgumentException("Cannot set a null ColumnModel");
        }
        TableColumnModel old=this.columnModel;
        if(columnModel!=old){
            if(old!=null){
                old.removeColumnModelListener(this);
            }
            this.columnModel=columnModel;
            columnModel.addColumnModelListener(this);
            firePropertyChange("columnModel",old,columnModel);
            resizeAndRepaint();
        }
    }

    public void resizeAndRepaint(){
        revalidate();
        repaint();
    }

    private int getWidthInRightToLeft(){
        if((table!=null)&&
                (table.getAutoResizeMode()!=JTable.AUTO_RESIZE_OFF)){
            return table.getWidth();
        }
        return super.getWidth();
    }

    public Rectangle getHeaderRect(int column){
        Rectangle r=new Rectangle();
        TableColumnModel cm=getColumnModel();
        r.height=getHeight();
        if(column<0){
            // x = width = 0;
            if(!getComponentOrientation().isLeftToRight()){
                r.x=getWidthInRightToLeft();
            }
        }else if(column>=cm.getColumnCount()){
            if(getComponentOrientation().isLeftToRight()){
                r.x=getWidth();
            }
        }else{
            for(int i=0;i<column;i++){
                r.x+=cm.getColumn(i).getWidth();
            }
            if(!getComponentOrientation().isLeftToRight()){
                r.x=getWidthInRightToLeft()-r.x-cm.getColumn(column).getWidth();
            }
            r.width=cm.getColumn(column).getWidth();
        }
        return r;
    }

    protected String paramString(){
        String reorderingAllowedString=(reorderingAllowed?
                "true":"false");
        String resizingAllowedString=(resizingAllowed?
                "true":"false");
        String updateTableInRealTimeString=(updateTableInRealTime?
                "true":"false");
        return super.paramString()+
                ",draggedDistance="+draggedDistance+
                ",reorderingAllowed="+reorderingAllowedString+
                ",resizingAllowed="+resizingAllowedString+
                ",updateTableInRealTime="+updateTableInRealTimeString;
    }

    protected TableColumnModel createDefaultColumnModel(){
        return new DefaultTableColumnModel();
    }

    protected void initializeLocalVars(){
        setOpaque(true);
        table=null;
        reorderingAllowed=true;
        resizingAllowed=true;
        draggedColumn=null;
        draggedDistance=0;
        resizingColumn=null;
        updateTableInRealTime=true;
        // I'm registered to do tool tips so we can draw tips for the
        // renderers
        ToolTipManager toolTipManager=ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        setDefaultRenderer(createDefaultRenderer());
    }
//
// Managing TableHeaderUI
//

    protected TableCellRenderer createDefaultRenderer(){
        return new DefaultTableCellHeaderRenderer();
    }

    public boolean getReorderingAllowed(){
        return reorderingAllowed;
    }

    public void setReorderingAllowed(boolean reorderingAllowed){
        boolean old=this.reorderingAllowed;
        this.reorderingAllowed=reorderingAllowed;
        firePropertyChange("reorderingAllowed",old,reorderingAllowed);
    }

    public boolean getResizingAllowed(){
        return resizingAllowed;
    }
//
// Managing models
//

    public void setResizingAllowed(boolean resizingAllowed){
        boolean old=this.resizingAllowed;
        this.resizingAllowed=resizingAllowed;
        firePropertyChange("resizingAllowed",old,resizingAllowed);
    }

    public TableColumn getDraggedColumn(){
        return draggedColumn;
    }
//
// Implementing TableColumnModelListener interface
//

    public void setDraggedColumn(TableColumn aColumn){
        draggedColumn=aColumn;
    }

    public int getDraggedDistance(){
        return draggedDistance;
    }

    public void setDraggedDistance(int distance){
        draggedDistance=distance;
    }

    public TableColumn getResizingColumn(){
        return resizingColumn;
    }

    public void setResizingColumn(TableColumn aColumn){
        resizingColumn=aColumn;
    }
//
//  Package Methods
//

    public boolean getUpdateTableInRealTime(){
        return updateTableInRealTime;
    }

    public void setUpdateTableInRealTime(boolean flag){
        updateTableInRealTime=flag;
    }

    public TableHeaderUI getUI(){
        return (TableHeaderUI)ui;
    }

    public void setUI(TableHeaderUI ui){
        if(this.ui!=ui){
            super.setUI(ui);
            repaint();
        }
    }

    public void columnAdded(TableColumnModelEvent e){
        resizeAndRepaint();
    }

    public void columnRemoved(TableColumnModelEvent e){
        resizeAndRepaint();
    }

    public void columnMoved(TableColumnModelEvent e){
        repaint();
    }

    public void columnMarginChanged(ChangeEvent e){
        resizeAndRepaint();
    }

    // --Redrawing the header is slow in cell selection mode.
    // --Since header selection is ugly and it is always clear from the
    // --view which columns are selected, don't redraw the header.
    public void columnSelectionChanged(ListSelectionEvent e){
    } // repaint(); }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if((ui!=null)&&(getUIClassID().equals(uiClassID))){
            ui.installUI(this);
        }
    }
/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJTableHeader();
        }
        return accessibleContext;
    }

    //
    // *** should also implement AccessibleSelection?
    // *** and what's up with keyboard navigation/manipulation?
    //
    protected class AccessibleJTableHeader extends AccessibleJComponent{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.PANEL;
        }

        public int getAccessibleChildrenCount(){
            return JTableHeader.this.columnModel.getColumnCount();
        }

        public Accessible getAccessibleChild(int i){
            if(i<0||i>=getAccessibleChildrenCount()){
                return null;
            }else{
                TableColumn aColumn=JTableHeader.this.columnModel.getColumn(i);
                TableCellRenderer renderer=aColumn.getHeaderRenderer();
                if(renderer==null){
                    if(defaultRenderer!=null){
                        renderer=defaultRenderer;
                    }else{
                        return null;
                    }
                }
                Component component=renderer.getTableCellRendererComponent(
                        JTableHeader.this.getTable(),
                        aColumn.getHeaderValue(),false,false,
                        -1,i);
                return new AccessibleJTableHeaderEntry(i,JTableHeader.this,JTableHeader.this.table);
            }
        }

        public Accessible getAccessibleAt(Point p){
            int column;
            // Locate the renderer under the Point
            if((column=JTableHeader.this.columnAtPoint(p))!=-1){
                TableColumn aColumn=JTableHeader.this.columnModel.getColumn(column);
                TableCellRenderer renderer=aColumn.getHeaderRenderer();
                if(renderer==null){
                    if(defaultRenderer!=null){
                        renderer=defaultRenderer;
                    }else{
                        return null;
                    }
                }
                Component component=renderer.getTableCellRendererComponent(
                        JTableHeader.this.getTable(),
                        aColumn.getHeaderValue(),false,false,
                        -1,column);
                return new AccessibleJTableHeaderEntry(column,JTableHeader.this,JTableHeader.this.table);
            }else{
                return null;
            }
        }

        protected class AccessibleJTableHeaderEntry extends AccessibleContext
                implements Accessible, AccessibleComponent{
            private JTableHeader parent;
            private int column;
            private JTable table;

            public AccessibleJTableHeaderEntry(int c,JTableHeader p,JTable t){
                parent=p;
                column=c;
                table=t;
                this.setAccessibleParent(parent);
            }

            public AccessibleContext getAccessibleContext(){
                return this;
            }

            public String getAccessibleName(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    String name=ac.getAccessibleName();
                    if((name!=null)&&(name!="")){
                        // return the cell renderer's AccessibleName
                        return name;
                    }
                }
                if((accessibleName!=null)&&(accessibleName!="")){
                    return accessibleName;
                }else{
                    // fall back to the client property
                    String name=(String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
                    if(name!=null){
                        return name;
                    }else{
                        return table.getColumnName(column);
                    }
                }
            }

            private AccessibleContext getCurrentAccessibleContext(){
                TableColumnModel tcm=table.getColumnModel();
                if(tcm!=null){
                    // Fixes 4772355 - ArrayOutOfBoundsException in
                    // JTableHeader
                    if(column<0||column>=tcm.getColumnCount()){
                        return null;
                    }
                    TableColumn aColumn=tcm.getColumn(column);
                    TableCellRenderer renderer=aColumn.getHeaderRenderer();
                    if(renderer==null){
                        if(defaultRenderer!=null){
                            renderer=defaultRenderer;
                        }else{
                            return null;
                        }
                    }
                    Component c=renderer.getTableCellRendererComponent(
                            JTableHeader.this.getTable(),
                            aColumn.getHeaderValue(),false,false,
                            -1,column);
                    if(c instanceof Accessible){
                        return ((Accessible)c).getAccessibleContext();
                    }
                }
                return null;
            }
            // AccessibleContext methods

            public void setAccessibleName(String s){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    ac.setAccessibleName(s);
                }else{
                    super.setAccessibleName(s);
                }
            }

            //
            // *** should check toolTip text for desc. (needs MouseEvent)
            //
            public String getAccessibleDescription(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getAccessibleDescription();
                }else{
                    return super.getAccessibleDescription();
                }
            }

            public void setAccessibleDescription(String s){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    ac.setAccessibleDescription(s);
                }else{
                    super.setAccessibleDescription(s);
                }
            }

            public AccessibleRole getAccessibleRole(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getAccessibleRole();
                }else{
                    return AccessibleRole.COLUMN_HEADER;
                }
            }

            public AccessibleStateSet getAccessibleStateSet(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    AccessibleStateSet states=ac.getAccessibleStateSet();
                    if(isShowing()){
                        states.add(AccessibleState.SHOWING);
                    }
                    return states;
                }else{
                    return new AccessibleStateSet();  // must be non null?
                }
            }

            public int getAccessibleIndexInParent(){
                return column;
            }

            public int getAccessibleChildrenCount(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getAccessibleChildrenCount();
                }else{
                    return 0;
                }
            }

            public Accessible getAccessibleChild(int i){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    Accessible accessibleChild=ac.getAccessibleChild(i);
                    ac.setAccessibleParent(this);
                    return accessibleChild;
                }else{
                    return null;
                }
            }

            public Locale getLocale(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getLocale();
                }else{
                    return null;
                }
            }

            public void addPropertyChangeListener(PropertyChangeListener l){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    ac.addPropertyChangeListener(l);
                }else{
                    super.addPropertyChangeListener(l);
                }
            }

            public void removePropertyChangeListener(PropertyChangeListener l){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    ac.removePropertyChangeListener(l);
                }else{
                    super.removePropertyChangeListener(l);
                }
            }

            public AccessibleAction getAccessibleAction(){
                return getCurrentAccessibleContext().getAccessibleAction();
            }

            public AccessibleComponent getAccessibleComponent(){
                return this; // to override getBounds()
            }

            public AccessibleSelection getAccessibleSelection(){
                return getCurrentAccessibleContext().getAccessibleSelection();
            }

            public AccessibleText getAccessibleText(){
                return getCurrentAccessibleContext().getAccessibleText();
            }

            public AccessibleValue getAccessibleValue(){
                return getCurrentAccessibleContext().getAccessibleValue();
            }

            public Color getBackground(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).getBackground();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.getBackground();
                    }else{
                        return null;
                    }
                }
            }
            // AccessibleComponent methods

            public void setBackground(Color c){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setBackground(c);
                }else{
                    Component cp=getCurrentComponent();
                    if(cp!=null){
                        cp.setBackground(c);
                    }
                }
            }

            public Color getForeground(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).getForeground();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.getForeground();
                    }else{
                        return null;
                    }
                }
            }

            public void setForeground(Color c){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setForeground(c);
                }else{
                    Component cp=getCurrentComponent();
                    if(cp!=null){
                        cp.setForeground(c);
                    }
                }
            }

            public Cursor getCursor(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).getCursor();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.getCursor();
                    }else{
                        Accessible ap=getAccessibleParent();
                        if(ap instanceof AccessibleComponent){
                            return ((AccessibleComponent)ap).getCursor();
                        }else{
                            return null;
                        }
                    }
                }
            }

            public void setCursor(Cursor c){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setCursor(c);
                }else{
                    Component cp=getCurrentComponent();
                    if(cp!=null){
                        cp.setCursor(c);
                    }
                }
            }

            public Font getFont(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).getFont();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.getFont();
                    }else{
                        return null;
                    }
                }
            }

            public void setFont(Font f){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setFont(f);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        c.setFont(f);
                    }
                }
            }

            public FontMetrics getFontMetrics(Font f){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).getFontMetrics(f);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.getFontMetrics(f);
                    }else{
                        return null;
                    }
                }
            }

            public boolean isEnabled(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).isEnabled();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.isEnabled();
                    }else{
                        return false;
                    }
                }
            }

            public void setEnabled(boolean b){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setEnabled(b);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        c.setEnabled(b);
                    }
                }
            }

            public boolean isVisible(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).isVisible();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.isVisible();
                    }else{
                        return false;
                    }
                }
            }

            private Component getCurrentComponent(){
                TableColumnModel tcm=table.getColumnModel();
                if(tcm!=null){
                    // Fixes 4772355 - ArrayOutOfBoundsException in
                    // JTableHeader
                    if(column<0||column>=tcm.getColumnCount()){
                        return null;
                    }
                    TableColumn aColumn=tcm.getColumn(column);
                    TableCellRenderer renderer=aColumn.getHeaderRenderer();
                    if(renderer==null){
                        if(defaultRenderer!=null){
                            renderer=defaultRenderer;
                        }else{
                            return null;
                        }
                    }
                    return renderer.getTableCellRendererComponent(
                            JTableHeader.this.getTable(),
                            aColumn.getHeaderValue(),false,false,
                            -1,column);
                }else{
                    return null;
                }
            }

            public void setVisible(boolean b){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setVisible(b);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        c.setVisible(b);
                    }
                }
            }

            public boolean isShowing(){
                if(isVisible()&&JTableHeader.this.isShowing()){
                    return true;
                }else{
                    return false;
                }
            }

            public boolean contains(Point p){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    Rectangle r=((AccessibleComponent)ac).getBounds();
                    return r.contains(p);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        Rectangle r=c.getBounds();
                        return r.contains(p);
                    }else{
                        return getBounds().contains(p);
                    }
                }
            }

            public Point getLocationOnScreen(){
                if(parent!=null){
                    Point parentLocation=parent.getLocationOnScreen();
                    Point componentLocation=getLocation();
                    componentLocation.translate(parentLocation.x,parentLocation.y);
                    return componentLocation;
                }else{
                    return null;
                }
            }

            public Point getLocation(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    Rectangle r=((AccessibleComponent)ac).getBounds();
                    return r.getLocation();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        Rectangle r=c.getBounds();
                        return r.getLocation();
                    }else{
                        return getBounds().getLocation();
                    }
                }
            }

            public void setLocation(Point p){
//                if ((parent != null)  && (parent.contains(p))) {
//                    ensureIndexIsVisible(indexInParent);
//                }
            }

            public Rectangle getBounds(){
                Rectangle r=table.getCellRect(-1,column,false);
                r.y=0;
                return r;
//                AccessibleContext ac = getCurrentAccessibleContext();
//                if (ac instanceof AccessibleComponent) {
//                    return ((AccessibleComponent) ac).getBounds();
//                } else {
//                  Component c = getCurrentComponent();
//                  if (c != null) {
//                      return c.getBounds();
//                  } else {
//                      Rectangle r = table.getCellRect(-1, column, false);
//                      r.y = 0;
//                      return r;
//                  }
//              }
            }

            public void setBounds(Rectangle r){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setBounds(r);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        c.setBounds(r);
                    }
                }
            }

            public Dimension getSize(){
                return getBounds().getSize();
//                AccessibleContext ac = getCurrentAccessibleContext();
//                if (ac instanceof AccessibleComponent) {
//                    Rectangle r = ((AccessibleComponent) ac).getBounds();
//                    return r.getSize();
//                } else {
//                    Component c = getCurrentComponent();
//                    if (c != null) {
//                        Rectangle r = c.getBounds();
//                        return r.getSize();
//                    } else {
//                        return getBounds().getSize();
//                    }
//                }
            }

            public void setSize(Dimension d){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setSize(d);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        c.setSize(d);
                    }
                }
            }

            public Accessible getAccessibleAt(Point p){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).getAccessibleAt(p);
                }else{
                    return null;
                }
            }

            public boolean isFocusTraversable(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    return ((AccessibleComponent)ac).isFocusTraversable();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.isFocusTraversable();
                    }else{
                        return false;
                    }
                }
            }

            public void requestFocus(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).requestFocus();
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        c.requestFocus();
                    }
                }
            }

            public void addFocusListener(FocusListener l){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).addFocusListener(l);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        c.addFocusListener(l);
                    }
                }
            }

            public void removeFocusListener(FocusListener l){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).removeFocusListener(l);
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        c.removeFocusListener(l);
                    }
                }
            }
        } // inner class AccessibleJTableHeaderElement
    }  // inner class AccessibleJTableHeader
}  // End of Class JTableHeader
