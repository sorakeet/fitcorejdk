package org.omg.DynamicAny;

public interface DynValueBoxOperations extends DynValueCommonOperations{
    org.omg.CORBA.Any get_boxed_value() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    void set_boxed_value(org.omg.CORBA.Any boxed) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

    DynAny get_boxed_value_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    void set_boxed_value_as_dyn_any(DynAny boxed) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
} // interface DynValueBoxOperations
