package org.omg.DynamicAny;

public interface DynValueOperations extends DynValueCommonOperations{
    String current_member_name() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    org.omg.CORBA.TCKind current_member_kind() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    NameValuePair[] get_members() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    void set_members(NameValuePair[] value) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    NameDynAnyPair[] get_members_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    void set_members_as_dyn_any(NameDynAnyPair[] value) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;
} // interface DynValueOperations
