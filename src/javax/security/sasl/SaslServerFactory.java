/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.sasl;

import javax.security.auth.callback.CallbackHandler;
import java.util.Map;

public abstract interface SaslServerFactory{
    public abstract SaslServer createSaslServer(
            String mechanism,
            String protocol,
            String serverName,
            Map<String,?> props,
            CallbackHandler cbh) throws SaslException;

    public abstract String[] getMechanismNames(Map<String,?> props);
}
