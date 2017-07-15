/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.ietf.jgss;

public class GSSException extends Exception{
    public static final int BAD_BINDINGS=1; //start with 1
    public static final int BAD_MECH=2;
    public static final int BAD_NAME=3;
    public static final int BAD_NAMETYPE=4;
    public static final int BAD_STATUS=5;
    public static final int BAD_MIC=6;
    public static final int CONTEXT_EXPIRED=7;
    public static final int CREDENTIALS_EXPIRED=8;
    public static final int DEFECTIVE_CREDENTIAL=9;
    public static final int DEFECTIVE_TOKEN=10;
    public static final int FAILURE=11;
    public static final int NO_CONTEXT=12;
    public static final int NO_CRED=13;
    public static final int BAD_QOP=14;
    public static final int UNAUTHORIZED=15;
    public static final int UNAVAILABLE=16;
    public static final int DUPLICATE_ELEMENT=17;
    public static final int NAME_NOT_MN=18;
    public static final int DUPLICATE_TOKEN=19;
    public static final int OLD_TOKEN=20;
    public static final int UNSEQ_TOKEN=21;
    public static final int GAP_TOKEN=22;
    private static final long serialVersionUID=-2706218945227726672L;
    private static String[] messages={
            "Channel binding mismatch", // BAD_BINDINGS
            "Unsupported mechanism requested", // BAD_MECH
            "Invalid name provided", // BAD_NAME
            "Name of unsupported type provided", //BAD_NAMETYPE
            "Invalid input status selector", // BAD_STATUS
            "Token had invalid integrity check", // BAD_SIG
            "Specified security context expired", // CONTEXT_EXPIRED
            "Expired credentials detected", // CREDENTIALS_EXPIRED
            "Defective credential detected", // DEFECTIVE_CREDENTIAL
            "Defective token detected", // DEFECTIVE_TOKEN
            "Failure unspecified at GSS-API level", // FAILURE
            "Security context init/accept not yet called or context deleted",
            // NO_CONTEXT
            "No valid credentials provided", // NO_CRED
            "Unsupported QOP value", // BAD_QOP
            "Operation unauthorized", // UNAUTHORIZED
            "Operation unavailable", // UNAVAILABLE
            "Duplicate credential element requested", //DUPLICATE_ELEMENT
            "Name contains multi-mechanism elements", // NAME_NOT_MN
            "The token was a duplicate of an earlier token", //DUPLICATE_TOKEN
            "The token's validity period has expired", //OLD_TOKEN
            "A later token has already been processed", //UNSEQ_TOKEN
            "An expected per-message token was not received", //GAP_TOKEN
    };
    private int major;
    private int minor=0;
    private String minorMessage=null;
    private String majorString=null;

    public GSSException(int majorCode){
        if(validateMajor(majorCode))
            major=majorCode;
        else
            major=FAILURE;
    }

    private boolean validateMajor(int major){
        if(major>0&&major<=messages.length)
            return (true);
        return (false);
    }

    GSSException(int majorCode,String majorString){
        if(validateMajor(majorCode))
            major=majorCode;
        else
            major=FAILURE;
        this.majorString=majorString;
    }

    public GSSException(int majorCode,int minorCode,String minorString){
        if(validateMajor(majorCode))
            major=majorCode;
        else
            major=FAILURE;
        minor=minorCode;
        minorMessage=minorString;
    }

    public int getMajor(){
        return major;
    }

    public int getMinor(){
        return minor;
    }

    public void setMinor(int minorCode,String message){
        minor=minorCode;
        minorMessage=message;
    }

    public String getMajorString(){
        if(majorString!=null)
            return majorString;
        else
            return messages[major-1];
    }

    public String getMinorString(){
        return minorMessage;
    }

    public String toString(){
        return ("GSSException: "+getMessage());
    }

    public String getMessage(){
        if(minor==0)
            return (getMajorString());
        return (getMajorString()
                +" (Mechanism level: "+getMinorString()+")");
    }
}
