/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

public abstract class ConfigurationSpi{
    protected abstract AppConfigurationEntry[] engineGetAppConfigurationEntry
            (String name);

    protected void engineRefresh(){
    }
}
