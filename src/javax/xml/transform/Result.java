/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform;

public interface Result{
    public static final String PI_DISABLE_OUTPUT_ESCAPING=
            "javax.xml.transform.disable-output-escaping";
    public static final String PI_ENABLE_OUTPUT_ESCAPING=
            "javax.xml.transform.enable-output-escaping";

    public String getSystemId();

    public void setSystemId(String systemId);
}
