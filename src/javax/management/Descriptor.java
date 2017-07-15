/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @author IBM Corp.
 * <p>
 * Copyright IBM Corp. 1999-2000.  All rights reserved.
 */
/**
 * @author IBM Corp.
 *
 * Copyright IBM Corp. 1999-2000.  All rights reserved.
 */
package javax.management;

import java.io.Serializable;
// Javadoc imports:

public interface Descriptor extends Serializable, Cloneable{
    public Object getFieldValue(String fieldName)
            throws RuntimeOperationsException;

    public void setField(String fieldName,Object fieldValue)
            throws RuntimeOperationsException;

    public String[] getFields();

    public String[] getFieldNames();

    public Object[] getFieldValues(String... fieldNames);

    public void removeField(String fieldName);

    public void setFields(String[] fieldNames,Object[] fieldValues)
            throws RuntimeOperationsException;

    public boolean isValid() throws RuntimeOperationsException;

    public int hashCode();

    public boolean equals(Object obj);

    public Object clone() throws RuntimeOperationsException;
}
