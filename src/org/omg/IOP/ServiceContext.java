package org.omg.IOP;

public final class ServiceContext implements org.omg.CORBA.portable.IDLEntity{
    public int context_id=(int)0;
    public byte context_data[]=null;

    public ServiceContext(){
    } // ctor

    public ServiceContext(int _context_id,byte[] _context_data){
        context_id=_context_id;
        context_data=_context_data;
    } // ctor
} // class ServiceContext
