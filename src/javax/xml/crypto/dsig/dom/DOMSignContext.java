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
 * $Id: DOMSignContext.java,v 1.9 2005/05/10 16:31:14 mullan Exp $
 */
/**
 * $Id: DOMSignContext.java,v 1.9 2005/05/10 16:31:14 mullan Exp $
 */
package javax.xml.crypto.dsig.dom;

import org.w3c.dom.Node;

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.XMLSignContext;
import java.security.Key;

public class DOMSignContext extends DOMCryptoContext implements XMLSignContext{
    private Node parent;
    private Node nextSibling;

    public DOMSignContext(Key signingKey,Node parent){
        if(signingKey==null){
            throw new NullPointerException("signingKey cannot be null");
        }
        if(parent==null){
            throw new NullPointerException("parent cannot be null");
        }
        setKeySelector(KeySelector.singletonKeySelector(signingKey));
        this.parent=parent;
    }

    public DOMSignContext(Key signingKey,Node parent,Node nextSibling){
        if(signingKey==null){
            throw new NullPointerException("signingKey cannot be null");
        }
        if(parent==null){
            throw new NullPointerException("parent cannot be null");
        }
        if(nextSibling==null){
            throw new NullPointerException("nextSibling cannot be null");
        }
        setKeySelector(KeySelector.singletonKeySelector(signingKey));
        this.parent=parent;
        this.nextSibling=nextSibling;
    }

    public DOMSignContext(KeySelector ks,Node parent){
        if(ks==null){
            throw new NullPointerException("key selector cannot be null");
        }
        if(parent==null){
            throw new NullPointerException("parent cannot be null");
        }
        setKeySelector(ks);
        this.parent=parent;
    }

    public DOMSignContext(KeySelector ks,Node parent,Node nextSibling){
        if(ks==null){
            throw new NullPointerException("key selector cannot be null");
        }
        if(parent==null){
            throw new NullPointerException("parent cannot be null");
        }
        if(nextSibling==null){
            throw new NullPointerException("nextSibling cannot be null");
        }
        setKeySelector(ks);
        this.parent=parent;
        this.nextSibling=nextSibling;
    }

    public Node getParent(){
        return parent;
    }

    public void setParent(Node parent){
        if(parent==null){
            throw new NullPointerException("parent is null");
        }
        this.parent=parent;
    }

    public Node getNextSibling(){
        return nextSibling;
    }

    public void setNextSibling(Node nextSibling){
        this.nextSibling=nextSibling;
    }
}
