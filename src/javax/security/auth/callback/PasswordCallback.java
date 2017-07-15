/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.callback;

public class PasswordCallback implements Callback, java.io.Serializable{
    private static final long serialVersionUID=2267422647454909926L;
    private String prompt;
    private boolean echoOn;
    private char[] inputPassword;

    public PasswordCallback(String prompt,boolean echoOn){
        if(prompt==null||prompt.length()==0)
            throw new IllegalArgumentException();
        this.prompt=prompt;
        this.echoOn=echoOn;
    }

    public String getPrompt(){
        return prompt;
    }

    public boolean isEchoOn(){
        return echoOn;
    }

    public char[] getPassword(){
        return (inputPassword==null?null:inputPassword.clone());
    }

    public void setPassword(char[] password){
        this.inputPassword=(password==null?null:password.clone());
    }

    public void clearPassword(){
        if(inputPassword!=null){
            for(int i=0;i<inputPassword.length;i++)
                inputPassword[i]=' ';
        }
    }
}
