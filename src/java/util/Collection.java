package java.util;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Collection<E> extends Iterable<E>{
    // Query Operations

    int size();

    boolean isEmpty();

    boolean contains(Object o);

    Object[] toArray();

    <T> T[] toArray(T[] a);

    boolean add(E e);
    // Modification Operations

    boolean remove(Object o);

    boolean containsAll(Collection<?> c);
    // Bulk Operations

    boolean addAll(Collection<? extends E> c);

    boolean removeAll(Collection<?> c);

    default boolean removeIf(Predicate<? super E> filter){
        Objects.requireNonNull(filter);
        boolean removed=false;
        final Iterator<E> each=iterator();
        while(each.hasNext()){
            if(filter.test(each.next())){
                each.remove();
                removed=true;
            }
        }
        return removed;
    }

    Iterator<E> iterator();

    @Override
    default Spliterator<E> spliterator(){
        return Spliterators.spliterator(this,0);
    }

    boolean retainAll(Collection<?> c);
    // Comparison and hashing

    void clear();

    int hashCode();

    boolean equals(Object o);

    default Stream<E> stream(){
        return StreamSupport.stream(spliterator(),false);
    }

    default Stream<E> parallelStream(){
        return StreamSupport.stream(spliterator(),true);
    }
}
