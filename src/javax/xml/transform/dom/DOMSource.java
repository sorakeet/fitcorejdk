/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.dom;

import org.w3c.dom.Node;

import javax.xml.transform.Source;

public class DOMSource implements Source{
    public static final String FEATURE=
            "http://javax.xml.transform.dom.DOMSource/feature";
    private Node node;
    private String systemID;

    public DOMSource(){
    }

    public DOMSource(Node n){
        setNode(n);
    }

    public DOMSource(Node node,String systemID){
        setNode(node);
        setSystemId(systemID);
    }

    public Node getNode(){
        return node;
    }

    public void setNode(Node node){
        this.node=node;
    }

    public void setSystemId(String systemID){
        this.systemID=systemID;
    }

    public String getSystemId(){
        return this.systemID;
    }
}
