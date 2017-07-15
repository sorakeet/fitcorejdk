/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.cert;

import java.net.URI;
import java.util.*;

public abstract class PKIXRevocationChecker extends PKIXCertPathChecker{
    private URI ocspResponder;
    private X509Certificate ocspResponderCert;
    private List<Extension> ocspExtensions=Collections.<Extension>emptyList();
    private Map<X509Certificate,byte[]> ocspResponses=Collections.emptyMap();
    private Set<Option> options=Collections.emptySet();

    protected PKIXRevocationChecker(){
    }

    public URI getOcspResponder(){
        return ocspResponder;
    }

    public void setOcspResponder(URI uri){
        this.ocspResponder=uri;
    }

    public X509Certificate getOcspResponderCert(){
        return ocspResponderCert;
    }

    public void setOcspResponderCert(X509Certificate cert){
        this.ocspResponderCert=cert;
    }

    public List<Extension> getOcspExtensions(){
        return Collections.unmodifiableList(ocspExtensions);
    }

    // request extensions; single extensions not supported
    public void setOcspExtensions(List<Extension> extensions){
        this.ocspExtensions=(extensions==null)
                ?Collections.<Extension>emptyList()
                :new ArrayList<Extension>(extensions);
    }

    public Map<X509Certificate,byte[]> getOcspResponses(){
        Map<X509Certificate,byte[]> copy=new HashMap<>(ocspResponses.size());
        for(Map.Entry<X509Certificate,byte[]> e : ocspResponses.entrySet()){
            copy.put(e.getKey(),e.getValue().clone());
        }
        return copy;
    }

    public void setOcspResponses(Map<X509Certificate,byte[]> responses){
        if(responses==null){
            this.ocspResponses=Collections.<X509Certificate,byte[]>emptyMap();
        }else{
            Map<X509Certificate,byte[]> copy=new HashMap<>(responses.size());
            for(Map.Entry<X509Certificate,byte[]> e : responses.entrySet()){
                copy.put(e.getKey(),e.getValue().clone());
            }
            this.ocspResponses=copy;
        }
    }

    public Set<Option> getOptions(){
        return Collections.unmodifiableSet(options);
    }

    public void setOptions(Set<Option> options){
        this.options=(options==null)
                ?Collections.<Option>emptySet()
                :new HashSet<Option>(options);
    }

    public abstract List<CertPathValidatorException> getSoftFailExceptions();

    @Override
    public PKIXRevocationChecker clone(){
        PKIXRevocationChecker copy=(PKIXRevocationChecker)super.clone();
        copy.ocspExtensions=new ArrayList<>(ocspExtensions);
        copy.ocspResponses=new HashMap<>(ocspResponses);
        // deep-copy the encoded responses, since they are mutable
        for(Map.Entry<X509Certificate,byte[]> entry :
                copy.ocspResponses.entrySet()){
            byte[] encoded=entry.getValue();
            entry.setValue(encoded.clone());
        }
        copy.options=new HashSet<>(options);
        return copy;
    }

    public enum Option{
        ONLY_END_ENTITY,
        PREFER_CRLS,
        NO_FALLBACK,
        SOFT_FAIL
    }
}
