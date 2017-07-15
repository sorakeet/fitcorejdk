/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

import java.util.EventObject;

public class FlavorEvent extends EventObject{
    public FlavorEvent(Clipboard source){
        super(source);
    }
}
