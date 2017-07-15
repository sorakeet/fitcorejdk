/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

public interface Validator{
    public ValidationEventHandler getEventHandler()
            throws JAXBException;

    public void setEventHandler(ValidationEventHandler handler)
            throws JAXBException;

    public boolean validate(Object subrootObj) throws JAXBException;

    public boolean validateRoot(Object rootObj) throws JAXBException;

    public void setProperty(String name,Object value)
            throws PropertyException;

    public Object getProperty(String name) throws PropertyException;
}
