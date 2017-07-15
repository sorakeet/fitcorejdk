/**
 * Copyright (c) 2002, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orb;

import com.sun.corba.se.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.se.impl.legacy.connection.USLPort;
import com.sun.corba.se.pept.transport.Acceptor;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory;
import com.sun.corba.se.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.se.spi.transport.IORToSocketInfo;
import com.sun.corba.se.spi.transport.ReadTimeouts;
import org.omg.PortableInterceptor.ORBInitializer;

public interface ORBData{
    public String getORBInitialHost();

    public int getORBInitialPort();

    public String getORBServerHost();

    public int getORBServerPort();

    public String getListenOnAllInterfaces();

    public com.sun.corba.se.spi.legacy.connection.ORBSocketFactory getLegacySocketFactory();

    public com.sun.corba.se.spi.transport.ORBSocketFactory getSocketFactory();

    public USLPort[] getUserSpecifiedListenPorts();

    public IORToSocketInfo getIORToSocketInfo();

    public IIOPPrimaryToContactInfo getIIOPPrimaryToContactInfo();

    public String getORBId();

    public boolean getORBServerIdPropertySpecified();

    public boolean isLocalOptimizationAllowed();

    public GIOPVersion getGIOPVersion();

    public int getHighWaterMark();

    public int getLowWaterMark();

    public int getNumberToReclaim();

    public int getGIOPFragmentSize();

    public int getGIOPBufferSize();

    public int getGIOPBuffMgrStrategy(GIOPVersion gv);

    public short getGIOPTargetAddressPreference();

    public short getGIOPAddressDisposition();

    public boolean useByteOrderMarkers();

    public boolean useByteOrderMarkersInEncapsulations();

    public boolean alwaysSendCodeSetServiceContext();

    public boolean getPersistentPortInitialized();

    public int getPersistentServerPort();

    public boolean getPersistentServerIdInitialized();

    public int getPersistentServerId();

    public boolean getServerIsORBActivated();

    public Class getBadServerIdHandler();

    public CodeSetComponentInfo getCodeSetComponentInfo();

    public ORBInitializer[] getORBInitializers();

    public StringPair[] getORBInitialReferences();

    public String getORBDefaultInitialReference();

    public String[] getORBDebugFlags();

    public Acceptor[] getAcceptors();

    public CorbaContactInfoListFactory getCorbaContactInfoListFactory();

    public String acceptorSocketType();

    public boolean acceptorSocketUseSelectThreadToWait();

    public boolean acceptorSocketUseWorkerThreadForEvent();

    public String connectionSocketType();

    public boolean connectionSocketUseSelectThreadToWait();

    public boolean connectionSocketUseWorkerThreadForEvent();

    public ReadTimeouts getTransportTCPReadTimeouts();

    public boolean disableDirectByteBufferUse();

    public boolean isJavaSerializationEnabled();

    public boolean useRepId();
}
// End of file.
