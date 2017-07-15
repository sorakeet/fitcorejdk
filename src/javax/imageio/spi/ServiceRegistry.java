/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import java.util.*;

public class ServiceRegistry{
    // Class -> Registry
    private Map categoryMap=new HashMap();

    public ServiceRegistry(Iterator<Class<?>> categories){
        if(categories==null){
            throw new IllegalArgumentException("categories == null!");
        }
        while(categories.hasNext()){
            Class category=(Class)categories.next();
            SubRegistry reg=new SubRegistry(this,category);
            categoryMap.put(category,reg);
        }
    }
    // The following two methods expose functionality from
    // sun.misc.Service.  If that class is made public, they may be
    // removed.
    //
    // The sun.misc.ServiceConfigurationError class may also be
    // exposed, in which case the references to 'an
    // <code>Error</code>' below should be changed to 'a
    // <code>ServiceConfigurationError</code>'.

    public static <T> Iterator<T> lookupProviders(Class<T> providerClass,
                                                  ClassLoader loader){
        if(providerClass==null){
            throw new IllegalArgumentException("providerClass == null!");
        }
        return ServiceLoader.load(providerClass,loader).iterator();
    }

    public static <T> Iterator<T> lookupProviders(Class<T> providerClass){
        if(providerClass==null){
            throw new IllegalArgumentException("providerClass == null!");
        }
        return ServiceLoader.load(providerClass).iterator();
    }

    public Iterator<Class<?>> getCategories(){
        Set keySet=categoryMap.keySet();
        return keySet.iterator();
    }

    public <T> boolean registerServiceProvider(T provider,
                                               Class<T> category){
        if(provider==null){
            throw new IllegalArgumentException("provider == null!");
        }
        SubRegistry reg=(SubRegistry)categoryMap.get(category);
        if(reg==null){
            throw new IllegalArgumentException("category unknown!");
        }
        if(!category.isAssignableFrom(provider.getClass())){
            throw new ClassCastException();
        }
        return reg.registerServiceProvider(provider);
    }

    public void registerServiceProviders(Iterator<?> providers){
        if(providers==null){
            throw new IllegalArgumentException("provider == null!");
        }
        while(providers.hasNext()){
            registerServiceProvider(providers.next());
        }
    }

    public void registerServiceProvider(Object provider){
        if(provider==null){
            throw new IllegalArgumentException("provider == null!");
        }
        Iterator regs=getSubRegistries(provider);
        while(regs.hasNext()){
            SubRegistry reg=(SubRegistry)regs.next();
            reg.registerServiceProvider(provider);
        }
    }

    private Iterator getSubRegistries(Object provider){
        List l=new ArrayList();
        Iterator iter=categoryMap.keySet().iterator();
        while(iter.hasNext()){
            Class c=(Class)iter.next();
            if(c.isAssignableFrom(provider.getClass())){
                l.add((SubRegistry)categoryMap.get(c));
            }
        }
        return l.iterator();
    }

    public <T> boolean deregisterServiceProvider(T provider,
                                                 Class<T> category){
        if(provider==null){
            throw new IllegalArgumentException("provider == null!");
        }
        SubRegistry reg=(SubRegistry)categoryMap.get(category);
        if(reg==null){
            throw new IllegalArgumentException("category unknown!");
        }
        if(!category.isAssignableFrom(provider.getClass())){
            throw new ClassCastException();
        }
        return reg.deregisterServiceProvider(provider);
    }

    public void deregisterServiceProvider(Object provider){
        if(provider==null){
            throw new IllegalArgumentException("provider == null!");
        }
        Iterator regs=getSubRegistries(provider);
        while(regs.hasNext()){
            SubRegistry reg=(SubRegistry)regs.next();
            reg.deregisterServiceProvider(provider);
        }
    }

    public boolean contains(Object provider){
        if(provider==null){
            throw new IllegalArgumentException("provider == null!");
        }
        Iterator regs=getSubRegistries(provider);
        while(regs.hasNext()){
            SubRegistry reg=(SubRegistry)regs.next();
            if(reg.contains(provider)){
                return true;
            }
        }
        return false;
    }

    public <T> Iterator<T> getServiceProviders(Class<T> category,
                                               Filter filter,
                                               boolean useOrdering){
        SubRegistry reg=(SubRegistry)categoryMap.get(category);
        if(reg==null){
            throw new IllegalArgumentException("category unknown!");
        }
        Iterator iter=getServiceProviders(category,useOrdering);
        return new FilterIterator(iter,filter);
    }

    public <T> Iterator<T> getServiceProviders(Class<T> category,
                                               boolean useOrdering){
        SubRegistry reg=(SubRegistry)categoryMap.get(category);
        if(reg==null){
            throw new IllegalArgumentException("category unknown!");
        }
        return reg.getServiceProviders(useOrdering);
    }

    public <T> T getServiceProviderByClass(Class<T> providerClass){
        if(providerClass==null){
            throw new IllegalArgumentException("providerClass == null!");
        }
        Iterator iter=categoryMap.keySet().iterator();
        while(iter.hasNext()){
            Class c=(Class)iter.next();
            if(c.isAssignableFrom(providerClass)){
                SubRegistry reg=(SubRegistry)categoryMap.get(c);
                T provider=reg.getServiceProviderByClass(providerClass);
                if(provider!=null){
                    return provider;
                }
            }
        }
        return null;
    }

    public <T> boolean setOrdering(Class<T> category,
                                   T firstProvider,
                                   T secondProvider){
        if(firstProvider==null||secondProvider==null){
            throw new IllegalArgumentException("provider is null!");
        }
        if(firstProvider==secondProvider){
            throw new IllegalArgumentException("providers are the same!");
        }
        SubRegistry reg=(SubRegistry)categoryMap.get(category);
        if(reg==null){
            throw new IllegalArgumentException("category unknown!");
        }
        if(reg.contains(firstProvider)&&
                reg.contains(secondProvider)){
            return reg.setOrdering(firstProvider,secondProvider);
        }
        return false;
    }

    public <T> boolean unsetOrdering(Class<T> category,
                                     T firstProvider,
                                     T secondProvider){
        if(firstProvider==null||secondProvider==null){
            throw new IllegalArgumentException("provider is null!");
        }
        if(firstProvider==secondProvider){
            throw new IllegalArgumentException("providers are the same!");
        }
        SubRegistry reg=(SubRegistry)categoryMap.get(category);
        if(reg==null){
            throw new IllegalArgumentException("category unknown!");
        }
        if(reg.contains(firstProvider)&&
                reg.contains(secondProvider)){
            return reg.unsetOrdering(firstProvider,secondProvider);
        }
        return false;
    }

    public void deregisterAll(Class<?> category){
        SubRegistry reg=(SubRegistry)categoryMap.get(category);
        if(reg==null){
            throw new IllegalArgumentException("category unknown!");
        }
        reg.clear();
    }

    public void finalize() throws Throwable{
        deregisterAll();
        super.finalize();
    }

    public void deregisterAll(){
        Iterator iter=categoryMap.values().iterator();
        while(iter.hasNext()){
            SubRegistry reg=(SubRegistry)iter.next();
            reg.clear();
        }
    }

    public interface Filter{
        boolean filter(Object provider);
    }
}

class SubRegistry{
    ServiceRegistry registry;
    Class category;
    // Provider Objects organized by partial oridering
    PartiallyOrderedSet poset=new PartiallyOrderedSet();
    // Class -> Provider Object of that class
    Map<Class<?>,Object> map=new HashMap();

    public SubRegistry(ServiceRegistry registry,Class category){
        this.registry=registry;
        this.category=category;
    }

    public boolean registerServiceProvider(Object provider){
        Object oprovider=map.get(provider.getClass());
        boolean present=oprovider!=null;
        if(present){
            deregisterServiceProvider(oprovider);
        }
        map.put(provider.getClass(),provider);
        poset.add(provider);
        if(provider instanceof RegisterableService){
            RegisterableService rs=(RegisterableService)provider;
            rs.onRegistration(registry,category);
        }
        return !present;
    }

    public boolean deregisterServiceProvider(Object provider){
        Object oprovider=map.get(provider.getClass());
        if(provider==oprovider){
            map.remove(provider.getClass());
            poset.remove(provider);
            if(provider instanceof RegisterableService){
                RegisterableService rs=(RegisterableService)provider;
                rs.onDeregistration(registry,category);
            }
            return true;
        }
        return false;
    }

    public boolean contains(Object provider){
        Object oprovider=map.get(provider.getClass());
        return oprovider==provider;
    }

    public boolean setOrdering(Object firstProvider,
                               Object secondProvider){
        return poset.setOrdering(firstProvider,secondProvider);
    }

    public boolean unsetOrdering(Object firstProvider,
                                 Object secondProvider){
        return poset.unsetOrdering(firstProvider,secondProvider);
    }

    public Iterator getServiceProviders(boolean useOrdering){
        if(useOrdering){
            return poset.iterator();
        }else{
            return map.values().iterator();
        }
    }

    public <T> T getServiceProviderByClass(Class<T> providerClass){
        return (T)map.get(providerClass);
    }

    public void clear(){
        Iterator iter=map.values().iterator();
        while(iter.hasNext()){
            Object provider=iter.next();
            iter.remove();
            if(provider instanceof RegisterableService){
                RegisterableService rs=(RegisterableService)provider;
                rs.onDeregistration(registry,category);
            }
        }
        poset.clear();
    }

    public void finalize(){
        clear();
    }
}

class FilterIterator<T> implements Iterator<T>{
    private Iterator<T> iter;
    private ServiceRegistry.Filter filter;
    private T next=null;

    public FilterIterator(Iterator<T> iter,
                          ServiceRegistry.Filter filter){
        this.iter=iter;
        this.filter=filter;
        advance();
    }

    private void advance(){
        while(iter.hasNext()){
            T elt=iter.next();
            if(filter.filter(elt)){
                next=elt;
                return;
            }
        }
        next=null;
    }

    public boolean hasNext(){
        return next!=null;
    }

    public T next(){
        if(next==null){
            throw new NoSuchElementException();
        }
        T o=next;
        advance();
        return o;
    }

    public void remove(){
        throw new UnsupportedOperationException();
    }
}
