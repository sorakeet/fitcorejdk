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
import java.util.Set;

public interface Processor{
    Set<String> getSupportedOptions();

    Set<String> getSupportedAnnotationTypes();

    SourceVersion getSupportedSourceVersion();

    void init(ProcessingEnvironment processingEnv);

    boolean process(Set<? extends TypeElement> annotations,
                    RoundEnvironment roundEnv);

    Iterable<? extends Completion> getCompletions(Element element,
                                                  AnnotationMirror annotation,
                                                  ExecutableElement member,
                                                  String userText);
}
