/**
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.spi;

import java.util.Locale;

public abstract class CalendarDataProvider extends LocaleServiceProvider{
    protected CalendarDataProvider(){
    }

    public abstract int getFirstDayOfWeek(Locale locale);

    public abstract int getMinimalDaysInFirstWeek(Locale locale);
}
