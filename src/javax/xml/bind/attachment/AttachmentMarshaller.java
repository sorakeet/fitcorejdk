/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.attachment;

import javax.activation.DataHandler;

public abstract class AttachmentMarshaller{
    public abstract String addMtomAttachment(DataHandler data,String elementNamespace,String elementLocalName);

    public abstract String addMtomAttachment(byte[] data,int offset,int length,String mimeType,String elementNamespace,String elementLocalName);

    public boolean isXOPPackage(){
        return false;
    }

    public abstract String addSwaRefAttachment(DataHandler data);
}
