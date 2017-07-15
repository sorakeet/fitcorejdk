/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: XPathFilterParameterSpec.java,v 1.4 2005/05/10 16:40:17 mullan Exp $
 */
/**
 * $Id: XPathFilterParameterSpec.java,v 1.4 2005/05/10 16:40:17 mullan Exp $
 */
package javax.xml.crypto.dsig.spec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class XPathFilterParameterSpec implements TransformParameterSpec{
    private String xPath;
    private Map<String,String> nsMap;

    public XPathFilterParameterSpec(String xPath){
        if(xPath==null){
            throw new NullPointerException();
        }
        this.xPath=xPath;
        this.nsMap=Collections.emptyMap();
    }

    @SuppressWarnings("rawtypes")
    public XPathFilterParameterSpec(String xPath,Map namespaceMap){
        if(xPath==null||namespaceMap==null){
            throw new NullPointerException();
        }
        this.xPath=xPath;
        Map<?,?> copy=new HashMap<>((Map<?,?>)namespaceMap);
        Iterator<? extends Entry<?,?>> entries=copy.entrySet().iterator();
        while(entries.hasNext()){
            Entry<?,?> me=entries.next();
            if(!(me.getKey() instanceof String)||
                    !(me.getValue() instanceof String)){
                throw new ClassCastException("not a String");
            }
        }
        @SuppressWarnings("unchecked")
        Map<String,String> temp=(Map<String,String>)copy;
        nsMap=Collections.unmodifiableMap(temp);
    }

    public String getXPath(){
        return xPath;
    }

    @SuppressWarnings("rawtypes")
    public Map getNamespaceMap(){
        return nsMap;
    }
}
