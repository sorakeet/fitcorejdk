/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.io.Serializable;
import java.util.Vector;

public final class StringContent implements AbstractDocument.Content, Serializable{
    private static final char[] empty=new char[0];
    transient Vector<PosRec> marks;
    private char[] data;
    private int count;

    public StringContent(){
        this(10);
    }

    public StringContent(int initialLength){
        if(initialLength<1){
            initialLength=1;
        }
        data=new char[initialLength];
        data[0]='\n';
        count=1;
    }

    public Position createPosition(int offset) throws BadLocationException{
        // some small documents won't have any sticky positions
        // at all, so the buffer is created lazily.
        if(marks==null){
            marks=new Vector<PosRec>();
        }
        return new StickyPosition(offset);
    }

    public int length(){
        return count;
    }
    // --- local methods ---------------------------------------

    public UndoableEdit insertString(int where,String str) throws BadLocationException{
        if(where>=count||where<0){
            throw new BadLocationException("Invalid location",count);
        }
        char[] chars=str.toCharArray();
        replace(where,0,chars,0,chars.length);
        if(marks!=null){
            updateMarksForInsert(where,str.length());
        }
        return new InsertUndo(where,str.length());
    }

    public UndoableEdit remove(int where,int nitems) throws BadLocationException{
        if(where+nitems>=count){
            throw new BadLocationException("Invalid range",count);
        }
        String removedString=getString(where,nitems);
        UndoableEdit edit=new RemoveUndo(where,removedString);
        replace(where,nitems,empty,0,0);
        if(marks!=null){
            updateMarksForRemove(where,nitems);
        }
        return edit;
    }

    public String getString(int where,int len) throws BadLocationException{
        if(where+len>count){
            throw new BadLocationException("Invalid range",count);
        }
        return new String(data,where,len);
    }

    public void getChars(int where,int len,Segment chars) throws BadLocationException{
        if(where+len>count){
            throw new BadLocationException("Invalid location",count);
        }
        chars.array=data;
        chars.offset=where;
        chars.count=len;
    }

    synchronized void updateMarksForRemove(int offset,int length){
        int n=marks.size();
        for(int i=0;i<n;i++){
            PosRec mark=marks.elementAt(i);
            if(mark.unused){
                // this record is no longer used, get rid of it
                marks.removeElementAt(i);
                i-=1;
                n-=1;
            }else if(mark.offset>=(offset+length)){
                mark.offset-=length;
            }else if(mark.offset>=offset){
                mark.offset=offset;
            }
        }
    }

    void replace(int offset,int length,
                 char[] replArray,int replOffset,int replLength){
        int delta=replLength-length;
        int src=offset+length;
        int nmove=count-src;
        int dest=src+delta;
        if((count+delta)>=data.length){
            // need to grow the array
            int newLength=Math.max(2*data.length,count+delta);
            char[] newData=new char[newLength];
            System.arraycopy(data,0,newData,0,offset);
            System.arraycopy(replArray,replOffset,newData,offset,replLength);
            System.arraycopy(data,src,newData,dest,nmove);
            data=newData;
        }else{
            // patch the existing array
            System.arraycopy(data,src,data,dest,nmove);
            System.arraycopy(replArray,replOffset,data,offset,replLength);
        }
        count=count+delta;
    }

    synchronized void updateMarksForInsert(int offset,int length){
        if(offset==0){
            // zero is a special case where we update only
            // marks after it.
            offset=1;
        }
        int n=marks.size();
        for(int i=0;i<n;i++){
            PosRec mark=marks.elementAt(i);
            if(mark.unused){
                // this record is no longer used, get rid of it
                marks.removeElementAt(i);
                i-=1;
                n-=1;
            }else if(mark.offset>=offset){
                mark.offset+=length;
            }
        }
    }

    void resize(int ncount){
        char[] ndata=new char[ncount];
        System.arraycopy(data,0,ndata,0,Math.min(ncount,count));
        data=ndata;
    }

    protected Vector getPositionsInRange(Vector v,int offset,
                                         int length){
        int n=marks.size();
        int end=offset+length;
        Vector placeIn=(v==null)?new Vector():v;
        for(int i=0;i<n;i++){
            PosRec mark=marks.elementAt(i);
            if(mark.unused){
                // this record is no longer used, get rid of it
                marks.removeElementAt(i);
                i-=1;
                n-=1;
            }else if(mark.offset>=offset&&mark.offset<=end)
                placeIn.addElement(new UndoPosRef(mark));
        }
        return placeIn;
    }

    protected void updateUndoPositions(Vector positions){
        for(int counter=positions.size()-1;counter>=0;counter--){
            UndoPosRef ref=(UndoPosRef)positions.elementAt(counter);
            // Check if the Position is still valid.
            if(ref.rec.unused){
                positions.removeElementAt(counter);
            }else
                ref.resetLocation();
        }
    }

    final class PosRec{
        int offset;
        boolean unused;
        PosRec(int offset){
            this.offset=offset;
        }
    }

    final class StickyPosition implements Position{
        PosRec rec;

        StickyPosition(int offset){
            rec=new PosRec(offset);
            marks.addElement(rec);
        }

        public String toString(){
            return Integer.toString(getOffset());
        }

        public int getOffset(){
            return rec.offset;
        }

        protected void finalize() throws Throwable{
            // schedule the record to be removed later
            // on another thread.
            rec.unused=true;
        }
    }

    final class UndoPosRef{
        protected int undoLocation;
        protected PosRec rec;

        UndoPosRef(PosRec rec){
            this.rec=rec;
            this.undoLocation=rec.offset;
        }

        protected void resetLocation(){
            rec.offset=undoLocation;
        }
    }

    class InsertUndo extends AbstractUndoableEdit{
        // Where the string goes.
        protected int offset;
        // Length of the string.
        protected int length;
        // The string that was inserted. To cut down on space needed this
        // will only be valid after an undo.
        protected String string;        public void redo() throws CannotRedoException{
            super.redo();
            try{
                synchronized(StringContent.this){
                    insertString(offset,string);
                    string=null;
                    // Update the Positions that were in the range removed.
                    if(posRefs!=null){
                        updateUndoPositions(posRefs);
                        posRefs=null;
                    }
                }
            }catch(BadLocationException bl){
                throw new CannotRedoException();
            }
        }
        // An array of instances of UndoPosRef for the Positions in the
        // range that was removed, valid after undo.
        protected Vector posRefs;
        protected InsertUndo(int offset,int length){
            super();
            this.offset=offset;
            this.length=length;
        }

        public void undo() throws CannotUndoException{
            super.undo();
            try{
                synchronized(StringContent.this){
                    // Get the Positions in the range being removed.
                    if(marks!=null)
                        posRefs=getPositionsInRange(null,offset,length);
                    string=getString(offset,length);
                    remove(offset,length);
                }
            }catch(BadLocationException bl){
                throw new CannotUndoException();
            }
        }

    }

    class RemoveUndo extends AbstractUndoableEdit{
        // Where the string goes.
        protected int offset;
        // Length of the string.
        protected int length;        public void undo() throws CannotUndoException{
            super.undo();
            try{
                synchronized(StringContent.this){
                    insertString(offset,string);
                    // Update the Positions that were in the range removed.
                    if(posRefs!=null){
                        updateUndoPositions(posRefs);
                        posRefs=null;
                    }
                    string=null;
                }
            }catch(BadLocationException bl){
                throw new CannotUndoException();
            }
        }
        // The string that was inserted. This will be null after an undo.
        protected String string;        public void redo() throws CannotRedoException{
            super.redo();
            try{
                synchronized(StringContent.this){
                    string=getString(offset,length);
                    // Get the Positions in the range being removed.
                    if(marks!=null)
                        posRefs=getPositionsInRange(null,offset,length);
                    remove(offset,length);
                }
            }catch(BadLocationException bl){
                throw new CannotRedoException();
            }
        }
        // An array of instances of UndoPosRef for the Positions in the
        // range that was removed, valid before undo.
        protected Vector posRefs;
        protected RemoveUndo(int offset,String string){
            super();
            this.offset=offset;
            this.string=string;
            this.length=string.length();
            if(marks!=null)
                posRefs=getPositionsInRange(null,offset,length);
        }


    }
}
