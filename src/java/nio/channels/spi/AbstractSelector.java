/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels.spi;

import sun.nio.ch.Interruptible;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractSelector
        extends Selector{
    // The provider that created this selector
    private final SelectorProvider provider;
    private final Set<SelectionKey> cancelledKeys=new HashSet<SelectionKey>();
    private AtomicBoolean selectorOpen=new AtomicBoolean(true);
    // -- Interruption machinery --
    private Interruptible interruptor=null;

    protected AbstractSelector(SelectorProvider provider){
        this.provider=provider;
    }

    void cancel(SelectionKey k){                       // package-private
        synchronized(cancelledKeys){
            cancelledKeys.add(k);
        }
    }

    public final boolean isOpen(){
        return selectorOpen.get();
    }

    public final SelectorProvider provider(){
        return provider;
    }

    public final void close() throws IOException{
        boolean open=selectorOpen.getAndSet(false);
        if(!open)
            return;
        implCloseSelector();
    }

    protected abstract void implCloseSelector() throws IOException;

    protected final Set<SelectionKey> cancelledKeys(){
        return cancelledKeys;
    }

    protected abstract SelectionKey register(AbstractSelectableChannel ch,
                                             int ops,Object att);

    protected final void deregister(AbstractSelectionKey key){
        ((AbstractSelectableChannel)key.channel()).removeKey(key);
    }

    protected final void begin(){
        if(interruptor==null){
            interruptor=new Interruptible(){
                public void interrupt(Thread ignore){
                    AbstractSelector.this.wakeup();
                }
            };
        }
        AbstractInterruptibleChannel.blockedOn(interruptor);
        Thread me=Thread.currentThread();
        if(me.isInterrupted())
            interruptor.interrupt(me);
    }

    protected final void end(){
        AbstractInterruptibleChannel.blockedOn(null);
    }
}
