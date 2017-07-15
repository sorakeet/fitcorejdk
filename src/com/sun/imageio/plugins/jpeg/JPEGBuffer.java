/**
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.imageio.plugins.jpeg;

import javax.imageio.IIOException;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

class JPEGBuffer{
    final int BUFFER_SIZE=4096;
    byte[] buf;
    int bufAvail;
    int bufPtr;
    ImageInputStream iis;
    private boolean debug=false;

    JPEGBuffer(ImageInputStream iis){
        buf=new byte[BUFFER_SIZE];
        bufAvail=0;
        bufPtr=0;
        this.iis=iis;
    }

    void readData(byte[] data) throws IOException{
        int count=data.length;
        // First see what's left in the buffer.
        if(bufAvail>=count){  // It's enough
            System.arraycopy(buf,bufPtr,data,0,count);
            bufAvail-=count;
            bufPtr+=count;
            return;
        }
        int offset=0;
        if(bufAvail>0){  // Some there, but not enough
            System.arraycopy(buf,bufPtr,data,0,bufAvail);
            offset=bufAvail;
            count-=bufAvail;
            bufAvail=0;
            bufPtr=0;
        }
        // Now read the rest directly from the stream
        if(iis.read(data,offset,count)!=count){
            throw new IIOException("Image format Error");
        }
    }

    void skipData(int count) throws IOException{
        // First see what's left in the buffer.
        if(bufAvail>=count){  // It's enough
            bufAvail-=count;
            bufPtr+=count;
            return;
        }
        if(bufAvail>0){  // Some there, but not enough
            count-=bufAvail;
            bufAvail=0;
            bufPtr=0;
        }
        // Now read the rest directly from the stream
        if(iis.skipBytes(count)!=count){
            throw new IIOException("Image format Error");
        }
    }

    void pushBack() throws IOException{
        iis.seek(iis.getStreamPosition()-bufAvail);
        bufAvail=0;
        bufPtr=0;
    }

    long getStreamPosition() throws IOException{
        return (iis.getStreamPosition()-bufAvail);
    }

    boolean scanForFF(JPEGImageReader reader) throws IOException{
        boolean retval=false;
        boolean foundFF=false;
        while(foundFF==false){
            while(bufAvail>0){
                if((buf[bufPtr++]&0xff)==0xff){
                    bufAvail--;
                    foundFF=true;
                    break;  // out of inner while
                }
                bufAvail--;
            }
            // Reload the buffer and keep going
            loadBuf(0);
            // Skip any remaining pad bytes
            if(foundFF==true){
                while((bufAvail>0)&&(buf[bufPtr]&0xff)==0xff){
                    bufPtr++;  // Only if it still is 0xff
                    bufAvail--;
                }
            }
            if(bufAvail==0){  // Premature EOF
                // send out a warning, but treat it as EOI
                //reader.warningOccurred(JPEGImageReader.WARNING_NO_EOI);
                retval=true;
                buf[0]=(byte)JPEG.EOI;
                bufAvail=1;
                bufPtr=0;
                foundFF=true;
            }
        }
        return retval;
    }

    void loadBuf(int count) throws IOException{
        if(debug){
            System.out.print("loadbuf called with ");
            System.out.print("count "+count+", ");
            System.out.println("bufAvail "+bufAvail+", ");
        }
        if(count!=0){
            if(bufAvail>=count){  // have enough
                return;
            }
        }else{
            if(bufAvail==BUFFER_SIZE){  // already full
                return;
            }
        }
        // First copy any remaining bytes down to the beginning
        if((bufAvail>0)&&(bufAvail<BUFFER_SIZE)){
            System.arraycopy(buf,bufPtr,buf,0,bufAvail);
        }
        // Now fill the rest of the buffer
        int ret=iis.read(buf,bufAvail,buf.length-bufAvail);
        if(debug){
            System.out.println("iis.read returned "+ret);
        }
        if(ret!=-1){
            bufAvail+=ret;
        }
        bufPtr=0;
        int minimum=Math.min(BUFFER_SIZE,count);
        if(bufAvail<minimum){
            throw new IIOException("Image Format Error");
        }
    }

    void print(int count){
        System.out.print("buffer has ");
        System.out.print(bufAvail);
        System.out.println(" bytes available");
        if(bufAvail<count){
            count=bufAvail;
        }
        for(int ptr=bufPtr;count>0;count--){
            int val=(int)buf[ptr++]&0xff;
            System.out.print(" "+Integer.toHexString(val));
        }
        System.out.println();
    }
}
