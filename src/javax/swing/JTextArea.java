/**
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JTextArea extends JTextComponent{
    private static final String uiClassID="TextAreaUI";
    // --- variables -------------------------------------------------
    private int rows;
    private int columns;
    private int columnWidth;
    private int rowHeight;
    private boolean wrap;
    private boolean word;

    public JTextArea(){
        this(null,null,0,0);
    }

    public JTextArea(Document doc,String text,int rows,int columns){
        super();
        this.rows=rows;
        this.columns=columns;
        if(doc==null){
            doc=createDefaultModel();
        }
        setDocument(doc);
        if(text!=null){
            setText(text);
            select(0,0);
        }
        if(rows<0){
            throw new IllegalArgumentException("rows: "+rows);
        }
        if(columns<0){
            throw new IllegalArgumentException("columns: "+columns);
        }
        LookAndFeel.installProperty(this,
                "focusTraversalKeysForward",
                JComponent.
                        getManagingFocusForwardTraversalKeys());
        LookAndFeel.installProperty(this,
                "focusTraversalKeysBackward",
                JComponent.
                        getManagingFocusBackwardTraversalKeys());
    }

    protected Document createDefaultModel(){
        return new PlainDocument();
    }    public void setTabSize(int size){
        Document doc=getDocument();
        if(doc!=null){
            int old=getTabSize();
            doc.putProperty(PlainDocument.tabSizeAttribute,Integer.valueOf(size));
            firePropertyChange("tabSize",old,size);
        }
    }

    public JTextArea(String text){
        this(null,text,0,0);
    }    public int getTabSize(){
        int size=8;
        Document doc=getDocument();
        if(doc!=null){
            Integer i=(Integer)doc.getProperty(PlainDocument.tabSizeAttribute);
            if(i!=null){
                size=i.intValue();
            }
        }
        return size;
    }

    public JTextArea(int rows,int columns){
        this(null,null,rows,columns);
    }

    public JTextArea(String text,int rows,int columns){
        this(null,text,rows,columns);
    }

    public JTextArea(Document doc){
        this(doc,null,0,0);
    }

    public boolean getLineWrap(){
        return wrap;
    }

    public void setLineWrap(boolean wrap){
        boolean old=this.wrap;
        this.wrap=wrap;
        firePropertyChange("lineWrap",old,wrap);
    }

    public boolean getWrapStyleWord(){
        return word;
    }

    public void setWrapStyleWord(boolean word){
        boolean old=this.word;
        this.word=word;
        firePropertyChange("wrapStyleWord",old,word);
    }

    public int getLineOfOffset(int offset) throws BadLocationException{
        Document doc=getDocument();
        if(offset<0){
            throw new BadLocationException("Can't translate offset to line",-1);
        }else if(offset>doc.getLength()){
            throw new BadLocationException("Can't translate offset to line",doc.getLength()+1);
        }else{
            Element map=getDocument().getDefaultRootElement();
            return map.getElementIndex(offset);
        }
    }
    // --- java.awt.TextArea methods ---------------------------------

    public int getLineStartOffset(int line) throws BadLocationException{
        int lineCount=getLineCount();
        if(line<0){
            throw new BadLocationException("Negative line",-1);
        }else if(line>=lineCount){
            throw new BadLocationException("No such line",getDocument().getLength()+1);
        }else{
            Element map=getDocument().getDefaultRootElement();
            Element lineElem=map.getElement(line);
            return lineElem.getStartOffset();
        }
    }

    public int getLineCount(){
        Element map=getDocument().getDefaultRootElement();
        return map.getElementCount();
    }

    public int getLineEndOffset(int line) throws BadLocationException{
        int lineCount=getLineCount();
        if(line<0){
            throw new BadLocationException("Negative line",-1);
        }else if(line>=lineCount){
            throw new BadLocationException("No such line",getDocument().getLength()+1);
        }else{
            Element map=getDocument().getDefaultRootElement();
            Element lineElem=map.getElement(line);
            int endOffset=lineElem.getEndOffset();
            // hide the implicit break at the end of the document
            return ((line==lineCount-1)?(endOffset-1):endOffset);
        }
    }

    public void insert(String str,int pos){
        Document doc=getDocument();
        if(doc!=null){
            try{
                doc.insertString(pos,str,null);
            }catch(BadLocationException e){
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public void append(String str){
        Document doc=getDocument();
        if(doc!=null){
            try{
                doc.insertString(doc.getLength(),str,null);
            }catch(BadLocationException e){
            }
        }
    }

    public void replaceRange(String str,int start,int end){
        if(end<start){
            throw new IllegalArgumentException("end before start");
        }
        Document doc=getDocument();
        if(doc!=null){
            try{
                if(doc instanceof AbstractDocument){
                    ((AbstractDocument)doc).replace(start,end-start,str,
                            null);
                }else{
                    doc.remove(start,end-start);
                    doc.insertString(start,str,null);
                }
            }catch(BadLocationException e){
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public int getRows(){
        return rows;
    }

    public void setRows(int rows){
        int oldVal=this.rows;
        if(rows<0){
            throw new IllegalArgumentException("rows less than zero.");
        }
        if(rows!=oldVal){
            this.rows=rows;
            invalidate();
        }
    }

    public int getColumns(){
        return columns;
    }
    // --- Component methods -----------------------------------------

    public void setColumns(int columns){
        int oldVal=this.columns;
        if(columns<0){
            throw new IllegalArgumentException("columns less than zero.");
        }
        if(columns!=oldVal){
            this.columns=columns;
            invalidate();
        }
    }

    public Dimension getPreferredScrollableViewportSize(){
        Dimension size=super.getPreferredScrollableViewportSize();
        size=(size==null)?new Dimension(400,400):size;
        Insets insets=getInsets();
        size.width=(columns==0)?size.width:
                columns*getColumnWidth()+insets.left+insets.right;
        size.height=(rows==0)?size.height:
                rows*getRowHeight()+insets.top+insets.bottom;
        return size;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction){
        switch(orientation){
            case SwingConstants.VERTICAL:
                return getRowHeight();
            case SwingConstants.HORIZONTAL:
                return getColumnWidth();
            default:
                throw new IllegalArgumentException("Invalid orientation: "+orientation);
        }
    }
    // --- Scrollable methods ----------------------------------------

    public boolean getScrollableTracksViewportWidth(){
        return (wrap)?true:super.getScrollableTracksViewportWidth();
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJTextArea();
        }
        return accessibleContext;
    }

    protected String paramString(){
        String wrapString=(wrap?
                "true":"false");
        String wordString=(word?
                "true":"false");
        return super.paramString()+
                ",colums="+columns+
                ",columWidth="+columnWidth+
                ",rows="+rows+
                ",rowHeight="+rowHeight+
                ",word="+wordString+
                ",wrap="+wrapString;
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
/////////////////
// Accessibility support
////////////////

    public String getUIClassID(){
        return uiClassID;
    }

    public Dimension getPreferredSize(){
        Dimension d=super.getPreferredSize();
        d=(d==null)?new Dimension(400,400):d;
        Insets insets=getInsets();
        if(columns!=0){
            d.width=Math.max(d.width,columns*getColumnWidth()+
                    insets.left+insets.right);
        }
        if(rows!=0){
            d.height=Math.max(d.height,rows*getRowHeight()+
                    insets.top+insets.bottom);
        }
        return d;
    }

    protected int getRowHeight(){
        if(rowHeight==0){
            FontMetrics metrics=getFontMetrics(getFont());
            rowHeight=metrics.getHeight();
        }
        return rowHeight;
    }

    protected int getColumnWidth(){
        if(columnWidth==0){
            FontMetrics metrics=getFontMetrics(getFont());
            columnWidth=metrics.charWidth('m');
        }
        return columnWidth;
    }

    public void setFont(Font f){
        super.setFont(f);
        rowHeight=0;
        columnWidth=0;
    }

    protected class AccessibleJTextArea extends AccessibleJTextComponent{
        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            return states;
        }
    }


}
