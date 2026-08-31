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
package io.meeds.social.report.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataItemDAO;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

/**
 * Container test running the report flow through the real listener chain:
 * MetadataService.createMetadataItem synchronously fires
 * social.metadataItem.created, whose MetadataItemAdded listener moves items of
 * a redirected activity (news article) to the redirected content object —
 * report items are exempted from that move so the duplicate guard and the
 * stale sweep, both querying the activity anchor, keep working on articles.
 */
public class ActivityReportServiceTest extends AbstractCoreTest {

  private static final Log        LOG                     = ExoLogger.getLogger(ActivityReportServiceTest.class);

  private static final String     CONTRAST_METADATA_TYPE  = "reportsMoveContrast";

  private static final String     REDIRECT_OBJECT_TYPE    = "news";

  private static final String     REDIRECT_OBJECT_ID      = "8";

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private ActivityManager         activityManager;

  private IdentityManager         identityManager;

  private MetadataService         metadataService;

  private MetadataItemDAO         metadataItemDAO;

  private ActivityReportService   activityReportService;

  private List<ExoSocialActivity> tearDownActivityList;

  private List<Space>             tearDownSpaceList;

  @Override
  public void setUp() throws Exception {
    try {
      super.setUp();
    } catch (Exception e) {
      LOG.error("Error initializing parent Test class", e);
    }
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    metadataItemDAO = getContainer().getComponentInstanceOfType(MetadataItemDAO.class);
    if (metadataService.getMetadataTypeByName(ActivityReportService.METADATA_TYPE_NAME) == null) {
      metadataService.addMetadataTypePlugin(new MetadataTypePlugin(newParam(89471,
                                                                            ActivityReportService.METADATA_TYPE_NAME)));
    }
    if (metadataService.getMetadataTypeByName(CONTRAST_METADATA_TYPE) == null) {
      metadataService.addMetadataTypePlugin(new MetadataTypePlugin(newParam(89999, CONTRAST_METADATA_TYPE)) {
        @Override
        public boolean isAllowMultipleItemsPerObject() {
          return true;
        }

        @Override
        public boolean isShareable() {
          return true;
        }
      });
    }
    activityReportService = newActivityReportService();

    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    tearDownActivityList = new ArrayList<>();
    tearDownSpaceList = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    restartTransaction();
    // delete items only: deleting Metadata rows directly through the DAO would
    // bypass the MetadataStorage cache and leave it serving transient entities
    metadataItemDAO.deleteAll();

    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }

    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }

    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    super.tearDown();
  }

  public void testReportOnRedirectedActivityKeepsAnchorAndDuplicateGuard() throws Exception {
    ExoSocialActivity activity = createRedirectedSpaceActivity("ReportedArticleSpace");

    activityReportService.reportActivity(activity.getId(), "spam", marySecurityIdentity());
    restartTransaction();

    // the synchronous MetadataItemAdded listener must NOT have moved the
    // report item to the redirected content object
    List<MetadataItem> anchoredReports =
                                       metadataService.getMetadataItemsByMetadataTypeAndObject(ActivityReportService.METADATA_TYPE_NAME,
                                                                                               activityMetadataObject(activity));
    assertEquals(1, anchoredReports.size());
    assertEquals(ActivityReportService.STATUS_ACTIVE,
                 anchoredReports.get(0).getProperties().get(ActivityReportService.STATUS_PROPERTY));
    List<MetadataItem> movedReports =
                                    metadataService.getMetadataItemsByMetadataTypeAndObject(ActivityReportService.METADATA_TYPE_NAME,
                                                                                            redirectedMetadataObject());
    assertEquals(0, movedReports.size());

    // contrast: a non-report item created on the same anchor IS moved by the
    // same listener chain — proves the harness exercises the move and the
    // exemption stays scoped to report items
    metadataService.createMetadataItem(activityMetadataObject(activity),
                                       new MetadataKey(CONTRAST_METADATA_TYPE,
                                                       maryIdentity.getId(),
                                                       Long.parseLong(maryIdentity.getId())),
                                       Long.parseLong(maryIdentity.getId()));
    restartTransaction();
    assertEquals(0,
                 metadataService.getMetadataItemsByMetadataTypeAndObject(CONTRAST_METADATA_TYPE,
                                                                         activityMetadataObject(activity))
                                .size());
    assertEquals(1,
                 metadataService.getMetadataItemsByMetadataTypeAndObject(CONTRAST_METADATA_TYPE, redirectedMetadataObject())
                                .size());

    // with the anchor preserved, the duplicate guard sees the first report
    try {
      activityReportService.reportActivity(activity.getId(), "spam", marySecurityIdentity());
      fail("Second report of the same redirected activity by the same user should be rejected");
    } catch (ObjectAlreadyExistsException e) {
      // expected
    }
  }

  public void testMarkReportsStaleOnRedirectedActivity() throws Exception {
    ExoSocialActivity activity = createRedirectedSpaceActivity("StaleArticleSpace");

    activityReportService.reportActivity(activity.getId(), "spam", marySecurityIdentity());
    restartTransaction();

    activityReportService.markReportsStale(activityManager.getActivity(activity.getId()));
    restartTransaction();

    List<MetadataItem> anchoredReports =
                                       metadataService.getMetadataItemsByMetadataTypeAndObject(ActivityReportService.METADATA_TYPE_NAME,
                                                                                               activityMetadataObject(activity));
    assertEquals(1, anchoredReports.size());
    assertEquals(ActivityReportService.STATUS_STALE,
                 anchoredReports.get(0).getProperties().get(ActivityReportService.STATUS_PROPERTY));

    // a stale report can be submitted again and is reactivated in place
    activityReportService.reportActivity(activity.getId(), "falseInformation", marySecurityIdentity());
    restartTransaction();
    anchoredReports = metadataService.getMetadataItemsByMetadataTypeAndObject(ActivityReportService.METADATA_TYPE_NAME,
                                                                              activityMetadataObject(activity));
    assertEquals(1, anchoredReports.size());
    assertEquals(ActivityReportService.STATUS_ACTIVE,
                 anchoredReports.get(0).getProperties().get(ActivityReportService.STATUS_PROPERTY));
    assertEquals("falseInformation", anchoredReports.get(0).getProperties().get(ActivityReportService.REASON_PROPERTY));
  }

  private ExoSocialActivity createRedirectedSpaceActivity(String spaceName) {
    Space space = createSpace(spaceName, johnIdentity.getRemoteId(), johnIdentity.getRemoteId(), maryIdentity.getRemoteId());
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("An article activity");
    activity.setUserId(johnIdentity.getId());
    activity.setType("news");
    // persist the same redirect a news article activity carries (set by
    // ActivityNewsProcessor at read time on real instances)
    activity.setMetadataObjectType(REDIRECT_OBJECT_TYPE);
    activity.setMetadataObjectId(REDIRECT_OBJECT_ID);
    activityManager.saveActivityNoReturn(spaceIdentity, activity);
    tearDownActivityList.add(activity);
    restartTransaction();

    ExoSocialActivity storedActivity = activityManager.getActivity(activity.getId());
    assertTrue("The stored activity must carry the content redirect for this scenario to be representative",
               storedActivity.hasSpecificMetadataObject());
    return storedActivity;
  }

  private MetadataObject activityMetadataObject(ExoSocialActivity activity) {
    return new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE, activity.getId());
  }

  private MetadataObject redirectedMetadataObject() {
    return new MetadataObject(REDIRECT_OBJECT_TYPE, REDIRECT_OBJECT_ID);
  }

  private org.exoplatform.services.security.Identity marySecurityIdentity() {
    return new org.exoplatform.services.security.Identity(maryIdentity.getRemoteId());
  }

  private ActivityReportService newActivityReportService() throws Exception {
    ActivityReportServiceImpl service = new ActivityReportServiceImpl();
    inject(service, "activityManager", getContainer().getComponentInstanceOfType(ActivityManager.class));
    inject(service, "identityManager", getContainer().getComponentInstanceOfType(IdentityManager.class));
    inject(service, "metadataService", getContainer().getComponentInstanceOfType(MetadataService.class));
    inject(service, "listenerService", getContainer().getComponentInstanceOfType(ListenerService.class));
    return service;
  }

  private void inject(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true); // NOSONAR
    field.set(target, value);
  }

  private InitParams newParam(long id, String name) {
    InitParams params = new InitParams();
    MetadataType metadataType = new MetadataType(id, name);
    ObjectParameter parameter = new ObjectParameter();
    parameter.setName("metadataType");
    parameter.setObject(metadataType);
    params.addParameter(parameter);
    return params;
  }

  private Space createSpace(String spaceName, String manager, String... members) {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setVisibility(Space.PRIVATE);
    String[] managers = new String[] { manager };
    space.setManagers(managers);
    Space createdSpace = spaceService.createSpace(space);
    if (ArrayUtils.isNotEmpty(members)) {
      Arrays.stream(members).forEach(u -> spaceService.addMember(createdSpace, u));
    }
    tearDownSpaceList.add(createdSpace);
    return createdSpace;
  }

}
