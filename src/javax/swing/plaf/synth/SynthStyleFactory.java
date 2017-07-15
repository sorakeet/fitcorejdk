/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;

public abstract class SynthStyleFactory{
    public SynthStyleFactory(){
    }

    public abstract SynthStyle getStyle(JComponent c,Region id);
}
