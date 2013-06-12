/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 10, 2013
 */
public interface SocialServiceExecutor {

  SocialServiceContext getSocialServiceContext();

  ProcessContext execute(ServiceContext<ProcessContext> serviceContext,
                           ProcessContext processContext);

  void setExecutorService(ExecutorService executorService);

  Future<ProcessContext> asyncExecute(AsyncProcessor asyncProcessor,
                                        ProcessContext processContext);
}
