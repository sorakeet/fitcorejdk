/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.dom;

import org.w3c.dom.Node;

import javax.xml.transform.Result;

public class DOMResult implements Result{
    public static final String FEATURE="http://javax.xml.transform.dom.DOMResult/feature";
    //////////////////////////////////////////////////////////////////////
    // Internal state.
    //////////////////////////////////////////////////////////////////////
    private Node node=null;
    private Node nextSibling=null;
    private String systemId=null;

    public DOMResult(){
        setNode(null);
        setNextSibling(null);
        setSystemId(null);
    }

    public DOMResult(Node node){
        setNode(node);
        setNextSibling(null);
        setSystemId(null);
    }

    public DOMResult(Node node,String systemId){
        setNode(node);
        setNextSibling(null);
        setSystemId(systemId);
    }

    public DOMResult(Node node,Node nextSibling){
        // does the corrent parent/child relationship exist?
        if(nextSibling!=null){
            // cannot be a sibling of a null node
            if(node==null){
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            }
            // nextSibling contained by node?
            if((node.compareDocumentPosition(nextSibling)&Node.DOCUMENT_POSITION_CONTAINED_BY)==0){
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        setNode(node);
        setNextSibling(nextSibling);
        setSystemId(null);
    }

    public DOMResult(Node node,Node nextSibling,String systemId){
        // does the corrent parent/child relationship exist?
        if(nextSibling!=null){
            // cannot be a sibling of a null node
            if(node==null){
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            }
            // nextSibling contained by node?
            if((node.compareDocumentPosition(nextSibling)&Node.DOCUMENT_POSITION_CONTAINED_BY)==0){
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        setNode(node);
        setNextSibling(nextSibling);
        setSystemId(systemId);
    }

    public Node getNode(){
        return node;
    }

    public void setNode(Node node){
        // does the corrent parent/child relationship exist?
        if(nextSibling!=null){
            // cannot be a sibling of a null node
            if(node==null){
                throw new IllegalStateException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            }
            // nextSibling contained by node?
            if((node.compareDocumentPosition(nextSibling)&Node.DOCUMENT_POSITION_CONTAINED_BY)==0){
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        this.node=node;
    }    public void setSystemId(String systemId){
        this.systemId=systemId;
    }

    public Node getNextSibling(){
        return nextSibling;
    }    public String getSystemId(){
        return systemId;
    }

    public void setNextSibling(Node nextSibling){
        // does the corrent parent/child relationship exist?
        if(nextSibling!=null){
            // cannot be a sibling of a null node
            if(node==null){
                throw new IllegalStateException("Cannot create a DOMResult when the nextSibling is contained by the \"null\" node.");
            }
            // nextSibling contained by node?
            if((node.compareDocumentPosition(nextSibling)&Node.DOCUMENT_POSITION_CONTAINED_BY)==0){
                throw new IllegalArgumentException("Cannot create a DOMResult when the nextSibling is not contained by the node.");
            }
        }
        this.nextSibling=nextSibling;
    }


}
