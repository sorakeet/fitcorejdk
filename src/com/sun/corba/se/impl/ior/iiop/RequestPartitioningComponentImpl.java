/**
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**
 */
package com.sun.corba.se.impl.ior.iiop;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.spi.ior.TaggedComponentBase;
import com.sun.corba.se.spi.ior.iiop.RequestPartitioningComponent;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import org.omg.CORBA_2_3.portable.OutputStream;

public class RequestPartitioningComponentImpl extends TaggedComponentBase
        implements RequestPartitioningComponent{
    private static ORBUtilSystemException wrapper=
            ORBUtilSystemException.get(CORBALogDomains.OA_IOR);
    private int partitionToUse;

    public RequestPartitioningComponentImpl(){
        partitionToUse=0;
    }

    public RequestPartitioningComponentImpl(int thePartitionToUse){
        if(thePartitionToUse<ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID||
                thePartitionToUse>ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID){
            throw wrapper.invalidRequestPartitioningComponentValue(
                    new Integer(thePartitionToUse),
                    new Integer(ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID),
                    new Integer(ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID));
        }
        partitionToUse=thePartitionToUse;
    }

    public int hashCode(){
        return partitionToUse;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof RequestPartitioningComponentImpl))
            return false;
        RequestPartitioningComponentImpl other=
                (RequestPartitioningComponentImpl)obj;
        return partitionToUse==other.partitionToUse;
    }

    public String toString(){
        return "RequestPartitioningComponentImpl[partitionToUse="+partitionToUse+"]";
    }

    public int getRequestPartitioningId(){
        return partitionToUse;
    }

    public void writeContents(OutputStream os){
        os.write_ulong(partitionToUse);
    }

    public int getId(){
        return ORBConstants.TAG_REQUEST_PARTITIONING_ID;
    }
}
