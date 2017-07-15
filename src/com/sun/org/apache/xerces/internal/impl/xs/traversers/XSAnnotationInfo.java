/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.xs.traversers;

import com.sun.org.apache.xerces.internal.impl.xs.opti.ElementImpl;
import org.w3c.dom.Element;

final class XSAnnotationInfo{
    String fAnnotation;
    int fLine;
    int fColumn;
    int fCharOffset;
    XSAnnotationInfo next;

    XSAnnotationInfo(String annotation,int line,int column,int charOffset){
        fAnnotation=annotation;
        fLine=line;
        fColumn=column;
        fCharOffset=charOffset;
    }

    XSAnnotationInfo(String annotation,Element annotationDecl){
        fAnnotation=annotation;
        if(annotationDecl instanceof ElementImpl){
            final ElementImpl annotationDeclImpl=(ElementImpl)annotationDecl;
            fLine=annotationDeclImpl.getLineNumber();
            fColumn=annotationDeclImpl.getColumnNumber();
            fCharOffset=annotationDeclImpl.getCharacterOffset();
        }else{
            fLine=-1;
            fColumn=-1;
            fCharOffset=-1;
        }
    }
} // XSAnnotationInfo
