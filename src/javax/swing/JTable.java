/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.reflect.misc.ReflectUtil;
import sun.swing.PrintingStatus;
import sun.swing.SwingUtilities2;
import sun.swing.SwingUtilities2.Section;

import javax.accessibility.*;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.print.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

import static sun.swing.SwingUtilities2.Section.*;

public class JTable extends JComponent implements TableModelListener, Scrollable,
        TableColumnModelListener, ListSelectionListener, CellEditorListener,
        Accessible, RowSorterListener{
    public static final int AUTO_RESIZE_OFF=0;
    public static final int AUTO_RESIZE_NEXT_COLUMN=1;
    public static final int AUTO_RESIZE_SUBSEQUENT_COLUMNS=2;
    public static final int AUTO_RESIZE_LAST_COLUMN=3;
    public static final int AUTO_RESIZE_ALL_COLUMNS=4;
//
// Static Constants
//
    private static final String uiClassID="TableUI";
//
// Instance Variables
//
    protected TableModel dataModel;
    protected TableColumnModel columnModel;
    protected ListSelectionModel selectionModel;
    protected JTableHeader tableHeader;
    protected int rowHeight;
    protected int rowMargin;
    protected Color gridColor;
    protected boolean showHorizontalLines;
    protected boolean showVerticalLines;
    protected int autoResizeMode;
    protected boolean autoCreateColumnsFromModel;
    protected Dimension preferredViewportSize;
    protected boolean rowSelectionAllowed;
    protected boolean cellSelectionEnabled;
    transient protected Component editorComp;
    transient protected TableCellEditor cellEditor;
    transient protected int editingColumn;
    transient protected int editingRow;
    transient protected Hashtable defaultRenderersByColumnClass;
    transient protected Hashtable defaultEditorsByColumnClass;
    protected Color selectionForeground;
    protected Color selectionBackground;
//
// Private state
//
    // WARNING: If you directly access this field you should also change the
    // SortManager.modelRowSizes field as well.
    private SizeSequence rowModel;
    private boolean dragEnabled;
    private boolean surrendersFocusOnKeystroke;
    private PropertyChangeListener editorRemover=null;
    private boolean columnSelectionAdjusting;
    private boolean rowSelectionAdjusting;
    private Throwable printError;
    private boolean isRowHeightSet;
    private boolean updateSelectionOnSort;
    private transient SortManager sortManager;
    private boolean ignoreSortChange;
    private boolean sorterChanged;
    private boolean autoCreateRowSorter;
    private boolean fillsViewportHeight;
    private DropMode dropMode=DropMode.USE_SELECTION;
    private transient DropLocation dropLocation;
    public JTable(){
        this(null,null,null);
    }

    public JTable(TableModel dm){
        this(dm,null,null);
    }
//
// Constructors
//

    public JTable(TableModel dm,TableColumnModel cm){
        this(dm,cm,null);
    }

    public JTable(TableModel dm,TableColumnModel cm,ListSelectionModel sm){
        super();
        setLayout(null);
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                JComponent.getManagingFocusForwardTraversalKeys());
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                JComponent.getManagingFocusBackwardTraversalKeys());
        if(cm==null){
            cm=createDefaultColumnModel();
            autoCreateColumnsFromModel=true;
        }
        setColumnModel(cm);
        if(sm==null){
            sm=createDefaultSelectionModel();
        }
        setSelectionModel(sm);
        // Set the model last, that way if the autoCreatColumnsFromModel has
        // been set above, we will automatically populate an empty columnModel
        // with suitable columns for the new model.
        if(dm==null){
            dm=createDefaultDataModel();
        }
        setModel(dm);
        initializeLocalVars();
        updateUI();
    }

    public JTable(int numRows,int numColumns){
        this(new DefaultTableModel(numRows,numColumns));
    }

    public JTable(Vector rowData,Vector columnNames){
        this(new DefaultTableModel(rowData,columnNames));
    }

    public JTable(final Object[][] rowData,final Object[] columnNames){
        this(new AbstractTableModel(){
            public String getColumnName(int column){
                return columnNames[column].toString();
            }

            public boolean isCellEditable(int row,int column){
                return true;
            }

            public void setValueAt(Object value,int row,int col){
                rowData[row][col]=value;
                fireTableCellUpdated(row,col);
            }

            public int getRowCount(){
                return rowData.length;
            }

            public int getColumnCount(){
                return columnNames.length;
            }

            public Object getValueAt(int row,int col){
                return rowData[row][col];
            }
        });
    }

    @Deprecated
    static public JScrollPane createScrollPaneForTable(JTable aTable){
        return new JScrollPane(aTable);
    }

    public void setRowHeight(int row,int rowHeight){
        if(rowHeight<=0){
            throw new IllegalArgumentException("New row height less than 1");
        }
        getRowModel().setSize(row,rowHeight);
        if(sortManager!=null){
            sortManager.setViewRowHeight(row,rowHeight);
        }
        resizeAndRepaint();
    }

    private SizeSequence getRowModel(){
        if(rowModel==null){
            rowModel=new SizeSequence(getRowCount(),getRowHeight());
        }
        return rowModel;
    }

    public int getRowHeight(){
        return rowHeight;
    }

    public void setRowHeight(int rowHeight){
        if(rowHeight<=0){
            throw new IllegalArgumentException("New row height less than 1");
        }
        int old=this.rowHeight;
        this.rowHeight=rowHeight;
        rowModel=null;
        if(sortManager!=null){
            sortManager.modelRowSizes=null;
        }
        isRowHeightSet=true;
        resizeAndRepaint();
        firePropertyChange("rowHeight",old,rowHeight);
    }

    protected void resizeAndRepaint(){
        revalidate();
        repaint();
    }

    public int getRowCount(){
        RowSorter sorter=getRowSorter();
        if(sorter!=null){
            return sorter.getViewRowCount();
        }
        return getModel().getRowCount();
    }

    public RowSorter<? extends TableModel> getRowSorter(){
        return (sortManager!=null)?sortManager.sorter:null;
    }
//
// Static Methods
//

    public void setRowSorter(RowSorter<? extends TableModel> sorter){
        RowSorter<? extends TableModel> oldRowSorter=null;
        if(sortManager!=null){
            oldRowSorter=sortManager.sorter;
            sortManager.dispose();
            sortManager=null;
        }
        rowModel=null;
        clearSelectionAndLeadAnchor();
        if(sorter!=null){
            sortManager=new SortManager(sorter);
        }
        resizeAndRepaint();
        firePropertyChange("rowSorter",oldRowSorter,sorter);
        firePropertyChange("sorter",oldRowSorter,sorter);
    }
//
// Table Attributes
//

    private void clearSelectionAndLeadAnchor(){
        selectionModel.setValueIsAdjusting(true);
        columnModel.getSelectionModel().setValueIsAdjusting(true);
        clearSelection();
        selectionModel.setAnchorSelectionIndex(-1);
        selectionModel.setLeadSelectionIndex(-1);
        columnModel.getSelectionModel().setAnchorSelectionIndex(-1);
        columnModel.getSelectionModel().setLeadSelectionIndex(-1);
        selectionModel.setValueIsAdjusting(false);
        columnModel.getSelectionModel().setValueIsAdjusting(false);
    }

    public void clearSelection(){
        selectionModel.clearSelection();
        columnModel.getSelectionModel().clearSelection();
    }

    public TableModel getModel(){
        return dataModel;
    }

    public void setModel(TableModel dataModel){
        if(dataModel==null){
            throw new IllegalArgumentException("Cannot set a null TableModel");
        }
        if(this.dataModel!=dataModel){
            TableModel old=this.dataModel;
            if(old!=null){
                old.removeTableModelListener(this);
            }
            this.dataModel=dataModel;
            dataModel.addTableModelListener(this);
            tableChanged(new TableModelEvent(dataModel,TableModelEvent.HEADER_ROW));
            firePropertyChange("model",old,dataModel);
            if(getAutoCreateRowSorter()){
                setRowSorter(new TableRowSorter<TableModel>(dataModel));
            }
        }
    }

    public Dimension getIntercellSpacing(){
        return new Dimension(getColumnModel().getColumnMargin(),rowMargin);
    }

    public void setIntercellSpacing(Dimension intercellSpacing){
        // Set the rowMargin here and columnMargin in the TableColumnModel
        setRowMargin(intercellSpacing.height);
        getColumnModel().setColumnMargin(intercellSpacing.width);
        resizeAndRepaint();
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
            // Set the column model of the header as well.
            if(tableHeader!=null){
                tableHeader.setColumnModel(columnModel);
            }
            firePropertyChange("columnModel",old,columnModel);
            resizeAndRepaint();
        }
    }

    public Color getGridColor(){
        return gridColor;
    }

    public void setGridColor(Color gridColor){
        if(gridColor==null){
            throw new IllegalArgumentException("New color is null");
        }
        Color old=this.gridColor;
        this.gridColor=gridColor;
        firePropertyChange("gridColor",old,gridColor);
        // Redraw
        repaint();
    }

    public void setShowGrid(boolean showGrid){
        setShowHorizontalLines(showGrid);
        setShowVerticalLines(showGrid);
        // Redraw
        repaint();
    }

    public boolean getShowHorizontalLines(){
        return showHorizontalLines;
    }

    public void setShowHorizontalLines(boolean showHorizontalLines){
        boolean old=this.showHorizontalLines;
        this.showHorizontalLines=showHorizontalLines;
        firePropertyChange("showHorizontalLines",old,showHorizontalLines);
        // Redraw
        repaint();
    }

    public boolean getShowVerticalLines(){
        return showVerticalLines;
    }

    public void setShowVerticalLines(boolean showVerticalLines){
        boolean old=this.showVerticalLines;
        this.showVerticalLines=showVerticalLines;
        firePropertyChange("showVerticalLines",old,showVerticalLines);
        // Redraw
        repaint();
    }

    public int getAutoResizeMode(){
        return autoResizeMode;
    }

    public void setAutoResizeMode(int mode){
        if((mode==AUTO_RESIZE_OFF)||
                (mode==AUTO_RESIZE_NEXT_COLUMN)||
                (mode==AUTO_RESIZE_SUBSEQUENT_COLUMNS)||
                (mode==AUTO_RESIZE_LAST_COLUMN)||
                (mode==AUTO_RESIZE_ALL_COLUMNS)){
            int old=autoResizeMode;
            autoResizeMode=mode;
            resizeAndRepaint();
            if(tableHeader!=null){
                tableHeader.resizeAndRepaint();
            }
            firePropertyChange("autoResizeMode",old,autoResizeMode);
        }
    }

    public boolean getAutoCreateColumnsFromModel(){
        return autoCreateColumnsFromModel;
    }

    public void setAutoCreateColumnsFromModel(boolean autoCreateColumnsFromModel){
        if(this.autoCreateColumnsFromModel!=autoCreateColumnsFromModel){
            boolean old=this.autoCreateColumnsFromModel;
            this.autoCreateColumnsFromModel=autoCreateColumnsFromModel;
            if(autoCreateColumnsFromModel){
                createDefaultColumnsFromModel();
            }
            firePropertyChange("autoCreateColumnsFromModel",old,autoCreateColumnsFromModel);
        }
    }

    public void createDefaultColumnsFromModel(){
        TableModel m=getModel();
        if(m!=null){
            // Remove any current columns
            TableColumnModel cm=getColumnModel();
            while(cm.getColumnCount()>0){
                cm.removeColumn(cm.getColumn(0));
            }
            // Create new columns from the data model info
            for(int i=0;i<m.getColumnCount();i++){
                TableColumn newColumn=new TableColumn(i);
                addColumn(newColumn);
            }
        }
    }

    public void addColumn(TableColumn aColumn){
        if(aColumn.getHeaderValue()==null){
            int modelColumn=aColumn.getModelIndex();
            String columnName=getModel().getColumnName(modelColumn);
            aColumn.setHeaderValue(columnName);
        }
        getColumnModel().addColumn(aColumn);
    }

    public void setDefaultRenderer(Class<?> columnClass,TableCellRenderer renderer){
        if(renderer!=null){
            defaultRenderersByColumnClass.put(columnClass,renderer);
        }else{
            defaultRenderersByColumnClass.remove(columnClass);
        }
    }

    public TableCellRenderer getDefaultRenderer(Class<?> columnClass){
        if(columnClass==null){
            return null;
        }else{
            Object renderer=defaultRenderersByColumnClass.get(columnClass);
            if(renderer!=null){
                return (TableCellRenderer)renderer;
            }else{
                Class c=columnClass.getSuperclass();
                if(c==null&&columnClass!=Object.class){
                    c=Object.class;
                }
                return getDefaultRenderer(c);
            }
        }
    }

    public void setDefaultEditor(Class<?> columnClass,TableCellEditor editor){
        if(editor!=null){
            defaultEditorsByColumnClass.put(columnClass,editor);
        }else{
            defaultEditorsByColumnClass.remove(columnClass);
        }
    }

    public TableCellEditor getDefaultEditor(Class<?> columnClass){
        if(columnClass==null){
            return null;
        }else{
            Object editor=defaultEditorsByColumnClass.get(columnClass);
            if(editor!=null){
                return (TableCellEditor)editor;
            }else{
                return getDefaultEditor(columnClass.getSuperclass());
            }
        }
    }

    public boolean getDragEnabled(){
        return dragEnabled;
    }

    public void setDragEnabled(boolean b){
        if(b&&GraphicsEnvironment.isHeadless()){
            throw new HeadlessException();
        }
        dragEnabled=b;
    }

    public final DropMode getDropMode(){
        return dropMode;
    }

    public final void setDropMode(DropMode dropMode){
        if(dropMode!=null){
            switch(dropMode){
                case USE_SELECTION:
                case ON:
                case INSERT:
                case INSERT_ROWS:
                case INSERT_COLS:
                case ON_OR_INSERT:
                case ON_OR_INSERT_ROWS:
                case ON_OR_INSERT_COLS:
                    this.dropMode=dropMode;
                    return;
            }
        }
        throw new IllegalArgumentException(dropMode+": Unsupported drop mode for table");
    }

    public final DropLocation getDropLocation(){
        return dropLocation;
    }

    public boolean getAutoCreateRowSorter(){
        return autoCreateRowSorter;
    }

    public void setAutoCreateRowSorter(boolean autoCreateRowSorter){
        boolean oldValue=this.autoCreateRowSorter;
        this.autoCreateRowSorter=autoCreateRowSorter;
        if(autoCreateRowSorter){
            setRowSorter(new TableRowSorter<TableModel>(getModel()));
        }
        firePropertyChange("autoCreateRowSorter",oldValue,
                autoCreateRowSorter);
    }

    public boolean getUpdateSelectionOnSort(){
        return updateSelectionOnSort;
    }

    public void setUpdateSelectionOnSort(boolean update){
        if(updateSelectionOnSort!=update){
            updateSelectionOnSort=update;
            firePropertyChange("updateSelectionOnSort",!update,update);
        }
    }

    //
// Selection methods
//
    public void setSelectionMode(int selectionMode){
        clearSelection();
        getSelectionModel().setSelectionMode(selectionMode);
        getColumnModel().getSelectionModel().setSelectionMode(selectionMode);
    }

    public ListSelectionModel getSelectionModel(){
        return selectionModel;
    }

    public void setSelectionModel(ListSelectionModel newModel){
        if(newModel==null){
            throw new IllegalArgumentException("Cannot set a null SelectionModel");
        }
        ListSelectionModel oldModel=selectionModel;
        if(newModel!=oldModel){
            if(oldModel!=null){
                oldModel.removeListSelectionListener(this);
            }
            selectionModel=newModel;
            newModel.addListSelectionListener(this);
            firePropertyChange("selectionModel",oldModel,newModel);
            repaint();
        }
    }

    public boolean getCellSelectionEnabled(){
        return getRowSelectionAllowed()&&getColumnSelectionAllowed();
    }

    public boolean getRowSelectionAllowed(){
        return rowSelectionAllowed;
    }

    public void setRowSelectionAllowed(boolean rowSelectionAllowed){
        boolean old=this.rowSelectionAllowed;
        this.rowSelectionAllowed=rowSelectionAllowed;
        if(old!=rowSelectionAllowed){
            repaint();
        }
        firePropertyChange("rowSelectionAllowed",old,rowSelectionAllowed);
    }

    public boolean getColumnSelectionAllowed(){
        return columnModel.getColumnSelectionAllowed();
    }

    public void setColumnSelectionAllowed(boolean columnSelectionAllowed){
        boolean old=columnModel.getColumnSelectionAllowed();
        columnModel.setColumnSelectionAllowed(columnSelectionAllowed);
        if(old!=columnSelectionAllowed){
            repaint();
        }
        firePropertyChange("columnSelectionAllowed",old,columnSelectionAllowed);
    }

    public void setCellSelectionEnabled(boolean cellSelectionEnabled){
        setRowSelectionAllowed(cellSelectionEnabled);
        setColumnSelectionAllowed(cellSelectionEnabled);
        boolean old=this.cellSelectionEnabled;
        this.cellSelectionEnabled=cellSelectionEnabled;
        firePropertyChange("cellSelectionEnabled",old,cellSelectionEnabled);
    }

    public void selectAll(){
        // If I'm currently editing, then I should stop editing
        if(isEditing()){
            removeEditor();
        }
        if(getRowCount()>0&&getColumnCount()>0){
            int oldLead;
            int oldAnchor;
            ListSelectionModel selModel;
            selModel=selectionModel;
            selModel.setValueIsAdjusting(true);
            oldLead=getAdjustedIndex(selModel.getLeadSelectionIndex(),true);
            oldAnchor=getAdjustedIndex(selModel.getAnchorSelectionIndex(),true);
            setRowSelectionInterval(0,getRowCount()-1);
            // this is done to restore the anchor and lead
            SwingUtilities2.setLeadAnchorWithoutSelection(selModel,oldLead,oldAnchor);
            selModel.setValueIsAdjusting(false);
            selModel=columnModel.getSelectionModel();
            selModel.setValueIsAdjusting(true);
            oldLead=getAdjustedIndex(selModel.getLeadSelectionIndex(),false);
            oldAnchor=getAdjustedIndex(selModel.getAnchorSelectionIndex(),false);
            setColumnSelectionInterval(0,getColumnCount()-1);
            // this is done to restore the anchor and lead
            SwingUtilities2.setLeadAnchorWithoutSelection(selModel,oldLead,oldAnchor);
            selModel.setValueIsAdjusting(false);
        }
    }

    private int getAdjustedIndex(int index,boolean row){
        int compare=row?getRowCount():getColumnCount();
        return index<compare?index:-1;
    }

    public void setRowSelectionInterval(int index0,int index1){
        selectionModel.setSelectionInterval(boundRow(index0),boundRow(index1));
    }

    public void setColumnSelectionInterval(int index0,int index1){
        columnModel.getSelectionModel().setSelectionInterval(boundColumn(index0),boundColumn(index1));
    }

    public void addRowSelectionInterval(int index0,int index1){
        selectionModel.addSelectionInterval(boundRow(index0),boundRow(index1));
    }

    public void addColumnSelectionInterval(int index0,int index1){
        columnModel.getSelectionModel().addSelectionInterval(boundColumn(index0),boundColumn(index1));
    }

    public void removeRowSelectionInterval(int index0,int index1){
        selectionModel.removeSelectionInterval(boundRow(index0),boundRow(index1));
    }

    private int boundRow(int row) throws IllegalArgumentException{
        if(row<0||row>=getRowCount()){
            throw new IllegalArgumentException("Row index out of range");
        }
        return row;
    }

    public void removeColumnSelectionInterval(int index0,int index1){
        columnModel.getSelectionModel().removeSelectionInterval(boundColumn(index0),boundColumn(index1));
    }

    private int boundColumn(int col){
        if(col<0||col>=getColumnCount()){
            throw new IllegalArgumentException("Column index out of range");
        }
        return col;
    }

    public int getColumnCount(){
        return getColumnModel().getColumnCount();
    }

    public int getSelectedColumn(){
        return columnModel.getSelectionModel().getMinSelectionIndex();
    }

    public int[] getSelectedColumns(){
        return columnModel.getSelectedColumns();
    }

    public int getSelectedRowCount(){
        int iMin=selectionModel.getMinSelectionIndex();
        int iMax=selectionModel.getMaxSelectionIndex();
        int count=0;
        for(int i=iMin;i<=iMax;i++){
            if(selectionModel.isSelectedIndex(i)){
                count++;
            }
        }
        return count;
    }

    public int getSelectedColumnCount(){
        return columnModel.getSelectedColumnCount();
    }

    public boolean isRowSelected(int row){
        return selectionModel.isSelectedIndex(row);
    }

    public boolean isColumnSelected(int column){
        return columnModel.getSelectionModel().isSelectedIndex(column);
    }

    public boolean isCellSelected(int row,int column){
        if(!getRowSelectionAllowed()&&!getColumnSelectionAllowed()){
            return false;
        }
        return (!getRowSelectionAllowed()||isRowSelected(row))&&
                (!getColumnSelectionAllowed()||isColumnSelected(column));
    }

    private void changeSelectionModel(ListSelectionModel sm,int index,
                                      boolean toggle,boolean extend,boolean selected,
                                      int anchor,boolean anchorSelected){
        if(extend){
            if(toggle){
                if(anchorSelected){
                    sm.addSelectionInterval(anchor,index);
                }else{
                    sm.removeSelectionInterval(anchor,index);
                    // this is a Windows-only behavior that we want for file lists
                    if(Boolean.TRUE==getClientProperty("Table.isFileList")){
                        sm.addSelectionInterval(index,index);
                        sm.setAnchorSelectionIndex(anchor);
                    }
                }
            }else{
                sm.setSelectionInterval(anchor,index);
            }
        }else{
            if(toggle){
                if(selected){
                    sm.removeSelectionInterval(index,index);
                }else{
                    sm.addSelectionInterval(index,index);
                }
            }else{
                sm.setSelectionInterval(index,index);
            }
        }
    }

    public void changeSelection(int rowIndex,int columnIndex,boolean toggle,boolean extend){
        ListSelectionModel rsm=getSelectionModel();
        ListSelectionModel csm=getColumnModel().getSelectionModel();
        int anchorRow=getAdjustedIndex(rsm.getAnchorSelectionIndex(),true);
        int anchorCol=getAdjustedIndex(csm.getAnchorSelectionIndex(),false);
        boolean anchorSelected=true;
        if(anchorRow==-1){
            if(getRowCount()>0){
                anchorRow=0;
            }
            anchorSelected=false;
        }
        if(anchorCol==-1){
            if(getColumnCount()>0){
                anchorCol=0;
            }
            anchorSelected=false;
        }
        // Check the selection here rather than in each selection model.
        // This is significant in cell selection mode if we are supposed
        // to be toggling the selection. In this case it is better to
        // ensure that the cell's selection state will indeed be changed.
        // If this were done in the code for the selection model it
        // might leave a cell in selection state if the row was
        // selected but the column was not - as it would toggle them both.
        boolean selected=isCellSelected(rowIndex,columnIndex);
        anchorSelected=anchorSelected&&isCellSelected(anchorRow,anchorCol);
        changeSelectionModel(csm,columnIndex,toggle,extend,selected,
                anchorCol,anchorSelected);
        changeSelectionModel(rsm,rowIndex,toggle,extend,selected,
                anchorRow,anchorSelected);
        // Scroll after changing the selection as blit scrolling is immediate,
        // so that if we cause the repaint after the scroll we end up painting
        // everything!
        if(getAutoscrolls()){
            Rectangle cellRect=getCellRect(rowIndex,columnIndex,false);
            if(cellRect!=null){
                scrollRectToVisible(cellRect);
            }
        }
    }

    public Color getSelectionForeground(){
        return selectionForeground;
    }

    public void setSelectionForeground(Color selectionForeground){
        Color old=this.selectionForeground;
        this.selectionForeground=selectionForeground;
        firePropertyChange("selectionForeground",old,selectionForeground);
        repaint();
    }

    public Color getSelectionBackground(){
        return selectionBackground;
    }

    public void setSelectionBackground(Color selectionBackground){
        Color old=this.selectionBackground;
        this.selectionBackground=selectionBackground;
        firePropertyChange("selectionBackground",old,selectionBackground);
        repaint();
    }

    public TableColumn getColumn(Object identifier){
        TableColumnModel cm=getColumnModel();
        int columnIndex=cm.getColumnIndex(identifier);
        return cm.getColumn(columnIndex);
    }

    public int convertColumnIndexToView(int modelColumnIndex){
        return SwingUtilities2.convertColumnIndexToView(
                getColumnModel(),modelColumnIndex);
    }

    public int convertRowIndexToView(int modelRowIndex){
        RowSorter sorter=getRowSorter();
        if(sorter!=null){
            return sorter.convertRowIndexToView(modelRowIndex);
        }
        return modelRowIndex;
    }

    public String getColumnName(int column){
        return getModel().getColumnName(convertColumnIndexToModel(column));
    }

    public int convertColumnIndexToModel(int viewColumnIndex){
        return SwingUtilities2.convertColumnIndexToModel(
                getColumnModel(),viewColumnIndex);
    }

    public Class<?> getColumnClass(int column){
        return getModel().getColumnClass(convertColumnIndexToModel(column));
    }

    public Object getValueAt(int row,int column){
        return getModel().getValueAt(convertRowIndexToModel(row),
                convertColumnIndexToModel(column));
    }

    public boolean isCellEditable(int row,int column){
        return getModel().isCellEditable(convertRowIndexToModel(row),
                convertColumnIndexToModel(column));
    }
//
// Informally implement the TableModel interface.
//

    public void removeColumn(TableColumn aColumn){
        getColumnModel().removeColumn(aColumn);
    }

    public void moveColumn(int column,int targetColumn){
        getColumnModel().moveColumn(column,targetColumn);
    }

    public int columnAtPoint(Point point){
        int x=point.x;
        if(!getComponentOrientation().isLeftToRight()){
            x=getWidth()-x-1;
        }
        return getColumnModel().getColumnIndexAtX(x);
    }

    public int rowAtPoint(Point point){
        int y=point.y;
        int result=(rowModel==null)?y/getRowHeight():rowModel.getIndex(y);
        if(result<0){
            return -1;
        }else if(result>=getRowCount()){
            return -1;
        }else{
            return result;
        }
    }

    private int viewIndexForColumn(TableColumn aColumn){
        TableColumnModel cm=getColumnModel();
        for(int column=0;column<cm.getColumnCount();column++){
            if(cm.getColumn(column)==aColumn){
                return column;
            }
        }
        return -1;
    }

    public void doLayout(){
        TableColumn resizingColumn=getResizingColumn();
        if(resizingColumn==null){
            setWidthsFromPreferredWidths(false);
        }else{
            // JTable behaves like a layout manger - but one in which the
            // user can come along and dictate how big one of the children
            // (columns) is supposed to be.
            // A column has been resized and JTable may need to distribute
            // any overall delta to other columns, according to the resize mode.
            int columnIndex=viewIndexForColumn(resizingColumn);
            int delta=getWidth()-getColumnModel().getTotalColumnWidth();
            accommodateDelta(columnIndex,delta);
            delta=getWidth()-getColumnModel().getTotalColumnWidth();
            // If the delta cannot be completely accomodated, then the
            // resizing column will have to take any remainder. This means
            // that the column is not being allowed to take the requested
            // width. This happens under many circumstances: For example,
            // AUTO_RESIZE_NEXT_COLUMN specifies that any delta be distributed
            // to the column after the resizing column. If one were to attempt
            // to resize the last column of the table, there would be no
            // columns after it, and hence nowhere to distribute the delta.
            // It would then be given entirely back to the resizing column,
            // preventing it from changing size.
            if(delta!=0){
                resizingColumn.setWidth(resizingColumn.getWidth()+delta);
            }
            // At this point the JTable has to work out what preferred sizes
            // would have resulted in the layout the user has chosen.
            // Thereafter, during window resizing etc. it has to work off
            // the preferred sizes as usual - the idea being that, whatever
            // the user does, everything stays in synch and things don't jump
            // around.
            setWidthsFromPreferredWidths(true);
        }
        super.doLayout();
    }

    @Deprecated
    public void sizeColumnsToFit(boolean lastColumnOnly){
        int oldAutoResizeMode=autoResizeMode;
        setAutoResizeMode(lastColumnOnly?AUTO_RESIZE_LAST_COLUMN
                :AUTO_RESIZE_ALL_COLUMNS);
        sizeColumnsToFit(-1);
        setAutoResizeMode(oldAutoResizeMode);
    }

    public void sizeColumnsToFit(int resizingColumn){
        if(resizingColumn==-1){
            setWidthsFromPreferredWidths(false);
        }else{
            if(autoResizeMode==AUTO_RESIZE_OFF){
                TableColumn aColumn=getColumnModel().getColumn(resizingColumn);
                aColumn.setPreferredWidth(aColumn.getWidth());
            }else{
                int delta=getWidth()-getColumnModel().getTotalColumnWidth();
                accommodateDelta(resizingColumn,delta);
                setWidthsFromPreferredWidths(true);
            }
        }
    }

    private void setWidthsFromPreferredWidths(final boolean inverse){
        int totalWidth=getWidth();
        int totalPreferred=getPreferredSize().width;
        int target=!inverse?totalWidth:totalPreferred;
        final TableColumnModel cm=columnModel;
        Resizable3 r=new Resizable3(){
            public int getElementCount(){
                return cm.getColumnCount();
            }

            public int getMidPointAt(int i){
                if(!inverse){
                    return cm.getColumn(i).getPreferredWidth();
                }else{
                    return cm.getColumn(i).getWidth();
                }
            }            public int getLowerBoundAt(int i){
                return cm.getColumn(i).getMinWidth();
            }

            public int getUpperBoundAt(int i){
                return cm.getColumn(i).getMaxWidth();
            }



            public void setSizeAt(int s,int i){
                if(!inverse){
                    cm.getColumn(i).setWidth(s);
                }else{
                    cm.getColumn(i).setPreferredWidth(s);
                }
            }
        };
        adjustSizes(target,r,inverse);
    }

    // Distribute delta over columns, as indicated by the autoresize mode.
    private void accommodateDelta(int resizingColumnIndex,int delta){
        int columnCount=getColumnCount();
        int from=resizingColumnIndex;
        int to;
        // Use the mode to determine how to absorb the changes.
        switch(autoResizeMode){
            case AUTO_RESIZE_NEXT_COLUMN:
                from=from+1;
                to=Math.min(from+1,columnCount);
                break;
            case AUTO_RESIZE_SUBSEQUENT_COLUMNS:
                from=from+1;
                to=columnCount;
                break;
            case AUTO_RESIZE_LAST_COLUMN:
                from=columnCount-1;
                to=from+1;
                break;
            case AUTO_RESIZE_ALL_COLUMNS:
                from=0;
                to=columnCount;
                break;
            default:
                return;
        }
        final int start=from;
        final int end=to;
        final TableColumnModel cm=columnModel;
        Resizable3 r=new Resizable3(){
            public int getElementCount(){
                return end-start;
            }

            public int getLowerBoundAt(int i){
                return cm.getColumn(i+start).getMinWidth();
            }

            public int getUpperBoundAt(int i){
                return cm.getColumn(i+start).getMaxWidth();
            }

            public int getMidPointAt(int i){
                return cm.getColumn(i+start).getWidth();
            }

            public void setSizeAt(int s,int i){
                cm.getColumn(i+start).setWidth(s);
            }
        };
        int totalWidth=0;
        for(int i=from;i<to;i++){
            TableColumn aColumn=columnModel.getColumn(i);
            int input=aColumn.getWidth();
            totalWidth=totalWidth+input;
        }
        adjustSizes(totalWidth+delta,r,false);
    }

    private void adjustSizes(long target,final Resizable3 r,boolean inverse){
        int N=r.getElementCount();
        long totalPreferred=0;
        for(int i=0;i<N;i++){
            totalPreferred+=r.getMidPointAt(i);
        }
        Resizable2 s;
        if((target<totalPreferred)==!inverse){
            s=new Resizable2(){
                public int getElementCount(){
                    return r.getElementCount();
                }

                public int getLowerBoundAt(int i){
                    return r.getLowerBoundAt(i);
                }

                public int getUpperBoundAt(int i){
                    return r.getMidPointAt(i);
                }

                public void setSizeAt(int newSize,int i){
                    r.setSizeAt(newSize,i);
                }
            };
        }else{
            s=new Resizable2(){
                public int getElementCount(){
                    return r.getElementCount();
                }

                public int getLowerBoundAt(int i){
                    return r.getMidPointAt(i);
                }

                public int getUpperBoundAt(int i){
                    return r.getUpperBoundAt(i);
                }

                public void setSizeAt(int newSize,int i){
                    r.setSizeAt(newSize,i);
                }
            };
        }
        adjustSizes(target,s,!inverse);
    }
//
// Adding and removing columns in the view
//

    private void adjustSizes(long target,Resizable2 r,boolean limitToRange){
        long totalLowerBound=0;
        long totalUpperBound=0;
        for(int i=0;i<r.getElementCount();i++){
            totalLowerBound+=r.getLowerBoundAt(i);
            totalUpperBound+=r.getUpperBoundAt(i);
        }
        if(limitToRange){
            target=Math.min(Math.max(totalLowerBound,target),totalUpperBound);
        }
        for(int i=0;i<r.getElementCount();i++){
            int lowerBound=r.getLowerBoundAt(i);
            int upperBound=r.getUpperBoundAt(i);
            // Check for zero. This happens when the distribution of the delta
            // finishes early due to a series of "fixed" entries at the end.
            // In this case, lowerBound == upperBound, for all subsequent terms.
            int newSize;
            if(totalLowerBound==totalUpperBound){
                newSize=lowerBound;
            }else{
                double f=(double)(target-totalLowerBound)/(totalUpperBound-totalLowerBound);
                newSize=(int)Math.round(lowerBound+f*(upperBound-lowerBound));
                // We'd need to round manually in an all integer version.
                // size[i] = (int)(((totalUpperBound - target) * lowerBound +
                //     (target - totalLowerBound) * upperBound)/(totalUpperBound-totalLowerBound));
            }
            r.setSizeAt(newSize,i);
            target-=newSize;
            totalLowerBound-=lowerBound;
            totalUpperBound-=upperBound;
        }
    }

    public boolean getSurrendersFocusOnKeystroke(){
        return surrendersFocusOnKeystroke;
    }

    public void setSurrendersFocusOnKeystroke(boolean surrendersFocusOnKeystroke){
        this.surrendersFocusOnKeystroke=surrendersFocusOnKeystroke;
    }
//
// Cover methods for various models and helper methods
//

    public boolean editCellAt(int row,int column){
        return editCellAt(row,column,null);
    }

    public boolean editCellAt(int row,int column,EventObject e){
        if(cellEditor!=null&&!cellEditor.stopCellEditing()){
            return false;
        }
        if(row<0||row>=getRowCount()||
                column<0||column>=getColumnCount()){
            return false;
        }
        if(!isCellEditable(row,column))
            return false;
        if(editorRemover==null){
            KeyboardFocusManager fm=
                    KeyboardFocusManager.getCurrentKeyboardFocusManager();
            editorRemover=new CellEditorRemover(fm);
            fm.addPropertyChangeListener("permanentFocusOwner",editorRemover);
        }
        TableCellEditor editor=getCellEditor(row,column);
        if(editor!=null&&editor.isCellEditable(e)){
            editorComp=prepareEditor(editor,row,column);
            if(editorComp==null){
                removeEditor();
                return false;
            }
            editorComp.setBounds(getCellRect(row,column,false));
            add(editorComp);
            editorComp.validate();
            editorComp.repaint();
            setCellEditor(editor);
            setEditingRow(row);
            setEditingColumn(column);
            editor.addCellEditorListener(this);
            return true;
        }
        return false;
    }

    public Component getEditorComponent(){
        return editorComp;
    }

    public int getEditingColumn(){
        return editingColumn;
    }

    public void setEditingColumn(int aColumn){
        editingColumn=aColumn;
    }

    public int getEditingRow(){
        return editingRow;
    }    private TableColumn getResizingColumn(){
        return (tableHeader==null)?null
                :tableHeader.getResizingColumn();
    }

    public void setEditingRow(int aRow){
        editingRow=aRow;
    }

    public TableUI getUI(){
        return (TableUI)ui;
    }

    public void setUI(TableUI ui){
        if(this.ui!=ui){
            super.setUI(ui);
            repaint();
        }
    }

    public void updateUI(){
        // Update the UIs of the cell renderers, cell editors and header renderers.
        TableColumnModel cm=getColumnModel();
        for(int column=0;column<cm.getColumnCount();column++){
            TableColumn aColumn=cm.getColumn(column);
            SwingUtilities.updateRendererOrEditorUI(aColumn.getCellRenderer());
            SwingUtilities.updateRendererOrEditorUI(aColumn.getCellEditor());
            SwingUtilities.updateRendererOrEditorUI(aColumn.getHeaderRenderer());
        }
        // Update the UIs of all the default renderers.
        Enumeration defaultRenderers=defaultRenderersByColumnClass.elements();
        while(defaultRenderers.hasMoreElements()){
            SwingUtilities.updateRendererOrEditorUI(defaultRenderers.nextElement());
        }
        // Update the UIs of all the default editors.
        Enumeration defaultEditors=defaultEditorsByColumnClass.elements();
        while(defaultEditors.hasMoreElements()){
            SwingUtilities.updateRendererOrEditorUI(defaultEditors.nextElement());
        }
        // Update the UI of the table header
        if(tableHeader!=null&&tableHeader.getParent()==null){
            tableHeader.updateUI();
        }
        // Update UI applied to parent ScrollPane
        configureEnclosingScrollPaneUI();
        setUI((TableUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected boolean processKeyBinding(KeyStroke ks,KeyEvent e,
                                        int condition,boolean pressed){
        boolean retValue=super.processKeyBinding(ks,e,condition,pressed);
        // Start editing when a key is typed. UI classes can disable this behavior
        // by setting the client property JTable.autoStartsEdit to Boolean.FALSE.
        if(!retValue&&condition==WHEN_ANCESTOR_OF_FOCUSED_COMPONENT&&
                isFocusOwner()&&
                !Boolean.FALSE.equals(getClientProperty("JTable.autoStartsEdit"))){
            // We do not have a binding for the event.
            Component editorComponent=getEditorComponent();
            if(editorComponent==null){
                // Only attempt to install the editor on a KEY_PRESSED,
                if(e==null||e.getID()!=KeyEvent.KEY_PRESSED){
                    return false;
                }
                // Don't start when just a modifier is pressed
                int code=e.getKeyCode();
                if(code==KeyEvent.VK_SHIFT||code==KeyEvent.VK_CONTROL||
                        code==KeyEvent.VK_ALT){
                    return false;
                }
                // Try to install the editor
                int leadRow=getSelectionModel().getLeadSelectionIndex();
                int leadColumn=getColumnModel().getSelectionModel().
                        getLeadSelectionIndex();
                if(leadRow!=-1&&leadColumn!=-1&&!isEditing()){
                    if(!editCellAt(leadRow,leadColumn,e)){
                        return false;
                    }
                }
                editorComponent=getEditorComponent();
                if(editorComponent==null){
                    return false;
                }
            }
            // If the editorComponent is a JComponent, pass the event to it.
            if(editorComponent instanceof JComponent){
                retValue=((JComponent)editorComponent).processKeyBinding
                        (ks,e,WHEN_FOCUSED,pressed);
                // If we have started an editor as a result of the user
                // pressing a key and the surrendersFocusOnKeystroke property
                // is true, give the focus to the new editor.
                if(getSurrendersFocusOnKeystroke()){
                    editorComponent.requestFocus();
                }
            }
        }
        return retValue;
    }

    public String getToolTipText(MouseEvent event){
        String tip=null;
        Point p=event.getPoint();
        // Locate the renderer under the event location
        int hitColumnIndex=columnAtPoint(p);
        int hitRowIndex=rowAtPoint(p);
        if((hitColumnIndex!=-1)&&(hitRowIndex!=-1)){
            TableCellRenderer renderer=getCellRenderer(hitRowIndex,hitColumnIndex);
            Component component=prepareRenderer(renderer,hitRowIndex,hitColumnIndex);
            // Now have to see if the component is a JComponent before
            // getting the tip
            if(component instanceof JComponent){
                // Convert the event to the renderer's coordinate system
                Rectangle cellRect=getCellRect(hitRowIndex,hitColumnIndex,false);
                p.translate(-cellRect.x,-cellRect.y);
                MouseEvent newEvent=new MouseEvent(component,event.getID(),
                        event.getWhen(),event.getModifiers(),
                        p.x,p.y,
                        event.getXOnScreen(),
                        event.getYOnScreen(),
                        event.getClickCount(),
                        event.isPopupTrigger(),
                        MouseEvent.NOBUTTON);
                tip=((JComponent)component).getToolTipText(newEvent);
            }
        }
        // No tip from the renderer get our own tip
        if(tip==null)
            tip=getToolTipText();
        return tip;
    }

    DropLocation dropLocationForPoint(Point p){
        DropLocation location=null;
        int row=rowAtPoint(p);
        int col=columnAtPoint(p);
        boolean outside=Boolean.TRUE==getClientProperty("Table.isFileList")
                &&SwingUtilities2.pointOutsidePrefSize(this,row,col,p);
        Rectangle rect=getCellRect(row,col,true);
        Section xSection, ySection;
        boolean between=false;
        boolean ltr=getComponentOrientation().isLeftToRight();
        switch(dropMode){
            case USE_SELECTION:
            case ON:
                if(row==-1||col==-1||outside){
                    location=new DropLocation(p,-1,-1,false,false);
                }else{
                    location=new DropLocation(p,row,col,false,false);
                }
                break;
            case INSERT:
                if(row==-1&&col==-1){
                    location=new DropLocation(p,0,0,true,true);
                    break;
                }
                xSection=SwingUtilities2.liesInHorizontal(rect,p,ltr,true);
                if(row==-1){
                    if(xSection==LEADING){
                        location=new DropLocation(p,getRowCount(),col,true,true);
                    }else if(xSection==TRAILING){
                        location=new DropLocation(p,getRowCount(),col+1,true,true);
                    }else{
                        location=new DropLocation(p,getRowCount(),col,true,false);
                    }
                }else if(xSection==LEADING||xSection==TRAILING){
                    ySection=SwingUtilities2.liesInVertical(rect,p,true);
                    if(ySection==LEADING){
                        between=true;
                    }else if(ySection==TRAILING){
                        row++;
                        between=true;
                    }
                    location=new DropLocation(p,row,
                            xSection==TRAILING?col+1:col,
                            between,true);
                }else{
                    if(SwingUtilities2.liesInVertical(rect,p,false)==TRAILING){
                        row++;
                    }
                    location=new DropLocation(p,row,col,true,false);
                }
                break;
            case INSERT_ROWS:
                if(row==-1&&col==-1){
                    location=new DropLocation(p,-1,-1,false,false);
                    break;
                }
                if(row==-1){
                    location=new DropLocation(p,getRowCount(),col,true,false);
                    break;
                }
                if(SwingUtilities2.liesInVertical(rect,p,false)==TRAILING){
                    row++;
                }
                location=new DropLocation(p,row,col,true,false);
                break;
            case ON_OR_INSERT_ROWS:
                if(row==-1&&col==-1){
                    location=new DropLocation(p,-1,-1,false,false);
                    break;
                }
                if(row==-1){
                    location=new DropLocation(p,getRowCount(),col,true,false);
                    break;
                }
                ySection=SwingUtilities2.liesInVertical(rect,p,true);
                if(ySection==LEADING){
                    between=true;
                }else if(ySection==TRAILING){
                    row++;
                    between=true;
                }
                location=new DropLocation(p,row,col,between,false);
                break;
            case INSERT_COLS:
                if(row==-1){
                    location=new DropLocation(p,-1,-1,false,false);
                    break;
                }
                if(col==-1){
                    location=new DropLocation(p,getColumnCount(),col,false,true);
                    break;
                }
                if(SwingUtilities2.liesInHorizontal(rect,p,ltr,false)==TRAILING){
                    col++;
                }
                location=new DropLocation(p,row,col,false,true);
                break;
            case ON_OR_INSERT_COLS:
                if(row==-1){
                    location=new DropLocation(p,-1,-1,false,false);
                    break;
                }
                if(col==-1){
                    location=new DropLocation(p,row,getColumnCount(),false,true);
                    break;
                }
                xSection=SwingUtilities2.liesInHorizontal(rect,p,ltr,true);
                if(xSection==LEADING){
                    between=true;
                }else if(xSection==TRAILING){
                    col++;
                    between=true;
                }
                location=new DropLocation(p,row,col,false,between);
                break;
            case ON_OR_INSERT:
                if(row==-1&&col==-1){
                    location=new DropLocation(p,0,0,true,true);
                    break;
                }
                xSection=SwingUtilities2.liesInHorizontal(rect,p,ltr,true);
                if(row==-1){
                    if(xSection==LEADING){
                        location=new DropLocation(p,getRowCount(),col,true,true);
                    }else if(xSection==TRAILING){
                        location=new DropLocation(p,getRowCount(),col+1,true,true);
                    }else{
                        location=new DropLocation(p,getRowCount(),col,true,false);
                    }
                    break;
                }
                ySection=SwingUtilities2.liesInVertical(rect,p,true);
                if(ySection==LEADING){
                    between=true;
                }else if(ySection==TRAILING){
                    row++;
                    between=true;
                }
                location=new DropLocation(p,row,
                        xSection==TRAILING?col+1:col,
                        between,
                        xSection!=MIDDLE);
                break;
            default:
                assert false:"Unexpected drop mode";
        }
        return location;
    }

    Object setDropLocation(TransferHandler.DropLocation location,
                           Object state,
                           boolean forDrop){
        Object retVal=null;
        DropLocation tableLocation=(DropLocation)location;
        if(dropMode==DropMode.USE_SELECTION){
            if(tableLocation==null){
                if(!forDrop&&state!=null){
                    clearSelection();
                    int[] rows=((int[][])state)[0];
                    int[] cols=((int[][])state)[1];
                    int[] anchleads=((int[][])state)[2];
                    for(int row : rows){
                        addRowSelectionInterval(row,row);
                    }
                    for(int col : cols){
                        addColumnSelectionInterval(col,col);
                    }
                    SwingUtilities2.setLeadAnchorWithoutSelection(
                            getSelectionModel(),anchleads[1],anchleads[0]);
                    SwingUtilities2.setLeadAnchorWithoutSelection(
                            getColumnModel().getSelectionModel(),
                            anchleads[3],anchleads[2]);
                }
            }else{
                if(dropLocation==null){
                    retVal=new int[][]{
                            getSelectedRows(),
                            getSelectedColumns(),
                            {getAdjustedIndex(getSelectionModel()
                                    .getAnchorSelectionIndex(),true),
                                    getAdjustedIndex(getSelectionModel()
                                            .getLeadSelectionIndex(),true),
                                    getAdjustedIndex(getColumnModel().getSelectionModel()
                                            .getAnchorSelectionIndex(),false),
                                    getAdjustedIndex(getColumnModel().getSelectionModel()
                                            .getLeadSelectionIndex(),false)}};
                }else{
                    retVal=state;
                }
                if(tableLocation.getRow()==-1){
                    clearSelectionAndLeadAnchor();
                }else{
                    setRowSelectionInterval(tableLocation.getRow(),
                            tableLocation.getRow());
                    setColumnSelectionInterval(tableLocation.getColumn(),
                            tableLocation.getColumn());
                }
            }
        }
        DropLocation old=dropLocation;
        dropLocation=tableLocation;
        firePropertyChange("dropLocation",old,dropLocation);
        return retVal;
    }
//
// Editing Support
//

    void setUIProperty(String propertyName,Object value){
        if(propertyName=="rowHeight"){
            if(!isRowHeightSet){
                setRowHeight(((Number)value).intValue());
                isRowHeightSet=false;
            }
            return;
        }
        super.setUIProperty(propertyName,value);
    }

    public void addNotify(){
        super.addNotify();
        configureEnclosingScrollPane();
    }

    protected void configureEnclosingScrollPane(){
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            JViewport port=(JViewport)parent;
            Container gp=port.getParent();
            if(gp instanceof JScrollPane){
                JScrollPane scrollPane=(JScrollPane)gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport=scrollPane.getViewport();
                if(viewport==null||
                        SwingUtilities.getUnwrappedView(viewport)!=this){
                    return;
                }
                scrollPane.setColumnHeaderView(getTableHeader());
                // configure the scrollpane for any LAF dependent settings
                configureEnclosingScrollPaneUI();
            }
        }
    }

    private void configureEnclosingScrollPaneUI(){
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            JViewport port=(JViewport)parent;
            Container gp=port.getParent();
            if(gp instanceof JScrollPane){
                JScrollPane scrollPane=(JScrollPane)gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport=scrollPane.getViewport();
                if(viewport==null||
                        SwingUtilities.getUnwrappedView(viewport)!=this){
                    return;
                }
                //  scrollPane.getViewport().setBackingStoreEnabled(true);
                Border border=scrollPane.getBorder();
                if(border==null||border instanceof UIResource){
                    Border scrollPaneBorder=
                            UIManager.getBorder("Table.scrollPaneBorder");
                    if(scrollPaneBorder!=null){
                        scrollPane.setBorder(scrollPaneBorder);
                    }
                }
                // add JScrollBar corner component if available from LAF and not already set by the user
                Component corner=
                        scrollPane.getCorner(JScrollPane.UPPER_TRAILING_CORNER);
                if(corner==null||corner instanceof UIResource){
                    corner=null;
                    try{
                        corner=(Component)UIManager.get(
                                "Table.scrollPaneCornerComponent");
                    }catch(Exception e){
                        // just ignore and don't set corner
                    }
                    scrollPane.setCorner(JScrollPane.UPPER_TRAILING_CORNER,
                            corner);
                }
            }
        }
    }

    public JTableHeader getTableHeader(){
        return tableHeader;
    }

    public void setTableHeader(JTableHeader tableHeader){
        if(this.tableHeader!=tableHeader){
            JTableHeader old=this.tableHeader;
            // Release the old header
            if(old!=null){
                old.setTable(null);
            }
            this.tableHeader=tableHeader;
            if(tableHeader!=null){
                tableHeader.setTable(this);
            }
            firePropertyChange("tableHeader",old,tableHeader);
        }
    }

    public void removeNotify(){
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                removePropertyChangeListener("permanentFocusOwner",editorRemover);
        editorRemover=null;
        unconfigureEnclosingScrollPane();
        super.removeNotify();
    }

    protected void unconfigureEnclosingScrollPane(){
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            JViewport port=(JViewport)parent;
            Container gp=port.getParent();
            if(gp instanceof JScrollPane){
                JScrollPane scrollPane=(JScrollPane)gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport=scrollPane.getViewport();
                if(viewport==null||
                        SwingUtilities.getUnwrappedView(viewport)!=this){
                    return;
                }
                scrollPane.setColumnHeaderView(null);
                // remove ScrollPane corner if one was added by the LAF
                Component corner=
                        scrollPane.getCorner(JScrollPane.UPPER_TRAILING_CORNER);
                if(corner instanceof UIResource){
                    scrollPane.setCorner(JScrollPane.UPPER_TRAILING_CORNER,
                            null);
                }
            }
        }
    }
//
// Managing TableUI
//

    void compWriteObjectNotify(){
        super.compWriteObjectNotify();
        // If ToolTipText != null, then the tooltip has already been
        // unregistered by JComponent.compWriteObjectNotify()
        if(getToolTipText()==null){
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }
    }

    protected String paramString(){
        String gridColorString=(gridColor!=null?
                gridColor.toString():"");
        String showHorizontalLinesString=(showHorizontalLines?
                "true":"false");
        String showVerticalLinesString=(showVerticalLines?
                "true":"false");
        String autoResizeModeString;
        if(autoResizeMode==AUTO_RESIZE_OFF){
            autoResizeModeString="AUTO_RESIZE_OFF";
        }else if(autoResizeMode==AUTO_RESIZE_NEXT_COLUMN){
            autoResizeModeString="AUTO_RESIZE_NEXT_COLUMN";
        }else if(autoResizeMode==AUTO_RESIZE_SUBSEQUENT_COLUMNS){
            autoResizeModeString="AUTO_RESIZE_SUBSEQUENT_COLUMNS";
        }else if(autoResizeMode==AUTO_RESIZE_LAST_COLUMN){
            autoResizeModeString="AUTO_RESIZE_LAST_COLUMN";
        }else if(autoResizeMode==AUTO_RESIZE_ALL_COLUMNS){
            autoResizeModeString="AUTO_RESIZE_ALL_COLUMNS";
        }else autoResizeModeString="";
        String autoCreateColumnsFromModelString=(autoCreateColumnsFromModel?
                "true":"false");
        String preferredViewportSizeString=(preferredViewportSize!=null?
                preferredViewportSize.toString()
                :"");
        String rowSelectionAllowedString=(rowSelectionAllowed?
                "true":"false");
        String cellSelectionEnabledString=(cellSelectionEnabled?
                "true":"false");
        String selectionForegroundString=(selectionForeground!=null?
                selectionForeground.toString():
                "");
        String selectionBackgroundString=(selectionBackground!=null?
                selectionBackground.toString():
                "");
        return super.paramString()+
                ",autoCreateColumnsFromModel="+autoCreateColumnsFromModelString+
                ",autoResizeMode="+autoResizeModeString+
                ",cellSelectionEnabled="+cellSelectionEnabledString+
                ",editingColumn="+editingColumn+
                ",editingRow="+editingRow+
                ",gridColor="+gridColorString+
                ",preferredViewportSize="+preferredViewportSizeString+
                ",rowHeight="+rowHeight+
                ",rowMargin="+rowMargin+
                ",rowSelectionAllowed="+rowSelectionAllowedString+
                ",selectionBackground="+selectionBackgroundString+
                ",selectionForeground="+selectionForegroundString+
                ",showHorizontalLines="+showHorizontalLinesString+
                ",showVerticalLines="+showVerticalLinesString;
    }

    public void sorterChanged(RowSorterEvent e){
        if(e.getType()==RowSorterEvent.Type.SORT_ORDER_CHANGED){
            JTableHeader header=getTableHeader();
            if(header!=null){
                header.repaint();
            }
        }else if(e.getType()==RowSorterEvent.Type.SORTED){
            sorterChanged=true;
            if(!ignoreSortChange){
                sortedTableChanged(e,null);
            }
        }
    }

    private void sortedTableChanged(RowSorterEvent sortedEvent,
                                    TableModelEvent e){
        int editingModelIndex=-1;
        ModelChange change=(e!=null)?new ModelChange(e):null;
        if((change==null||!change.allRowsChanged)&&
                this.editingRow!=-1){
            editingModelIndex=convertRowIndexToModel(sortedEvent,
                    this.editingRow);
        }
        sortManager.prepareForChange(sortedEvent,change);
        if(e!=null){
            if(change.type==TableModelEvent.UPDATE){
                repaintSortedRows(change);
            }
            notifySorter(change);
            if(change.type!=TableModelEvent.UPDATE){
                // If the Sorter is unsorted we will not have received
                // notification, force treating insert/delete as a change.
                sorterChanged=true;
            }
        }else{
            sorterChanged=true;
        }
        sortManager.processChange(sortedEvent,change,sorterChanged);
        if(sorterChanged){
            // Update the editing row
            if(this.editingRow!=-1){
                int newIndex=(editingModelIndex==-1)?-1:
                        convertRowIndexToView(editingModelIndex,change);
                restoreSortingEditingRow(newIndex);
            }
            // And handle the appropriate repainting.
            if(e==null||change.type!=TableModelEvent.UPDATE){
                resizeAndRepaint();
            }
        }
        // Check if lead/anchor need to be reset.
        if(change!=null&&change.allRowsChanged){
            clearSelectionAndLeadAnchor();
            resizeAndRepaint();
        }
    }
//
// Managing models
//

    private void repaintSortedRows(ModelChange change){
        if(change.startModelIndex>change.endModelIndex||
                change.startModelIndex+10<change.endModelIndex){
            // Too much has changed, punt
            repaint();
            return;
        }
        int eventColumn=change.event.getColumn();
        int columnViewIndex=eventColumn;
        if(columnViewIndex==TableModelEvent.ALL_COLUMNS){
            columnViewIndex=0;
        }else{
            columnViewIndex=convertColumnIndexToView(columnViewIndex);
            if(columnViewIndex==-1){
                return;
            }
        }
        int modelIndex=change.startModelIndex;
        while(modelIndex<=change.endModelIndex){
            int viewIndex=convertRowIndexToView(modelIndex++);
            if(viewIndex!=-1){
                Rectangle dirty=getCellRect(viewIndex,columnViewIndex,
                        false);
                int x=dirty.x;
                int w=dirty.width;
                if(eventColumn==TableModelEvent.ALL_COLUMNS){
                    x=0;
                    w=getWidth();
                }
                repaint(x,dirty.y,w,dirty.height);
            }
        }
    }

    private void restoreSortingSelection(int[] selection,int lead,
                                         ModelChange change){
        // Convert the selection from model to view
        for(int i=selection.length-1;i>=0;i--){
            selection[i]=convertRowIndexToView(selection[i],change);
        }
        lead=convertRowIndexToView(lead,change);
        // Check for the common case of no change in selection for 1 row
        if(selection.length==0||
                (selection.length==1&&selection[0]==getSelectedRow())){
            return;
        }
        // And apply the new selection
        selectionModel.setValueIsAdjusting(true);
        selectionModel.clearSelection();
        for(int i=selection.length-1;i>=0;i--){
            if(selection[i]!=-1){
                selectionModel.addSelectionInterval(selection[i],
                        selection[i]);
            }
        }
        SwingUtilities2.setLeadAnchorWithoutSelection(
                selectionModel,lead,lead);
        selectionModel.setValueIsAdjusting(false);
    }

    public int getSelectedRow(){
        return selectionModel.getMinSelectionIndex();
    }

    private int convertRowIndexToView(int modelIndex,ModelChange change){
        if(modelIndex<0){
            return -1;
        }
        if(change!=null&&modelIndex>=change.startModelIndex){
            if(change.type==TableModelEvent.INSERT){
                if(modelIndex+change.length>=change.modelRowCount){
                    return -1;
                }
                return sortManager.sorter.convertRowIndexToView(
                        modelIndex+change.length);
            }else if(change.type==TableModelEvent.DELETE){
                if(modelIndex<=change.endModelIndex){
                    // deleted
                    return -1;
                }else{
                    if(modelIndex-change.length>=change.modelRowCount){
                        return -1;
                    }
                    return sortManager.sorter.convertRowIndexToView(
                            modelIndex-change.length);
                }
            }
            // else, updated
        }
        if(modelIndex>=getModel().getRowCount()){
            return -1;
        }
        return sortManager.sorter.convertRowIndexToView(modelIndex);
    }

    private void restoreSortingEditingRow(int editingRow){
        if(editingRow==-1){
            // Editing row no longer being shown, cancel editing
            TableCellEditor editor=getCellEditor();
            if(editor!=null){
                // First try and cancel
                editor.cancelCellEditing();
                if(getCellEditor()!=null){
                    // CellEditor didn't cede control, forcefully
                    // remove it
                    removeEditor();
                }
            }
        }else{
            // Repositioning handled in BasicTableUI
            this.editingRow=editingRow;
            repaint();
        }
    }

    private void notifySorter(ModelChange change){
        try{
            ignoreSortChange=true;
            sorterChanged=false;
            switch(change.type){
                case TableModelEvent.UPDATE:
                    if(change.event.getLastRow()==Integer.MAX_VALUE){
                        sortManager.sorter.allRowsChanged();
                    }else if(change.event.getColumn()==
                            TableModelEvent.ALL_COLUMNS){
                        sortManager.sorter.rowsUpdated(change.startModelIndex,
                                change.endModelIndex);
                    }else{
                        sortManager.sorter.rowsUpdated(change.startModelIndex,
                                change.endModelIndex,
                                change.event.getColumn());
                    }
                    break;
                case TableModelEvent.INSERT:
                    sortManager.sorter.rowsInserted(change.startModelIndex,
                            change.endModelIndex);
                    break;
                case TableModelEvent.DELETE:
                    sortManager.sorter.rowsDeleted(change.startModelIndex,
                            change.endModelIndex);
                    break;
            }
        }finally{
            ignoreSortChange=false;
        }
    }
//
// RowSorterListener
//

    private int[] convertSelectionToModel(RowSorterEvent e){
        int[] selection=getSelectedRows();
        for(int i=selection.length-1;i>=0;i--){
            selection[i]=convertRowIndexToModel(e,selection[i]);
        }
        return selection;
    }

    public int[] getSelectedRows(){
        int iMin=selectionModel.getMinSelectionIndex();
        int iMax=selectionModel.getMaxSelectionIndex();
        if((iMin==-1)||(iMax==-1)){
            return new int[0];
        }
        int[] rvTmp=new int[1+(iMax-iMin)];
        int n=0;
        for(int i=iMin;i<=iMax;i++){
            if(selectionModel.isSelectedIndex(i)){
                rvTmp[n++]=i;
            }
        }
        int[] rv=new int[n];
        System.arraycopy(rvTmp,0,rv,0,n);
        return rv;
    }

    private int convertRowIndexToModel(RowSorterEvent e,int viewIndex){
        if(e!=null){
            if(e.getPreviousRowCount()==0){
                return viewIndex;
            }
            // range checking handled by RowSorterEvent
            return e.convertPreviousRowIndexToModel(viewIndex);
        }
        // Make sure the viewIndex is valid
        if(viewIndex<0||viewIndex>=getRowCount()){
            return -1;
        }
        return convertRowIndexToModel(viewIndex);
    }

    public int convertRowIndexToModel(int viewRowIndex){
        RowSorter sorter=getRowSorter();
        if(sorter!=null){
            return sorter.convertRowIndexToModel(viewRowIndex);
        }
        return viewRowIndex;
    }

    public void tableChanged(TableModelEvent e){
        if(e==null||e.getFirstRow()==TableModelEvent.HEADER_ROW){
            // The whole thing changed
            clearSelectionAndLeadAnchor();
            rowModel=null;
            if(sortManager!=null){
                try{
                    ignoreSortChange=true;
                    sortManager.sorter.modelStructureChanged();
                }finally{
                    ignoreSortChange=false;
                }
                sortManager.allChanged();
            }
            if(getAutoCreateColumnsFromModel()){
                // This will effect invalidation of the JTable and JTableHeader.
                createDefaultColumnsFromModel();
                return;
            }
            resizeAndRepaint();
            return;
        }
        if(sortManager!=null){
            sortedTableChanged(null,e);
            return;
        }
        // The totalRowHeight calculated below will be incorrect if
        // there are variable height rows. Repaint the visible region,
        // but don't return as a revalidate may be necessary as well.
        if(rowModel!=null){
            repaint();
        }
        if(e.getType()==TableModelEvent.INSERT){
            tableRowsInserted(e);
            return;
        }
        if(e.getType()==TableModelEvent.DELETE){
            tableRowsDeleted(e);
            return;
        }
        int modelColumn=e.getColumn();
        int start=e.getFirstRow();
        int end=e.getLastRow();
        Rectangle dirtyRegion;
        if(modelColumn==TableModelEvent.ALL_COLUMNS){
            // 1 or more rows changed
            dirtyRegion=new Rectangle(0,start*getRowHeight(),
                    getColumnModel().getTotalColumnWidth(),0);
        }else{
            // A cell or column of cells has changed.
            // Unlike the rest of the methods in the JTable, the TableModelEvent
            // uses the coordinate system of the model instead of the view.
            // This is the only place in the JTable where this "reverse mapping"
            // is used.
            int column=convertColumnIndexToView(modelColumn);
            dirtyRegion=getCellRect(start,column,false);
        }
        // Now adjust the height of the dirty region according to the value of "end".
        // Check for Integer.MAX_VALUE as this will cause an overflow.
        if(end!=Integer.MAX_VALUE){
            dirtyRegion.height=(end-start+1)*getRowHeight();
            repaint(dirtyRegion.x,dirtyRegion.y,dirtyRegion.width,dirtyRegion.height);
        }
        // In fact, if the end is Integer.MAX_VALUE we need to revalidate anyway
        // because the scrollbar may need repainting.
        else{
            clearSelectionAndLeadAnchor();
            resizeAndRepaint();
            rowModel=null;
        }
    }

    private void tableRowsInserted(TableModelEvent e){
        int start=e.getFirstRow();
        int end=e.getLastRow();
        if(start<0){
            start=0;
        }
        if(end<0){
            end=getRowCount()-1;
        }
        // Adjust the selection to account for the new rows.
        int length=end-start+1;
        selectionModel.insertIndexInterval(start,length,true);
        // If we have variable height rows, adjust the row model.
        if(rowModel!=null){
            rowModel.insertEntries(start,length,getRowHeight());
        }
        int rh=getRowHeight();
        Rectangle drawRect=new Rectangle(0,start*rh,
                getColumnModel().getTotalColumnWidth(),
                (getRowCount()-start)*rh);
        revalidate();
        // PENDING(milne) revalidate calls repaint() if parent is a ScrollPane
        // repaint still required in the unusual case where there is no ScrollPane
        repaint(drawRect);
    }

    private void tableRowsDeleted(TableModelEvent e){
        int start=e.getFirstRow();
        int end=e.getLastRow();
        if(start<0){
            start=0;
        }
        if(end<0){
            end=getRowCount()-1;
        }
        int deletedCount=end-start+1;
        int previousRowCount=getRowCount()+deletedCount;
        // Adjust the selection to account for the new rows
        selectionModel.removeIndexInterval(start,end);
        // If we have variable height rows, adjust the row model.
        if(rowModel!=null){
            rowModel.removeEntries(start,deletedCount);
        }
        int rh=getRowHeight();
        Rectangle drawRect=new Rectangle(0,start*rh,
                getColumnModel().getTotalColumnWidth(),
                (previousRowCount-start)*rh);
        revalidate();
        // PENDING(milne) revalidate calls repaint() if parent is a ScrollPane
        // repaint still required in the unusual case where there is no ScrollPane
        repaint(drawRect);
    }

    public void columnAdded(TableColumnModelEvent e){
        // If I'm currently editing, then I should stop editing
        if(isEditing()){
            removeEditor();
        }
        resizeAndRepaint();
    }

    public boolean isEditing(){
        return cellEditor!=null;
    }

    public void removeEditor(){
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                removePropertyChangeListener("permanentFocusOwner",editorRemover);
        editorRemover=null;
        TableCellEditor editor=getCellEditor();
        if(editor!=null){
            editor.removeCellEditorListener(this);
            if(editorComp!=null){
                Component focusOwner=
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                boolean isFocusOwnerInTheTable=focusOwner!=null?
                        SwingUtilities.isDescendingFrom(focusOwner,this):false;
                remove(editorComp);
                if(isFocusOwnerInTheTable){
                    requestFocusInWindow();
                }
            }
            Rectangle cellRect=getCellRect(editingRow,editingColumn,false);
            setCellEditor(null);
            setEditingColumn(-1);
            setEditingRow(-1);
            editorComp=null;
            repaint(cellRect);
        }
    }

    public Rectangle getCellRect(int row,int column,boolean includeSpacing){
        Rectangle r=new Rectangle();
        boolean valid=true;
        if(row<0){
            // y = height = 0;
            valid=false;
        }else if(row>=getRowCount()){
            r.y=getHeight();
            valid=false;
        }else{
            r.height=getRowHeight(row);
            r.y=(rowModel==null)?row*r.height:rowModel.getPosition(row);
        }
        if(column<0){
            if(!getComponentOrientation().isLeftToRight()){
                r.x=getWidth();
            }
            // otherwise, x = width = 0;
            valid=false;
        }else if(column>=getColumnCount()){
            if(getComponentOrientation().isLeftToRight()){
                r.x=getWidth();
            }
            // otherwise, x = width = 0;
            valid=false;
        }else{
            TableColumnModel cm=getColumnModel();
            if(getComponentOrientation().isLeftToRight()){
                for(int i=0;i<column;i++){
                    r.x+=cm.getColumn(i).getWidth();
                }
            }else{
                for(int i=cm.getColumnCount()-1;i>column;i--){
                    r.x+=cm.getColumn(i).getWidth();
                }
            }
            r.width=cm.getColumn(column).getWidth();
        }
        if(valid&&!includeSpacing){
            // Bound the margins by their associated dimensions to prevent
            // returning bounds with negative dimensions.
            int rm=Math.min(getRowMargin(),r.height);
            int cm=Math.min(getColumnModel().getColumnMargin(),r.width);
            // This is not the same as grow(), it rounds differently.
            r.setBounds(r.x+cm/2,r.y+rm/2,r.width-cm,r.height-rm);
        }
        return r;
    }
//
// Implementing TableModelListener interface
//

    public int getRowHeight(int row){
        return (rowModel==null)?getRowHeight():rowModel.getSize(row);
    }

    public int getRowMargin(){
        return rowMargin;
    }

    public void setRowMargin(int rowMargin){
        int old=this.rowMargin;
        this.rowMargin=rowMargin;
        resizeAndRepaint();
        firePropertyChange("rowMargin",old,rowMargin);
    }
//
// Implementing TableColumnModelListener interface
//

    public TableCellEditor getCellEditor(){
        return cellEditor;
    }

    public void setCellEditor(TableCellEditor anEditor){
        TableCellEditor oldEditor=cellEditor;
        cellEditor=anEditor;
        firePropertyChange("tableCellEditor",oldEditor,anEditor);
    }    public void columnRemoved(TableColumnModelEvent e){
        // If I'm currently editing, then I should stop editing
        if(isEditing()){
            removeEditor();
        }
        resizeAndRepaint();
    }

    public void valueChanged(ListSelectionEvent e){
        if(sortManager!=null){
            sortManager.viewSelectionChanged(e);
        }
        boolean isAdjusting=e.getValueIsAdjusting();
        if(rowSelectionAdjusting&&!isAdjusting){
            // The assumption is that when the model is no longer adjusting
            // we will have already gotten all the changes, and therefore
            // don't need to do an additional paint.
            rowSelectionAdjusting=false;
            return;
        }
        rowSelectionAdjusting=isAdjusting;
        // The getCellRect() calls will fail unless there is at least one column.
        if(getRowCount()<=0||getColumnCount()<=0){
            return;
        }
        int firstIndex=limit(e.getFirstIndex(),0,getRowCount()-1);
        int lastIndex=limit(e.getLastIndex(),0,getRowCount()-1);
        Rectangle firstRowRect=getCellRect(firstIndex,0,false);
        Rectangle lastRowRect=getCellRect(lastIndex,getColumnCount()-1,false);
        Rectangle dirtyRegion=firstRowRect.union(lastRowRect);
        repaint(dirtyRegion);
    }    public void columnMoved(TableColumnModelEvent e){
        if(isEditing()&&!getCellEditor().stopCellEditing()){
            getCellEditor().cancelCellEditing();
        }
        repaint();
    }

    private int limit(int i,int a,int b){
        return Math.min(b,Math.max(i,a));
    }    public void columnMarginChanged(ChangeEvent e){
        if(isEditing()&&!getCellEditor().stopCellEditing()){
            getCellEditor().cancelCellEditing();
        }
        TableColumn resizingColumn=getResizingColumn();
        // Need to do this here, before the parent's
        // layout manager calls getPreferredSize().
        if(resizingColumn!=null&&autoResizeMode==AUTO_RESIZE_OFF){
            resizingColumn.setPreferredWidth(resizingColumn.getWidth());
        }
        resizeAndRepaint();
    }

    public void editingStopped(ChangeEvent e){
        // Take in the new value
        TableCellEditor editor=getCellEditor();
        if(editor!=null){
            Object value=editor.getCellEditorValue();
            setValueAt(value,editingRow,editingColumn);
            removeEditor();
        }
    }

    public void setValueAt(Object aValue,int row,int column){
        getModel().setValueAt(aValue,convertRowIndexToModel(row),
                convertColumnIndexToModel(column));
    }    public void columnSelectionChanged(ListSelectionEvent e){
        boolean isAdjusting=e.getValueIsAdjusting();
        if(columnSelectionAdjusting&&!isAdjusting){
            // The assumption is that when the model is no longer adjusting
            // we will have already gotten all the changes, and therefore
            // don't need to do an additional paint.
            columnSelectionAdjusting=false;
            return;
        }
        columnSelectionAdjusting=isAdjusting;
        // The getCellRect() call will fail unless there is at least one row.
        if(getRowCount()<=0||getColumnCount()<=0){
            return;
        }
        int firstIndex=limit(e.getFirstIndex(),0,getColumnCount()-1);
        int lastIndex=limit(e.getLastIndex(),0,getColumnCount()-1);
        int minRow=0;
        int maxRow=getRowCount()-1;
        if(getRowSelectionAllowed()){
            minRow=selectionModel.getMinSelectionIndex();
            maxRow=selectionModel.getMaxSelectionIndex();
            int leadRow=getAdjustedIndex(selectionModel.getLeadSelectionIndex(),true);
            if(minRow==-1||maxRow==-1){
                if(leadRow==-1){
                    // nothing to repaint, return
                    return;
                }
                // only thing to repaint is the lead
                minRow=maxRow=leadRow;
            }else{
                // We need to consider more than just the range between
                // the min and max selected index. The lead row, which could
                // be outside this range, should be considered also.
                if(leadRow!=-1){
                    minRow=Math.min(minRow,leadRow);
                    maxRow=Math.max(maxRow,leadRow);
                }
            }
        }
        Rectangle firstColumnRect=getCellRect(minRow,firstIndex,false);
        Rectangle lastColumnRect=getCellRect(maxRow,lastIndex,false);
        Rectangle dirtyRegion=firstColumnRect.union(lastColumnRect);
        repaint(dirtyRegion);
    }
//
// Implementing ListSelectionListener interface
//

    public Dimension getPreferredScrollableViewportSize(){
        return preferredViewportSize;
    }
//
// Implementing the CellEditorListener interface
//

    public void setPreferredScrollableViewportSize(Dimension size){
        preferredViewportSize=size;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction){
        int leadingRow;
        int leadingCol;
        Rectangle leadingCellRect;
        int leadingVisibleEdge;
        int leadingCellEdge;
        int leadingCellSize;
        leadingRow=getLeadingRow(visibleRect);
        leadingCol=getLeadingCol(visibleRect);
        if(orientation==SwingConstants.VERTICAL&&leadingRow<0){
            // Couldn't find leading row - return some default value
            return getRowHeight();
        }else if(orientation==SwingConstants.HORIZONTAL&&leadingCol<0){
            // Couldn't find leading col - return some default value
            return 100;
        }
        // Note that it's possible for one of leadingCol or leadingRow to be
        // -1, depending on the orientation.  This is okay, as getCellRect()
        // still provides enough information to calculate the unit increment.
        leadingCellRect=getCellRect(leadingRow,leadingCol,true);
        leadingVisibleEdge=leadingEdge(visibleRect,orientation);
        leadingCellEdge=leadingEdge(leadingCellRect,orientation);
        if(orientation==SwingConstants.VERTICAL){
            leadingCellSize=leadingCellRect.height;
        }else{
            leadingCellSize=leadingCellRect.width;
        }
        // 4 cases:
        // #1: Leading cell fully visible, reveal next cell
        // #2: Leading cell fully visible, hide leading cell
        // #3: Leading cell partially visible, hide rest of leading cell
        // #4: Leading cell partially visible, reveal rest of leading cell
        if(leadingVisibleEdge==leadingCellEdge){ // Leading cell is fully
            // visible
            // Case #1: Reveal previous cell
            if(direction<0){
                int retVal=0;
                if(orientation==SwingConstants.VERTICAL){
                    // Loop past any zero-height rows
                    while(--leadingRow>=0){
                        retVal=getRowHeight(leadingRow);
                        if(retVal!=0){
                            break;
                        }
                    }
                }else{ // HORIZONTAL
                    // Loop past any zero-width cols
                    while(--leadingCol>=0){
                        retVal=getCellRect(leadingRow,leadingCol,true).width;
                        if(retVal!=0){
                            break;
                        }
                    }
                }
                return retVal;
            }else{ // Case #2: hide leading cell
                return leadingCellSize;
            }
        }else{ // Leading cell is partially hidden
            // Compute visible, hidden portions
            int hiddenAmt=Math.abs(leadingVisibleEdge-leadingCellEdge);
            int visibleAmt=leadingCellSize-hiddenAmt;
            if(direction>0){
                // Case #3: hide showing portion of leading cell
                return visibleAmt;
            }else{ // Case #4: reveal hidden portion of leading cell
                return hiddenAmt;
            }
        }
    }    public void editingCanceled(ChangeEvent e){
        removeEditor();
    }
//
// Implementing the Scrollable interface
//

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,int direction){
        if(getRowCount()==0){
            // Short-circuit empty table model
            if(SwingConstants.VERTICAL==orientation){
                int rh=getRowHeight();
                return (rh>0)?Math.max(rh,(visibleRect.height/rh)*rh):
                        visibleRect.height;
            }else{
                return visibleRect.width;
            }
        }
        // Shortcut for vertical scrolling of a table w/ uniform row height
        if(null==rowModel&&SwingConstants.VERTICAL==orientation){
            int row=rowAtPoint(visibleRect.getLocation());
            assert row!=-1;
            int col=columnAtPoint(visibleRect.getLocation());
            Rectangle cellRect=getCellRect(row,col,true);
            if(cellRect.y==visibleRect.y){
                int rh=getRowHeight();
                assert rh>0;
                return Math.max(rh,(visibleRect.height/rh)*rh);
            }
        }
        if(direction<0){
            return getPreviousBlockIncrement(visibleRect,orientation);
        }else{
            return getNextBlockIncrement(visibleRect,orientation);
        }
    }

    public boolean getScrollableTracksViewportWidth(){
        return !(autoResizeMode==AUTO_RESIZE_OFF);
    }

    public boolean getScrollableTracksViewportHeight(){
        Container parent=SwingUtilities.getUnwrappedParent(this);
        return getFillsViewportHeight()
                &&parent instanceof JViewport
                &&parent.getHeight()>getPreferredSize().height;
    }

    public boolean getFillsViewportHeight(){
        return fillsViewportHeight;
    }

    public void setFillsViewportHeight(boolean fillsViewportHeight){
        boolean old=this.fillsViewportHeight;
        this.fillsViewportHeight=fillsViewportHeight;
        resizeAndRepaint();
        firePropertyChange("fillsViewportHeight",old,fillsViewportHeight);
    }

    private int getPreviousBlockIncrement(Rectangle visibleRect,
                                          int orientation){
        // Measure back from visible leading edge
        // If we hit the cell on its leading edge, it becomes the leading cell.
        // Else, use following cell
        int row;
        int col;
        int newEdge;
        Point newCellLoc;
        int visibleLeadingEdge=leadingEdge(visibleRect,orientation);
        boolean leftToRight=getComponentOrientation().isLeftToRight();
        int newLeadingEdge;
        // Roughly determine the new leading edge by measuring back from the
        // leading visible edge by the size of the visible rect, and find the
        // cell there.
        if(orientation==SwingConstants.VERTICAL){
            newEdge=visibleLeadingEdge-visibleRect.height;
            int x=visibleRect.x+(leftToRight?0:visibleRect.width);
            newCellLoc=new Point(x,newEdge);
        }else if(leftToRight){
            newEdge=visibleLeadingEdge-visibleRect.width;
            newCellLoc=new Point(newEdge,visibleRect.y);
        }else{ // Horizontal, right-to-left
            newEdge=visibleLeadingEdge+visibleRect.width;
            newCellLoc=new Point(newEdge-1,visibleRect.y);
        }
        row=rowAtPoint(newCellLoc);
        col=columnAtPoint(newCellLoc);
        // If we're measuring past the beginning of the table, we get an invalid
        // cell.  Just go to the beginning of the table in this case.
        if(orientation==SwingConstants.VERTICAL&row<0){
            newLeadingEdge=0;
        }else if(orientation==SwingConstants.HORIZONTAL&col<0){
            if(leftToRight){
                newLeadingEdge=0;
            }else{
                newLeadingEdge=getWidth();
            }
        }else{
            // Refine our measurement
            Rectangle newCellRect=getCellRect(row,col,true);
            int newCellLeadingEdge=leadingEdge(newCellRect,orientation);
            int newCellTrailingEdge=trailingEdge(newCellRect,orientation);
            // Usually, we hit in the middle of newCell, and want to scroll to
            // the beginning of the cell after newCell.  But there are a
            // couple corner cases where we want to scroll to the beginning of
            // newCell itself.  These cases are:
            // 1) newCell is so large that it ends at or extends into the
            //    visibleRect (newCell is the leading cell, or is adjacent to
            //    the leading cell)
            // 2) newEdge happens to fall right on the beginning of a cell
            // Case 1
            if((orientation==SwingConstants.VERTICAL||leftToRight)&&
                    (newCellTrailingEdge>=visibleLeadingEdge)){
                newLeadingEdge=newCellLeadingEdge;
            }else if(orientation==SwingConstants.HORIZONTAL&&
                    !leftToRight&&
                    newCellTrailingEdge<=visibleLeadingEdge){
                newLeadingEdge=newCellLeadingEdge;
            }
            // Case 2:
            else if(newEdge==newCellLeadingEdge){
                newLeadingEdge=newCellLeadingEdge;
            }
            // Common case: scroll to cell after newCell
            else{
                newLeadingEdge=newCellTrailingEdge;
            }
        }
        return Math.abs(visibleLeadingEdge-newLeadingEdge);
    }

    private int getNextBlockIncrement(Rectangle visibleRect,
                                      int orientation){
        // Find the cell at the trailing edge.  Return the distance to put
        // that cell at the leading edge.
        int trailingRow=getTrailingRow(visibleRect);
        int trailingCol=getTrailingCol(visibleRect);
        Rectangle cellRect;
        boolean cellFillsVis;
        int cellLeadingEdge;
        int cellTrailingEdge;
        int newLeadingEdge;
        int visibleLeadingEdge=leadingEdge(visibleRect,orientation);
        // If we couldn't find trailing cell, just return the size of the
        // visibleRect.  Note that, for instance, we don't need the
        // trailingCol to proceed if we're scrolling vertically, because
        // cellRect will still fill in the required dimensions.  This would
        // happen if we're scrolling vertically, and the table is not wide
        // enough to fill the visibleRect.
        if(orientation==SwingConstants.VERTICAL&&trailingRow<0){
            return visibleRect.height;
        }else if(orientation==SwingConstants.HORIZONTAL&&trailingCol<0){
            return visibleRect.width;
        }
        cellRect=getCellRect(trailingRow,trailingCol,true);
        cellLeadingEdge=leadingEdge(cellRect,orientation);
        cellTrailingEdge=trailingEdge(cellRect,orientation);
        if(orientation==SwingConstants.VERTICAL||
                getComponentOrientation().isLeftToRight()){
            cellFillsVis=cellLeadingEdge<=visibleLeadingEdge;
        }else{ // Horizontal, right-to-left
            cellFillsVis=cellLeadingEdge>=visibleLeadingEdge;
        }
        if(cellFillsVis){
            // The visibleRect contains a single large cell.  Scroll to the end
            // of this cell, so the following cell is the first cell.
            newLeadingEdge=cellTrailingEdge;
        }else if(cellTrailingEdge==trailingEdge(visibleRect,orientation)){
            // The trailing cell happens to end right at the end of the
            // visibleRect.  Again, scroll to the beginning of the next cell.
            newLeadingEdge=cellTrailingEdge;
        }else{
            // Common case: the trailing cell is partially visible, and isn't
            // big enough to take up the entire visibleRect.  Scroll so it
            // becomes the leading cell.
            newLeadingEdge=cellLeadingEdge;
        }
        return Math.abs(newLeadingEdge-visibleLeadingEdge);
    }

    private int getLeadingRow(Rectangle visibleRect){
        Point leadingPoint;
        if(getComponentOrientation().isLeftToRight()){
            leadingPoint=new Point(visibleRect.x,visibleRect.y);
        }else{
            leadingPoint=new Point(visibleRect.x+visibleRect.width-1,
                    visibleRect.y);
        }
        return rowAtPoint(leadingPoint);
    }

    private int getLeadingCol(Rectangle visibleRect){
        Point leadingPoint;
        if(getComponentOrientation().isLeftToRight()){
            leadingPoint=new Point(visibleRect.x,visibleRect.y);
        }else{
            leadingPoint=new Point(visibleRect.x+visibleRect.width-1,
                    visibleRect.y);
        }
        return columnAtPoint(leadingPoint);
    }

    private int getTrailingRow(Rectangle visibleRect){
        Point trailingPoint;
        if(getComponentOrientation().isLeftToRight()){
            trailingPoint=new Point(visibleRect.x,
                    visibleRect.y+visibleRect.height-1);
        }else{
            trailingPoint=new Point(visibleRect.x+visibleRect.width-1,
                    visibleRect.y+visibleRect.height-1);
        }
        return rowAtPoint(trailingPoint);
    }

    private int getTrailingCol(Rectangle visibleRect){
        Point trailingPoint;
        if(getComponentOrientation().isLeftToRight()){
            trailingPoint=new Point(visibleRect.x+visibleRect.width-1,
                    visibleRect.y);
        }else{
            trailingPoint=new Point(visibleRect.x,visibleRect.y);
        }
        return columnAtPoint(trailingPoint);
    }

    private int leadingEdge(Rectangle rect,int orientation){
        if(orientation==SwingConstants.VERTICAL){
            return rect.y;
        }else if(getComponentOrientation().isLeftToRight()){
            return rect.x;
        }else{ // Horizontal, right-to-left
            return rect.x+rect.width;
        }
    }

    private int trailingEdge(Rectangle rect,int orientation){
        if(orientation==SwingConstants.VERTICAL){
            return rect.y+rect.height;
        }else if(getComponentOrientation().isLeftToRight()){
            return rect.x+rect.width;
        }else{ // Horizontal, right-to-left
            return rect.x;
        }
    }

    protected void initializeLocalVars(){
        updateSelectionOnSort=true;
        setOpaque(true);
        createDefaultRenderers();
        createDefaultEditors();
        setTableHeader(createDefaultTableHeader());
        setShowGrid(true);
        setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setRowHeight(16);
        isRowHeightSet=false;
        setRowMargin(1);
        setRowSelectionAllowed(true);
        setCellEditor(null);
        setEditingColumn(-1);
        setEditingRow(-1);
        setSurrendersFocusOnKeystroke(false);
        setPreferredScrollableViewportSize(new Dimension(450,400));
        // I'm registered to do tool tips so we can draw tips for the renderers
        ToolTipManager toolTipManager=ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        setAutoscrolls(true);
    }

    protected TableModel createDefaultDataModel(){
        return new DefaultTableModel();
    }

    protected TableColumnModel createDefaultColumnModel(){
        return new DefaultTableColumnModel();
    }
//
// Protected Methods
//

    protected ListSelectionModel createDefaultSelectionModel(){
        return new DefaultListSelectionModel();
    }

    protected JTableHeader createDefaultTableHeader(){
        return new JTableHeader(columnModel);
    }

    public TableCellRenderer getCellRenderer(int row,int column){
        TableColumn tableColumn=getColumnModel().getColumn(column);
        TableCellRenderer renderer=tableColumn.getCellRenderer();
        if(renderer==null){
            renderer=getDefaultRenderer(getColumnClass(column));
        }
        return renderer;
    }

    public Component prepareRenderer(TableCellRenderer renderer,int row,int column){
        Object value=getValueAt(row,column);
        boolean isSelected=false;
        boolean hasFocus=false;
        // Only indicate the selection and focused cell if not printing
        if(!isPaintingForPrint()){
            isSelected=isCellSelected(row,column);
            boolean rowIsLead=
                    (selectionModel.getLeadSelectionIndex()==row);
            boolean colIsLead=
                    (columnModel.getSelectionModel().getLeadSelectionIndex()==column);
            hasFocus=(rowIsLead&&colIsLead)&&isFocusOwner();
        }
        return renderer.getTableCellRendererComponent(this,value,
                isSelected,hasFocus,
                row,column);
    }

    public TableCellEditor getCellEditor(int row,int column){
        TableColumn tableColumn=getColumnModel().getColumn(column);
        TableCellEditor editor=tableColumn.getCellEditor();
        if(editor==null){
            editor=getDefaultEditor(getColumnClass(column));
        }
        return editor;
    }

    public Component prepareEditor(TableCellEditor editor,int row,int column){
        Object value=getValueAt(row,column);
        boolean isSelected=isCellSelected(row,column);
        Component comp=editor.getTableCellEditorComponent(this,value,isSelected,
                row,column);
        if(comp instanceof JComponent){
            JComponent jComp=(JComponent)comp;
            if(jComp.getNextFocusableComponent()==null){
                jComp.setNextFocusableComponent(this);
            }
        }
        return comp;
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

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        if((ui!=null)&&(getUIClassID().equals(uiClassID))){
            ui.installUI(this);
        }
        createDefaultRenderers();
        createDefaultEditors();
        // If ToolTipText != null, then the tooltip has already been
        // registered by JComponent.readObject() and we don't want
        // to re-register here
        if(getToolTipText()==null){
            ToolTipManager.sharedInstance().registerComponent(this);
        }
    }

    protected void createDefaultRenderers(){
        defaultRenderersByColumnClass=new UIDefaults(8,0.75f);
        // Objects
        defaultRenderersByColumnClass.put(Object.class,(UIDefaults.LazyValue)t->new DefaultTableCellRenderer.UIResource());
        // Numbers
        defaultRenderersByColumnClass.put(Number.class,(UIDefaults.LazyValue)t->new NumberRenderer());
        // Doubles and Floats
        defaultRenderersByColumnClass.put(Float.class,(UIDefaults.LazyValue)t->new DoubleRenderer());
        defaultRenderersByColumnClass.put(Double.class,(UIDefaults.LazyValue)t->new DoubleRenderer());
        // Dates
        defaultRenderersByColumnClass.put(Date.class,(UIDefaults.LazyValue)t->new DateRenderer());
        // Icons and ImageIcons
        defaultRenderersByColumnClass.put(Icon.class,(UIDefaults.LazyValue)t->new IconRenderer());
        defaultRenderersByColumnClass.put(ImageIcon.class,(UIDefaults.LazyValue)t->new IconRenderer());
        // Booleans
        defaultRenderersByColumnClass.put(Boolean.class,(UIDefaults.LazyValue)t->new BooleanRenderer());
    }

    protected void createDefaultEditors(){
        defaultEditorsByColumnClass=new UIDefaults(3,0.75f);
        // Objects
        defaultEditorsByColumnClass.put(Object.class,(UIDefaults.LazyValue)t->new GenericEditor());
        // Numbers
        defaultEditorsByColumnClass.put(Number.class,(UIDefaults.LazyValue)t->new NumberEditor());
        // Booleans
        defaultEditorsByColumnClass.put(Boolean.class,(UIDefaults.LazyValue)t->new BooleanEditor());
    }

    public boolean print() throws PrinterException{
        return print(PrintMode.FIT_WIDTH);
    }

    public boolean print(PrintMode printMode) throws PrinterException{
        return print(printMode,null,null);
    }

    public boolean print(PrintMode printMode,
                         MessageFormat headerFormat,
                         MessageFormat footerFormat) throws PrinterException{
        boolean showDialogs=!GraphicsEnvironment.isHeadless();
        return print(printMode,headerFormat,footerFormat,
                showDialogs,null,showDialogs);
    }

    public boolean print(PrintMode printMode,
                         MessageFormat headerFormat,
                         MessageFormat footerFormat,
                         boolean showPrintDialog,
                         PrintRequestAttributeSet attr,
                         boolean interactive) throws PrinterException,
            HeadlessException{
        return print(printMode,
                headerFormat,
                footerFormat,
                showPrintDialog,
                attr,
                interactive,
                null);
    }

    public boolean print(PrintMode printMode,
                         MessageFormat headerFormat,
                         MessageFormat footerFormat,
                         boolean showPrintDialog,
                         PrintRequestAttributeSet attr,
                         boolean interactive,
                         PrintService service) throws PrinterException,
            HeadlessException{
        // complain early if an invalid parameter is specified for headless mode
        boolean isHeadless=GraphicsEnvironment.isHeadless();
        if(isHeadless){
            if(showPrintDialog){
                throw new HeadlessException("Can't show print dialog.");
            }
            if(interactive){
                throw new HeadlessException("Can't run interactively.");
            }
        }
        // Get a PrinterJob.
        // Do this before anything with side-effects since it may throw a
        // security exception - in which case we don't want to do anything else.
        final PrinterJob job=PrinterJob.getPrinterJob();
        if(isEditing()){
            // try to stop cell editing, and failing that, cancel it
            if(!getCellEditor().stopCellEditing()){
                getCellEditor().cancelCellEditing();
            }
        }
        if(attr==null){
            attr=new HashPrintRequestAttributeSet();
        }
        final PrintingStatus printingStatus;
        // fetch the Printable
        Printable printable=
                getPrintable(printMode,headerFormat,footerFormat);
        if(interactive){
            // wrap the Printable so that we can print on another thread
            printable=new ThreadSafePrintable(printable);
            printingStatus=PrintingStatus.createPrintingStatus(this,job);
            printable=printingStatus.createNotificationPrintable(printable);
        }else{
            // to please compiler
            printingStatus=null;
        }
        // set the printable on the PrinterJob
        job.setPrintable(printable);
        // if specified, set the PrintService on the PrinterJob
        if(service!=null){
            job.setPrintService(service);
        }
        // if requested, show the print dialog
        if(showPrintDialog&&!job.printDialog(attr)){
            // the user cancelled the print dialog
            return false;
        }
        // if not interactive, just print on this thread (no dialog)
        if(!interactive){
            // do the printing
            job.print(attr);
            // we're done
            return true;
        }
        // make sure this is clear since we'll check it after
        printError=null;
        // to synchronize on
        final Object lock=new Object();
        // copied so we can access from the inner class
        final PrintRequestAttributeSet copyAttr=attr;
        // this runnable will be used to do the printing
        // (and save any throwables) on another thread
        Runnable runnable=new Runnable(){
            public void run(){
                try{
                    // do the printing
                    job.print(copyAttr);
                }catch(Throwable t){
                    // save any Throwable to be rethrown
                    synchronized(lock){
                        printError=t;
                    }
                }finally{
                    // we're finished - hide the dialog
                    printingStatus.dispose();
                }
            }
        };
        // start printing on another thread
        Thread th=new Thread(runnable);
        th.start();
        printingStatus.showModal(true);
        // look for any error that the printing may have generated
        Throwable pe;
        synchronized(lock){
            pe=printError;
            printError=null;
        }
        // check the type of error and handle it
        if(pe!=null){
            // a subclass of PrinterException meaning the job was aborted,
            // in this case, by the user
            if(pe instanceof PrinterAbortException){
                return false;
            }else if(pe instanceof PrinterException){
                throw (PrinterException)pe;
            }else if(pe instanceof RuntimeException){
                throw (RuntimeException)pe;
            }else if(pe instanceof Error){
                throw (Error)pe;
            }
            // can not happen
            throw new AssertionError(pe);
        }
        return true;
    }

    public Printable getPrintable(PrintMode printMode,
                                  MessageFormat headerFormat,
                                  MessageFormat footerFormat){
        return new TablePrintable(this,printMode,headerFormat,footerFormat);
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJTable();
        }
        return accessibleContext;
    }

    public enum PrintMode{
        NORMAL,
        FIT_WIDTH
    }

    private interface Resizable2{
        public int getElementCount();

        public int getLowerBoundAt(int i);

        public int getUpperBoundAt(int i);

        public void setSizeAt(int newSize,int i);
    }

    private interface Resizable3 extends Resizable2{
        public int getMidPointAt(int i);
    }

    public static final class DropLocation extends TransferHandler.DropLocation{
        private final int row;
        private final int col;
        private final boolean isInsertRow;
        private final boolean isInsertCol;

        private DropLocation(Point p,int row,int col,
                             boolean isInsertRow,boolean isInsertCol){
            super(p);
            this.row=row;
            this.col=col;
            this.isInsertRow=isInsertRow;
            this.isInsertCol=isInsertCol;
        }

        public int getRow(){
            return row;
        }

        public int getColumn(){
            return col;
        }

        public boolean isInsertRow(){
            return isInsertRow;
        }

        public boolean isInsertColumn(){
            return isInsertCol;
        }

        public String toString(){
            return getClass().getName()
                    +"[dropPoint="+getDropPoint()+","
                    +"row="+row+","
                    +"column="+col+","
                    +"insertRow="+isInsertRow+","
                    +"insertColumn="+isInsertCol+"]";
        }
    }

    static class NumberRenderer extends DefaultTableCellRenderer.UIResource{
        public NumberRenderer(){
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }
    }

    static class DoubleRenderer extends NumberRenderer{
        NumberFormat formatter;

        public DoubleRenderer(){
            super();
        }

        public void setValue(Object value){
            if(formatter==null){
                formatter=NumberFormat.getInstance();
            }
            setText((value==null)?"":formatter.format(value));
        }
    }

    static class DateRenderer extends DefaultTableCellRenderer.UIResource{
        DateFormat formatter;

        public DateRenderer(){
            super();
        }

        public void setValue(Object value){
            if(formatter==null){
                formatter=DateFormat.getDateInstance();
            }
            setText((value==null)?"":formatter.format(value));
        }
    }

    static class IconRenderer extends DefaultTableCellRenderer.UIResource{
        public IconRenderer(){
            super();
            setHorizontalAlignment(JLabel.CENTER);
        }

        public void setValue(Object value){
            setIcon((value instanceof Icon)?(Icon)value:null);
        }
    }

    static class BooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource{
        private static final Border noFocusBorder=new EmptyBorder(1,1,1,1);

        public BooleanRenderer(){
            super();
            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }

        public Component getTableCellRendererComponent(JTable table,Object value,
                                                       boolean isSelected,boolean hasFocus,int row,int column){
            if(isSelected){
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            }else{
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setSelected((value!=null&&((Boolean)value).booleanValue()));
            if(hasFocus){
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            }else{
                setBorder(noFocusBorder);
            }
            return this;
        }
    }
//
// Serialization
//

    static class GenericEditor extends DefaultCellEditor{
        Class[] argTypes=new Class[]{String.class};
        java.lang.reflect.Constructor constructor;
        Object value;

        public GenericEditor(){
            super(new JTextField());
            getComponent().setName("Table.editor");
        }

        public Object getCellEditorValue(){
            return value;
        }

        public boolean stopCellEditing(){
            String s=(String)super.getCellEditorValue();
            // Here we are dealing with the case where a user
            // has deleted the string value in a cell, possibly
            // after a failed validation. Return null, so that
            // they have the option to replace the value with
            // null or use escape to restore the original.
            // For Strings, return "" for backward compatibility.
            try{
                if("".equals(s)){
                    if(constructor.getDeclaringClass()==String.class){
                        value=s;
                    }
                    return super.stopCellEditing();
                }
                SwingUtilities2.checkAccess(constructor.getModifiers());
                value=constructor.newInstance(new Object[]{s});
            }catch(Exception e){
                ((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }

        public Component getTableCellEditorComponent(JTable table,Object value,
                                                     boolean isSelected,
                                                     int row,int column){
            this.value=null;
            ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
            try{
                Class<?> type=table.getColumnClass(column);
                // Since our obligation is to produce a value which is
                // assignable for the required type it is OK to use the
                // String constructor for columns which are declared
                // to contain Objects. A String is an Object.
                if(type==Object.class){
                    type=String.class;
                }
                ReflectUtil.checkPackageAccess(type);
                SwingUtilities2.checkAccess(type.getModifiers());
                constructor=type.getConstructor(argTypes);
            }catch(Exception e){
                return null;
            }
            return super.getTableCellEditorComponent(table,value,isSelected,row,column);
        }
    }

    static class NumberEditor extends GenericEditor{
        public NumberEditor(){
            ((JTextField)getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    static class BooleanEditor extends DefaultCellEditor{
        public BooleanEditor(){
            super(new JCheckBox());
            JCheckBox checkBox=(JCheckBox)getComponent();
            checkBox.setHorizontalAlignment(JCheckBox.CENTER);
        }
    }

    private final class SortManager{
        RowSorter<? extends TableModel> sorter;
        // Selection, in terms of the model. This is lazily created
        // as needed.
        private ListSelectionModel modelSelection;
        private int modelLeadIndex;
        // Set to true while in the process of changing the selection.
        // If this is true the selection change is ignored.
        private boolean syncingSelection;
        // Temporary cache of selection, in terms of model. This is only used
        // if we don't need the full weight of modelSelection.
        private int[] lastModelSelection;
        // Heights of the rows in terms of the model.
        private SizeSequence modelRowSizes;

        SortManager(RowSorter<? extends TableModel> sorter){
            this.sorter=sorter;
            sorter.addRowSorterListener(JTable.this);
        }

        public void dispose(){
            if(sorter!=null){
                sorter.removeRowSorterListener(JTable.this);
            }
        }

        public void setViewRowHeight(int viewIndex,int rowHeight){
            if(modelRowSizes==null){
                modelRowSizes=new SizeSequence(getModel().getRowCount(),
                        getRowHeight());
            }
            modelRowSizes.setSize(convertRowIndexToModel(viewIndex),rowHeight);
        }

        public void allChanged(){
            modelLeadIndex=-1;
            modelSelection=null;
            modelRowSizes=null;
        }

        public void viewSelectionChanged(ListSelectionEvent e){
            if(!syncingSelection&&modelSelection!=null){
                modelSelection=null;
            }
        }

        public void prepareForChange(RowSorterEvent sortEvent,
                                     ModelChange change){
            if(getUpdateSelectionOnSort()){
                cacheSelection(sortEvent,change);
            }
        }

        private void cacheSelection(RowSorterEvent sortEvent,
                                    ModelChange change){
            if(sortEvent!=null){
                // sort order changed. If modelSelection is null and filtering
                // is enabled we need to cache the selection in terms of the
                // underlying model, this will allow us to correctly restore
                // the selection even if rows are filtered out.
                if(modelSelection==null&&
                        sorter.getViewRowCount()!=getModel().getRowCount()){
                    modelSelection=new DefaultListSelectionModel();
                    ListSelectionModel viewSelection=getSelectionModel();
                    int min=viewSelection.getMinSelectionIndex();
                    int max=viewSelection.getMaxSelectionIndex();
                    int modelIndex;
                    for(int viewIndex=min;viewIndex<=max;viewIndex++){
                        if(viewSelection.isSelectedIndex(viewIndex)){
                            modelIndex=convertRowIndexToModel(
                                    sortEvent,viewIndex);
                            if(modelIndex!=-1){
                                modelSelection.addSelectionInterval(
                                        modelIndex,modelIndex);
                            }
                        }
                    }
                    modelIndex=convertRowIndexToModel(sortEvent,
                            viewSelection.getLeadSelectionIndex());
                    SwingUtilities2.setLeadAnchorWithoutSelection(
                            modelSelection,modelIndex,modelIndex);
                }else if(modelSelection==null){
                    // Sorting changed, haven't cached selection in terms
                    // of model and no filtering. Temporarily cache selection.
                    cacheModelSelection(sortEvent);
                }
            }else if(change.allRowsChanged){
                // All the rows have changed, chuck any cached selection.
                modelSelection=null;
            }else if(modelSelection!=null){
                // Table changed, reflect changes in cached selection model.
                switch(change.type){
                    case TableModelEvent.DELETE:
                        modelSelection.removeIndexInterval(change.startModelIndex,
                                change.endModelIndex);
                        break;
                    case TableModelEvent.INSERT:
                        modelSelection.insertIndexInterval(change.startModelIndex,
                                change.length,
                                true);
                        break;
                    default:
                        break;
                }
            }else{
                // table changed, but haven't cached rows, temporarily
                // cache them.
                cacheModelSelection(null);
            }
        }

        private void cacheModelSelection(RowSorterEvent sortEvent){
            lastModelSelection=convertSelectionToModel(sortEvent);
            modelLeadIndex=convertRowIndexToModel(sortEvent,
                    selectionModel.getLeadSelectionIndex());
        }

        public void processChange(RowSorterEvent sortEvent,
                                  ModelChange change,
                                  boolean sorterChanged){
            if(change!=null){
                if(change.allRowsChanged){
                    modelRowSizes=null;
                    rowModel=null;
                }else if(modelRowSizes!=null){
                    if(change.type==TableModelEvent.INSERT){
                        modelRowSizes.insertEntries(change.startModelIndex,
                                change.endModelIndex-
                                        change.startModelIndex+1,
                                getRowHeight());
                    }else if(change.type==TableModelEvent.DELETE){
                        modelRowSizes.removeEntries(change.startModelIndex,
                                change.endModelIndex-
                                        change.startModelIndex+1);
                    }
                }
            }
            if(sorterChanged){
                setViewRowHeightsFromModel();
                restoreSelection(change);
            }
        }

        private void setViewRowHeightsFromModel(){
            if(modelRowSizes!=null){
                rowModel.setSizes(getRowCount(),getRowHeight());
                for(int viewIndex=getRowCount()-1;viewIndex>=0;
                    viewIndex--){
                    int modelIndex=convertRowIndexToModel(viewIndex);
                    rowModel.setSize(viewIndex,
                            modelRowSizes.getSize(modelIndex));
                }
            }
        }

        private void restoreSelection(ModelChange change){
            syncingSelection=true;
            if(lastModelSelection!=null){
                restoreSortingSelection(lastModelSelection,
                        modelLeadIndex,change);
                lastModelSelection=null;
            }else if(modelSelection!=null){
                ListSelectionModel viewSelection=getSelectionModel();
                viewSelection.setValueIsAdjusting(true);
                viewSelection.clearSelection();
                int min=modelSelection.getMinSelectionIndex();
                int max=modelSelection.getMaxSelectionIndex();
                int viewIndex;
                for(int modelIndex=min;modelIndex<=max;modelIndex++){
                    if(modelSelection.isSelectedIndex(modelIndex)){
                        viewIndex=convertRowIndexToView(modelIndex);
                        if(viewIndex!=-1){
                            viewSelection.addSelectionInterval(viewIndex,
                                    viewIndex);
                        }
                    }
                }
                // Restore the lead
                int viewLeadIndex=modelSelection.getLeadSelectionIndex();
                if(viewLeadIndex!=-1&&!modelSelection.isSelectionEmpty()){
                    viewLeadIndex=convertRowIndexToView(viewLeadIndex);
                }
                SwingUtilities2.setLeadAnchorWithoutSelection(
                        viewSelection,viewLeadIndex,viewLeadIndex);
                viewSelection.setValueIsAdjusting(false);
            }
            syncingSelection=false;
        }
    }

    private final class ModelChange{
        // Starting index of the change, in terms of the model
        int startModelIndex;
        // Ending index of the change, in terms of the model
        int endModelIndex;
        // Type of change
        int type;
        // Number of rows in the model
        int modelRowCount;
        // The event that triggered this.
        TableModelEvent event;
        // Length of the change (end - start + 1)
        int length;
        // True if the event indicates all the contents have changed
        boolean allRowsChanged;

        ModelChange(TableModelEvent e){
            startModelIndex=Math.max(0,e.getFirstRow());
            endModelIndex=e.getLastRow();
            modelRowCount=getModel().getRowCount();
            if(endModelIndex<0){
                endModelIndex=Math.max(0,modelRowCount-1);
            }
            length=endModelIndex-startModelIndex+1;
            type=e.getType();
            event=e;
            allRowsChanged=(e.getLastRow()==Integer.MAX_VALUE);
        }
    }
/////////////////
// Printing Support
/////////////////

    // This class tracks changes in the keyboard focus state. It is used
    // when the JTable is editing to determine when to cancel the edit.
    // If focus switches to a component outside of the jtable, but in the
    // same window, this will cancel editing.
    class CellEditorRemover implements PropertyChangeListener{
        KeyboardFocusManager focusManager;

        public CellEditorRemover(KeyboardFocusManager fm){
            this.focusManager=fm;
        }

        public void propertyChange(PropertyChangeEvent ev){
            if(!isEditing()||getClientProperty("terminateEditOnFocusLost")!=Boolean.TRUE){
                return;
            }
            Component c=focusManager.getPermanentFocusOwner();
            while(c!=null){
                if(c==JTable.this){
                    // focus remains inside the table
                    return;
                }else if((c instanceof Window)||
                        (c instanceof Applet&&c.getParent()==null)){
                    if(c==SwingUtilities.getRoot(JTable.this)){
                        if(!getCellEditor().stopCellEditing()){
                            getCellEditor().cancelCellEditing();
                        }
                    }
                    break;
                }
                c=c.getParent();
            }
        }
    }

    private class ThreadSafePrintable implements Printable{
        private Printable printDelegate;
        private int retVal;
        private Throwable retThrowable;

        public ThreadSafePrintable(Printable printDelegate){
            this.printDelegate=printDelegate;
        }

        public int print(final Graphics graphics,
                         final PageFormat pageFormat,
                         final int pageIndex) throws PrinterException{
            // We'll use this Runnable
            Runnable runnable=new Runnable(){
                public synchronized void run(){
                    try{
                        // call into the delegate and save the return value
                        retVal=printDelegate.print(graphics,pageFormat,pageIndex);
                    }catch(Throwable throwable){
                        // save any Throwable to be rethrown
                        retThrowable=throwable;
                    }finally{
                        // notify the caller that we're done
                        notifyAll();
                    }
                }
            };
            synchronized(runnable){
                // make sure these are initialized
                retVal=-1;
                retThrowable=null;
                // call into the EDT
                SwingUtilities.invokeLater(runnable);
                // wait for the runnable to finish
                while(retVal==-1&&retThrowable==null){
                    try{
                        runnable.wait();
                    }catch(InterruptedException ie){
                        // short process, safe to ignore interrupts
                    }
                }
                // if the delegate threw a throwable, rethrow it here
                if(retThrowable!=null){
                    if(retThrowable instanceof PrinterException){
                        throw (PrinterException)retThrowable;
                    }else if(retThrowable instanceof RuntimeException){
                        throw (RuntimeException)retThrowable;
                    }else if(retThrowable instanceof Error){
                        throw (Error)retThrowable;
                    }
                    // can not happen
                    throw new AssertionError(retThrowable);
                }
                return retVal;
            }
        }
    }

    //
    // *** should also implement AccessibleSelection?
    // *** and what's up with keyboard navigation/manipulation?
    //
    protected class AccessibleJTable extends AccessibleJComponent
            implements AccessibleSelection, ListSelectionListener, TableModelListener,
            TableColumnModelListener, CellEditorListener, PropertyChangeListener,
            AccessibleExtendedTable{
        int previousFocusedRow;
        int previousFocusedCol;
        // end of AccessibleExtendedTable implementation ------------
        // start of AccessibleTable implementation ------------------
        private Accessible caption;
        // Listeners to track model, etc. changes to as to re-place the other
        // listeners
        private Accessible summary;        public void propertyChange(PropertyChangeEvent e){
            String name=e.getPropertyName();
            Object oldValue=e.getOldValue();
            Object newValue=e.getNewValue();
            // re-set tableModel listeners
            if(name.compareTo("model")==0){
                if(oldValue!=null&&oldValue instanceof TableModel){
                    ((TableModel)oldValue).removeTableModelListener(this);
                }
                if(newValue!=null&&newValue instanceof TableModel){
                    ((TableModel)newValue).addTableModelListener(this);
                }
                // re-set selectionModel listeners
            }else if(name.compareTo("selectionModel")==0){
                Object source=e.getSource();
                if(source==JTable.this){    // row selection model
                    if(oldValue!=null&&
                            oldValue instanceof ListSelectionModel){
                        ((ListSelectionModel)oldValue).removeListSelectionListener(this);
                    }
                    if(newValue!=null&&
                            newValue instanceof ListSelectionModel){
                        ((ListSelectionModel)newValue).addListSelectionListener(this);
                    }
                }else if(source==JTable.this.getColumnModel()){
                    if(oldValue!=null&&
                            oldValue instanceof ListSelectionModel){
                        ((ListSelectionModel)oldValue).removeListSelectionListener(this);
                    }
                    if(newValue!=null&&
                            newValue instanceof ListSelectionModel){
                        ((ListSelectionModel)newValue).addListSelectionListener(this);
                    }
                }else{
                    //        System.out.println("!!! Bug in source of selectionModel propertyChangeEvent");
                }
                // re-set columnModel listeners
                // and column's selection property listener as well
            }else if(name.compareTo("columnModel")==0){
                if(oldValue!=null&&oldValue instanceof TableColumnModel){
                    TableColumnModel tcm=(TableColumnModel)oldValue;
                    tcm.removeColumnModelListener(this);
                    tcm.getSelectionModel().removeListSelectionListener(this);
                }
                if(newValue!=null&&newValue instanceof TableColumnModel){
                    TableColumnModel tcm=(TableColumnModel)newValue;
                    tcm.addColumnModelListener(this);
                    tcm.getSelectionModel().addListSelectionListener(this);
                }
                // re-se cellEditor listeners
            }else if(name.compareTo("tableCellEditor")==0){
                if(oldValue!=null&&oldValue instanceof TableCellEditor){
                    ((TableCellEditor)oldValue).removeCellEditorListener(this);
                }
                if(newValue!=null&&newValue instanceof TableCellEditor){
                    ((TableCellEditor)newValue).addCellEditorListener(this);
                }
            }
        }
        // Listeners to echo changes to the AccessiblePropertyChange mechanism
        private Accessible[] rowDescription;
        private Accessible[] columnDescription;        public void tableChanged(TableModelEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    null,null);
            if(e!=null){
                int firstColumn=e.getColumn();
                int lastColumn=e.getColumn();
                if(firstColumn==TableModelEvent.ALL_COLUMNS){
                    firstColumn=0;
                    lastColumn=getColumnCount()-1;
                }
                // Fire a property change event indicating the table model
                // has changed.
                AccessibleJTableModelChange change=
                        new AccessibleJTableModelChange(e.getType(),
                                e.getFirstRow(),
                                e.getLastRow(),
                                firstColumn,
                                lastColumn);
                firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                        null,change);
            }
        }

        protected AccessibleJTable(){
            super();
            JTable.this.addPropertyChangeListener(this);
            JTable.this.getSelectionModel().addListSelectionListener(this);
            TableColumnModel tcm=JTable.this.getColumnModel();
            tcm.addColumnModelListener(this);
            tcm.getSelectionModel().addListSelectionListener(this);
            JTable.this.getModel().addTableModelListener(this);
            previousFocusedRow=JTable.this.getSelectionModel().
                    getLeadSelectionIndex();
            previousFocusedCol=JTable.this.getColumnModel().
                    getSelectionModel().getLeadSelectionIndex();
        }

        public void tableRowsInserted(TableModelEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    null,null);
            // Fire a property change event indicating the table model
            // has changed.
            int firstColumn=e.getColumn();
            int lastColumn=e.getColumn();
            if(firstColumn==TableModelEvent.ALL_COLUMNS){
                firstColumn=0;
                lastColumn=getColumnCount()-1;
            }
            AccessibleJTableModelChange change=
                    new AccessibleJTableModelChange(e.getType(),
                            e.getFirstRow(),
                            e.getLastRow(),
                            firstColumn,
                            lastColumn);
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                    null,change);
        }

        public void tableRowsDeleted(TableModelEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    null,null);
            // Fire a property change event indicating the table model
            // has changed.
            int firstColumn=e.getColumn();
            int lastColumn=e.getColumn();
            if(firstColumn==TableModelEvent.ALL_COLUMNS){
                firstColumn=0;
                lastColumn=getColumnCount()-1;
            }
            AccessibleJTableModelChange change=
                    new AccessibleJTableModelChange(e.getType(),
                            e.getFirstRow(),
                            e.getLastRow(),
                            firstColumn,
                            lastColumn);
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                    null,change);
        }        public void columnAdded(TableColumnModelEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    null,null);
            // Fire a property change event indicating the table model
            // has changed.
            int type=AccessibleTableModelChange.INSERT;
            AccessibleJTableModelChange change=
                    new AccessibleJTableModelChange(type,
                            0,
                            0,
                            e.getFromIndex(),
                            e.getToIndex());
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                    null,change);
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.TABLE;
        }        public void columnRemoved(TableColumnModelEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    null,null);
            // Fire a property change event indicating the table model
            // has changed.
            int type=AccessibleTableModelChange.DELETE;
            AccessibleJTableModelChange change=
                    new AccessibleJTableModelChange(type,
                            0,
                            0,
                            e.getFromIndex(),
                            e.getToIndex());
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                    null,change);
        }

        public int getAccessibleChildrenCount(){
            return (JTable.this.getColumnCount()*JTable.this.getRowCount());
        }        public void columnMoved(TableColumnModelEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    null,null);
            // Fire property change events indicating the table model
            // has changed.
            int type=AccessibleTableModelChange.DELETE;
            AccessibleJTableModelChange change=
                    new AccessibleJTableModelChange(type,
                            0,
                            0,
                            e.getFromIndex(),
                            e.getFromIndex());
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                    null,change);
            int type2=AccessibleTableModelChange.INSERT;
            AccessibleJTableModelChange change2=
                    new AccessibleJTableModelChange(type2,
                            0,
                            0,
                            e.getToIndex(),
                            e.getToIndex());
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                    null,change2);
        }

        public Accessible getAccessibleChild(int i){
            if(i<0||i>=getAccessibleChildrenCount()){
                return null;
            }else{
                // children increase across, and then down, for tables
                // (arbitrary decision)
                int column=getAccessibleColumnAtIndex(i);
                int row=getAccessibleRowAtIndex(i);
                TableColumn aColumn=getColumnModel().getColumn(column);
                TableCellRenderer renderer=aColumn.getCellRenderer();
                if(renderer==null){
                    Class<?> columnClass=getColumnClass(column);
                    renderer=getDefaultRenderer(columnClass);
                }
                Component component=renderer.getTableCellRendererComponent(
                        JTable.this,null,false,false,
                        row,column);
                return new AccessibleJTableCell(JTable.this,row,column,
                        getAccessibleIndexAt(row,column));
            }
        }        public void columnMarginChanged(ChangeEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    null,null);
        }

        public int getAccessibleRowAtIndex(int i){
            int columnCount=getAccessibleColumnCount();
            if(columnCount==0){
                return -1;
            }else{
                return (i/columnCount);
            }
        }        public void columnSelectionChanged(ListSelectionEvent e){
            // we should now re-place our TableColumn listener
        }

        public int getAccessibleColumnAtIndex(int i){
            int columnCount=getAccessibleColumnCount();
            if(columnCount==0){
                return -1;
            }else{
                return (i%columnCount);
            }
        }        public void editingStopped(ChangeEvent e){
            // it'd be great if we could figure out which cell, and pass that
            // somehow as a parameter
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    null,null);
        }

        public int getAccessibleIndexAt(int r,int c){
            return ((r*getAccessibleColumnCount())+c);
        }        public void editingCanceled(ChangeEvent e){
            // nothing to report, 'cause nothing changed
        }

        public Accessible getAccessibleAt(Point p){
            int column=columnAtPoint(p);
            int row=rowAtPoint(p);
            if((column!=-1)&&(row!=-1)){
                TableColumn aColumn=getColumnModel().getColumn(column);
                TableCellRenderer renderer=aColumn.getCellRenderer();
                if(renderer==null){
                    Class<?> columnClass=getColumnClass(column);
                    renderer=getDefaultRenderer(columnClass);
                }
                Component component=renderer.getTableCellRendererComponent(
                        JTable.this,null,false,false,
                        row,column);
                return new AccessibleJTableCell(JTable.this,row,column,
                        getAccessibleIndexAt(row,column));
            }
            return null;
        }        public void valueChanged(ListSelectionEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
            // Using lead selection index to cover both cases: node selected and node
            // is focused but not selected (Ctrl+up/down)
            int focusedRow=JTable.this.getSelectionModel().getLeadSelectionIndex();
            int focusedCol=JTable.this.getColumnModel().getSelectionModel().
                    getLeadSelectionIndex();
            if(focusedRow!=previousFocusedRow||
                    focusedCol!=previousFocusedCol){
                Accessible oldA=getAccessibleAt(previousFocusedRow,previousFocusedCol);
                Accessible newA=getAccessibleAt(focusedRow,focusedCol);
                firePropertyChange(AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY,
                        oldA,newA);
                previousFocusedRow=focusedRow;
                previousFocusedCol=focusedCol;
            }
        }
        // AccessibleContext support

        public int getAccessibleRow(int index){
            return getAccessibleRowAtIndex(index);
        }        public AccessibleSelection getAccessibleSelection(){
            return this;
        }

        public int getAccessibleColumn(int index){
            return getAccessibleColumnAtIndex(index);
        }

        public int getAccessibleIndex(int r,int c){
            return getAccessibleIndexAt(r,c);
        }

        public Accessible getAccessibleCaption(){
            return this.caption;
        }

        public void setAccessibleCaption(Accessible a){
            Accessible oldCaption=caption;
            this.caption=a;
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_CAPTION_CHANGED,
                    oldCaption,this.caption);
        }
        // AccessibleSelection support

        protected class AccessibleJTableModelChange
                implements AccessibleTableModelChange{
            protected int type;
            protected int firstRow;
            protected int lastRow;
            protected int firstColumn;
            protected int lastColumn;

            protected AccessibleJTableModelChange(int type,int firstRow,
                                                  int lastRow,int firstColumn,
                                                  int lastColumn){
                this.type=type;
                this.firstRow=firstRow;
                this.lastRow=lastRow;
                this.firstColumn=firstColumn;
                this.lastColumn=lastColumn;
            }

            public int getType(){
                return type;
            }

            public int getFirstRow(){
                return firstRow;
            }

            public int getLastRow(){
                return lastRow;
            }

            public int getFirstColumn(){
                return firstColumn;
            }

            public int getLastColumn(){
                return lastColumn;
            }
        }        public int getAccessibleSelectionCount(){
            int rowsSel=JTable.this.getSelectedRowCount();
            int colsSel=JTable.this.getSelectedColumnCount();
            if(JTable.this.cellSelectionEnabled){ // a contiguous block
                return rowsSel*colsSel;
            }else{
                // a column swath and a row swath, with a shared block
                if(JTable.this.getRowSelectionAllowed()&&
                        JTable.this.getColumnSelectionAllowed()){
                    return rowsSel*JTable.this.getColumnCount()+
                            colsSel*JTable.this.getRowCount()-
                            rowsSel*colsSel;
                    // just one or more rows in selection
                }else if(JTable.this.getRowSelectionAllowed()){
                    return rowsSel*JTable.this.getColumnCount();
                    // just one or more rows in selection
                }else if(JTable.this.getColumnSelectionAllowed()){
                    return colsSel*JTable.this.getRowCount();
                }else{
                    return 0;    // JTable doesn't allow selections
                }
            }
        }

        private class AccessibleTableHeader implements AccessibleTable{
            private JTableHeader header;
            private TableColumnModel headerModel;

            AccessibleTableHeader(JTableHeader header){
                this.header=header;
                this.headerModel=header.getColumnModel();
            }

            public Accessible getAccessibleCaption(){
                return null;
            }

            public void setAccessibleCaption(Accessible a){
            }

            public Accessible getAccessibleSummary(){
                return null;
            }

            public void setAccessibleSummary(Accessible a){
            }

            public int getAccessibleRowCount(){
                return 1;
            }

            public int getAccessibleColumnCount(){
                return headerModel.getColumnCount();
            }

            public Accessible getAccessibleAt(int row,int column){
                // TIGER - 4715503
                TableColumn aColumn=headerModel.getColumn(column);
                TableCellRenderer renderer=aColumn.getHeaderRenderer();
                if(renderer==null){
                    renderer=header.getDefaultRenderer();
                }
                Component component=renderer.getTableCellRendererComponent(
                        header.getTable(),
                        aColumn.getHeaderValue(),false,false,
                        -1,column);
                return new AccessibleJTableHeaderCell(row,column,
                        JTable.this.getTableHeader(),
                        component);
            }

            public int getAccessibleRowExtentAt(int r,int c){
                return 1;
            }

            public int getAccessibleColumnExtentAt(int r,int c){
                return 1;
            }

            public AccessibleTable getAccessibleRowHeader(){
                return null;
            }

            public void setAccessibleRowHeader(AccessibleTable table){
            }

            public AccessibleTable getAccessibleColumnHeader(){
                return null;
            }

            public void setAccessibleColumnHeader(AccessibleTable table){
            }

            public Accessible getAccessibleRowDescription(int r){
                return null;
            }

            public void setAccessibleRowDescription(int r,Accessible a){
            }

            public Accessible getAccessibleColumnDescription(int c){
                return null;
            }

            public void setAccessibleColumnDescription(int c,Accessible a){
            }

            public boolean isAccessibleSelected(int r,int c){
                return false;
            }

            public boolean isAccessibleRowSelected(int r){
                return false;
            }

            public boolean isAccessibleColumnSelected(int c){
                return false;
            }

            public int[] getSelectedAccessibleRows(){
                return new int[0];
            }

            public int[] getSelectedAccessibleColumns(){
                return new int[0];
            }
        }        public Accessible getAccessibleSelection(int i){
            if(i<0||i>getAccessibleSelectionCount()){
                return null;
            }
            int rowsSel=JTable.this.getSelectedRowCount();
            int colsSel=JTable.this.getSelectedColumnCount();
            int rowIndicies[]=getSelectedRows();
            int colIndicies[]=getSelectedColumns();
            int ttlCols=JTable.this.getColumnCount();
            int ttlRows=JTable.this.getRowCount();
            int r;
            int c;
            if(JTable.this.cellSelectionEnabled){ // a contiguous block
                r=rowIndicies[i/colsSel];
                c=colIndicies[i%colsSel];
                return getAccessibleChild((r*ttlCols)+c);
            }else{
                // a column swath and a row swath, with a shared block
                if(JTable.this.getRowSelectionAllowed()&&
                        JTable.this.getColumnSelectionAllowed()){
                    // Situation:
                    //   We have a table, like the 6x3 table below,
                    //   wherein three colums and one row selected
                    //   (selected cells marked with "*", unselected "0"):
                    //
                    //            0 * 0 * * 0
                    //            * * * * * *
                    //            0 * 0 * * 0
                    //
                    // State machine below walks through the array of
                    // selected rows in two states: in a selected row,
                    // and not in one; continuing until we are in a row
                    // in which the ith selection exists.  Then we return
                    // the appropriate cell.  In the state machine, we
                    // always do rows above the "current" selected row first,
                    // then the cells in the selected row.  If we're done
                    // with the state machine before finding the requested
                    // selected child, we handle the rows below the last
                    // selected row at the end.
                    //
                    int curIndex=i;
                    final int IN_ROW=0;
                    final int NOT_IN_ROW=1;
                    int state=(rowIndicies[0]==0?IN_ROW:NOT_IN_ROW);
                    int j=0;
                    int prevRow=-1;
                    while(j<rowIndicies.length){
                        switch(state){
                            case IN_ROW:   // on individual row full of selections
                                if(curIndex<ttlCols){ // it's here!
                                    c=curIndex%ttlCols;
                                    r=rowIndicies[j];
                                    return getAccessibleChild((r*ttlCols)+c);
                                }else{                               // not here
                                    curIndex-=ttlCols;
                                }
                                // is the next row in table selected or not?
                                if(j+1==rowIndicies.length||
                                        rowIndicies[j]!=rowIndicies[j+1]-1){
                                    state=NOT_IN_ROW;
                                    prevRow=rowIndicies[j];
                                }
                                j++;  // we didn't return earlier, so go to next row
                                break;
                            case NOT_IN_ROW:  // sparse bunch of rows of selections
                                if(curIndex<
                                        (colsSel*(rowIndicies[j]-
                                                (prevRow==-1?0:(prevRow+1))))){
                                    // it's here!
                                    c=colIndicies[curIndex%colsSel];
                                    r=(j>0?rowIndicies[j-1]+1:0)
                                            +curIndex/colsSel;
                                    return getAccessibleChild((r*ttlCols)+c);
                                }else{                               // not here
                                    curIndex-=colsSel*(rowIndicies[j]-
                                            (prevRow==-1?0:(prevRow+1)));
                                }
                                state=IN_ROW;
                                break;
                        }
                    }
                    // we got here, so we didn't find it yet; find it in
                    // the last sparse bunch of rows
                    if(curIndex<
                            (colsSel*(ttlRows-
                                    (prevRow==-1?0:(prevRow+1))))){ // it's here!
                        c=colIndicies[curIndex%colsSel];
                        r=rowIndicies[j-1]+curIndex/colsSel+1;
                        return getAccessibleChild((r*ttlCols)+c);
                    }else{                               // not here
                        // we shouldn't get to this spot in the code!
//                      System.out.println("Bug in AccessibleJTable.getAccessibleSelection()");
                    }
                    // one or more rows selected
                }else if(JTable.this.getRowSelectionAllowed()){
                    c=i%ttlCols;
                    r=rowIndicies[i/ttlCols];
                    return getAccessibleChild((r*ttlCols)+c);
                    // one or more columns selected
                }else if(JTable.this.getColumnSelectionAllowed()){
                    c=colIndicies[i%colsSel];
                    r=i/colsSel;
                    return getAccessibleChild((r*ttlCols)+c);
                }
            }
            return null;
        }

        protected class AccessibleJTableCell extends AccessibleContext
                implements Accessible, AccessibleComponent{
            private JTable parent;
            private int row;
            private int column;
            private int index;

            public AccessibleJTableCell(JTable t,int r,int c,int i){
                parent=t;
                row=r;
                column=c;
                index=i;
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
                    return (String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
                }
            }

            protected AccessibleContext getCurrentAccessibleContext(){
                TableColumn aColumn=getColumnModel().getColumn(column);
                TableCellRenderer renderer=aColumn.getCellRenderer();
                if(renderer==null){
                    Class<?> columnClass=getColumnClass(column);
                    renderer=getDefaultRenderer(columnClass);
                }
                Component component=renderer.getTableCellRendererComponent(
                        JTable.this,getValueAt(row,column),
                        false,false,row,column);
                if(component instanceof Accessible){
                    return component.getAccessibleContext();
                }else{
                    return null;
                }
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

            protected Component getCurrentComponent(){
                TableColumn aColumn=getColumnModel().getColumn(column);
                TableCellRenderer renderer=aColumn.getCellRenderer();
                if(renderer==null){
                    Class<?> columnClass=getColumnClass(column);
                    renderer=getDefaultRenderer(columnClass);
                }
                return renderer.getTableCellRendererComponent(
                        JTable.this,null,false,false,
                        row,column);
            }            //
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
            }            public void setAccessibleDescription(String s){
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
                    return AccessibleRole.UNKNOWN;
                }
            }

            public AccessibleStateSet getAccessibleStateSet(){
                AccessibleContext ac=getCurrentAccessibleContext();
                AccessibleStateSet as=null;
                if(ac!=null){
                    as=ac.getAccessibleStateSet();
                }
                if(as==null){
                    as=new AccessibleStateSet();
                }
                Rectangle rjt=JTable.this.getVisibleRect();
                Rectangle rcell=JTable.this.getCellRect(row,column,false);
                if(rjt.intersects(rcell)){
                    as.add(AccessibleState.SHOWING);
                }else{
                    if(as.contains(AccessibleState.SHOWING)){
                        as.remove(AccessibleState.SHOWING);
                    }
                }
                if(parent.isCellSelected(row,column)){
                    as.add(AccessibleState.SELECTED);
                }else if(as.contains(AccessibleState.SELECTED)){
                    as.remove(AccessibleState.SELECTED);
                }
                if((row==getSelectedRow())&&(column==getSelectedColumn())){
                    as.add(AccessibleState.ACTIVE);
                }
                as.add(AccessibleState.TRANSIENT);
                return as;
            }

            public Accessible getAccessibleParent(){
                return parent;
            }

            public int getAccessibleIndexInParent(){
                return index;
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
            // AccessibleComponent methods





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
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    if(ac.getAccessibleParent()!=null){
                        return ((AccessibleComponent)ac).isShowing();
                    }else{
                        // Fixes 4529616 - AccessibleJTableCell.isShowing()
                        // returns false when the cell on the screen
                        // if no parent
                        return isVisible();
                    }
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.isShowing();
                    }else{
                        return false;
                    }
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
                if(parent!=null&&parent.isShowing()){
                    Point parentLocation=parent.getLocationOnScreen();
                    Point componentLocation=getLocation();
                    componentLocation.translate(parentLocation.x,parentLocation.y);
                    return componentLocation;
                }else{
                    return null;
                }
            }

            public Point getLocation(){
                if(parent!=null){
                    Rectangle r=parent.getCellRect(row,column,false);
                    if(r!=null){
                        return r.getLocation();
                    }
                }
                return null;
            }

            public void setLocation(Point p){
//              if ((parent != null)  && (parent.contains(p))) {
//                  ensureIndexIsVisible(indexInParent);
//              }
            }

            public Rectangle getBounds(){
                if(parent!=null){
                    return parent.getCellRect(row,column,false);
                }else{
                    return null;
                }
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
                if(parent!=null){
                    Rectangle r=parent.getCellRect(row,column,false);
                    if(r!=null){
                        return r.getSize();
                    }
                }
                return null;
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
        } // inner class AccessibleJTableCell        public boolean isAccessibleChildSelected(int i){
            int column=getAccessibleColumnAtIndex(i);
            int row=getAccessibleRowAtIndex(i);
            return JTable.this.isCellSelected(row,column);
        }

        private class AccessibleJTableHeaderCell extends AccessibleContext
                implements Accessible, AccessibleComponent{
            private int row;
            private int column;
            private JTableHeader parent;
            private Component rendererComponent;

            public AccessibleJTableHeaderCell(int row,int column,
                                              JTableHeader parent,
                                              Component rendererComponent){
                this.row=row;
                this.column=column;
                this.parent=parent;
                this.rendererComponent=rendererComponent;
                this.setAccessibleParent(parent);
            }

            public AccessibleContext getAccessibleContext(){
                return this;
            }

            private AccessibleContext getCurrentAccessibleContext(){
                return rendererComponent.getAccessibleContext();
            }

            private Component getCurrentComponent(){
                return rendererComponent;
            }
            // AccessibleContext methods ==========

            public String getAccessibleName(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    String name=ac.getAccessibleName();
                    if((name!=null)&&(name!="")){
                        return ac.getAccessibleName();
                    }
                }
                if((accessibleName!=null)&&(accessibleName!="")){
                    return accessibleName;
                }else{
                    return null;
                }
            }

            public void setAccessibleName(String s){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    ac.setAccessibleName(s);
                }else{
                    super.setAccessibleName(s);
                }
            }

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
                    return AccessibleRole.UNKNOWN;
                }
            }

            public AccessibleStateSet getAccessibleStateSet(){
                AccessibleContext ac=getCurrentAccessibleContext();
                AccessibleStateSet as=null;
                if(ac!=null){
                    as=ac.getAccessibleStateSet();
                }
                if(as==null){
                    as=new AccessibleStateSet();
                }
                Rectangle rjt=JTable.this.getVisibleRect();
                Rectangle rcell=JTable.this.getCellRect(row,column,false);
                if(rjt.intersects(rcell)){
                    as.add(AccessibleState.SHOWING);
                }else{
                    if(as.contains(AccessibleState.SHOWING)){
                        as.remove(AccessibleState.SHOWING);
                    }
                }
                if(JTable.this.isCellSelected(row,column)){
                    as.add(AccessibleState.SELECTED);
                }else if(as.contains(AccessibleState.SELECTED)){
                    as.remove(AccessibleState.SELECTED);
                }
                if((row==getSelectedRow())&&(column==getSelectedColumn())){
                    as.add(AccessibleState.ACTIVE);
                }
                as.add(AccessibleState.TRANSIENT);
                return as;
            }

            public Accessible getAccessibleParent(){
                return parent;
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
            // AccessibleComponent methods ==========

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
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    if(ac.getAccessibleParent()!=null){
                        return ((AccessibleComponent)ac).isShowing();
                    }else{
                        // Fixes 4529616 - AccessibleJTableCell.isShowing()
                        // returns false when the cell on the screen
                        // if no parent
                        return isVisible();
                    }
                }else{
                    Component c=getCurrentComponent();
                    if(c!=null){
                        return c.isShowing();
                    }else{
                        return false;
                    }
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
                if(parent!=null&&parent.isShowing()){
                    Point parentLocation=parent.getLocationOnScreen();
                    Point componentLocation=getLocation();
                    componentLocation.translate(parentLocation.x,parentLocation.y);
                    return componentLocation;
                }else{
                    return null;
                }
            }

            public Point getLocation(){
                if(parent!=null){
                    Rectangle r=parent.getHeaderRect(column);
                    if(r!=null){
                        return r.getLocation();
                    }
                }
                return null;
            }

            public void setLocation(Point p){
            }

            public Rectangle getBounds(){
                if(parent!=null){
                    return parent.getHeaderRect(column);
                }else{
                    return null;
                }
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
                if(parent!=null){
                    Rectangle r=parent.getHeaderRect(column);
                    if(r!=null){
                        return r.getSize();
                    }
                }
                return null;
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
        } // inner class AccessibleJTableHeaderCell        public void addAccessibleSelection(int i){
            // TIGER - 4495286
            int column=getAccessibleColumnAtIndex(i);
            int row=getAccessibleRowAtIndex(i);
            JTable.this.changeSelection(row,column,true,false);
        }

        public void removeAccessibleSelection(int i){
            if(JTable.this.cellSelectionEnabled){
                int column=getAccessibleColumnAtIndex(i);
                int row=getAccessibleRowAtIndex(i);
                JTable.this.removeRowSelectionInterval(row,row);
                JTable.this.removeColumnSelectionInterval(column,column);
            }
        }

        public void clearAccessibleSelection(){
            JTable.this.clearSelection();
        }

        public void selectAllAccessibleSelection(){
            if(JTable.this.cellSelectionEnabled){
                JTable.this.selectAll();
            }
        }
        // begin AccessibleExtendedTable implementation -------------











        public AccessibleTable getAccessibleTable(){
            return this;
        }





        public Accessible getAccessibleSummary(){
            return this.summary;
        }

        public void setAccessibleSummary(Accessible a){
            Accessible oldSummary=summary;
            this.summary=a;
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_SUMMARY_CHANGED,
                    oldSummary,this.summary);
        }

        public int getAccessibleRowCount(){
            return JTable.this.getRowCount();
        }

        public int getAccessibleColumnCount(){
            return JTable.this.getColumnCount();
        }

        public Accessible getAccessibleAt(int r,int c){
            return getAccessibleChild((r*getAccessibleColumnCount())+c);
        }

        public int getAccessibleRowExtentAt(int r,int c){
            return 1;
        }

        public int getAccessibleColumnExtentAt(int r,int c){
            return 1;
        }

        public AccessibleTable getAccessibleRowHeader(){
            // row headers are not supported
            return null;
        }

        public void setAccessibleRowHeader(AccessibleTable a){
            // row headers are not supported
        }

        public AccessibleTable getAccessibleColumnHeader(){
            JTableHeader header=JTable.this.getTableHeader();
            return header==null?null:new AccessibleTableHeader(header);
        }



        public void setAccessibleColumnHeader(AccessibleTable a){
            // XXX not implemented
        }

        public Accessible getAccessibleRowDescription(int r){
            if(r<0||r>=getAccessibleRowCount()){
                throw new IllegalArgumentException(Integer.toString(r));
            }
            if(rowDescription==null){
                return null;
            }else{
                return rowDescription[r];
            }
        }

        public void setAccessibleRowDescription(int r,Accessible a){
            if(r<0||r>=getAccessibleRowCount()){
                throw new IllegalArgumentException(Integer.toString(r));
            }
            if(rowDescription==null){
                int numRows=getAccessibleRowCount();
                rowDescription=new Accessible[numRows];
            }
            rowDescription[r]=a;
        }

        public Accessible getAccessibleColumnDescription(int c){
            if(c<0||c>=getAccessibleColumnCount()){
                throw new IllegalArgumentException(Integer.toString(c));
            }
            if(columnDescription==null){
                return null;
            }else{
                return columnDescription[c];
            }
        }

        public void setAccessibleColumnDescription(int c,Accessible a){
            if(c<0||c>=getAccessibleColumnCount()){
                throw new IllegalArgumentException(Integer.toString(c));
            }
            if(columnDescription==null){
                int numColumns=getAccessibleColumnCount();
                columnDescription=new Accessible[numColumns];
            }
            columnDescription[c]=a;
        }

        public boolean isAccessibleSelected(int r,int c){
            return JTable.this.isCellSelected(r,c);
        }

        public boolean isAccessibleRowSelected(int r){
            return JTable.this.isRowSelected(r);
        }

        public boolean isAccessibleColumnSelected(int c){
            return JTable.this.isColumnSelected(c);
        }

        public int[] getSelectedAccessibleRows(){
            return JTable.this.getSelectedRows();
        }

        public int[] getSelectedAccessibleColumns(){
            return JTable.this.getSelectedColumns();
        }






        // end of AccessibleTable implementation --------------------


        // Begin AccessibleJTableHeader ========== // TIGER - 4715503


    }  // inner class AccessibleJTable








/////////////////
// Accessibility support
////////////////




}  // End of Class JTable
