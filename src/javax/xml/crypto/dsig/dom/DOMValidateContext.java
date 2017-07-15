/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: DOMValidateContext.java,v 1.8 2005/05/10 16:31:14 mullan Exp $
 */
/**
 * $Id: DOMValidateContext.java,v 1.8 2005/05/10 16:31:14 mullan Exp $
 */
package javax.xml.crypto.dsig.dom;

import org.w3c.dom.Node;

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.XMLValidateContext;
import java.security.Key;

public class DOMValidateContext extends DOMCryptoContext
        implements XMLValidateContext{
    private Node node;

    public DOMValidateContext(KeySelector ks,Node node){
        if(ks==null){
            throw new NullPointerException("key selector is null");
        }
        init(node,ks);
    }

    private void init(Node node,KeySelector ks){
        if(node==null){
            throw new NullPointerException("node is null");
        }
        this.node=node;
        super.setKeySelector(ks);
        if(System.getSecurityManager()!=null){
            super.setProperty("org.jcp.xml.dsig.secureValidation",
                    Boolean.TRUE);
        }
    }

    public DOMValidateContext(Key validatingKey,Node node){
        if(validatingKey==null){
            throw new NullPointerException("validatingKey is null");
        }
        init(node,KeySelector.singletonKeySelector(validatingKey));
    }

    public Node getNode(){
        return node;
    }

    public void setNode(Node node){
        if(node==null){
            throw new NullPointerException();
        }
        this.node=node;
    }
}
