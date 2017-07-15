/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EventObject;
import java.util.Vector;

public class DefaultTreeCellEditor implements ActionListener, TreeCellEditor,
        TreeSelectionListener{
    protected TreeCellEditor realEditor;
    protected DefaultTreeCellRenderer renderer;
    protected Container editingContainer;
    transient protected Component editingComponent;
    protected boolean canEdit;
    protected transient int offset;
    protected transient JTree tree;
    protected transient TreePath lastPath;
    protected transient Timer timer;
    protected transient int lastRow;
    protected Color borderSelectionColor;
    protected transient Icon editingIcon;
    protected Font font;

    public DefaultTreeCellEditor(JTree tree,
                                 DefaultTreeCellRenderer renderer){
        this(tree,renderer,null);
    }

    public DefaultTreeCellEditor(JTree tree,DefaultTreeCellRenderer renderer,
                                 TreeCellEditor editor){
        this.renderer=renderer;
        realEditor=editor;
        if(realEditor==null)
            realEditor=createTreeCellEditor();
        editingContainer=createContainer();
        setTree(tree);
        setBorderSelectionColor(UIManager.getColor
                ("Tree.editorBorderSelectionColor"));
    }

    protected void setTree(JTree newTree){
        if(tree!=newTree){
            if(tree!=null)
                tree.removeTreeSelectionListener(this);
            tree=newTree;
            if(tree!=null)
                tree.addTreeSelectionListener(this);
            if(timer!=null){
                timer.stop();
            }
        }
    }

    protected Container createContainer(){
        return new EditorContainer();
    }

    protected TreeCellEditor createTreeCellEditor(){
        Border aBorder=UIManager.getBorder("Tree.editorBorder");
        DefaultCellEditor editor=new DefaultCellEditor
                (new DefaultTextField(aBorder)){
            public boolean shouldSelectCell(EventObject event){
                boolean retValue=super.shouldSelectCell(event);
                return retValue;
            }
        };
        // One click to edit.
        editor.setClickCountToStart(1);
        return editor;
    }

    public Color getBorderSelectionColor(){
        return borderSelectionColor;
    }
    //
    // TreeCellEditor
    //

    public void setBorderSelectionColor(Color newColor){
        borderSelectionColor=newColor;
    }

    public Component getTreeCellEditorComponent(JTree tree,Object value,
                                                boolean isSelected,
                                                boolean expanded,
                                                boolean leaf,int row){
        setTree(tree);
        lastRow=row;
        determineOffset(tree,value,isSelected,expanded,leaf,row);
        if(editingComponent!=null){
            editingContainer.remove(editingComponent);
        }
        editingComponent=realEditor.getTreeCellEditorComponent(tree,value,
                isSelected,expanded,leaf,row);
        // this is kept for backwards compatibility but isn't really needed
        // with the current BasicTreeUI implementation.
        TreePath newPath=tree.getPathForRow(row);
        canEdit=(lastPath!=null&&newPath!=null&&
                lastPath.equals(newPath));
        Font font=getFont();
        if(font==null){
            if(renderer!=null)
                font=renderer.getFont();
            if(font==null)
                font=tree.getFont();
        }
        editingContainer.setFont(font);
        prepareForEditing();
        return editingContainer;
    }

    public Font getFont(){
        return font;
    }

    public void setFont(Font font){
        this.font=font;
    }

    protected void determineOffset(JTree tree,Object value,
                                   boolean isSelected,boolean expanded,
                                   boolean leaf,int row){
        if(renderer!=null){
            if(leaf)
                editingIcon=renderer.getLeafIcon();
            else if(expanded)
                editingIcon=renderer.getOpenIcon();
            else
                editingIcon=renderer.getClosedIcon();
            if(editingIcon!=null)
                offset=renderer.getIconTextGap()+
                        editingIcon.getIconWidth();
            else
                offset=renderer.getIconTextGap();
        }else{
            editingIcon=null;
            offset=0;
        }
    }

    protected void prepareForEditing(){
        if(editingComponent!=null){
            editingContainer.add(editingComponent);
        }
    }

    public Object getCellEditorValue(){
        return realEditor.getCellEditorValue();
    }

    public boolean isCellEditable(EventObject event){
        boolean retValue=false;
        boolean editable=false;
        if(event!=null){
            if(event.getSource() instanceof JTree){
                setTree((JTree)event.getSource());
                if(event instanceof MouseEvent){
                    TreePath path=tree.getPathForLocation(
                            ((MouseEvent)event).getX(),
                            ((MouseEvent)event).getY());
                    editable=(lastPath!=null&&path!=null&&
                            lastPath.equals(path));
                    if(path!=null){
                        lastRow=tree.getRowForPath(path);
                        Object value=path.getLastPathComponent();
                        boolean isSelected=tree.isRowSelected(lastRow);
                        boolean expanded=tree.isExpanded(path);
                        TreeModel treeModel=tree.getModel();
                        boolean leaf=treeModel.isLeaf(value);
                        determineOffset(tree,value,isSelected,
                                expanded,leaf,lastRow);
                    }
                }
            }
        }
        if(!realEditor.isCellEditable(event))
            return false;
        if(canEditImmediately(event))
            retValue=true;
        else if(editable&&shouldStartEditingTimer(event)){
            startEditingTimer();
        }else if(timer!=null&&timer.isRunning())
            timer.stop();
        if(retValue)
            prepareForEditing();
        return retValue;
    }

    public boolean shouldSelectCell(EventObject event){
        return realEditor.shouldSelectCell(event);
    }
    //
    // TreeSelectionListener
    //

    public boolean stopCellEditing(){
        if(realEditor.stopCellEditing()){
            cleanupAfterEditing();
            return true;
        }
        return false;
    }
    //
    // ActionListener (for Timer).
    //

    public void cancelCellEditing(){
        realEditor.cancelCellEditing();
        cleanupAfterEditing();
    }
    //
    // Local methods
    //

    public void addCellEditorListener(CellEditorListener l){
        realEditor.addCellEditorListener(l);
    }

    public void removeCellEditorListener(CellEditorListener l){
        realEditor.removeCellEditorListener(l);
    }

    private void cleanupAfterEditing(){
        if(editingComponent!=null){
            editingContainer.remove(editingComponent);
        }
        editingComponent=null;
    }

    protected boolean shouldStartEditingTimer(EventObject event){
        if((event instanceof MouseEvent)&&
                SwingUtilities.isLeftMouseButton((MouseEvent)event)){
            MouseEvent me=(MouseEvent)event;
            return (me.getClickCount()==1&&
                    inHitRegion(me.getX(),me.getY()));
        }
        return false;
    }

    protected void startEditingTimer(){
        if(timer==null){
            timer=new Timer(1200,this);
            timer.setRepeats(false);
        }
        timer.start();
    }

    protected boolean canEditImmediately(EventObject event){
        if((event instanceof MouseEvent)&&
                SwingUtilities.isLeftMouseButton((MouseEvent)event)){
            MouseEvent me=(MouseEvent)event;
            return ((me.getClickCount()>2)&&
                    inHitRegion(me.getX(),me.getY()));
        }
        return (event==null);
    }

    protected boolean inHitRegion(int x,int y){
        if(lastRow!=-1&&tree!=null){
            Rectangle bounds=tree.getRowBounds(lastRow);
            ComponentOrientation treeOrientation=tree.getComponentOrientation();
            if(treeOrientation.isLeftToRight()){
                if(bounds!=null&&x<=(bounds.x+offset)&&
                        offset<(bounds.width-5)){
                    return false;
                }
            }else if(bounds!=null&&
                    (x>=(bounds.x+bounds.width-offset+5)||
                            x<=(bounds.x+5))&&
                    offset<(bounds.width-5)){
                return false;
            }
        }
        return true;
    }

    public CellEditorListener[] getCellEditorListeners(){
        return ((DefaultCellEditor)realEditor).getCellEditorListeners();
    }

    public void valueChanged(TreeSelectionEvent e){
        if(tree!=null){
            if(tree.getSelectionCount()==1)
                lastPath=tree.getSelectionPath();
            else
                lastPath=null;
        }
        if(timer!=null){
            timer.stop();
        }
    }

    public void actionPerformed(ActionEvent e){
        if(tree!=null&&lastPath!=null){
            tree.startEditingAtPath(lastPath);
        }
    }

    // Serialization support.
    private void writeObject(ObjectOutputStream s) throws IOException{
        Vector<Object> values=new Vector<Object>();
        s.defaultWriteObject();
        // Save the realEditor, if its Serializable.
        if(realEditor!=null&&realEditor instanceof Serializable){
            values.addElement("realEditor");
            values.addElement(realEditor);
        }
        s.writeObject(values);
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        Vector values=(Vector)s.readObject();
        int indexCounter=0;
        int maxCounter=values.size();
        if(indexCounter<maxCounter&&values.elementAt(indexCounter).
                equals("realEditor")){
            realEditor=(TreeCellEditor)values.elementAt(++indexCounter);
            indexCounter++;
        }
    }

    public class DefaultTextField extends JTextField{
        protected Border border;

        public DefaultTextField(Border border){
            setBorder(border);
        }

        // implements java.awt.MenuContainer
        public Font getFont(){
            Font font=super.getFont();
            // Prefer the parent containers font if our font is a
            // FontUIResource
            if(font instanceof FontUIResource){
                Container parent=getParent();
                if(parent!=null&&parent.getFont()!=null)
                    font=parent.getFont();
            }
            return font;
        }        public void setBorder(Border border){
            super.setBorder(border);
            this.border=border;
        }

        public Dimension getPreferredSize(){
            Dimension size=super.getPreferredSize();
            // If not font has been set, prefer the renderers height.
            if(renderer!=null&&
                    DefaultTreeCellEditor.this.getFont()==null){
                Dimension rSize=renderer.getPreferredSize();
                size.height=rSize.height;
            }
            return size;
        }        public Border getBorder(){
            return border;
        }




    }

    public class EditorContainer extends Container{
        public EditorContainer(){
            setLayout(null);
        }

        // This should not be used. It will be removed when new API is
        // allowed.
        public void EditorContainer(){
            setLayout(null);
        }

        public void doLayout(){
            if(editingComponent!=null){
                int width=getWidth();
                int height=getHeight();
                if(getComponentOrientation().isLeftToRight()){
                    editingComponent.setBounds(
                            offset,0,width-offset,height);
                }else{
                    editingComponent.setBounds(
                            0,0,width-offset,height);
                }
            }
        }

        public Dimension getPreferredSize(){
            if(editingComponent!=null){
                Dimension pSize=editingComponent.getPreferredSize();
                pSize.width+=offset+5;
                Dimension rSize=(renderer!=null)?
                        renderer.getPreferredSize():null;
                if(rSize!=null)
                    pSize.height=Math.max(pSize.height,rSize.height);
                if(editingIcon!=null)
                    pSize.height=Math.max(pSize.height,
                            editingIcon.getIconHeight());
                // Make sure width is at least 100.
                pSize.width=Math.max(pSize.width,100);
                return pSize;
            }
            return new Dimension(0,0);
        }

        public void paint(Graphics g){
            int width=getWidth();
            int height=getHeight();
            // Then the icon.
            if(editingIcon!=null){
                int yLoc=calculateIconY(editingIcon);
                if(getComponentOrientation().isLeftToRight()){
                    editingIcon.paintIcon(this,g,0,yLoc);
                }else{
                    editingIcon.paintIcon(
                            this,g,width-editingIcon.getIconWidth(),
                            yLoc);
                }
            }
            // Border selection color
            Color background=getBorderSelectionColor();
            if(background!=null){
                g.setColor(background);
                g.drawRect(0,0,width-1,height-1);
            }
            super.paint(g);
        }

        private int calculateIconY(Icon icon){
            // To make sure the icon position matches that of the
            // renderer, use the same algorithm as JLabel
            // (SwingUtilities.layoutCompoundLabel).
            int iconHeight=icon.getIconHeight();
            int textHeight=editingComponent.getFontMetrics(
                    editingComponent.getFont()).getHeight();
            int textY=iconHeight/2-textHeight/2;
            int totalY=Math.min(0,textY);
            int totalHeight=Math.max(iconHeight,textY+textHeight)-
                    totalY;
            return getHeight()/2-(totalY+(totalHeight/2));
        }
    }
}
