/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

import java.util.*;

public abstract class AccessibleBundle{
    private static Hashtable table=new Hashtable();
    private final String defaultResourceBundleName
            ="com.sun.accessibility.internal.resources.accessibility";
    protected String key=null;

    public AccessibleBundle(){
    }

    public String toString(){
        return toDisplayString();
    }

    public String toDisplayString(){
        return toDisplayString(Locale.getDefault());
    }

    public String toDisplayString(Locale locale){
        return toDisplayString(defaultResourceBundleName,locale);
    }

    protected String toDisplayString(String resourceBundleName,
                                     Locale locale){
        // loads the resource bundle if necessary
        loadResourceBundle(resourceBundleName,locale);
        // returns the localized string
        Object o=table.get(locale);
        if(o!=null&&o instanceof Hashtable){
            Hashtable resourceTable=(Hashtable)o;
            o=resourceTable.get(key);
            if(o!=null&&o instanceof String){
                return (String)o;
            }
        }
        return key;
    }

    private void loadResourceBundle(String resourceBundleName,
                                    Locale locale){
        if(!table.contains(locale)){
            try{
                Hashtable resourceTable=new Hashtable();
                ResourceBundle bundle=ResourceBundle.getBundle(resourceBundleName,locale);
                Enumeration iter=bundle.getKeys();
                while(iter.hasMoreElements()){
                    String key=(String)iter.nextElement();
                    resourceTable.put(key,bundle.getObject(key));
                }
                table.put(locale,resourceTable);
            }catch(MissingResourceException e){
                System.err.println("loadResourceBundle: "+e);
                // Just return so toDisplayString() returns the
                // non-localized key.
                return;
            }
        }
    }
}
