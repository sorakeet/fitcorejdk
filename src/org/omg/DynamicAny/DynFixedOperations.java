package org.omg.DynamicAny;

public interface DynFixedOperations extends DynAnyOperations{
    String get_value();

    boolean set_value(String val) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue;
} // interface DynFixedOperations
