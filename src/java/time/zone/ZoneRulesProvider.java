/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * * Neither the name of JSR-310 nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 *
 *
 *
 *
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java.time.zone;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ZoneRulesProvider{
    private static final CopyOnWriteArrayList<ZoneRulesProvider> PROVIDERS=new CopyOnWriteArrayList<>();
    private static final ConcurrentMap<String,ZoneRulesProvider> ZONES=new ConcurrentHashMap<>(512,0.75f,2);

    static{
        // if the property java.time.zone.DefaultZoneRulesProvider is
        // set then its value is the class name of the default provider
        final List<ZoneRulesProvider> loaded=new ArrayList<>();
        AccessController.doPrivileged(new PrivilegedAction<Object>(){
            public Object run(){
                String prop=System.getProperty("java.time.zone.DefaultZoneRulesProvider");
                if(prop!=null){
                    try{
                        Class<?> c=Class.forName(prop,true,ClassLoader.getSystemClassLoader());
                        ZoneRulesProvider provider=ZoneRulesProvider.class.cast(c.newInstance());
                        registerProvider(provider);
                        loaded.add(provider);
                    }catch(Exception x){
                        throw new Error(x);
                    }
                }else{
                    registerProvider(new TzdbZoneRulesProvider());
                }
                return null;
            }
        });
        ServiceLoader<ZoneRulesProvider> sl=ServiceLoader.load(ZoneRulesProvider.class,ClassLoader.getSystemClassLoader());
        Iterator<ZoneRulesProvider> it=sl.iterator();
        while(it.hasNext()){
            ZoneRulesProvider provider;
            try{
                provider=it.next();
            }catch(ServiceConfigurationError ex){
                if(ex.getCause() instanceof SecurityException){
                    continue;  // ignore the security exception, try the next provider
                }
                throw ex;
            }
            boolean found=false;
            for(ZoneRulesProvider p : loaded){
                if(p.getClass()==provider.getClass()){
                    found=true;
                }
            }
            if(!found){
                registerProvider0(provider);
                loaded.add(provider);
            }
        }
        // CopyOnWriteList could be slow if lots of providers and each added individually
        PROVIDERS.addAll(loaded);
    }

    protected ZoneRulesProvider(){
    }

    //-------------------------------------------------------------------------
    public static Set<String> getAvailableZoneIds(){
        return new HashSet<>(ZONES.keySet());
    }

    public static ZoneRules getRules(String zoneId,boolean forCaching){
        Objects.requireNonNull(zoneId,"zoneId");
        return getProvider(zoneId).provideRules(zoneId,forCaching);
    }

    private static ZoneRulesProvider getProvider(String zoneId){
        ZoneRulesProvider provider=ZONES.get(zoneId);
        if(provider==null){
            if(ZONES.isEmpty()){
                throw new ZoneRulesException("No time-zone data files registered");
            }
            throw new ZoneRulesException("Unknown time-zone ID: "+zoneId);
        }
        return provider;
    }

    public static NavigableMap<String,ZoneRules> getVersions(String zoneId){
        Objects.requireNonNull(zoneId,"zoneId");
        return getProvider(zoneId).provideVersions(zoneId);
    }

    //-------------------------------------------------------------------------
    public static void registerProvider(ZoneRulesProvider provider){
        Objects.requireNonNull(provider,"provider");
        registerProvider0(provider);
        PROVIDERS.add(provider);
    }

    private static void registerProvider0(ZoneRulesProvider provider){
        for(String zoneId : provider.provideZoneIds()){
            Objects.requireNonNull(zoneId,"zoneId");
            ZoneRulesProvider old=ZONES.putIfAbsent(zoneId,provider);
            if(old!=null){
                throw new ZoneRulesException(
                        "Unable to register zone as one already registered with that ID: "+zoneId+
                                ", currently loading from provider: "+provider);
            }
        }
    }

    public static boolean refresh(){
        boolean changed=false;
        for(ZoneRulesProvider provider : PROVIDERS){
            changed|=provider.provideRefresh();
        }
        return changed;
    }

    //-----------------------------------------------------------------------
    protected abstract Set<String> provideZoneIds();

    protected abstract ZoneRules provideRules(String zoneId,boolean forCaching);

    protected abstract NavigableMap<String,ZoneRules> provideVersions(String zoneId);

    protected boolean provideRefresh(){
        return false;
    }
}
