/**
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import sun.misc.Signal;
import sun.misc.SignalHandler;

class Terminator{
    private static SignalHandler handler=null;

    static void setup(){
        if(handler!=null) return;
        SignalHandler sh=new SignalHandler(){
            public void handle(Signal sig){
                Shutdown.exit(sig.getNumber()+0200);
            }
        };
        handler=sh;
        // When -Xrs is specified the user is responsible for
        // ensuring that shutdown hooks are run by calling
        // System.exit()
        try{
            Signal.handle(new Signal("INT"),sh);
        }catch(IllegalArgumentException e){
        }
        try{
            Signal.handle(new Signal("TERM"),sh);
        }catch(IllegalArgumentException e){
        }
    }

    static void teardown(){
        /** The current sun.misc.Signal class does not support
         * the cancellation of handlers
         */
    }
}
