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
package io.meeds.social.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.exoplatform.social.core.application.ProfileUpdatesPublisher;
import org.exoplatform.social.core.application.RelationshipPublisher;

import tools.jackson.databind.ObjectMapper;

/**
 * The collaborative activity declaration analytics reads from this jar: it
 * names the system posts and comments Social writes in a stream, so that
 * none of them counts as a user's collaborative act. The types are the exact
 * strings the publishers set, read from their constants.
 */
public class CollaborativeActivityDeclarationTest {

  private static final String DECLARATION_FILE = "analytics-collaborative-activities.json";

  @Test
  @SuppressWarnings("unchecked")
  public void theDeclarationExcludesEverySystemTypeTheStreamPublishersSet() throws IOException {
    Map<String, Object> declaration;
    try (InputStream content = getClass().getClassLoader().getResourceAsStream(DECLARATION_FILE)) {
      assertNotNull("the declaration ships at the root of the core jar, which lands in /lib", content);
      declaration = new ObjectMapper().readValue(content, Map.class);
    }

    assertEquals(List.of("social"), declaration.get("modules"));
    assertFalse("the stream events are declared by analytics itself, not here", declaration.containsKey("operations"));
    assertFalse(declaration.containsKey("legacyOperations"));
    assertFalse(declaration.containsKey("browserOperations"));

    Set<String> expectedTypes = Set.of(ProfileUpdatesPublisher.USER_PROFILE_ACTIVITY,
                                       ProfileUpdatesPublisher.PEOPLE_APP_ID,
                                       RelationshipPublisher.USER_ACTIVITIES_FOR_RELATIONSHIP,
                                       RelationshipPublisher.USER_COMMENTS_ACTIVITY_FOR_RELATIONSHIP);
    List<String> excludedTypes = (List<String>) declaration.get("excludedActivityTypes");
    assertEquals(expectedTypes, new HashSet<>(excludedTypes));
    assertEquals("one entry per type", expectedTypes.size(), excludedTypes.size());

    Map<String, Object> labels = (Map<String, Object>) declaration.get("labels");
    assertEquals(List.of("social.collaborativeActivity.help.notCounted.profile",
                         "social.collaborativeActivity.help.notCounted.connection"),
                 labels.get("notCounted"));
    assertFalse("nothing of Social's own is counted: the post and the comment are analytics' labels",
                labels.containsKey("counted"));
  }

}
