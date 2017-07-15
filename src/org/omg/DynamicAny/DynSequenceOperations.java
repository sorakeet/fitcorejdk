package org.omg.DynamicAny;

public interface DynSequenceOperations extends DynAnyOperations{
    int get_length();

    void set_length(int len) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    org.omg.CORBA.Any[] get_elements();

    void set_elements(org.omg.CORBA.Any[] value) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    DynAny[] get_elements_as_dyn_any();

    void set_elements_as_dyn_any(DynAny[] value) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;
} // interface DynSequenceOperations
