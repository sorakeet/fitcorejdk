/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

import java.util.List;

public interface MidiDevice extends AutoCloseable{
    public Info getDeviceInfo();

    public void open() throws MidiUnavailableException;

    public void close();

    public boolean isOpen();

    public long getMicrosecondPosition();

    public int getMaxReceivers();

    public int getMaxTransmitters();

    public Receiver getReceiver() throws MidiUnavailableException;

    List<Receiver> getReceivers();

    public Transmitter getTransmitter() throws MidiUnavailableException;

    List<Transmitter> getTransmitters();

    public static class Info{
        private String name;
        private String vendor;
        private String description;
        private String version;

        protected Info(String name,String vendor,String description,String version){
            this.name=name;
            this.vendor=vendor;
            this.description=description;
            this.version=version;
        }

        public final int hashCode(){
            return super.hashCode();
        }

        public final boolean equals(Object obj){
            return super.equals(obj);
        }

        public final String toString(){
            return name;
        }

        public final String getName(){
            return name;
        }

        public final String getVendor(){
            return vendor;
        }

        public final String getDescription(){
            return description;
        }

        public final String getVersion(){
            return version;
        }
    } // class Info
}
