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

import org.exoplatform.services.security.ConversationState;

import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.plugin.HtmlTransformerPlugin;

@Service
public class HtmlTransformerServiceImpl implements HtmlTransformerService {

  private List<HtmlTransformerPlugin> transformers = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void addPlugin(HtmlTransformerPlugin plugin) {
    transformers.add(plugin);
  }

  @Override
  public void removePlugin(HtmlTransformerPlugin plugin) {
    transformers.remove(plugin);
  }

  @Override
  public String transform(String html, HtmlTransformerContext context) {
    if (context == null) {
      context = new HtmlTransformerContext();
    }
    setUserIdentity(context);
    for (HtmlTransformerPlugin plugin : transformers) {
      html = plugin.transform(html, context);
    }
    return html;
  }

  private void setUserIdentity(HtmlTransformerContext context) {
    if (context.getUserIdentity() == null) {
      if (ConversationState.getCurrent() == null) {
        context.setSystem(true);
      } else {
        context.setUserIdentity(ConversationState.getCurrent().getIdentity());
      }
    }
  }

}
