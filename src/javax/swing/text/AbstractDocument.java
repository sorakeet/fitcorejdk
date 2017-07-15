/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import sun.font.BidiUtils;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreeNode;
import javax.swing.undo.*;
import java.awt.font.TextAttribute;
import java.io.*;
import java.text.Bidi;
import java.util.*;

public abstract class AbstractDocument implements Document, Serializable{
    public static final String ParagraphElementName="paragraph";
    public static final String ContentElementName="content";
    public static final String SectionElementName="section";    public Dictionary<Object,Object> getDocumentProperties(){
        if(documentProperties==null){
            documentProperties=new Hashtable<Object,Object>(2);
        }
        return documentProperties;
    }
    public static final String BidiElementName="bidi level";    public void setDocumentProperties(Dictionary<Object,Object> x){
        documentProperties=x;
    }
    public static final String ElementNameAttribute="$ename";
    protected static final String BAD_LOCATION="document location failure";
    static final String I18NProperty="i18n";
    static final Object MultiByteProperty="multiByte";
    static final String AsyncLoadPriority="load priority";
    private static final String BAD_LOCK_STATE="document lock failure";
    private static Boolean defaultI18NProperty;
    protected EventListenerList listenerList=new EventListenerList();
    // ----- member variables ------------------------------------------
    private transient int numReaders;
    // --- Document methods -----------------------------------------
    private transient Thread currWriter;    public void render(Runnable r){
        readLock();
        try{
            r.run();
        }finally{
            readUnlock();
        }
    }
    private transient int numWriters;    public int getLength(){
        return data.length()-1;
    }
    private transient boolean notifyingListeners;    public void addDocumentListener(DocumentListener listener){
        listenerList.add(DocumentListener.class,listener);
    }
    private Dictionary<Object,Object> documentProperties=null;    public void removeDocumentListener(DocumentListener listener){
        listenerList.remove(DocumentListener.class,listener);
    }
    private Content data;
    private AttributeContext context;    public void addUndoableEditListener(UndoableEditListener listener){
        listenerList.add(UndoableEditListener.class,listener);
    }
    private transient BranchElement bidiRoot;    public void removeUndoableEditListener(UndoableEditListener listener){
        listenerList.remove(UndoableEditListener.class,listener);
    }
    private DocumentFilter documentFilter;
    private transient DocumentFilter.FilterBypass filterBypass;    public final Object getProperty(Object key){
        return getDocumentProperties().get(key);
    }

    protected AbstractDocument(Content data){
        this(data,StyleContext.getDefaultStyleContext());
    }    public final void putProperty(Object key,Object value){
        if(value!=null){
            getDocumentProperties().put(key,value);
        }else{
            getDocumentProperties().remove(key);
        }
        if(key==TextAttribute.RUN_DIRECTION
                &&Boolean.TRUE.equals(getProperty(I18NProperty))){
            //REMIND - this needs to flip on the i18n property if run dir
            //is rtl and the i18n property is not already on.
            writeLock();
            try{
                DefaultDocumentEvent e
                        =new DefaultDocumentEvent(0,getLength(),
                        DocumentEvent.EventType.INSERT);
                updateBidi(e);
            }finally{
                writeUnlock();
            }
        }
    }

    protected AbstractDocument(Content data,AttributeContext context){
        this.data=data;
        this.context=context;
        bidiRoot=new BidiRootElement();
        if(defaultI18NProperty==null){
            // determine default setting for i18n support
            String o=java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<String>(){
                        public String run(){
                            return System.getProperty(I18NProperty);
                        }
                    }
            );
            if(o!=null){
                defaultI18NProperty=Boolean.valueOf(o);
            }else{
                defaultI18NProperty=Boolean.FALSE;
            }
        }
        putProperty(I18NProperty,defaultI18NProperty);
        //REMIND(bcb) This creates an initial bidi element to account for
        //the \n that exists by default in the content.  Doing it this way
        //seems to expose a little too much knowledge of the content given
        //to us by the sub-class.  Consider having the sub-class' constructor
        //make an initial call to insertUpdate.
        writeLock();
        try{
            Element[] p=new Element[1];
            p[0]=new BidiElement(bidiRoot,0,1,0);
            bidiRoot.replace(0,0,p);
        }finally{
            writeUnlock();
        }
    }    public void remove(int offs,int len) throws BadLocationException{
        DocumentFilter filter=getDocumentFilter();
        writeLock();
        try{
            if(filter!=null){
                filter.remove(getFilterBypass(),offs,len);
            }else{
                handleRemove(offs,len);
            }
        }finally{
            writeUnlock();
        }
    }

    static boolean isLeftToRight(Document doc,int p0,int p1){
        if(Boolean.TRUE.equals(doc.getProperty(I18NProperty))){
            if(doc instanceof AbstractDocument){
                AbstractDocument adoc=(AbstractDocument)doc;
                Element bidiRoot=adoc.getBidiRootElement();
                int index=bidiRoot.getElementIndex(p0);
                Element bidiElem=bidiRoot.getElement(index);
                if(bidiElem.getEndOffset()>=p1){
                    AttributeSet bidiAttrs=bidiElem.getAttributes();
                    return ((StyleConstants.getBidiLevel(bidiAttrs)%2)==0);
                }
            }
        }
        return true;
    }

    protected void fireInsertUpdate(DocumentEvent e){
        notifyingListeners=true;
        try{
            // Guaranteed to return a non-null array
            Object[] listeners=listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for(int i=listeners.length-2;i>=0;i-=2){
                if(listeners[i]==DocumentListener.class){
                    // Lazily create the event:
                    // if (e == null)
                    // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                    ((DocumentListener)listeners[i+1]).insertUpdate(e);
                }
            }
        }finally{
            notifyingListeners=false;
        }
    }

    protected void fireChangedUpdate(DocumentEvent e){
        notifyingListeners=true;
        try{
            // Guaranteed to return a non-null array
            Object[] listeners=listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for(int i=listeners.length-2;i>=0;i-=2){
                if(listeners[i]==DocumentListener.class){
                    // Lazily create the event:
                    // if (e == null)
                    // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                    ((DocumentListener)listeners[i+1]).changedUpdate(e);
                }
            }
        }finally{
            notifyingListeners=false;
        }
    }    public void insertString(int offs,String str,AttributeSet a) throws BadLocationException{
        if((str==null)||(str.length()==0)){
            return;
        }
        DocumentFilter filter=getDocumentFilter();
        writeLock();
        try{
            if(filter!=null){
                filter.insertString(getFilterBypass(),offs,str,a);
            }else{
                handleInsertString(offs,str,a);
            }
        }finally{
            writeUnlock();
        }
    }

    protected void fireRemoveUpdate(DocumentEvent e){
        notifyingListeners=true;
        try{
            // Guaranteed to return a non-null array
            Object[] listeners=listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for(int i=listeners.length-2;i>=0;i-=2){
                if(listeners[i]==DocumentListener.class){
                    // Lazily create the event:
                    // if (e == null)
                    // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                    ((DocumentListener)listeners[i+1]).removeUpdate(e);
                }
            }
        }finally{
            notifyingListeners=false;
        }
    }

    protected void fireUndoableEditUpdate(UndoableEditEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==UndoableEditListener.class){
                // Lazily create the event:
                // if (e == null)
                // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                ((UndoableEditListener)listeners[i+1]).undoableEditHappened(e);
            }
        }
    }    public String getText(int offset,int length) throws BadLocationException{
        if(length<0){
            throw new BadLocationException("Length must be positive",length);
        }
        String str=data.getString(offset,length);
        return str;
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }    public void getText(int offset,int length,Segment txt) throws BadLocationException{
        if(length<0){
            throw new BadLocationException("Length must be positive",length);
        }
        data.getChars(offset,length,txt);
    }

    public int getAsynchronousLoadPriority(){
        Integer loadPriority=(Integer)
                getProperty(AbstractDocument.AsyncLoadPriority);
        if(loadPriority!=null){
            return loadPriority.intValue();
        }
        return -1;
    }    public synchronized Position createPosition(int offs) throws BadLocationException{
        return data.createPosition(offs);
    }

    public void setAsynchronousLoadPriority(int p){
        Integer loadPriority=(p>=0)?Integer.valueOf(p):null;
        putProperty(AbstractDocument.AsyncLoadPriority,loadPriority);
    }    public final Position getStartPosition(){
        Position p;
        try{
            p=createPosition(0);
        }catch(BadLocationException bl){
            p=null;
        }
        return p;
    }

    public DocumentFilter getDocumentFilter(){
        return documentFilter;
    }    public final Position getEndPosition(){
        Position p;
        try{
            p=createPosition(data.length());
        }catch(BadLocationException bl){
            p=null;
        }
        return p;
    }

    public void setDocumentFilter(DocumentFilter filter){
        documentFilter=filter;
    }    public Element[] getRootElements(){
        Element[] elems=new Element[2];
        elems[0]=getDefaultRootElement();
        elems[1]=getBidiRootElement();
        return elems;
    }

    public DocumentListener[] getDocumentListeners(){
        return listenerList.getListeners(DocumentListener.class);
    }    public abstract Element getDefaultRootElement();
    // ---- local methods -----------------------------------------

    public UndoableEditListener[] getUndoableEditListeners(){
        return listenerList.getListeners(UndoableEditListener.class);
    }

    void handleRemove(int offs,int len) throws BadLocationException{
        if(len>0){
            if(offs<0||(offs+len)>getLength()){
                throw new BadLocationException("Invalid remove",
                        getLength()+1);
            }
            DefaultDocumentEvent chng=
                    new DefaultDocumentEvent(offs,len,DocumentEvent.EventType.REMOVE);
            boolean isComposedTextElement;
            // Check whether the position of interest is the composed text
            isComposedTextElement=Utilities.isComposedTextElement(this,offs);
            removeUpdate(chng);
            UndoableEdit u=data.remove(offs,len);
            if(u!=null){
                chng.addEdit(u);
            }
            postRemoveUpdate(chng);
            // Mark the edit as done.
            chng.end();
            fireRemoveUpdate(chng);
            // only fire undo if Content implementation supports it
            // undo for the composed text is not supported for now
            if((u!=null)&&!isComposedTextElement){
                fireUndoableEditUpdate(new UndoableEditEvent(this,chng));
            }
        }
    }    public Element getBidiRootElement(){
        return bidiRoot;
    }

    public void replace(int offset,int length,String text,
                        AttributeSet attrs) throws BadLocationException{
        if(length==0&&(text==null||text.length()==0)){
            return;
        }
        DocumentFilter filter=getDocumentFilter();
        writeLock();
        try{
            if(filter!=null){
                filter.replace(getFilterBypass(),offset,length,text,
                        attrs);
            }else{
                if(length>0){
                    remove(offset,length);
                }
                if(text!=null&&text.length()>0){
                    insertString(offset,text,attrs);
                }
            }
        }finally{
            writeUnlock();
        }
    }

    private void handleInsertString(int offs,String str,AttributeSet a)
            throws BadLocationException{
        if((str==null)||(str.length()==0)){
            return;
        }
        UndoableEdit u=data.insertString(offs,str);
        DefaultDocumentEvent e=
                new DefaultDocumentEvent(offs,str.length(),DocumentEvent.EventType.INSERT);
        if(u!=null){
            e.addEdit(u);
        }
        // see if complex glyph layout support is needed
        if(getProperty(I18NProperty).equals(Boolean.FALSE)){
            // if a default direction of right-to-left has been specified,
            // we want complex layout even if the text is all left to right.
            Object d=getProperty(TextAttribute.RUN_DIRECTION);
            if((d!=null)&&(d.equals(TextAttribute.RUN_DIRECTION_RTL))){
                putProperty(I18NProperty,Boolean.TRUE);
            }else{
                char[] chars=str.toCharArray();
                if(SwingUtilities2.isComplexLayout(chars,0,chars.length)){
                    putProperty(I18NProperty,Boolean.TRUE);
                }
            }
        }
        insertUpdate(e,a);
        // Mark the edit as done.
        e.end();
        fireInsertUpdate(e);
        // only fire undo if Content implementation supports it
        // undo for the composed text is not supported for now
        if(u!=null&&(a==null||!a.isDefined(StyleConstants.ComposedTextAttribute))){
            fireUndoableEditUpdate(new UndoableEditEvent(this,e));
        }
    }    public abstract Element getParagraphElement(int pos);

    private DocumentFilter.FilterBypass getFilterBypass(){
        if(filterBypass==null){
            filterBypass=new DefaultFilterBypass();
        }
        return filterBypass;
    }

    protected final AttributeContext getAttributeContext(){
        return context;
    }

    protected void insertUpdate(DefaultDocumentEvent chng,AttributeSet attr){
        if(getProperty(I18NProperty).equals(Boolean.TRUE))
            updateBidi(chng);
        // Check if a multi byte is encountered in the inserted text.
        if(chng.type==DocumentEvent.EventType.INSERT&&
                chng.getLength()>0&&
                !Boolean.TRUE.equals(getProperty(MultiByteProperty))){
            Segment segment=SegmentCache.getSharedSegment();
            try{
                getText(chng.getOffset(),chng.getLength(),segment);
                segment.first();
                do{
                    if((int)segment.current()>255){
                        putProperty(MultiByteProperty,Boolean.TRUE);
                        break;
                    }
                }while(segment.next()!=Segment.DONE);
            }catch(BadLocationException ble){
                // Should never happen
            }
            SegmentCache.releaseSharedSegment(segment);
        }
    }

    protected void removeUpdate(DefaultDocumentEvent chng){
    }

    protected void postRemoveUpdate(DefaultDocumentEvent chng){
        if(getProperty(I18NProperty).equals(Boolean.TRUE))
            updateBidi(chng);
    }    void updateBidi(DefaultDocumentEvent chng){
        // Calculate the range of paragraphs affected by the change.
        int firstPStart;
        int lastPEnd;
        if(chng.type==DocumentEvent.EventType.INSERT
                ||chng.type==DocumentEvent.EventType.CHANGE){
            int chngStart=chng.getOffset();
            int chngEnd=chngStart+chng.getLength();
            firstPStart=getParagraphElement(chngStart).getStartOffset();
            lastPEnd=getParagraphElement(chngEnd).getEndOffset();
        }else if(chng.type==DocumentEvent.EventType.REMOVE){
            Element paragraph=getParagraphElement(chng.getOffset());
            firstPStart=paragraph.getStartOffset();
            lastPEnd=paragraph.getEndOffset();
        }else{
            throw new Error("Internal error: unknown event type.");
        }
        //System.out.println("updateBidi: firstPStart = " + firstPStart + " lastPEnd = " + lastPEnd );
        // Calculate the bidi levels for the affected range of paragraphs.  The
        // levels array will contain a bidi level for each character in the
        // affected text.
        byte levels[]=calculateBidiLevels(firstPStart,lastPEnd);
        Vector<Element> newElements=new Vector<Element>();
        // Calculate the first span of characters in the affected range with
        // the same bidi level.  If this level is the same as the level of the
        // previous bidi element (the existing bidi element containing
        // firstPStart-1), then merge in the previous element.  If not, but
        // the previous element overlaps the affected range, truncate the
        // previous element at firstPStart.
        int firstSpanStart=firstPStart;
        int removeFromIndex=0;
        if(firstSpanStart>0){
            int prevElemIndex=bidiRoot.getElementIndex(firstPStart-1);
            removeFromIndex=prevElemIndex;
            Element prevElem=bidiRoot.getElement(prevElemIndex);
            int prevLevel=StyleConstants.getBidiLevel(prevElem.getAttributes());
            //System.out.println("createbidiElements: prevElem= " + prevElem  + " prevLevel= " + prevLevel + "level[0] = " + levels[0]);
            if(prevLevel==levels[0]){
                firstSpanStart=prevElem.getStartOffset();
            }else if(prevElem.getEndOffset()>firstPStart){
                newElements.addElement(new BidiElement(bidiRoot,
                        prevElem.getStartOffset(),
                        firstPStart,prevLevel));
            }else{
                removeFromIndex++;
            }
        }
        int firstSpanEnd=0;
        while((firstSpanEnd<levels.length)&&(levels[firstSpanEnd]==levels[0]))
            firstSpanEnd++;
        // Calculate the last span of characters in the affected range with
        // the same bidi level.  If this level is the same as the level of the
        // next bidi element (the existing bidi element containing lastPEnd),
        // then merge in the next element.  If not, but the next element
        // overlaps the affected range, adjust the next element to start at
        // lastPEnd.
        int lastSpanEnd=lastPEnd;
        Element newNextElem=null;
        int removeToIndex=bidiRoot.getElementCount()-1;
        if(lastSpanEnd<=getLength()){
            int nextElemIndex=bidiRoot.getElementIndex(lastPEnd);
            removeToIndex=nextElemIndex;
            Element nextElem=bidiRoot.getElement(nextElemIndex);
            int nextLevel=StyleConstants.getBidiLevel(nextElem.getAttributes());
            if(nextLevel==levels[levels.length-1]){
                lastSpanEnd=nextElem.getEndOffset();
            }else if(nextElem.getStartOffset()<lastPEnd){
                newNextElem=new BidiElement(bidiRoot,lastPEnd,
                        nextElem.getEndOffset(),
                        nextLevel);
            }else{
                removeToIndex--;
            }
        }
        int lastSpanStart=levels.length;
        while((lastSpanStart>firstSpanEnd)
                &&(levels[lastSpanStart-1]==levels[levels.length-1]))
            lastSpanStart--;
        // If the first and last spans are contiguous and have the same level,
        // merge them and create a single new element for the entire span.
        // Otherwise, create elements for the first and last spans as well as
        // any spans in between.
        if((firstSpanEnd==lastSpanStart)&&(levels[0]==levels[levels.length-1])){
            newElements.addElement(new BidiElement(bidiRoot,firstSpanStart,
                    lastSpanEnd,levels[0]));
        }else{
            // Create an element for the first span.
            newElements.addElement(new BidiElement(bidiRoot,firstSpanStart,
                    firstSpanEnd+firstPStart,
                    levels[0]));
            // Create elements for the spans in between the first and last
            for(int i=firstSpanEnd;i<lastSpanStart;){
                //System.out.println("executed line 872");
                int j;
                for(j=i;(j<levels.length)&&(levels[j]==levels[i]);j++) ;
                newElements.addElement(new BidiElement(bidiRoot,firstPStart+i,
                        firstPStart+j,
                        (int)levels[i]));
                i=j;
            }
            // Create an element for the last span.
            newElements.addElement(new BidiElement(bidiRoot,
                    lastSpanStart+firstPStart,
                    lastSpanEnd,
                    levels[levels.length-1]));
        }
        if(newNextElem!=null)
            newElements.addElement(newNextElem);
        // Calculate the set of existing bidi elements which must be
        // removed.
        int removedElemCount=0;
        if(bidiRoot.getElementCount()>0){
            removedElemCount=removeToIndex-removeFromIndex+1;
        }
        Element[] removedElems=new Element[removedElemCount];
        for(int i=0;i<removedElemCount;i++){
            removedElems[i]=bidiRoot.getElement(removeFromIndex+i);
        }
        Element[] addedElems=new Element[newElements.size()];
        newElements.copyInto(addedElems);
        // Update the change record.
        ElementEdit ee=new ElementEdit(bidiRoot,removeFromIndex,
                removedElems,addedElems);
        chng.addEdit(ee);
        // Update the bidi element structure.
        bidiRoot.replace(removeFromIndex,removedElems.length,addedElems);
    }

    public void dump(PrintStream out){
        Element root=getDefaultRootElement();
        if(root instanceof AbstractElement){
            ((AbstractElement)root).dump(out,0);
        }
        bidiRoot.dump(out,0);
    }    private byte[] calculateBidiLevels(int firstPStart,int lastPEnd){
        byte levels[]=new byte[lastPEnd-firstPStart];
        int levelsEnd=0;
        Boolean defaultDirection=null;
        Object d=getProperty(TextAttribute.RUN_DIRECTION);
        if(d instanceof Boolean){
            defaultDirection=(Boolean)d;
        }
        // For each paragraph in the given range of paragraphs, get its
        // levels array and add it to the levels array for the entire span.
        for(int o=firstPStart;o<lastPEnd;){
            Element p=getParagraphElement(o);
            int pStart=p.getStartOffset();
            int pEnd=p.getEndOffset();
            // default run direction for the paragraph.  This will be
            // null if there is no direction override specified (i.e.
            // the direction will be determined from the content).
            Boolean direction=defaultDirection;
            d=p.getAttributes().getAttribute(TextAttribute.RUN_DIRECTION);
            if(d instanceof Boolean){
                direction=(Boolean)d;
            }
            //System.out.println("updateBidi: paragraph start = " + pStart + " paragraph end = " + pEnd);
            // Create a Bidi over this paragraph then get the level
            // array.
            Segment seg=SegmentCache.getSharedSegment();
            try{
                getText(pStart,pEnd-pStart,seg);
            }catch(BadLocationException e){
                throw new Error("Internal error: "+e.toString());
            }
            // REMIND(bcb) we should really be using a Segment here.
            Bidi bidiAnalyzer;
            int bidiflag=Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT;
            if(direction!=null){
                if(TextAttribute.RUN_DIRECTION_LTR.equals(direction)){
                    bidiflag=Bidi.DIRECTION_LEFT_TO_RIGHT;
                }else{
                    bidiflag=Bidi.DIRECTION_RIGHT_TO_LEFT;
                }
            }
            bidiAnalyzer=new Bidi(seg.array,seg.offset,null,0,seg.count,
                    bidiflag);
            BidiUtils.getLevels(bidiAnalyzer,levels,levelsEnd);
            levelsEnd+=bidiAnalyzer.getLength();
            o=p.getEndOffset();
            SegmentCache.releaseSharedSegment(seg);
        }
        // REMIND(bcb) remove this code when debugging is done.
        if(levelsEnd!=levels.length)
            throw new Error("levelsEnd assertion failed.");
        return levels;
    }

    protected final Content getContent(){
        return data;
    }

    protected Element createLeafElement(Element parent,AttributeSet a,int p0,int p1){
        return new LeafElement(parent,a,p0,p1);
    }

    protected Element createBranchElement(Element parent,AttributeSet a){
        return new BranchElement(parent,a);
    }

    protected synchronized final Thread getCurrentWriter(){
        return currWriter;
    }
    // --- Document locking ----------------------------------

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        s.defaultReadObject();
        listenerList=new EventListenerList();
        // Restore bidi structure
        //REMIND(bcb) This creates an initial bidi element to account for
        //the \n that exists by default in the content.
        bidiRoot=new BidiRootElement();
        try{
            writeLock();
            Element[] p=new Element[1];
            p[0]=new BidiElement(bidiRoot,0,1,0);
            bidiRoot.replace(0,0,p);
        }finally{
            writeUnlock();
        }
        // At this point bidi root is only partially correct. To fully
        // restore it we need access to getDefaultRootElement. But, this
        // is created by the subclass and at this point will be null. We
        // thus use registerValidation.
        s.registerValidation(new ObjectInputValidation(){
            public void validateObject(){
                try{
                    writeLock();
                    DefaultDocumentEvent e=new DefaultDocumentEvent
                            (0,getLength(),
                                    DocumentEvent.EventType.INSERT);
                    updateBidi(e);
                }finally{
                    writeUnlock();
                }
            }
        },0);
    }

    public interface Content{
        public Position createPosition(int offset) throws BadLocationException;

        public int length();

        public UndoableEdit insertString(int where,String str) throws BadLocationException;

        public UndoableEdit remove(int where,int nitems) throws BadLocationException;

        public String getString(int where,int len) throws BadLocationException;

        public void getChars(int where,int len,Segment txt) throws BadLocationException;
    }    protected synchronized final void writeLock(){
        try{
            while((numReaders>0)||(currWriter!=null)){
                if(Thread.currentThread()==currWriter){
                    if(notifyingListeners){
                        // Assuming one doesn't do something wrong in a
                        // subclass this should only happen if a
                        // DocumentListener tries to mutate the document.
                        throw new IllegalStateException(
                                "Attempt to mutate in notification");
                    }
                    numWriters++;
                    return;
                }
                wait();
            }
            currWriter=Thread.currentThread();
            numWriters=1;
        }catch(InterruptedException e){
            throw new Error("Interrupted attempt to acquire write lock");
        }
    }

    public interface AttributeContext{
        public AttributeSet addAttribute(AttributeSet old,Object name,Object value);

        public AttributeSet addAttributes(AttributeSet old,AttributeSet attr);

        public AttributeSet removeAttribute(AttributeSet old,Object name);

        public AttributeSet removeAttributes(AttributeSet old,Enumeration<?> names);

        public AttributeSet removeAttributes(AttributeSet old,AttributeSet attrs);

        public AttributeSet getEmptySet();

        public void reclaim(AttributeSet a);
    }    protected synchronized final void writeUnlock(){
        if(--numWriters<=0){
            numWriters=0;
            currWriter=null;
            notifyAll();
        }
    }

    public static class ElementEdit extends AbstractUndoableEdit implements DocumentEvent.ElementChange{
        private Element e;
        private int index;
        private Element[] removed;
        private Element[] added;

        public ElementEdit(Element e,int index,Element[] removed,Element[] added){
            super();
            this.e=e;
            this.index=index;
            this.removed=removed;
            this.added=added;
        }

        public Element getElement(){
            return e;
        }

        public int getIndex(){
            return index;
        }

        public Element[] getChildrenRemoved(){
            return removed;
        }

        public Element[] getChildrenAdded(){
            return added;
        }

        public void undo() throws CannotUndoException{
            super.undo();
            // PENDING(prinz) need MutableElement interface, canUndo() should check
            ((BranchElement)e).replace(index,added.length,removed);
            // Since this event will be reused, switch around added/removed.
            Element[] tmp=removed;
            removed=added;
            added=tmp;
        }

        public void redo() throws CannotRedoException{
            super.redo();
            // Since this event will be reused, switch around added/removed.
            Element[] tmp=removed;
            removed=added;
            added=tmp;
            // PENDING(prinz) need MutableElement interface, canRedo() should check
            ((BranchElement)e).replace(index,removed.length,added);
        }
    }    public synchronized final void readLock(){
        try{
            while(currWriter!=null){
                if(currWriter==Thread.currentThread()){
                    // writer has full read access.... may try to acquire
                    // lock in notification
                    return;
                }
                wait();
            }
            numReaders+=1;
        }catch(InterruptedException e){
            throw new Error("Interrupted attempt to acquire read lock");
        }
    }

    public abstract class AbstractElement implements Element, MutableAttributeSet, Serializable, TreeNode{
        // ---- variables -----------------------------------------------------
        private Element parent;
        private transient AttributeSet attributes;

        public AbstractElement(Element parent,AttributeSet a){
            this.parent=parent;
            attributes=getAttributeContext().getEmptySet();
            if(a!=null){
                addAttributes(a);
            }
        }
        // --- AttributeSet ----------------------------
        // delegated to the immutable field "attributes"

        public void dump(PrintStream psOut,int indentAmount){
            PrintWriter out;
            try{
                out=new PrintWriter(new OutputStreamWriter(psOut,"JavaEsc"),
                        true);
            }catch(UnsupportedEncodingException e){
                out=new PrintWriter(psOut,true);
            }
            indent(out,indentAmount);
            if(getName()==null){
                out.print("<??");
            }else{
                out.print("<"+getName());
            }
            if(getAttributeCount()>0){
                out.println("");
                // dump the attributes
                Enumeration names=attributes.getAttributeNames();
                while(names.hasMoreElements()){
                    Object name=names.nextElement();
                    indent(out,indentAmount+1);
                    out.println(name+"="+getAttribute(name));
                }
                indent(out,indentAmount);
            }
            out.println(">");
            if(isLeaf()){
                indent(out,indentAmount+1);
                out.print("["+getStartOffset()+","+getEndOffset()+"]");
                Content c=getContent();
                try{
                    String contentStr=c.getString(getStartOffset(),
                            getEndOffset()-getStartOffset())/**.trim()*/;
                    if(contentStr.length()>40){
                        contentStr=contentStr.substring(0,40)+"...";
                    }
                    out.println("["+contentStr+"]");
                }catch(BadLocationException e){
                }
            }else{
                int n=getElementCount();
                for(int i=0;i<n;i++){
                    AbstractElement e=(AbstractElement)getElement(i);
                    e.dump(psOut,indentAmount+1);
                }
            }
        }

        private final void indent(PrintWriter out,int n){
            for(int i=0;i<n;i++){
                out.print("  ");
            }
        }

        public int getAttributeCount(){
            return attributes.getAttributeCount();
        }

        public boolean isDefined(Object attrName){
            return attributes.isDefined(attrName);
        }

        public boolean isEqual(AttributeSet attr){
            return attributes.isEqual(attr);
        }

        public AttributeSet copyAttributes(){
            return attributes.copyAttributes();
        }

        public Object getAttribute(Object attrName){
            Object value=attributes.getAttribute(attrName);
            if(value==null){
                // The delegate nor it's resolvers had a match,
                // so we'll try to resolve through the parent
                // element.
                AttributeSet a=(parent!=null)?parent.getAttributes():null;
                if(a!=null){
                    value=a.getAttribute(attrName);
                }
            }
            return value;
        }

        public Enumeration<?> getAttributeNames(){
            return attributes.getAttributeNames();
        }

        public boolean containsAttribute(Object name,Object value){
            return attributes.containsAttribute(name,value);
        }
        // --- MutableAttributeSet ----------------------------------
        // should fetch a new immutable record for the field
        // "attributes".

        public boolean containsAttributes(AttributeSet attrs){
            return attributes.containsAttributes(attrs);
        }

        public AttributeSet getResolveParent(){
            AttributeSet a=attributes.getResolveParent();
            if((a==null)&&(parent!=null)){
                a=parent.getAttributes();
            }
            return a;
        }

        public void setResolveParent(AttributeSet parent){
            checkForIllegalCast();
            AttributeContext context=getAttributeContext();
            if(parent!=null){
                attributes=
                        context.addAttribute(attributes,StyleConstants.ResolveAttribute,
                                parent);
            }else{
                attributes=
                        context.removeAttribute(attributes,StyleConstants.ResolveAttribute);
            }
        }

        public void addAttribute(Object name,Object value){
            checkForIllegalCast();
            AttributeContext context=getAttributeContext();
            attributes=context.addAttribute(attributes,name,value);
        }

        public void addAttributes(AttributeSet attr){
            checkForIllegalCast();
            AttributeContext context=getAttributeContext();
            attributes=context.addAttributes(attributes,attr);
        }

        public void removeAttribute(Object name){
            checkForIllegalCast();
            AttributeContext context=getAttributeContext();
            attributes=context.removeAttribute(attributes,name);
        }

        public void removeAttributes(Enumeration<?> names){
            checkForIllegalCast();
            AttributeContext context=getAttributeContext();
            attributes=context.removeAttributes(attributes,names);
        }
        // --- Element methods -------------------------------------

        public void removeAttributes(AttributeSet attrs){
            checkForIllegalCast();
            AttributeContext context=getAttributeContext();
            if(attrs==this){
                attributes=context.getEmptySet();
            }else{
                attributes=context.removeAttributes(attributes,attrs);
            }
        }

        private final void checkForIllegalCast(){
            Thread t=getCurrentWriter();
            if((t==null)||(t!=Thread.currentThread())){
                throw new StateInvariantError("Illegal cast to MutableAttributeSet");
            }
        }

        public Document getDocument(){
            return AbstractDocument.this;
        }

        public Element getParentElement(){
            return parent;
        }

        public String getName(){
            if(attributes.isDefined(ElementNameAttribute)){
                return (String)attributes.getAttribute(ElementNameAttribute);
            }
            return null;
        }

        public AttributeSet getAttributes(){
            return this;
        }

        public abstract int getStartOffset();

        public abstract int getEndOffset();

        public abstract int getElementIndex(int offset);

        public abstract int getElementCount();
        // --- TreeNode methods -------------------------------------

        public abstract Element getElement(int index);        public TreeNode getChildAt(int childIndex){
            return (TreeNode)getElement(childIndex);
        }

        public abstract boolean isLeaf();        public int getChildCount(){
            return getElementCount();
        }

        private void writeObject(ObjectOutputStream s) throws IOException{
            s.defaultWriteObject();
            StyleContext.writeAttributeSet(s,attributes);
        }        public TreeNode getParent(){
            return (TreeNode)getParentElement();
        }

        private void readObject(ObjectInputStream s)
                throws ClassNotFoundException, IOException{
            s.defaultReadObject();
            MutableAttributeSet attr=new SimpleAttributeSet();
            StyleContext.readAttributeSet(s,attr);
            AttributeContext context=getAttributeContext();
            attributes=context.addAttributes(SimpleAttributeSet.EMPTY,attr);
        }        public int getIndex(TreeNode node){
            for(int counter=getChildCount()-1;counter>=0;counter--)
                if(getChildAt(counter)==node)
                    return counter;
            return -1;
        }

        public abstract boolean getAllowsChildren();

        public abstract Enumeration children();
        // --- serialization ---------------------------------------------






    }    public synchronized final void readUnlock(){
        if(currWriter==Thread.currentThread()){
            // writer has full read access.... may try to acquire
            // lock in notification
            return;
        }
        if(numReaders<=0){
            throw new StateInvariantError(BAD_LOCK_STATE);
        }
        numReaders-=1;
        notify();
    }
    // --- serialization ---------------------------------------------

    public class BranchElement extends AbstractElement{
        // ------ members ----------------------------------------------
        private AbstractElement[] children;
        private int nchildren;
        private int lastIndex;

        public BranchElement(Element parent,AttributeSet a){
            super(parent,a);
            children=new AbstractElement[1];
            nchildren=0;
            lastIndex=-1;
        }
        // --- Element methods -----------------------------------

        public Element positionToElement(int pos){
            int index=getElementIndex(pos);
            Element child=children[index];
            int p0=child.getStartOffset();
            int p1=child.getEndOffset();
            if((pos>=p0)&&(pos<p1)){
                return child;
            }
            return null;
        }

        public void replace(int offset,int length,Element[] elems){
            int delta=elems.length-length;
            int src=offset+length;
            int nmove=nchildren-src;
            int dest=src+delta;
            if((nchildren+delta)>=children.length){
                // need to grow the array
                int newLength=Math.max(2*children.length,nchildren+delta);
                AbstractElement[] newChildren=new AbstractElement[newLength];
                System.arraycopy(children,0,newChildren,0,offset);
                System.arraycopy(elems,0,newChildren,offset,elems.length);
                System.arraycopy(children,src,newChildren,dest,nmove);
                children=newChildren;
            }else{
                // patch the existing array
                System.arraycopy(children,src,children,dest,nmove);
                System.arraycopy(elems,0,children,offset,elems.length);
            }
            nchildren=nchildren+delta;
        }        public int getStartOffset(){
            return children[0].getStartOffset();
        }

        public String toString(){
            return "BranchElement("+getName()+") "+getStartOffset()+","+
                    getEndOffset()+"\n";
        }        public int getEndOffset(){
            Element child=
                    (nchildren>0)?children[nchildren-1]:children[0];
            return child.getEndOffset();
        }

        public String getName(){
            String nm=super.getName();
            if(nm==null){
                nm=ParagraphElementName;
            }
            return nm;
        }        public Element getElement(int index){
            if(index<nchildren){
                return children[index];
            }
            return null;
        }

        public int getElementCount(){
            return nchildren;
        }

        public int getElementIndex(int offset){
            int index;
            int lower=0;
            int upper=nchildren-1;
            int mid=0;
            int p0=getStartOffset();
            int p1;
            if(nchildren==0){
                return 0;
            }
            if(offset>=getEndOffset()){
                return nchildren-1;
            }
            // see if the last index can be used.
            if((lastIndex>=lower)&&(lastIndex<=upper)){
                Element lastHit=children[lastIndex];
                p0=lastHit.getStartOffset();
                p1=lastHit.getEndOffset();
                if((offset>=p0)&&(offset<p1)){
                    return lastIndex;
                }
                // last index wasn't a hit, but it does give useful info about
                // where a hit (if any) would be.
                if(offset<p0){
                    upper=lastIndex;
                }else{
                    lower=lastIndex;
                }
            }
            while(lower<=upper){
                mid=lower+((upper-lower)/2);
                Element elem=children[mid];
                p0=elem.getStartOffset();
                p1=elem.getEndOffset();
                if((offset>=p0)&&(offset<p1)){
                    // found the location
                    index=mid;
                    lastIndex=index;
                    return index;
                }else if(offset<p0){
                    upper=mid-1;
                }else{
                    lower=mid+1;
                }
            }
            // didn't find it, but we indicate the index of where it would belong
            if(offset<p0){
                index=mid;
            }else{
                index=mid+1;
            }
            lastIndex=index;
            return index;
        }

        public boolean isLeaf(){
            return false;
        }
        // ------ TreeNode ----------------------------------------------

        public boolean getAllowsChildren(){
            return true;
        }

        public Enumeration children(){
            if(nchildren==0)
                return null;
            Vector<AbstractElement> tempVector=new Vector<AbstractElement>(nchildren);
            for(int counter=0;counter<nchildren;counter++)
                tempVector.addElement(children[counter]);
            return tempVector.elements();
        }



    }

    public class LeafElement extends AbstractElement{
        // ---- members -----------------------------------------------------
        private transient Position p0;
        private transient Position p1;        public String toString(){
            return "LeafElement("+getName()+") "+p0+","+p1+"\n";
        }
        // --- Element methods ---------------------------------------------

        public LeafElement(Element parent,AttributeSet a,int offs0,int offs1){
            super(parent,a);
            try{
                p0=createPosition(offs0);
                p1=createPosition(offs1);
            }catch(BadLocationException e){
                p0=null;
                p1=null;
                throw new StateInvariantError("Can't create Position references");
            }
        }        public int getStartOffset(){
            return p0.getOffset();
        }

        private void writeObject(ObjectOutputStream s) throws IOException{
            s.defaultWriteObject();
            s.writeInt(p0.getOffset());
            s.writeInt(p1.getOffset());
        }        public int getEndOffset(){
            return p1.getOffset();
        }

        private void readObject(ObjectInputStream s)
                throws ClassNotFoundException, IOException{
            s.defaultReadObject();
            // set the range with positions that track change
            int off0=s.readInt();
            int off1=s.readInt();
            try{
                p0=createPosition(off0);
                p1=createPosition(off1);
            }catch(BadLocationException e){
                p0=null;
                p1=null;
                throw new IOException("Can't restore Position references");
            }
        }        public String getName(){
            String nm=super.getName();
            if(nm==null){
                nm=ContentElementName;
            }
            return nm;
        }

        public int getElementIndex(int pos){
            return -1;
        }

        public Element getElement(int index){
            return null;
        }

        public int getElementCount(){
            return 0;
        }

        public boolean isLeaf(){
            return true;
        }
        // ------ TreeNode ----------------------------------------------

        public boolean getAllowsChildren(){
            return false;
        }

        public Enumeration children(){
            return null;
        }
        // --- serialization ---------------------------------------------






    }

    class BidiRootElement extends BranchElement{
        BidiRootElement(){
            super(null,null);
        }

        public String getName(){
            return "bidi root";
        }
    }

    class BidiElement extends LeafElement{
        BidiElement(Element parent,int start,int end,int level){
            super(parent,new SimpleAttributeSet(),start,end);
            addAttribute(StyleConstants.BidiLevel,Integer.valueOf(level));
            //System.out.println("BidiElement: start = " + start
            //                   + " end = " + end + " level = " + level );
        }

        public String getName(){
            return BidiElementName;
        }

        boolean isLeftToRight(){
            return ((getLevel()%2)==0);
        }

        int getLevel(){
            Integer o=(Integer)getAttribute(StyleConstants.BidiLevel);
            if(o!=null){
                return o.intValue();
            }
            return 0;  // Level 0 is base level (non-embedded) left-to-right
        }
    }

    public class DefaultDocumentEvent extends CompoundEdit implements DocumentEvent{
        // --- member variables ------------------------------------
        private int offset;
        private int length;
        // --- CompoundEdit methods --------------------------
        private Hashtable<Element,ElementChange> changeLookup;
        private EventType type;

        public DefaultDocumentEvent(int offs,int len,EventType type){
            super();
            offset=offs;
            length=len;
            this.type=type;
        }

        public void undo() throws CannotUndoException{
            writeLock();
            try{
                // change the state
                super.undo();
                // fire a DocumentEvent to notify the view(s)
                UndoRedoDocumentEvent ev=new UndoRedoDocumentEvent(this,true);
                if(type==EventType.REMOVE){
                    fireInsertUpdate(ev);
                }else if(type==EventType.INSERT){
                    fireRemoveUpdate(ev);
                }else{
                    fireChangedUpdate(ev);
                }
            }finally{
                writeUnlock();
            }
        }

        public void redo() throws CannotRedoException{
            writeLock();
            try{
                // change the state
                super.redo();
                // fire a DocumentEvent to notify the view(s)
                UndoRedoDocumentEvent ev=new UndoRedoDocumentEvent(this,false);
                if(type==EventType.INSERT){
                    fireInsertUpdate(ev);
                }else if(type==EventType.REMOVE){
                    fireRemoveUpdate(ev);
                }else{
                    fireChangedUpdate(ev);
                }
            }finally{
                writeUnlock();
            }
        }

        public boolean addEdit(UndoableEdit anEdit){
            // if the number of changes gets too great, start using
            // a hashtable for to locate the change for a given element.
            if((changeLookup==null)&&(edits.size()>10)){
                changeLookup=new Hashtable<Element,ElementChange>();
                int n=edits.size();
                for(int i=0;i<n;i++){
                    Object o=edits.elementAt(i);
                    if(o instanceof ElementChange){
                        ElementChange ec=(ElementChange)o;
                        changeLookup.put(ec.getElement(),ec);
                    }
                }
            }
            // if we have a hashtable... add the entry if it's
            // an ElementChange.
            if((changeLookup!=null)&&(anEdit instanceof ElementChange)){
                ElementChange ec=(ElementChange)anEdit;
                changeLookup.put(ec.getElement(),ec);
            }
            return super.addEdit(anEdit);
        }

        public boolean isSignificant(){
            return true;
        }
        // --- DocumentEvent methods --------------------------

        public String getPresentationName(){
            EventType type=getType();
            if(type==EventType.INSERT)
                return UIManager.getString("AbstractDocument.additionText");
            if(type==EventType.REMOVE)
                return UIManager.getString("AbstractDocument.deletionText");
            return UIManager.getString("AbstractDocument.styleChangeText");
        }        public EventType getType(){
            return type;
        }

        public String getUndoPresentationName(){
            return UIManager.getString("AbstractDocument.undoText")+" "+
                    getPresentationName();
        }

        public String getRedoPresentationName(){
            return UIManager.getString("AbstractDocument.redoText")+" "+
                    getPresentationName();
        }        public int getLength(){
            return length;
        }

        public String toString(){
            return edits.toString();
        }        public Document getDocument(){
            return AbstractDocument.this;
        }

        public int getOffset(){
            return offset;
        }        public ElementChange getChange(Element elem){
            if(changeLookup!=null){
                return changeLookup.get(elem);
            }
            int n=edits.size();
            for(int i=0;i<n;i++){
                Object o=edits.elementAt(i);
                if(o instanceof ElementChange){
                    ElementChange c=(ElementChange)o;
                    if(elem.equals(c.getElement())){
                        return c;
                    }
                }
            }
            return null;
        }




    }

    class UndoRedoDocumentEvent implements DocumentEvent{
        private DefaultDocumentEvent src=null;
        private EventType type=null;

        public UndoRedoDocumentEvent(DefaultDocumentEvent src,boolean isUndo){
            this.src=src;
            if(isUndo){
                if(src.getType().equals(EventType.INSERT)){
                    type=EventType.REMOVE;
                }else if(src.getType().equals(EventType.REMOVE)){
                    type=EventType.INSERT;
                }else{
                    type=src.getType();
                }
            }else{
                type=src.getType();
            }
        }

        public DefaultDocumentEvent getSource(){
            return src;
        }

        // DocumentEvent methods delegated to DefaultDocumentEvent source
        // except getType() which depends on operation (Undo or Redo).
        public int getOffset(){
            return src.getOffset();
        }

        public int getLength(){
            return src.getLength();
        }

        public Document getDocument(){
            return src.getDocument();
        }

        public EventType getType(){
            return type;
        }

        public ElementChange getChange(Element elem){
            return src.getChange(elem);
        }
    }

    private class DefaultFilterBypass extends DocumentFilter.FilterBypass{
        public Document getDocument(){
            return AbstractDocument.this;
        }

        public void remove(int offset,int length) throws
                BadLocationException{
            handleRemove(offset,length);
        }

        public void insertString(int offset,String string,
                                 AttributeSet attr) throws
                BadLocationException{
            handleInsertString(offset,string,attr);
        }

        public void replace(int offset,int length,String text,
                            AttributeSet attrs) throws BadLocationException{
            handleRemove(offset,length);
            handleInsertString(offset,text,attrs);
        }
    }






































}
