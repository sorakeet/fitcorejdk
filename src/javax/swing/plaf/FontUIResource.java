/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import java.awt.*;

public class FontUIResource extends Font implements UIResource{
    public FontUIResource(String name,int style,int size){
        super(name,style,size);
    }

    public FontUIResource(Font font){
        super(font);
    }
}
