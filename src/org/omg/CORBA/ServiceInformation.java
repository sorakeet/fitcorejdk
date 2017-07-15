/**
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class ServiceInformation implements org.omg.CORBA.portable.IDLEntity{
    public int[] service_options;
    public ServiceDetail[] service_details;

    public ServiceInformation(){
    }

    public ServiceInformation(int[] __service_options,
                              ServiceDetail[] __service_details){
        service_options=__service_options;
        service_details=__service_details;
    }
}
