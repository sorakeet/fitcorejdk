/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: SelfIteratorNoPredicate.java,v 1.2.4.2 2005/09/14 19:45:21 jeffsuttor Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: SelfIteratorNoPredicate.java,v 1.2.4.2 2005/09/14 19:45:21 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.compiler.Compiler;

public class SelfIteratorNoPredicate extends LocPathIterator{
    static final long serialVersionUID=-4226887905279814201L;

    SelfIteratorNoPredicate(Compiler compiler,int opPos,int analysis)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis,false);
    }

    public SelfIteratorNoPredicate()
            throws javax.xml.transform.TransformerException{
        super(null);
    }

    public int asNode(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        return xctxt.getCurrentNode();
    }

    public int nextNode(){
        if(m_foundLast)
            return DTM.NULL;
        int next;
        DTM dtm=m_cdtm;
        m_lastFetched=next=(DTM.NULL==m_lastFetched)
                ?m_context
                :DTM.NULL;
        // m_lastFetched = next;
        if(DTM.NULL!=next){
            m_pos++;
            return next;
        }else{
            m_foundLast=true;
            return DTM.NULL;
        }
    }

    public int getLastPos(XPathContext xctxt){
        return 1;
    }
}
