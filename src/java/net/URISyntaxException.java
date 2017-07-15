/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

public class URISyntaxException
        extends Exception{
    private static final long serialVersionUID=2137979680897488891L;
    private String input;
    private int index;

    public URISyntaxException(String input,String reason){
        this(input,reason,-1);
    }

    public URISyntaxException(String input,String reason,int index){
        super(reason);
        if((input==null)||(reason==null))
            throw new NullPointerException();
        if(index<-1)
            throw new IllegalArgumentException();
        this.input=input;
        this.index=index;
    }

    public String getInput(){
        return input;
    }

    public int getIndex(){
        return index;
    }

    public String getMessage(){
        StringBuffer sb=new StringBuffer();
        sb.append(getReason());
        if(index>-1){
            sb.append(" at index ");
            sb.append(index);
        }
        sb.append(": ");
        sb.append(input);
        return sb.toString();
    }

    public String getReason(){
        return super.getMessage();
    }
}
