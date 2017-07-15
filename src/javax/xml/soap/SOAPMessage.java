/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public abstract class SOAPMessage{
    public static final String CHARACTER_SET_ENCODING=
            "javax.xml.soap.character-set-encoding";
    public static final String WRITE_XML_DECLARATION=
            "javax.xml.soap.write-xml-declaration";

    public abstract String getContentDescription();

    public abstract void setContentDescription(String description);

    public abstract SOAPPart getSOAPPart();

    public SOAPBody getSOAPBody() throws SOAPException{
        throw new UnsupportedOperationException("getSOAPBody must be overridden by all subclasses of SOAPMessage");
    }

    public SOAPHeader getSOAPHeader() throws SOAPException{
        throw new UnsupportedOperationException("getSOAPHeader must be overridden by all subclasses of SOAPMessage");
    }

    public abstract void removeAllAttachments();

    public abstract int countAttachments();

    public abstract Iterator getAttachments();

    public abstract Iterator getAttachments(MimeHeaders headers);

    public abstract void removeAttachments(MimeHeaders headers);

    public abstract AttachmentPart getAttachment(SOAPElement element) throws SOAPException;

    public abstract void addAttachmentPart(AttachmentPart AttachmentPart);

    public AttachmentPart createAttachmentPart(DataHandler dataHandler){
        AttachmentPart attachment=createAttachmentPart();
        attachment.setDataHandler(dataHandler);
        return attachment;
    }

    public abstract AttachmentPart createAttachmentPart();

    public abstract MimeHeaders getMimeHeaders();

    public AttachmentPart createAttachmentPart(
            Object content,
            String contentType){
        AttachmentPart attachment=createAttachmentPart();
        attachment.setContent(content,contentType);
        return attachment;
    }

    public abstract void saveChanges() throws SOAPException;

    public abstract boolean saveRequired();

    public abstract void writeTo(OutputStream out)
            throws SOAPException, IOException;

    public void setProperty(String property,Object value)
            throws SOAPException{
        throw new UnsupportedOperationException("setProperty must be overridden by all subclasses of SOAPMessage");
    }

    public Object getProperty(String property) throws SOAPException{
        throw new UnsupportedOperationException("getProperty must be overridden by all subclasses of SOAPMessage");
    }
}
