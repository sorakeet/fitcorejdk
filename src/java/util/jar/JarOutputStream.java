/**
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.jar;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JarOutputStream extends ZipOutputStream{
    private static final int JAR_MAGIC=0xCAFE;
    private boolean firstEntry=true;

    public JarOutputStream(OutputStream out,Manifest man) throws IOException{
        super(out);
        if(man==null){
            throw new NullPointerException("man");
        }
        ZipEntry e=new ZipEntry(JarFile.MANIFEST_NAME);
        putNextEntry(e);
        man.write(new BufferedOutputStream(this));
        closeEntry();
    }

    public void putNextEntry(ZipEntry ze) throws IOException{
        if(firstEntry){
            // Make sure that extra field data for first JAR
            // entry includes JAR magic number id.
            byte[] edata=ze.getExtra();
            if(edata==null||!hasMagic(edata)){
                if(edata==null){
                    edata=new byte[4];
                }else{
                    // Prepend magic to existing extra data
                    byte[] tmp=new byte[edata.length+4];
                    System.arraycopy(edata,0,tmp,4,edata.length);
                    edata=tmp;
                }
                set16(edata,0,JAR_MAGIC); // extra field id
                set16(edata,2,0);         // extra field size
                ze.setExtra(edata);
            }
            firstEntry=false;
        }
        super.putNextEntry(ze);
    }

    private static boolean hasMagic(byte[] edata){
        try{
            int i=0;
            while(i<edata.length){
                if(get16(edata,i)==JAR_MAGIC){
                    return true;
                }
                i+=get16(edata,i+2)+4;
            }
        }catch(ArrayIndexOutOfBoundsException e){
            // Invalid extra field data
        }
        return false;
    }

    private static int get16(byte[] b,int off){
        return Byte.toUnsignedInt(b[off])|(Byte.toUnsignedInt(b[off+1])<<8);
    }

    private static void set16(byte[] b,int off,int value){
        b[off+0]=(byte)value;
        b[off+1]=(byte)(value>>8);
    }

    public JarOutputStream(OutputStream out) throws IOException{
        super(out);
    }
}
