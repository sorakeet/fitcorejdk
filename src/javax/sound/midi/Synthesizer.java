/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public interface Synthesizer extends MidiDevice{
    // SYNTHESIZER METHODS

    public int getMaxPolyphony();

    public long getLatency();

    public MidiChannel[] getChannels();

    public VoiceStatus[] getVoiceStatus();

    public boolean isSoundbankSupported(Soundbank soundbank);

    public boolean loadInstrument(Instrument instrument);

    public void unloadInstrument(Instrument instrument);

    public boolean remapInstrument(Instrument from,Instrument to);

    public Soundbank getDefaultSoundbank();

    public Instrument[] getAvailableInstruments();

    public Instrument[] getLoadedInstruments();

    public boolean loadAllInstruments(Soundbank soundbank);

    public void unloadAllInstruments(Soundbank soundbank);

    public boolean loadInstruments(Soundbank soundbank,Patch[] patchList);

    public void unloadInstruments(Soundbank soundbank,Patch[] patchList);
    // RECEIVER METHODS
    /**
     * Obtains the name of the receiver.
     * @return receiver name
     */
    //  public abstract String getName();
    /**
     * Opens the receiver.
     * @throws MidiUnavailableException if the receiver is cannot be opened,
     * usually because the MIDI device is in use by another application.
     * @throws SecurityException if the receiver cannot be opened due to security
     * restrictions.
     */
    //  public abstract void open() throws MidiUnavailableException, SecurityException;
    /**
     * Closes the receiver.
     */
    //  public abstract void close();
    /**
     * Sends a MIDI event to the receiver.
     * @param event event to send.
     * @throws IllegalStateException if the receiver is not open.
     */
    //  public void send(MidiEvent event) throws IllegalStateException {
    //
    //  }
    /**
     * Obtains the set of controls supported by the
     * element.  If no controls are supported, returns an
     * array of length 0.
     * @return set of controls
     */
    // $$kk: 03.04.99: josh bloch recommends getting rid of this:
    // what can you really do with a set of untyped controls??
    // $$kk: 03.05.99: i am putting this back in.  for one thing,
    // you can check the length and know whether you should keep
    // looking....
    // public Control[] getControls();
    /**
     * Obtains the specified control.
     * @param controlClass class of the requested control
     * @return requested control object, or null if the
     * control is not supported.
     */
    // public Control getControl(Class controlClass);
}
