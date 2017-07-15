/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public class ShortMessage extends MidiMessage{
    // Status byte defines
    // System common messages
    public static final int MIDI_TIME_CODE=0xF1; // 241
    public static final int SONG_POSITION_POINTER=0xF2; // 242
    public static final int SONG_SELECT=0xF3; // 243
    public static final int TUNE_REQUEST=0xF6; // 246
    public static final int END_OF_EXCLUSIVE=0xF7; // 247
    // System real-time messages
    public static final int TIMING_CLOCK=0xF8; // 248
    public static final int START=0xFA; // 250
    public static final int CONTINUE=0xFB; // 251
    public static final int STOP=0xFC; //252
    public static final int ACTIVE_SENSING=0xFE; // 254
    public static final int SYSTEM_RESET=0xFF; // 255
    // Channel voice message upper nibble defines
    public static final int NOTE_OFF=0x80;  // 128
    public static final int NOTE_ON=0x90;  // 144
    public static final int POLY_PRESSURE=0xA0;  // 160
    public static final int CONTROL_CHANGE=0xB0;  // 176
    public static final int PROGRAM_CHANGE=0xC0;  // 192
    public static final int CHANNEL_PRESSURE=0xD0;  // 208
    public static final int PITCH_BEND=0xE0;  // 224
    // Instance variables

    public ShortMessage(){
        this(new byte[3]);
        // Default message data: NOTE_ON on Channel 0 with max volume
        data[0]=(byte)(NOTE_ON&0xFF);
        data[1]=(byte)64;
        data[2]=(byte)127;
        length=3;
    }

    // $$fb this should throw an Exception in case of an illegal message!
    protected ShortMessage(byte[] data){
        // $$fb this may set an invalid message.
        // Can't correct without compromising compatibility
        super(data);
    }

    public ShortMessage(int status) throws InvalidMidiDataException{
        super(null);
        setMessage(status); // can throw InvalidMidiDataException
    }

    public void setMessage(int status) throws InvalidMidiDataException{
        // check for valid values
        int dataLength=getDataLength(status); // can throw InvalidMidiDataException
        if(dataLength!=0){
            throw new InvalidMidiDataException("Status byte; "+status+" requires "+dataLength+" data bytes");
        }
        setMessage(status,0,0);
    }

    public void setMessage(int status,int data1,int data2) throws InvalidMidiDataException{
        // check for valid values
        int dataLength=getDataLength(status); // can throw InvalidMidiDataException
        if(dataLength>0){
            if(data1<0||data1>127){
                throw new InvalidMidiDataException("data1 out of range: "+data1);
            }
            if(dataLength>1){
                if(data2<0||data2>127){
                    throw new InvalidMidiDataException("data2 out of range: "+data2);
                }
            }
        }
        // set the length
        length=dataLength+1;
        // re-allocate array if ShortMessage(byte[]) constructor gave array with fewer elements
        if(data==null||data.length<length){
            data=new byte[3];
        }
        // set the data
        data[0]=(byte)(status&0xFF);
        if(length>1){
            data[1]=(byte)(data1&0xFF);
            if(length>2){
                data[2]=(byte)(data2&0xFF);
            }
        }
    }

    protected final int getDataLength(int status) throws InvalidMidiDataException{
        // system common and system real-time messages
        switch(status){
            case 0xF6:                      // Tune Request
            case 0xF7:                      // EOX
                // System real-time messages
            case 0xF8:                      // Timing Clock
            case 0xF9:                      // Undefined
            case 0xFA:                      // Start
            case 0xFB:                      // Continue
            case 0xFC:                      // Stop
            case 0xFD:                      // Undefined
            case 0xFE:                      // Active Sensing
            case 0xFF:                      // System Reset
                return 0;
            case 0xF1:                      // MTC Quarter Frame
            case 0xF3:                      // Song Select
                return 1;
            case 0xF2:                      // Song Position Pointer
                return 2;
            default:
        }
        // channel voice and mode messages
        switch(status&0xF0){
            case 0x80:
            case 0x90:
            case 0xA0:
            case 0xB0:
            case 0xE0:
                return 2;
            case 0xC0:
            case 0xD0:
                return 1;
            default:
                throw new InvalidMidiDataException("Invalid status byte: "+status);
        }
    }

    public ShortMessage(int status,int data1,int data2)
            throws InvalidMidiDataException{
        super(null);
        setMessage(status,data1,data2); // can throw InvalidMidiDataException
    }

    public ShortMessage(int command,int channel,int data1,int data2)
            throws InvalidMidiDataException{
        super(null);
        setMessage(command,channel,data1,data2);
    }

    public void setMessage(int command,int channel,int data1,int data2) throws InvalidMidiDataException{
        // check for valid values
        if(command>=0xF0||command<0x80){
            throw new InvalidMidiDataException("command out of range: 0x"+Integer.toHexString(command));
        }
        if((channel&0xFFFFFFF0)!=0){ // <=> (channel<0 || channel>15)
            throw new InvalidMidiDataException("channel out of range: "+channel);
        }
        setMessage((command&0xF0)|(channel&0x0F),data1,data2);
    }

    public int getChannel(){
        // this returns 0 if an invalid message is set
        return (getStatus()&0x0F);
    }

    public int getCommand(){
        // this returns 0 if an invalid message is set
        return (getStatus()&0xF0);
    }

    public int getData1(){
        if(length>1){
            return (data[1]&0xFF);
        }
        return 0;
    }

    public int getData2(){
        if(length>2){
            return (data[2]&0xFF);
        }
        return 0;
    }

    public Object clone(){
        byte[] newData=new byte[length];
        System.arraycopy(data,0,newData,0,newData.length);
        ShortMessage msg=new ShortMessage(newData);
        return msg;
    }
}
