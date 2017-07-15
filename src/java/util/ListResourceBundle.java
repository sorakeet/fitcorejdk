/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 * <p>
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.util;

import sun.util.ResourceBundleEnumeration;

public abstract class ListResourceBundle extends ResourceBundle{
    private Map<String,Object> lookup=null;

    public ListResourceBundle(){
    }

    // Implements java.util.ResourceBundle.handleGetObject; inherits javadoc specification.
    public final Object handleGetObject(String key){
        // lazily load the lookup hashtable.
        if(lookup==null){
            loadLookup();
        }
        if(key==null){
            throw new NullPointerException();
        }
        return lookup.get(key); // this class ignores locales
    }

    public Enumeration<String> getKeys(){
        // lazily load the lookup hashtable.
        if(lookup==null){
            loadLookup();
        }
        ResourceBundle parent=this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(),
                (parent!=null)?parent.getKeys():null);
    }

    protected Set<String> handleKeySet(){
        if(lookup==null){
            loadLookup();
        }
        return lookup.keySet();
    }
    // ==================privates====================

    private synchronized void loadLookup(){
        if(lookup!=null)
            return;
        Object[][] contents=getContents();
        HashMap<String,Object> temp=new HashMap<>(contents.length);
        for(int i=0;i<contents.length;++i){
            // key must be non-null String, value must be non-null
            String key=(String)contents[i][0];
            Object value=contents[i][1];
            if(key==null||value==null){
                throw new NullPointerException();
            }
            temp.put(key,value);
        }
        lookup=temp;
    }

    abstract protected Object[][] getContents();
}
