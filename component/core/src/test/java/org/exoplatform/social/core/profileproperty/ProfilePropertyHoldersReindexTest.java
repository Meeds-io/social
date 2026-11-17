/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.profileproperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;

/**
 * The reindex of a property's holders after an option edit: which edits
 * trigger it, and the walk's paging, failure and stop rules. The two
 * transactional page methods are stubbed on a spy, and their un-advised bodies
 * tested directly, so that no container is booted.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfilePropertyHoldersReindexTest {

  private static final long          SETTING_ID = 3L;

  @Mock
  private ProfileSettingStorage      profileSettingStorage;

  @Mock
  private SettingService             settingService;

  @Mock
  private IndexingService            indexingService;

  @Mock
  private ListenerService            listenerService;

  @Mock
  private TranslationService         translationService;

  @Mock
  private IdentityDAO                identityDAO;

  private ProfilePropertyServiceImpl service;

  @Before
  public void setUp() {
    service = spy(new ProfilePropertyServiceImpl(null,
                                                 profileSettingStorage,
                                                 settingService,
                                                 indexingService,
                                                 listenerService,
                                                 translationService,
                                                 identityDAO));
  }

  @Test
  public void testValueOnlyChangeReindexesTheHolders() throws Exception {
    mockStoredSetting(true, List.of(option(1L, "HR")), List.of(option(1L, "Human resources")));

    service.updatePropertySetting(postedSetting(true, List.of(option(1L, "Human resources"))));

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testValueAndLabelChangeReindexesTheHolders() throws Exception {
    mockStoredSetting(true, List.of(option(1L, "HR")), List.of(option(1L, "Human resources")));
    mockLabels(1L, Map.of(Locale.ENGLISH, "HR"));

    service.updatePropertySetting(postedSetting(true, List.of(option(1L, "Human resources", Map.of("en", "Human resources")))));

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testLabelOnlyChangeReindexesTheHolders() throws Exception {
    mockStoredSetting(true, List.of(option(1L, "HR")), List.of(option(1L, "HR")));
    mockLabels(1L, Map.of(Locale.ENGLISH, "HR"));

    service.updatePropertySetting(postedSetting(true, List.of(option(1L, "HR", Map.of("en", "Human resources")))));

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testLabelChangeBesideANullTranslationReindexesTheHolders() throws Exception {
    mockStoredSetting(true, List.of(option(1L, "HR")), List.of(option(1L, "HR")));
    mockLabels(1L, Map.of(Locale.ENGLISH, "HR"));
    Map<String, String> translations = new java.util.HashMap<>();
    translations.put("en", "Human resources");
    translations.put("fr", null);

    service.updatePropertySetting(postedSetting(true, List.of(option(1L, "HR", translations))));

    verify(translationService).saveTranslationLabels(anyString(), eq(1L), anyString(), eq(Map.of(Locale.ENGLISH, "Human resources")));
    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testRemovedOptionReindexesTheHolders() throws Exception {
    mockStoredSetting(true, List.of(option(1L, "HR"), option(2L, "IT")), List.of(option(1L, "HR")));
    mockLabels(1L, Map.of(Locale.ENGLISH, "HR"));

    service.updatePropertySetting(postedSetting(true, List.of(option(1L, "HR", Map.of("en", "HR")))));

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testDropdownSwitchedOffReindexesTheHolders() throws Exception {
    mockStoredSetting(true, List.of(option(1L, "HR")), List.of(option(1L, "HR")));

    service.updatePropertySetting(postedSetting(false, List.of(option(1L, "HR"))));

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testDropdownSwitchedOnReindexesTheHolders() throws Exception {
    mockStoredSetting(false, List.of(option(1L, "HR")), List.of(option(1L, "HR")));
    mockLabels(1L, Map.of(Locale.ENGLISH, "HR"));

    service.updatePropertySetting(postedSetting(true, List.of(option(1L, "HR", Map.of("en", "HR")))));

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testPropertyShownAgainReindexesTheHolders() throws Exception {
    mockStoredSetting(false, List.of(), List.of());
    profileSettingStorage.getProfileSettingById(SETTING_ID).setVisible(false);

    service.updatePropertySetting(postedSetting(false, List.of()));

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testPropertyHiddenDoesNotReindex() throws Exception {
    mockStoredSetting(false, List.of(), List.of());
    ProfilePropertySetting posted = postedSetting(false, List.of());
    posted.setVisible(false);

    service.updatePropertySetting(posted);

    verify(listenerService, never()).broadcast(eq(ProfilePropertyServiceImpl.PROPERTY_OPTIONS_UPDATED_EVENT), any(), any());
  }

  /**
   * A multi-valued property's value is indexed without its accents, a
   * single-valued one's with them: switching the flag either way changes the
   * string the directory computes, so the holders are reindexed.
   */
  @Test
  public void testPropertyMadeMultiValuedReindexesTheHolders() throws Exception {
    mockStoredSetting(false, List.of(), List.of());
    ProfilePropertySetting posted = postedSetting(false, List.of());
    posted.setMultiValued(true);

    service.updatePropertySetting(posted);

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testPropertyMadeSingleValuedReindexesTheHolders() throws Exception {
    mockStoredSetting(false, List.of(), List.of());
    profileSettingStorage.getProfileSettingById(SETTING_ID).setMultiValued(true);

    service.updatePropertySetting(postedSetting(false, List.of()));

    verifyOptionsUpdatedBroadcast();
  }

  @Test
  public void testUnchangedOptionsDoNotReindex() throws Exception {
    mockStoredSetting(true, List.of(option(1L, "HR")), List.of(option(1L, "HR")));
    mockLabels(1L, Map.of(Locale.ENGLISH, "HR"));

    service.updatePropertySetting(postedSetting(true, List.of(option(1L, "HR", Map.of("en", "HR")))));

    verify(listenerService, never()).broadcast(eq(ProfilePropertyServiceImpl.PROPERTY_OPTIONS_UPDATED_EVENT), any(), any());
  }

  @Test
  public void testWalkReindexesEveryHolderPageByPage() {
    service.propertyHoldersPageSize = 2;
    doReturn(List.of(11L, 12L)).when(service).readPropertyHolders("department", 0);
    doReturn(List.of(13L, 14L)).when(service).readPropertyHolders("department", 12L);
    doReturn(List.of(15L)).when(service).readPropertyHolders("department", 14L);
    List<Long> reindexed = recordReindexedIds();

    service.walkPropertyHolders("department");

    assertEquals(List.of(11L, 12L, 13L, 14L, 15L), reindexed);
  }

  @Test
  public void testWalkResumesAfterAPageWhoseReindexFails() {
    service.propertyHoldersPageSize = 2;
    doReturn(List.of(11L, 12L)).when(service).readPropertyHolders("department", 0);
    doReturn(List.of(13L)).when(service).readPropertyHolders("department", 12L);
    doThrow(new IllegalStateException("queue unavailable")).when(service).reindexIdentities(List.of(11L, 12L));
    doAnswer(invocation -> null).when(service).reindexIdentities(List.of(13L));

    service.walkPropertyHolders("department");

    verify(service).reindexIdentities(List.of(13L));
  }

  @Test
  public void testWalkEndsOnAPageWhoseReadFailsAndTheNextWalkRuns() {
    doThrow(new IllegalStateException("database unavailable")).when(service).readPropertyHolders("department", 0);
    doReturn(List.of(21L)).when(service).readPropertyHolders("city", 0);
    doAnswer(invocation -> null).when(service).reindexIdentities(any());

    service.reindexPropertyHolders("department");
    service.reindexPropertyHolders("city");

    verify(service, timeout(5000)).reindexIdentities(List.of(21L));
    verify(service, timeout(5000).times(1)).readPropertyHolders("department", 0);
  }

  @Test
  public void testStopDuringAPageThatThrowsOnInterruptStartsNoFurtherPage() throws Exception {
    assertStopStartsNoFurtherPage(true);
  }

  @Test
  public void testStopDuringAPageThatCompletesStartsNoFurtherPage() throws Exception {
    assertStopStartsNoFurtherPage(false);
  }

  @Test
  public void testReadPropertyHoldersReadsAKeysetPage() {
    when(identityDAO.getIdsHoldingPropertyAfterId(OrganizationIdentityProvider.NAME,
                                                  "department",
                                                  12L,
                                                  ProfilePropertyServiceImpl.PROPERTY_HOLDERS_PAGE_SIZE)).thenReturn(List.of(13L));

    assertEquals(List.of(13L), service.doReadPropertyHolders("department", 12L));
  }

  @Test
  public void testReindexIdentitiesQueuesEachProfile() {
    service.doReindexIdentities(List.of(13L, 14L));

    verify(indexingService).reindex(ProfileIndexingServiceConnector.TYPE, "13");
    verify(indexingService).reindex(ProfileIndexingServiceConnector.TYPE, "14");
  }

  private void assertStopStartsNoFurtherPage(boolean throwOnInterrupt) throws Exception {
    service.propertyHoldersPageSize = 1;
    CountDownLatch pageStarted = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    doReturn(List.of(11L)).when(service).readPropertyHolders("department", 0);
    lenient().doReturn(List.of(12L)).when(service).readPropertyHolders("department", 11L);
    doAnswer(invocation -> {
      pageStarted.countDown();
      try {
        release.await();
      } catch (InterruptedException e) {
        if (throwOnInterrupt) {
          throw new IllegalStateException("interrupted", e);
        }
      }
      return null;
    }).when(service).reindexIdentities(List.of(11L));

    service.reindexPropertyHolders("department");
    assertTrue(pageStarted.await(5, TimeUnit.SECONDS));
    service.stop();

    verify(service, never()).readPropertyHolders("department", 11L);
    service.reindexPropertyHolders("city");
    verify(service, never()).readPropertyHolders(eq("city"), anyLong());
  }

  private List<Long> recordReindexedIds() {
    List<Long> reindexed = new ArrayList<>();
    doAnswer(invocation -> {
      reindexed.addAll(invocation.getArgument(0));
      return null;
    }).when(service).reindexIdentities(any());
    return reindexed;
  }

  private void verifyOptionsUpdatedBroadcast() {
    verify(listenerService).broadcast(eq(ProfilePropertyServiceImpl.PROPERTY_OPTIONS_UPDATED_EVENT),
                                      eq(service),
                                      any(ProfilePropertySetting.class));
  }

  private void mockStoredSetting(boolean dropdownBefore,
                                 List<ProfilePropertyOption> optionsBefore,
                                 List<ProfilePropertyOption> optionsAfter) {
    ProfilePropertySetting stored = setting(dropdownBefore, optionsBefore);
    when(profileSettingStorage.getProfileSettingById(SETTING_ID)).thenReturn(stored);
    when(profileSettingStorage.saveProfilePropertySetting(any(), eq(false))).thenAnswer(invocation -> invocation.getArgument(0));
    lenient().when(profileSettingStorage.getProfilePropertyOptions(SETTING_ID, 0, 0)).thenReturn(optionsAfter);
  }

  private void mockLabels(long optionId, Map<Locale, String> labels) throws Exception {
    TranslationField translationField = new TranslationField();
    translationField.setLabels(labels);
    when(translationService.getTranslationField(anyString(), eq(optionId), anyString())).thenReturn(translationField);
  }

  private ProfilePropertySetting postedSetting(boolean dropdown, List<ProfilePropertyOption> options) {
    return setting(dropdown, options);
  }

  private ProfilePropertySetting setting(boolean dropdown, List<ProfilePropertyOption> options) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId(SETTING_ID);
    setting.setPropertyName("department");
    setting.setPropertyType("text");
    setting.setDropdownList(dropdown);
    setting.setVisible(true);
    setting.setActive(true);
    setting.setPropertyOptions(new ArrayList<>(options));
    return setting;
  }

  private static ProfilePropertyOption option(long id, String value) {
    return option(id, value, Map.of());
  }

  private static ProfilePropertyOption option(long id, String value, Map<String, String> translations) {
    ProfilePropertyOption option = new ProfilePropertyOption();
    option.setId(id);
    option.setValue(value);
    option.setPropertySettingId(SETTING_ID);
    option.setTranslations(translations);
    return option;
  }
}
