/**
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.monitor;

public class MonitorSettingException extends javax.management.JMRuntimeException{
    private static final long serialVersionUID=-8807913418190202007L;

    public MonitorSettingException(){
        super();
    }

    public MonitorSettingException(String message){
        super(message);
    }
}
