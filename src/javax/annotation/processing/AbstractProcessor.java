/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.annotation.processing;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractProcessor implements Processor{
    protected ProcessingEnvironment processingEnv;
    private boolean initialized=false;

    protected AbstractProcessor(){
    }

    public Set<String> getSupportedOptions(){
        SupportedOptions so=this.getClass().getAnnotation(SupportedOptions.class);
        if(so==null)
            return Collections.emptySet();
        else
            return arrayToSet(so.value());
    }

    public Set<String> getSupportedAnnotationTypes(){
        SupportedAnnotationTypes sat=this.getClass().getAnnotation(SupportedAnnotationTypes.class);
        if(sat==null){
            if(isInitialized())
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "No SupportedAnnotationTypes annotation "+
                                "found on "+this.getClass().getName()+
                                ", returning an empty set.");
            return Collections.emptySet();
        }else
            return arrayToSet(sat.value());
    }

    public SourceVersion getSupportedSourceVersion(){
        SupportedSourceVersion ssv=this.getClass().getAnnotation(SupportedSourceVersion.class);
        SourceVersion sv=null;
        if(ssv==null){
            sv=SourceVersion.RELEASE_6;
            if(isInitialized())
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "No SupportedSourceVersion annotation "+
                                "found on "+this.getClass().getName()+
                                ", returning "+sv+".");
        }else
            sv=ssv.value();
        return sv;
    }

    public synchronized void init(ProcessingEnvironment processingEnv){
        if(initialized)
            throw new IllegalStateException("Cannot call init more than once.");
        Objects.requireNonNull(processingEnv,"Tool provided null ProcessingEnvironment");
        this.processingEnv=processingEnv;
        initialized=true;
    }

    public abstract boolean process(Set<? extends TypeElement> annotations,
                                    RoundEnvironment roundEnv);

    public Iterable<? extends Completion> getCompletions(Element element,
                                                         AnnotationMirror annotation,
                                                         ExecutableElement member,
                                                         String userText){
        return Collections.emptyList();
    }

    protected synchronized boolean isInitialized(){
        return initialized;
    }

    private static Set<String> arrayToSet(String[] array){
        assert array!=null;
        Set<String> set=new HashSet<String>(array.length);
        for(String s : array)
            set.add(s);
        return Collections.unmodifiableSet(set);
    }
}
