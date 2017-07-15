/**
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.undo;

public interface UndoableEdit{
    public void undo() throws CannotUndoException;

    public boolean canUndo();

    public void redo() throws CannotRedoException;

    public boolean canRedo();

    public void die();

    public boolean addEdit(UndoableEdit anEdit);

    public boolean replaceEdit(UndoableEdit anEdit);

    public boolean isSignificant();

    public String getPresentationName();

    public String getUndoPresentationName();

    public String getRedoPresentationName();
}
