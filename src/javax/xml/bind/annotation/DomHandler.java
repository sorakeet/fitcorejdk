/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

public interface DomHandler<ElementT,ResultT extends Result>{
    ResultT createUnmarshaller(ValidationEventHandler errorHandler);

    ElementT getElement(ResultT rt);

    Source marshal(ElementT n,ValidationEventHandler errorHandler);
}
