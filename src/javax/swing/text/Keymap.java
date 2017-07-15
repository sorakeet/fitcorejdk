/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;

public interface Keymap{
    public String getName();

    public Action getDefaultAction();

    public void setDefaultAction(Action a);

    public Action getAction(KeyStroke key);

    public KeyStroke[] getBoundKeyStrokes();

    public Action[] getBoundActions();

    public KeyStroke[] getKeyStrokesForAction(Action a);

    public boolean isLocallyDefined(KeyStroke key);

    public void addActionForKeyStroke(KeyStroke key,Action a);

    public void removeKeyStrokeBinding(KeyStroke keys);

    public void removeBindings();

    public Keymap getResolveParent();

    public void setResolveParent(Keymap parent);
}
