/**
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

final class SerializationTester{
    private static ObjectOutputStream stream;

    static{
        try{
            stream=new ObjectOutputStream(new OutputStream(){
                public void write(int b){
                }
            });
        }catch(IOException cannotHappen){
        }
    }

    private SerializationTester(){
    }

    static boolean test(Object obj){
        if(!(obj instanceof Serializable)){
            return false;
        }
        try{
            stream.writeObject(obj);
        }catch(IOException e){
            return false;
        }finally{
            // Fix for 4503661.
            // Reset the stream so that it doesn't keep a reference to the
            // written object.
            try{
                stream.reset();
            }catch(IOException e){
                // Ignore the exception.
            }
        }
        return true;
    }
}
