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
package com.sun.org.apache.xml.internal.security.algorithms;

import com.sun.org.apache.xml.internal.security.signature.XMLSignatureException;
import org.w3c.dom.Element;

import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class SignatureAlgorithmSpi{
    protected abstract String engineGetURI();

    protected abstract String engineGetJCEAlgorithmString();

    protected abstract String engineGetJCEProviderName();

    protected abstract void engineUpdate(byte[] input) throws XMLSignatureException;

    protected abstract void engineUpdate(byte input) throws XMLSignatureException;

    protected abstract void engineUpdate(byte buf[],int offset,int len)
            throws XMLSignatureException;

    protected abstract void engineInitSign(Key signingKey) throws XMLSignatureException;

    protected abstract void engineInitSign(Key signingKey,SecureRandom secureRandom)
            throws XMLSignatureException;

    protected abstract void engineInitSign(
            Key signingKey,AlgorithmParameterSpec algorithmParameterSpec
    ) throws XMLSignatureException;

    protected abstract byte[] engineSign() throws XMLSignatureException;

    protected abstract void engineInitVerify(Key verificationKey) throws XMLSignatureException;

    protected abstract boolean engineVerify(byte[] signature) throws XMLSignatureException;

    protected abstract void engineSetParameter(AlgorithmParameterSpec params)
            throws XMLSignatureException;

    protected void engineGetContextFromElement(Element element){
    }

    protected abstract void engineSetHMACOutputLength(int HMACOutputLength)
            throws XMLSignatureException;

    public void reset(){
    }
}
