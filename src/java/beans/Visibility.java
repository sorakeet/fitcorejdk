/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

public interface Visibility{
    boolean needsGui();

    void dontUseGui();

    void okToUseGui();

    boolean avoidingGui();
}
