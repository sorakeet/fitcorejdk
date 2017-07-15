/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class Pipe{
    protected Pipe(){
    }

    public static Pipe open() throws IOException{
        return SelectorProvider.provider().openPipe();
    }

    public abstract SourceChannel source();

    public abstract SinkChannel sink();

    public static abstract class SourceChannel
            extends AbstractSelectableChannel
            implements ReadableByteChannel, ScatteringByteChannel{
        protected SourceChannel(SelectorProvider provider){
            super(provider);
        }

        public final int validOps(){
            return SelectionKey.OP_READ;
        }
    }

    public static abstract class SinkChannel
            extends AbstractSelectableChannel
            implements WritableByteChannel, GatheringByteChannel{
        protected SinkChannel(SelectorProvider provider){
            super(provider);
        }

        public final int validOps(){
            return SelectionKey.OP_WRITE;
        }
    }
}
