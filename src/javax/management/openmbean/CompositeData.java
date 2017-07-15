/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import java.util.Collection;
// jmx import
//

public interface CompositeData{
    public CompositeType getCompositeType();

    public Object get(String key);

    public Object[] getAll(String[] keys);

    public boolean containsKey(String key);

    public boolean containsValue(Object value);

    public Collection<?> values();

    public int hashCode();

    public boolean equals(Object obj);

    public String toString();
}
