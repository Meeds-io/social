/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostCreateTask;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletContext;
import lombok.Synchronized;

/**
 * A Service used to execute in an async way, server startup Tasks such as
 * importing default data or configuration
 */
@Service
public class ContainerStartupTaskService implements ApplicationContextAware {

  private static final Log            LOG       = ExoLogger.getLogger(ContainerStartupTaskService.class);

  @Autowired
  private PortalContainer             container;

  private ApplicationContext          applicationContext;

  @Value("${meeds.startup.threadCount:5}")
  private int                         threadCount;

  private ExecutorService             executorService;

  private Map<String, List<Runnable>> tasksById = new ConcurrentHashMap<>();

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void init() {
    List<ContainerStartableService> serviceList = applicationContext.getBeansOfType(ContainerStartableService.class)
                                                                    .values()
                                                                    .stream()
                                                                    .sorted((a,
                                                                             b) -> (a.getOrder() - b.getOrder()))
                                                                    .toList();
    serviceList.forEach(s -> addTask(s.getId(), s::start));

    LOG.info("Run {} startup Tasks in asynchronous threads", serviceList.size());
    this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    if (container.isStarted()) {
      CompletableFuture.runAsync(ContainerStartupTaskService.this::start, executorService);
    } else {
      PortalContainer.addInitTask(container.getPortalContext(), new PortalContainerPostCreateTask() {
        @Override
        public void execute(ServletContext context, PortalContainer portalContainer) {
          CompletableFuture.runAsync(ContainerStartupTaskService.this::start, executorService);
        }
      });
    }
  }

  public void start() {
    LOG.info("Run startup Tasks in dedicated threads");
    long start = System.currentTimeMillis();
    tasksById.entrySet()
             .parallelStream()
             .forEach(entry -> entry.getValue()
                                    .stream()
                                    .forEachOrdered(r -> {
                                      long startup = System.currentTimeMillis();
                                      LOG.info("Startup Asynchronous Task with chain id {}", entry.getKey());
                                      try {
                                        Future<?> future = executorService.submit(r);
                                        future.get();
                                      } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                      } catch (Exception e) {
                                        LOG.warn("Error while executing an asynchronous startup task ", e);
                                      }
                                      LOG.info("Asynchronous Task with chain id {} finished within {}ms",
                                               entry.getKey(),
                                               System.currentTimeMillis() - startup);
                                    }));
    LOG.info("Startup Tasks finished within {}ms", System.currentTimeMillis() - start);
    tasksById.clear();
  }

  @Synchronized
  public void addTask(String id, Runnable task) {
    tasksById.computeIfAbsent(id, k -> Collections.synchronizedList(new ArrayList<>())).add(task);
  }

  @PreDestroy
  public void stop() {
    executorService.shutdown();
  }

}
