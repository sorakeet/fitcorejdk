/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.metadata;

import org.w3c.dom.Node;

import javax.imageio.IIOException;

public class IIOInvalidTreeException extends IIOException{
    protected Node offendingNode=null;

    public IIOInvalidTreeException(String message,Node offendingNode){
        super(message);
        this.offendingNode=offendingNode;
    }

    public IIOInvalidTreeException(String message,Throwable cause,
                                   Node offendingNode){
        super(message,cause);
        this.offendingNode=offendingNode;
    }

    public Node getOffendingNode(){
        return offendingNode;
    }
}
