/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

public interface MulticastChannel
        extends NetworkChannel{
    @Override
    void close() throws IOException;

    MembershipKey join(InetAddress group,NetworkInterface interf)
            throws IOException;

    MembershipKey join(InetAddress group,NetworkInterface interf,InetAddress source)
            throws IOException;
}
