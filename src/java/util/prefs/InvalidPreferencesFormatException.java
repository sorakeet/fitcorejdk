/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.prefs;

public class InvalidPreferencesFormatException extends Exception{
    private static final long serialVersionUID=-791715184232119669L;

    public InvalidPreferencesFormatException(Throwable cause){
        super(cause);
    }

    public InvalidPreferencesFormatException(String message){
        super(message);
    }

    public InvalidPreferencesFormatException(String message,Throwable cause){
        super(message,cause);
    }
}
