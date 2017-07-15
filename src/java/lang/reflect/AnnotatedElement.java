/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.reflect;

import sun.reflect.annotation.AnnotationSupport;
import sun.reflect.annotation.AnnotationType;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface AnnotatedElement{
    default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass){
        return getAnnotation(annotationClass)!=null;
    }

    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    Annotation[] getAnnotations();

    default <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass){
        /**
         * Definition of associated: directly or indirectly present OR
         * neither directly nor indirectly present AND the element is
         * a Class, the annotation type is inheritable, and the
         * annotation type is associated with the superclass of the
         * element.
         */
        T[] result=getDeclaredAnnotationsByType(annotationClass);
        if(result.length==0&& // Neither directly nor indirectly present
                this instanceof Class&& // the element is a class
                AnnotationType.getInstance(annotationClass).isInherited()){ // Inheritable
            Class<?> superClass=((Class<?>)this).getSuperclass();
            if(superClass!=null){
                // Determine if the annotation is associated with the
                // superclass
                result=superClass.getAnnotationsByType(annotationClass);
            }
        }
        return result;
    }

    default <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass){
        Objects.requireNonNull(annotationClass);
        return AnnotationSupport.
                getDirectlyAndIndirectlyPresent(Arrays.stream(getDeclaredAnnotations()).
                                collect(Collectors.toMap(Annotation::annotationType,
                                        Function.identity(),
                                        ((first,second)->first),
                                        LinkedHashMap::new)),
                        annotationClass);
    }

    Annotation[] getDeclaredAnnotations();

    default <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass){
        Objects.requireNonNull(annotationClass);
        // Loop over all directly-present annotations looking for a matching one
        for(Annotation annotation : getDeclaredAnnotations()){
            if(annotationClass.equals(annotation.annotationType())){
                // More robust to do a dynamic cast at runtime instead
                // of compile-time only.
                return annotationClass.cast(annotation);
            }
        }
        return null;
    }
}
