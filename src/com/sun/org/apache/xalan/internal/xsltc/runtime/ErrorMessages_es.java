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

public class ErrorMessages_es extends ListResourceBundle{
    // These message should be read from a locale-specific resource bundle
    public Object[][] getContents(){
        return new Object[][]{
                /**
                 * Note to translators:  the substitution text in the following message
                 * is a class name.  Used for internal errors in the processor.
                */
                {BasisLibrary.RUN_TIME_INTERNAL_ERR,
                        "Error interno de tiempo de ejecuci\u00F3n en ''{0}''"},
                /**
                 * Note to translators:  <xsl:copy> is a keyword that should not be
                 * translated.
                */
                {BasisLibrary.RUN_TIME_COPY_ERR,
                        "Error de tiempo de ejecuci\u00F3n al ejecutar <xsl:copy>."},
                /**
                 * Note to translators:  The substitution text refers to data types.
                 * The message is displayed if a value in a particular context needs to
                 * be converted to type {1}, but that's not possible for a value of type
                 * {0}.
                */
                {BasisLibrary.DATA_CONVERSION_ERR,
                        "Conversi\u00F3n no v\u00E1lida de ''{0}'' a ''{1}''."},
                /**
                 * Note to translators:  This message is displayed if the function named
                 * by the substitution text is not a function that is supported.  XSLTC
                 * is the acronym naming the product.
                */
                {BasisLibrary.EXTERNAL_FUNC_ERR,
                        "Funci\u00F3n externa ''{0}'' no soportada por XSLTC."},
                /**
                 * Note to translators:  This message is displayed if two values are
                 * compared for equality, but the data type of one of the values is
                 * unknown.
                */
                {BasisLibrary.EQUALITY_EXPR_ERR,
                        "Tipo de argumento desconocido en la expresi\u00F3n de igualdad."},
                /**
                 * Note to translators:  The substitution text for {0} will be a data
                 * type; the substitution text for {1} will be the name of a function.
                 * This is displayed if an argument of the particular data type is not
                 * permitted for a call to this function.
                */
                {BasisLibrary.INVALID_ARGUMENT_ERR,
                        "Tipo de argumento ''{0}'' no v\u00E1lido en la llamada a ''{1}''"},
                /**
                 * Note to translators:  There is way of specifying a format for a
                 * number using a pattern; the processor was unable to format the
                 * particular value using the specified pattern.
                */
                {BasisLibrary.FORMAT_NUMBER_ERR,
                        "Intentando formatear n\u00FAmero ''{0}'' mediante el patr\u00F3n ''{1}''."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor was unable to create a copy of an
                 * iterator.  (See definition of iterator above.)
                */
                {BasisLibrary.ITERATOR_CLONE_ERR,
                        "No se puede clonar el iterador ''{0}''."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.AXIS_SUPPORT_ERR,
                        "El iterador para el eje ''{0}'' no est\u00E1 soportado."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
                        "El iterador para el eje introducido ''{0}'' no est\u00E1 soportado."},
                /**
                 * Note to translators:  This message is reported if the stylesheet
                 * being processed attempted to construct an XML document with an
                 * attribute in a place other than on an element.  The substitution text
                 * specifies the name of the attribute.
                */
                {BasisLibrary.STRAY_ATTRIBUTE_ERR,
                        "El atributo ''{0}'' est\u00E1 fuera del elemento."},
                /**
                 * Note to translators:  As with the preceding message, a namespace
                 * declaration has the form of an attribute and is only permitted to
                 * appear on an element.  The substitution text {0} is the namespace
                 * prefix and {1} is the URI that was being used in the erroneous
                 * namespace declaration.
                */
                {BasisLibrary.STRAY_NAMESPACE_ERR,
                        "Declaraci\u00F3n del espacio de nombres ''{0}''=''{1}'' fuera del elemento."},
                /**
                 * Note to translators:  The stylesheet contained a reference to a
                 * namespace prefix that was undefined.  The value of the substitution
                 * text is the name of the prefix.
                */
                {BasisLibrary.NAMESPACE_PREFIX_ERR,
                        "No se ha declarado el espacio de nombres para el prefijo ''{0}''."},
                /**
                 * Note to translators:  The following represents an internal error.
                 * DOMAdapter is a Java class in XSLTC.
                */
                {BasisLibrary.DOM_ADAPTER_INIT_ERR,
                        "Se ha creado DOMAdapter mediante un tipo incorrecto de DOM de origen."},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not describe to XSLTC the structure of the input XML document's
                 * DTD.
                */
                {BasisLibrary.PARSER_DTD_SUPPORT_ERR,
                        "El analizador SAX que est\u00E1 utilizando no maneja los eventos de declaraci\u00F3n DTD."},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not distinguish between ordinary XML attributes and namespace
                 * declarations.
                */
                {BasisLibrary.NAMESPACES_SUPPORT_ERR,
                        "El analizador SAX que est\u00E1 utilizando no soporta los espacios de nombres XML."},
                /**
                 * Note to translators:  The substitution text is the URI that was in
                 * error.
                */
                {BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR,
                        "No se ha podido resolver la referencia al URI ''{0}''."},
                /**
                 * Note to translators:  The stylesheet contained an element that was
                 * not recognized as part of the XSL syntax.  The substitution text
                 * gives the element name.
                */
                {BasisLibrary.UNSUPPORTED_XSL_ERR,
                        "Elemento ''{0}'' de XSL no soportado"},
                /**
                 * Note to translators:  The stylesheet referred to an extension to the
                 * XSL syntax and indicated that it was defined by XSLTC, but XSLTC does
                 * not recognize the particular extension named.  The substitution text
                 * gives the extension name.
                */
                {BasisLibrary.UNSUPPORTED_EXT_ERR,
                        "Extensi\u00F3n ''{0}'' de XSLTC no reconocida"},
                /**
                 * Note to translators:  This error message is produced if the translet
                 * class was compiled using a newer version of XSLTC and deployed for
                 * execution with an older version of XSLTC.  The substitution text is
                 * the name of the translet class.
                */
                {BasisLibrary.UNKNOWN_TRANSLET_VERSION_ERR,
                        "El translet especificado, ''{0}'' se ha creado con una versi\u00F3n de XSLTC m\u00E1s reciente que la versi\u00F3n del tiempo de ejecuci\u00F3n de XSLTC que se est\u00E1 utilizando. Debe volver a compilar la hoja de estilo o utilizar una versi\u00F3n m\u00E1s reciente de XSLTC para ejecutar este translet."},
                /**
                 * Note to translators:  An attribute whose effective value is required
                 * to be a "QName" had a value that was incorrect.
                 * 'QName' is an XML syntactic term that must not be translated.  The
                 * substitution text contains the actual value of the attribute.
                */
                {BasisLibrary.INVALID_QNAME_ERR,
                        "Un atributo cuyo valor debe ser un QName ten\u00EDa el valor ''{0}''"},
                /**
                 * Note to translators:  An attribute whose effective value is required
                 * to be a "NCName" had a value that was incorrect.
                 * 'NCName' is an XML syntactic term that must not be translated.  The
                 * substitution text contains the actual value of the attribute.
                */
                {BasisLibrary.INVALID_NCNAME_ERR,
                        "Un atributo cuyo valor debe ser un NCName ten\u00EDa el valor ''{0}''"},
                {BasisLibrary.UNALLOWED_EXTENSION_FUNCTION_ERR,
                        "El uso de la funci\u00F3n de extensi\u00F3n ''{0}'' no est\u00E1 permitido cuando la funci\u00F3n de procesamiento seguro se ha definido en true."},
                {BasisLibrary.UNALLOWED_EXTENSION_ELEMENT_ERR,
                        "El uso del elemento de extensi\u00F3n ''{0}'' no est\u00E1 permitido cuando la funci\u00F3n de procesamiento seguro se ha definido en true."},
        };
    }
}
