/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.attachment;

import javax.activation.DataHandler;

public abstract class AttachmentUnmarshaller{
    public abstract DataHandler getAttachmentAsDataHandler(String cid);

    public abstract byte[] getAttachmentAsByteArray(String cid);

    public boolean isXOPPackage(){
        return false;
    }
}
