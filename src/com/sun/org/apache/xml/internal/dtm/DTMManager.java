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
 * $Id: DTMManager.java,v 1.2.4.1 2005/09/15 08:14:54 suresh_emailid Exp $
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
 * $Id: DTMManager.java,v 1.2.4.1 2005/09/15 08:14:54 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;

public abstract class DTMManager{
    public static final int IDENT_DTM_NODE_BITS=16;
    public static final int IDENT_NODE_DEFAULT=(1<<IDENT_DTM_NODE_BITS)-1;
    public static final int IDENT_DTM_DEFAULT=~IDENT_NODE_DEFAULT;
    public static final int IDENT_MAX_DTMS=(IDENT_DTM_DEFAULT>>>IDENT_DTM_NODE_BITS)+1;
    // -------------------- private methods --------------------
    private static boolean debug;

    static{
        try{
            debug=SecuritySupport.getSystemProperty("dtm.debug")!=null;
        }catch(SecurityException ex){
        }
    }

    public boolean m_incremental=false;
    public boolean m_source_location=false;
    protected XMLStringFactory m_xsf=null;
    private boolean _useServicesMechanism;

    protected DTMManager(){
    }

    public static DTMManager newInstance(XMLStringFactory xsf)
            throws DTMConfigurationException{
        final DTMManager factoryImpl=new com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault();
        factoryImpl.setXMLStringFactory(xsf);
        return factoryImpl;
    }

    public XMLStringFactory getXMLStringFactory(){
        return m_xsf;
    }

    public void setXMLStringFactory(XMLStringFactory xsf){
        m_xsf=xsf;
    }

    public abstract DTM getDTM(javax.xml.transform.Source source,
                               boolean unique,DTMWSFilter whiteSpaceFilter,
                               boolean incremental,boolean doIndexing);

    public abstract DTM getDTM(int nodeHandle);

    public abstract int getDTMHandleFromNode(org.w3c.dom.Node node);

    public abstract DTM createDocumentFragment();

    public abstract boolean release(DTM dtm,boolean shouldHardDelete);

    public abstract DTMIterator createDTMIterator(Object xpathCompiler,
                                                  int pos);

    public abstract DTMIterator createDTMIterator(String xpathString,
                                                  PrefixResolver presolver);

    public abstract DTMIterator createDTMIterator(int whatToShow,
                                                  DTMFilter filter,boolean entityReferenceExpansion);

    public abstract DTMIterator createDTMIterator(int node);

    public boolean getIncremental(){
        return m_incremental;
    }

    public void setIncremental(boolean incremental){
        m_incremental=incremental;
    }

    public boolean getSource_location(){
        return m_source_location;
    }

    public void setSource_location(boolean sourceLocation){
        m_source_location=sourceLocation;
    }

    public boolean useServicesMechnism(){
        return _useServicesMechanism;
    }

    public void setServicesMechnism(boolean flag){
        _useServicesMechanism=flag;
    }

    public abstract int getDTMIdentity(DTM dtm);

    public int getDTMIdentityMask(){
        return IDENT_DTM_DEFAULT;
    }

    public int getNodeIdentityMask(){
        return IDENT_NODE_DEFAULT;
    }
    //
    // Classes
    //

    static class ConfigurationError
            extends Error{
        static final long serialVersionUID=5122054096615067992L;
        //
        // Data
        //
        private Exception exception;
        //
        // Constructors
        //

        ConfigurationError(String msg,Exception x){
            super(msg);
            this.exception=x;
        } // <init>(String,Exception)
        //
        // Public methods
        //

        Exception getException(){
            return exception;
        } // getException():Exception
    } // class ConfigurationError
}
