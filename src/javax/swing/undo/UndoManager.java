/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.undo;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import java.util.Vector;

public class UndoManager extends CompoundEdit implements UndoableEditListener{
    int indexOfNextAdd;
    int limit;

    public UndoManager(){
        super();
        indexOfNextAdd=0;
        limit=100;
        edits.ensureCapacity(limit);
    }

    public synchronized int getLimit(){
        return limit;
    }

    public synchronized void setLimit(int l){
        if(!inProgress)
            throw new RuntimeException("Attempt to call UndoManager.setLimit() after UndoManager.end() has been called");
        limit=l;
        trimForLimit();
    }

    protected void trimForLimit(){
        if(limit>=0){
            int size=edits.size();
//          System.out.print("limit: " + limit +
//                           " size: " + size +
//                           " indexOfNextAdd: " + indexOfNextAdd +
//                           "\n");
            if(size>limit){
                int halfLimit=limit/2;
                int keepFrom=indexOfNextAdd-1-halfLimit;
                int keepTo=indexOfNextAdd-1+halfLimit;
                // These are ints we're playing with, so dividing by two
                // rounds down for odd numbers, so make sure the limit was
                // honored properly. Note that the keep range is
                // inclusive.
                if(keepTo-keepFrom+1>limit){
                    keepFrom++;
                }
                // The keep range is centered on indexOfNextAdd,
                // but odds are good that the actual edits Vector
                // isn't. Move the keep range to keep it legal.
                if(keepFrom<0){
                    keepTo-=keepFrom;
                    keepFrom=0;
                }
                if(keepTo>=size){
                    int delta=size-keepTo-1;
                    keepTo+=delta;
                    keepFrom+=delta;
                }
//              System.out.println("Keeping " + keepFrom + " " + keepTo);
                trimEdits(keepTo+1,size-1);
                trimEdits(0,keepFrom-1);
            }
        }
    }

    protected void trimEdits(int from,int to){
        if(from<=to){
//          System.out.println("Trimming " + from + " " + to + " with index " +
//                           indexOfNextAdd);
            for(int i=to;from<=i;i--){
                UndoableEdit e=edits.elementAt(i);
//              System.out.println("JUM: Discarding " +
//                                 e.getUndoPresentationName());
                e.die();
                // PENDING(rjrjr) when Vector supports range deletion (JDK
                // 1.2) , we can optimize the next line considerably.
                edits.removeElementAt(i);
            }
            if(indexOfNextAdd>to){
//              System.out.print("...right...");
                indexOfNextAdd-=to-from+1;
            }else if(indexOfNextAdd>=from){
//              System.out.println("...mid...");
                indexOfNextAdd=from;
            }
//          System.out.println("new index " + indexOfNextAdd);
        }
    }

    public synchronized void discardAllEdits(){
        for(UndoableEdit e : edits){
            e.die();
        }
        edits=new Vector<UndoableEdit>();
        indexOfNextAdd=0;
        // PENDING(rjrjr) when vector grows a removeRange() method
        // (expected in JDK 1.2), trimEdits() will be nice and
        // efficient, and this method can call that instead.
    }

    public synchronized void undoOrRedo() throws CannotRedoException,
            CannotUndoException{
        if(indexOfNextAdd==edits.size()){
            undo();
        }else{
            redo();
        }
    }

    public synchronized void undo() throws CannotUndoException{
        if(inProgress){
            UndoableEdit edit=editToBeUndone();
            if(edit==null){
                throw new CannotUndoException();
            }
            undoTo(edit);
        }else{
            super.undo();
        }
    }

    protected UndoableEdit editToBeUndone(){
        int i=indexOfNextAdd;
        while(i>0){
            UndoableEdit edit=edits.elementAt(--i);
            if(edit.isSignificant()){
                return edit;
            }
        }
        return null;
    }

    protected void undoTo(UndoableEdit edit) throws CannotUndoException{
        boolean done=false;
        while(!done){
            UndoableEdit next=edits.elementAt(--indexOfNextAdd);
            next.undo();
            done=next==edit;
        }
    }

    public synchronized void redo() throws CannotRedoException{
        if(inProgress){
            UndoableEdit edit=editToBeRedone();
            if(edit==null){
                throw new CannotRedoException();
            }
            redoTo(edit);
        }else{
            super.redo();
        }
    }

    protected UndoableEdit editToBeRedone(){
        int count=edits.size();
        int i=indexOfNextAdd;
        while(i<count){
            UndoableEdit edit=edits.elementAt(i++);
            if(edit.isSignificant()){
                return edit;
            }
        }
        return null;
    }

    protected void redoTo(UndoableEdit edit) throws CannotRedoException{
        boolean done=false;
        while(!done){
            UndoableEdit next=edits.elementAt(indexOfNextAdd++);
            next.redo();
            done=next==edit;
        }
    }

    public synchronized boolean addEdit(UndoableEdit anEdit){
        boolean retVal;
        // Trim from the indexOfNextAdd to the end, as we'll
        // never reach these edits once the new one is added.
        trimEdits(indexOfNextAdd,edits.size()-1);
        retVal=super.addEdit(anEdit);
        if(inProgress){
            retVal=true;
        }
        // Maybe super added this edit, maybe it didn't (perhaps
        // an in progress compound edit took it instead. Or perhaps
        // this UndoManager is no longer in progress). So make sure
        // the indexOfNextAdd is pointed at the right place.
        indexOfNextAdd=edits.size();
        // Enforce the limit
        trimForLimit();
        return retVal;
    }

    public synchronized void end(){
        super.end();
        this.trimEdits(indexOfNextAdd,edits.size()-1);
    }

    public synchronized boolean canUndo(){
        if(inProgress){
            UndoableEdit edit=editToBeUndone();
            return edit!=null&&edit.canUndo();
        }else{
            return super.canUndo();
        }
    }

    public synchronized boolean canRedo(){
        if(inProgress){
            UndoableEdit edit=editToBeRedone();
            return edit!=null&&edit.canRedo();
        }else{
            return super.canRedo();
        }
    }

    public synchronized String getUndoPresentationName(){
        if(inProgress){
            if(canUndo()){
                return editToBeUndone().getUndoPresentationName();
            }else{
                return UIManager.getString("AbstractUndoableEdit.undoText");
            }
        }else{
            return super.getUndoPresentationName();
        }
    }

    public synchronized String getRedoPresentationName(){
        if(inProgress){
            if(canRedo()){
                return editToBeRedone().getRedoPresentationName();
            }else{
                return UIManager.getString("AbstractUndoableEdit.redoText");
            }
        }else{
            return super.getRedoPresentationName();
        }
    }

    public String toString(){
        return super.toString()+" limit: "+limit+
                " indexOfNextAdd: "+indexOfNextAdd;
    }

    public synchronized boolean canUndoOrRedo(){
        if(indexOfNextAdd==edits.size()){
            return canUndo();
        }else{
            return canRedo();
        }
    }

    public synchronized String getUndoOrRedoPresentationName(){
        if(indexOfNextAdd==edits.size()){
            return getUndoPresentationName();
        }else{
            return getRedoPresentationName();
        }
    }

    public void undoableEditHappened(UndoableEditEvent e){
        addEdit(e.getEdit());
    }
}
