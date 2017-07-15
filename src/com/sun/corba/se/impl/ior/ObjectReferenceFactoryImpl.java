/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.ior;

import com.sun.corba.se.spi.ior.IORFactories;
import com.sun.corba.se.spi.ior.IORFactory;
import com.sun.corba.se.spi.ior.IORTemplateList;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.PortableInterceptor.ObjectReferenceFactory;
import org.omg.PortableInterceptor.ObjectReferenceFactoryHelper;

public class ObjectReferenceFactoryImpl extends ObjectReferenceProducerBase
        implements ObjectReferenceFactory, StreamableValue{
    // Note that this repository ID must reflect the implementation
    // of the abstract valuetype (that is, this class), not the
    // repository ID of the org.omg.PortableInterceptor.ObjectReferenceFactory
    // class.  This allows for multiple independent implementations
    // of the abstract valuetype, should that become necessary.
    public static final String repositoryId=
            "IDL:com/sun/corba/se/impl/ior/ObjectReferenceFactoryImpl:1.0";
    transient private IORTemplateList iorTemplates;

    public ObjectReferenceFactoryImpl(InputStream is){
        super((ORB)(is.orb()));
        _read(is);
    }

    public void _read(InputStream is){
        org.omg.CORBA_2_3.portable.InputStream istr=
                (org.omg.CORBA_2_3.portable.InputStream)is;
        iorTemplates=IORFactories.makeIORTemplateList(istr);
    }

    public void _write(OutputStream os){
        org.omg.CORBA_2_3.portable.OutputStream ostr=
                (org.omg.CORBA_2_3.portable.OutputStream)os;
        iorTemplates.write(ostr);
    }

    public TypeCode _type(){
        return ObjectReferenceFactoryHelper.type();
    }

    public ObjectReferenceFactoryImpl(ORB orb,IORTemplateList iortemps){
        super(orb);
        iorTemplates=iortemps;
    }

    public int hashCode(){
        return iorTemplates.hashCode();
    }

    public boolean equals(Object obj){
        if(!(obj instanceof ObjectReferenceFactoryImpl))
            return false;
        ObjectReferenceFactoryImpl other=(ObjectReferenceFactoryImpl)obj;
        return (iorTemplates!=null)&&
                iorTemplates.equals(other.iorTemplates);
    }

    public String[] _truncatable_ids(){
        return new String[]{repositoryId};
    }

    public IORFactory getIORFactory(){
        return iorTemplates;
    }

    public IORTemplateList getIORTemplateList(){
        return iorTemplates;
    }
}
