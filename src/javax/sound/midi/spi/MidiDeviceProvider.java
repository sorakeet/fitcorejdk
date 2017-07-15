/**
 * Copyright (c) 1999, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi.spi;

import javax.sound.midi.MidiDevice;

public abstract class MidiDeviceProvider{
    public boolean isDeviceSupported(MidiDevice.Info info){
        MidiDevice.Info infos[]=getDeviceInfo();
        for(int i=0;i<infos.length;i++){
            if(info.equals(infos[i])){
                return true;
            }
        }
        return false;
    }

    public abstract MidiDevice.Info[] getDeviceInfo();

    public abstract MidiDevice getDevice(MidiDevice.Info info);
}
