/**
 * Copyright (c) 2001, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * File: Sync.java
 * <p>
 * Originally written by Doug Lea and released into the public domain.
 * This may be used for any purposes whatsoever without acknowledgment.
 * Thanks for the assistance and support of Sun Microsystems Labs,
 * and everyone contributing, testing, and using this code.
 * <p>
 * History:
 * Date       Who                What
 * 11Jun1998  dl               Create public version
 * 5Aug1998  dl               Added some convenient time constants
 */
/**
 File: Sync.java

 Originally written by Doug Lea and released into the public domain.
 This may be used for any purposes whatsoever without acknowledgment.
 Thanks for the assistance and support of Sun Microsystems Labs,
 and everyone contributing, testing, and using this code.

 History:
 Date       Who                What
 11Jun1998  dl               Create public version
 5Aug1998  dl               Added some convenient time constants
 */
package com.sun.corba.se.impl.orbutil.concurrent;

public interface Sync{
    public static final long ONE_SECOND=1000;
    public static final long ONE_MINUTE=60*ONE_SECOND;
    public static final long ONE_HOUR=60*ONE_MINUTE;
    public static final long ONE_DAY=24*ONE_HOUR;
    public static final long ONE_WEEK=7*ONE_DAY;
    // Not that it matters, but there is some variation across
    // standard sources about value at msec precision.
    // The value used is the same as in java.util.GregorianCalendar
    public static final long ONE_YEAR=(long)(365.2425*ONE_DAY);
    public static final long ONE_CENTURY=100*ONE_YEAR;

    public void acquire() throws InterruptedException;

    public boolean attempt(long msecs) throws InterruptedException;

    public void release();
}
