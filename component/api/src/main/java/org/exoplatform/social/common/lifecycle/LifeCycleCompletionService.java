/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.common.lifecycle;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.picocontainer.Startable;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import lombok.SneakyThrows;

/**
 * Process the callable request out of the http request.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@SuppressWarnings("rawtypes")
public class LifeCycleCompletionService implements Startable {

  private static final String       THREAD_NUMBER_KEY       = "thread-number";

  private static final String       ASYNC_EXECUTION_KEY     = "async-execution";

  private static final int          DEFAULT_THREAD_NUMBER   = 1;

  private static final boolean      DEFAULT_ASYNC_EXECUTION = true;

  private ExecutorService           executor;

  private ExecutorCompletionService ecs;

  private int                       configThreadNumber;

  private boolean                   configAsyncExecution;

  public LifeCycleCompletionService(InitParams params) {
    ValueParam threadNumber = params == null ? null : params.getValueParam(THREAD_NUMBER_KEY);
    ValueParam asyncExecution = params == null ? null : params.getValueParam(ASYNC_EXECUTION_KEY);

    this.configThreadNumber = threadNumber == null ? DEFAULT_THREAD_NUMBER : Integer.valueOf(threadNumber.getValue());
    this.configAsyncExecution = asyncExecution == null ? DEFAULT_ASYNC_EXECUTION : Boolean.valueOf(asyncExecution.getValue());
    this.executor = Executors.newFixedThreadPool(this.configThreadNumber);
    this.ecs = new ExecutorCompletionService(executor);
  }

  @Override
  public void stop() {
    executor.shutdown();
  }

  @SuppressWarnings("unchecked")
  public void addTask(Callable callable) {
    ecs.submit(callable);
  }

  @SneakyThrows
  public void waitCompletionFinished() {
    executor.awaitTermination(1, TimeUnit.SECONDS);
  }

  public boolean isAsync() {
    return this.configAsyncExecution;
  }

  public long getActiveTaskCount() {
    if (executor instanceof ThreadPoolExecutor threadPoolExecutor) {
      return threadPoolExecutor.getActiveCount();
    } else {
      return 0;
    }
  }

  @SneakyThrows
  public void waitAllTaskFinished(long timeout) {
    long start = System.currentTimeMillis();
    while (getActiveTaskCount() != 0 && System.currentTimeMillis() - start <= timeout) {
      Thread.sleep(100);
    }
  }

}
