/**
 * Copyright (c) 1999, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

import java.io.IOException;
import java.io.InputStream;

public class AudioInputStream extends InputStream{
    protected AudioFormat format;
    protected long frameLength;
    protected int frameSize;
    protected long framePos;
    private InputStream stream;
    private long markpos;
    private byte[] pushBackBuffer=null;
    private int pushBackLen=0;
    private byte[] markPushBackBuffer=null;
    private int markPushBackLen=0;

    public AudioInputStream(InputStream stream,AudioFormat format,long length){
        super();
        this.format=format;
        this.frameLength=length;
        this.frameSize=format.getFrameSize();
        // any frameSize that is not well-defined will
        // cause that this stream will be read in bytes
        if(this.frameSize==AudioSystem.NOT_SPECIFIED||frameSize<=0){
            this.frameSize=1;
        }
        this.stream=stream;
        framePos=0;
        markpos=0;
    }

    public AudioInputStream(TargetDataLine line){
        TargetDataLineInputStream tstream=new TargetDataLineInputStream(line);
        format=line.getFormat();
        frameLength=AudioSystem.NOT_SPECIFIED;
        frameSize=format.getFrameSize();
        if(frameSize==AudioSystem.NOT_SPECIFIED||frameSize<=0){
            frameSize=1;
        }
        this.stream=tstream;
        framePos=0;
        markpos=0;
    }

    public AudioFormat getFormat(){
        return format;
    }

    public long getFrameLength(){
        return frameLength;
    }

    public int read() throws IOException{
        if(frameSize!=1){
            throw new IOException("cannot read a single byte if frame size > 1");
        }
        byte[] data=new byte[1];
        int temp=read(data);
        if(temp<=0){
            // we have a weird situation if read(byte[]) returns 0!
            return -1;
        }
        return data[0]&0xFF;
    }

    private class TargetDataLineInputStream extends InputStream{
        TargetDataLine line;

        TargetDataLineInputStream(TargetDataLine line){
            super();
            this.line=line;
        }

        public int available() throws IOException{
            return line.available();
        }

        //$$fb 2001-07-16: added this method to correctly close the underlying TargetDataLine.
        // fixes bug 4479984
        public void close() throws IOException{
            // the line needs to be flushed and stopped to avoid a dead lock...
            // Probably related to bugs 4417527, 4334868, 4383457
            if(line.isActive()){
                line.flush();
                line.stop();
            }
            line.close();
        }

        public int read() throws IOException{
            byte[] b=new byte[1];
            int value=read(b,0,1);
            if(value==-1){
                return -1;
            }
            value=(int)b[0];
            if(line.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)){
                value+=128;
            }
            return value;
        }

        public int read(byte[] b,int off,int len) throws IOException{
            try{
                return line.read(b,off,len);
            }catch(IllegalArgumentException e){
                throw new IOException(e.getMessage());
            }
        }
    }    public int read(byte[] b) throws IOException{
        return read(b,0,b.length);
    }

    public int read(byte[] b,int off,int len) throws IOException{
        // make sure we don't read fractions of a frame.
        if((len%frameSize)!=0){
            len-=(len%frameSize);
            if(len==0){
                return 0;
            }
        }
        if(frameLength!=AudioSystem.NOT_SPECIFIED){
            if(framePos>=frameLength){
                return -1;
            }else{
                // don't try to read beyond our own set length in frames
                if((len/frameSize)>(frameLength-framePos)){
                    len=(int)(frameLength-framePos)*frameSize;
                }
            }
        }
        int bytesRead=0;
        int thisOff=off;
        // if we've bytes left from last call to read(),
        // use them first
        if(pushBackLen>0&&len>=pushBackLen){
            System.arraycopy(pushBackBuffer,0,
                    b,off,pushBackLen);
            thisOff+=pushBackLen;
            len-=pushBackLen;
            bytesRead+=pushBackLen;
            pushBackLen=0;
        }
        int thisBytesRead=stream.read(b,thisOff,len);
        if(thisBytesRead==-1){
            return -1;
        }
        if(thisBytesRead>0){
            bytesRead+=thisBytesRead;
        }
        if(bytesRead>0){
            pushBackLen=bytesRead%frameSize;
            if(pushBackLen>0){
                // copy everything we got from the beginning of the frame
                // to our pushback buffer
                if(pushBackBuffer==null){
                    pushBackBuffer=new byte[frameSize];
                }
                System.arraycopy(b,off+bytesRead-pushBackLen,
                        pushBackBuffer,0,pushBackLen);
                bytesRead-=pushBackLen;
            }
            // make sure to update our framePos
            framePos+=bytesRead/frameSize;
        }
        return bytesRead;
    }

    public long skip(long n) throws IOException{
        // make sure not to skip fractional frames
        if((n%frameSize)!=0){
            n-=(n%frameSize);
        }
        if(frameLength!=AudioSystem.NOT_SPECIFIED){
            // don't skip more than our set length in frames.
            if((n/frameSize)>(frameLength-framePos)){
                n=(frameLength-framePos)*frameSize;
            }
        }
        long temp=stream.skip(n);
        // if no error, update our position.
        if(temp%frameSize!=0){
            // Throw an IOException if we've skipped a fractional number of frames
            throw new IOException("Could not skip an integer number of frames.");
        }
        if(temp>=0){
            framePos+=temp/frameSize;
        }
        return temp;
    }

    public int available() throws IOException{
        int temp=stream.available();
        // don't return greater than our set length in frames
        if((frameLength!=AudioSystem.NOT_SPECIFIED)&&((temp/frameSize)>(frameLength-framePos))){
            return (int)(frameLength-framePos)*frameSize;
        }else{
            return temp;
        }
    }

    public void close() throws IOException{
        stream.close();
    }

    public void mark(int readlimit){
        stream.mark(readlimit);
        if(markSupported()){
            markpos=framePos;
            // remember the pushback buffer
            markPushBackLen=pushBackLen;
            if(markPushBackLen>0){
                if(markPushBackBuffer==null){
                    markPushBackBuffer=new byte[frameSize];
                }
                System.arraycopy(pushBackBuffer,0,markPushBackBuffer,0,markPushBackLen);
            }
        }
    }

    public void reset() throws IOException{
        stream.reset();
        framePos=markpos;
        // re-create the pushback buffer
        pushBackLen=markPushBackLen;
        if(pushBackLen>0){
            if(pushBackBuffer==null){
                pushBackBuffer=new byte[frameSize-1];
            }
            System.arraycopy(markPushBackBuffer,0,pushBackBuffer,0,pushBackLen);
        }
    }

    public boolean markSupported(){
        return stream.markSupported();
    }


}
