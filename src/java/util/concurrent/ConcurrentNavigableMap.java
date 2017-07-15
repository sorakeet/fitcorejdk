/**
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/**
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent;

import java.util.NavigableMap;
import java.util.NavigableSet;

public interface ConcurrentNavigableMap<K,V>
        extends ConcurrentMap<K,V>, NavigableMap<K,V>{
    ConcurrentNavigableMap<K,V> descendingMap();

    public NavigableSet<K> navigableKeySet();

    public NavigableSet<K> descendingKeySet();

    ConcurrentNavigableMap<K,V> subMap(K fromKey,boolean fromInclusive,
                                       K toKey,boolean toInclusive);

    ConcurrentNavigableMap<K,V> headMap(K toKey,boolean inclusive);

    ConcurrentNavigableMap<K,V> tailMap(K fromKey,boolean inclusive);

    ConcurrentNavigableMap<K,V> subMap(K fromKey,K toKey);

    ConcurrentNavigableMap<K,V> headMap(K toKey);

    ConcurrentNavigableMap<K,V> tailMap(K fromKey);

    NavigableSet<K> keySet();
}
