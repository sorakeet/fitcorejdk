/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: XNumber.java,v 1.2.4.2 2005/09/14 20:34:46 jeffsuttor Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
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
/**
 * $Id: XNumber.java,v 1.2.4.2 2005/09/14 20:34:46 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;

public class XNumber extends XObject{
    static final long serialVersionUID=-2720400709619020193L;
    double m_val;

    public XNumber(double d){
        super();
        m_val=d;
    }

    public XNumber(Number num){
        super();
        m_val=num.doubleValue();
        setObject(num);
    }

    public int getType(){
        return CLASS_NUMBER;
    }

    public String getTypeString(){
        return "#NUMBER";
    }

    public double num(){
        return m_val;
    }

    public boolean bool(){
        return (Double.isNaN(m_val)||(m_val==0.0))?false:true;
    }

    public String str(){
        if(Double.isNaN(m_val)){
            return "NaN";
        }else if(Double.isInfinite(m_val)){
            if(m_val>0)
                return "Infinity";
            else
                return "-Infinity";
        }
        double num=m_val;
        String s=Double.toString(num);
        int len=s.length();
        if(s.charAt(len-2)=='.'&&s.charAt(len-1)=='0'){
            s=s.substring(0,len-2);
            if(s.equals("-0"))
                return "0";
            return s;
        }
        int e=s.indexOf('E');
        if(e<0){
            if(s.charAt(len-1)=='0')
                return s.substring(0,len-1);
            else
                return s;
        }
        int exp=Integer.parseInt(s.substring(e+1));
        String sign;
        if(s.charAt(0)=='-'){
            sign="-";
            s=s.substring(1);
            --e;
        }else
            sign="";
        int nDigits=e-2;
        if(exp>=nDigits)
            return sign+s.substring(0,1)+s.substring(2,e)
                    +zeros(exp-nDigits);
        // Eliminate trailing 0's - bugzilla 14241
        while(s.charAt(e-1)=='0')
            e--;
        if(exp>0)
            return sign+s.substring(0,1)+s.substring(2,2+exp)+"."
                    +s.substring(2+exp,e);
        return sign+"0."+zeros(-1-exp)+s.substring(0,1)
                +s.substring(2,e);
    }
//  /**
//   * Cast result object to a string.
//   *
//   * @return "NaN" if the number is NaN, Infinity or -Infinity if
//   * the number is infinite or the string value of the number.
//   */
//  private static final int PRECISION = 16;
//  public String str()
//  {
//
//    if (Double.isNaN(m_val))
//    {
//      return "NaN";
//    }
//    else if (Double.isInfinite(m_val))
//    {
//      if (m_val > 0)
//        return "Infinity";
//      else
//        return "-Infinity";
//    }
//
//    long longVal = (long)m_val;
//    if ((double)longVal == m_val)
//      return Long.toString(longVal);
//
//
//    String s = Double.toString(m_val);
//    int len = s.length();
//
//    if (s.charAt(len - 2) == '.' && s.charAt(len - 1) == '0')
//    {
//      return s.substring(0, len - 2);
//    }
//
//    int exp = 0;
//    int e = s.indexOf('E');
//    if (e != -1)
//    {
//      exp = Integer.parseInt(s.substring(e + 1));
//      s = s.substring(0,e);
//      len = e;
//    }
//
//    // Calculate Significant Digits:
//    // look from start of string for first digit
//    // look from end for last digit
//    // significant digits = end - start + (0 or 1 depending on decimal location)
//
//    int decimalPos = -1;
//    int start = (s.charAt(0) == '-') ? 1 : 0;
//    findStart: for( ; start < len; start++ )
//    {
//      switch (s.charAt(start))
//      {
//      case '0':
//        break;
//      case '.':
//        decimalPos = start;
//        break;
//      default:
//        break findStart;
//      }
//    }
//    int end = s.length() - 1;
//    findEnd: for( ; end > start; end-- )
//    {
//      switch (s.charAt(end))
//      {
//      case '0':
//        break;
//      case '.':
//        decimalPos = end;
//        break;
//      default:
//        break findEnd;
//      }
//    }
//
//    int sigDig = end - start;
//
//    // clarify decimal location if it has not yet been found
//    if (decimalPos == -1)
//      decimalPos = s.indexOf('.');
//
//    // if decimal is not between start and end, add one to sigDig
//    if (decimalPos < start || decimalPos > end)
//      ++sigDig;
//
//    // reduce significant digits to PRECISION if necessary
//    if (sigDig > PRECISION)
//    {
//      // re-scale BigDecimal in order to get significant digits = PRECISION
//      BigDecimal num = new BigDecimal(s);
//      int newScale = num.scale() - (sigDig - PRECISION);
//      if (newScale < 0)
//        newScale = 0;
//      s = num.setScale(newScale, BigDecimal.ROUND_HALF_UP).toString();
//
//      // remove trailing '0's; keep track of decimalPos
//      int truncatePoint = s.length();
//      while (s.charAt(--truncatePoint) == '0')
//        ;
//
//      if (s.charAt(truncatePoint) == '.')
//      {
//        decimalPos = truncatePoint;
//      }
//      else
//      {
//        decimalPos = s.indexOf('.');
//        truncatePoint += 1;
//      }
//
//      s = s.substring(0, truncatePoint);
//      len = s.length();
//    }
//
//    // Account for exponent by adding zeros as needed
//    // and moving the decimal place
//
//    if (exp == 0)
//       return s;
//
//    start = 0;
//    String sign;
//    if (s.charAt(0) == '-')
//    {
//      sign = "-";
//      start++;
//    }
//    else
//      sign = "";
//
//    String wholePart = s.substring(start, decimalPos);
//    String decimalPart = s.substring(decimalPos + 1);
//
//    // get the number of digits right of the decimal
//    int decimalLen = decimalPart.length();
//
//    if (exp >= decimalLen)
//      return sign + wholePart + decimalPart + zeros(exp - decimalLen);
//
//    if (exp > 0)
//      return sign + wholePart + decimalPart.substring(0, exp) + "."
//             + decimalPart.substring(exp);
//
//    return sign + "0." + zeros(-1 - exp) + wholePart + decimalPart;
//  }

    static private String zeros(int n){
        if(n<1)
            return "";
        char[] buf=new char[n];
        for(int i=0;i<n;i++){
            buf[i]='0';
        }
        return new String(buf);
    }

    public Object object(){
        if(null==m_obj)
            setObject(new Double(m_val));
        return m_obj;
    }

    public boolean equals(XObject obj2){
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.
        int t=obj2.getType();
        try{
            if(t==XObject.CLASS_NODESET)
                return obj2.equals(this);
            else if(t==XObject.CLASS_BOOLEAN)
                return obj2.bool()==bool();
            else
                return m_val==obj2.num();
        }catch(javax.xml.transform.TransformerException te){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(te);
        }
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        visitor.visitNumberLiteral(owner,this);
    }

    public double num(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        return m_val;
    }

    public boolean isStableNumber(){
        return true;
    }
}
