/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth;

import sun.security.util.ResourcesMgr;

import java.io.*;
import java.security.*;
import java.text.MessageFormat;
import java.util.*;

public final class Subject implements Serializable{
    private static final long serialVersionUID=-8308522755600156056L;
    private static final int PRINCIPAL_SET=1;
    private static final int PUB_CREDENTIAL_SET=2;
    private static final int PRIV_CREDENTIAL_SET=3;
    private static final ProtectionDomain[] NULL_PD_ARRAY
            =new ProtectionDomain[0];
    Set<Principal> principals;
    transient Set<Object> pubCredentials;
    transient Set<Object> privCredentials;
    private volatile boolean readOnly=false;

    public Subject(){
        this.principals=Collections.synchronizedSet
                (new SecureSet<Principal>(this,PRINCIPAL_SET));
        this.pubCredentials=Collections.synchronizedSet
                (new SecureSet<Object>(this,PUB_CREDENTIAL_SET));
        this.privCredentials=Collections.synchronizedSet
                (new SecureSet<Object>(this,PRIV_CREDENTIAL_SET));
    }

    public Subject(boolean readOnly,Set<? extends Principal> principals,
                   Set<?> pubCredentials,Set<?> privCredentials){
        if(principals==null||
                pubCredentials==null||
                privCredentials==null)
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.input.s."));
        this.principals=Collections.synchronizedSet(new SecureSet<Principal>
                (this,PRINCIPAL_SET,principals));
        this.pubCredentials=Collections.synchronizedSet(new SecureSet<Object>
                (this,PUB_CREDENTIAL_SET,pubCredentials));
        this.privCredentials=Collections.synchronizedSet(new SecureSet<Object>
                (this,PRIV_CREDENTIAL_SET,privCredentials));
        this.readOnly=readOnly;
    }

    public static Subject getSubject(final AccessControlContext acc){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(AuthPermissionHolder.GET_SUBJECT_PERMISSION);
        }
        if(acc==null){
            throw new NullPointerException(ResourcesMgr.getString
                    ("invalid.null.AccessControlContext.provided"));
        }
        // return the Subject from the DomainCombiner of the provided context
        return AccessController.doPrivileged
                (new PrivilegedAction<Subject>(){
                    public Subject run(){
                        DomainCombiner dc=acc.getDomainCombiner();
                        if(!(dc instanceof SubjectDomainCombiner))
                            return null;
                        SubjectDomainCombiner sdc=(SubjectDomainCombiner)dc;
                        return sdc.getSubject();
                    }
                });
    }

    public static <T> T doAs(final Subject subject,
                             final PrivilegedAction<T> action){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(AuthPermissionHolder.DO_AS_PERMISSION);
        }
        if(action==null)
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.action.provided"));
        // set up the new Subject-based AccessControlContext
        // for doPrivileged
        final AccessControlContext currentAcc=AccessController.getContext();
        // call doPrivileged and push this new context on the stack
        return AccessController.doPrivileged
                (action,
                        createContext(subject,currentAcc));
    }

    private static AccessControlContext createContext(final Subject subject,
                                                      final AccessControlContext acc){
        return AccessController.doPrivileged
                (new PrivilegedAction<AccessControlContext>(){
                    public AccessControlContext run(){
                        if(subject==null)
                            return new AccessControlContext(acc,null);
                        else
                            return new AccessControlContext
                                    (acc,
                                            new SubjectDomainCombiner(subject));
                    }
                });
    }

    public static <T> T doAs(final Subject subject,
                             final PrivilegedExceptionAction<T> action)
            throws PrivilegedActionException{
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(AuthPermissionHolder.DO_AS_PERMISSION);
        }
        if(action==null)
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.action.provided"));
        // set up the new Subject-based AccessControlContext for doPrivileged
        final AccessControlContext currentAcc=AccessController.getContext();
        // call doPrivileged and push this new context on the stack
        return AccessController.doPrivileged
                (action,
                        createContext(subject,currentAcc));
    }

    public static <T> T doAsPrivileged(final Subject subject,
                                       final PrivilegedAction<T> action,
                                       final AccessControlContext acc){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(AuthPermissionHolder.DO_AS_PRIVILEGED_PERMISSION);
        }
        if(action==null)
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.action.provided"));
        // set up the new Subject-based AccessControlContext
        // for doPrivileged
        final AccessControlContext callerAcc=
                (acc==null?
                        new AccessControlContext(NULL_PD_ARRAY):
                        acc);
        // call doPrivileged and push this new context on the stack
        return AccessController.doPrivileged
                (action,
                        createContext(subject,callerAcc));
    }

    public static <T> T doAsPrivileged(final Subject subject,
                                       final PrivilegedExceptionAction<T> action,
                                       final AccessControlContext acc)
            throws PrivilegedActionException{
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(AuthPermissionHolder.DO_AS_PRIVILEGED_PERMISSION);
        }
        if(action==null)
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.action.provided"));
        // set up the new Subject-based AccessControlContext for doPrivileged
        final AccessControlContext callerAcc=
                (acc==null?
                        new AccessControlContext(NULL_PD_ARRAY):
                        acc);
        // call doPrivileged and push this new context on the stack
        return AccessController.doPrivileged
                (action,
                        createContext(subject,callerAcc));
    }

    public void setReadOnly(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(AuthPermissionHolder.SET_READ_ONLY_PERMISSION);
        }
        this.readOnly=true;
    }

    public boolean isReadOnly(){
        return this.readOnly;
    }

    public Set<Principal> getPrincipals(){
        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return principals;
    }

    public <T extends Principal> Set<T> getPrincipals(Class<T> c){
        if(c==null)
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.Class.provided"));
        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet<T>(PRINCIPAL_SET,c);
    }

    public Set<Object> getPublicCredentials(){
        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return pubCredentials;
    }

    public Set<Object> getPrivateCredentials(){
        // XXX
        // we do not need a security check for
        // AuthPermission(getPrivateCredentials)
        // because we already restrict access to private credentials
        // via the PrivateCredentialPermission.  all the extra AuthPermission
        // would do is protect the set operations themselves
        // (like size()), which don't seem security-sensitive.
        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return privCredentials;
    }

    public <T> Set<T> getPublicCredentials(Class<T> c){
        if(c==null)
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.Class.provided"));
        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet<T>(PUB_CREDENTIAL_SET,c);
    }

    public <T> Set<T> getPrivateCredentials(Class<T> c){
        // XXX
        // we do not need a security check for
        // AuthPermission(getPrivateCredentials)
        // because we already restrict access to private credentials
        // via the PrivateCredentialPermission.  all the extra AuthPermission
        // would do is protect the set operations themselves
        // (like size()), which don't seem security-sensitive.
        if(c==null)
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.Class.provided"));
        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet<T>(PRIV_CREDENTIAL_SET,c);
    }

    public int hashCode(){
        /**
         * The hashcode is derived exclusive or-ing the
         * hashcodes of this Subject's Principals and credentials.
         *
         * If a particular credential was destroyed
         * ({@code credential.hashCode()} throws an
         * {@code IllegalStateException}),
         * the hashcode for that credential is derived via:
         * {@code credential.getClass().toString().hashCode()}.
         */
        int hashCode=0;
        synchronized(principals){
            Iterator<Principal> pIterator=principals.iterator();
            while(pIterator.hasNext()){
                Principal p=pIterator.next();
                hashCode^=p.hashCode();
            }
        }
        synchronized(pubCredentials){
            Iterator<Object> pubCIterator=pubCredentials.iterator();
            while(pubCIterator.hasNext()){
                hashCode^=getCredHashCode(pubCIterator.next());
            }
        }
        return hashCode;
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(o instanceof Subject){
            final Subject that=(Subject)o;
            // check the principal and credential sets
            Set<Principal> thatPrincipals;
            synchronized(that.principals){
                // avoid deadlock from dual locks
                thatPrincipals=new HashSet<Principal>(that.principals);
            }
            if(!principals.equals(thatPrincipals)){
                return false;
            }
            Set<Object> thatPubCredentials;
            synchronized(that.pubCredentials){
                // avoid deadlock from dual locks
                thatPubCredentials=new HashSet<Object>(that.pubCredentials);
            }
            if(!pubCredentials.equals(thatPubCredentials)){
                return false;
            }
            Set<Object> thatPrivCredentials;
            synchronized(that.privCredentials){
                // avoid deadlock from dual locks
                thatPrivCredentials=new HashSet<Object>(that.privCredentials);
            }
            if(!privCredentials.equals(thatPrivCredentials)){
                return false;
            }
            return true;
        }
        return false;
    }

    public String toString(){
        return toString(true);
    }

    String toString(boolean includePrivateCredentials){
        String s=ResourcesMgr.getString("Subject.");
        String suffix="";
        synchronized(principals){
            Iterator<Principal> pI=principals.iterator();
            while(pI.hasNext()){
                Principal p=pI.next();
                suffix=suffix+ResourcesMgr.getString(".Principal.")+
                        p.toString()+ResourcesMgr.getString("NEWLINE");
            }
        }
        synchronized(pubCredentials){
            Iterator<Object> pI=pubCredentials.iterator();
            while(pI.hasNext()){
                Object o=pI.next();
                suffix=suffix+
                        ResourcesMgr.getString(".Public.Credential.")+
                        o.toString()+ResourcesMgr.getString("NEWLINE");
            }
        }
        if(includePrivateCredentials){
            synchronized(privCredentials){
                Iterator<Object> pI=privCredentials.iterator();
                while(pI.hasNext()){
                    try{
                        Object o=pI.next();
                        suffix+=ResourcesMgr.getString
                                (".Private.Credential.")+
                                o.toString()+
                                ResourcesMgr.getString("NEWLINE");
                    }catch(SecurityException se){
                        suffix+=ResourcesMgr.getString
                                (".Private.Credential.inaccessible.");
                        break;
                    }
                }
            }
        }
        return s+suffix;
    }

    private int getCredHashCode(Object o){
        try{
            return o.hashCode();
        }catch(IllegalStateException ise){
            return o.getClass().toString().hashCode();
        }
    }

    private void writeObject(ObjectOutputStream oos)
            throws IOException{
        synchronized(principals){
            oos.defaultWriteObject();
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        ObjectInputStream.GetField gf=s.readFields();
        readOnly=gf.get("readOnly",false);
        Set<Principal> inputPrincs=(Set<Principal>)gf.get("principals",null);
        // Rewrap the principals into a SecureSet
        if(inputPrincs==null){
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.input.s."));
        }
        try{
            principals=Collections.synchronizedSet(new SecureSet<Principal>
                    (this,PRINCIPAL_SET,inputPrincs));
        }catch(NullPointerException npe){
            // Sometimes people deserialize the principals set only.
            // Subject is not accessible, so just don't fail.
            principals=Collections.synchronizedSet
                    (new SecureSet<Principal>(this,PRINCIPAL_SET));
        }
        // The Credential {@code Set} is not serialized, but we do not
        // want the default deserialization routine to set it to null.
        this.pubCredentials=Collections.synchronizedSet
                (new SecureSet<Object>(this,PUB_CREDENTIAL_SET));
        this.privCredentials=Collections.synchronizedSet
                (new SecureSet<Object>(this,PRIV_CREDENTIAL_SET));
    }

    private static class SecureSet<E>
            extends AbstractSet<E>
            implements Serializable{
        private static final long serialVersionUID=7911754171111800359L;
        private static final ObjectStreamField[] serialPersistentFields={
                new ObjectStreamField("this$0",Subject.class),
                new ObjectStreamField("elements",LinkedList.class),
                new ObjectStreamField("which",int.class)
        };
        Subject subject;
        LinkedList<E> elements;
        private int which;

        SecureSet(Subject subject,int which){
            this.subject=subject;
            this.which=which;
            this.elements=new LinkedList<E>();
        }

        SecureSet(Subject subject,int which,Set<? extends E> set){
            this.subject=subject;
            this.which=which;
            this.elements=new LinkedList<E>(set);
        }

        public boolean removeAll(Collection<?> c){
            Objects.requireNonNull(c);
            boolean modified=false;
            final Iterator<E> e=iterator();
            while(e.hasNext()){
                E next;
                if(which!=Subject.PRIV_CREDENTIAL_SET){
                    next=e.next();
                }else{
                    next=AccessController.doPrivileged
                            (new PrivilegedAction<E>(){
                                public E run(){
                                    return e.next();
                                }
                            });
                }
                Iterator<?> ce=c.iterator();
                while(ce.hasNext()){
                    Object o=ce.next();
                    if(next==null){
                        if(o==null){
                            e.remove();
                            modified=true;
                            break;
                        }
                    }else if(next.equals(o)){
                        e.remove();
                        modified=true;
                        break;
                    }
                }
            }
            return modified;
        }        public int size(){
            return elements.size();
        }

        private void writeObject(ObjectOutputStream oos)
                throws IOException{
            if(which==Subject.PRIV_CREDENTIAL_SET){
                // check permissions before serializing
                Iterator<E> i=iterator();
                while(i.hasNext()){
                    i.next();
                }
            }
            ObjectOutputStream.PutField fields=oos.putFields();
            fields.put("this$0",subject);
            fields.put("elements",elements);
            fields.put("which",which);
            oos.writeFields();
        }        public Iterator<E> iterator(){
            final LinkedList<E> list=elements;
            return new Iterator<E>(){
                ListIterator<E> i=list.listIterator(0);

                public boolean hasNext(){
                    return i.hasNext();
                }

                public E next(){
                    if(which!=Subject.PRIV_CREDENTIAL_SET){
                        return i.next();
                    }
                    SecurityManager sm=System.getSecurityManager();
                    if(sm!=null){
                        try{
                            sm.checkPermission(new PrivateCredentialPermission
                                    (list.get(i.nextIndex()).getClass().getName(),
                                            subject.getPrincipals()));
                        }catch(SecurityException se){
                            i.next();
                            throw (se);
                        }
                    }
                    return i.next();
                }

                public void remove(){
                    if(subject.isReadOnly()){
                        throw new IllegalStateException(ResourcesMgr.getString
                                ("Subject.is.read.only"));
                    }
                    SecurityManager sm=System.getSecurityManager();
                    if(sm!=null){
                        switch(which){
                            case Subject.PRINCIPAL_SET:
                                sm.checkPermission(AuthPermissionHolder.MODIFY_PRINCIPALS_PERMISSION);
                                break;
                            case Subject.PUB_CREDENTIAL_SET:
                                sm.checkPermission(AuthPermissionHolder.MODIFY_PUBLIC_CREDENTIALS_PERMISSION);
                                break;
                            default:
                                sm.checkPermission(AuthPermissionHolder.MODIFY_PRIVATE_CREDENTIALS_PERMISSION);
                                break;
                        }
                    }
                    i.remove();
                }
            };
        }

        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream ois)
                throws IOException, ClassNotFoundException{
            ObjectInputStream.GetField fields=ois.readFields();
            subject=(Subject)fields.get("this$0",null);
            which=fields.get("which",0);
            LinkedList<E> tmp=(LinkedList<E>)fields.get("elements",null);
            if(tmp.getClass()!=LinkedList.class){
                elements=new LinkedList<E>(tmp);
            }else{
                elements=tmp;
            }
        }        public boolean add(E o){
            if(subject.isReadOnly()){
                throw new IllegalStateException
                        (ResourcesMgr.getString("Subject.is.read.only"));
            }
            SecurityManager sm=System.getSecurityManager();
            if(sm!=null){
                switch(which){
                    case Subject.PRINCIPAL_SET:
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PRINCIPALS_PERMISSION);
                        break;
                    case Subject.PUB_CREDENTIAL_SET:
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PUBLIC_CREDENTIALS_PERMISSION);
                        break;
                    default:
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PRIVATE_CREDENTIALS_PERMISSION);
                        break;
                }
            }
            switch(which){
                case Subject.PRINCIPAL_SET:
                    if(!(o instanceof Principal)){
                        throw new SecurityException(ResourcesMgr.getString
                                ("attempting.to.add.an.object.which.is.not.an.instance.of.java.security.Principal.to.a.Subject.s.Principal.Set"));
                    }
                    break;
                default:
                    // ok to add Objects of any kind to credential sets
                    break;
            }
            // check for duplicates
            if(!elements.contains(o))
                return elements.add(o);
            else
                return false;
        }

        public boolean remove(Object o){
            final Iterator<E> e=iterator();
            while(e.hasNext()){
                E next;
                if(which!=Subject.PRIV_CREDENTIAL_SET){
                    next=e.next();
                }else{
                    next=AccessController.doPrivileged
                            (new PrivilegedAction<E>(){
                                public E run(){
                                    return e.next();
                                }
                            });
                }
                if(next==null){
                    if(o==null){
                        e.remove();
                        return true;
                    }
                }else if(next.equals(o)){
                    e.remove();
                    return true;
                }
            }
            return false;
        }

        public boolean contains(Object o){
            final Iterator<E> e=iterator();
            while(e.hasNext()){
                E next;
                if(which!=Subject.PRIV_CREDENTIAL_SET){
                    next=e.next();
                }else{
                    // For private credentials:
                    // If the caller does not have read permission for
                    // for o.getClass(), we throw a SecurityException.
                    // Otherwise we check the private cred set to see whether
                    // it contains the Object
                    SecurityManager sm=System.getSecurityManager();
                    if(sm!=null){
                        sm.checkPermission(new PrivateCredentialPermission
                                (o.getClass().getName(),
                                        subject.getPrincipals()));
                    }
                    next=AccessController.doPrivileged
                            (new PrivilegedAction<E>(){
                                public E run(){
                                    return e.next();
                                }
                            });
                }
                if(next==null){
                    if(o==null){
                        return true;
                    }
                }else if(next.equals(o)){
                    return true;
                }
            }
            return false;
        }



        public boolean retainAll(Collection<?> c){
            Objects.requireNonNull(c);
            boolean modified=false;
            boolean retain=false;
            final Iterator<E> e=iterator();
            while(e.hasNext()){
                retain=false;
                E next;
                if(which!=Subject.PRIV_CREDENTIAL_SET){
                    next=e.next();
                }else{
                    next=AccessController.doPrivileged
                            (new PrivilegedAction<E>(){
                                public E run(){
                                    return e.next();
                                }
                            });
                }
                Iterator<?> ce=c.iterator();
                while(ce.hasNext()){
                    Object o=ce.next();
                    if(next==null){
                        if(o==null){
                            retain=true;
                            break;
                        }
                    }else if(next.equals(o)){
                        retain=true;
                        break;
                    }
                }
                if(!retain){
                    e.remove();
                    retain=false;
                    modified=true;
                }
            }
            return modified;
        }

        public void clear(){
            final Iterator<E> e=iterator();
            while(e.hasNext()){
                E next;
                if(which!=Subject.PRIV_CREDENTIAL_SET){
                    next=e.next();
                }else{
                    next=AccessController.doPrivileged
                            (new PrivilegedAction<E>(){
                                public E run(){
                                    return e.next();
                                }
                            });
                }
                e.remove();
            }
        }




    }

    static class AuthPermissionHolder{
        static final AuthPermission DO_AS_PERMISSION=
                new AuthPermission("doAs");
        static final AuthPermission DO_AS_PRIVILEGED_PERMISSION=
                new AuthPermission("doAsPrivileged");
        static final AuthPermission SET_READ_ONLY_PERMISSION=
                new AuthPermission("setReadOnly");
        static final AuthPermission GET_SUBJECT_PERMISSION=
                new AuthPermission("getSubject");
        static final AuthPermission MODIFY_PRINCIPALS_PERMISSION=
                new AuthPermission("modifyPrincipals");
        static final AuthPermission MODIFY_PUBLIC_CREDENTIALS_PERMISSION=
                new AuthPermission("modifyPublicCredentials");
        static final AuthPermission MODIFY_PRIVATE_CREDENTIALS_PERMISSION=
                new AuthPermission("modifyPrivateCredentials");
    }

    private class ClassSet<T> extends AbstractSet<T>{
        private int which;
        private Class<T> c;
        private Set<T> set;

        ClassSet(int which,Class<T> c){
            this.which=which;
            this.c=c;
            set=new HashSet<T>();
            switch(which){
                case Subject.PRINCIPAL_SET:
                    synchronized(principals){
                        populateSet();
                    }
                    break;
                case Subject.PUB_CREDENTIAL_SET:
                    synchronized(pubCredentials){
                        populateSet();
                    }
                    break;
                default:
                    synchronized(privCredentials){
                        populateSet();
                    }
                    break;
            }
        }

        @SuppressWarnings("unchecked")
        /**To suppress warning from line 1374*/
        private void populateSet(){
            final Iterator<?> iterator;
            switch(which){
                case Subject.PRINCIPAL_SET:
                    iterator=Subject.this.principals.iterator();
                    break;
                case Subject.PUB_CREDENTIAL_SET:
                    iterator=Subject.this.pubCredentials.iterator();
                    break;
                default:
                    iterator=Subject.this.privCredentials.iterator();
                    break;
            }
            // Check whether the caller has permisson to get
            // credentials of Class c
            while(iterator.hasNext()){
                Object next;
                if(which==Subject.PRIV_CREDENTIAL_SET){
                    next=AccessController.doPrivileged
                            (new PrivilegedAction<Object>(){
                                public Object run(){
                                    return iterator.next();
                                }
                            });
                }else{
                    next=iterator.next();
                }
                if(c.isAssignableFrom(next.getClass())){
                    if(which!=Subject.PRIV_CREDENTIAL_SET){
                        set.add((T)next);
                    }else{
                        // Check permission for private creds
                        SecurityManager sm=System.getSecurityManager();
                        if(sm!=null){
                            sm.checkPermission(new PrivateCredentialPermission
                                    (next.getClass().getName(),
                                            Subject.this.getPrincipals()));
                        }
                        set.add((T)next);
                    }
                }
            }
        }

        public int size(){
            return set.size();
        }

        public Iterator<T> iterator(){
            return set.iterator();
        }

        public boolean add(T o){
            if(!o.getClass().isAssignableFrom(c)){
                MessageFormat form=new MessageFormat(ResourcesMgr.getString
                        ("attempting.to.add.an.object.which.is.not.an.instance.of.class"));
                Object[] source={c.toString()};
                throw new SecurityException(form.format(source));
            }
            return set.add(o);
        }
    }
}
