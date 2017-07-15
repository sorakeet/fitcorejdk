/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.ietf.jgss;

public class MessageProp{
    private boolean privacyState;
    private int qop;
    private boolean dupToken;
    private boolean oldToken;
    private boolean unseqToken;
    private boolean gapToken;
    private int minorStatus;
    private String minorString;

    public MessageProp(boolean privState){
        this(0,privState);
    }

    public MessageProp(int qop,boolean privState){
        this.qop=qop;
        this.privacyState=privState;
        resetStatusValues();
    }

    private void resetStatusValues(){
        dupToken=false;
        oldToken=false;
        unseqToken=false;
        gapToken=false;
        minorStatus=0;
        minorString=null;
    }

    public int getQOP(){
        return qop;
    }

    public void setQOP(int qop){
        this.qop=qop;
    }

    public boolean getPrivacy(){
        return (privacyState);
    }

    public void setPrivacy(boolean privState){
        this.privacyState=privState;
    }

    public boolean isDuplicateToken(){
        return dupToken;
    }

    public boolean isOldToken(){
        return oldToken;
    }

    public boolean isUnseqToken(){
        return unseqToken;
    }

    public boolean isGapToken(){
        return gapToken;
    }

    public int getMinorStatus(){
        return minorStatus;
    }

    public String getMinorString(){
        return minorString;
    }

    public void setSupplementaryStates(boolean duplicate,
                                       boolean old,boolean unseq,boolean gap,
                                       int minorStatus,String minorString){
        this.dupToken=duplicate;
        this.oldToken=old;
        this.unseqToken=unseq;
        this.gapToken=gap;
        this.minorStatus=minorStatus;
        this.minorString=minorString;
    }
}
