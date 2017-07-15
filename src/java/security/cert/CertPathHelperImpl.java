/**
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

import sun.security.provider.certpath.CertPathHelper;
import sun.security.x509.GeneralNameInterface;

import java.util.Date;
import java.util.Set;

class CertPathHelperImpl extends CertPathHelper{
    private CertPathHelperImpl(){
        // empty
    }

    synchronized static void initialize(){
        if(CertPathHelper.instance==null){
            CertPathHelper.instance=new CertPathHelperImpl();
        }
    }

    protected void implSetPathToNames(X509CertSelector sel,
                                      Set<GeneralNameInterface> names){
        sel.setPathToNamesInternal(names);
    }

    protected void implSetDateAndTime(X509CRLSelector sel,Date date,long skew){
        sel.setDateAndTime(date,skew);
    }
}
