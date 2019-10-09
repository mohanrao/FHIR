/*
 * (C) Copyright IBM Corp. 2016,2017,2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;

import com.ibm.fhir.model.resource.SearchParameter;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.Duration;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.Quantity;
import com.ibm.fhir.persistence.exception.FHIRPersistenceProcessorException;

public abstract class AbstractProcessor<T> implements Processor<T> {
    
    @SuppressWarnings("unchecked")
    public T process(SearchParameter parameter, Object value) throws  FHIRPersistenceProcessorException {
        try {
            Class<?> valueType = value.getClass();
            if (Code.class.isAssignableFrom(valueType)) {
                // handle subclasses of Code just like Code
                valueType = Code.class;
            } else if (valueType == Duration.class) {
                valueType = Quantity.class;
            } else if (valueType == Extension.class) {
                value = getValue((Extension) value);
                valueType = value.getClass();
            } else if ((valueType.getDeclaredFields().length == 0) || 
                    (valueType.getDeclaredFields().length == 1 && valueType.getDeclaredFields()[0].isSynthetic())) {
                valueType = valueType.getSuperclass();
            }
            Method processMethod = this.getClass().getMethod("process", SearchParameter.class, valueType);
            return (T) processMethod.invoke(this, parameter, value);
        }
        catch(NoSuchMethodException | IllegalAccessException e) {
            StringBuilder sb = new StringBuilder("Unexpected error while processing parameter");
            if (parameter != null) {
                sb.append(' '); 
                sb.append(parameter.getCode().getValue());
            }
            throw new FHIRPersistenceProcessorException(sb.toString(), e);
        }
        catch(InvocationTargetException e) {
            Throwable targetException = e.getCause();
            if (targetException instanceof FHIRPersistenceProcessorException) {
                FHIRPersistenceProcessorException spe = (FHIRPersistenceProcessorException) targetException;
                throw spe;
            }
            else {
                StringBuilder sb = new StringBuilder("Unexpected error while processing parameter");
                if (parameter != null) {
                    sb.append(' ');
                    sb.append(parameter.getCode().getValue());
                }
                throw new FHIRPersistenceProcessorException(sb.toString(), e);
            }
        }
    }

    protected String getValue(Object wrapper) {
        try {
            Object literal = wrapper.getClass().getMethod("getValue").invoke(wrapper);
            return (String) literal.getClass().getMethod("value").invoke(literal);
        } catch (Exception e) {
        }
        return null;
    }

    protected Element getValue(Extension extension) {
        try {
            for (Method method : Extension.class.getDeclaredMethods()) {
                String methodName = method.getName();
                if (methodName.startsWith("getValue")) {
                    Object value = method.invoke(extension);
                    if (value != null) {
                        return (Element) value;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    protected java.util.Date convertToDate(String str) throws ParseException {
        TimestampUtil timestampUtil = TimestampUtil.create();
        return timestampUtil.getTimestamp(str); 
    }
}
