/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.callback;

public class TextOutputCallback implements Callback, java.io.Serializable{
    public static final int INFORMATION=0;
    public static final int WARNING=1;
    public static final int ERROR=2;
    private static final long serialVersionUID=1689502495511663102L;
    private int messageType;
    private String message;

    public TextOutputCallback(int messageType,String message){
        if((messageType!=INFORMATION&&
                messageType!=WARNING&&messageType!=ERROR)||
                message==null||message.length()==0)
            throw new IllegalArgumentException();
        this.messageType=messageType;
        this.message=message;
    }

    public int getMessageType(){
        return messageType;
    }

    public String getMessage(){
        return message;
    }
}
