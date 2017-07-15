/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.spi;

import java.util.Locale;

public abstract class LocaleServiceProvider{
    protected LocaleServiceProvider(){
    }

    public boolean isSupportedLocale(Locale locale){
        locale=locale.stripExtensions(); // throws NPE if locale == null
        for(Locale available : getAvailableLocales()){
            if(locale.equals(available.stripExtensions())){
                return true;
            }
        }
        return false;
    }

    public abstract Locale[] getAvailableLocales();
}
