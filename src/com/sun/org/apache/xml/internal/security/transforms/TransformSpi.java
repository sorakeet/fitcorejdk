/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sun.org.apache.xml.internal.security.transforms;

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;

public abstract class TransformSpi{
    protected XMLSignatureInput enginePerformTransform(
            XMLSignatureInput input
    ) throws IOException, CanonicalizationException, InvalidCanonicalizerException,
            TransformationException, ParserConfigurationException, SAXException{
        return enginePerformTransform(input,null);
    }

    protected XMLSignatureInput enginePerformTransform(
            XMLSignatureInput input,Transform transformObject
    ) throws IOException, CanonicalizationException, InvalidCanonicalizerException,
            TransformationException, ParserConfigurationException, SAXException{
        return enginePerformTransform(input,null,transformObject);
    }

    protected XMLSignatureInput enginePerformTransform(
            XMLSignatureInput input,OutputStream os,Transform transformObject
    ) throws IOException, CanonicalizationException, InvalidCanonicalizerException,
            TransformationException, ParserConfigurationException, SAXException{
        throw new UnsupportedOperationException();
    }

    protected abstract String engineGetURI();
}