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
package io.meeds.social.category.injection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryDescriptor;
import io.meeds.social.category.plugin.CategoryTranslationPlugin;
import io.meeds.social.category.storage.CategoryStorage;

/**
 * The « Work Areas » branch social ships (EXO-94953), read from the shipped
 * {@code default-categories.json}, and how the import treats it on an
 * instance that already imported every other default category.
 */
@ExtendWith(MockitoExtension.class)
class CategoryImportServiceTest {

  private static final Path         DESCRIPTORS_FILE   = Path.of("src/main/resources/default-categories.json");

  private static final Path         LABELS_FILE        =
                                                Path.of("../../webapp/src/main/resources/locale/portlet/CategoryManagement_en.properties");

  private static final String       WORK_AREAS         = "workAreasCategory";

  private static final List<String> WORK_AREA_CHILDREN = List.of("workAreaTasksAndProjectsCategory",
                                                                 "workAreaFormsAndListsCategory",
                                                                 "workAreaProcessesCategory",
                                                                 "workAreaContentAndKnowledgeCategory",
                                                                 "workAreaPeopleAndSpacesCategory",
                                                                 "workAreaCommunicationAndCalendarCategory",
                                                                 "workAreaInsightsCategory",
                                                                 "workAreaAdministrationCategory");

  private static final long         ROOT_ID            = 1L;

  @Mock
  private CategoryTranslationImportService categoryTranslationImportService;

  @Mock
  private CategoryStorage                  categoryStorage;

  @Mock
  private IdentityManager                  identityManager;

  @Mock
  private UserACL                          userAcl;

  @Mock
  private SettingService                   settingService;

  @InjectMocks
  private CategoryImportService            categoryImportService;

  private List<CategoryDescriptor>         descriptors;

  /**
   * Reads the shipped descriptors through the importer's own parser.
   *
   * @throws Exception when the file cannot be read
   */
  @BeforeEach
  void readShippedDescriptors() throws Exception {
    URL url = DESCRIPTORS_FILE.toUri().toURL();
    descriptors = categoryImportService.parseDescriptors(url);
    assertFalse(descriptors.isEmpty(), "the shipped default categories must parse");
  }

  /**
   * The branch ships with exactly its eight sub-categories, all in the first
   * release: a child added later under an imported parent is never imported
   * without a forced re-import.
   */
  @Test
  void workAreasShipsWithItsEightSubCategories() {
    CategoryDescriptor workAreas = getDescriptor(WORK_AREAS);
    assertNotNull(workAreas, "Work Areas must be a top-level default category");
    assertEquals(WORK_AREA_CHILDREN, workAreas.getSubCategories().stream().map(CategoryDescriptor::getNameId).toList());
    assertTrue(workAreas.getSubCategories().stream().allMatch(c -> c.getSubCategories() == null || c.getSubCategories().isEmpty()),
               "a Work Areas sub-category has no children of its own");
    assertEquals(List.of("*:/platform/users"), workAreas.getLinkPermissions());
    workAreas.getSubCategories().forEach(c -> assertEquals(List.of("*:/platform/users"), c.getLinkPermissions(), c.getNameId()));
  }

  /**
   * Every nameId of the file is unique and none is the importer's own
   * {@code version} key, since they all share one settings namespace.
   */
  @Test
  void everyNameIdIsUniqueAndNotTheVersionKey() {
    List<String> nameIds = new ArrayList<>();
    descriptors.forEach(d -> collectNameIds(d, nameIds));
    assertEquals(nameIds.size(), new HashSet<>(nameIds).size(), "duplicate nameId in " + nameIds);
    assertFalse(nameIds.contains("version"));
  }

  /**
   * Every English label key the descriptors name is in the English bundle the
   * import reads, or the category is stored under its raw key.
   *
   * @throws Exception when the bundle cannot be read
   */
  @Test
  void everyLabelKeyIsInTheEnglishBundle() throws Exception {
    Properties labels = new Properties();
    try (InputStream inputStream = Files.newInputStream(LABELS_FILE)) {
      labels.load(inputStream);
    }
    List<CategoryDescriptor> all = new ArrayList<>();
    descriptors.forEach(d -> collect(d, all));
    for (CategoryDescriptor descriptor : all) {
      String key = descriptor.getNames().get("en");
      assertTrue(labels.containsKey(key), "missing label " + key + " of " + descriptor.getNameId());
    }
    assertEquals("Work Areas", labels.getProperty("categoryManagement.category.workAreas.name"));
  }

  /**
   * On an instance that imported every other default category, the next start
   * imports Work Areas and its eight children, with no forced re-import, and
   * touches none of the categories already there (renamed, moved or deleted by
   * an administrator).
   */
  @Test
  void anExistingInstanceImportsTheNewBranchAloneWithItsChildren() {
    Map<String, Long> stored = new HashMap<>();
    AtomicLong nextId = new AtomicLong(100);
    descriptors.stream()
               .filter(d -> !WORK_AREAS.equals(d.getNameId()))
               .forEach(d -> collectNameIds(d, new ArrayList<>()).forEach(id -> stored.put(id, nextId.incrementAndGet())));
    lenient().when(settingService.get(any(Context.class), any(Scope.class), anyString()))
             .thenAnswer(invocation -> {
               Long id = stored.get(invocation.getArgument(2, String.class));
               return id == null ? null : SettingValue.create(String.valueOf(id));
             });
    mockIdentities();
    Category root = new Category();
    root.setId(ROOT_ID);
    when(categoryStorage.getRootCategory(anyLong())).thenReturn(root);
    when(categoryStorage.createCategory(any(Category.class))).thenAnswer(invocation -> {
      Category category = invocation.getArgument(0);
      category.setId(nextId.incrementAndGet());
      return category;
    });

    descriptors.forEach(categoryImportService::importDescriptor);

    Set<String> imported = new HashSet<>(WORK_AREA_CHILDREN);
    imported.add(WORK_AREAS);
    verify(categoryStorage, times(imported.size())).createCategory(any(Category.class));
    verify(categoryStorage, never()).updateCategory(any(Category.class));
    verify(categoryStorage, never()).getCategory(anyLong());
    for (String nameId : imported) {
      verify(settingService).set(eq(CategoryImportService.CATEGORY_CONTEXT),
                                 eq(CategoryImportService.CATEGORY_IMPORT_SCOPE),
                                 eq(nameId),
                                 any());
    }
    for (String nameId : stored.keySet()) {
      verify(settingService, never()).set(any(Context.class), any(Scope.class), eq(nameId), any());
    }
    verify(categoryTranslationImportService,
           times(imported.size())).saveTranslationLabels(eq(CategoryTranslationPlugin.OBJECT_TYPE),
                                                                              anyLong(),
                                                                              eq(CategoryTranslationPlugin.NAME_FIELD),
                                                                              anyMap());
  }

  /**
   * The settings scope and context that hold each imported category's id keep
   * their stored names: every instance's existing mapping lives under them,
   * and email-connector reads them by their literals.
   */
  @Test
  void theImportedIdsStayUnderTheirStoredScopeAndContext() {
    assertEquals("CATEGORY_IMPORT", CategoryImportService.CATEGORY_IMPORT_SCOPE.getId());
    assertEquals("CATEGORY", CategoryImportService.CATEGORY_CONTEXT.getId());
  }

  /**
   * Answers the super user and the administrators group identities the
   * import creates categories with.
   */
  private void mockIdentities() {
    when(userAcl.getSuperUser()).thenReturn("root");
    Identity userIdentity = new Identity("1");
    when(identityManager.getOrCreateUserIdentity("root")).thenReturn(userIdentity);
    Identity groupIdentity = new Identity("2");
    when(identityManager.getOrCreateGroupIdentity(CategoryImportService.ADMINISTRATORS_GROUP)).thenReturn(groupIdentity);
  }

  /**
   * @param nameId a top-level nameId
   * @return its descriptor, null when the file has none
   */
  private CategoryDescriptor getDescriptor(String nameId) {
    return descriptors.stream().filter(d -> nameId.equals(d.getNameId())).findFirst().orElse(null);
  }

  /**
   * @param descriptor a descriptor
   * @param nameIds the list to add its nameId and its descendants' to
   * @return that list
   */
  private static List<String> collectNameIds(CategoryDescriptor descriptor, List<String> nameIds) {
    List<CategoryDescriptor> all = new ArrayList<>();
    collect(descriptor, all);
    all.forEach(d -> nameIds.add(d.getNameId()));
    return nameIds;
  }

  /**
   * @param descriptor a descriptor
   * @param all the list to add it and its descendants to
   */
  private static void collect(CategoryDescriptor descriptor, List<CategoryDescriptor> all) {
    all.add(descriptor);
    if (descriptor.getSubCategories() != null) {
      descriptor.getSubCategories().forEach(d -> collect(d, all));
    }
  }

}
