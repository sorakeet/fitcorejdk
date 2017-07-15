/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.undo;

import javax.swing.*;
import java.io.Serializable;

public class AbstractUndoableEdit implements UndoableEdit, Serializable{
    protected static final String UndoName="Undo";
    protected static final String RedoName="Redo";
    boolean hasBeenDone;
    boolean alive;

    public AbstractUndoableEdit(){
        super();
        hasBeenDone=true;
        alive=true;
    }

    public void undo() throws CannotUndoException{
        if(!canUndo()){
            throw new CannotUndoException();
        }
        hasBeenDone=false;
    }

    public boolean canUndo(){
        return alive&&hasBeenDone;
    }

    public void redo() throws CannotRedoException{
        if(!canRedo()){
            throw new CannotRedoException();
        }
        hasBeenDone=true;
    }

    public boolean canRedo(){
        return alive&&!hasBeenDone;
    }

    public void die(){
        alive=false;
    }

    public boolean addEdit(UndoableEdit anEdit){
        return false;
    }

    public boolean replaceEdit(UndoableEdit anEdit){
        return false;
    }

    public boolean isSignificant(){
        return true;
    }

    public String getPresentationName(){
        return "";
    }

    public String getUndoPresentationName(){
        String name=getPresentationName();
        if(!"".equals(name)){
            name=UIManager.getString("AbstractUndoableEdit.undoText")+
                    " "+name;
        }else{
            name=UIManager.getString("AbstractUndoableEdit.undoText");
        }
        return name;
    }

    public String getRedoPresentationName(){
        String name=getPresentationName();
        if(!"".equals(name)){
            name=UIManager.getString("AbstractUndoableEdit.redoText")+
                    " "+name;
        }else{
            name=UIManager.getString("AbstractUndoableEdit.redoText");
        }
        return name;
    }

    public String toString(){
        return super.toString()
                +" hasBeenDone: "+hasBeenDone
                +" alive: "+alive;
    }
}
