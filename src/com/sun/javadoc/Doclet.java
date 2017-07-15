/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public abstract class Doclet{
    public static boolean start(RootDoc root){
        return true;
    }

    public static int optionLength(String option){
        return 0;  // default is option unknown
    }

    public static boolean validOptions(String options[][],
                                       DocErrorReporter reporter){
        return true;  // default is options are valid
    }

    public static LanguageVersion languageVersion(){
        return LanguageVersion.JAVA_1_1;
    }
}
