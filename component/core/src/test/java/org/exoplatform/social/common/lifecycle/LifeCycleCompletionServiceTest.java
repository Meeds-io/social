/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;

/**
 * JUnit 4: surefire runs this module's JUnit 4 suites only, and a suite of
 * them runs JUnit 4 classes only.
 */
public class LifeCycleCompletionServiceTest {

  private LifeCycleCompletionService completionService;

  @After
  public void tearDown() {
    if (completionService != null) {
      completionService.stop();
    }
  }

  /**
   * Nothing keeps a task, nor its result, once it has run: the pool runs one
   * task per lifecycle event, and a task returns the event with its payload.
   */
  @Test
  public void testACompletedTaskIsNotRetained() throws Exception {
    completionService = new LifeCycleCompletionService(null);
    AtomicReference<WeakReference<Object>> result = new AtomicReference<>();
    CountDownLatch running = new CountDownLatch(1);

    completionService.addTask(() -> {
      Object taskResult = new Object();
      result.set(new WeakReference<>(taskResult));
      running.countDown();
      return taskResult;
    });

    assertTrue(running.await(10, TimeUnit.SECONDS));
    for (int i = 0; i < 100 && result.get().get() != null; i++) {
      System.gc(); // NOSONAR the reference is cleared only by a collection
      Thread.sleep(20);
    }
    assertNull("The result of a completed task is still reachable", result.get().get());
  }

}
