package org.exoplatform.social.core.listeners;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.storage.PageStorage;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

@RunWith(MockitoJUnitRunner.class)

public class ManagerPropertySettingUpdatedListenerTest {

  @Mock
  PageStorage pageStorage;

  @Mock
  ProfilePropertyService profilePropertyService;

  @Mock
  PortalContainer portalContainer;

  @Mock
  UserACL userACL;

  private static final MockedStatic<PortalContainer> PORTAL_CONTAINER = mockStatic(PortalContainer.class);


  @Test
  public void testOnEvent() {
    PORTAL_CONTAINER.when(PortalContainer::getInstance).thenReturn(portalContainer);
    lenient().when(userACL.getSuperUser()).thenReturn("root");
    ManagerPropertySettingUpdatedListener managerPropertySettingUpdatedListener = new ManagerPropertySettingUpdatedListener(pageStorage, userACL);
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting("testProperty", "text", true, true, null, 1L, true, false, false, true, false, 1L, System.currentTimeMillis());
    Event<ProfilePropertyService, ProfilePropertySetting> event = new Event<>("profile-property-setting-updated", profilePropertyService, profilePropertySetting);
    try {
      managerPropertySettingUpdatedListener.onEvent(event);
    } catch (Exception e) {
      fail();
    }
    verify(pageStorage, times(0)).savePage(any());

    PageState pageState = new PageState("MyTeam",
                                        "myteam page",
                                        false,
                                        "PagesFactory",
                                        List.of("/platform/users"),
                                        "/platform/administrators",
                                        "page",
                                        "/myteam");
    PageContext pageContext = new PageContext(PageKey.parse("portal::global::organizationalChart"), pageState);
    when(pageStorage.loadPage(any())).thenReturn(pageContext);
    profilePropertySetting = new ProfilePropertySetting("manager", "text", true, true, null, 1L, true, false, false, true, false, 1L, System.currentTimeMillis());
    event = new Event<>("profile-property-setting-updated", profilePropertyService, profilePropertySetting);
    try {
      managerPropertySettingUpdatedListener.onEvent(event);
    } catch (Exception e) {
      fail();
    }

    verify(pageStorage, times(2)).savePage(any());
  }

  @After
  public void tearDown() throws Exception {
    PORTAL_CONTAINER.close();
  }
}
