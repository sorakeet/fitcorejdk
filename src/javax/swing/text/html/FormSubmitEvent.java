/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.Element;
import java.net.URL;

public class FormSubmitEvent extends HTMLFrameHyperlinkEvent{
    private MethodType method;

    ;
    private String data;

    FormSubmitEvent(Object source,EventType type,URL targetURL,
                    Element sourceElement,String targetFrame,
                    MethodType method,String data){
        super(source,type,targetURL,sourceElement,targetFrame);
        this.method=method;
        this.data=data;
    }

    public MethodType getMethod(){
        return method;
    }

    public String getData(){
        return data;
    }
    public enum MethodType{GET,POST}
}
