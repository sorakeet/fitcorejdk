package org.omg.CosNaming;

public final class Binding implements org.omg.CORBA.portable.IDLEntity{
    public NameComponent binding_name[]=null;
    // name
    public BindingType binding_type=null;

    public Binding(){
    } // ctor

    public Binding(NameComponent[] _binding_name,BindingType _binding_type){
        binding_name=_binding_name;
        binding_type=_binding_type;
    } // ctor
} // class Binding
