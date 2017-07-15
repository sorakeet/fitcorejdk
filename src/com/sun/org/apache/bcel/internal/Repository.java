/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal;
/** ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.util.ClassPath;
import com.sun.org.apache.bcel.internal.util.SyntheticRepository;

import java.io.IOException;

public abstract class Repository{
    private static com.sun.org.apache.bcel.internal.util.Repository _repository=
            SyntheticRepository.getInstance();

    public static com.sun.org.apache.bcel.internal.util.Repository getRepository(){
        return _repository;
    }

    public static void setRepository(com.sun.org.apache.bcel.internal.util.Repository rep){
        _repository=rep;
    }

    public static JavaClass lookupClass(Class clazz){
        try{
            return _repository.loadClass(clazz);
        }catch(ClassNotFoundException ex){
            return null;
        }
    }

    public static ClassPath.ClassFile lookupClassFile(String class_name){
        try{
            return ClassPath.SYSTEM_CLASS_PATH.getClassFile(class_name);
        }catch(IOException e){
            return null;
        }
    }

    public static void clearCache(){
        _repository.clear();
    }

    public static JavaClass addClass(JavaClass clazz){
        JavaClass old=_repository.findClass(clazz.getClassName());
        _repository.storeClass(clazz);
        return old;
    }

    public static void removeClass(String clazz){
        _repository.removeClass(_repository.findClass(clazz));
    }

    public static void removeClass(JavaClass clazz){
        _repository.removeClass(clazz);
    }

    public static JavaClass[] getSuperClasses(String class_name){
        JavaClass jc=lookupClass(class_name);
        return (jc==null?null:getSuperClasses(jc));
    }

    public static JavaClass lookupClass(String class_name){
        try{
            JavaClass clazz=_repository.findClass(class_name);
            if(clazz==null){
                return _repository.loadClass(class_name);
            }else{
                return clazz;
            }
        }catch(ClassNotFoundException ex){
            return null;
        }
    }

    public static JavaClass[] getSuperClasses(JavaClass clazz){
        return clazz.getSuperClasses();
    }

    public static JavaClass[] getInterfaces(String class_name){
        return getInterfaces(lookupClass(class_name));
    }

    public static JavaClass[] getInterfaces(JavaClass clazz){
        return clazz.getAllInterfaces();
    }

    public static boolean instanceOf(String clazz,String super_class){
        return instanceOf(lookupClass(clazz),lookupClass(super_class));
    }

    public static boolean instanceOf(JavaClass clazz,JavaClass super_class){
        return clazz.instanceOf(super_class);
    }

    public static boolean instanceOf(JavaClass clazz,String super_class){
        return instanceOf(clazz,lookupClass(super_class));
    }

    public static boolean instanceOf(String clazz,JavaClass super_class){
        return instanceOf(lookupClass(clazz),super_class);
    }

    public static boolean implementationOf(String clazz,String inter){
        return implementationOf(lookupClass(clazz),lookupClass(inter));
    }

    public static boolean implementationOf(JavaClass clazz,JavaClass inter){
        return clazz.implementationOf(inter);
    }

    public static boolean implementationOf(JavaClass clazz,String inter){
        return implementationOf(clazz,lookupClass(inter));
    }

    public static boolean implementationOf(String clazz,JavaClass inter){
        return implementationOf(lookupClass(clazz),inter);
    }
}
