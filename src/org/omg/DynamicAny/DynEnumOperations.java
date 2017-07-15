package org.omg.DynamicAny;

public interface DynEnumOperations extends DynAnyOperations{
    String get_as_string();

    void set_as_string(String value) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    int get_as_ulong();

    void set_as_ulong(int value) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;
} // interface DynEnumOperations
