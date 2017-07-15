/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: NodeSortRecord.java,v 1.5 2005/09/28 13:48:36 pvedula Exp $
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: NodeSortRecord.java,v 1.5 2005/09/28 13:48:36 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xalan.internal.xsltc.CollatorFactory;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.utils.StringComparable;

import java.text.Collator;
import java.util.Locale;

public abstract class NodeSortRecord{
    public static final int COMPARE_STRING=0;
    public static final int COMPARE_NUMERIC=1;
    public static final int COMPARE_ASCENDING=0;
    public static final int COMPARE_DESCENDING=1;
    private static final Collator DEFAULT_COLLATOR=Collator.getInstance();
    protected Collator _collator=DEFAULT_COLLATOR;
    protected Collator[] _collators;
    protected Locale _locale;
    protected CollatorFactory _collatorFactory;
    protected SortSettings _settings;
    private DOM _dom=null;
    private int _node;           // The position in the current iterator
    private int _last=0;       // Number of nodes in the current iterator
    private int _scanned=0;    // Number of key levels extracted from DOM
    private Object[] _values; // Contains Comparable  objects

    public NodeSortRecord(){
        this(0);
    }

    public NodeSortRecord(int node){
        _node=node;
    }

    public final void initialize(int node,int last,DOM dom,
                                 SortSettings settings)
            throws TransletException{
        _dom=dom;
        _node=node;
        _last=last;
        _settings=settings;
        int levels=settings.getSortOrders().length;
        _values=new Object[levels];
        String colFactClassname=null;
        try{
            // -- W. Eliot Kimber (eliot@isogen.com)
            colFactClassname=
                    SecuritySupport.getSystemProperty("com.sun.org.apache.xalan.internal.xsltc.COLLATOR_FACTORY");
        }catch(SecurityException e){
            // If we can't read the propery, just use default collator
        }
        if(colFactClassname!=null){
            try{
                Object candObj=ObjectFactory.findProviderClass(colFactClassname,true);
                _collatorFactory=(CollatorFactory)candObj;
            }catch(ClassNotFoundException e){
                throw new TransletException(e);
            }
            Locale[] locales=settings.getLocales();
            _collators=new Collator[levels];
            for(int i=0;i<levels;i++){
                _collators[i]=_collatorFactory.getCollator(locales[i]);
            }
            _collator=_collators[0];
        }else{
            _collators=settings.getCollators();
            _collator=_collators[0];
        }
    }

    public final int getNode(){
        return _node;
    }

    public final int compareDocOrder(NodeSortRecord other){
        return _node-other._node;
    }

    public int compareTo(NodeSortRecord other){
        int cmp, level;
        int[] sortOrder=_settings.getSortOrders();
        int levels=_settings.getSortOrders().length;
        int[] compareTypes=_settings.getTypes();
        for(level=0;level<levels;level++){
            // Compare the two nodes either as numeric or text values
            if(compareTypes[level]==COMPARE_NUMERIC){
                final Double our=numericValue(level);
                final Double their=other.numericValue(level);
                cmp=our.compareTo(their);
            }else{
                final Comparable our=stringValue(level);
                final Comparable their=other.stringValue(level);
                cmp=our.compareTo(their);
            }
            // Return inverse compare value if inverse sort order
            if(cmp!=0){
                return sortOrder[level]==COMPARE_DESCENDING?0-cmp:cmp;
            }
        }
        // Compare based on document order if all sort keys are equal
        return (_node-other._node);
    }

    private final Comparable stringValue(int level){
        // Get value from our array if possible
        if(_scanned<=level){
            AbstractTranslet translet=_settings.getTranslet();
            Locale[] locales=_settings.getLocales();
            String[] caseOrder=_settings.getCaseOrders();
            // Get value from DOM if accessed for the first time
            final String str=extractValueFromDOM(_dom,_node,level,
                    translet,_last);
            final Comparable key=
                    StringComparable.getComparator(str,locales[level],
                            _collators[level],
                            caseOrder[level]);
            _values[_scanned++]=key;
            return (key);
        }
        return ((Comparable)_values[level]);
    }

    private final Double numericValue(int level){
        // Get value from our vector if possible
        if(_scanned<=level){
            AbstractTranslet translet=_settings.getTranslet();
            // Get value from DOM if accessed for the first time
            final String str=extractValueFromDOM(_dom,_node,level,
                    translet,_last);
            Double num;
            try{
                num=new Double(str);
            }
            // Treat number as NaN if it cannot be parsed as a double
            catch(NumberFormatException e){
                num=new Double(Double.NEGATIVE_INFINITY);
            }
            _values[_scanned++]=num;
            return (num);
        }
        return ((Double)_values[level]);
    }

    public abstract String extractValueFromDOM(DOM dom,int current,int level,
                                               AbstractTranslet translet,
                                               int last);

    public Collator[] getCollator(){
        return _collators;
    }
}
