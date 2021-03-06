/*
 * Copyright (c) 2003-2007 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */
package freemarker.ext.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import freemarker.template.utility.UndeclaredThrowableException;

class MethodUtilities
{
    // Get rid of these on Java 5
    private static final Method METHOD_IS_VARARGS = getIsVarArgsMethod(Method.class);
    private static final Method CONSTRUCTOR_IS_VARARGS = getIsVarArgsMethod(Constructor.class);
    
    /**
     * Determines whether a type represented by a class object is 
     * convertible to another type represented by a class object using a 
     * method invocation conversion, without matching object and primitive
     * types. This method is used to determine the more specific type when
     * comparing signatures of methods.
     * @return true if either formal type is assignable from actual type, 
     * or formal and actual are both primitive types and actual can be
     * subject to widening conversion to formal.
     */
    static boolean isMoreSpecificOrTheSame(Class specific, Class generic, boolean primitiveIsConvertibleToClass) {
        // Check for identity or widening reference conversion
        if(generic.isAssignableFrom(specific)) {
            return true;
        }
        // Check for widening primitive conversion.
        if(generic.isPrimitive()) {
            if(generic == Short.TYPE && (specific == Byte.TYPE)) {
                return true;
            }
            if(generic == Integer.TYPE && 
               (specific == Short.TYPE || specific == Byte.TYPE)) {
                return true;
            }
            if(generic == Long.TYPE && 
               (specific == Integer.TYPE || specific == Short.TYPE || 
                specific == Byte.TYPE)) {
                return true;
            }
            if(generic == Float.TYPE && 
               (specific == Long.TYPE || specific == Integer.TYPE || 
                specific == Short.TYPE || specific == Byte.TYPE)) {
                return true;
            }
            if(generic == Double.TYPE && 
               (specific == Float.TYPE || specific == Long.TYPE || 
                specific == Integer.TYPE || specific == Short.TYPE || 
                specific == Byte.TYPE)) {
                return true; 
            }
            // FIXME: What's with (boolean, Boolean) and (int, Long) and such?
        }
        return false;
    }
    
    /**
     * Attention, this doesn't handle primitive classes correctly, nor numerical conversions.
     */
    static Set getAssignables(Class c1, Class c2) {
        Set s = new HashSet();
        collectAssignables(c1, c2, s);
        return s;
    }
    
    private static void collectAssignables(Class c1, Class c2, Set s) {
        if(c1.isAssignableFrom(c2)) {
            s.add(c1);
        }
        Class sc = c1.getSuperclass();
        if(sc != null) {
            collectAssignables(sc, c2, s);
        }
        Class[] itf = c1.getInterfaces();
        for(int i = 0; i < itf.length; ++i) {
            collectAssignables(itf[i], c2, s);
        }
    }

    static Class[] getParameterTypes(Member member) {
        if(member instanceof Method) {
            return ((Method)member).getParameterTypes();
        }
        if(member instanceof Constructor) {
            return ((Constructor)member).getParameterTypes();
        }
        throw new RuntimeException();
    }

    static boolean isVarArgs(Member member) {
        if(member instanceof Method) { 
            return isVarArgs(member, METHOD_IS_VARARGS);
        }
        if(member instanceof Constructor) {
            return isVarArgs(member, CONSTRUCTOR_IS_VARARGS);
        }
        throw new RuntimeException();
    }
    
    private static boolean isVarArgs(Member member, Method isVarArgsMethod) {
        if(isVarArgsMethod == null) {
            return false;
        }
        try {
            return ((Boolean)isVarArgsMethod.invoke(member, (Object[]) null)).booleanValue();
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch(Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private static Method getIsVarArgsMethod(Class memberClass) {
        try {
            return memberClass.getMethod("isVarArgs", (Class[]) null);
        }
        catch(NoSuchMethodException e) {
            return null; // pre 1.5 JRE
        }
    }
}