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
 * $Id: XPathType.java,v 1.4 2005/05/10 16:40:17 mullan Exp $
 */
/**
 * $Id: XPathType.java,v 1.4 2005/05/10 16:40:17 mullan Exp $
 */
package javax.xml.crypto.dsig.spec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XPathType{
    private final String expression;
    private final Filter filter;
    private Map<String,String> nsMap;
    @SuppressWarnings("rawtypes")
    public XPathType(String expression,Filter filter,Map namespaceMap){
        this(expression,filter);
        if(namespaceMap==null){
            throw new NullPointerException("namespaceMap cannot be null");
        }
        Map<?,?> copy=new HashMap<>((Map<?,?>)namespaceMap);
        Iterator<? extends Map.Entry<?,?>> entries=copy.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<?,?> me=entries.next();
            if(!(me.getKey() instanceof String)||
                    !(me.getValue() instanceof String)){
                throw new ClassCastException("not a String");
            }
        }
        @SuppressWarnings("unchecked")
        Map<String,String> temp=(Map<String,String>)copy;
        nsMap=Collections.unmodifiableMap(temp);
    }

    public XPathType(String expression,Filter filter){
        if(expression==null){
            throw new NullPointerException("expression cannot be null");
        }
        if(filter==null){
            throw new NullPointerException("filter cannot be null");
        }
        this.expression=expression;
        this.filter=filter;
        this.nsMap=Collections.emptyMap();
    }

    public String getExpression(){
        return expression;
    }

    public Filter getFilter(){
        return filter;
    }

    @SuppressWarnings("rawtypes")
    public Map getNamespaceMap(){
        return nsMap;
    }

    public static class Filter{
        public static final Filter INTERSECT=new Filter("intersect");
        public static final Filter SUBTRACT=new Filter("subtract");
        public static final Filter UNION=new Filter("union");
        private final String operation;
        private Filter(String operation){
            this.operation=operation;
        }

        public String toString(){
            return operation;
        }
    }
}
