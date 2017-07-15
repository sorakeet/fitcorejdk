/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import java.awt.*;

public class InsetsUIResource extends Insets implements UIResource{
    public InsetsUIResource(int top,int left,int bottom,int right){
        super(top,left,bottom,right);
    }
}
