/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public interface Soundbank{
    public String getName();

    public String getVersion();

    public String getVendor();

    public String getDescription();

    public SoundbankResource[] getResources();

    public Instrument[] getInstruments();

    public Instrument getInstrument(Patch patch);
}
