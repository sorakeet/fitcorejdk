/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public interface MidiChannel{
    public void noteOn(int noteNumber,int velocity);

    public void noteOff(int noteNumber,int velocity);

    public void noteOff(int noteNumber);

    public void setPolyPressure(int noteNumber,int pressure);

    public int getPolyPressure(int noteNumber);

    public int getChannelPressure();

    public void setChannelPressure(int pressure);

    public void controlChange(int controller,int value);

    public int getController(int controller);

    public void programChange(int program);

    public void programChange(int bank,int program);

    public int getProgram();

    public int getPitchBend();

    public void setPitchBend(int bend);

    public void resetAllControllers();

    public void allNotesOff();

    public void allSoundOff();

    public boolean localControl(boolean on);

    public boolean getMono();

    public void setMono(boolean on);

    public boolean getOmni();

    public void setOmni(boolean on);

    public boolean getMute();

    public void setMute(boolean mute);

    public boolean getSolo();

    public void setSolo(boolean soloState);
}
