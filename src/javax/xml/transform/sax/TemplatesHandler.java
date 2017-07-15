/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.sax;

import org.xml.sax.ContentHandler;

import javax.xml.transform.Templates;

public interface TemplatesHandler extends ContentHandler{
    public Templates getTemplates();

    public String getSystemId();

    public void setSystemId(String systemID);
}
