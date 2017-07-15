/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

final public class ManageReferralControl extends BasicControl{
    public static final String OID="2.16.840.1.113730.3.4.2";
    private static final long serialVersionUID=3017756160149982566L;

    public ManageReferralControl(){
        super(OID,true,null);
    }

    public ManageReferralControl(boolean criticality){
        super(OID,criticality,null);
    }
}
