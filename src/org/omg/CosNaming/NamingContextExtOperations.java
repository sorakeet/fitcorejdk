package org.omg.CosNaming;

public interface NamingContextExtOperations extends NamingContextOperations{
    String to_string(NameComponent[] n) throws org.omg.CosNaming.NamingContextPackage.InvalidName;

    NameComponent[] to_name(String sn) throws org.omg.CosNaming.NamingContextPackage.InvalidName;

    String to_url(String addr,String sn) throws org.omg.CosNaming.NamingContextExtPackage.InvalidAddress, org.omg.CosNaming.NamingContextPackage.InvalidName;

    org.omg.CORBA.Object resolve_str(String sn) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;
} // interface NamingContextExtOperations
