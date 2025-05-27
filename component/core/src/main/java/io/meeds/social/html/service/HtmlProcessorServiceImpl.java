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
package io.meeds.social.html.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import io.meeds.social.html.model.HtmlProcessorContext;
import io.meeds.social.html.plugin.HtmlProcessorPlugin;

@Service
public class HtmlProcessorServiceImpl implements HtmlProcessorService {

  private List<HtmlProcessorPlugin> processors = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void addPlugin(HtmlProcessorPlugin plugin) {
    processors.add(plugin);
  }

  @Override
  public void removePlugin(HtmlProcessorPlugin plugin) {
    processors.remove(plugin);
  }

  @Override
  public String process(String html, HtmlProcessorContext context) {
    for (HtmlProcessorPlugin plugin : processors) {
      html = plugin.process(html, context);
    }
    return html;
  }

}
