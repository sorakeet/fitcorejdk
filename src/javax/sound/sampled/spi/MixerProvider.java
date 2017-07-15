/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled.spi;

import javax.sound.sampled.Mixer;

public abstract class MixerProvider{
    public boolean isMixerSupported(Mixer.Info info){
        Mixer.Info infos[]=getMixerInfo();
        for(int i=0;i<infos.length;i++){
            if(info.equals(infos[i])){
                return true;
            }
        }
        return false;
    }

    public abstract Mixer.Info[] getMixerInfo();

    public abstract Mixer getMixer(Mixer.Info info);
}
