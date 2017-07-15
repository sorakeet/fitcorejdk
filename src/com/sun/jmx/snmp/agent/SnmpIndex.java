/**
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// java imports
//

import com.sun.jmx.snmp.SnmpOid;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
// jmx imports
//

public class SnmpIndex implements Serializable{
    private static final long serialVersionUID=8712159739982192146L;
    // PRIVATE VARIABLES
    //------------------
    private Vector<SnmpOid> oids=new Vector<>();
    private int size=0;

    public SnmpIndex(SnmpOid[] oidList){
        size=oidList.length;
        for(int i=0;i<size;i++){
            // The order is important ...
            //
            oids.addElement(oidList[i]);
        }
    }

    public SnmpIndex(SnmpOid oid){
        oids.addElement(oid);
        size=1;
    }

    public int getNbComponents(){
        return size;
    }

    public Vector<SnmpOid> getComponents(){
        return oids;
    }

    public boolean equals(SnmpIndex index){
        if(size!=index.getNbComponents())
            return false;
        // The two vectors have the same length.
        // Compare each single element ...
        //
        SnmpOid oid1;
        SnmpOid oid2;
        Vector<SnmpOid> components=index.getComponents();
        for(int i=0;i<size;i++){
            oid1=oids.elementAt(i);
            oid2=components.elementAt(i);
            if(oid1.equals(oid2)==false)
                return false;
        }
        return true;
    }

    public int compareTo(SnmpIndex index){
        int length=index.getNbComponents();
        Vector<SnmpOid> components=index.getComponents();
        SnmpOid oid1;
        SnmpOid oid2;
        int comp;
        for(int i=0;i<size;i++){
            if(i>length){
                // There is no more element in the index
                //
                return 1;
            }
            // Access the element ...
            //
            oid1=oids.elementAt(i);
            oid2=components.elementAt(i);
            comp=oid1.compareTo(oid2);
            if(comp==0)
                continue;
            return comp;
        }
        return 0;
    }

    @Override
    public String toString(){
        final StringBuilder msg=new StringBuilder();
        for(Enumeration<SnmpOid> e=oids.elements();e.hasMoreElements();){
            SnmpOid val=e.nextElement();
            msg.append("//").append(val.toString());
        }
        return msg.toString();
    }
}
