/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.spec;

public class PSSParameterSpec implements AlgorithmParameterSpec{
    public static final PSSParameterSpec DEFAULT=new PSSParameterSpec();
    private String mdName="SHA-1";
    private String mgfName="MGF1";
    private AlgorithmParameterSpec mgfSpec=MGF1ParameterSpec.SHA1;
    private int saltLen=20;
    private int trailerField=1;

    private PSSParameterSpec(){
    }

    public PSSParameterSpec(String mdName,String mgfName,
                            AlgorithmParameterSpec mgfSpec,
                            int saltLen,int trailerField){
        if(mdName==null){
            throw new NullPointerException("digest algorithm is null");
        }
        if(mgfName==null){
            throw new NullPointerException("mask generation function "+
                    "algorithm is null");
        }
        if(saltLen<0){
            throw new IllegalArgumentException("negative saltLen value: "+
                    saltLen);
        }
        if(trailerField<0){
            throw new IllegalArgumentException("negative trailerField: "+
                    trailerField);
        }
        this.mdName=mdName;
        this.mgfName=mgfName;
        this.mgfSpec=mgfSpec;
        this.saltLen=saltLen;
        this.trailerField=trailerField;
    }

    public PSSParameterSpec(int saltLen){
        if(saltLen<0){
            throw new IllegalArgumentException("negative saltLen value: "+
                    saltLen);
        }
        this.saltLen=saltLen;
    }

    public String getDigestAlgorithm(){
        return mdName;
    }

    public String getMGFAlgorithm(){
        return mgfName;
    }

    public AlgorithmParameterSpec getMGFParameters(){
        return mgfSpec;
    }

    public int getSaltLength(){
        return saltLen;
    }

    public int getTrailerField(){
        return trailerField;
    }
}
