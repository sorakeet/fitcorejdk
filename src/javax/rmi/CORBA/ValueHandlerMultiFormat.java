/**
 * Copyright (c) 2002, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.rmi.CORBA;

public interface ValueHandlerMultiFormat extends ValueHandler{
    byte getMaximumStreamFormatVersion();

    void writeValue(org.omg.CORBA.portable.OutputStream out,
                    java.io.Serializable value,
                    byte streamFormatVersion);
}
