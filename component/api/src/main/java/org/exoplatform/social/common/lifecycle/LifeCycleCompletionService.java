/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.common.lifecycle;

import java.util.concurrent.Callable;
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
 */
@SuppressWarnings("rawtypes")
public class LifeCycleCompletionService implements Startable {

  private static final String       THREAD_NUMBER_KEY       = "thread-number";

  private static final String       ASYNC_EXECUTION_KEY     = "async-execution";

  private static final int          DEFAULT_THREAD_NUMBER   = 1;

  private static final boolean      DEFAULT_ASYNC_EXECUTION = true;

  private ExecutorService           executor;

  private int                       configThreadNumber;

  private boolean                   configAsyncExecution;

  public LifeCycleCompletionService(InitParams params) {
    ValueParam threadNumber = params == null ? null : params.getValueParam(THREAD_NUMBER_KEY);
    ValueParam asyncExecution = params == null ? null : params.getValueParam(ASYNC_EXECUTION_KEY);

    this.configThreadNumber = threadNumber == null ? DEFAULT_THREAD_NUMBER : Integer.valueOf(threadNumber.getValue());
    this.configAsyncExecution = asyncExecution == null ? DEFAULT_ASYNC_EXECUTION : Boolean.valueOf(asyncExecution.getValue());
    this.executor = Executors.newFixedThreadPool(this.configThreadNumber);
  }

  @Override
  public void stop() {
    executor.shutdown();
  }

  /**
   * Runs the task on the pool. Neither its result nor an exception it lets
   * escape is kept: a task logs its own errors.
   *
   * @param callable the task to run
   */
  @SuppressWarnings("unchecked")
  public void addTask(Callable callable) {
    executor.submit(callable); // NOSONAR the future is deliberately not kept
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
