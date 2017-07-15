/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

public class W3CDomHandler implements DomHandler<Element,DOMResult>{
    private DocumentBuilder builder;

    public W3CDomHandler(){
        this.builder=null;
    }

    public W3CDomHandler(DocumentBuilder builder){
        if(builder==null)
            throw new IllegalArgumentException();
        this.builder=builder;
    }

    public DocumentBuilder getBuilder(){
        return builder;
    }

    public void setBuilder(DocumentBuilder builder){
        this.builder=builder;
    }

    public DOMResult createUnmarshaller(ValidationEventHandler errorHandler){
        if(builder==null)
            return new DOMResult();
        else
            return new DOMResult(builder.newDocument());
    }

    public Element getElement(DOMResult r){
        // JAXP spec is ambiguous about what really happens in this case,
        // so work defensively
        Node n=r.getNode();
        if(n instanceof Document){
            return ((Document)n).getDocumentElement();
        }
        if(n instanceof Element)
            return (Element)n;
        if(n instanceof DocumentFragment)
            return (Element)n.getChildNodes().item(0);
        // if the result object contains something strange,
        // it is not a user problem, but it is a JAXB provider's problem.
        // That's why we throw a runtime exception.
        throw new IllegalStateException(n.toString());
    }

    public Source marshal(Element element,ValidationEventHandler errorHandler){
        return new DOMSource(element);
    }
}
