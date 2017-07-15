/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public class LineUnavailableException extends Exception{
    public LineUnavailableException(){
        super();
    }

    public LineUnavailableException(String message){
        super(message);
    }
}
