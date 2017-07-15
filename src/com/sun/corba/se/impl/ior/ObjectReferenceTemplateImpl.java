/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.ior;

import com.sun.corba.se.spi.ior.*;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import org.omg.PortableInterceptor.ObjectReferenceTemplateHelper;

public class ObjectReferenceTemplateImpl extends ObjectReferenceProducerBase
        implements ObjectReferenceTemplate, StreamableValue{
    // Note that this repository ID must reflect the implementation
    // of the abstract valuetype (that is, this class), not the
    // repository ID of the org.omg.PortableInterceptor.ObjectReferenceTemplate
    // class.  This allows for multiple independent implementations
    // of the abstract valuetype, should that become necessary.
    public static final String repositoryId=
            "IDL:com/sun/corba/se/impl/ior/ObjectReferenceTemplateImpl:1.0";
    transient private IORTemplate iorTemplate;

    public ObjectReferenceTemplateImpl(InputStream is){
        super((ORB)(is.orb()));
        _read(is);
    }

    public void _read(InputStream is){
        org.omg.CORBA_2_3.portable.InputStream istr=
                (org.omg.CORBA_2_3.portable.InputStream)is;
        iorTemplate=IORFactories.makeIORTemplate(istr);
        orb=(ORB)(istr.orb());
    }

    public void _write(OutputStream os){
        org.omg.CORBA_2_3.portable.OutputStream ostr=
                (org.omg.CORBA_2_3.portable.OutputStream)os;
        iorTemplate.write(ostr);
    }

    public TypeCode _type(){
        return ObjectReferenceTemplateHelper.type();
    }

    public ObjectReferenceTemplateImpl(ORB orb,IORTemplate iortemp){
        super(orb);
        iorTemplate=iortemp;
    }

    public int hashCode(){
        return iorTemplate.hashCode();
    }

    public boolean equals(Object obj){
        if(!(obj instanceof ObjectReferenceTemplateImpl))
            return false;
        ObjectReferenceTemplateImpl other=(ObjectReferenceTemplateImpl)obj;
        return (iorTemplate!=null)&&
                iorTemplate.equals(other.iorTemplate);
    }

    public String[] _truncatable_ids(){
        return new String[]{repositoryId};
    }

    public String server_id(){
        int val=iorTemplate.getObjectKeyTemplate().getServerId();
        return Integer.toString(val);
    }

    public String orb_id(){
        return iorTemplate.getObjectKeyTemplate().getORBId();
    }

    public String[] adapter_name(){
        ObjectAdapterId poaid=
                iorTemplate.getObjectKeyTemplate().getObjectAdapterId();
        return poaid.getAdapterName();
    }

    public IORFactory getIORFactory(){
        return iorTemplate;
    }

    public IORTemplateList getIORTemplateList(){
        IORTemplateList tl=IORFactories.makeIORTemplateList();
        tl.add(iorTemplate);
        tl.makeImmutable();
        return tl;
    }
}
