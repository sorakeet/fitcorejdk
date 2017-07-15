/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java_cup.internal.runtime;

import java.util.Stack;

public class virtual_parse_stack{
    /**-----------------------------------------------------------*/
    /**--- (Access to) Instance Variables ------------------------*/
    protected Stack real_stack;
    /**-----------------------------------------------------------*/
    protected int real_next;
    protected Stack vstack;
    /**--- Constructor(s) ----------------------------------------*/
    public virtual_parse_stack(Stack shadowing_stack) throws Exception{
        /** sanity check */
        if(shadowing_stack==null)
            throw new Exception(
                    "Internal parser error: attempt to create null virtual stack");
        /** set up our internals */
        real_stack=shadowing_stack;
        vstack=new Stack();
        real_next=0;
        /** get one element onto the virtual portion of the stack */
        get_from_real();
    }
    /**-----------------------------------------------------------*/

    /**--- General Methods ---------------------------------------*/
    protected void get_from_real(){
        Symbol stack_sym;
        /** don't transfer if the real stack is empty */
        if(real_next>=real_stack.size()) return;
        /** get a copy of the first Symbol we have not transfered */
        stack_sym=(Symbol)real_stack.elementAt(real_stack.size()-1-real_next);
        /** record the transfer */
        real_next++;
        /** put the state number from the Symbol onto the virtual stack */
        vstack.push(new Integer(stack_sym.parse_state));
    }

    public boolean empty(){
        /** if vstack is empty then we were unable to transfer onto it and
         the whole thing is empty. */
        return vstack.empty();
    }

    public int top() throws Exception{
        if(vstack.empty())
            throw new Exception(
                    "Internal parser error: top() called on empty virtual stack");
        return ((Integer)vstack.peek()).intValue();
    }

    public void pop() throws Exception{
        if(vstack.empty())
            throw new Exception(
                    "Internal parser error: pop from empty virtual stack");
        /** pop it */
        vstack.pop();
        /** if we are now empty transfer an element (if there is one) */
        if(vstack.empty())
            get_from_real();
    }

    public void push(int state_num){
        vstack.push(new Integer(state_num));
    }
    /**-----------------------------------------------------------*/
}
