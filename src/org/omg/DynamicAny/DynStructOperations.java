package org.omg.DynamicAny;

public interface DynStructOperations extends DynAnyOperations{
    String current_member_name() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    org.omg.CORBA.TCKind current_member_kind() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    NameValuePair[] get_members();

    void set_members(NameValuePair[] value) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    NameDynAnyPair[] get_members_as_dyn_any();

    void set_members_as_dyn_any(NameDynAnyPair[] value) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;
} // interface DynStructOperations
