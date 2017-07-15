/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

public interface PropertyEditor{
    Object getValue();

    void setValue(Object value);
    //----------------------------------------------------------------------

    boolean isPaintable();

    void paintValue(java.awt.Graphics gfx,java.awt.Rectangle box);
    //----------------------------------------------------------------------

    String getJavaInitializationString();
    //----------------------------------------------------------------------

    String getAsText();

    void setAsText(String text) throws IllegalArgumentException;
    //----------------------------------------------------------------------

    String[] getTags();
    //----------------------------------------------------------------------

    java.awt.Component getCustomEditor();

    boolean supportsCustomEditor();
    //----------------------------------------------------------------------

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);
}
