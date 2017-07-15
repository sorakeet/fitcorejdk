/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRelation;
import javax.accessibility.AccessibleRole;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JScrollPane extends JComponent implements ScrollPaneConstants, Accessible{
    private static final String uiClassID="ScrollPaneUI";
    protected int verticalScrollBarPolicy=VERTICAL_SCROLLBAR_AS_NEEDED;
    protected int horizontalScrollBarPolicy=HORIZONTAL_SCROLLBAR_AS_NEEDED;
    protected JViewport viewport;
    protected JScrollBar verticalScrollBar;
    protected JScrollBar horizontalScrollBar;
    protected JViewport rowHeader;
    protected JViewport columnHeader;
    protected Component lowerLeft;
    protected Component lowerRight;
    protected Component upperLeft;
    protected Component upperRight;
    private Border viewportBorder;
    private boolean wheelScrollState=true;

    public JScrollPane(Component view){
        this(view,VERTICAL_SCROLLBAR_AS_NEEDED,HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public JScrollPane(Component view,int vsbPolicy,int hsbPolicy){
        setLayout(new ScrollPaneLayout.UIResource());
        setVerticalScrollBarPolicy(vsbPolicy);
        setHorizontalScrollBarPolicy(hsbPolicy);
        setViewport(createViewport());
        setVerticalScrollBar(createVerticalScrollBar());
        setHorizontalScrollBar(createHorizontalScrollBar());
        if(view!=null){
            setViewportView(view);
        }
        setUIProperty("opaque",true);
        updateUI();
        if(!this.getComponentOrientation().isLeftToRight()){
            viewport.setViewPosition(new Point(Integer.MAX_VALUE,0));
        }
    }

    public void updateUI(){
        setUI((ScrollPaneUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    @Override
    public boolean isValidateRoot(){
        return true;
    }

    protected String paramString(){
        String viewportBorderString=(viewportBorder!=null?
                viewportBorder.toString():"");
        String viewportString=(viewport!=null?
                viewport.toString():"");
        String verticalScrollBarPolicyString;
        if(verticalScrollBarPolicy==VERTICAL_SCROLLBAR_AS_NEEDED){
            verticalScrollBarPolicyString="VERTICAL_SCROLLBAR_AS_NEEDED";
        }else if(verticalScrollBarPolicy==VERTICAL_SCROLLBAR_NEVER){
            verticalScrollBarPolicyString="VERTICAL_SCROLLBAR_NEVER";
        }else if(verticalScrollBarPolicy==VERTICAL_SCROLLBAR_ALWAYS){
            verticalScrollBarPolicyString="VERTICAL_SCROLLBAR_ALWAYS";
        }else verticalScrollBarPolicyString="";
        String horizontalScrollBarPolicyString;
        if(horizontalScrollBarPolicy==HORIZONTAL_SCROLLBAR_AS_NEEDED){
            horizontalScrollBarPolicyString="HORIZONTAL_SCROLLBAR_AS_NEEDED";
        }else if(horizontalScrollBarPolicy==HORIZONTAL_SCROLLBAR_NEVER){
            horizontalScrollBarPolicyString="HORIZONTAL_SCROLLBAR_NEVER";
        }else if(horizontalScrollBarPolicy==HORIZONTAL_SCROLLBAR_ALWAYS){
            horizontalScrollBarPolicyString="HORIZONTAL_SCROLLBAR_ALWAYS";
        }else horizontalScrollBarPolicyString="";
        String horizontalScrollBarString=(horizontalScrollBar!=null?
                horizontalScrollBar.toString()
                :"");
        String verticalScrollBarString=(verticalScrollBar!=null?
                verticalScrollBar.toString():"");
        String columnHeaderString=(columnHeader!=null?
                columnHeader.toString():"");
        String rowHeaderString=(rowHeader!=null?
                rowHeader.toString():"");
        String lowerLeftString=(lowerLeft!=null?
                lowerLeft.toString():"");
        String lowerRightString=(lowerRight!=null?
                lowerRight.toString():"");
        String upperLeftString=(upperLeft!=null?
                upperLeft.toString():"");
        String upperRightString=(upperRight!=null?
                upperRight.toString():"");
        return super.paramString()+
                ",columnHeader="+columnHeaderString+
                ",horizontalScrollBar="+horizontalScrollBarString+
                ",horizontalScrollBarPolicy="+horizontalScrollBarPolicyString+
                ",lowerLeft="+lowerLeftString+
                ",lowerRight="+lowerRightString+
                ",rowHeader="+rowHeaderString+
                ",upperLeft="+upperLeftString+
                ",upperRight="+upperRightString+
                ",verticalScrollBar="+verticalScrollBarString+
                ",verticalScrollBarPolicy="+verticalScrollBarPolicyString+
                ",viewport="+viewportString+
                ",viewportBorder="+viewportBorderString;
    }

    public void setLayout(LayoutManager layout){
        if(layout instanceof ScrollPaneLayout){
            super.setLayout(layout);
            ((ScrollPaneLayout)layout).syncWithScrollPane(this);
        }else if(layout==null){
            super.setLayout(layout);
        }else{
            String s="layout of JScrollPane must be a ScrollPaneLayout";
            throw new ClassCastException(s);
        }
    }

    public JScrollBar createHorizontalScrollBar(){
        return new ScrollBar(JScrollBar.HORIZONTAL);
    }

    public JScrollBar createVerticalScrollBar(){
        return new ScrollBar(JScrollBar.VERTICAL);
    }

    protected JViewport createViewport(){
        return new JViewport();
    }

    public void setViewportView(Component view){
        if(getViewport()==null){
            setViewport(createViewport());
        }
        getViewport().setView(view);
    }

    public JScrollPane(int vsbPolicy,int hsbPolicy){
        this(null,vsbPolicy,hsbPolicy);
    }

    public JScrollPane(){
        this(null,VERTICAL_SCROLLBAR_AS_NEEDED,HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public ScrollPaneUI getUI(){
        return (ScrollPaneUI)ui;
    }

    public void setUI(ScrollPaneUI ui){
        super.setUI(ui);
    }

    public int getVerticalScrollBarPolicy(){
        return verticalScrollBarPolicy;
    }

    public void setVerticalScrollBarPolicy(int policy){
        switch(policy){
            case VERTICAL_SCROLLBAR_AS_NEEDED:
            case VERTICAL_SCROLLBAR_NEVER:
            case VERTICAL_SCROLLBAR_ALWAYS:
                break;
            default:
                throw new IllegalArgumentException("invalid verticalScrollBarPolicy");
        }
        int old=verticalScrollBarPolicy;
        verticalScrollBarPolicy=policy;
        firePropertyChange("verticalScrollBarPolicy",old,policy);
        revalidate();
        repaint();
    }

    public int getHorizontalScrollBarPolicy(){
        return horizontalScrollBarPolicy;
    }

    public void setHorizontalScrollBarPolicy(int policy){
        switch(policy){
            case HORIZONTAL_SCROLLBAR_AS_NEEDED:
            case HORIZONTAL_SCROLLBAR_NEVER:
            case HORIZONTAL_SCROLLBAR_ALWAYS:
                break;
            default:
                throw new IllegalArgumentException("invalid horizontalScrollBarPolicy");
        }
        int old=horizontalScrollBarPolicy;
        horizontalScrollBarPolicy=policy;
        firePropertyChange("horizontalScrollBarPolicy",old,policy);
        revalidate();
        repaint();
    }

    public Border getViewportBorder(){
        return viewportBorder;
    }    @Transient
    public JScrollBar getHorizontalScrollBar(){
        return horizontalScrollBar;
    }

    public void setViewportBorder(Border viewportBorder){
        Border oldValue=this.viewportBorder;
        this.viewportBorder=viewportBorder;
        firePropertyChange("viewportBorder",oldValue,viewportBorder);
    }    public void setHorizontalScrollBar(JScrollBar horizontalScrollBar){
        JScrollBar old=getHorizontalScrollBar();
        this.horizontalScrollBar=horizontalScrollBar;
        if(horizontalScrollBar!=null){
            add(horizontalScrollBar,HORIZONTAL_SCROLLBAR);
        }else if(old!=null){
            remove(old);
        }
        firePropertyChange("horizontalScrollBar",old,horizontalScrollBar);
        revalidate();
        repaint();
    }

    public Rectangle getViewportBorderBounds(){
        Rectangle borderR=new Rectangle(getSize());
        Insets insets=getInsets();
        borderR.x=insets.left;
        borderR.y=insets.top;
        borderR.width-=insets.left+insets.right;
        borderR.height-=insets.top+insets.bottom;
        boolean leftToRight=SwingUtilities.isLeftToRight(this);
        /** If there's a visible column header remove the space it
         * needs from the top of borderR.
         */
        JViewport colHead=getColumnHeader();
        if((colHead!=null)&&(colHead.isVisible())){
            int colHeadHeight=colHead.getHeight();
            borderR.y+=colHeadHeight;
            borderR.height-=colHeadHeight;
        }
        /** If there's a visible row header remove the space it needs
         * from the left of borderR.
         */
        JViewport rowHead=getRowHeader();
        if((rowHead!=null)&&(rowHead.isVisible())){
            int rowHeadWidth=rowHead.getWidth();
            if(leftToRight){
                borderR.x+=rowHeadWidth;
            }
            borderR.width-=rowHeadWidth;
        }
        /** If there's a visible vertical scrollbar remove the space it needs
         * from the width of borderR.
         */
        JScrollBar vsb=getVerticalScrollBar();
        if((vsb!=null)&&(vsb.isVisible())){
            int vsbWidth=vsb.getWidth();
            if(!leftToRight){
                borderR.x+=vsbWidth;
            }
            borderR.width-=vsbWidth;
        }
        /** If there's a visible horizontal scrollbar remove the space it needs
         * from the height of borderR.
         */
        JScrollBar hsb=getHorizontalScrollBar();
        if((hsb!=null)&&(hsb.isVisible())){
            borderR.height-=hsb.getHeight();
        }
        return borderR;
    }

    @Transient
    public JViewport getRowHeader(){
        return rowHeader;
    }    @Transient
    public JScrollBar getVerticalScrollBar(){
        return verticalScrollBar;
    }

    public void setRowHeader(JViewport rowHeader){
        JViewport old=getRowHeader();
        this.rowHeader=rowHeader;
        if(rowHeader!=null){
            add(rowHeader,ROW_HEADER);
        }else if(old!=null){
            remove(old);
        }
        firePropertyChange("rowHeader",old,rowHeader);
        revalidate();
        repaint();
    }    public void setVerticalScrollBar(JScrollBar verticalScrollBar){
        JScrollBar old=getVerticalScrollBar();
        this.verticalScrollBar=verticalScrollBar;
        add(verticalScrollBar,VERTICAL_SCROLLBAR);
        firePropertyChange("verticalScrollBar",old,verticalScrollBar);
        revalidate();
        repaint();
    }

    @Transient
    public JViewport getColumnHeader(){
        return columnHeader;
    }

    public void setColumnHeader(JViewport columnHeader){
        JViewport old=getColumnHeader();
        this.columnHeader=columnHeader;
        if(columnHeader!=null){
            add(columnHeader,COLUMN_HEADER);
        }else if(old!=null){
            remove(old);
        }
        firePropertyChange("columnHeader",old,columnHeader);
        revalidate();
        repaint();
    }    public JViewport getViewport(){
        return viewport;
    }

    public void setRowHeaderView(Component view){
        if(getRowHeader()==null){
            setRowHeader(createViewport());
        }
        getRowHeader().setView(view);
    }    public void setViewport(JViewport viewport){
        JViewport old=getViewport();
        this.viewport=viewport;
        if(viewport!=null){
            add(viewport,VIEWPORT);
        }else if(old!=null){
            remove(old);
        }
        firePropertyChange("viewport",old,viewport);
        if(accessibleContext!=null){
            ((AccessibleJScrollPane)accessibleContext).resetViewPort();
        }
        revalidate();
        repaint();
    }

    public void setColumnHeaderView(Component view){
        if(getColumnHeader()==null){
            setColumnHeader(createViewport());
        }
        getColumnHeader().setView(view);
    }

    public Component getCorner(String key){
        boolean isLeftToRight=getComponentOrientation().isLeftToRight();
        if(key.equals(LOWER_LEADING_CORNER)){
            key=isLeftToRight?LOWER_LEFT_CORNER:LOWER_RIGHT_CORNER;
        }else if(key.equals(LOWER_TRAILING_CORNER)){
            key=isLeftToRight?LOWER_RIGHT_CORNER:LOWER_LEFT_CORNER;
        }else if(key.equals(UPPER_LEADING_CORNER)){
            key=isLeftToRight?UPPER_LEFT_CORNER:UPPER_RIGHT_CORNER;
        }else if(key.equals(UPPER_TRAILING_CORNER)){
            key=isLeftToRight?UPPER_RIGHT_CORNER:UPPER_LEFT_CORNER;
        }
        if(key.equals(LOWER_LEFT_CORNER)){
            return lowerLeft;
        }else if(key.equals(LOWER_RIGHT_CORNER)){
            return lowerRight;
        }else if(key.equals(UPPER_LEFT_CORNER)){
            return upperLeft;
        }else if(key.equals(UPPER_RIGHT_CORNER)){
            return upperRight;
        }else{
            return null;
        }
    }

    public void setCorner(String key,Component corner){
        Component old;
        boolean isLeftToRight=getComponentOrientation().isLeftToRight();
        if(key.equals(LOWER_LEADING_CORNER)){
            key=isLeftToRight?LOWER_LEFT_CORNER:LOWER_RIGHT_CORNER;
        }else if(key.equals(LOWER_TRAILING_CORNER)){
            key=isLeftToRight?LOWER_RIGHT_CORNER:LOWER_LEFT_CORNER;
        }else if(key.equals(UPPER_LEADING_CORNER)){
            key=isLeftToRight?UPPER_LEFT_CORNER:UPPER_RIGHT_CORNER;
        }else if(key.equals(UPPER_TRAILING_CORNER)){
            key=isLeftToRight?UPPER_RIGHT_CORNER:UPPER_LEFT_CORNER;
        }
        if(key.equals(LOWER_LEFT_CORNER)){
            old=lowerLeft;
            lowerLeft=corner;
        }else if(key.equals(LOWER_RIGHT_CORNER)){
            old=lowerRight;
            lowerRight=corner;
        }else if(key.equals(UPPER_LEFT_CORNER)){
            old=upperLeft;
            upperLeft=corner;
        }else if(key.equals(UPPER_RIGHT_CORNER)){
            old=upperRight;
            upperRight=corner;
        }else{
            throw new IllegalArgumentException("invalid corner key");
        }
        if(old!=null){
            remove(old);
        }
        if(corner!=null){
            add(corner,key);
        }
        firePropertyChange(key,old,corner);
        revalidate();
        repaint();
    }

    public void setComponentOrientation(ComponentOrientation co){
        super.setComponentOrientation(co);
        if(verticalScrollBar!=null)
            verticalScrollBar.setComponentOrientation(co);
        if(horizontalScrollBar!=null)
            horizontalScrollBar.setComponentOrientation(co);
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJScrollPane();
        }
        return accessibleContext;
    }

    public boolean isWheelScrollingEnabled(){
        return wheelScrollState;
    }

    public void setWheelScrollingEnabled(boolean handleWheel){
        boolean old=wheelScrollState;
        wheelScrollState=handleWheel;
        firePropertyChange("wheelScrollingEnabled",old,handleWheel);
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

    protected class ScrollBar extends JScrollBar implements UIResource{
        private boolean unitIncrementSet;
        private boolean blockIncrementSet;

        public ScrollBar(int orientation){
            super(orientation);
            this.putClientProperty("JScrollBar.fastWheelScrolling",
                    Boolean.TRUE);
        }

        public int getUnitIncrement(int direction){
            JViewport vp=getViewport();
            if(!unitIncrementSet&&(vp!=null)&&
                    (vp.getView() instanceof Scrollable)){
                Scrollable view=(Scrollable)(vp.getView());
                Rectangle vr=vp.getViewRect();
                return view.getScrollableUnitIncrement(vr,getOrientation(),direction);
            }else{
                return super.getUnitIncrement(direction);
            }
        }

        public void setUnitIncrement(int unitIncrement){
            unitIncrementSet=true;
            this.putClientProperty("JScrollBar.fastWheelScrolling",null);
            super.setUnitIncrement(unitIncrement);
        }

        public int getBlockIncrement(int direction){
            JViewport vp=getViewport();
            if(blockIncrementSet||vp==null){
                return super.getBlockIncrement(direction);
            }else if(vp.getView() instanceof Scrollable){
                Scrollable view=(Scrollable)(vp.getView());
                Rectangle vr=vp.getViewRect();
                return view.getScrollableBlockIncrement(vr,getOrientation(),direction);
            }else if(getOrientation()==VERTICAL){
                return vp.getExtentSize().height;
            }else{
                return vp.getExtentSize().width;
            }
        }

        public void setBlockIncrement(int blockIncrement){
            blockIncrementSet=true;
            this.putClientProperty("JScrollBar.fastWheelScrolling",null);
            super.setBlockIncrement(blockIncrement);
        }
    }

    protected class AccessibleJScrollPane extends AccessibleJComponent
            implements ChangeListener, PropertyChangeListener{
        protected JViewport viewPort=null;

        public AccessibleJScrollPane(){
            super();
            resetViewPort();
            // initialize the AccessibleRelationSets for the JScrollPane
            // and JScrollBar(s)
            JScrollBar scrollBar=getHorizontalScrollBar();
            if(scrollBar!=null){
                setScrollBarRelations(scrollBar);
            }
            scrollBar=getVerticalScrollBar();
            if(scrollBar!=null){
                setScrollBarRelations(scrollBar);
            }
        }

        public void resetViewPort(){
            if(viewPort!=null){
                viewPort.removeChangeListener(this);
                viewPort.removePropertyChangeListener(this);
            }
            viewPort=JScrollPane.this.getViewport();
            if(viewPort!=null){
                viewPort.addChangeListener(this);
                viewPort.addPropertyChangeListener(this);
            }
        }

        void setScrollBarRelations(JScrollBar scrollBar){
            /**
             * The JScrollBar is a CONTROLLER_FOR the JScrollPane.
             * The JScrollPane is CONTROLLED_BY the JScrollBar.
             */
            AccessibleRelation controlledBy=
                    new AccessibleRelation(AccessibleRelation.CONTROLLED_BY,
                            scrollBar);
            AccessibleRelation controllerFor=
                    new AccessibleRelation(AccessibleRelation.CONTROLLER_FOR,
                            JScrollPane.this);
            // set the relation set for the scroll bar
            AccessibleContext ac=scrollBar.getAccessibleContext();
            ac.getAccessibleRelationSet().add(controllerFor);
            // set the relation set for the scroll pane
            getAccessibleRelationSet().add(controlledBy);
        }

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.SCROLL_PANE;
        }

        public void stateChanged(ChangeEvent e){
            if(e==null){
                throw new NullPointerException();
            }
            firePropertyChange(ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                    Boolean.valueOf(false),
                    Boolean.valueOf(true));
        }

        public void propertyChange(PropertyChangeEvent e){
            String propertyName=e.getPropertyName();
            if(propertyName=="horizontalScrollBar"||
                    propertyName=="verticalScrollBar"){
                if(e.getNewValue() instanceof JScrollBar){
                    setScrollBarRelations((JScrollBar)e.getNewValue());
                }
            }
        }
    }








/////////////////
// Accessibility support
////////////////




}
