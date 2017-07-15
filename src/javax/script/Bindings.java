/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.script;

import java.util.Map;

public interface Bindings extends Map<String,Object>{
    public boolean containsKey(Object key);

    public Object get(Object key);

    public Object put(String name,Object value);

    public Object remove(Object key);

    public void putAll(Map<? extends String,? extends Object> toMerge);
}
