/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import javax.xml.namespace.QName;

public abstract class JAXBIntrospector{
    public static Object getValue(Object jaxbElement){
        if(jaxbElement instanceof JAXBElement){
            return ((JAXBElement)jaxbElement).getValue();
        }else{
            // assume that class of this instance is
            // annotated with @XmlRootElement.
            return jaxbElement;
        }
    }

    public abstract boolean isElement(Object object);

    public abstract QName getElementName(Object jaxbElement);
}
