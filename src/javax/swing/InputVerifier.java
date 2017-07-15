/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

public abstract class InputVerifier{
    public boolean shouldYieldFocus(JComponent input){
        return verify(input);
    }

    public abstract boolean verify(JComponent input);
}
