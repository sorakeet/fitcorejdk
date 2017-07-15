/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.swing.SwingUtilities2;
import sun.swing.SwingUtilities2.Section;

import javax.accessibility.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ListUI;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.List;

import static sun.swing.SwingUtilities2.Section.LEADING;
import static sun.swing.SwingUtilities2.Section.TRAILING;

public class JList<E> extends JComponent implements Scrollable, Accessible{
    public static final int VERTICAL=0;
    public static final int VERTICAL_WRAP=1;
    public static final int HORIZONTAL_WRAP=2;
    private static final String uiClassID="ListUI";
    private int fixedCellWidth=-1;
    private int fixedCellHeight=-1;
    private int horizontalScrollIncrement=-1;
    private E prototypeCellValue;
    private int visibleRowCount=8;
    private Color selectionForeground;
    private Color selectionBackground;
    private boolean dragEnabled;
    private ListSelectionModel selectionModel;
    private ListModel<E> dataModel;
    private ListCellRenderer<? super E> cellRenderer;
    private ListSelectionListener selectionListener;
    private int layoutOrientation;
    private DropMode dropMode=DropMode.USE_SELECTION;
    private transient DropLocation dropLocation;

    public JList(final E[] listData){
        this(
                new AbstractListModel<E>(){
                    public int getSize(){
                        return listData.length;
                    }

                    public E getElementAt(int i){
                        return listData[i];
                    }
                }
        );
    }

    public JList(ListModel<E> dataModel){
        if(dataModel==null){
            throw new IllegalArgumentException("dataModel must be non null");
        }
        // Register with the ToolTipManager so that tooltips from the
        // renderer show through.
        ToolTipManager toolTipManager=ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        layoutOrientation=VERTICAL;
        this.dataModel=dataModel;
        selectionModel=createSelectionModel();
        setAutoscrolls(true);
        setOpaque(true);
        updateUI();
    }

    public void updateUI(){
        setUI((ListUI)UIManager.getUI(this));
        ListCellRenderer<? super E> renderer=getCellRenderer();
        if(renderer instanceof Component){
            SwingUtilities.updateComponentTreeUI((Component)renderer);
        }
    }

    public String getUIClassID(){
        return uiClassID;
    }

    public String getToolTipText(MouseEvent event){
        if(event!=null){
            Point p=event.getPoint();
            int index=locationToIndex(p);
            ListCellRenderer<? super E> r=getCellRenderer();
            Rectangle cellBounds;
            if(index!=-1&&r!=null&&(cellBounds=
                    getCellBounds(index,index))!=null&&
                    cellBounds.contains(p.x,p.y)){
                ListSelectionModel lsm=getSelectionModel();
                Component rComponent=r.getListCellRendererComponent(
                        this,getModel().getElementAt(index),index,
                        lsm.isSelectedIndex(index),
                        (hasFocus()&&(lsm.getLeadSelectionIndex()==
                                index)));
                if(rComponent instanceof JComponent){
                    MouseEvent newEvent;
                    p.translate(-cellBounds.x,-cellBounds.y);
                    newEvent=new MouseEvent(rComponent,event.getID(),
                            event.getWhen(),
                            event.getModifiers(),
                            p.x,p.y,
                            event.getXOnScreen(),
                            event.getYOnScreen(),
                            event.getClickCount(),
                            event.isPopupTrigger(),
                            MouseEvent.NOBUTTON);
                    String tip=((JComponent)rComponent).getToolTipText(
                            newEvent);
                    if(tip!=null){
                        return tip;
                    }
                }
            }
        }
        return super.getToolTipText();
    }

    DropLocation dropLocationForPoint(Point p){
        DropLocation location=null;
        Rectangle rect=null;
        int index=locationToIndex(p);
        if(index!=-1){
            rect=getCellBounds(index,index);
        }
        switch(dropMode){
            case USE_SELECTION:
            case ON:
                location=new DropLocation(p,
                        (rect!=null&&rect.contains(p))?index:-1,
                        false);
                break;
            case INSERT:
                if(index==-1){
                    location=new DropLocation(p,getModel().getSize(),true);
                    break;
                }
                if(layoutOrientation==HORIZONTAL_WRAP){
                    boolean ltr=getComponentOrientation().isLeftToRight();
                    if(SwingUtilities2.liesInHorizontal(rect,p,ltr,false)==TRAILING){
                        index++;
                        // special case for below all cells
                    }else if(index==getModel().getSize()-1&&p.y>=rect.y+rect.height){
                        index++;
                    }
                }else{
                    if(SwingUtilities2.liesInVertical(rect,p,false)==TRAILING){
                        index++;
                    }
                }
                location=new DropLocation(p,index,true);
                break;
            case ON_OR_INSERT:
                if(index==-1){
                    location=new DropLocation(p,getModel().getSize(),true);
                    break;
                }
                boolean between=false;
                if(layoutOrientation==HORIZONTAL_WRAP){
                    boolean ltr=getComponentOrientation().isLeftToRight();
                    Section section=SwingUtilities2.liesInHorizontal(rect,p,ltr,true);
                    if(section==TRAILING){
                        index++;
                        between=true;
                        // special case for below all cells
                    }else if(index==getModel().getSize()-1&&p.y>=rect.y+rect.height){
                        index++;
                        between=true;
                    }else if(section==LEADING){
                        between=true;
                    }
                }else{
                    Section section=SwingUtilities2.liesInVertical(rect,p,true);
                    if(section==LEADING){
                        between=true;
                    }else if(section==TRAILING){
                        index++;
                        between=true;
                    }
                }
                location=new DropLocation(p,index,between);
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
        DropLocation listLocation=(DropLocation)location;
        if(dropMode==DropMode.USE_SELECTION){
            if(listLocation==null){
                if(!forDrop&&state!=null){
                    setSelectedIndices(((int[][])state)[0]);
                    int anchor=((int[][])state)[1][0];
                    int lead=((int[][])state)[1][1];
                    SwingUtilities2.setLeadAnchorWithoutSelection(
                            getSelectionModel(),lead,anchor);
                }
            }else{
                if(dropLocation==null){
                    int[] inds=getSelectedIndices();
                    retVal=new int[][]{inds,{getAnchorSelectionIndex(),
                            getLeadSelectionIndex()}};
                }else{
                    retVal=state;
                }
                int index=listLocation.getIndex();
                if(index==-1){
                    clearSelection();
                    getSelectionModel().setAnchorSelectionIndex(-1);
                    getSelectionModel().setLeadSelectionIndex(-1);
                }else{
                    setSelectionInterval(index,index);
                }
            }
        }
        DropLocation old=dropLocation;
        dropLocation=listLocation;
        firePropertyChange("dropLocation",old,dropLocation);
        return retVal;
    }

    public int getAnchorSelectionIndex(){
        return getSelectionModel().getAnchorSelectionIndex();
    }

    public int getLeadSelectionIndex(){
        return getSelectionModel().getLeadSelectionIndex();
    }

    public void clearSelection(){
        getSelectionModel().clearSelection();
    }    private void updateFixedCellSize(){
        ListCellRenderer<? super E> cr=getCellRenderer();
        E value=getPrototypeCellValue();
        if((cr!=null)&&(value!=null)){
            Component c=cr.getListCellRendererComponent(this,value,0,false,false);
            /** The ListUI implementation will add Component c to its private
             * CellRendererPane however we can't assume that's already
             * been done here.  So we temporarily set the one "inherited"
             * property that may affect the renderer components preferred size:
             * its font.
             */
            Font f=c.getFont();
            c.setFont(getFont());
            Dimension d=c.getPreferredSize();
            fixedCellWidth=d.width;
            fixedCellHeight=d.height;
            c.setFont(f);
        }
    }

    public void setSelectionInterval(int anchor,int lead){
        getSelectionModel().setSelectionInterval(anchor,lead);
    }    public E getPrototypeCellValue(){
        return prototypeCellValue;
    }

    @Transient
    public int[] getSelectedIndices(){
        ListSelectionModel sm=getSelectionModel();
        int iMin=sm.getMinSelectionIndex();
        int iMax=sm.getMaxSelectionIndex();
        if((iMin<0)||(iMax<0)){
            return new int[0];
        }
        int[] rvTmp=new int[1+(iMax-iMin)];
        int n=0;
        for(int i=iMin;i<=iMax;i++){
            if(sm.isSelectedIndex(i)){
                rvTmp[n++]=i;
            }
        }
        int[] rv=new int[n];
        System.arraycopy(rvTmp,0,rv,0,n);
        return rv;
    }    public void setPrototypeCellValue(E prototypeCellValue){
        E oldValue=this.prototypeCellValue;
        this.prototypeCellValue=prototypeCellValue;
        /** If the prototypeCellValue has changed and is non-null,
         * then recompute fixedCellWidth and fixedCellHeight.
         */
        if((prototypeCellValue!=null)&&!prototypeCellValue.equals(oldValue)){
            updateFixedCellSize();
        }
        firePropertyChange("prototypeCellValue",oldValue,prototypeCellValue);
    }

    public void setSelectedIndices(int[] indices){
        ListSelectionModel sm=getSelectionModel();
        sm.clearSelection();
        int size=getModel().getSize();
        for(int i : indices){
            if(i<size){
                sm.addSelectionInterval(i,i);
            }
        }
    }

    public ListSelectionModel getSelectionModel(){
        return selectionModel;
    }

    public void setSelectionModel(ListSelectionModel selectionModel){
        if(selectionModel==null){
            throw new IllegalArgumentException("selectionModel must be non null");
        }
        /** Remove the forwarding ListSelectionListener from the old
         * selectionModel, and add it to the new one, if necessary.
         */
        if(selectionListener!=null){
            this.selectionModel.removeListSelectionListener(selectionListener);
            selectionModel.addListSelectionListener(selectionListener);
        }
        ListSelectionModel oldValue=this.selectionModel;
        this.selectionModel=selectionModel;
        firePropertyChange("selectionModel",oldValue,selectionModel);
    }

    protected String paramString(){
        String selectionForegroundString=(selectionForeground!=null?
                selectionForeground.toString():
                "");
        String selectionBackgroundString=(selectionBackground!=null?
                selectionBackground.toString():
                "");
        return super.paramString()+
                ",fixedCellHeight="+fixedCellHeight+
                ",fixedCellWidth="+fixedCellWidth+
                ",horizontalScrollIncrement="+horizontalScrollIncrement+
                ",selectionBackground="+selectionBackgroundString+
                ",selectionForeground="+selectionForegroundString+
                ",visibleRowCount="+visibleRowCount+
                ",layoutOrientation="+layoutOrientation;
    }

    public ListModel<E> getModel(){
        return dataModel;
    }

    public void setModel(ListModel<E> model){
        if(model==null){
            throw new IllegalArgumentException("model must be non null");
        }
        ListModel<E> oldValue=dataModel;
        dataModel=model;
        firePropertyChange("model",oldValue,dataModel);
        clearSelection();
    }

    @Transient
    public ListCellRenderer<? super E> getCellRenderer(){
        return cellRenderer;
    }

    public void setCellRenderer(ListCellRenderer<? super E> cellRenderer){
        ListCellRenderer<? super E> oldValue=this.cellRenderer;
        this.cellRenderer=cellRenderer;
        /** If the cellRenderer has changed and prototypeCellValue
         * was set, then recompute fixedCellWidth and fixedCellHeight.
         */
        if((cellRenderer!=null)&&!cellRenderer.equals(oldValue)){
            updateFixedCellSize();
        }
        firePropertyChange("cellRenderer",oldValue,cellRenderer);
    }

    protected ListSelectionModel createSelectionModel(){
        return new DefaultListSelectionModel();
    }

    public JList(final Vector<? extends E> listData){
        this(
                new AbstractListModel<E>(){
                    public int getSize(){
                        return listData.size();
                    }

                    public E getElementAt(int i){
                        return listData.elementAt(i);
                    }
                }
        );
    }

    public JList(){
        this(
                new AbstractListModel<E>(){
                    public int getSize(){
                        return 0;
                    }

                    public E getElementAt(int i){
                        throw new IndexOutOfBoundsException("No Data Model");
                    }
                }
        );
    }

    public Color getSelectionForeground(){
        return selectionForeground;
    }

    public void setSelectionForeground(Color selectionForeground){
        Color oldValue=this.selectionForeground;
        this.selectionForeground=selectionForeground;
        firePropertyChange("selectionForeground",oldValue,selectionForeground);
    }

    public Color getSelectionBackground(){
        return selectionBackground;
    }

    public void setSelectionBackground(Color selectionBackground){
        Color oldValue=this.selectionBackground;
        this.selectionBackground=selectionBackground;
        firePropertyChange("selectionBackground",oldValue,selectionBackground);
    }

    public int getLastVisibleIndex(){
        boolean leftToRight=this.getComponentOrientation().isLeftToRight();
        Rectangle r=getVisibleRect();
        Point lastPoint;
        if(leftToRight){
            lastPoint=new Point((r.x+r.width)-1,(r.y+r.height)-1);
        }else{
            lastPoint=new Point(r.x,(r.y+r.height)-1);
        }
        int location=locationToIndex(lastPoint);
        if(location!=-1){
            Rectangle bounds=getCellBounds(location,location);
            if(bounds!=null){
                SwingUtilities.computeIntersection(r.x,r.y,r.width,r.height,bounds);
                if(bounds.width==0||bounds.height==0){
                    // Try the top left(LTR) or top right(RTL) corner, and
                    // then go across checking each cell for HORIZONTAL_WRAP.
                    // Try the lower left corner, and then go across checking
                    // each cell for other list layout orientation.
                    boolean isHorizontalWrap=
                            (getLayoutOrientation()==HORIZONTAL_WRAP);
                    Point visibleLocation=isHorizontalWrap?
                            new Point(lastPoint.x,r.y):
                            new Point(r.x,lastPoint.y);
                    int last;
                    int visIndex=-1;
                    int lIndex=location;
                    location=-1;
                    do{
                        last=visIndex;
                        visIndex=locationToIndex(visibleLocation);
                        if(visIndex!=-1){
                            bounds=getCellBounds(visIndex,visIndex);
                            if(visIndex!=lIndex&&bounds!=null&&
                                    bounds.contains(visibleLocation)){
                                location=visIndex;
                                if(isHorizontalWrap){
                                    visibleLocation.y=bounds.y+bounds.height;
                                    if(visibleLocation.y>=lastPoint.y){
                                        // Past visible region, bail.
                                        last=visIndex;
                                    }
                                }else{
                                    visibleLocation.x=bounds.x+bounds.width;
                                    if(visibleLocation.x>=lastPoint.x){
                                        // Past visible region, bail.
                                        last=visIndex;
                                    }
                                }
                            }else{
                                last=visIndex;
                            }
                        }
                    }while(visIndex!=-1&&last!=visIndex);
                }
            }
        }
        return location;
    }

    public int getLayoutOrientation(){
        return layoutOrientation;
    }

    public void setLayoutOrientation(int layoutOrientation){
        int oldValue=this.layoutOrientation;
        switch(layoutOrientation){
            case VERTICAL:
            case VERTICAL_WRAP:
            case HORIZONTAL_WRAP:
                this.layoutOrientation=layoutOrientation;
                firePropertyChange("layoutOrientation",oldValue,layoutOrientation);
                break;
            default:
                throw new IllegalArgumentException("layoutOrientation must be one of: VERTICAL, HORIZONTAL_WRAP or VERTICAL_WRAP");
        }
    }

    public int locationToIndex(Point location){
        ListUI ui=getUI();
        return (ui!=null)?ui.locationToIndex(this,location):-1;
    }

    public ListUI getUI(){
        return (ListUI)ui;
    }

    public void setUI(ListUI ui){
        super.setUI(ui);
    }

    public Rectangle getCellBounds(int index0,int index1){
        ListUI ui=getUI();
        return (ui!=null)?ui.getCellBounds(this,index0,index1):null;
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
        throw new IllegalArgumentException(dropMode+": Unsupported drop mode for list");
    }

    public final DropLocation getDropLocation(){
        return dropLocation;
    }

    public int getNextMatch(String prefix,int startIndex,Position.Bias bias){
        ListModel<E> model=getModel();
        int max=model.getSize();
        if(prefix==null){
            throw new IllegalArgumentException();
        }
        if(startIndex<0||startIndex>=max){
            throw new IllegalArgumentException();
        }
        prefix=prefix.toUpperCase();
        // start search from the next element after the selected element
        int increment=(bias==Position.Bias.Forward)?1:-1;
        int index=startIndex;
        do{
            E element=model.getElementAt(index);
            if(element!=null){
                String string;
                if(element instanceof String){
                    string=((String)element).toUpperCase();
                }else{
                    string=element.toString();
                    if(string!=null){
                        string=string.toUpperCase();
                    }
                }
                if(string!=null&&string.startsWith(prefix)){
                    return index;
                }
            }
            index=(index+increment+max)%max;
        }while(index!=startIndex);
        return -1;
    }

    public Point indexToLocation(int index){
        ListUI ui=getUI();
        return (ui!=null)?ui.indexToLocation(this,index):null;
    }

    public void setListData(final E[] listData){
        setModel(
                new AbstractListModel<E>(){
                    public int getSize(){
                        return listData.length;
                    }

                    public E getElementAt(int i){
                        return listData[i];
                    }
                }
        );
    }

    public void setListData(final Vector<? extends E> listData){
        setModel(
                new AbstractListModel<E>(){
                    public int getSize(){
                        return listData.size();
                    }

                    public E getElementAt(int i){
                        return listData.elementAt(i);
                    }
                }
        );
    }

    protected void fireSelectionValueChanged(int firstIndex,int lastIndex,
                                             boolean isAdjusting){
        Object[] listeners=listenerList.getListenerList();
        ListSelectionEvent e=null;
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ListSelectionListener.class){
                if(e==null){
                    e=new ListSelectionEvent(this,firstIndex,lastIndex,
                            isAdjusting);
                }
                ((ListSelectionListener)listeners[i+1]).valueChanged(e);
            }
        }
    }

    public void addListSelectionListener(ListSelectionListener listener){
        if(selectionListener==null){
            selectionListener=new ListSelectionHandler();
            getSelectionModel().addListSelectionListener(selectionListener);
        }
        listenerList.add(ListSelectionListener.class,listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener){
        listenerList.remove(ListSelectionListener.class,listener);
    }

    public ListSelectionListener[] getListSelectionListeners(){
        return listenerList.getListeners(ListSelectionListener.class);
    }

    public int getSelectionMode(){
        return getSelectionModel().getSelectionMode();
    }

    public void setSelectionMode(int selectionMode){
        getSelectionModel().setSelectionMode(selectionMode);
    }

    public int getMaxSelectionIndex(){
        return getSelectionModel().getMaxSelectionIndex();
    }

    public boolean isSelectedIndex(int index){
        return getSelectionModel().isSelectedIndex(index);
    }

    public boolean isSelectionEmpty(){
        return getSelectionModel().isSelectionEmpty();
    }

    public void addSelectionInterval(int anchor,int lead){
        getSelectionModel().addSelectionInterval(anchor,lead);
    }

    public void removeSelectionInterval(int index0,int index1){
        getSelectionModel().removeSelectionInterval(index0,index1);
    }

    public boolean getValueIsAdjusting(){
        return getSelectionModel().getValueIsAdjusting();
    }

    public void setValueIsAdjusting(boolean b){
        getSelectionModel().setValueIsAdjusting(b);
    }

    @Deprecated
    public Object[] getSelectedValues(){
        ListSelectionModel sm=getSelectionModel();
        ListModel<E> dm=getModel();
        int iMin=sm.getMinSelectionIndex();
        int iMax=sm.getMaxSelectionIndex();
        if((iMin<0)||(iMax<0)){
            return new Object[0];
        }
        Object[] rvTmp=new Object[1+(iMax-iMin)];
        int n=0;
        for(int i=iMin;i<=iMax;i++){
            if(sm.isSelectedIndex(i)){
                rvTmp[n++]=dm.getElementAt(i);
            }
        }
        Object[] rv=new Object[n];
        System.arraycopy(rvTmp,0,rv,0,n);
        return rv;
    }

    public List<E> getSelectedValuesList(){
        ListSelectionModel sm=getSelectionModel();
        ListModel<E> dm=getModel();
        int iMin=sm.getMinSelectionIndex();
        int iMax=sm.getMaxSelectionIndex();
        if((iMin<0)||(iMax<0)){
            return Collections.emptyList();
        }
        List<E> selectedItems=new ArrayList<E>();
        for(int i=iMin;i<=iMax;i++){
            if(sm.isSelectedIndex(i)){
                selectedItems.add(dm.getElementAt(i));
            }
        }
        return selectedItems;
    }

    public int getSelectedIndex(){
        return getMinSelectionIndex();
    }

    public int getMinSelectionIndex(){
        return getSelectionModel().getMinSelectionIndex();
    }

    public void setSelectedIndex(int index){
        if(index>=getModel().getSize()){
            return;
        }
        getSelectionModel().setSelectionInterval(index,index);
    }

    public void setSelectedValue(Object anObject,boolean shouldScroll){
        if(anObject==null)
            setSelectedIndex(-1);
        else if(!anObject.equals(getSelectedValue())){
            int i, c;
            ListModel<E> dm=getModel();
            for(i=0,c=dm.getSize();i<c;i++)
                if(anObject.equals(dm.getElementAt(i))){
                    setSelectedIndex(i);
                    if(shouldScroll)
                        ensureIndexIsVisible(i);
                    repaint();  /** FIX-ME setSelectedIndex does not redraw all the time with the basic l&f**/
                    return;
                }
            setSelectedIndex(-1);
        }
        repaint(); /** FIX-ME setSelectedIndex does not redraw all the time with the basic l&f**/
    }

    public void ensureIndexIsVisible(int index){
        Rectangle cellBounds=getCellBounds(index,index);
        if(cellBounds!=null){
            scrollRectToVisible(cellBounds);
        }
    }

    public E getSelectedValue(){
        int i=getMinSelectionIndex();
        return (i==-1)?null:getModel().getElementAt(i);
    }

    public Dimension getPreferredScrollableViewportSize(){
        if(getLayoutOrientation()!=VERTICAL){
            return getPreferredSize();
        }
        Insets insets=getInsets();
        int dx=insets.left+insets.right;
        int dy=insets.top+insets.bottom;
        int visibleRowCount=getVisibleRowCount();
        int fixedCellWidth=getFixedCellWidth();
        int fixedCellHeight=getFixedCellHeight();
        if((fixedCellWidth>0)&&(fixedCellHeight>0)){
            int width=fixedCellWidth+dx;
            int height=(visibleRowCount*fixedCellHeight)+dy;
            return new Dimension(width,height);
        }else if(getModel().getSize()>0){
            int width=getPreferredSize().width;
            int height;
            Rectangle r=getCellBounds(0,0);
            if(r!=null){
                height=(visibleRowCount*r.height)+dy;
            }else{
                // Will only happen if UI null, shouldn't matter what we return
                height=1;
            }
            return new Dimension(width,height);
        }else{
            fixedCellWidth=(fixedCellWidth>0)?fixedCellWidth:256;
            fixedCellHeight=(fixedCellHeight>0)?fixedCellHeight:16;
            return new Dimension(fixedCellWidth,fixedCellHeight*visibleRowCount);
        }
    }

    public int getFixedCellWidth(){
        return fixedCellWidth;
    }

    public void setFixedCellWidth(int width){
        int oldValue=fixedCellWidth;
        fixedCellWidth=width;
        firePropertyChange("fixedCellWidth",oldValue,fixedCellWidth);
    }

    public int getFixedCellHeight(){
        return fixedCellHeight;
    }

    public void setFixedCellHeight(int height){
        int oldValue=fixedCellHeight;
        fixedCellHeight=height;
        firePropertyChange("fixedCellHeight",oldValue,fixedCellHeight);
    }

    public int getVisibleRowCount(){
        return visibleRowCount;
    }

    public void setVisibleRowCount(int visibleRowCount){
        int oldValue=this.visibleRowCount;
        this.visibleRowCount=Math.max(0,visibleRowCount);
        firePropertyChange("visibleRowCount",oldValue,visibleRowCount);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction){
        checkScrollableParameters(visibleRect,orientation);
        if(orientation==SwingConstants.VERTICAL){
            int row=locationToIndex(visibleRect.getLocation());
            if(row==-1){
                return 0;
            }else{
                /** Scroll Down */
                if(direction>0){
                    Rectangle r=getCellBounds(row,row);
                    return (r==null)?0:r.height-(visibleRect.y-r.y);
                }
                /** Scroll Up */
                else{
                    Rectangle r=getCellBounds(row,row);
                    /** The first row is completely visible and it's row 0.
                     * We're done.
                     */
                    if((r.y==visibleRect.y)&&(row==0)){
                        return 0;
                    }
                    /** The first row is completely visible, return the
                     * height of the previous row or 0 if the first row
                     * is the top row of the list.
                     */
                    else if(r.y==visibleRect.y){
                        Point loc=r.getLocation();
                        loc.y--;
                        int prevIndex=locationToIndex(loc);
                        Rectangle prevR=getCellBounds(prevIndex,prevIndex);
                        if(prevR==null||prevR.y>=r.y){
                            return 0;
                        }
                        return prevR.height;
                    }
                    /** The first row is partially visible, return the
                     * height of hidden part.
                     */
                    else{
                        return visibleRect.y-r.y;
                    }
                }
            }
        }else if(orientation==SwingConstants.HORIZONTAL&&
                getLayoutOrientation()!=JList.VERTICAL){
            boolean leftToRight=getComponentOrientation().isLeftToRight();
            int index;
            Point leadingPoint;
            if(leftToRight){
                leadingPoint=visibleRect.getLocation();
            }else{
                leadingPoint=new Point(visibleRect.x+visibleRect.width-1,
                        visibleRect.y);
            }
            index=locationToIndex(leadingPoint);
            if(index!=-1){
                Rectangle cellBounds=getCellBounds(index,index);
                if(cellBounds!=null&&cellBounds.contains(leadingPoint)){
                    int leadingVisibleEdge;
                    int leadingCellEdge;
                    if(leftToRight){
                        leadingVisibleEdge=visibleRect.x;
                        leadingCellEdge=cellBounds.x;
                    }else{
                        leadingVisibleEdge=visibleRect.x+visibleRect.width;
                        leadingCellEdge=cellBounds.x+cellBounds.width;
                    }
                    if(leadingCellEdge!=leadingVisibleEdge){
                        if(direction<0){
                            // Show remainder of leading cell
                            return Math.abs(leadingVisibleEdge-leadingCellEdge);
                        }else if(leftToRight){
                            // Hide rest of leading cell
                            return leadingCellEdge+cellBounds.width-leadingVisibleEdge;
                        }else{
                            // Hide rest of leading cell
                            return leadingVisibleEdge-cellBounds.x;
                        }
                    }
                    // ASSUME: All cells are the same width
                    return cellBounds.width;
                }
            }
        }
        Font f=getFont();
        return (f!=null)?f.getSize():1;
    }

    private void checkScrollableParameters(Rectangle visibleRect,int orientation){
        if(visibleRect==null){
            throw new IllegalArgumentException("visibleRect must be non-null");
        }
        switch(orientation){
            case SwingConstants.VERTICAL:
            case SwingConstants.HORIZONTAL:
                break;
            default:
                throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation,int direction){
        checkScrollableParameters(visibleRect,orientation);
        if(orientation==SwingConstants.VERTICAL){
            int inc=visibleRect.height;
            /** Scroll Down */
            if(direction>0){
                // last cell is the lowest left cell
                int last=locationToIndex(new Point(visibleRect.x,visibleRect.y+visibleRect.height-1));
                if(last!=-1){
                    Rectangle lastRect=getCellBounds(last,last);
                    if(lastRect!=null){
                        inc=lastRect.y-visibleRect.y;
                        if((inc==0)&&(last<getModel().getSize()-1)){
                            inc=lastRect.height;
                        }
                    }
                }
            }
            /** Scroll Up */
            else{
                int newFirst=locationToIndex(new Point(visibleRect.x,visibleRect.y-visibleRect.height));
                int first=getFirstVisibleIndex();
                if(newFirst!=-1){
                    if(first==-1){
                        first=locationToIndex(visibleRect.getLocation());
                    }
                    Rectangle newFirstRect=getCellBounds(newFirst,newFirst);
                    Rectangle firstRect=getCellBounds(first,first);
                    if((newFirstRect!=null)&&(firstRect!=null)){
                        while((newFirstRect.y+visibleRect.height<
                                firstRect.y+firstRect.height)&&
                                (newFirstRect.y<firstRect.y)){
                            newFirst++;
                            newFirstRect=getCellBounds(newFirst,newFirst);
                        }
                        inc=visibleRect.y-newFirstRect.y;
                        if((inc<=0)&&(newFirstRect.y>0)){
                            newFirst--;
                            newFirstRect=getCellBounds(newFirst,newFirst);
                            if(newFirstRect!=null){
                                inc=visibleRect.y-newFirstRect.y;
                            }
                        }
                    }
                }
            }
            return inc;
        }else if(orientation==SwingConstants.HORIZONTAL&&
                getLayoutOrientation()!=JList.VERTICAL){
            boolean leftToRight=getComponentOrientation().isLeftToRight();
            int inc=visibleRect.width;
            /** Scroll Right (in ltr mode) or Scroll Left (in rtl mode) */
            if(direction>0){
                // position is upper right if ltr, or upper left otherwise
                int x=visibleRect.x+(leftToRight?(visibleRect.width-1):0);
                int last=locationToIndex(new Point(x,visibleRect.y));
                if(last!=-1){
                    Rectangle lastRect=getCellBounds(last,last);
                    if(lastRect!=null){
                        if(leftToRight){
                            inc=lastRect.x-visibleRect.x;
                        }else{
                            inc=visibleRect.x+visibleRect.width
                                    -(lastRect.x+lastRect.width);
                        }
                        if(inc<0){
                            inc+=lastRect.width;
                        }else if((inc==0)&&(last<getModel().getSize()-1)){
                            inc=lastRect.width;
                        }
                    }
                }
            }
            /** Scroll Left (in ltr mode) or Scroll Right (in rtl mode) */
            else{
                // position is upper left corner of the visibleRect shifted
                // left by the visibleRect.width if ltr, or upper right shifted
                // right by the visibleRect.width otherwise
                int x=visibleRect.x+(leftToRight
                        ?-visibleRect.width
                        :visibleRect.width-1+visibleRect.width);
                int first=locationToIndex(new Point(x,visibleRect.y));
                if(first!=-1){
                    Rectangle firstRect=getCellBounds(first,first);
                    if(firstRect!=null){
                        // the right of the first cell
                        int firstRight=firstRect.x+firstRect.width;
                        if(leftToRight){
                            if((firstRect.x<visibleRect.x-visibleRect.width)
                                    &&(firstRight<visibleRect.x)){
                                inc=visibleRect.x-firstRight;
                            }else{
                                inc=visibleRect.x-firstRect.x;
                            }
                        }else{
                            int visibleRight=visibleRect.x+visibleRect.width;
                            if((firstRight>visibleRight+visibleRect.width)
                                    &&(firstRect.x>visibleRight)){
                                inc=firstRect.x-visibleRight;
                            }else{
                                inc=firstRight-visibleRight;
                            }
                        }
                    }
                }
            }
            return inc;
        }
        return visibleRect.width;
    }

    public int getFirstVisibleIndex(){
        Rectangle r=getVisibleRect();
        int first;
        if(this.getComponentOrientation().isLeftToRight()){
            first=locationToIndex(r.getLocation());
        }else{
            first=locationToIndex(new Point((r.x+r.width)-1,r.y));
        }
        if(first!=-1){
            Rectangle bounds=getCellBounds(first,first);
            if(bounds!=null){
                SwingUtilities.computeIntersection(r.x,r.y,r.width,r.height,bounds);
                if(bounds.width==0||bounds.height==0){
                    first=-1;
                }
            }
        }
        return first;
    }

    public boolean getScrollableTracksViewportWidth(){
        if(getLayoutOrientation()==HORIZONTAL_WRAP&&
                getVisibleRowCount()<=0){
            return true;
        }
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            return parent.getWidth()>getPreferredSize().width;
        }
        return false;
    }

    public boolean getScrollableTracksViewportHeight(){
        if(getLayoutOrientation()==VERTICAL_WRAP&&
                getVisibleRowCount()<=0){
            return true;
        }
        Container parent=SwingUtilities.getUnwrappedParent(this);
        if(parent instanceof JViewport){
            return parent.getHeight()>getPreferredSize().height;
        }
        return false;
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

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJList();
        }
        return accessibleContext;
    }

    public static final class DropLocation extends TransferHandler.DropLocation{
        private final int index;
        private final boolean isInsert;

        private DropLocation(Point p,int index,boolean isInsert){
            super(p);
            this.index=index;
            this.isInsert=isInsert;
        }

        public int getIndex(){
            return index;
        }

        public boolean isInsert(){
            return isInsert;
        }

        public String toString(){
            return getClass().getName()
                    +"[dropPoint="+getDropPoint()+","
                    +"index="+index+","
                    +"insert="+isInsert+"]";
        }
    }

    private class ListSelectionHandler implements ListSelectionListener, Serializable{
        public void valueChanged(ListSelectionEvent e){
            fireSelectionValueChanged(e.getFirstIndex(),
                    e.getLastIndex(),
                    e.getValueIsAdjusting());
        }
    }

    protected class AccessibleJList extends AccessibleJComponent
            implements AccessibleSelection, PropertyChangeListener,
            ListSelectionListener, ListDataListener{
        int leadSelectionIndex;

        public AccessibleJList(){
            super();
            JList.this.addPropertyChangeListener(this);
            JList.this.getSelectionModel().addListSelectionListener(this);
            JList.this.getModel().addListDataListener(this);
            leadSelectionIndex=JList.this.getLeadSelectionIndex();
        }

        public void propertyChange(PropertyChangeEvent e){
            String name=e.getPropertyName();
            Object oldValue=e.getOldValue();
            Object newValue=e.getNewValue();
            // re-set listData listeners
            if(name.compareTo("model")==0){
                if(oldValue!=null&&oldValue instanceof ListModel){
                    ((ListModel)oldValue).removeListDataListener(this);
                }
                if(newValue!=null&&newValue instanceof ListModel){
                    ((ListModel)newValue).addListDataListener(this);
                }
                // re-set listSelectionModel listeners
            }else if(name.compareTo("selectionModel")==0){
                if(oldValue!=null&&oldValue instanceof ListSelectionModel){
                    ((ListSelectionModel)oldValue).removeListSelectionListener(this);
                }
                if(newValue!=null&&newValue instanceof ListSelectionModel){
                    ((ListSelectionModel)newValue).addListSelectionListener(this);
                }
                firePropertyChange(
                        AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                        Boolean.valueOf(false),Boolean.valueOf(true));
            }
        }

        public void intervalAdded(ListDataEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
        }        public void valueChanged(ListSelectionEvent e){
            int oldLeadSelectionIndex=leadSelectionIndex;
            leadSelectionIndex=JList.this.getLeadSelectionIndex();
            if(oldLeadSelectionIndex!=leadSelectionIndex){
                Accessible oldLS, newLS;
                oldLS=(oldLeadSelectionIndex>=0)
                        ?getAccessibleChild(oldLeadSelectionIndex)
                        :null;
                newLS=(leadSelectionIndex>=0)
                        ?getAccessibleChild(leadSelectionIndex)
                        :null;
                firePropertyChange(AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY,
                        oldLS,newLS);
            }
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
            firePropertyChange(AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
            // Process the State changes for Multiselectable
            AccessibleStateSet s=getAccessibleStateSet();
            ListSelectionModel lsm=JList.this.getSelectionModel();
            if(lsm.getSelectionMode()!=ListSelectionModel.SINGLE_SELECTION){
                if(!s.contains(AccessibleState.MULTISELECTABLE)){
                    s.add(AccessibleState.MULTISELECTABLE);
                    firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            null,AccessibleState.MULTISELECTABLE);
                }
            }else{
                if(s.contains(AccessibleState.MULTISELECTABLE)){
                    s.remove(AccessibleState.MULTISELECTABLE);
                    firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            AccessibleState.MULTISELECTABLE,null);
                }
            }
        }

        public void intervalRemoved(ListDataEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
        }

        public void contentsChanged(ListDataEvent e){
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    Boolean.valueOf(false),Boolean.valueOf(true));
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.LIST;
        }
        // AccessibleContext methods

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            if(selectionModel.getSelectionMode()!=
                    ListSelectionModel.SINGLE_SELECTION){
                states.add(AccessibleState.MULTISELECTABLE);
            }
            return states;
        }

        public int getAccessibleChildrenCount(){
            return getModel().getSize();
        }

        public Accessible getAccessibleChild(int i){
            if(i>=getModel().getSize()){
                return null;
            }else{
                return new AccessibleJListChild(JList.this,i);
            }
        }

        public Accessible getAccessibleAt(Point p){
            int i=locationToIndex(p);
            if(i>=0){
                return new AccessibleJListChild(JList.this,i);
            }else{
                return null;
            }
        }

        protected class AccessibleJListChild extends AccessibleContext
                implements Accessible, AccessibleComponent{
            private JList<E> parent=null;
            private int indexInParent;
            private Component component=null;
            private AccessibleContext accessibleContext=null;
            private ListModel<E> listModel;
            private ListCellRenderer<? super E> cellRenderer=null;

            public AccessibleJListChild(JList<E> parent,int indexInParent){
                this.parent=parent;
                this.setAccessibleParent(parent);
                this.indexInParent=indexInParent;
                if(parent!=null){
                    listModel=parent.getModel();
                    cellRenderer=parent.getCellRenderer();
                }
            }

            // Accessible Methods
            public AccessibleContext getAccessibleContext(){
                return this;
            }

            public String getAccessibleName(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getAccessibleName();
                }else{
                    return null;
                }
            }

            private AccessibleContext getCurrentAccessibleContext(){
                Component c=getComponentAtIndex(indexInParent);
                if(c instanceof Accessible){
                    return c.getAccessibleContext();
                }else{
                    return null;
                }
            }

            private Component getComponentAtIndex(int index){
                if(index<0||index>=listModel.getSize()){
                    return null;
                }
                if((parent!=null)
                        &&(listModel!=null)
                        &&cellRenderer!=null){
                    E value=listModel.getElementAt(index);
                    boolean isSelected=parent.isSelectedIndex(index);
                    boolean isFocussed=parent.isFocusOwner()
                            &&(index==parent.getLeadSelectionIndex());
                    return cellRenderer.getListCellRendererComponent(
                            parent,
                            value,
                            index,
                            isSelected,
                            isFocussed);
                }else{
                    return null;
                }
            }
            // AccessibleContext methods

            public void setAccessibleName(String s){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    ac.setAccessibleName(s);
                }
            }

            public String getAccessibleDescription(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getAccessibleDescription();
                }else{
                    return null;
                }
            }

            public void setAccessibleDescription(String s){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    ac.setAccessibleDescription(s);
                }
            }

            public AccessibleRole getAccessibleRole(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getAccessibleRole();
                }else{
                    return null;
                }
            }

            public AccessibleStateSet getAccessibleStateSet(){
                AccessibleContext ac=getCurrentAccessibleContext();
                AccessibleStateSet s;
                if(ac!=null){
                    s=ac.getAccessibleStateSet();
                }else{
                    s=new AccessibleStateSet();
                }
                s.add(AccessibleState.SELECTABLE);
                if(parent.isFocusOwner()
                        &&(indexInParent==parent.getLeadSelectionIndex())){
                    s.add(AccessibleState.ACTIVE);
                }
                if(parent.isSelectedIndex(indexInParent)){
                    s.add(AccessibleState.SELECTED);
                }
                if(this.isShowing()){
                    s.add(AccessibleState.SHOWING);
                }else if(s.contains(AccessibleState.SHOWING)){
                    s.remove(AccessibleState.SHOWING);
                }
                if(this.isVisible()){
                    s.add(AccessibleState.VISIBLE);
                }else if(s.contains(AccessibleState.VISIBLE)){
                    s.remove(AccessibleState.VISIBLE);
                }
                s.add(AccessibleState.TRANSIENT); // cell-rendered
                return s;
            }

            public int getAccessibleIndexInParent(){
                return indexInParent;
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
                }
            }

            public void removePropertyChangeListener(PropertyChangeListener l){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    ac.removePropertyChangeListener(l);
                }
            }

            public AccessibleAction getAccessibleAction(){
                return getCurrentAccessibleContext().getAccessibleAction();
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

            private Component getCurrentComponent(){
                return getComponentAtIndex(indexInParent);
            }            public AccessibleSelection getAccessibleSelection(){
                return getCurrentAccessibleContext().getAccessibleSelection();
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
            }            public AccessibleText getAccessibleText(){
                return getCurrentAccessibleContext().getAccessibleText();
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
            }            public AccessibleValue getAccessibleValue(){
                return getCurrentAccessibleContext().getAccessibleValue();
            }
            // AccessibleComponent methods

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
                int fi=parent.getFirstVisibleIndex();
                int li=parent.getLastVisibleIndex();
                // The UI incorrectly returns a -1 for the last
                // visible index if the list is smaller than the
                // viewport size.
                if(li==-1){
                    li=parent.getModel().getSize()-1;
                }
                return ((indexInParent>=fi)
                        &&(indexInParent<=li));
            }

            public void setVisible(boolean b){
            }

            public boolean isShowing(){
                return (parent.isShowing()&&isVisible());
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
                    Point listLocation=parent.getLocationOnScreen();
                    Point componentLocation=parent.indexToLocation(indexInParent);
                    if(componentLocation!=null){
                        componentLocation.translate(listLocation.x,listLocation.y);
                        return componentLocation;
                    }else{
                        return null;
                    }
                }else{
                    return null;
                }
            }

            public Point getLocation(){
                if(parent!=null){
                    return parent.indexToLocation(indexInParent);
                }else{
                    return null;
                }
            }

            public void setLocation(Point p){
                if((parent!=null)&&(parent.contains(p))){
                    ensureIndexIsVisible(indexInParent);
                }
            }

            public Rectangle getBounds(){
                if(parent!=null){
                    return parent.getCellBounds(indexInParent,indexInParent);
                }else{
                    return null;
                }
            }

            public void setBounds(Rectangle r){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac instanceof AccessibleComponent){
                    ((AccessibleComponent)ac).setBounds(r);
                }
            }

            public Dimension getSize(){
                Rectangle cellBounds=this.getBounds();
                if(cellBounds!=null){
                    return cellBounds.getSize();
                }else{
                    return null;
                }
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







            // TIGER - 4733624
            public AccessibleIcon[] getAccessibleIcon(){
                AccessibleContext ac=getCurrentAccessibleContext();
                if(ac!=null){
                    return ac.getAccessibleIcon();
                }else{
                    return null;
                }
            }
        } // inner class AccessibleJListChild

        public AccessibleSelection getAccessibleSelection(){
            return this;
        }
        // AccessibleSelection methods

        public int getAccessibleSelectionCount(){
            return JList.this.getSelectedIndices().length;
        }

        public Accessible getAccessibleSelection(int i){
            int len=getAccessibleSelectionCount();
            if(i<0||i>=len){
                return null;
            }else{
                return getAccessibleChild(JList.this.getSelectedIndices()[i]);
            }
        }

        public boolean isAccessibleChildSelected(int i){
            return isSelectedIndex(i);
        }

        public void addAccessibleSelection(int i){
            JList.this.addSelectionInterval(i,i);
        }

        public void removeAccessibleSelection(int i){
            JList.this.removeSelectionInterval(i,i);
        }

        public void clearAccessibleSelection(){
            JList.this.clearSelection();
        }

        public void selectAllAccessibleSelection(){
            JList.this.addSelectionInterval(0,getAccessibleChildrenCount()-1);
        }


    } // inner class AccessibleJList






}
