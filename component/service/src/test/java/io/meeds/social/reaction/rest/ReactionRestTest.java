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
package io.meeds.social.reaction.rest;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.entity.ProfileEntity;

import io.meeds.social.reaction.model.ObjectReactions;
import io.meeds.social.reaction.model.Reaction;
import io.meeds.social.reaction.model.ReactionOption;
import io.meeds.social.reaction.service.ReactionService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReactionRestTest {

  private static final RequestPostProcessor JOHN = request -> {
                                                     request.setRemoteUser("john");
                                                     return request;
                                                   };

  private static final String               BODY = "{\"reactionId\":\"love\"}";

  @Mock
  private ReactionService                   reactionService;

  @Mock
  private IdentityManager                   identityManager;

  @InjectMocks
  private ReactionRest                      rest;

  private MockMvc                           mockMvc;

  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(rest).build();
    stub("activity", "1", null, Map.of("like", 2L), List.of(new Reaction(7, "like", "activity", "1")), "like");
  }

  private void stub(String objectType,
                    String objectId,
                    String reactionId,
                    Map<String, Long> counts,
                    List<Reaction> reactions,
                    String userReactionId) throws Exception {
    when(reactionService.getObjectReactions(objectType, objectId, reactionId, 0, 20, "john")).thenReturn(new ObjectReactions(counts,
                                                                                                                              reactions,
                                                                                                                              userReactionId));
  }

  @Test
  public void optionsAreTheRegisteredOnes() throws Exception {
    when(reactionService.getReactionOptions()).thenReturn(List.of(new ReactionOption("like", "x", "l", "l", "l", 1, null)));
    when(reactionService.getReactionOptions("task")).thenReturn(List.of());

    mockMvc.perform(get("/reactions/options")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("like"));
    mockMvc.perform(get("/reactions/options?objectType=task")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
  }

  @Test
  public void unfilteredActivityListHasNoProfilesAndTheReactionOfTheCaller() throws Exception {
    mockMvc.perform(get("/reactions/activity/1").with(JOHN))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.counts.like").value(2))
           .andExpect(jsonPath("$.reactions[0].reactionId").value("like"))
           .andExpect(jsonPath("$.reactorProfiles").doesNotExist())
           .andExpect(jsonPath("$.userReactionId").value("like"));

    verify(identityManager, never()).getIdentity(7L);
  }

  @Test
  public void filteredActivityListAndAnyListOfAnotherObjectTypeHaveProfiles() throws Exception {
    Identity identity = new Identity("7");
    identity.setProfile(new Profile(identity));
    when(identityManager.getIdentity(7L)).thenReturn(identity);
    ProfileEntity profileEntity = new ProfileEntity();
    profileEntity.setId("7");
    profileEntity.setFullname("John Doe");
    stub("activity", "1", "like", Map.of("like", 2L), List.of(new Reaction(7, "like", "activity", "1")), "like");
    stub("taskComment", "5", null, Map.of("love", 1L), List.of(new Reaction(7, "love", "taskComment", "5")), null);

    try (MockedStatic<EntityBuilder> builder = mockStatic(EntityBuilder.class)) {
      builder.when(() -> EntityBuilder.buildEntityProfile(identity.getProfile(), null)).thenReturn(profileEntity);

      mockMvc.perform(get("/reactions/activity/1?reactionId=like").with(JOHN))
             .andExpect(status().isOk())
             .andExpect(jsonPath("$.reactorProfiles.length()").value(1));
      mockMvc.perform(get("/reactions/taskComment/5").with(JOHN))
             .andExpect(status().isOk())
             .andExpect(jsonPath("$.reactorProfiles.length()").value(1))
             .andExpect(jsonPath("$.userReactionId").doesNotExist());
    }
  }

  @Test
  public void aReactorWithoutIdentityIsLeftOut() throws Exception {
    stub("activity", "1", "like", Map.of("like", 2L), List.of(new Reaction(7, "like", "activity", "1")), "like");
    stub("activity", "1", "love", Map.of("like", 2L), List.of(new Reaction(8, "love", "activity", "1")), "like");

    mockMvc.perform(get("/reactions/activity/1?reactionId=like").with(JOHN)).andExpect(jsonPath("$.reactorProfiles").isEmpty());
    mockMvc.perform(get("/reactions/activity/1?reactionId=love").with(JOHN)).andExpect(jsonPath("$.reactorProfiles").isEmpty());
  }

  @Test
  public void theLimitDefaultsAndIsCapped() throws Exception {
    when(reactionService.getObjectReactions("activity", "1", null, 4, 100, "john")).thenReturn(new ObjectReactions(Map.of(),
                                                                                                                  List.of(),
                                                                                                                  null));
    mockMvc.perform(get("/reactions/activity/1?limit=-3").with(JOHN)).andExpect(status().isOk());
    mockMvc.perform(get("/reactions/activity/1?limit=5000&offset=4").with(JOHN)).andExpect(status().isOk());

    verify(reactionService).getObjectReactions("activity", "1", null, 0, 20, "john");
    verify(reactionService).getObjectReactions("activity", "1", null, 4, 100, "john");
  }

  @Test
  public void listErrorsAreMapped() throws Exception {
    when(reactionService.getObjectReactions("activity", "404", null, 0, 20, "john")).thenThrow(new ObjectNotFoundException("x"));
    when(reactionService.getObjectReactions("activity", "403", null, 0, 20, "john")).thenThrow(new IllegalAccessException("x"));
    when(reactionService.getObjectReactions("other", "1", null, 0, 20, "john")).thenThrow(new IllegalArgumentException("x"));

    mockMvc.perform(get("/reactions/activity/404").with(JOHN)).andExpect(status().isNotFound());
    mockMvc.perform(get("/reactions/activity/403").with(JOHN)).andExpect(status().isForbidden());
    mockMvc.perform(get("/reactions/other/1").with(JOHN)).andExpect(status().isBadRequest());
  }

  @Test
  public void setAndDeleteReactionAreTheCallersAndMapTheirErrors() throws Exception {
    mockMvc.perform(put("/reactions/activity/1").with(JOHN).contentType(MediaType.APPLICATION_JSON).content(BODY))
           .andExpect(status().isNoContent());
    mockMvc.perform(delete("/reactions/activity/1").with(JOHN)).andExpect(status().isNoContent());
    verify(reactionService).setReaction("activity", "1", "love", "john");
    verify(reactionService).deleteReaction("activity", "1", "john");

    doThrow(new ObjectNotFoundException("x")).when(reactionService).setReaction("activity", "404", "love", "john");
    doThrow(new IllegalAccessException("x")).when(reactionService).setReaction("activity", "403", "love", "john");
    doThrow(new IllegalArgumentException("x")).when(reactionService).setReaction("other", "1", "love", "john");
    doThrow(new ObjectNotFoundException("x")).when(reactionService).deleteReaction("activity", "404", "john");
    doThrow(new IllegalAccessException("x")).when(reactionService).deleteReaction("activity", "403", "john");
    doThrow(new IllegalArgumentException("x")).when(reactionService).deleteReaction("other", "1", "john");

    for (String path : List.of("activity/404", "activity/403", "other/1")) {
      int expected = path.endsWith("404") ? 404 : path.endsWith("403") ? 403 : 400;
      mockMvc.perform(put("/reactions/" + path).with(JOHN).contentType(MediaType.APPLICATION_JSON).content(BODY))
             .andExpect(status().is(expected));
      mockMvc.perform(delete("/reactions/" + path).with(JOHN)).andExpect(status().is(expected));
    }
  }

}
