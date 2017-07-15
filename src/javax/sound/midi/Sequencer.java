/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

import java.io.IOException;
import java.io.InputStream;

public interface Sequencer extends MidiDevice{
    public static final int LOOP_CONTINUOUSLY=-1;

    public void setSequence(Sequence sequence) throws InvalidMidiDataException;

    public Sequence getSequence();

    public void setSequence(InputStream stream) throws IOException, InvalidMidiDataException;

    public void start();

    public void stop();

    public boolean isRunning();

    public void startRecording();

    public void stopRecording();

    public boolean isRecording();

    public void recordEnable(Track track,int channel);

    public void recordDisable(Track track);

    public float getTempoInBPM();

    public void setTempoInBPM(float bpm);

    public float getTempoInMPQ();

    public void setTempoInMPQ(float mpq);

    public float getTempoFactor();

    public void setTempoFactor(float factor);

    public long getTickLength();

    public long getTickPosition();

    public void setTickPosition(long tick);

    public long getMicrosecondLength();

    public long getMicrosecondPosition();

    public void setMicrosecondPosition(long microseconds);

    public SyncMode getMasterSyncMode();

    public void setMasterSyncMode(SyncMode sync);

    public SyncMode[] getMasterSyncModes();

    public SyncMode getSlaveSyncMode();

    public void setSlaveSyncMode(SyncMode sync);

    public SyncMode[] getSlaveSyncModes();

    public void setTrackMute(int track,boolean mute);

    public boolean getTrackMute(int track);

    public void setTrackSolo(int track,boolean solo);

    public boolean getTrackSolo(int track);

    public boolean addMetaEventListener(MetaEventListener listener);

    public void removeMetaEventListener(MetaEventListener listener);

    public int[] addControllerEventListener(ControllerEventListener listener,int[] controllers);

    public int[] removeControllerEventListener(ControllerEventListener listener,int[] controllers);

    public long getLoopStartPoint();

    public void setLoopStartPoint(long tick);

    public long getLoopEndPoint();

    public void setLoopEndPoint(long tick);

    public int getLoopCount();

    public void setLoopCount(int count);

    public static class SyncMode{
        public static final SyncMode INTERNAL_CLOCK=new SyncMode("Internal Clock");
        public static final SyncMode MIDI_SYNC=new SyncMode("MIDI Sync");
        public static final SyncMode MIDI_TIME_CODE=new SyncMode("MIDI Time Code");
        public static final SyncMode NO_SYNC=new SyncMode("No Timing");
        private String name;

        protected SyncMode(String name){
            this.name=name;
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
    } // class SyncMode
}
