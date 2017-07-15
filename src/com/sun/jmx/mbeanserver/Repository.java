/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import com.sun.jmx.defaults.ServiceName;

import javax.management.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MBEANSERVER_LOGGER;

public class Repository{
    // Private fields -------------------------------------------->
    private final Map<String,Map<String,NamedObject>> domainTb;
    private final String domain;
    private final ReentrantReadWriteLock lock;
    private volatile int nbElements=0;
    public Repository(String domain){
        this(domain,true);
    }
    // Private fields <=============================================
    // Private methods --------------------------------------------->

    public Repository(String domain,boolean fairLock){
        lock=new ReentrantReadWriteLock(fairLock);
        domainTb=new HashMap<String,Map<String,NamedObject>>(5);
        if(domain!=null&&domain.length()!=0)
            this.domain=domain.intern(); // we use == domain later on...
        else
            this.domain=ServiceName.DOMAIN;
        // Creates a new hashtable for the default domain
        domainTb.put(this.domain,new HashMap<String,NamedObject>());
    }

    private void addNewDomMoi(final DynamicMBean object,
                              final String dom,
                              final ObjectName name,
                              final RegistrationContext context){
        final Map<String,NamedObject> moiTb=
                new HashMap<String,NamedObject>();
        final String key=name.getCanonicalKeyPropertyListString();
        addMoiToTb(object,name,key,moiTb,context);
        domainTb.put(dom,moiTb);
        nbElements++;
    }

    private void registering(RegistrationContext context){
        if(context==null) return;
        try{
            context.registering();
        }catch(RuntimeOperationsException x){
            throw x;
        }catch(RuntimeException x){
            throw new RuntimeOperationsException(x);
        }
    }

    private void addMoiToTb(final DynamicMBean object,
                            final ObjectName name,
                            final String key,
                            final Map<String,NamedObject> moiTb,
                            final RegistrationContext context){
        registering(context);
        moiTb.put(key,new NamedObject(name,object));
    }

    public String[] getDomains(){
        lock.readLock().lock();
        final List<String> result;
        try{
            // Temporary list
            result=new ArrayList<String>(domainTb.size());
            for(Map.Entry<String,Map<String,NamedObject>> entry :
                    domainTb.entrySet()){
                // Skip domains that are in the table but have no
                // MBean registered in them
                // in particular the default domain may be like this
                Map<String,NamedObject> t=entry.getValue();
                if(t!=null&&t.size()!=0)
                    result.add(entry.getKey());
            }
        }finally{
            lock.readLock().unlock();
        }
        // Make an array from result.
        return result.toArray(new String[result.size()]);
    }

    public void addMBean(final DynamicMBean object,ObjectName name,
                         final RegistrationContext context)
            throws InstanceAlreadyExistsException{
        if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
            MBEANSERVER_LOGGER.logp(Level.FINER,Repository.class.getName(),
                    "addMBean","name = "+name);
        }
        // Extract the domain name.
        String dom=name.getDomain().intern();
        boolean to_default_domain=false;
        // Set domain to default if domain is empty and not already set
        if(dom.length()==0)
            name=Util.newObjectName(domain+name.toString());
        // Do we have default domain ?
        if(dom==domain){  // ES: OK (dom & domain are interned)
            to_default_domain=true;
            dom=domain;
        }else{
            to_default_domain=false;
        }
        // Validate name for an object
        if(name.isPattern()){
            throw new RuntimeOperationsException(
                    new IllegalArgumentException("Repository: cannot add mbean for "+
                            "pattern name "+name.toString()));
        }
        lock.writeLock().lock();
        try{
            // Domain cannot be JMImplementation if entry does not exist
            if(!to_default_domain&&
                    dom.equals("JMImplementation")&&
                    domainTb.containsKey("JMImplementation")){
                throw new RuntimeOperationsException(
                        new IllegalArgumentException(
                                "Repository: domain name cannot be JMImplementation"));
            }
            // If domain does not already exist, add it to the hash table
            final Map<String,NamedObject> moiTb=domainTb.get(dom);
            if(moiTb==null){
                addNewDomMoi(object,dom,name,context);
                return;
            }else{
                // Add instance if not already present
                String cstr=name.getCanonicalKeyPropertyListString();
                NamedObject elmt=moiTb.get(cstr);
                if(elmt!=null){
                    throw new InstanceAlreadyExistsException(name.toString());
                }else{
                    nbElements++;
                    addMoiToTb(object,name,cstr,moiTb,context);
                }
            }
        }finally{
            lock.writeLock().unlock();
        }
    }

    public boolean contains(ObjectName name){
        if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
            MBEANSERVER_LOGGER.logp(Level.FINER,Repository.class.getName(),
                    "contains"," name = "+name);
        }
        lock.readLock().lock();
        try{
            return (retrieveNamedObject(name)!=null);
        }finally{
            lock.readLock().unlock();
        }
    }
    // Private methods <=============================================
    // Protected methods --------------------------------------------->
    // Protected methods <=============================================
    // Public methods --------------------------------------------->

    private NamedObject retrieveNamedObject(ObjectName name){
        // No patterns inside reposit
        if(name.isPattern()) return null;
        // Extract the domain name.
        String dom=name.getDomain().intern();
        // Default domain case
        if(dom.length()==0){
            dom=domain;
        }
        Map<String,NamedObject> moiTb=domainTb.get(dom);
        if(moiTb==null){
            return null; // No domain containing registered object names
        }
        return moiTb.get(name.getCanonicalKeyPropertyListString());
    }

    public DynamicMBean retrieve(ObjectName name){
        if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
            MBEANSERVER_LOGGER.logp(Level.FINER,Repository.class.getName(),
                    "retrieve","name = "+name);
        }
        // Calls internal retrieve method to get the named object
        lock.readLock().lock();
        try{
            NamedObject no=retrieveNamedObject(name);
            if(no==null) return null;
            else return no.getObject();
        }finally{
            lock.readLock().unlock();
        }
    }

    public Set<NamedObject> query(ObjectName pattern,QueryExp query){
        final Set<NamedObject> result=new HashSet<NamedObject>();
        // The following filter cases are considered:
        // null, "", "*:*" : names in all domains
        // ":*", ":[key=value],*" : names in defaultDomain
        // "domain:*", "domain:[key=value],*" : names in the specified domain
        // Surely one of the most frequent cases ... query on the whole world
        ObjectName name;
        if(pattern==null||
                pattern.getCanonicalName().length()==0||
                pattern.equals(ObjectName.WILDCARD))
            name=ObjectName.WILDCARD;
        else name=pattern;
        lock.readLock().lock();
        try{
            // If pattern is not a pattern, retrieve this mbean !
            if(!name.isPattern()){
                final NamedObject no=retrieveNamedObject(name);
                if(no!=null) result.add(no);
                return result;
            }
            // All names in all domains
            if(name==ObjectName.WILDCARD){
                for(Map<String,NamedObject> moiTb : domainTb.values()){
                    result.addAll(moiTb.values());
                }
                return result;
            }
            final String canonical_key_property_list_string=
                    name.getCanonicalKeyPropertyListString();
            final boolean allNames=
                    (canonical_key_property_list_string.length()==0);
            final ObjectNamePattern namePattern=
                    (allNames?null:new ObjectNamePattern(name));
            // All names in default domain
            if(name.getDomain().length()==0){
                final Map<String,NamedObject> moiTb=domainTb.get(domain);
                if(allNames)
                    result.addAll(moiTb.values());
                else
                    addAllMatching(moiTb,result,namePattern);
                return result;
            }
            if(!name.isDomainPattern()){
                final Map<String,NamedObject> moiTb=domainTb.get(name.getDomain());
                if(moiTb==null) return Collections.emptySet();
                if(allNames)
                    result.addAll(moiTb.values());
                else
                    addAllMatching(moiTb,result,namePattern);
                return result;
            }
            // Pattern matching in the domain name (*, ?)
            final String dom2Match=name.getDomain();
            for(String dom : domainTb.keySet()){
                if(Util.wildmatch(dom,dom2Match)){
                    final Map<String,NamedObject> moiTb=domainTb.get(dom);
                    if(allNames)
                        result.addAll(moiTb.values());
                    else
                        addAllMatching(moiTb,result,namePattern);
                }
            }
            return result;
        }finally{
            lock.readLock().unlock();
        }
    }

    private void addAllMatching(final Map<String,NamedObject> moiTb,
                                final Set<NamedObject> result,
                                final ObjectNamePattern pattern){
        synchronized(moiTb){
            for(NamedObject no : moiTb.values()){
                final ObjectName on=no.getName();
                // if all couples (property, value) are contained
                if(pattern.matchKeys(on)) result.add(no);
            }
        }
    }

    public void remove(final ObjectName name,
                       final RegistrationContext context)
            throws InstanceNotFoundException{
        // Debugging stuff
        if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
            MBEANSERVER_LOGGER.logp(Level.FINER,Repository.class.getName(),
                    "remove","name = "+name);
        }
        // Extract domain name.
        String dom=name.getDomain().intern();
        // Default domain case
        if(dom.length()==0) dom=domain;
        lock.writeLock().lock();
        try{
            // Find the domain subtable
            final Map<String,NamedObject> moiTb=domainTb.get(dom);
            if(moiTb==null){
                throw new InstanceNotFoundException(name.toString());
            }
            // Remove the corresponding element
            if(moiTb.remove(name.getCanonicalKeyPropertyListString())==null){
                throw new InstanceNotFoundException(name.toString());
            }
            // We removed it !
            nbElements--;
            // No more object for this domain, we remove this domain hashtable
            if(moiTb.isEmpty()){
                domainTb.remove(dom);
                // set a new default domain table (always present)
                // need to reinstantiate a hashtable because of possible
                // big buckets array size inside table, never cleared,
                // thus the new !
                if(dom==domain) // ES: OK dom and domain are interned.
                    domainTb.put(domain,new HashMap<String,NamedObject>());
            }
            unregistering(context,name);
        }finally{
            lock.writeLock().unlock();
        }
    }

    private void unregistering(RegistrationContext context,ObjectName name){
        if(context==null) return;
        try{
            context.unregistered();
        }catch(Exception x){
            // shouldn't come here...
            MBEANSERVER_LOGGER.log(Level.FINE,
                    "Unexpected exception while unregistering "+name,
                    x);
        }
    }

    public Integer getCount(){
        return nbElements;
    }

    public String getDefaultDomain(){
        return domain;
    }

    public interface RegistrationContext{
        public void registering();

        public void unregistered();
    }

    private final static class ObjectNamePattern{
        public final ObjectName pattern;
        private final String[] keys;
        private final String[] values;
        private final String properties;
        private final boolean isPropertyListPattern;
        private final boolean isPropertyValuePattern;

        public ObjectNamePattern(ObjectName pattern){
            this(pattern.isPropertyListPattern(),
                    pattern.isPropertyValuePattern(),
                    pattern.getCanonicalKeyPropertyListString(),
                    pattern.getKeyPropertyList(),
                    pattern);
        }

        ObjectNamePattern(boolean propertyListPattern,
                          boolean propertyValuePattern,
                          String canonicalProps,
                          Map<String,String> keyPropertyList,
                          ObjectName pattern){
            this.isPropertyListPattern=propertyListPattern;
            this.isPropertyValuePattern=propertyValuePattern;
            this.properties=canonicalProps;
            final int len=keyPropertyList.size();
            this.keys=new String[len];
            this.values=new String[len];
            int i=0;
            for(Map.Entry<String,String> entry : keyPropertyList.entrySet()){
                keys[i]=entry.getKey();
                values[i]=entry.getValue();
                i++;
            }
            this.pattern=pattern;
        }

        public boolean matchKeys(ObjectName name){
            // If key property value pattern but not key property list
            // pattern, then the number of key properties must be equal
            //
            if(isPropertyValuePattern&&
                    !isPropertyListPattern&&
                    (name.getKeyPropertyList().size()!=keys.length))
                return false;
            // If key property value pattern or key property list pattern,
            // then every property inside pattern should exist in name
            //
            if(isPropertyValuePattern||isPropertyListPattern){
                for(int i=keys.length-1;i>=0;i--){
                    // Find value in given object name for key at current
                    // index in receiver
                    //
                    String v=name.getKeyProperty(keys[i]);
                    // Did we find a value for this key ?
                    //
                    if(v==null) return false;
                    // If this property is ok (same key, same value), go to next
                    //
                    if(isPropertyValuePattern&&
                            pattern.isPropertyValuePattern(keys[i])){
                        // wildmatch key property values
                        // values[i] is the pattern;
                        // v is the string
                        if(Util.wildmatch(v,values[i]))
                            continue;
                        else
                            return false;
                    }
                    if(v.equals(values[i])) continue;
                    return false;
                }
                return true;
            }
            // If no pattern, then canonical names must be equal
            //
            final String p1=name.getCanonicalKeyPropertyListString();
            final String p2=properties;
            return (p1.equals(p2));
        }
    }
    // Public methods <=============================================
}
