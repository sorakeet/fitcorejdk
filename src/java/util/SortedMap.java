/**
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

public interface SortedMap<K,V> extends Map<K,V>{
    Comparator<? super K> comparator();

    SortedMap<K,V> subMap(K fromKey,K toKey);

    SortedMap<K,V> headMap(K toKey);

    SortedMap<K,V> tailMap(K fromKey);

    K firstKey();

    K lastKey();

    Set<K> keySet();

    Collection<V> values();

    Set<Entry<K,V>> entrySet();
}
