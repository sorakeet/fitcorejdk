/**
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.security.KeyStore.LoadStoreParameter;
import static java.security.KeyStore.ProtectionParameter;

public final class DomainLoadStoreParameter implements LoadStoreParameter{
    private final URI configuration;
    private final Map<String,ProtectionParameter> protectionParams;

    public DomainLoadStoreParameter(URI configuration,
                                    Map<String,ProtectionParameter> protectionParams){
        if(configuration==null||protectionParams==null){
            throw new NullPointerException("invalid null input");
        }
        this.configuration=configuration;
        this.protectionParams=
                Collections.unmodifiableMap(new HashMap<>(protectionParams));
    }

    public URI getConfiguration(){
        return configuration;
    }

    public Map<String,ProtectionParameter> getProtectionParams(){
        return protectionParams;
    }

    @Override
    public ProtectionParameter getProtectionParameter(){
        return null;
    }
}
