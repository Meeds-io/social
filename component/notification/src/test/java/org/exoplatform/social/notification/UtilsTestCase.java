package org.exoplatform.social.notification;


import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.space.model.Space;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UtilsTestCase extends AbstractCoreTest {

  Space space ;
  @Override
  public void setUp() throws Exception {
    super.setUp();
    space = new Space();
    space.setDisplayName("my space");
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("new space ");
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setEditor(rootIdentity.getRemoteId());
    space.setAvatarLastUpdated(System.currentTimeMillis());
    Space createdSpace = spaceService.createSpace(space, rootIdentity.getRemoteId());

    Arrays.asList(rootIdentity.getRemoteId(), demoIdentity.getRemoteId(), johnIdentity.getRemoteId())
          .forEach(u -> spaceService.addMember(createdSpace, u));

    tearDownSpaceList.add(createdSpace);
    System.setProperty(CommonsUtils.CONFIGURED_DOMAIN_URL_KEY, "http://test.com");
  }

  public void testProcessLinkInActivityTitle() {
    String title = "<a href=\"www.yahoo.com\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\">Hotmail Site</a>";
    title = Utils.processLinkTitle(title);
    assertEquals("<a href=\"www.yahoo.com\" style=\"color: #2f5e92; text-decoration: none;\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\" style=\"color: #2f5e92; text-decoration: none;\">Hotmail Site</a>", title);
    
    title = "Shared a document <a href=\"portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\">New design. eXo in Smart Watch.jpg</a>";
    assertEquals("Shared a document <a href=\"http://test.com/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\" style=\"color: #2f5e92; text-decoration: none;\">New design. eXo in Smart Watch.jpg</a>", Utils.processLinkTitle(title));
    title = "Shared a document <a href=\"/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\">New design. eXo in Smart Watch.jpg</a>";
    assertEquals("Shared a document <a href=\"http://test.com/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\" style=\"color: #2f5e92; text-decoration: none;\">New design. eXo in Smart Watch.jpg</a>", Utils.processLinkTitle(title));
  }

  public void testGetMentioners() {
    String mentionTitle ="<a href=\"/portal/dw/profile/demo\"> " + demoIdentity.getProfile().getFullName() + " </a>";
    Set<String> receivers = Utils.getMentioners(mentionTitle, rootIdentity.getId(), space.getId());
    assertEquals(1, receivers.size());
    mentionTitle ="<a href=\"/portal/dw/profile/root\"> "+ rootIdentity.getProfile().getFullName() + " </a>";
    receivers = Utils.getMentioners(mentionTitle, rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());
    mentionTitle ="<a href=\"/portal/dw/profile/ghost\"> "+ ghostIdentity.getProfile().getFullName() + " </a>";
    receivers = Utils.getMentioners(mentionTitle, rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());
  }


  public void testSendToCommenters() {
    Set<String> receivers = new HashSet<>();
    String[] commenters = new String[] { demoIdentity.getId()};
    Utils.sendToCommeters(receivers, commenters, rootIdentity.getId(), space.getId());
    assertEquals(1, receivers.size());

    receivers = new HashSet<>();
    commenters = new String[] { rootIdentity.getId()};
    Utils.sendToCommeters(receivers, commenters, rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());

    receivers = new HashSet<>();
    commenters = new String[] {ghostIdentity.getId()};
    Utils.sendToCommeters(receivers, commenters, rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());
  }

  public void testSendToActivityPoster() {
    Set<String> receivers = new HashSet<>();
    Utils.sendToActivityPoster(receivers, demoIdentity.getId(), rootIdentity.getId(), space.getId());
    assertEquals(1, receivers.size());

    receivers = new HashSet<>();
    Utils.sendToActivityPoster(receivers, rootIdentity.getId(), rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());

    receivers = new HashSet<>();
    Utils.sendToActivityPoster(receivers, ghostIdentity.getId(), rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());
  }
}
