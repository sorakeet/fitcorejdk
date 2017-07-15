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
 * Written by Doug Lea and Josh Bloch with assistance from members of JCP
 * JSR-166 Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/**
 *
 *
 *
 *
 *
 * Written by Doug Lea and Josh Bloch with assistance from members of JCP
 * JSR-166 Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util;

public interface NavigableMap<K,V> extends SortedMap<K,V>{
    Entry<K,V> lowerEntry(K key);

    K lowerKey(K key);

    Entry<K,V> floorEntry(K key);

    K floorKey(K key);

    Entry<K,V> ceilingEntry(K key);

    K ceilingKey(K key);

    Entry<K,V> higherEntry(K key);

    K higherKey(K key);

    Entry<K,V> firstEntry();

    Entry<K,V> lastEntry();

    Entry<K,V> pollFirstEntry();

    Entry<K,V> pollLastEntry();

    NavigableMap<K,V> descendingMap();

    NavigableSet<K> navigableKeySet();

    NavigableSet<K> descendingKeySet();

    NavigableMap<K,V> subMap(K fromKey,boolean fromInclusive,
                             K toKey,boolean toInclusive);

    NavigableMap<K,V> headMap(K toKey,boolean inclusive);

    NavigableMap<K,V> tailMap(K fromKey,boolean inclusive);

    SortedMap<K,V> subMap(K fromKey,K toKey);

    SortedMap<K,V> headMap(K toKey);

    SortedMap<K,V> tailMap(K fromKey);
}
