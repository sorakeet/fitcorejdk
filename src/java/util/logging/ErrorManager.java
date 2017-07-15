/**
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

public class ErrorManager{
    public final static int GENERIC_FAILURE=0;
    public final static int WRITE_FAILURE=1;
    public final static int FLUSH_FAILURE=2;
    public final static int CLOSE_FAILURE=3;
    public final static int OPEN_FAILURE=4;
    public final static int FORMAT_FAILURE=5;
    private boolean reported=false;

    public synchronized void error(String msg,Exception ex,int code){
        if(reported){
            // We only report the first error, to avoid clogging
            // the screen.
            return;
        }
        reported=true;
        String text="java.util.logging.ErrorManager: "+code;
        if(msg!=null){
            text=text+": "+msg;
        }
        System.err.println(text);
        if(ex!=null){
            ex.printStackTrace();
        }
    }
}
