/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public interface IRObjectOperations{
    // read interface
    DefinitionKind def_kind();

    // write interface
    void destroy();
} // interface IRObjectOperations
