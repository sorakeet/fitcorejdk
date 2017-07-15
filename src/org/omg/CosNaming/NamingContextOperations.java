package org.omg.CosNaming;

public interface NamingContextOperations{
    void bind(NameComponent[] n,org.omg.CORBA.Object obj) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, org.omg.CosNaming.NamingContextPackage.AlreadyBound;

    void bind_context(NameComponent[] n,NamingContext nc) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, org.omg.CosNaming.NamingContextPackage.AlreadyBound;

    void rebind(NameComponent[] n,org.omg.CORBA.Object obj) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;

    void rebind_context(NameComponent[] n,NamingContext nc) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;

    org.omg.CORBA.Object resolve(NameComponent[] n) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;

    void unbind(NameComponent[] n) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;

    void list(int how_many,BindingListHolder bl,BindingIteratorHolder bi);

    NamingContext new_context();

    NamingContext bind_new_context(NameComponent[] n) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.AlreadyBound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;

    void destroy() throws org.omg.CosNaming.NamingContextPackage.NotEmpty;
} // interface NamingContextOperations
