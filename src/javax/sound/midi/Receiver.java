/**
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public interface Receiver extends AutoCloseable{
    //$$fb 2002-04-12: fix for 4662090: Contradiction in Receiver specification
    public void send(MidiMessage message,long timeStamp);

    public void close();
}
