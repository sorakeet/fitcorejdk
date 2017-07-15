/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum SourceVersion{
    RELEASE_0,
    RELEASE_1,
    RELEASE_2,
    RELEASE_3,
    RELEASE_4,
    RELEASE_5,
    RELEASE_6,
    RELEASE_7,
    RELEASE_8;
    // Note that when adding constants for newer releases, the
    // behavior of latest() and latestSupported() must be updated too.

    private static final SourceVersion latestSupported=getLatestSupported();
    private final static Set<String> keywords;

    static{
        Set<String> s=new HashSet<String>();
        String[] kws={
                "abstract","continue","for","new","switch",
                "assert","default","if","package","synchronized",
                "boolean","do","goto","private","this",
                "break","double","implements","protected","throw",
                "byte","else","import","public","throws",
                "case","enum","instanceof","return","transient",
                "catch","extends","int","short","try",
                "char","final","interface","static","void",
                "class","finally","long","strictfp","volatile",
                "const","float","native","super","while",
                // literals
                "null","true","false"
        };
        for(String kw : kws)
            s.add(kw);
        keywords=Collections.unmodifiableSet(s);
    }

    public static SourceVersion latest(){
        return RELEASE_8;
    }

    private static SourceVersion getLatestSupported(){
        try{
            String specVersion=System.getProperty("java.specification.version");
            if("1.8".equals(specVersion))
                return RELEASE_8;
            else if("1.7".equals(specVersion))
                return RELEASE_7;
            else if("1.6".equals(specVersion))
                return RELEASE_6;
        }catch(SecurityException se){
        }
        return RELEASE_5;
    }

    public static SourceVersion latestSupported(){
        return latestSupported;
    }

    public static boolean isName(CharSequence name){
        String id=name.toString();
        for(String s : id.split("\\.",-1)){
            if(!isIdentifier(s)||isKeyword(s))
                return false;
        }
        return true;
    }

    public static boolean isIdentifier(CharSequence name){
        String id=name.toString();
        if(id.length()==0){
            return false;
        }
        int cp=id.codePointAt(0);
        if(!Character.isJavaIdentifierStart(cp)){
            return false;
        }
        for(int i=Character.charCount(cp);
            i<id.length();
            i+=Character.charCount(cp)){
            cp=id.codePointAt(i);
            if(!Character.isJavaIdentifierPart(cp)){
                return false;
            }
        }
        return true;
    }

    public static boolean isKeyword(CharSequence s){
        String keywordOrLiteral=s.toString();
        return keywords.contains(keywordOrLiteral);
    }
}
