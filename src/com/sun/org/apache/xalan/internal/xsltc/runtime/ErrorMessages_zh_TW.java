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

public class ErrorMessages_zh_TW extends ListResourceBundle{
    // These message should be read from a locale-specific resource bundle
    public Object[][] getContents(){
        return new Object[][]{
                /**
                 * Note to translators:  the substitution text in the following message
                 * is a class name.  Used for internal errors in the processor.
                */
                {BasisLibrary.RUN_TIME_INTERNAL_ERR,
                        "''{0}'' \u4E2D\u7684\u57F7\u884C\u968E\u6BB5\u5167\u90E8\u932F\u8AA4"},
                /**
                 * Note to translators:  <xsl:copy> is a keyword that should not be
                 * translated.
                */
                {BasisLibrary.RUN_TIME_COPY_ERR,
                        "\u57F7\u884C <xsl:copy> \u6642\u767C\u751F\u57F7\u884C\u968E\u6BB5\u932F\u8AA4"},
                /**
                 * Note to translators:  The substitution text refers to data types.
                 * The message is displayed if a value in a particular context needs to
                 * be converted to type {1}, but that's not possible for a value of type
                 * {0}.
                */
                {BasisLibrary.DATA_CONVERSION_ERR,
                        "\u5F9E ''{0}'' \u81F3 ''{1}'' \u7684\u8F49\u63DB\u7121\u6548\u3002"},
                /**
                 * Note to translators:  This message is displayed if the function named
                 * by the substitution text is not a function that is supported.  XSLTC
                 * is the acronym naming the product.
                */
                {BasisLibrary.EXTERNAL_FUNC_ERR,
                        "XSLTC \u4E0D\u652F\u63F4\u5916\u90E8\u51FD\u6578 ''{0}''\u3002"},
                /**
                 * Note to translators:  This message is displayed if two values are
                 * compared for equality, but the data type of one of the values is
                 * unknown.
                */
                {BasisLibrary.EQUALITY_EXPR_ERR,
                        "\u76F8\u7B49\u6027\u8868\u793A\u5F0F\u4E2D\u7684\u5F15\u6578\u985E\u578B\u4E0D\u660E\u3002"},
                /**
                 * Note to translators:  The substitution text for {0} will be a data
                 * type; the substitution text for {1} will be the name of a function.
                 * This is displayed if an argument of the particular data type is not
                 * permitted for a call to this function.
                */
                {BasisLibrary.INVALID_ARGUMENT_ERR,
                        "\u547C\u53EB ''{1}'' \u4E2D\u7684\u5F15\u6578\u985E\u578B ''{0}'' \u7121\u6548"},
                /**
                 * Note to translators:  There is way of specifying a format for a
                 * number using a pattern; the processor was unable to format the
                 * particular value using the specified pattern.
                */
                {BasisLibrary.FORMAT_NUMBER_ERR,
                        "\u5617\u8A66\u4F7F\u7528\u6A23\u5F0F ''{1}'' \u683C\u5F0F\u5316\u6578\u5B57 ''{0}''\u3002"},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor was unable to create a copy of an
                 * iterator.  (See definition of iterator above.)
                */
                {BasisLibrary.ITERATOR_CLONE_ERR,
                        "\u7121\u6CD5\u8907\u88FD\u91CD\u8907\u7A0B\u5F0F ''{0}''\u3002"},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.AXIS_SUPPORT_ERR,
                        "\u4E0D\u652F\u63F4\u8EF8 ''{0}'' \u7684\u91CD\u8907\u7A0B\u5F0F\u3002"},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
                        "\u4E0D\u652F\u63F4\u985E\u578B\u8EF8 ''{0}'' \u7684\u91CD\u8907\u7A0B\u5F0F\u3002"},
                /**
                 * Note to translators:  This message is reported if the stylesheet
                 * being processed attempted to construct an XML document with an
                 * attribute in a place other than on an element.  The substitution text
                 * specifies the name of the attribute.
                */
                {BasisLibrary.STRAY_ATTRIBUTE_ERR,
                        "\u5C6C\u6027 ''{0}'' \u5728\u5143\u7D20\u4E4B\u5916\u3002"},
                /**
                 * Note to translators:  As with the preceding message, a namespace
                 * declaration has the form of an attribute and is only permitted to
                 * appear on an element.  The substitution text {0} is the namespace
                 * prefix and {1} is the URI that was being used in the erroneous
                 * namespace declaration.
                */
                {BasisLibrary.STRAY_NAMESPACE_ERR,
                        "\u547D\u540D\u7A7A\u9593\u5BA3\u544A ''{0}''=''{1}'' \u8D85\u51FA\u5143\u7D20\u5916\u3002"},
                /**
                 * Note to translators:  The stylesheet contained a reference to a
                 * namespace prefix that was undefined.  The value of the substitution
                 * text is the name of the prefix.
                */
                {BasisLibrary.NAMESPACE_PREFIX_ERR,
                        "\u5B57\u9996 ''{0}'' \u7684\u547D\u540D\u7A7A\u9593\u5C1A\u672A\u5BA3\u544A\u3002"},
                /**
                 * Note to translators:  The following represents an internal error.
                 * DOMAdapter is a Java class in XSLTC.
                */
                {BasisLibrary.DOM_ADAPTER_INIT_ERR,
                        "\u4F7F\u7528\u932F\u8AA4\u7684\u4F86\u6E90 DOM \u985E\u578B\u5EFA\u7ACB DOMAdapter\u3002"},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not describe to XSLTC the structure of the input XML document's
                 * DTD.
                */
                {BasisLibrary.PARSER_DTD_SUPPORT_ERR,
                        "\u60A8\u6B63\u5728\u4F7F\u7528\u7684 SAX \u5256\u6790\u5668\u4E0D\u6703\u8655\u7406 DTD \u5BA3\u544A\u4E8B\u4EF6\u3002"},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not distinguish between ordinary XML attributes and namespace
                 * declarations.
                */
                {BasisLibrary.NAMESPACES_SUPPORT_ERR,
                        "\u60A8\u6B63\u5728\u4F7F\u7528\u7684 SAX \u5256\u6790\u5668\u4E0D\u652F\u63F4 XML \u547D\u540D\u7A7A\u9593\u3002"},
                /**
                 * Note to translators:  The substitution text is the URI that was in
                 * error.
                */
                {BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR,
                        "\u7121\u6CD5\u89E3\u6790 URI \u53C3\u7167 ''{0}''\u3002"},
                /**
                 * Note to translators:  The stylesheet contained an element that was
                 * not recognized as part of the XSL syntax.  The substitution text
                 * gives the element name.
                */
                {BasisLibrary.UNSUPPORTED_XSL_ERR,
                        "\u4E0D\u652F\u63F4\u7684 XSL \u5143\u7D20 ''{0}''"},
                /**
                 * Note to translators:  The stylesheet referred to an extension to the
                 * XSL syntax and indicated that it was defined by XSLTC, but XSLTC does
                 * not recognize the particular extension named.  The substitution text
                 * gives the extension name.
                */
                {BasisLibrary.UNSUPPORTED_EXT_ERR,
                        "\u7121\u6CD5\u8FA8\u8B58\u7684 XSLTC \u64F4\u5145\u5957\u4EF6 ''{0}''"},
                /**
                 * Note to translators:  This error message is produced if the translet
                 * class was compiled using a newer version of XSLTC and deployed for
                 * execution with an older version of XSLTC.  The substitution text is
                 * the name of the translet class.
                */
                {BasisLibrary.UNKNOWN_TRANSLET_VERSION_ERR,
                        "\u5EFA\u7ACB\u6307\u5B9A translet ''{0}'' \u7684 XSLTC \u7248\u672C\u6BD4\u4F7F\u7528\u4E2D XSLTC \u57F7\u884C\u968E\u6BB5\u7684\u7248\u672C\u8F03\u65B0\u3002\u60A8\u5FC5\u9808\u91CD\u65B0\u7DE8\u8B6F\u6A23\u5F0F\u8868\uFF0C\u6216\u4F7F\u7528\u8F03\u65B0\u7684 XSLTC \u7248\u672C\u4F86\u57F7\u884C\u6B64 translet\u3002"},
                /**
                 * Note to translators:  An attribute whose effective value is required
                 * to be a "QName" had a value that was incorrect.
                 * 'QName' is an XML syntactic term that must not be translated.  The
                 * substitution text contains the actual value of the attribute.
                */
                {BasisLibrary.INVALID_QNAME_ERR,
                        "\u503C\u5FC5\u9808\u70BA QName \u7684\u5C6C\u6027\uFF0C\u5177\u6709\u503C ''{0}''"},
                /**
                 * Note to translators:  An attribute whose effective value is required
                 * to be a "NCName" had a value that was incorrect.
                 * 'NCName' is an XML syntactic term that must not be translated.  The
                 * substitution text contains the actual value of the attribute.
                */
                {BasisLibrary.INVALID_NCNAME_ERR,
                        "\u503C\u5FC5\u9808\u70BA NCName \u7684\u5C6C\u6027\uFF0C\u5177\u6709\u503C ''{0}''"},
                {BasisLibrary.UNALLOWED_EXTENSION_FUNCTION_ERR,
                        "\u7576\u5B89\u5168\u8655\u7406\u529F\u80FD\u8A2D\u70BA\u771F\u6642\uFF0C\u4E0D\u5141\u8A31\u4F7F\u7528\u64F4\u5145\u5957\u4EF6\u51FD\u6578 ''{0}''\u3002"},
                {BasisLibrary.UNALLOWED_EXTENSION_ELEMENT_ERR,
                        "\u7576\u5B89\u5168\u8655\u7406\u529F\u80FD\u8A2D\u70BA\u771F\u6642\uFF0C\u4E0D\u5141\u8A31\u4F7F\u7528\u64F4\u5145\u5957\u4EF6\u5143\u7D20 ''{0}''\u3002"},
        };
    }
}
