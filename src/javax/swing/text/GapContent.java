/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Vector;

public class GapContent extends GapVector implements AbstractDocument.Content, Serializable{
    final static int GROWTH_SIZE=1024*512;
    // --- variables --------------------------------------
    private static final char[] empty=new char[0];
    private transient MarkVector marks;
    private transient MarkData search;    protected int getArrayLength(){
        char[] carray=(char[])getArray();
        return carray.length;
    }
    // --- AbstractDocument.Content methods -------------------------
    private transient int unusedMarks=0;
    private transient ReferenceQueue<StickyPosition> queue;

    public GapContent(){
        this(10);
    }

    public GapContent(int initialLength){
        super(Math.max(initialLength,2));
        char[] implied=new char[1];
        implied[0]='\n';
        replace(0,0,implied,implied.length);
        marks=new MarkVector();
        search=new MarkData(0);
        queue=new ReferenceQueue<StickyPosition>();
    }

    protected Object allocateArray(int len){
        return new char[len];
    }

    public Position createPosition(int offset) throws BadLocationException{
        while(queue.poll()!=null){
            unusedMarks++;
        }
        if(unusedMarks>Math.max(5,(marks.size()/10))){
            removeUnusedMarks();
        }
        int g0=getGapStart();
        int g1=getGapEnd();
        int index=(offset<g0)?offset:offset+(g1-g0);
        search.index=index;
        int sortIndex=findSortIndex(search);
        MarkData m;
        StickyPosition position;
        if(sortIndex<marks.size()
                &&(m=marks.elementAt(sortIndex)).index==index
                &&(position=m.getPosition())!=null){
            //position references the correct StickyPostition
        }else{
            position=new StickyPosition();
            m=new MarkData(index,position,queue);
            position.setMark(m);
            marks.insertElementAt(m,sortIndex);
        }
        return position;
    }

    public int length(){
        int len=getArrayLength()-(getGapEnd()-getGapStart());
        return len;
    }

    public UndoableEdit insertString(int where,String str) throws BadLocationException{
        if(where>length()||where<0){
            throw new BadLocationException("Invalid insert",length());
        }
        char[] chars=str.toCharArray();
        replace(where,0,chars,chars.length);
        return new InsertUndo(where,str.length());
    }

    public UndoableEdit remove(int where,int nitems) throws BadLocationException{
        if(where+nitems>=length()){
            throw new BadLocationException("Invalid remove",length()+1);
        }
        String removedString=getString(where,nitems);
        UndoableEdit edit=new RemoveUndo(where,removedString);
        replace(where,nitems,empty,0);
        return edit;
    }

    public String getString(int where,int len) throws BadLocationException{
        Segment s=new Segment();
        getChars(where,len,s);
        return new String(s.array,s.offset,s.count);
    }

    public void getChars(int where,int len,Segment chars) throws BadLocationException{
        int end=where+len;
        if(where<0||end<0){
            throw new BadLocationException("Invalid location",-1);
        }
        if(end>length()||where>length()){
            throw new BadLocationException("Invalid location",length()+1);
        }
        int g0=getGapStart();
        int g1=getGapEnd();
        char[] array=(char[])getArray();
        if((where+len)<=g0){
            // below gap
            chars.array=array;
            chars.offset=where;
        }else if(where>=g0){
            // above gap
            chars.array=array;
            chars.offset=g1+where-g0;
        }else{
            // spans the gap
            int before=g0-where;
            if(chars.isPartialReturn()){
                // partial return allowed, return amount before the gap
                chars.array=array;
                chars.offset=where;
                chars.count=before;
                return;
            }
            // partial return not allowed, must copy
            chars.array=new char[len];
            chars.offset=0;
            System.arraycopy(array,where,chars.array,0,before);
            System.arraycopy(array,g1,chars.array,before,len-before);
        }
        chars.count=len;
    }

    final int findSortIndex(MarkData o){
        int lower=0;
        int upper=marks.size()-1;
        int mid=0;
        if(upper==-1){
            return 0;
        }
        int cmp;
        MarkData last=marks.elementAt(upper);
        cmp=compare(o,last);
        if(cmp>0)
            return upper+1;
        while(lower<=upper){
            mid=lower+((upper-lower)/2);
            MarkData entry=marks.elementAt(mid);
            cmp=compare(o,entry);
            if(cmp==0){
                // found a match
                return mid;
            }else if(cmp<0){
                upper=mid-1;
            }else{
                lower=mid+1;
            }
        }
        // didn't find it, but we indicate the index of where it would belong.
        return (cmp<0)?mid:mid+1;
    }

    final int compare(MarkData o1,MarkData o2){
        if(o1.index<o2.index){
            return -1;
        }else if(o1.index>o2.index){
            return 1;
        }else{
            return 0;
        }
    }

    final void removeUnusedMarks(){
        int n=marks.size();
        MarkVector cleaned=new MarkVector(n);
        for(int i=0;i<n;i++){
            MarkData mark=marks.elementAt(i);
            if(mark.get()!=null){
                cleaned.addElement(mark);
            }
        }
        marks=cleaned;
        unusedMarks=0;
    }
    // --- gap management -------------------------------

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        s.defaultReadObject();
        marks=new MarkVector();
        search=new MarkData(0);
        queue=new ReferenceQueue<StickyPosition>();
    }    protected void shiftEnd(int newSize){
        int oldGapEnd=getGapEnd();
        super.shiftEnd(newSize);
        // Adjust marks.
        int dg=getGapEnd()-oldGapEnd;
        int adjustIndex=findMarkAdjustIndex(oldGapEnd);
        int n=marks.size();
        for(int i=adjustIndex;i<n;i++){
            MarkData mark=marks.elementAt(i);
            mark.index+=dg;
        }
    }

    protected Vector getPositionsInRange(Vector v,int offset,int length){
        int endOffset=offset+length;
        int startIndex;
        int endIndex;
        int g0=getGapStart();
        int g1=getGapEnd();
        // Find the index of the marks.
        if(offset<g0){
            if(offset==0){
                // findMarkAdjustIndex start at 1!
                startIndex=0;
            }else{
                startIndex=findMarkAdjustIndex(offset);
            }
            if(endOffset>=g0){
                endIndex=findMarkAdjustIndex(endOffset+(g1-g0)+1);
            }else{
                endIndex=findMarkAdjustIndex(endOffset+1);
            }
        }else{
            startIndex=findMarkAdjustIndex(offset+(g1-g0));
            endIndex=findMarkAdjustIndex(endOffset+(g1-g0)+1);
        }
        Vector placeIn=(v==null)?new Vector(Math.max(1,endIndex-
                startIndex)):v;
        for(int counter=startIndex;counter<endIndex;counter++){
            placeIn.addElement(new UndoPosRef(marks.elementAt(counter)));
        }
        return placeIn;
    }    int getNewArraySize(int reqSize){
        if(reqSize<GROWTH_SIZE){
            return super.getNewArraySize(reqSize);
        }else{
            return reqSize+GROWTH_SIZE;
        }
    }

    protected void updateUndoPositions(Vector positions,int offset,
                                       int length){
        // Find the indexs of the end points.
        int endOffset=offset+length;
        int g1=getGapEnd();
        int startIndex;
        int endIndex=findMarkAdjustIndex(g1+1);
        if(offset!=0){
            startIndex=findMarkAdjustIndex(g1);
        }else{
            startIndex=0;
        }
        // Reset the location of the refenences.
        for(int counter=positions.size()-1;counter>=0;counter--){
            UndoPosRef ref=(UndoPosRef)positions.elementAt(counter);
            ref.resetLocation(endOffset,g1);
        }
        // We have to resort the marks in the range startIndex to endIndex.
        // We can take advantage of the fact that it will be in
        // increasing order, accept there will be a bunch of MarkData's with
        // the index g1 (or 0 if offset == 0) interspersed throughout.
        if(startIndex<endIndex){
            Object[] sorted=new Object[endIndex-startIndex];
            int addIndex=0;
            int counter;
            if(offset==0){
                // If the offset is 0, the positions won't have incremented,
                // have to do the reverse thing.
                // Find the elements in startIndex whose index is 0
                for(counter=startIndex;counter<endIndex;counter++){
                    MarkData mark=marks.elementAt(counter);
                    if(mark.index==0){
                        sorted[addIndex++]=mark;
                    }
                }
                for(counter=startIndex;counter<endIndex;counter++){
                    MarkData mark=marks.elementAt(counter);
                    if(mark.index!=0){
                        sorted[addIndex++]=mark;
                    }
                }
            }else{
                for(counter=startIndex;counter<endIndex;counter++){
                    MarkData mark=marks.elementAt(counter);
                    if(mark.index!=g1){
                        sorted[addIndex++]=mark;
                    }
                }
                for(counter=startIndex;counter<endIndex;counter++){
                    MarkData mark=marks.elementAt(counter);
                    if(mark.index==g1){
                        sorted[addIndex++]=mark;
                    }
                }
            }
            // And replace
            marks.replaceRange(startIndex,endIndex,sorted);
        }
    }    protected void shiftGap(int newGapStart){
        int oldGapStart=getGapStart();
        int dg=newGapStart-oldGapStart;
        int oldGapEnd=getGapEnd();
        int newGapEnd=oldGapEnd+dg;
        int gapSize=oldGapEnd-oldGapStart;
        // shift gap in the character array
        super.shiftGap(newGapStart);
        // update the marks
        if(dg>0){
            // Move gap up, move data and marks down.
            int adjustIndex=findMarkAdjustIndex(oldGapStart);
            int n=marks.size();
            for(int i=adjustIndex;i<n;i++){
                MarkData mark=marks.elementAt(i);
                if(mark.index>=newGapEnd){
                    break;
                }
                mark.index-=gapSize;
            }
        }else if(dg<0){
            // Move gap down, move data and marks up.
            int adjustIndex=findMarkAdjustIndex(newGapStart);
            int n=marks.size();
            for(int i=adjustIndex;i<n;i++){
                MarkData mark=marks.elementAt(i);
                if(mark.index>=oldGapEnd){
                    break;
                }
                mark.index+=gapSize;
            }
        }
        resetMarksAtZero();
    }

    static class MarkVector extends GapVector{
        MarkData[] oneMark=new MarkData[1];

        MarkVector(){
            super();
        }

        MarkVector(int size){
            super(size);
        }        protected Object allocateArray(int len){
            return new MarkData[len];
        }

        public void addElement(MarkData m){
            insertElementAt(m,size());
        }        protected int getArrayLength(){
            MarkData[] marks=(MarkData[])getArray();
            return marks.length;
        }

        public int size(){
            int len=getArrayLength()-(getGapEnd()-getGapStart());
            return len;
        }

        public void insertElementAt(MarkData m,int index){
            oneMark[0]=m;
            replace(index,0,oneMark,1);
        }

        public MarkData elementAt(int index){
            int g0=getGapStart();
            int g1=getGapEnd();
            MarkData[] array=(MarkData[])getArray();
            if(index<g0){
                // below gap
                return array[index];
            }else{
                // above gap
                index+=g1-g0;
                return array[index];
            }
        }

        protected void replaceRange(int start,int end,Object[] marks){
            int g0=getGapStart();
            int g1=getGapEnd();
            int index=start;
            int newIndex=0;
            Object[] array=(Object[])getArray();
            if(start>=g0){
                // Completely passed gap
                index+=(g1-g0);
                end+=(g1-g0);
            }else if(end>=g0){
                // straddles gap
                end+=(g1-g0);
                while(index<g0){
                    array[index++]=marks[newIndex++];
                }
                index=g1;
            }else{
                // below gap
                while(index<end){
                    array[index++]=marks[newIndex++];
                }
            }
            while(index<end){
                array[index++]=marks[newIndex++];
            }
        }




    }    protected void resetMarksAtZero(){
        if(marks!=null&&getGapStart()==0){
            int g1=getGapEnd();
            for(int counter=0, maxCounter=marks.size();
                counter<maxCounter;counter++){
                MarkData mark=marks.elementAt(counter);
                if(mark.index<=g1){
                    mark.index=0;
                }else{
                    break;
                }
            }
        }
    }

    final class MarkData extends WeakReference<StickyPosition>{
        int index;

        MarkData(int index){
            super(null);
            this.index=index;
        }

        MarkData(int index,StickyPosition position,ReferenceQueue<? super StickyPosition> queue){
            super(position,queue);
            this.index=index;
        }

        public final int getOffset(){
            int g0=getGapStart();
            int g1=getGapEnd();
            int offs=(index<g0)?index:index-(g1-g0);
            return Math.max(offs,0);
        }

        StickyPosition getPosition(){
            return get();
        }
    }    protected void shiftGapStartDown(int newGapStart){
        // Push aside all marks from oldGapStart down to newGapStart.
        int adjustIndex=findMarkAdjustIndex(newGapStart);
        int n=marks.size();
        int g0=getGapStart();
        int g1=getGapEnd();
        for(int i=adjustIndex;i<n;i++){
            MarkData mark=marks.elementAt(i);
            if(mark.index>g0){
                // no more marks to adjust
                break;
            }
            mark.index=g1;
        }
        // shift the gap in the character array
        super.shiftGapStartDown(newGapStart);
        resetMarksAtZero();
    }

    final class StickyPosition implements Position{
        MarkData mark;

        StickyPosition(){
        }

        void setMark(MarkData mark){
            this.mark=mark;
        }

        public String toString(){
            return Integer.toString(getOffset());
        }

        public final int getOffset(){
            return mark.getOffset();
        }
    }    protected void shiftGapEndUp(int newGapEnd){
        int adjustIndex=findMarkAdjustIndex(getGapEnd());
        int n=marks.size();
        for(int i=adjustIndex;i<n;i++){
            MarkData mark=marks.elementAt(i);
            if(mark.index>=newGapEnd){
                break;
            }
            mark.index=newGapEnd;
        }
        // shift the gap in the character array
        super.shiftGapEndUp(newGapEnd);
        resetMarksAtZero();
    }

    final class UndoPosRef{
        protected int undoLocation;
        protected MarkData rec;

        UndoPosRef(MarkData rec){
            this.rec=rec;
            this.undoLocation=rec.getOffset();
        }

        protected void resetLocation(int endOffset,int g1){
            if(undoLocation!=endOffset){
                this.rec.index=undoLocation;
            }else{
                this.rec.index=g1;
            }
        }
    } // End of GapContent.UndoPosRef

    class InsertUndo extends AbstractUndoableEdit{
        protected int offset;
        protected int length;
        protected String string;        public void redo() throws CannotRedoException{
            super.redo();
            try{
                insertString(offset,string);
                string=null;
                // Update the Positions that were in the range removed.
                if(posRefs!=null){
                    updateUndoPositions(posRefs,offset,length);
                    posRefs=null;
                }
            }catch(BadLocationException bl){
                throw new CannotRedoException();
            }
        }
        protected Vector posRefs;
        protected InsertUndo(int offset,int length){
            super();
            this.offset=offset;
            this.length=length;
        }

        public void undo() throws CannotUndoException{
            super.undo();
            try{
                // Get the Positions in the range being removed.
                posRefs=getPositionsInRange(null,offset,length);
                string=getString(offset,length);
                remove(offset,length);
            }catch(BadLocationException bl){
                throw new CannotUndoException();
            }
        }

    } // GapContent.InsertUndo    final int findMarkAdjustIndex(int searchIndex){
        search.index=Math.max(searchIndex,1);
        int index=findSortIndex(search);
        // return the first in the series
        // (ie. there may be duplicates).
        for(int i=index-1;i>=0;i--){
            MarkData d=marks.elementAt(i);
            if(d.index!=search.index){
                break;
            }
            index-=1;
        }
        return index;
    }

    class RemoveUndo extends AbstractUndoableEdit{
        protected int offset;
        protected int length;        public void undo() throws CannotUndoException{
            super.undo();
            try{
                insertString(offset,string);
                // Update the Positions that were in the range removed.
                if(posRefs!=null){
                    updateUndoPositions(posRefs,offset,length);
                    posRefs=null;
                }
                string=null;
            }catch(BadLocationException bl){
                throw new CannotUndoException();
            }
        }
        protected String string;        public void redo() throws CannotRedoException{
            super.redo();
            try{
                string=getString(offset,length);
                // Get the Positions in the range being removed.
                posRefs=getPositionsInRange(null,offset,length);
                remove(offset,length);
            }catch(BadLocationException bl){
                throw new CannotRedoException();
            }
        }
        protected Vector posRefs;
        protected RemoveUndo(int offset,String string){
            super();
            this.offset=offset;
            this.string=string;
            this.length=string.length();
            posRefs=getPositionsInRange(null,offset,length);
        }


    } // GapContent.RemoveUndo




    // --- serialization -------------------------------------


    // --- undo support --------------------------------------










}
