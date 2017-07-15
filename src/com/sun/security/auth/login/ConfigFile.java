/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth.login;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.net.URI;
// NOTE: As of JDK 8, this class instantiates
// sun.security.provider.ConfigFile.Spi and forwards all methods to that
// implementation. All implementation fixes and enhancements should be made to
// sun.security.provider.ConfigFile.Spi and not this class.
// See JDK-8005117 for more information.

@jdk.Exported
public class ConfigFile extends Configuration{
    private final sun.security.provider.ConfigFile.Spi spi;

    public ConfigFile(){
        spi=new sun.security.provider.ConfigFile.Spi();
    }

    public ConfigFile(URI uri){
        spi=new sun.security.provider.ConfigFile.Spi(uri);
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry
            (String applicationName){
        return spi.engineGetAppConfigurationEntry(applicationName);
    }

    @Override
    public void refresh(){
        spi.engineRefresh();
    }
}
