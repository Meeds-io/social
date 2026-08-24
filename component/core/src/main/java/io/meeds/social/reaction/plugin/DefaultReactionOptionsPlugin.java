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
package io.meeds.social.reaction.plugin;

import java.util.List;

import org.springframework.stereotype.Component;

import io.meeds.social.reaction.model.ReactionOption;
import io.meeds.social.reaction.service.ReactionService;

@Component
public class DefaultReactionOptionsPlugin implements ReactionOptionPlugin {

  private static final String LABEL_KEY_PREFIX        = "UIActivity.reaction.";

  private static final String ACTIVE_LABEL_KEY_PREFIX   = "UIActivity.reaction.active.";

  private static final String SELECTED_LABEL_KEY_PREFIX = "UIActivity.reaction.selected.";

  private static final List<ReactionOption> DEFAULT_OPTIONS =
      List.of(option(ReactionService.LIKE_REACTION_ID, "\uD83D\uDC4D", 10),
              option("applause", "\uD83D\uDC4F", 20),
              option("love", "\u2764\uFE0F", 30),
              option("insightful", "\uD83D\uDCA1", 40),
              option("sad", "\uD83D\uDE22", 50),
              option("funny", "\uD83D\uDE02", 60));

  @Override
  public List<ReactionOption> getReactionOptions() {
    return DEFAULT_OPTIONS;
  }

  private static ReactionOption option(String id, String emoji, int rank) {
    return new ReactionOption(id, emoji, LABEL_KEY_PREFIX + id, ACTIVE_LABEL_KEY_PREFIX + id, SELECTED_LABEL_KEY_PREFIX + id, rank, null);
  }

}
