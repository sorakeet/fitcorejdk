/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

public interface TabableView{
    float getTabbedSpan(float x,TabExpander e);

    float getPartialSpan(int p0,int p1);
}
