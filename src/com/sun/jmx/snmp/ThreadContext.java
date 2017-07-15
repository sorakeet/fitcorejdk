/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class ThreadContext implements Cloneable{
    private static ThreadLocal<ThreadContext> localContext=
            new ThreadLocal<ThreadContext>();
    /** The context of a thread is stored as a linked list.  At the
     head of the list is the value returned by localContext.get().
     At the tail of the list is a sentinel ThreadContext value with
     "previous" and "key" both null.  There is a different sentinel
     object for each thread.

     Because a null key indicates the sentinel, we reject attempts to
     push context entries with a null key.

     The reason for using a sentinel rather than just terminating
     the list with a null reference is to protect against incorrect
     or even malicious code.  If you have a reference to the
     sentinel value, you can erase the context stack.  Only the
     caller of the first "push" that put something on the stack can
     get such a reference, so if that caller does not give this
     reference away, no one else can erase the stack.

     If the restore method took a null reference to mean an empty
     stack, anyone could erase the stack, since anyone can make a
     null reference.

     When the stack is empty, we discard the sentinel object and
     have localContext.get() return null.  Then we recreate the
     sentinel object on the first subsequent push.

     ThreadContext objects are immutable.  As a consequence, you can
     give a ThreadContext object to setInitialContext that is no
     longer current.  But the interface says this can be rejected,
     in case we remove immutability later.  */
    private /**final*/
            ThreadContext previous;
    private /**final*/
            String key;
    private /**final*/
            Object value;

    private ThreadContext(ThreadContext previous,String key,Object value){
        this.previous=previous;
        this.key=key;
        this.value=value;
    }

    public static Object get(String key) throws IllegalArgumentException{
        ThreadContext context=contextContaining(key);
        if(context==null)
            return null;
        else
            return context.value;
    }

    private static ThreadContext contextContaining(String key)
            throws IllegalArgumentException{
        if(key==null)
            throw new IllegalArgumentException("null key");
        for(ThreadContext context=getContext();
            context!=null;
            context=context.previous){
            if(key.equals(context.key))
                return context;
            /** Note that "context.key" may be null if "context" is the
             sentinel, so don't write "if (context.key.equals(key))"!  */
        }
        return null;
    }
//  /**
//   * Change the value that was most recently associated with the given key
//   * in a <code>push</code> operation not cancelled by a subsequent
//   * <code>restore</code>.  If there is no such association, nothing happens
//   * and the return value is null.
//   *
//   * @param key the key of interest.
//   * @param value the new value to associate with that key.
//   *
//   * @return the value that was previously associated with the key, or null
//   * if the key does not exist in the stack.
//   *
//   * @exception IllegalArgumentException if <code>key</code> is null.
//   */
//  public static Object set(String key, Object value)
//          throws IllegalArgumentException {
//      ThreadContext context = contextContaining(key);
//      if (context == null)
//          return null;
//      Object old = context.value;
//      context.value = value;
//      return old;
//  }

    private static ThreadContext getContext(){
        return localContext.get();
    }

    private static void setContext(ThreadContext context){
        localContext.set(context);
    }

    public static boolean contains(String key)
            throws IllegalArgumentException{
        return (contextContaining(key)!=null);
    }

    public static ThreadContext push(String key,Object value)
            throws IllegalArgumentException{
        if(key==null)
            throw new IllegalArgumentException("null key");
        ThreadContext oldContext=getContext();
        if(oldContext==null)
            oldContext=new ThreadContext(null,null,null);  // make sentinel
        ThreadContext newContext=new ThreadContext(oldContext,key,value);
        setContext(newContext);
        return oldContext;
    }

    public static ThreadContext getThreadContext(){
        return getContext();
    }

    public static void restore(ThreadContext oldContext)
            throws NullPointerException, IllegalArgumentException{
        /** The following test is not strictly necessary in the code as it
         stands today, since the reference to "oldContext.key" would
         generate a NullPointerException anyway.  But if someone
         didn't notice that during subsequent changes, they could
         accidentally permit restore(null) with the semantics of
         trashing the context stack.  */
        if(oldContext==null)
            throw new NullPointerException();
        /** Check that the restored context is in the stack.  */
        for(ThreadContext context=getContext();
            context!=oldContext;
            context=context.previous){
            if(context==null){
                throw new IllegalArgumentException("Restored context is not "+
                        "contained in current "+
                        "context");
            }
        }
        /** Discard the sentinel if the stack is empty.  This means that it
         is an error to call "restore" a second time with the
         ThreadContext value that means an empty stack.  That's why we
         don't say that it is all right to restore the stack to the
         state it was already in.  */
        if(oldContext.key==null)
            oldContext=null;
        setContext(oldContext);
    }

    public void setInitialContext(ThreadContext context)
            throws IllegalArgumentException{
        /** The following test assumes that we discard sentinels when the
         stack is empty.  */
        if(getContext()!=null)
            throw new IllegalArgumentException("previous context not empty");
        setContext(context);
    }
}
