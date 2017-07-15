/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import java.util.Locale;

public abstract class IIOServiceProvider implements RegisterableService{
    protected String vendorName;
    protected String version;

    public IIOServiceProvider(String vendorName,
                              String version){
        if(vendorName==null){
            throw new IllegalArgumentException("vendorName == null!");
        }
        if(version==null){
            throw new IllegalArgumentException("version == null!");
        }
        this.vendorName=vendorName;
        this.version=version;
    }

    public IIOServiceProvider(){
    }

    public void onRegistration(ServiceRegistry registry,
                               Class<?> category){
    }

    public void onDeregistration(ServiceRegistry registry,
                                 Class<?> category){
    }

    public String getVendorName(){
        return vendorName;
    }

    public String getVersion(){
        return version;
    }

    public abstract String getDescription(Locale locale);
}
