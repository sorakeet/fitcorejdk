package org.omg.CosNaming;

public interface BindingIteratorOperations{
    boolean next_one(BindingHolder b);

    boolean next_n(int how_many,BindingListHolder bl);

    void destroy();
} // interface BindingIteratorOperations
