package org.exoplatform.social.core.listeners;

import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.storage.PageStorage;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)

public class ManagerPropertySettingUpdatedListenerTest {

  @Mock
  PageStorage pageStorage;

  @Mock
  ProfilePropertyService profilePropertyService;

  @Test
  public void testOnEvent() {
    ManagerPropertySettingUpdatedListener managerPropertySettingUpdatedListener = new ManagerPropertySettingUpdatedListener(pageStorage);
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
                                        List.of("/platform/administrators"),
                                        List.of("/platform/administrators"),
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

    verify(pageStorage, times(1)).savePage(any());
  }

}
