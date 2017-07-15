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
 * $Id: XSLTCSource.java,v 1.2.4.1 2005/09/06 12:43:28 pvedula Exp $
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
 * $Id: XSLTCSource.java,v 1.2.4.1 2005/09/06 12:43:28 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.trax;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import com.sun.org.apache.xalan.internal.xsltc.dom.DOMWSFilter;
import com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl;
import com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public final class XSLTCSource implements Source{
    private String _systemId=null;
    private Source _source=null;
    private ThreadLocal _dom=new ThreadLocal();

    public XSLTCSource(String systemId){
        _systemId=systemId;
    }

    public XSLTCSource(Source source){
        _source=source;
    }

    protected DOM getDOM(XSLTCDTMManager dtmManager,AbstractTranslet translet)
            throws SAXException{
        SAXImpl idom=(SAXImpl)_dom.get();
        if(idom!=null){
            if(dtmManager!=null){
                idom.migrateTo(dtmManager);
            }
        }else{
            Source source=_source;
            if(source==null){
                if(_systemId!=null&&_systemId.length()>0){
                    source=new StreamSource(_systemId);
                }else{
                    ErrorMsg err=new ErrorMsg(ErrorMsg.XSLTC_SOURCE_ERR);
                    throw new SAXException(err.toString());
                }
            }
            DOMWSFilter wsfilter=null;
            if(translet!=null&&translet instanceof StripFilter){
                wsfilter=new DOMWSFilter(translet);
            }
            boolean hasIdCall=(translet!=null)?translet.hasIdCall():false;
            if(dtmManager==null){
                dtmManager=XSLTCDTMManager.newInstance();
            }
            idom=(SAXImpl)dtmManager.getDTM(source,true,wsfilter,false,false,hasIdCall);
            String systemId=getSystemId();
            if(systemId!=null){
                idom.setDocumentURI(systemId);
            }
            _dom.set(idom);
        }
        return idom;
    }    public void setSystemId(String systemId){
        _systemId=systemId;
        if(_source!=null){
            _source.setSystemId(systemId);
        }
    }

    public String getSystemId(){
        if(_source!=null){
            return _source.getSystemId();
        }else{
            return (_systemId);
        }
    }


}
