/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * $Id: DOMStructure.java,v 1.6 2005/05/09 18:33:26 mullan Exp $
 */
/**
 * $Id: DOMStructure.java,v 1.6 2005/05/09 18:33:26 mullan Exp $
 */
package javax.xml.crypto.dom;

import org.w3c.dom.Node;

import javax.xml.crypto.XMLStructure;

public class DOMStructure implements XMLStructure{
    private final Node node;

    public DOMStructure(Node node){
        if(node==null){
            throw new NullPointerException("node cannot be null");
        }
        this.node=node;
    }

    public Node getNode(){
        return node;
    }

    public boolean isFeatureSupported(String feature){
        if(feature==null){
            throw new NullPointerException();
        }else{
            return false;
        }
    }
}
