/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;
import java.awt.event.InputEvent;
import java.net.URL;

public class HTMLFrameHyperlinkEvent extends HyperlinkEvent{
    private String targetFrame;

    public HTMLFrameHyperlinkEvent(Object source,EventType type,URL targetURL,
                                   String targetFrame){
        super(source,type,targetURL);
        this.targetFrame=targetFrame;
    }

    public HTMLFrameHyperlinkEvent(Object source,EventType type,URL targetURL,String desc,
                                   String targetFrame){
        super(source,type,targetURL,desc);
        this.targetFrame=targetFrame;
    }

    public HTMLFrameHyperlinkEvent(Object source,EventType type,URL targetURL,
                                   Element sourceElement,String targetFrame){
        super(source,type,targetURL,null,sourceElement);
        this.targetFrame=targetFrame;
    }

    public HTMLFrameHyperlinkEvent(Object source,EventType type,URL targetURL,String desc,
                                   Element sourceElement,String targetFrame){
        super(source,type,targetURL,desc,sourceElement);
        this.targetFrame=targetFrame;
    }

    public HTMLFrameHyperlinkEvent(Object source,EventType type,URL targetURL,
                                   String desc,Element sourceElement,
                                   InputEvent inputEvent,String targetFrame){
        super(source,type,targetURL,desc,sourceElement,inputEvent);
        this.targetFrame=targetFrame;
    }

    public String getTarget(){
        return targetFrame;
    }
}
