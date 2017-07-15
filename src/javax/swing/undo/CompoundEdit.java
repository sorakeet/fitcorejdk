/**
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.undo;

import java.util.Enumeration;
import java.util.Vector;

public class CompoundEdit extends AbstractUndoableEdit{
    protected Vector<UndoableEdit> edits;
    boolean inProgress;

    public CompoundEdit(){
        super();
        inProgress=true;
        edits=new Vector<UndoableEdit>();
    }

    public void die(){
        int size=edits.size();
        for(int i=size-1;i>=0;i--){
            UndoableEdit e=edits.elementAt(i);
//          System.out.println("CompoundEdit(" + i + "): Discarding " +
//                             e.getUndoPresentationName());
            e.die();
        }
        super.die();
    }

    public void undo() throws CannotUndoException{
        super.undo();
        int i=edits.size();
        while(i-->0){
            UndoableEdit e=edits.elementAt(i);
            e.undo();
        }
    }

    public boolean canUndo(){
        return !isInProgress()&&super.canUndo();
    }

    public void redo() throws CannotRedoException{
        super.redo();
        Enumeration cursor=edits.elements();
        while(cursor.hasMoreElements()){
            ((UndoableEdit)cursor.nextElement()).redo();
        }
    }

    public boolean canRedo(){
        return !isInProgress()&&super.canRedo();
    }

    public boolean addEdit(UndoableEdit anEdit){
        if(!inProgress){
            return false;
        }else{
            UndoableEdit last=lastEdit();
            // If this is the first subedit received, just add it.
            // Otherwise, give the last one a chance to absorb the new
            // one.  If it won't, give the new one a chance to absorb
            // the last one.
            if(last==null){
                edits.addElement(anEdit);
            }else if(!last.addEdit(anEdit)){
                if(anEdit.replaceEdit(last)){
                    edits.removeElementAt(edits.size()-1);
                }
                edits.addElement(anEdit);
            }
            return true;
        }
    }

    protected UndoableEdit lastEdit(){
        int count=edits.size();
        if(count>0)
            return edits.elementAt(count-1);
        else
            return null;
    }

    public boolean isSignificant(){
        Enumeration cursor=edits.elements();
        while(cursor.hasMoreElements()){
            if(((UndoableEdit)cursor.nextElement()).isSignificant()){
                return true;
            }
        }
        return false;
    }

    public String getPresentationName(){
        UndoableEdit last=lastEdit();
        if(last!=null){
            return last.getPresentationName();
        }else{
            return super.getPresentationName();
        }
    }

    public String getUndoPresentationName(){
        UndoableEdit last=lastEdit();
        if(last!=null){
            return last.getUndoPresentationName();
        }else{
            return super.getUndoPresentationName();
        }
    }

    public String getRedoPresentationName(){
        UndoableEdit last=lastEdit();
        if(last!=null){
            return last.getRedoPresentationName();
        }else{
            return super.getRedoPresentationName();
        }
    }

    public String toString(){
        return super.toString()
                +" inProgress: "+inProgress
                +" edits: "+edits;
    }

    public boolean isInProgress(){
        return inProgress;
    }

    public void end(){
        inProgress=false;
    }
}
