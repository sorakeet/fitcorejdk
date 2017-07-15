/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// NewInstance.java - create a new instance of a class by name.
// http://www.saxproject.org
// Written by Edwin Goei, edwingo@apache.org
// and by David Brownell, dbrownell@users.sourceforge.net
// NO WARRANTY!  This class is in the Public Domain.
// $Id: NewInstance.java,v 1.2 2005/06/10 03:50:50 jeffsuttor Exp $
package org.xml.sax.helpers;

class NewInstance{
    private static final String DEFAULT_PACKAGE="com.sun.org.apache.xerces.internal";

    static Object newInstance(ClassLoader classLoader,String className)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException{
        // make sure we have access to restricted packages
        boolean internal=false;
        if(System.getSecurityManager()!=null){
            if(className!=null&&className.startsWith(DEFAULT_PACKAGE)){
                internal=true;
            }
        }
        Class driverClass;
        if(classLoader==null||internal){
            driverClass=Class.forName(className);
        }else{
            driverClass=classLoader.loadClass(className);
        }
        return driverClass.newInstance();
    }
}
