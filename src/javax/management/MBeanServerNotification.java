/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class MBeanServerNotification extends Notification{
    public static final String REGISTRATION_NOTIFICATION=
            "JMX.mbean.registered";
    public static final String UNREGISTRATION_NOTIFICATION=
            "JMX.mbean.unregistered";
    private static final long serialVersionUID=2876477500475969677L;
    private final ObjectName objectName;

    public MBeanServerNotification(String type,Object source,
                                   long sequenceNumber,ObjectName objectName){
        super(type,source,sequenceNumber);
        this.objectName=objectName;
    }

    public ObjectName getMBeanName(){
        return objectName;
    }

    @Override
    public String toString(){
        return super.toString()+"[mbeanName="+objectName+"]";
    }
}
