/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xalan.internal.xsltc.runtime;

import java.util.ListResourceBundle;

public class ErrorMessages extends ListResourceBundle{
    // These message should be read from a locale-specific resource bundle
    public Object[][] getContents(){
        return new Object[][]{
                /**
                 * Note to translators:  the substitution text in the following message
                 * is a class name.  Used for internal errors in the processor.
                */
                {BasisLibrary.RUN_TIME_INTERNAL_ERR,
                        "Run-time internal error in ''{0}''"},
                /**
                 * Note to translators:  <xsl:copy> is a keyword that should not be
                 * translated.
                */
                {BasisLibrary.RUN_TIME_COPY_ERR,
                        "Run-time error when executing <xsl:copy>."},
                /**
                 * Note to translators:  The substitution text refers to data types.
                 * The message is displayed if a value in a particular context needs to
                 * be converted to type {1}, but that's not possible for a value of type
                 * {0}.
                */
                {BasisLibrary.DATA_CONVERSION_ERR,
                        "Invalid conversion from ''{0}'' to ''{1}''."},
                /**
                 * Note to translators:  This message is displayed if the function named
                 * by the substitution text is not a function that is supported.  XSLTC
                 * is the acronym naming the product.
                */
                {BasisLibrary.EXTERNAL_FUNC_ERR,
                        "External function ''{0}'' not supported by XSLTC."},
                /**
                 * Note to translators:  This message is displayed if two values are
                 * compared for equality, but the data type of one of the values is
                 * unknown.
                */
                {BasisLibrary.EQUALITY_EXPR_ERR,
                        "Unknown argument type in equality expression."},
                /**
                 * Note to translators:  The substitution text for {0} will be a data
                 * type; the substitution text for {1} will be the name of a function.
                 * This is displayed if an argument of the particular data type is not
                 * permitted for a call to this function.
                */
                {BasisLibrary.INVALID_ARGUMENT_ERR,
                        "Invalid argument type ''{0}'' in call to ''{1}''"},
                /**
                 * Note to translators:  There is way of specifying a format for a
                 * number using a pattern; the processor was unable to format the
                 * particular value using the specified pattern.
                */
                {BasisLibrary.FORMAT_NUMBER_ERR,
                        "Attempting to format number ''{0}'' using pattern ''{1}''."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor was unable to create a copy of an
                 * iterator.  (See definition of iterator above.)
                */
                {BasisLibrary.ITERATOR_CLONE_ERR,
                        "Cannot clone iterator ''{0}''."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.AXIS_SUPPORT_ERR,
                        "Iterator for axis ''{0}'' not supported."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
                        "Iterator for typed axis ''{0}'' not supported."},
                /**
                 * Note to translators:  This message is reported if the stylesheet
                 * being processed attempted to construct an XML document with an
                 * attribute in a place other than on an element.  The substitution text
                 * specifies the name of the attribute.
                */
                {BasisLibrary.STRAY_ATTRIBUTE_ERR,
                        "Attribute ''{0}'' outside of element."},
                /**
                 * Note to translators:  As with the preceding message, a namespace
                 * declaration has the form of an attribute and is only permitted to
                 * appear on an element.  The substitution text {0} is the namespace
                 * prefix and {1} is the URI that was being used in the erroneous
                 * namespace declaration.
                */
                {BasisLibrary.STRAY_NAMESPACE_ERR,
                        "Namespace declaration ''{0}''=''{1}'' outside of element."},
                /**
                 * Note to translators:  The stylesheet contained a reference to a
                 * namespace prefix that was undefined.  The value of the substitution
                 * text is the name of the prefix.
                */
                {BasisLibrary.NAMESPACE_PREFIX_ERR,
                        "Namespace for prefix ''{0}'' has not been declared."},
                /**
                 * Note to translators:  The following represents an internal error.
                 * DOMAdapter is a Java class in XSLTC.
                */
                {BasisLibrary.DOM_ADAPTER_INIT_ERR,
                        "DOMAdapter created using wrong type of source DOM."},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not describe to XSLTC the structure of the input XML document's
                 * DTD.
                */
                {BasisLibrary.PARSER_DTD_SUPPORT_ERR,
                        "The SAX parser you are using does not handle DTD declaration events."},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not distinguish between ordinary XML attributes and namespace
                 * declarations.
                */
                {BasisLibrary.NAMESPACES_SUPPORT_ERR,
                        "The SAX parser you are using does not have support for XML Namespaces."},
                /**
                 * Note to translators:  The substitution text is the URI that was in
                 * error.
                */
                {BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR,
                        "Could not resolve the URI reference ''{0}''."},
                /**
                 * Note to translators:  The stylesheet contained an element that was
                 * not recognized as part of the XSL syntax.  The substitution text
                 * gives the element name.
                */
                {BasisLibrary.UNSUPPORTED_XSL_ERR,
                        "Unsupported XSL element ''{0}''"},
                /**
                 * Note to translators:  The stylesheet referred to an extension to the
                 * XSL syntax and indicated that it was defined by XSLTC, but XSLTC does
                 * not recognize the particular extension named.  The substitution text
                 * gives the extension name.
                */
                {BasisLibrary.UNSUPPORTED_EXT_ERR,
                        "Unrecognized XSLTC extension ''{0}''"},
                /**
                 * Note to translators:  This error message is produced if the translet
                 * class was compiled using a newer version of XSLTC and deployed for
                 * execution with an older version of XSLTC.  The substitution text is
                 * the name of the translet class.
                */
                {BasisLibrary.UNKNOWN_TRANSLET_VERSION_ERR,
                        "The specified translet, ''{0}'', was created using a version of XSLTC more recent than the version of the XSLTC run-time that is in use.  You must recompile the stylesheet or use a more recent version of XSLTC to run this translet."},
                /**
                 * Note to translators:  An attribute whose effective value is required
                 * to be a "QName" had a value that was incorrect.
                 * 'QName' is an XML syntactic term that must not be translated.  The
                 * substitution text contains the actual value of the attribute.
                */
                {BasisLibrary.INVALID_QNAME_ERR,
                        "An attribute whose value must be a QName had the value ''{0}''"},
                /**
                 * Note to translators:  An attribute whose effective value is required
                 * to be a "NCName" had a value that was incorrect.
                 * 'NCName' is an XML syntactic term that must not be translated.  The
                 * substitution text contains the actual value of the attribute.
                */
                {BasisLibrary.INVALID_NCNAME_ERR,
                        "An attribute whose value must be an NCName had the value ''{0}''"},
                {BasisLibrary.UNALLOWED_EXTENSION_FUNCTION_ERR,
                        "Use of the extension function ''{0}'' is not allowed when the secure processing feature is set to true."},
                {BasisLibrary.UNALLOWED_EXTENSION_ELEMENT_ERR,
                        "Use of the extension element ''{0}'' is not allowed when the secure processing feature is set to true."},
        };
    }
}
