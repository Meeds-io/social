/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.deprecation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@Aspect
public class DeprecatedAPIAspect {
  private static final Log                  LOG            = ExoLogger.getLogger(DeprecatedAPIAspect.class);

  private static final boolean              IS_DEVELOPING  = PropertyManager.isDevelopping();

  private static final Map<String, Boolean> LOGGED_METHODS = new ConcurrentHashMap<>();

  @Around("execution(* *(..)) && @annotation(org.exoplatform.deprecation.DeprecatedAPI)")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    if (IS_DEVELOPING) {
      MethodSignature signature = (MethodSignature) point.getSignature();
      if (signature != null) {
        String className = signature.getDeclaringType().getName();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String fullMethodName = className + "#" + methodName;

        DeprecatedAPI annotation = method.getAnnotation(DeprecatedAPI.class);
        String message = annotation.value();
        boolean insist = annotation.insist();
        if (insist || !LOGGED_METHODS.containsKey(fullMethodName)) {
          LOGGED_METHODS.put(fullMethodName, true);
          LOG.warn("'{}#{}' has been deprecated. Please take time to cleanup code.",
                   className,
                   methodName,
                   new DeprecatedAPIException(message,
                                              signature.getDeclaringType(),
                                              methodName));
        }
      }
    }
    return point.proceed();
  }

}
