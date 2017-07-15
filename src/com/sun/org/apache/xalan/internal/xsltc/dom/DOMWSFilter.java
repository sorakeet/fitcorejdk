/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: DOMWSFilter.java,v 1.2.4.1 2005/09/06 06:14:31 pvedula Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * $Id: DOMWSFilter.java,v 1.2.4.1 2005/09/06 06:14:31 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMWSFilter;

import java.util.HashMap;
import java.util.Map;

public class DOMWSFilter implements DTMWSFilter{
    private AbstractTranslet m_translet;
    private StripFilter m_filter;
    // The Map for DTM to mapping array
    private Map<DTM,short[]> m_mappings;
    // Cache the DTM and mapping that are used last time
    private DTM m_currentDTM;
    private short[] m_currentMapping;

    public DOMWSFilter(AbstractTranslet translet){
        m_translet=translet;
        m_mappings=new HashMap<>();
        if(translet instanceof StripFilter){
            m_filter=(StripFilter)translet;
        }
    }

    public short getShouldStripSpace(int node,DTM dtm){
        if(m_filter!=null&&dtm instanceof DOM){
            DOM dom=(DOM)dtm;
            int type=0;
            if(dtm instanceof DOMEnhancedForDTM){
                DOMEnhancedForDTM mappableDOM=(DOMEnhancedForDTM)dtm;
                short[] mapping;
                if(dtm==m_currentDTM){
                    mapping=m_currentMapping;
                }else{
                    mapping=m_mappings.get(dtm);
                    if(mapping==null){
                        mapping=mappableDOM.getMapping(
                                m_translet.getNamesArray(),
                                m_translet.getUrisArray(),
                                m_translet.getTypesArray());
                        m_mappings.put(dtm,mapping);
                        m_currentDTM=dtm;
                        m_currentMapping=mapping;
                    }
                }
                int expType=mappableDOM.getExpandedTypeID(node);
                // %OPT% The mapping array does not have information about all the
                // exptypes. However it does contain enough information about all names
                // in the translet's namesArray. If the expType does not fall into the
                // range of the mapping array, it means that the expType is not for one
                // of the recognized names. In this case we can just set the type to -1.
                if(expType>=0&&expType<mapping.length)
                    type=mapping[expType];
                else
                    type=-1;
            }else{
                return INHERIT;
            }
            if(m_filter.stripSpace(dom,node,type)){
                return STRIP;
            }else{
                return NOTSTRIP;
            }
        }else{
            return NOTSTRIP;
        }
    }
}