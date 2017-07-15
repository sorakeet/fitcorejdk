/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.portable;

import org.omg.CORBA.TypeCode;

public interface Streamable{
    void _read(InputStream istream);

    void _write(OutputStream ostream);

    TypeCode _type();
}
