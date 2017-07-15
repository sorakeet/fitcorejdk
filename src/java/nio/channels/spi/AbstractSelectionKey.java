/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels.spi;

import java.nio.channels.SelectionKey;

public abstract class AbstractSelectionKey
        extends SelectionKey{
    private volatile boolean valid=true;

    protected AbstractSelectionKey(){
    }

    public final boolean isValid(){
        return valid;
    }

    public final void cancel(){
        // Synchronizing "this" to prevent this key from getting canceled
        // multiple times by different threads, which might cause race
        // condition between selector's select() and channel's close().
        synchronized(this){
            if(valid){
                valid=false;
                ((AbstractSelector)selector()).cancel(this);
            }
        }
    }

    void invalidate(){                                 // package-private
        valid=false;
    }
}
