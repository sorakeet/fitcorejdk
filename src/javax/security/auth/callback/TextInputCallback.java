/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.callback;

public class TextInputCallback implements Callback, java.io.Serializable{
    private static final long serialVersionUID=-8064222478852811804L;
    private String prompt;
    private String defaultText;
    private String inputText;

    public TextInputCallback(String prompt){
        if(prompt==null||prompt.length()==0)
            throw new IllegalArgumentException();
        this.prompt=prompt;
    }

    public TextInputCallback(String prompt,String defaultText){
        if(prompt==null||prompt.length()==0||
                defaultText==null||defaultText.length()==0)
            throw new IllegalArgumentException();
        this.prompt=prompt;
        this.defaultText=defaultText;
    }

    public String getPrompt(){
        return prompt;
    }

    public String getDefaultText(){
        return defaultText;
    }

    public String getText(){
        return inputText;
    }

    public void setText(String text){
        this.inputText=text;
    }
}
