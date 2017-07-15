/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public class HeadlessException extends UnsupportedOperationException{
    private static final long serialVersionUID=167183644944358563L;

    public HeadlessException(){
    }

    public HeadlessException(String msg){
        super(msg);
    }

    public String getMessage(){
        String superMessage=super.getMessage();
        String headlessMessage=GraphicsEnvironment.getHeadlessMessage();
        if(superMessage==null){
            return headlessMessage;
        }else if(headlessMessage==null){
            return superMessage;
        }else{
            return superMessage+headlessMessage;
        }
    }
}
