/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public abstract class FocusTraversalPolicy{
    public abstract Component getComponentAfter(Container aContainer,
                                                Component aComponent);

    public abstract Component getComponentBefore(Container aContainer,
                                                 Component aComponent);

    public abstract Component getFirstComponent(Container aContainer);

    public abstract Component getLastComponent(Container aContainer);

    public Component getInitialComponent(Window window){
        if(window==null){
            throw new IllegalArgumentException("window cannot be equal to null.");
        }
        Component def=getDefaultComponent(window);
        if(def==null&&window.isFocusableWindow()){
            def=window;
        }
        return def;
    }

    public abstract Component getDefaultComponent(Container aContainer);
}
