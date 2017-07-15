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
 */
package java.util;

import sun.util.ResourceBundleEnumeration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class PropertyResourceBundle extends ResourceBundle{
    // ==================privates====================
    private Map<String,Object> lookup;

    @SuppressWarnings({"unchecked","rawtypes"})
    public PropertyResourceBundle(InputStream stream) throws IOException{
        Properties properties=new Properties();
        properties.load(stream);
        lookup=new HashMap(properties);
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public PropertyResourceBundle(Reader reader) throws IOException{
        Properties properties=new Properties();
        properties.load(reader);
        lookup=new HashMap(properties);
    }

    // Implements java.util.ResourceBundle.handleGetObject; inherits javadoc specification.
    public Object handleGetObject(String key){
        if(key==null){
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    public Enumeration<String> getKeys(){
        ResourceBundle parent=this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(),
                (parent!=null)?parent.getKeys():null);
    }

    protected Set<String> handleKeySet(){
        return lookup.keySet();
    }
}
