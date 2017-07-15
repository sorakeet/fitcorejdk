/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

public interface SOAPHeaderElement extends SOAPElement{
    public String getActor();

    public void setActor(String actorURI);

    public String getRole();

    public void setRole(String uri) throws SOAPException;

    public boolean getMustUnderstand();

    public void setMustUnderstand(boolean mustUnderstand);

    public boolean getRelay();

    public void setRelay(boolean relay) throws SOAPException;
}
