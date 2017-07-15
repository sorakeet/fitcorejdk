/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.spi;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.util.Map;

public interface LoginModule{
    void initialize(Subject subject,CallbackHandler callbackHandler,
                    Map<String,?> sharedState,
                    Map<String,?> options);

    boolean login() throws LoginException;

    boolean commit() throws LoginException;

    boolean abort() throws LoginException;

    boolean logout() throws LoginException;
}
