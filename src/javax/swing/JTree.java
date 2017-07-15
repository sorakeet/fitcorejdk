/**
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.swing.SwingUtilities2;
import sun.swing.SwingUtilities2.Section;

import javax.accessibility.*;
import javax.swing.event.*;
import javax.swing.plaf.TreeUI;
import javax.swing.text.Position;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.ConstructorProperties;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import static sun.swing.SwingUtilities2.Section.LEADING;
import static sun.swing.SwingUtilities2.Section.TRAILING;

@SuppressWarnings("serial")
public class JTree extends JComponent implements Scrollable, Accessible{
    //
    // Bound property names
    //
    public final static String CELL_RENDERER_PROPERTY="cellRenderer";
    public final static String TREE_MODEL_PROPERTY="model";
    public final static String ROOT_VISIBLE_PROPERTY="rootVisible";
    public final static String SHOWS_ROOT_HANDLES_PROPERTY="showsRootHandles";
    public final static String ROW_HEIGHT_PROPERTY="rowHeight";
    public final static String CELL_EDITOR_PROPERTY="cellEditor";
    public final static String EDITABLE_PROPERTY="editable";
    public final static String LARGE_MODEL_PROPERTY="largeModel";
    public final static String SELECTION_MODEL_PROPERTY="selectionModel";
    public final static String VISIBLE_ROW_COUNT_PROPERTY="visibleRowCount";
    public final static String INVOKES_STOP_CELL_EDITING_PROPERTY="invokesStopCellEditing";
    public final static String SCROLLS_ON_EXPAND_PROPERTY="scrollsOnExpand";
    public final static String TOGGLE_CLICK_COUNT_PROPERTY="toggleClickCount";
    public final static String LEAD_SELECTION_PATH_PROPERTY="leadSelectionPath";
    public final static String ANCHOR_SELECTION_PATH_PROPERTY="anchorSelectionPath";
    public final static String EXPANDS_SELECTED_PATHS_PROPERTY="expandsSelectedPaths";
    private static final String uiClassID="TreeUI";
    private static int TEMP_STACK_SIZE=11;
    transient protected TreeModel treeModel;
    transient protected TreeSelectionModel selectionModel;
    protected boolean rootVisible;
    transient protected TreeCellRenderer cellRenderer;
    protected int rowHeight;
    protected boolean showsRootHandles;
    protected transient TreeSelectionRedirector selectionRedirector;
    transient protected TreeCellEditor cellEditor;
    protected boolean editable;
    protected boolean largeModel;
    protected int visibleRowCount;
    protected boolean invokesStopCellEditing;
    protected boolean scrollsOnExpand;
    protected int toggleClickCount;
    transient protected TreeModelListener treeModelListener;
    private boolean rowHeightSet=false;
    transient private Hashtable<TreePath,Boolean> expandedState;
    private boolean showsRootHandlesSet=false;
    private boolean scrollsOnExpandSet=false;
    transient private Stack<Stack<TreePath>> expandedStack;
    private TreePath leadPath;
    private TreePath anchorPath;
    private boolean expandsSelectedPaths;
    private boolean settingUI;
    private boolean dragEnabled;
    private DropMode dropMode=DropMode.USE_SELECTION;
    private transient DropLocation dropLocation;
    private int expandRow=-1;
    private TreeTimer dropTimer;
    private transient TreeExpansionListener uiTreeExpansionListener;
    public JTree(){
        this(getDefaultTreeModel());
    }

    protected static TreeModel getDefaultTreeModel(){
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("JTree");
        DefaultMutableTreeNode parent;
        parent=new DefaultMutableTreeNode("colors");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("blue"));
        parent.add(new DefaultMutableTreeNode("violet"));
        parent.add(new DefaultMutableTreeNode("red"));
        parent.add(new DefaultMutableTreeNode("yellow"));
        parent=new DefaultMutableTreeNode("sports");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("basketball"));
        parent.add(new DefaultMutableTreeNode("soccer"));
        parent.add(new DefaultMutableTreeNode("football"));
        parent.add(new DefaultMutableTreeNode("hockey"));
        parent=new DefaultMutableTreeNode("food");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("hot dogs"));
        parent.add(new DefaultMutableTreeNode("pizza"));
        parent.add(new DefaultMutableTreeNode("ravioli"));
        parent.add(new DefaultMutableTreeNode("bananas"));
        return new DefaultTreeModel(root);
    }

    @ConstructorProperties({"model"})
    public JTree(TreeModel newModel){
        super();
        expandedStack=new Stack<Stack<TreePath>>();
        toggleClickCount=2;
        expandedState=new Hashtable<TreePath,Boolean>();
        setLayout(null);
        rowHeight=16;
        visibleRowCount=20;
        rootVisible=true;
        selectionModel=new DefaultTreeSelectionModel();
        cellRenderer=null;
        scrollsOnExpand=true;
        setOpaque(true);
        expandsSelectedPaths=true;
        updateUI();
        setModel(newModel);
    }

    public void updateUI(){
        setUI((TreeUI)UIManager.getUI(this));
        SwingUtilities.updateRendererOrEditorUI(getCellRenderer());
        SwingUtilities.updateRendererOrEditorUI(getCellEditor());
    }

    public String getUIClassID(){
        return uiClassID;
    }

    public String getToolTipText(MouseEvent event){
        String tip=null;
        if(event!=null){
            Point p=event.getPoint();
            int selRow=getRowForLocation(p.x,p.y);
            TreeCellRenderer r=getCellRenderer();
            if(selRow!=-1&&r!=null){
                TreePath path=getPathForRow(selRow);
                Object lastPath=path.getLastPathComponent();
                Component rComponent=r.getTreeCellRendererComponent
                        (this,lastPath,isRowSelected(selRow),
                                isExpanded(selRow),getModel().isLeaf(lastPath),selRow,
                                true);
                if(rComponent instanceof JComponent){
                    MouseEvent newEvent;
                    Rectangle pathBounds=getPathBounds(path);
                    p.translate(-pathBounds.x,-pathBounds.y);
                    newEvent=new MouseEvent(rComponent,event.getID(),
                            event.getWhen(),
                            event.getModifiers(),
                            p.x,p.y,
                            event.getXOnScreen(),
                            event.getYOnScreen(),
                            event.getClickCount(),
                            event.isPopupTrigger(),
                            MouseEvent.NOBUTTON);
                    tip=((JComponent)rComponent).getToolTipText(newEvent);
                }
            }
        }
        // No tip from the renderer get our own tip
        if(tip==null){
            tip=getToolTipText();
        }
        return tip;
    }

    DropLocation dropLocationForPoint(Point p){
        DropLocation location=null;
        int row=getClosestRowForLocation(p.x,p.y);
        Rectangle bounds=getRowBounds(row);
        TreeModel model=getModel();
        Object root=(model==null)?null:model.getRoot();
        TreePath rootPath=(root==null)?null:new TreePath(root);
        TreePath child;
        TreePath parent;
        boolean outside=row==-1
                ||p.y<bounds.y
                ||p.y>=bounds.y+bounds.height;
        switch(dropMode){
            case USE_SELECTION:
            case ON:
                if(outside){
                    location=new DropLocation(p,null,-1);
                }else{
                    location=new DropLocation(p,getPathForRow(row),-1);
                }
                break;
            case INSERT:
            case ON_OR_INSERT:
                if(row==-1){
                    if(root!=null&&!model.isLeaf(root)&&isExpanded(rootPath)){
                        location=new DropLocation(p,rootPath,0);
                    }else{
                        location=new DropLocation(p,null,-1);
                    }
                    break;
                }
                boolean checkOn=dropMode==DropMode.ON_OR_INSERT
                        ||!model.isLeaf(getPathForRow(row).getLastPathComponent());
                Section section=SwingUtilities2.liesInVertical(bounds,p,checkOn);
                if(section==LEADING){
                    child=getPathForRow(row);
                    parent=child.getParentPath();
                }else if(section==TRAILING){
                    int index=row+1;
                    if(index>=getRowCount()){
                        if(model.isLeaf(root)||!isExpanded(rootPath)){
                            location=new DropLocation(p,null,-1);
                        }else{
                            parent=rootPath;
                            index=model.getChildCount(root);
                            location=new DropLocation(p,parent,index);
                        }
                        break;
                    }
                    child=getPathForRow(index);
                    parent=child.getParentPath();
                }else{
                    assert checkOn;
                    location=new DropLocation(p,getPathForRow(row),-1);
                    break;
                }
                if(parent!=null){
                    location=new DropLocation(p,parent,
                            model.getIndexOfChild(parent.getLastPathComponent(),
                                    child.getLastPathComponent()));
                }else if(checkOn||!model.isLeaf(root)){
                    location=new DropLocation(p,rootPath,-1);
                }else{
                    location=new DropLocation(p,null,-1);
                }
                break;
            default:
                assert false:"Unexpected drop mode";
        }
        if(outside||row!=expandRow){
            cancelDropTimer();
        }
        if(!outside&&row!=expandRow){
            if(isCollapsed(row)){
                expandRow=row;
                startDropTimer();
            }
        }
        return location;
    }

    Object setDropLocation(TransferHandler.DropLocation location,
                           Object state,
                           boolean forDrop){
        Object retVal=null;
        DropLocation treeLocation=(DropLocation)location;
        if(dropMode==DropMode.USE_SELECTION){
            if(treeLocation==null){
                if(!forDrop&&state!=null){
                    setSelectionPaths(((TreePath[][])state)[0]);
                    setAnchorSelectionPath(((TreePath[][])state)[1][0]);
                    setLeadSelectionPath(((TreePath[][])state)[1][1]);
                }
            }else{
                if(dropLocation==null){
                    TreePath[] paths=getSelectionPaths();
                    if(paths==null){
                        paths=new TreePath[0];
                    }
                    retVal=new TreePath[][]{paths,
                            {getAnchorSelectionPath(),getLeadSelectionPath()}};
                }else{
                    retVal=state;
                }
                setSelectionPath(treeLocation.getPath());
            }
        }
        DropLocation old=dropLocation;
        dropLocation=treeLocation;
        firePropertyChange("dropLocation",old,dropLocation);
        return retVal;
    }

    void dndDone(){
        cancelDropTimer();
        dropTimer=null;
    }

    private void cancelDropTimer(){
        if(dropTimer!=null&&dropTimer.isRunning()){
            expandRow=-1;
            dropTimer.stop();
        }
    }

    void setUIProperty(String propertyName,Object value){
        if(propertyName=="rowHeight"){
            if(!rowHeightSet){
                setRowHeight(((Number)value).intValue());
                rowHeightSet=false;
            }
        }else if(propertyName=="scrollsOnExpand"){
            if(!scrollsOnExpandSet){
                setScrollsOnExpand(((Boolean)value).booleanValue());
                scrollsOnExpandSet=false;
            }
        }else if(propertyName=="showsRootHandles"){
            if(!showsRootHandlesSet){
                setShowsRootHandles(((Boolean)value).booleanValue());
                showsRootHandlesSet=false;
            }
        }else{
            super.setUIProperty(propertyName,value);
        }
    }

    protected String paramString(){
        String rootVisibleString=(rootVisible?
                "true":"false");
        String showsRootHandlesString=(showsRootHandles?
                "true":"false");
        String editableString=(editable?
                "true":"false");
        String largeModelString=(largeModel?
                "true":"false");
        String invokesStopCellEditingString=(invokesStopCellEditing?
                "true":"false");
        String scrollsOnExpandString=(scrollsOnExpand?
                "true":"false");
        return super.paramString()+
                ",editable="+editableString+
                ",invokesStopCellEditing="+invokesStopCellEditingString+
                ",largeModel="+largeModelString+
                ",rootVisible="+rootVisibleString+
                ",rowHeight="+rowHeight+
                ",scrollsOnExpand="+scrollsOnExpandString+
                ",showsRootHandles="+showsRootHandlesString+
                ",toggleClickCount="+toggleClickCount+
                ",visibleRowCount="+visibleRowCount;
    }

    public TreePath getLeadSelectionPath(){
        return leadPath;
    }

    public void setLeadSelectionPath(TreePath newPath){
        TreePath oldValue=leadPath;
        leadPath=newPath;
        firePropertyChange(LEAD_SELECTION_PATH_PROPERTY,oldValue,newPath);
        if(accessibleContext!=null){
            ((AccessibleJTree)accessibleContext).
                    fireActiveDescendantPropertyChange(oldValue,newPath);
        }
    }

    public TreePath getAnchorSelectionPath(){
        return anchorPath;
    }

    public void setAnchorSelectionPath(TreePath newPath){
        TreePath oldValue=anchorPath;
        anchorPath=newPath;
        firePropertyChange(ANCHOR_SELECTION_PATH_PROPERTY,oldValue,newPath);
    }

    public TreePath[] getSelectionPaths(){
        TreePath[] selectionPaths=getSelectionModel().getSelectionPaths();
        return (selectionPaths!=null&&selectionPaths.length>0)?selectionPaths:null;
    }

    public void setSelectionPaths(TreePath[] paths){
        getSelectionModel().setSelectionPaths(paths);
    }

    public TreeCellRenderer getCellRenderer(){
        return cellRenderer;
    }

    public void setCellRenderer(TreeCellRenderer x){
        TreeCellRenderer oldValue=cellRenderer;
        cellRenderer=x;
        firePropertyChange(CELL_RENDERER_PROPERTY,oldValue,cellRenderer);
        invalidate();
    }

    public TreeCellEditor getCellEditor(){
        return cellEditor;
    }

    public void setCellEditor(TreeCellEditor cellEditor){
        TreeCellEditor oldEditor=this.cellEditor;
        this.cellEditor=cellEditor;
        firePropertyChange(CELL_EDITOR_PROPERTY,oldEditor,cellEditor);
        invalidate();
    }

    public JTree(Object[] value){
        this(createTreeModel(value));
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        expandRoot();
    }

    protected static TreeModel createTreeModel(Object value){
        DefaultMutableTreeNode root;
        if((value instanceof Object[])||(value instanceof Hashtable)||
                (value instanceof Vector)){
            root=new DefaultMutableTreeNode("root");
            DynamicUtilTreeNode.createChildren(root,value);
        }else{
            root=new DynamicUtilTreeNode("root",value);
        }
        return new DefaultTreeModel(root,false);
    }

    private void expandRoot(){
        TreeModel model=getModel();
        if(model!=null&&model.getRoot()!=null){
            expandPath(new TreePath(model.getRoot()));
        }
    }

    public void expandPath(TreePath path){
        // Only expand if not leaf!
        TreeModel model=getModel();
        if(path!=null&&model!=null&&
                !model.isLeaf(path.getLastPathComponent())){
            setExpandedState(path,true);
        }
    }

    protected void setExpandedState(TreePath path,boolean state){
        if(path!=null){
            // Make sure all parents of path are expanded.
            Stack<TreePath> stack;
            TreePath parentPath=path.getParentPath();
            if(expandedStack.size()==0){
                stack=new Stack<TreePath>();
            }else{
                stack=expandedStack.pop();
            }
            try{
                while(parentPath!=null){
                    if(isExpanded(parentPath)){
                        parentPath=null;
                    }else{
                        stack.push(parentPath);
                        parentPath=parentPath.getParentPath();
                    }
                }
                for(int counter=stack.size()-1;counter>=0;counter--){
                    parentPath=stack.pop();
                    if(!isExpanded(parentPath)){
                        try{
                            fireTreeWillExpand(parentPath);
                        }catch(ExpandVetoException eve){
                            // Expand vetoed!
                            return;
                        }
                        expandedState.put(parentPath,Boolean.TRUE);
                        fireTreeExpanded(parentPath);
                        if(accessibleContext!=null){
                            ((AccessibleJTree)accessibleContext).
                                    fireVisibleDataPropertyChange();
                        }
                    }
                }
            }finally{
                if(expandedStack.size()<TEMP_STACK_SIZE){
                    stack.removeAllElements();
                    expandedStack.push(stack);
                }
            }
            if(!state){
                // collapse last path.
                Object cValue=expandedState.get(path);
                if(cValue!=null&&((Boolean)cValue).booleanValue()){
                    try{
                        fireTreeWillCollapse(path);
                    }catch(ExpandVetoException eve){
                        return;
                    }
                    expandedState.put(path,Boolean.FALSE);
                    fireTreeCollapsed(path);
                    if(removeDescendantSelectedPaths(path,false)&&
                            !isPathSelected(path)){
                        // A descendant was selected, select the parent.
                        addSelectionPath(path);
                    }
                    if(accessibleContext!=null){
                        ((AccessibleJTree)accessibleContext).
                                fireVisibleDataPropertyChange();
                    }
                }
            }else{
                // Expand last path.
                Object cValue=expandedState.get(path);
                if(cValue==null||!((Boolean)cValue).booleanValue()){
                    try{
                        fireTreeWillExpand(path);
                    }catch(ExpandVetoException eve){
                        return;
                    }
                    expandedState.put(path,Boolean.TRUE);
                    fireTreeExpanded(path);
                    if(accessibleContext!=null){
                        ((AccessibleJTree)accessibleContext).
                                fireVisibleDataPropertyChange();
                    }
                }
            }
        }
    }

    public void addSelectionPath(TreePath path){
        getSelectionModel().addSelectionPath(path);
    }

    public boolean isPathSelected(TreePath path){
        return getSelectionModel().isPathSelected(path);
    }

    public boolean isExpanded(TreePath path){
        if(path==null)
            return false;
        Object value;
        do{
            value=expandedState.get(path);
            if(value==null||!((Boolean)value).booleanValue())
                return false;
        }while((path=path.getParentPath())!=null);
        return true;
    }

    public void fireTreeExpanded(TreePath path){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        TreeExpansionEvent e=null;
        if(uiTreeExpansionListener!=null){
            e=new TreeExpansionEvent(this,path);
            uiTreeExpansionListener.treeExpanded(e);
        }
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TreeExpansionListener.class&&
                    listeners[i+1]!=uiTreeExpansionListener){
                // Lazily create the event:
                if(e==null)
                    e=new TreeExpansionEvent(this,path);
                ((TreeExpansionListener)listeners[i+1]).
                        treeExpanded(e);
            }
        }
    }

    public void fireTreeCollapsed(TreePath path){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        TreeExpansionEvent e=null;
        if(uiTreeExpansionListener!=null){
            e=new TreeExpansionEvent(this,path);
            uiTreeExpansionListener.treeCollapsed(e);
        }
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TreeExpansionListener.class&&
                    listeners[i+1]!=uiTreeExpansionListener){
                // Lazily create the event:
                if(e==null)
                    e=new TreeExpansionEvent(this,path);
                ((TreeExpansionListener)listeners[i+1]).
                        treeCollapsed(e);
            }
        }
    }

    public void fireTreeWillExpand(TreePath path) throws ExpandVetoException{
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        TreeExpansionEvent e=null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TreeWillExpandListener.class){
                // Lazily create the event:
                if(e==null)
                    e=new TreeExpansionEvent(this,path);
                ((TreeWillExpandListener)listeners[i+1]).
                        treeWillExpand(e);
            }
        }
    }

    public void fireTreeWillCollapse(TreePath path) throws ExpandVetoException{
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        TreeExpansionEvent e=null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TreeWillExpandListener.class){
                // Lazily create the event:
                if(e==null)
                    e=new TreeExpansionEvent(this,path);
                ((TreeWillExpandListener)listeners[i+1]).
                        treeWillCollapse(e);
            }
        }
    }

    protected boolean removeDescendantSelectedPaths(TreePath path,
                                                    boolean includePath){
        TreePath[] toRemove=getDescendantSelectedPaths(path,includePath);
        if(toRemove!=null){
            getSelectionModel().removeSelectionPaths(toRemove);
            return true;
        }
        return false;
    }

    private TreePath[] getDescendantSelectedPaths(TreePath path,
                                                  boolean includePath){
        TreeSelectionModel sm=getSelectionModel();
        TreePath[] selPaths=(sm!=null)?sm.getSelectionPaths():
                null;
        if(selPaths!=null){
            boolean shouldRemove=false;
            for(int counter=selPaths.length-1;counter>=0;counter--){
                if(selPaths[counter]!=null&&
                        path.isDescendant(selPaths[counter])&&
                        (!path.equals(selPaths[counter])||includePath))
                    shouldRemove=true;
                else
                    selPaths[counter]=null;
            }
            if(!shouldRemove){
                selPaths=null;
            }
            return selPaths;
        }
        return null;
    }

    public JTree(Vector<?> value){
        this(createTreeModel(value));
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        expandRoot();
    }

    public JTree(Hashtable<?,?> value){
        this(createTreeModel(value));
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        expandRoot();
    }

    public JTree(TreeNode root){
        this(root,false);
    }

    public JTree(TreeNode root,boolean asksAllowsChildren){
        this(new DefaultTreeModel(root,asksAllowsChildren));
    }

    public boolean isRootVisible(){
        return rootVisible;
    }

    public void setRootVisible(boolean rootVisible){
        boolean oldValue=this.rootVisible;
        this.rootVisible=rootVisible;
        firePropertyChange(ROOT_VISIBLE_PROPERTY,oldValue,this.rootVisible);
        if(accessibleContext!=null){
            ((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
        }
    }

    public boolean getShowsRootHandles(){
        return showsRootHandles;
    }

    public void setShowsRootHandles(boolean newValue){
        boolean oldValue=showsRootHandles;
        TreeModel model=getModel();
        showsRootHandles=newValue;
        showsRootHandlesSet=true;
        firePropertyChange(SHOWS_ROOT_HANDLES_PROPERTY,oldValue,
                showsRootHandles);
        if(accessibleContext!=null){
            ((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
        }
        invalidate();
    }

    public TreeModel getModel(){
        return treeModel;
    }

    public void setModel(TreeModel newModel){
        clearSelection();
        TreeModel oldModel=treeModel;
        if(treeModel!=null&&treeModelListener!=null)
            treeModel.removeTreeModelListener(treeModelListener);
        if(accessibleContext!=null){
            if(treeModel!=null){
                treeModel.removeTreeModelListener((TreeModelListener)accessibleContext);
            }
            if(newModel!=null){
                newModel.addTreeModelListener((TreeModelListener)accessibleContext);
            }
        }
        treeModel=newModel;
        clearToggledPaths();
        if(treeModel!=null){
            if(treeModelListener==null)
                treeModelListener=createTreeModelListener();
            if(treeModelListener!=null)
                treeModel.addTreeModelListener(treeModelListener);
            // Mark the root as expanded, if it isn't a leaf.
            Object treeRoot=treeModel.getRoot();
            if(treeRoot!=null&&
                    !treeModel.isLeaf(treeRoot)){
                expandedState.put(new TreePath(treeRoot),
                        Boolean.TRUE);
            }
        }
        firePropertyChange(TREE_MODEL_PROPERTY,oldModel,treeModel);
        invalidate();
    }

    public void clearSelection(){
        getSelectionModel().clearSelection();
    }

    public TreeSelectionModel getSelectionModel(){
        return selectionModel;
    }

    public void setSelectionModel(TreeSelectionModel selectionModel){
        if(selectionModel==null)
            selectionModel=EmptySelectionModel.sharedInstance();
        TreeSelectionModel oldValue=this.selectionModel;
        if(this.selectionModel!=null&&selectionRedirector!=null){
            this.selectionModel.removeTreeSelectionListener
                    (selectionRedirector);
        }
        if(accessibleContext!=null){
            this.selectionModel.removeTreeSelectionListener((TreeSelectionListener)accessibleContext);
            selectionModel.addTreeSelectionListener((TreeSelectionListener)accessibleContext);
        }
        this.selectionModel=selectionModel;
        if(selectionRedirector!=null){
            this.selectionModel.addTreeSelectionListener(selectionRedirector);
        }
        firePropertyChange(SELECTION_MODEL_PROPERTY,oldValue,
                this.selectionModel);
        if(accessibleContext!=null){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
        }
    }

    protected void clearToggledPaths(){
        expandedState.clear();
    }

    protected TreeModelListener createTreeModelListener(){
        return new TreeModelHandler();
    }

    public boolean isLargeModel(){
        return largeModel;
    }

    public void setLargeModel(boolean newValue){
        boolean oldValue=largeModel;
        largeModel=newValue;
        firePropertyChange(LARGE_MODEL_PROPERTY,oldValue,newValue);
    }
    //
    // The following are convenience methods that get forwarded to the
    // current TreeUI.
    //

    public boolean getInvokesStopCellEditing(){
        return invokesStopCellEditing;
    }

    public void setInvokesStopCellEditing(boolean newValue){
        boolean oldValue=invokesStopCellEditing;
        invokesStopCellEditing=newValue;
        firePropertyChange(INVOKES_STOP_CELL_EDITING_PROPERTY,oldValue,
                newValue);
    }

    public boolean getScrollsOnExpand(){
        return scrollsOnExpand;
    }

    public void setScrollsOnExpand(boolean newValue){
        boolean oldValue=scrollsOnExpand;
        scrollsOnExpand=newValue;
        scrollsOnExpandSet=true;
        firePropertyChange(SCROLLS_ON_EXPAND_PROPERTY,oldValue,
                newValue);
    }

    public int getToggleClickCount(){
        return toggleClickCount;
    }

    public void setToggleClickCount(int clickCount){
        int oldCount=toggleClickCount;
        toggleClickCount=clickCount;
        firePropertyChange(TOGGLE_CLICK_COUNT_PROPERTY,oldCount,
                clickCount);
    }

    public boolean getExpandsSelectedPaths(){
        return expandsSelectedPaths;
    }

    public void setExpandsSelectedPaths(boolean newValue){
        boolean oldValue=expandsSelectedPaths;
        expandsSelectedPaths=newValue;
        firePropertyChange(EXPANDS_SELECTED_PATHS_PROPERTY,oldValue,
                newValue);
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
                case ON_OR_INSERT:
                    this.dropMode=dropMode;
                    return;
            }
        }
        throw new IllegalArgumentException(dropMode+": Unsupported drop mode for tree");
    }

    public final DropLocation getDropLocation(){
        return dropLocation;
    }

    private void startDropTimer(){
        if(dropTimer==null){
            dropTimer=new TreeTimer();
        }
        dropTimer.start();
    }

    public boolean isPathEditable(TreePath path){
        return isEditable();
    }

    public boolean isEditable(){
        return editable;
    }

    public void setEditable(boolean flag){
        boolean oldValue=this.editable;
        this.editable=flag;
        firePropertyChange(EDITABLE_PROPERTY,oldValue,flag);
        if(accessibleContext!=null){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    (oldValue?AccessibleState.EDITABLE:null),
                    (flag?AccessibleState.EDITABLE:null));
        }
    }

    public void setSelectionRow(int row){
        int[] rows={row};
        setSelectionRows(rows);
    }

    public void addSelectionRow(int row){
        int[] rows={row};
        addSelectionRows(rows);
    }

    public void addSelectionRows(int[] rows){
        TreeUI ui=getUI();
        if(ui!=null&&rows!=null){
            int numRows=rows.length;
            TreePath[] paths=new TreePath[numRows];
            for(int counter=0;counter<numRows;counter++)
                paths[counter]=ui.getPathForRow(this,rows[counter]);
            addSelectionPaths(paths);
        }
    }

    public void addSelectionPaths(TreePath[] paths){
        getSelectionModel().addSelectionPaths(paths);
    }

    public Object getLastSelectedPathComponent(){
        TreePath selPath=getSelectionModel().getSelectionPath();
        if(selPath!=null)
            return selPath.getLastPathComponent();
        return null;
    }

    public TreePath getSelectionPath(){
        return getSelectionModel().getSelectionPath();
    }

    public void setSelectionPath(TreePath path){
        getSelectionModel().setSelectionPath(path);
    }

    public int[] getSelectionRows(){
        return getSelectionModel().getSelectionRows();
    }

    public void setSelectionRows(int[] rows){
        TreeUI ui=getUI();
        if(ui!=null&&rows!=null){
            int numRows=rows.length;
            TreePath[] paths=new TreePath[numRows];
            for(int counter=0;counter<numRows;counter++){
                paths[counter]=ui.getPathForRow(this,rows[counter]);
            }
            setSelectionPaths(paths);
        }
    }

    public TreeUI getUI(){
        return (TreeUI)ui;
    }

    public void setUI(TreeUI ui){
        if(this.ui!=ui){
            settingUI=true;
            uiTreeExpansionListener=null;
            try{
                super.setUI(ui);
            }finally{
                settingUI=false;
            }
        }
    }

    public int getSelectionCount(){
        return selectionModel.getSelectionCount();
    }

    public int getMinSelectionRow(){
        return getSelectionModel().getMinSelectionRow();
    }

    public int getMaxSelectionRow(){
        return getSelectionModel().getMaxSelectionRow();
    }

    public int getLeadSelectionRow(){
        TreePath leadPath=getLeadSelectionPath();
        if(leadPath!=null){
            return getRowForPath(leadPath);
        }
        return -1;
    }

    public int getRowForPath(TreePath path){
        TreeUI tree=getUI();
        if(tree!=null)
            return tree.getRowForPath(this,path);
        return -1;
    }

    public Enumeration<TreePath> getExpandedDescendants(TreePath parent){
        if(!isExpanded(parent))
            return null;
        Enumeration<TreePath> toggledPaths=expandedState.keys();
        Vector<TreePath> elements=null;
        TreePath path;
        Object value;
        if(toggledPaths!=null){
            while(toggledPaths.hasMoreElements()){
                path=toggledPaths.nextElement();
                value=expandedState.get(path);
                // Add the path if it is expanded, a descendant of parent,
                // and it is visible (all parents expanded). This is rather
                // expensive!
                if(path!=parent&&value!=null&&
                        ((Boolean)value).booleanValue()&&
                        parent.isDescendant(path)&&isVisible(path)){
                    if(elements==null){
                        elements=new Vector<TreePath>();
                    }
                    elements.addElement(path);
                }
            }
        }
        if(elements==null){
            Set<TreePath> empty=Collections.emptySet();
            return Collections.enumeration(empty);
        }
        return elements.elements();
    }

    public boolean isVisible(TreePath path){
        if(path!=null){
            TreePath parentPath=path.getParentPath();
            if(parentPath!=null)
                return isExpanded(parentPath);
            // Root.
            return true;
        }
        return false;
    }

    public boolean hasBeenExpanded(TreePath path){
        return (path!=null&&expandedState.get(path)!=null);
    }

    public boolean isCollapsed(TreePath path){
        return !isExpanded(path);
    }

    public boolean isCollapsed(int row){
        return !isExpanded(row);
    }

    public void scrollRowToVisible(int row){
        scrollPathToVisible(getPathForRow(row));
    }

    public void scrollPathToVisible(TreePath path){
        if(path!=null){
            makeVisible(path);
            Rectangle bounds=getPathBounds(path);
            if(bounds!=null){
                scrollRectToVisible(bounds);
                if(accessibleContext!=null){
                    ((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
                }
            }
        }
    }

    public void makeVisible(TreePath path){
        if(path!=null){
            TreePath parentPath=path.getParentPath();
            if(parentPath!=null){
                expandPath(parentPath);
            }
        }
    }

    public Rectangle getPathBounds(TreePath path){
        TreeUI tree=getUI();
        if(tree!=null)
            return tree.getPathBounds(this,path);
        return null;
    }

    public TreePath getPathForRow(int row){
        TreeUI tree=getUI();
        if(tree!=null)
            return tree.getPathForRow(this,row);
        return null;
    }

    public void expandRow(int row){
        expandPath(getPathForRow(row));
    }

    public void collapseRow(int row){
        collapsePath(getPathForRow(row));
    }

    public void collapsePath(TreePath path){
        setExpandedState(path,false);
    }

    public TreePath getPathForLocation(int x,int y){
        TreePath closestPath=getClosestPathForLocation(x,y);
        if(closestPath!=null){
            Rectangle pathBounds=getPathBounds(closestPath);
            if(pathBounds!=null&&
                    x>=pathBounds.x&&x<(pathBounds.x+pathBounds.width)&&
                    y>=pathBounds.y&&y<(pathBounds.y+pathBounds.height))
                return closestPath;
        }
        return null;
    }

    public int getRowForLocation(int x,int y){
        return getRowForPath(getPathForLocation(x,y));
    }

    public boolean isEditing(){
        TreeUI tree=getUI();
        if(tree!=null)
            return tree.isEditing(this);
        return false;
    }

    public boolean stopEditing(){
        TreeUI tree=getUI();
        if(tree!=null)
            return tree.stopEditing(this);
        return false;
    }
    //
    // Following are primarily convenience methods for mapping from
    // row based selections to path selections.  Sometimes it is
    // easier to deal with these than paths (mouse downs, key downs
    // usually just deal with index based selections).
    // Since row based selections require a UI many of these won't work
    // without one.
    //

    public void cancelEditing(){
        TreeUI tree=getUI();
        if(tree!=null)
            tree.cancelEditing(this);
    }

    public void startEditingAtPath(TreePath path){
        TreeUI tree=getUI();
        if(tree!=null)
            tree.startEditingAtPath(this,path);
    }

    public TreePath getEditingPath(){
        TreeUI tree=getUI();
        if(tree!=null)
            return tree.getEditingPath(this);
        return null;
    }

    public void setSelectionInterval(int index0,int index1){
        TreePath[] paths=getPathBetweenRows(index0,index1);
        this.getSelectionModel().setSelectionPaths(paths);
    }

    protected TreePath[] getPathBetweenRows(int index0,int index1){
        TreeUI tree=getUI();
        if(tree!=null){
            int rowCount=getRowCount();
            if(rowCount>0&&!((index0<0&&index1<0)||
                    (index0>=rowCount&&index1>=rowCount))){
                index0=Math.min(rowCount-1,Math.max(index0,0));
                index1=Math.min(rowCount-1,Math.max(index1,0));
                int minIndex=Math.min(index0,index1);
                int maxIndex=Math.max(index0,index1);
                TreePath[] selection=new TreePath[
                        maxIndex-minIndex+1];
                for(int counter=minIndex;counter<=maxIndex;counter++){
                    selection[counter-minIndex]=
                            tree.getPathForRow(this,counter);
                }
                return selection;
            }
        }
        return new TreePath[0];
    }

    public int getRowCount(){
        TreeUI tree=getUI();
        if(tree!=null)
            return tree.getRowCount(this);
        return 0;
    }

    public void addSelectionInterval(int index0,int index1){
        TreePath[] paths=getPathBetweenRows(index0,index1);
        if(paths!=null&&paths.length>0){
            this.getSelectionModel().addSelectionPaths(paths);
        }
    }

    public void removeSelectionInterval(int index0,int index1){
        TreePath[] paths=getPathBetweenRows(index0,index1);
        if(paths!=null&&paths.length>0){
            this.getSelectionModel().removeSelectionPaths(paths);
        }
    }

    public void removeSelectionPath(TreePath path){
        this.getSelectionModel().removeSelectionPath(path);
    }

    public void removeSelectionRow(int row){
        int[] rows={row};
        removeSelectionRows(rows);
    }

    public void removeSelectionRows(int[] rows){
        TreeUI ui=getUI();
        if(ui!=null&&rows!=null){
            int numRows=rows.length;
            TreePath[] paths=new TreePath[numRows];
            for(int counter=0;counter<numRows;counter++)
                paths[counter]=ui.getPathForRow(this,rows[counter]);
            removeSelectionPaths(paths);
        }
    }

    public void removeSelectionPaths(TreePath[] paths){
        this.getSelectionModel().removeSelectionPaths(paths);
    }

    public boolean isSelectionEmpty(){
        return getSelectionModel().isSelectionEmpty();
    }

    public void addTreeExpansionListener(TreeExpansionListener tel){
        if(settingUI){
            uiTreeExpansionListener=tel;
        }
        listenerList.add(TreeExpansionListener.class,tel);
    }

    public void removeTreeExpansionListener(TreeExpansionListener tel){
        listenerList.remove(TreeExpansionListener.class,tel);
        if(uiTreeExpansionListener==tel){
            uiTreeExpansionListener=null;
        }
    }

    public TreeExpansionListener[] getTreeExpansionListeners(){
        return listenerList.getListeners(TreeExpansionListener.class);
    }

    public void addTreeWillExpandListener(TreeWillExpandListener tel){
        listenerList.add(TreeWillExpandListener.class,tel);
    }

    public void removeTreeWillExpandListener(TreeWillExpandListener tel){
        listenerList.remove(TreeWillExpandListener.class,tel);
    }

    public TreeWillExpandListener[] getTreeWillExpandListeners(){
        return listenerList.getListeners(TreeWillExpandListener.class);
    }

    public void addTreeSelectionListener(TreeSelectionListener tsl){
        listenerList.add(TreeSelectionListener.class,tsl);
        if(listenerList.getListenerCount(TreeSelectionListener.class)!=0
                &&selectionRedirector==null){
            selectionRedirector=new TreeSelectionRedirector();
            selectionModel.addTreeSelectionListener(selectionRedirector);
        }
    }

    public void removeTreeSelectionListener(TreeSelectionListener tsl){
        listenerList.remove(TreeSelectionListener.class,tsl);
        if(listenerList.getListenerCount(TreeSelectionListener.class)==0
                &&selectionRedirector!=null){
            selectionModel.removeTreeSelectionListener
                    (selectionRedirector);
            selectionRedirector=null;
        }
    }

    public TreeSelectionListener[] getTreeSelectionListeners(){
        return listenerList.getListeners(TreeSelectionListener.class);
    }

    protected void fireValueChanged(TreeSelectionEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            // TreeSelectionEvent e = null;
            if(listeners[i]==TreeSelectionListener.class){
                // Lazily create the event:
                // if (e == null)
                // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                ((TreeSelectionListener)listeners[i+1]).valueChanged(e);
            }
        }
    }

    public void treeDidChange(){
        revalidate();
        repaint();
    }

    public TreePath getNextMatch(String prefix,int startingRow,
                                 Position.Bias bias){
        int max=getRowCount();
        if(prefix==null){
            throw new IllegalArgumentException();
        }
        if(startingRow<0||startingRow>=max){
            throw new IllegalArgumentException();
        }
        prefix=prefix.toUpperCase();
        // start search from the next/previous element froom the
        // selected element
        int increment=(bias==Position.Bias.Forward)?1:-1;
        int row=startingRow;
        do{
            TreePath path=getPathForRow(row);
            String text=convertValueToText(
                    path.getLastPathComponent(),isRowSelected(row),
                    isExpanded(row),true,row,false);
            if(text.toUpperCase().startsWith(prefix)){
                return path;
            }
            row=(row+increment+max)%max;
        }while(row!=startingRow);
        return null;
    }

    public String convertValueToText(Object value,boolean selected,
                                     boolean expanded,boolean leaf,int row,
                                     boolean hasFocus){
        if(value!=null){
            String sValue=value.toString();
            if(sValue!=null){
                return sValue;
            }
        }
        return "";
    }

    public boolean isRowSelected(int row){
        return getSelectionModel().isRowSelected(row);
    }

    public boolean isExpanded(int row){
        TreeUI tree=getUI();
        if(tree!=null){
            TreePath path=tree.getPathForRow(this,row);
            if(path!=null){
                Boolean value=expandedState.get(path);
                return (value!=null&&value.booleanValue());
            }
        }
        return false;
    }

    // Serialization support.
    private void writeObject(ObjectOutputStream s) throws IOException{
        Vector<Object> values=new Vector<Object>();
        s.defaultWriteObject();
        // Save the cellRenderer, if its Serializable.
        if(cellRenderer!=null&&cellRenderer instanceof Serializable){
            values.addElement("cellRenderer");
            values.addElement(cellRenderer);
        }
        // Save the cellEditor, if its Serializable.
        if(cellEditor!=null&&cellEditor instanceof Serializable){
            values.addElement("cellEditor");
            values.addElement(cellEditor);
        }
        // Save the treeModel, if its Serializable.
        if(treeModel!=null&&treeModel instanceof Serializable){
            values.addElement("treeModel");
            values.addElement(treeModel);
        }
        // Save the selectionModel, if its Serializable.
        if(selectionModel!=null&&selectionModel instanceof Serializable){
            values.addElement("selectionModel");
            values.addElement(selectionModel);
        }
        Object expandedData=getArchivableExpandedState();
        if(expandedData!=null){
            values.addElement("expandedState");
            values.addElement(expandedData);
        }
        s.writeObject(values);
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                ui.installUI(this);
            }
        }
    }

    private Object getArchivableExpandedState(){
        TreeModel model=getModel();
        if(model!=null){
            Enumeration<TreePath> paths=expandedState.keys();
            if(paths!=null){
                Vector<Object> state=new Vector<Object>();
                while(paths.hasMoreElements()){
                    TreePath path=paths.nextElement();
                    Object archivePath;
                    try{
                        archivePath=getModelIndexsForPath(path);
                    }catch(Error error){
                        archivePath=null;
                    }
                    if(archivePath!=null){
                        state.addElement(archivePath);
                        state.addElement(expandedState.get(path));
                    }
                }
                return state;
            }
        }
        return null;
    }

    private int[] getModelIndexsForPath(TreePath path){
        if(path!=null){
            TreeModel model=getModel();
            int count=path.getPathCount();
            int[] indexs=new int[count-1];
            Object parent=model.getRoot();
            for(int counter=1;counter<count;counter++){
                indexs[counter-1]=model.getIndexOfChild
                        (parent,path.getPathComponent(counter));
                parent=path.getPathComponent(counter);
                if(indexs[counter-1]<0)
                    return null;
            }
            return indexs;
        }
        return null;
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        // Create an instance of expanded state.
        expandedState=new Hashtable<TreePath,Boolean>();
        expandedStack=new Stack<Stack<TreePath>>();
        Vector<?> values=(Vector)s.readObject();
        int indexCounter=0;
        int maxCounter=values.size();
        if(indexCounter<maxCounter&&values.elementAt(indexCounter).
                equals("cellRenderer")){
            cellRenderer=(TreeCellRenderer)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter<maxCounter&&values.elementAt(indexCounter).
                equals("cellEditor")){
            cellEditor=(TreeCellEditor)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter<maxCounter&&values.elementAt(indexCounter).
                equals("treeModel")){
            treeModel=(TreeModel)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter<maxCounter&&values.elementAt(indexCounter).
                equals("selectionModel")){
            selectionModel=(TreeSelectionModel)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter<maxCounter&&values.elementAt(indexCounter).
                equals("expandedState")){
            unarchiveExpandedState(values.elementAt(++indexCounter));
            indexCounter++;
        }
        // Reinstall the redirector.
        if(listenerList.getListenerCount(TreeSelectionListener.class)!=0){
            selectionRedirector=new TreeSelectionRedirector();
            selectionModel.addTreeSelectionListener(selectionRedirector);
        }
        // Listener to TreeModel.
        if(treeModel!=null){
            treeModelListener=createTreeModelListener();
            if(treeModelListener!=null)
                treeModel.addTreeModelListener(treeModelListener);
        }
    }

    private void unarchiveExpandedState(Object state){
        if(state instanceof Vector){
            Vector<?> paths=(Vector)state;
            for(int counter=paths.size()-1;counter>=0;counter--){
                Boolean eState=(Boolean)paths.elementAt(counter--);
                TreePath path;
                try{
                    path=getPathForIndexs((int[])paths.elementAt(counter));
                    if(path!=null)
                        expandedState.put(path,eState);
                }catch(Error error){
                }
            }
        }
    }

    private TreePath getPathForIndexs(int[] indexs){
        if(indexs==null)
            return null;
        TreeModel model=getModel();
        if(model==null)
            return null;
        int count=indexs.length;
        Object parent=model.getRoot();
        if(parent==null)
            return null;
        TreePath parentPath=new TreePath(parent);
        for(int counter=0;counter<count;counter++){
            parent=model.getChild(parent,indexs[counter]);
            if(parent==null)
                return null;
            parentPath=parentPath.pathByAddingChild(parent);
        }
        return parentPath;
    }

    public Dimension getPreferredScrollableViewportSize(){
        int width=getPreferredSize().width;
        int visRows=getVisibleRowCount();
        int height=-1;
        if(isFixedRowHeight())
            height=visRows*getRowHeight();
        else{
            TreeUI ui=getUI();
            if(ui!=null&&visRows>0){
                int rc=ui.getRowCount(this);
                if(rc>=visRows){
                    Rectangle bounds=getRowBounds(visRows-1);
                    if(bounds!=null){
                        height=bounds.y+bounds.height;
                    }
                }else if(rc>0){
                    Rectangle bounds=getRowBounds(0);
                    if(bounds!=null){
                        height=bounds.height*visRows;
                    }
                }
            }
            if(height==-1){
                height=16*visRows;
            }
        }
        return new Dimension(width,height);
    }

    public int getRowHeight(){
        return rowHeight;
    }

    public void setRowHeight(int rowHeight){
        int oldValue=this.rowHeight;
        this.rowHeight=rowHeight;
        rowHeightSet=true;
        firePropertyChange(ROW_HEIGHT_PROPERTY,oldValue,this.rowHeight);
        invalidate();
    }

    public boolean isFixedRowHeight(){
        return (rowHeight>0);
    }

    public Rectangle getRowBounds(int row){
        return getPathBounds(getPathForRow(row));
    }
    //
    // Scrollable interface
    //

    public int getVisibleRowCount(){
        return visibleRowCount;
    }

    public void setVisibleRowCount(int newCount){
        int oldCount=visibleRowCount;
        visibleRowCount=newCount;
        firePropertyChange(VISIBLE_ROW_COUNT_PROPERTY,oldCount,
                visibleRowCount);
        invalidate();
        if(accessibleContext!=null){
            ((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
        }
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,int direction){
        if(orientation==SwingConstants.VERTICAL){
            Rectangle rowBounds;
            int firstIndex=getClosestRowForLocation
                    (0,visibleRect.y);
            if(firstIndex!=-1){
                rowBounds=getRowBounds(firstIndex);
                if(rowBounds.y!=visibleRect.y){
                    if(direction<0){
                        // UP
                        return Math.max(0,(visibleRect.y-rowBounds.y));
                    }
                    return (rowBounds.y+rowBounds.height-visibleRect.y);
                }
                if(direction<0){ // UP
                    if(firstIndex!=0){
                        rowBounds=getRowBounds(firstIndex-1);
                        return rowBounds.height;
                    }
                }else{
                    return rowBounds.height;
                }
            }
            return 0;
        }
        return 4;
    }

    public int getClosestRowForLocation(int x,int y){
        return getRowForPath(getClosestPathForLocation(x,y));
    }

    public TreePath getClosestPathForLocation(int x,int y){
        TreeUI tree=getUI();
        if(tree!=null)
            return tree.getClosestPathForLocation(this,x,y);
        return null;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,int direction){
        return (orientation==SwingConstants.VERTICAL)?visibleRect.height:
                visibleRect.width;
    }

    public boolean getScrollableTracksViewportWidth(){
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            return parent.getWidth()>getPreferredSize().width;
        }
        return false;
    }

    public boolean getScrollableTracksViewportHeight(){
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            return parent.getHeight()>getPreferredSize().height;
        }
        return false;
    }

    protected void
    removeDescendantToggledPaths(Enumeration<TreePath> toRemove){
        if(toRemove!=null){
            while(toRemove.hasMoreElements()){
                Enumeration<?> descendants=getDescendantToggledPaths
                        (toRemove.nextElement());
                if(descendants!=null){
                    while(descendants.hasMoreElements()){
                        expandedState.remove(descendants.nextElement());
                    }
                }
            }
        }
    }

    protected Enumeration<TreePath>
    getDescendantToggledPaths(TreePath parent){
        if(parent==null)
            return null;
        Vector<TreePath> descendants=new Vector<TreePath>();
        Enumeration<TreePath> nodes=expandedState.keys();
        while(nodes.hasMoreElements()){
            TreePath path=nodes.nextElement();
            if(parent.isDescendant(path))
                descendants.addElement(path);
        }
        return descendants.elements();
    }

    void removeDescendantSelectedPaths(TreeModelEvent e){
        TreePath pPath=SwingUtilities2.getTreePath(e,getModel());
        Object[] oldChildren=e.getChildren();
        TreeSelectionModel sm=getSelectionModel();
        if(sm!=null&&pPath!=null&&oldChildren!=null&&
                oldChildren.length>0){
            for(int counter=oldChildren.length-1;counter>=0;
                counter--){
                // Might be better to call getDescendantSelectedPaths
                // numerous times, then push to the model.
                removeDescendantSelectedPaths(pPath.pathByAddingChild
                        (oldChildren[counter]),true);
            }
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJTree();
        }
        return accessibleContext;
    }

    public static final class DropLocation extends TransferHandler.DropLocation{
        private final TreePath path;
        private final int index;

        private DropLocation(Point p,TreePath path,int index){
            super(p);
            this.path=path;
            this.index=index;
        }

        public int getChildIndex(){
            return index;
        }

        public TreePath getPath(){
            return path;
        }

        public String toString(){
            return getClass().getName()
                    +"[dropPoint="+getDropPoint()+","
                    +"path="+path+","
                    +"childIndex="+index+"]";
        }
    }

    @SuppressWarnings("serial")
    protected static class EmptySelectionModel extends
            DefaultTreeSelectionModel{
        protected static final EmptySelectionModel sharedInstance=
                new EmptySelectionModel();

        static public EmptySelectionModel sharedInstance(){
            return sharedInstance;
        }

        public void setRowMapper(RowMapper mapper){
        }

        public void setSelectionMode(int mode){
        }

        public void setSelectionPaths(TreePath[] paths){
        }

        public void addSelectionPaths(TreePath[] paths){
        }

        public void removeSelectionPaths(TreePath[] paths){
        }

        public void addTreeSelectionListener(TreeSelectionListener listener){
        }

        public void removeTreeSelectionListener(
                TreeSelectionListener listener){
        }

        public void addPropertyChangeListener(
                PropertyChangeListener listener){
        }

        public void removePropertyChangeListener(
                PropertyChangeListener listener){
        }
    }

    @SuppressWarnings("serial")
    public static class DynamicUtilTreeNode extends DefaultMutableTreeNode{
        protected boolean hasChildren;
        protected Object childValue;
        protected boolean loadedChildren;

        public DynamicUtilTreeNode(Object value,Object children){
            super(value);
            loadedChildren=false;
            childValue=children;
            if(children!=null){
                if(children instanceof Vector)
                    setAllowsChildren(true);
                else if(children instanceof Hashtable)
                    setAllowsChildren(true);
                else if(children instanceof Object[])
                    setAllowsChildren(true);
                else
                    setAllowsChildren(false);
            }else
                setAllowsChildren(false);
        }

        public TreeNode getChildAt(int index){
            if(!loadedChildren)
                loadChildren();
            return super.getChildAt(index);
        }

        public int getChildCount(){
            if(!loadedChildren)
                loadChildren();
            return super.getChildCount();
        }

        protected void loadChildren(){
            loadedChildren=true;
            createChildren(this,childValue);
        }

        public static void createChildren(DefaultMutableTreeNode parent,
                                          Object children){
            if(children instanceof Vector){
                Vector<?> childVector=(Vector)children;
                for(int counter=0, maxCounter=childVector.size();
                    counter<maxCounter;counter++)
                    parent.add(new DynamicUtilTreeNode
                            (childVector.elementAt(counter),
                                    childVector.elementAt(counter)));
            }else if(children instanceof Hashtable){
                Hashtable<?,?> childHT=(Hashtable)children;
                Enumeration<?> keys=childHT.keys();
                Object aKey;
                while(keys.hasMoreElements()){
                    aKey=keys.nextElement();
                    parent.add(new DynamicUtilTreeNode(aKey,
                            childHT.get(aKey)));
                }
            }else if(children instanceof Object[]){
                Object[] childArray=(Object[])children;
                for(int counter=0, maxCounter=childArray.length;
                    counter<maxCounter;counter++)
                    parent.add(new DynamicUtilTreeNode(childArray[counter],
                            childArray[counter]));
            }
        }

        public Enumeration children(){
            if(!loadedChildren)
                loadChildren();
            return super.children();
        }

        public boolean isLeaf(){
            return !getAllowsChildren();
        }
    }

    @SuppressWarnings("serial")
    private class TreeTimer extends Timer{
        public TreeTimer(){
            super(2000,null);
            setRepeats(false);
        }

        public void fireActionPerformed(ActionEvent ae){
            JTree.this.expandRow(expandRow);
        }
    }

    @SuppressWarnings("serial")
    protected class TreeSelectionRedirector implements Serializable,
            TreeSelectionListener{
        public void valueChanged(TreeSelectionEvent e){
            TreeSelectionEvent newE;
            newE=(TreeSelectionEvent)e.cloneWithSource(JTree.this);
            fireValueChanged(newE);
        }
    } // End of class JTree.TreeSelectionRedirector
/////////////////
// Accessibility support
////////////////

    protected class TreeModelHandler implements TreeModelListener{
        public void treeNodesChanged(TreeModelEvent e){
        }

        public void treeNodesInserted(TreeModelEvent e){
        }

        public void treeStructureChanged(TreeModelEvent e){
            if(e==null)
                return;
            // NOTE: If I change this to NOT remove the descendants
            // and update BasicTreeUIs treeStructureChanged method
            // to update descendants in response to a treeStructureChanged
            // event, all the children of the event won't collapse!
            TreePath parent=SwingUtilities2.getTreePath(e,getModel());
            if(parent==null)
                return;
            if(parent.getPathCount()==1){
                // New root, remove everything!
                clearToggledPaths();
                Object treeRoot=treeModel.getRoot();
                if(treeRoot!=null&&
                        !treeModel.isLeaf(treeRoot)){
                    // Mark the root as expanded, if it isn't a leaf.
                    expandedState.put(parent,Boolean.TRUE);
                }
            }else if(expandedState.get(parent)!=null){
                Vector<TreePath> toRemove=new Vector<TreePath>(1);
                boolean isExpanded=isExpanded(parent);
                toRemove.addElement(parent);
                removeDescendantToggledPaths(toRemove.elements());
                if(isExpanded){
                    TreeModel model=getModel();
                    if(model==null||model.isLeaf
                            (parent.getLastPathComponent()))
                        collapsePath(parent);
                    else
                        expandedState.put(parent,Boolean.TRUE);
                }
            }
            removeDescendantSelectedPaths(parent,false);
        }

        public void treeNodesRemoved(TreeModelEvent e){
            if(e==null)
                return;
            TreePath parent=SwingUtilities2.getTreePath(e,getModel());
            Object[] children=e.getChildren();
            if(children==null)
                return;
            TreePath rPath;
            Vector<TreePath> toRemove
                    =new Vector<TreePath>(Math.max(1,children.length));
            for(int counter=children.length-1;counter>=0;counter--){
                rPath=parent.pathByAddingChild(children[counter]);
                if(expandedState.get(rPath)!=null)
                    toRemove.addElement(rPath);
            }
            if(toRemove.size()>0)
                removeDescendantToggledPaths(toRemove.elements());
            TreeModel model=getModel();
            if(model==null||model.isLeaf(parent.getLastPathComponent()))
                expandedState.remove(parent);
            removeDescendantSelectedPaths(e);
        }
    }

    @SuppressWarnings("serial")
    protected class AccessibleJTree extends AccessibleJComponent
            implements AccessibleSelection, TreeSelectionListener,
            TreeModelListener, TreeExpansionListener{
        TreePath leadSelectionPath;
        Accessible leadSelectionAccessible;

        public AccessibleJTree(){
            // Add a tree model listener for JTree
            TreeModel model=JTree.this.getModel();
            if(model!=null){
                model.addTreeModelListener(this);
            }
            JTree.this.addTreeExpansionListener(this);
            JTree.this.addTreeSelectionListener(this);
            leadSelectionPath=JTree.this.getLeadSelectionPath();
            leadSelectionAccessible=(leadSelectionPath!=null)
                    ?new AccessibleJTreeNode(JTree.this,
                    leadSelectionPath,
                    JTree.this)
                    :null;
        }

        public void treeExpanded(TreeExpansionEvent e){
            fireVisibleDataPropertyChange();
            TreePath path=e.getPath();
            if(path!=null){
                // TIGER - 4839971
                // Set parent to null so AccessibleJTreeNode computes
                // its parent.
                AccessibleJTreeNode node=new AccessibleJTreeNode(JTree.this,
                        path,
                        null);
                PropertyChangeEvent pce=new PropertyChangeEvent(node,
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        AccessibleState.COLLAPSED,
                        AccessibleState.EXPANDED);
                firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        null,pce);
            }
        }        public void valueChanged(TreeSelectionEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
        }

        public void treeCollapsed(TreeExpansionEvent e){
            fireVisibleDataPropertyChange();
            TreePath path=e.getPath();
            if(path!=null){
                // Set parent to null so AccessibleJTreeNode computes
                // its parent.
                AccessibleJTreeNode node=new AccessibleJTreeNode(JTree.this,
                        path,
                        null);
                PropertyChangeEvent pce=new PropertyChangeEvent(node,
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        AccessibleState.EXPANDED,
                        AccessibleState.COLLAPSED);
                firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        null,pce);
            }
        }        public void fireVisibleDataPropertyChange(){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
        }
        // Fire the visible data changes for the model changes.

        void fireActiveDescendantPropertyChange(TreePath oldPath,TreePath newPath){
            if(oldPath!=newPath){
                Accessible oldLSA=(oldPath!=null)
                        ?new AccessibleJTreeNode(JTree.this,
                        oldPath,
                        null)
                        :null;
                Accessible newLSA=(newPath!=null)
                        ?new AccessibleJTreeNode(JTree.this,
                        newPath,
                        null)
                        :null;
                firePropertyChange(AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY,
                        oldLSA,newLSA);
            }
        }        public void treeNodesChanged(TreeModelEvent e){
            fireVisibleDataPropertyChange();
        }

        private AccessibleContext getCurrentAccessibleContext(){
            Component c=getCurrentComponent();
            if(c instanceof Accessible){
                return c.getAccessibleContext();
            }else{
                return null;
            }
        }        public void treeNodesInserted(TreeModelEvent e){
            fireVisibleDataPropertyChange();
        }

        private Component getCurrentComponent(){
            // is the object visible?
            // if so, get row, selected, focus & leaf state,
            // and then get the renderer component and return it
            TreeModel model=JTree.this.getModel();
            if(model==null){
                return null;
            }
            Object treeRoot=model.getRoot();
            if(treeRoot==null)
                return null;
            TreePath path=new TreePath(treeRoot);
            if(JTree.this.isVisible(path)){
                TreeCellRenderer r=JTree.this.getCellRenderer();
                TreeUI ui=JTree.this.getUI();
                if(ui!=null){
                    int row=ui.getRowForPath(JTree.this,path);
                    int lsr=JTree.this.getLeadSelectionRow();
                    boolean hasFocus=JTree.this.isFocusOwner()
                            &&(lsr==row);
                    boolean selected=JTree.this.isPathSelected(path);
                    boolean expanded=JTree.this.isExpanded(path);
                    return r.getTreeCellRendererComponent(JTree.this,
                            treeRoot,selected,expanded,
                            model.isLeaf(treeRoot),row,hasFocus);
                }
            }
            return null;
        }        public void treeNodesRemoved(TreeModelEvent e){
            fireVisibleDataPropertyChange();
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.TREE;
        }        public void treeStructureChanged(TreeModelEvent e){
            fireVisibleDataPropertyChange();
        }

        public int getAccessibleChildrenCount(){
            TreeModel model=JTree.this.getModel();
            if(model==null){
                return 0;
            }
            if(isRootVisible()){
                return 1;    // the root node
            }
            Object treeRoot=model.getRoot();
            if(treeRoot==null)
                return 0;
            // return the root's first set of children count
            return model.getChildCount(treeRoot);
        }

        public Accessible getAccessibleChild(int i){
            TreeModel model=JTree.this.getModel();
            if(model==null){
                return null;
            }
            Object treeRoot=model.getRoot();
            if(treeRoot==null){
                return null;
            }
            if(isRootVisible()){
                if(i==0){    // return the root node Accessible
                    Object[] objPath={treeRoot};
                    if(objPath[0]==null)
                        return null;
                    TreePath path=new TreePath(objPath);
                    return new AccessibleJTreeNode(JTree.this,path,JTree.this);
                }else{
                    return null;
                }
            }
            // return Accessible for one of root's child nodes
            int count=model.getChildCount(treeRoot);
            if(i<0||i>=count){
                return null;
            }
            Object obj=model.getChild(treeRoot,i);
            if(obj==null)
                return null;
            Object[] objPath={treeRoot,obj};
            TreePath path=new TreePath(objPath);
            return new AccessibleJTreeNode(JTree.this,path,JTree.this);
        }

        public Accessible getAccessibleAt(Point p){
            TreePath path=getClosestPathForLocation(p.x,p.y);
            if(path!=null){
                // JTree.this is NOT the parent; parent will get computed later
                return new AccessibleJTreeNode(JTree.this,path,null);
            }else{
                return null;
            }
        }

        public int getAccessibleIndexInParent(){
            // didn't ever need to override this...
            return super.getAccessibleIndexInParent();
        }

        public int getAccessibleSelectionCount(){
            Object[] rootPath=new Object[1];
            rootPath[0]=treeModel.getRoot();
            if(rootPath[0]==null)
                return 0;
            TreePath childPath=new TreePath(rootPath);
            if(JTree.this.isPathSelected(childPath)){
                return 1;
            }else{
                return 0;
            }
        }
        // Overridden methods from AccessibleJComponent

        protected class AccessibleJTreeNode extends AccessibleContext
                implements Accessible, AccessibleComponent, AccessibleSelection,
                AccessibleAction{
            private JTree tree=null;
            private TreeModel treeModel=null;
            private Object obj=null;
            private TreePath path=null;
            private Accessible accessibleParent=null;
            private int index=0;
            private boolean isLeaf=false;

            public AccessibleJTreeNode(JTree t,TreePath p,Accessible ap){
                tree=t;
                path=p;
                accessibleParent=ap;
                treeModel=t.getModel();
                obj=p.getLastPathComponent();
                if(treeModel!=null){
                    isLeaf=treeModel.isLeaf(obj);
                }
            }

            public AccessibleContext getAccessibleContext(){
                return this;
            }            private TreePath getChildTreePath(int i){
                // Tree nodes can't be so complex that they have
                // two sets of children -> we're ignoring that case
                if(i<0||i>=getAccessibleChildrenCount()){
                    return null;
                }else{
                    Object childObj=treeModel.getChild(obj,i);
                    Object[] objPath=path.getPath();
                    Object[] objChildPath=new Object[objPath.length+1];
                    System.arraycopy(objPath,0,objChildPath,0,objPath.length);
                    objChildPath[objChildPath.length-1]=childObj;
                    return new TreePath(objChildPath);
                }
            }

            public String getAccessibleName(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    String name=ac.getAccessibleName();
                    if((name!=null)&&(name!="")){
                        return ac.getAccessibleName();
                    }else{
                        return null;
                    }
                }
                if((accessibleName!=null)&&(accessibleName!="")){
                    return accessibleName;
                }else{
                    // fall back to the client property
                    return (String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
                }
            }

            private AccessibleContext getCurrentAccessibleContext(){
                Component c=getCurrentComponent();
                if(c instanceof Accessible){
                    return c.getAccessibleContext();
                }else{
                    return null;
                }
            }

            private Component getCurrentComponent(){
                // is the object visible?
                // if so, get row, selected, focus & leaf state,
                // and then get the renderer component and return it
                if(tree.isVisible(path)){
                    TreeCellRenderer r=tree.getCellRenderer();
                    if(r==null){
                        return null;
                    }
                    TreeUI ui=tree.getUI();
                    if(ui!=null){
                        int row=ui.getRowForPath(JTree.this,path);
                        boolean selected=tree.isPathSelected(path);
                        boolean expanded=tree.isExpanded(path);
                        boolean hasFocus=false; // how to tell?? -PK
                        return r.getTreeCellRendererComponent(tree,obj,
                                selected,expanded,isLeaf,row,hasFocus);
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
            // *** should check tooltip text for desc. (needs MouseEvent)
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
                    return AccessibleRole.UNKNOWN;
                }
            }

            public AccessibleStateSet getAccessibleStateSet(){
                AccessibleContext ac=getCurrentAccessibleContext();
                AccessibleStateSet states;
                if(ac!=null){
                    states=ac.getAccessibleStateSet();
                }else{
                    states=new AccessibleStateSet();
                }
                // need to test here, 'cause the underlying component
                // is a cellRenderer, which is never showing...
                if(isShowing()){
                    states.add(AccessibleState.SHOWING);
                }else if(states.contains(AccessibleState.SHOWING)){
                    states.remove(AccessibleState.SHOWING);
                }
                if(isVisible()){
                    states.add(AccessibleState.VISIBLE);
                }else if(states.contains(AccessibleState.VISIBLE)){
                    states.remove(AccessibleState.VISIBLE);
                }
                if(tree.isPathSelected(path)){
                    states.add(AccessibleState.SELECTED);
                }
                if(path==getLeadSelectionPath()){
                    states.add(AccessibleState.ACTIVE);
                }
                if(!isLeaf){
                    states.add(AccessibleState.EXPANDABLE);
                }
                if(tree.isExpanded(path)){
                    states.add(AccessibleState.EXPANDED);
                }else{
                    states.add(AccessibleState.COLLAPSED);
                }
                if(tree.isEditable()){
                    states.add(AccessibleState.EDITABLE);
                }
                return states;
            }

            public Accessible getAccessibleParent(){
                // someone wants to know, so we need to create our parent
                // if we don't have one (hey, we're a talented kid!)
                if(accessibleParent==null){
                    Object[] objPath=path.getPath();
                    if(objPath.length>1){
                        Object objParent=objPath[objPath.length-2];
                        if(treeModel!=null){
                            index=treeModel.getIndexOfChild(objParent,obj);
                        }
                        Object[] objParentPath=new Object[objPath.length-1];
                        System.arraycopy(objPath,0,objParentPath,
                                0,objPath.length-1);
                        TreePath parentPath=new TreePath(objParentPath);
                        accessibleParent=new AccessibleJTreeNode(tree,
                                parentPath,
                                null);
                        this.setAccessibleParent(accessibleParent);
                    }else if(treeModel!=null){
                        accessibleParent=tree; // we're the top!
                        index=0; // we're an only child!
                        this.setAccessibleParent(accessibleParent);
                    }
                }
                return accessibleParent;
            }

            public int getAccessibleIndexInParent(){
                // index is invalid 'till we have an accessibleParent...
                if(accessibleParent==null){
                    getAccessibleParent();
                }
                Object[] objPath=path.getPath();
                if(objPath.length>1){
                    Object objParent=objPath[objPath.length-2];
                    if(treeModel!=null){
                        index=treeModel.getIndexOfChild(objParent,obj);
                    }
                }
                return index;
            }

            public int getAccessibleChildrenCount(){
                // Tree nodes can't be so complex that they have
                // two sets of children -> we're ignoring that case
                return treeModel.getChildCount(obj);
            }

            public Accessible getAccessibleChild(int i){
                // Tree nodes can't be so complex that they have
                // two sets of children -> we're ignoring that case
                if(i<0||i>=getAccessibleChildrenCount()){
                    return null;
                }else{
                    Object childObj=treeModel.getChild(obj,i);
                    Object[] objPath=path.getPath();
                    Object[] objChildPath=new Object[objPath.length+1];
                    System.arraycopy(objPath,0,objChildPath,0,objPath.length);
                    objChildPath[objChildPath.length-1]=childObj;
                    TreePath childPath=new TreePath(objChildPath);
                    return new AccessibleJTreeNode(JTree.this,childPath,this);
                }
            }

            public Locale getLocale(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getLocale();
                }else{
                    return tree.getLocale();
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
                return this;
            }

            public AccessibleComponent getAccessibleComponent(){
                return this; // to override getBounds()
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
            }            public AccessibleSelection getAccessibleSelection(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null&&isLeaf){
                    return getCurrentAccessibleContext().getAccessibleSelection();
                }else{
                    return this;
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
            }            public AccessibleText getAccessibleText(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return getCurrentAccessibleContext().getAccessibleText();
                }else{
                    return null;
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
            }            public AccessibleValue getAccessibleValue(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return getCurrentAccessibleContext().getAccessibleValue();
                }else{
                    return null;
                }
            }
            // AccessibleComponent methods

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
                Rectangle pathBounds=tree.getPathBounds(path);
                Rectangle parentBounds=tree.getVisibleRect();
                return pathBounds!=null&&parentBounds!=null&&
                        parentBounds.intersects(pathBounds);
            }

            public void setVisible(boolean b){
            }

            public boolean isShowing(){
                return (tree.isShowing()&&isVisible());
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
                if(tree!=null){
                    Point treeLocation=tree.getLocationOnScreen();
                    Rectangle pathBounds=tree.getPathBounds(path);
                    if(treeLocation!=null&&pathBounds!=null){
                        Point nodeLocation=new Point(pathBounds.x,
                                pathBounds.y);
                        nodeLocation.translate(treeLocation.x,treeLocation.y);
                        return nodeLocation;
                    }else{
                        return null;
                    }
                }else{
                    return null;
                }
            }

            public Point getLocation(){
                Rectangle r=getBounds();
                if(r!=null){
                    return r.getLocation();
                }else{
                    return null;
                }
            }

            public void setLocation(Point p){
            }

            public Rectangle getBounds(){
                Rectangle r=tree.getPathBounds(path);
                Accessible parent=getAccessibleParent();
                if(parent!=null){
                    if(parent instanceof AccessibleJTreeNode){
                        Point parentLoc=((AccessibleJTreeNode)parent).getLocationInJTree();
                        if(parentLoc!=null&&r!=null){
                            r.translate(-parentLoc.x,-parentLoc.y);
                        }else{
                            return null;        // not visible!
                        }
                    }
                }
                return r;
            }

            protected Point getLocationInJTree(){
                Rectangle r=tree.getPathBounds(path);
                if(r!=null){
                    return r.getLocation();
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
                return getBounds().getSize();
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

            @SuppressWarnings("deprecation")
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

            public int getAccessibleActionCount(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    AccessibleAction aa=ac.getAccessibleAction();
                    if(aa!=null){
                        return (aa.getAccessibleActionCount()+(isLeaf?0:1));
                    }
                }
                return isLeaf?0:1;
            }

            public String getAccessibleActionDescription(int i){
                if(i<0||i>=getAccessibleActionCount()){
                    return null;
                }
                AccessibleContext ac=getCurrentAccessibleContext();
                if(i==0){
                    // TIGER - 4766636
                    return AccessibleAction.TOGGLE_EXPAND;
                }else if(ac!=null){
                    AccessibleAction aa=ac.getAccessibleAction();
                    if(aa!=null){
                        return aa.getAccessibleActionDescription(i-1);
                    }
                }
                return null;
            }

            public boolean doAccessibleAction(int i){
                if(i<0||i>=getAccessibleActionCount()){
                    return false;
                }
                AccessibleContext ac=getCurrentAccessibleContext();
                if(i==0){
                    if(JTree.this.isExpanded(path)){
                        JTree.this.collapsePath(path);
                    }else{
                        JTree.this.expandPath(path);
                    }
                    return true;
                }else if(ac!=null){
                    AccessibleAction aa=ac.getAccessibleAction();
                    if(aa!=null){
                        return aa.doAccessibleAction(i-1);
                    }
                }
                return false;
            }


            // AccessibleSelection methods

            public int getAccessibleSelectionCount(){
                int count=0;
                int childCount=getAccessibleChildrenCount();
                for(int i=0;i<childCount;i++){
                    TreePath childPath=getChildTreePath(i);
                    if(tree.isPathSelected(childPath)){
                        count++;
                    }
                }
                return count;
            }

            public Accessible getAccessibleSelection(int i){
                int childCount=getAccessibleChildrenCount();
                if(i<0||i>=childCount){
                    return null;        // out of range
                }
                int count=0;
                for(int j=0;j<childCount&&i>=count;j++){
                    TreePath childPath=getChildTreePath(j);
                    if(tree.isPathSelected(childPath)){
                        if(count==i){
                            return new AccessibleJTreeNode(tree,childPath,this);
                        }else{
                            count++;
                        }
                    }
                }
                return null;
            }

            public boolean isAccessibleChildSelected(int i){
                int childCount=getAccessibleChildrenCount();
                if(i<0||i>=childCount){
                    return false;       // out of range
                }else{
                    TreePath childPath=getChildTreePath(i);
                    return tree.isPathSelected(childPath);
                }
            }

            public void addAccessibleSelection(int i){
                TreeModel model=JTree.this.getModel();
                if(model!=null){
                    if(i>=0&&i<getAccessibleChildrenCount()){
                        TreePath path=getChildTreePath(i);
                        JTree.this.addSelectionPath(path);
                    }
                }
            }

            public void removeAccessibleSelection(int i){
                TreeModel model=JTree.this.getModel();
                if(model!=null){
                    if(i>=0&&i<getAccessibleChildrenCount()){
                        TreePath path=getChildTreePath(i);
                        JTree.this.removeSelectionPath(path);
                    }
                }
            }

            public void clearAccessibleSelection(){
                int childCount=getAccessibleChildrenCount();
                for(int i=0;i<childCount;i++){
                    removeAccessibleSelection(i);
                }
            }

            public void selectAllAccessibleSelection(){
                TreeModel model=JTree.this.getModel();
                if(model!=null){
                    int childCount=getAccessibleChildrenCount();
                    TreePath path;
                    for(int i=0;i<childCount;i++){
                        path=getChildTreePath(i);
                        JTree.this.addSelectionPath(path);
                    }
                }
            }
            // AccessibleAction methods






        } // inner class AccessibleJTreeNode









        // AccessibleSelection methods
        public AccessibleSelection getAccessibleSelection(){
            return this;
        }



        public Accessible getAccessibleSelection(int i){
            // The JTree can have only one accessible child, the root.
            if(i==0){
                Object[] rootPath=new Object[1];
                rootPath[0]=treeModel.getRoot();
                if(rootPath[0]==null)
                    return null;
                TreePath childPath=new TreePath(rootPath);
                if(JTree.this.isPathSelected(childPath)){
                    return new AccessibleJTreeNode(JTree.this,childPath,JTree.this);
                }
            }
            return null;
        }

        public boolean isAccessibleChildSelected(int i){
            // The JTree can have only one accessible child, the root.
            if(i==0){
                Object[] rootPath=new Object[1];
                rootPath[0]=treeModel.getRoot();
                if(rootPath[0]==null)
                    return false;
                TreePath childPath=new TreePath(rootPath);
                return JTree.this.isPathSelected(childPath);
            }else{
                return false;
            }
        }

        public void addAccessibleSelection(int i){
            TreeModel model=JTree.this.getModel();
            if(model!=null){
                if(i==0){
                    Object[] objPath={model.getRoot()};
                    if(objPath[0]==null)
                        return;
                    TreePath path=new TreePath(objPath);
                    JTree.this.addSelectionPath(path);
                }
            }
        }

        public void removeAccessibleSelection(int i){
            TreeModel model=JTree.this.getModel();
            if(model!=null){
                if(i==0){
                    Object[] objPath={model.getRoot()};
                    if(objPath[0]==null)
                        return;
                    TreePath path=new TreePath(objPath);
                    JTree.this.removeSelectionPath(path);
                }
            }
        }

        public void clearAccessibleSelection(){
            int childCount=getAccessibleChildrenCount();
            for(int i=0;i<childCount;i++){
                removeAccessibleSelection(i);
            }
        }

        public void selectAllAccessibleSelection(){
            TreeModel model=JTree.this.getModel();
            if(model!=null){
                Object[] objPath={model.getRoot()};
                if(objPath[0]==null)
                    return;
                TreePath path=new TreePath(objPath);
                JTree.this.addSelectionPath(path);
            }
        }


    }  // inner class AccessibleJTree
} // End of class JTree
