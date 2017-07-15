/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import java.util.Collection;
import java.util.Set;
// jmx import
//

public interface TabularData /**extends Map*/
{
    public TabularType getTabularType();

    public Object[] calculateIndex(CompositeData value);

    public int size();

    public boolean isEmpty();

    public boolean containsKey(Object[] key);

    public boolean containsValue(CompositeData value);

    public CompositeData get(Object[] key);

    public void put(CompositeData value);

    public CompositeData remove(Object[] key);

    public void putAll(CompositeData[] values);

    public void clear();

    public Set<?> keySet();

    public Collection<?> values();

    public int hashCode();

    public boolean equals(Object obj);

    public String toString();
}
