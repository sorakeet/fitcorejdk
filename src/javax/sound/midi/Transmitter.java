/**
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public interface Transmitter extends AutoCloseable{
    public Receiver getReceiver();

    public void setReceiver(Receiver receiver);

    public void close();
}
