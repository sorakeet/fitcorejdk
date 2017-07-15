/**
 * Copyright (c) 1994, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

public interface Observer{
    void update(Observable o,Object arg);
}
