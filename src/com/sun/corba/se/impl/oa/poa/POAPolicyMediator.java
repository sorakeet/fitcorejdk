/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.oa.poa;

import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;

public interface POAPolicyMediator{
    Policies getPolicies();

    int getScid();

    int getServerId();

    Object getInvocationServant(byte[] id,
                                String operation) throws ForwardRequest;

    void returnServant();

    void etherealizeAll();

    void clearAOM();

    ServantManager getServantManager() throws WrongPolicy;

    void setServantManager(ServantManager servantManager) throws WrongPolicy;

    Servant getDefaultServant() throws NoServant, WrongPolicy;

    void setDefaultServant(Servant servant) throws WrongPolicy;

    void activateObject(byte[] id,Servant servant)
            throws ObjectAlreadyActive, ServantAlreadyActive, WrongPolicy;

    Servant deactivateObject(byte[] id) throws ObjectNotActive, WrongPolicy;

    byte[] newSystemId() throws WrongPolicy;

    byte[] servantToId(Servant servant) throws ServantNotActive, WrongPolicy;

    Servant idToServant(byte[] id) throws ObjectNotActive, WrongPolicy;
}
