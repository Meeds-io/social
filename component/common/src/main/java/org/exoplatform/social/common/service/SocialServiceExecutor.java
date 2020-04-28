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
package org.exoplatform.social.common.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface SocialServiceExecutor {

  ProcessContext execute(ServiceContext<ProcessContext> serviceContext,
                           ProcessContext processContext);
  
  ProcessContext execute(Processor processor,
                       ProcessContext processContext);

  void setExecutorService(ExecutorService executorService);

  Future<ProcessContext> asyncProcess(AsyncProcessor asyncProcessor,
                                        ProcessContext processContext);
  
  ProcessContext async(AsyncProcessor asyncProcessor,
                                      ProcessContext processContext);
  
  ProcessContext async(AsyncProcessor asyncProcessor,
                       ProcessContext processContext, AsyncCallback callback);
}
