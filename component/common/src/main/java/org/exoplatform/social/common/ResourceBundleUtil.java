/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.common;

import java.text.MessageFormat;
import java.util.List;

/**
 * ResourceBundleUtil
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Aug 31, 2010
 */
public class ResourceBundleUtil {

  private static final ThreadLocal<MessageFormat> messageFormatRef = new ThreadLocal<MessageFormat>() {
    @Override
    protected MessageFormat initialValue() {
      return new MessageFormat("");
    }
  };

  private static final String[] EMPTY_MESSAGE_ARGUMENTS = new String[]{};

  /**
   * Replace convention arguments of pattern {index} with messageArguments[index].
   * @param message
   * @param messageArguments
   * @return expected message with replaced arguments
   */
  public static String replaceArguments(String message, String[] messageArguments) {
    if (messageArguments == null) {
      messageArguments = EMPTY_MESSAGE_ARGUMENTS;
    }
    final MessageFormat messageFormat = messageFormatRef.get();
    //pre-process single quote
    message = processSingleQuote(message);
    
    //
    messageFormat.applyPattern(message);
    return messageFormat.format(messageArguments);
  }
  
  /**
   * Replace convention arguments of pattern {index} with messageArguments[index].
   * @param message
   * @param messageArguments
   * @return expected message with replaced arguments
   * @since 1.2.2
   */
  public static String replaceArguments(String message, List<String> messageArguments) {
    return replaceArguments(message, messageArguments.toArray(new String[0]));
  }
  /**
   * With single quote in resource bundle, MessageFormat will be escape single quote
   * then it needs to replace single quote by double single quotes
   * 
   * Sample:
   *      {@literal I'm connected with {0} => I''m connected with {0}}
   * @param message given message replacement
   * @return add double single quotes.
   */
  public static String processSingleQuote(String message) {
    String temp = message;
    while(true) {
      if (temp.indexOf("''") < 0) {
        break;
      }
      
      temp = temp.replace("''", "'");
    }
    
    return temp.replaceAll("'", "''");
  }

}
