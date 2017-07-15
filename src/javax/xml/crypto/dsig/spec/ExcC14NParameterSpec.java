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
 * $Id: ExcC14NParameterSpec.java,v 1.7 2005/05/13 18:45:42 mullan Exp $
 */
/**
 * $Id: ExcC14NParameterSpec.java,v 1.7 2005/05/13 18:45:42 mullan Exp $
 */
package javax.xml.crypto.dsig.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ExcC14NParameterSpec implements C14NMethodParameterSpec{
    public static final String DEFAULT="#default";
    private List<String> preList;

    public ExcC14NParameterSpec(){
        preList=Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    public ExcC14NParameterSpec(List prefixList){
        if(prefixList==null){
            throw new NullPointerException("prefixList cannot be null");
        }
        List<?> copy=new ArrayList<>((List<?>)prefixList);
        for(int i=0, size=copy.size();i<size;i++){
            if(!(copy.get(i) instanceof String)){
                throw new ClassCastException("not a String");
            }
        }
        @SuppressWarnings("unchecked")
        List<String> temp=(List<String>)copy;
        preList=Collections.unmodifiableList(temp);
    }

    @SuppressWarnings("rawtypes")
    public List getPrefixList(){
        return preList;
    }
}
