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

public class ErrorMessages_pt_BR extends ListResourceBundle{
    // These message should be read from a locale-specific resource bundle
    public Object[][] getContents(){
        return new Object[][]{
                /**
                 * Note to translators:  the substitution text in the following message
                 * is a class name.  Used for internal errors in the processor.
                */
                {BasisLibrary.RUN_TIME_INTERNAL_ERR,
                        "Erro interno de runtime em ''{0}''"},
                /**
                 * Note to translators:  <xsl:copy> is a keyword that should not be
                 * translated.
                */
                {BasisLibrary.RUN_TIME_COPY_ERR,
                        "Erro de runtime ao executar <xsl:copy>."},
                /**
                 * Note to translators:  The substitution text refers to data types.
                 * The message is displayed if a value in a particular context needs to
                 * be converted to type {1}, but that's not possible for a value of type
                 * {0}.
                */
                {BasisLibrary.DATA_CONVERSION_ERR,
                        "Convers\u00E3o inv\u00E1lida de ''{0}'' para ''{1}''."},
                /**
                 * Note to translators:  This message is displayed if the function named
                 * by the substitution text is not a function that is supported.  XSLTC
                 * is the acronym naming the product.
                */
                {BasisLibrary.EXTERNAL_FUNC_ERR,
                        "Fun\u00E7\u00E3o externa ''{0}'' n\u00E3o suportada por XSLTC."},
                /**
                 * Note to translators:  This message is displayed if two values are
                 * compared for equality, but the data type of one of the values is
                 * unknown.
                */
                {BasisLibrary.EQUALITY_EXPR_ERR,
                        "Tipo de argumento desconhecido na express\u00E3o de igualdade."},
                /**
                 * Note to translators:  The substitution text for {0} will be a data
                 * type; the substitution text for {1} will be the name of a function.
                 * This is displayed if an argument of the particular data type is not
                 * permitted for a call to this function.
                */
                {BasisLibrary.INVALID_ARGUMENT_ERR,
                        "Tipo de argumento inv\u00E1lido ''{0}'' na chamada para ''{1}''"},
                /**
                 * Note to translators:  There is way of specifying a format for a
                 * number using a pattern; the processor was unable to format the
                 * particular value using the specified pattern.
                */
                {BasisLibrary.FORMAT_NUMBER_ERR,
                        "Tentativa de formatar o n\u00FAmero ''{0}'' usando o padr\u00E3o ''{1}''."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor was unable to create a copy of an
                 * iterator.  (See definition of iterator above.)
                */
                {BasisLibrary.ITERATOR_CLONE_ERR,
                        "N\u00E3o \u00E9 poss\u00EDvel clonar o iterador ''{0}''."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.AXIS_SUPPORT_ERR,
                        "Iterador do eixo ''{0}'' n\u00E3o suportado."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
                        "Iterador do eixo digitado ''{0}'' n\u00E3o suportado."},
                /**
                 * Note to translators:  This message is reported if the stylesheet
                 * being processed attempted to construct an XML document with an
                 * attribute in a place other than on an element.  The substitution text
                 * specifies the name of the attribute.
                */
                {BasisLibrary.STRAY_ATTRIBUTE_ERR,
                        "Atributo ''{0}'' fora do elemento."},
                /**
                 * Note to translators:  As with the preceding message, a namespace
                 * declaration has the form of an attribute and is only permitted to
                 * appear on an element.  The substitution text {0} is the namespace
                 * prefix and {1} is the URI that was being used in the erroneous
                 * namespace declaration.
                */
                {BasisLibrary.STRAY_NAMESPACE_ERR,
                        "Declara\u00E7\u00E3o de namespace ''{0}''=''{1}'' fora do elemento."},
                /**
                 * Note to translators:  The stylesheet contained a reference to a
                 * namespace prefix that was undefined.  The value of the substitution
                 * text is the name of the prefix.
                */
                {BasisLibrary.NAMESPACE_PREFIX_ERR,
                        "O namespace do prefixo ''{0}'' n\u00E3o foi declarado."},
                /**
                 * Note to translators:  The following represents an internal error.
                 * DOMAdapter is a Java class in XSLTC.
                */
                {BasisLibrary.DOM_ADAPTER_INIT_ERR,
                        "DOMAdapter criado usando o tipo incorreto de DOM de origem."},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not describe to XSLTC the structure of the input XML document's
                 * DTD.
                */
                {BasisLibrary.PARSER_DTD_SUPPORT_ERR,
                        "O parser SAX que voc\u00EA est\u00E1 usando n\u00E3o trata eventos de declara\u00E7\u00E3o de DTD."},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not distinguish between ordinary XML attributes and namespace
                 * declarations.
                */
                {BasisLibrary.NAMESPACES_SUPPORT_ERR,
                        "O parser SAX que voc\u00EA est\u00E1 usando n\u00E3o tem suporte para os Namespaces de XML."},
                /**
                 * Note to translators:  The substitution text is the URI that was in
                 * error.
                */
                {BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR,
                        "N\u00E3o foi poss\u00EDvel resolver a refer\u00EAncia do URI ''{0}''."},
                /**
                 * Note to translators:  The stylesheet contained an element that was
                 * not recognized as part of the XSL syntax.  The substitution text
                 * gives the element name.
                */
                {BasisLibrary.UNSUPPORTED_XSL_ERR,
                        "Elemento XSL ''{0}'' n\u00E3o suportado"},
                /**
                 * Note to translators:  The stylesheet referred to an extension to the
                 * XSL syntax and indicated that it was defined by XSLTC, but XSLTC does
                 * not recognize the particular extension named.  The substitution text
                 * gives the extension name.
                */
                {BasisLibrary.UNSUPPORTED_EXT_ERR,
                        "Extens\u00E3o ''{0}'' de XSLTC n\u00E3o reconhecida"},
                /**
                 * Note to translators:  This error message is produced if the translet
                 * class was compiled using a newer version of XSLTC and deployed for
                 * execution with an older version of XSLTC.  The substitution text is
                 * the name of the translet class.
                */
                {BasisLibrary.UNKNOWN_TRANSLET_VERSION_ERR,
                        "O translet especificado, ''{0}'', foi criado usando uma vers\u00E3o do XSLTC mais recente que a vers\u00E3o de runtime de XSLTC em uso. Recompile a folha de estilos ou use uma vers\u00E3o mais recente de XSLTC para executar este translet."},
                /**
                 * Note to translators:  An attribute whose effective value is required
                 * to be a "QName" had a value that was incorrect.
                 * 'QName' is an XML syntactic term that must not be translated.  The
                 * substitution text contains the actual value of the attribute.
                */
                {BasisLibrary.INVALID_QNAME_ERR,
                        "Um atributo cujo valor deve ser um QName tinha o valor ''{0}''"},
                /**
                 * Note to translators:  An attribute whose effective value is required
                 * to be a "NCName" had a value that was incorrect.
                 * 'NCName' is an XML syntactic term that must not be translated.  The
                 * substitution text contains the actual value of the attribute.
                */
                {BasisLibrary.INVALID_NCNAME_ERR,
                        "Um atributo cujo valor deve ser um NCName tinha o valor ''{0}''"},
                {BasisLibrary.UNALLOWED_EXTENSION_FUNCTION_ERR,
                        "O uso da fun\u00E7\u00E3o da extens\u00E3o ''{0}'' n\u00E3o ser\u00E1 permitido quando o recurso de processamento seguro for definido como verdadeiro."},
                {BasisLibrary.UNALLOWED_EXTENSION_ELEMENT_ERR,
                        "O uso do elemento da extens\u00E3o ''{0}'' n\u00E3o ser\u00E1 permitido quando o recurso de processamento seguro for definido como verdadeiro."},
        };
    }
}
