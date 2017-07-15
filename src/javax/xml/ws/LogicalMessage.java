/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.Source;

public interface LogicalMessage{
    public Source getPayload();

    public void setPayload(Source payload);

    public Object getPayload(JAXBContext context);

    public void setPayload(Object payload,JAXBContext context);
}
