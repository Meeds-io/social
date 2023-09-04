package io.meeds.social.notification.rest;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.portal.rest.services.BaseRestServicesTestCase;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import io.meeds.social.notification.rest.WebNotificationRestService;

import javax.ws.rs.core.Response;

import org.mockito.Mock;
import static org.mockito.Mockito.*;

public class WebNotificationRestServiceTest extends BaseRestServicesTestCase {

  @Mock
  WebNotificationService webNotificationService;

  @Override
  protected Class<?> getComponentClass() {
    return WebNotificationRestService.class;
  }

  public void testUnauthorizedMarkAsRead() throws Exception {
    startSessionAs("john");

    webNotificationService = mock(WebNotificationService.class);
    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setTo("mary");
    when(webNotificationService.getNotificationInfo(anyString())).thenReturn(notificationInfo);

    WebNotificationRestService webNotificationRestService = new WebNotificationRestService(webNotificationService);
    Response response = webNotificationRestService.updateNotifications("markAsRead", "1");

    assertEquals(response.getStatus(), 401);

  }

  public void testAuthorizedMarkAsRead() throws Exception {
    startSessionAs("john");

    webNotificationService = mock(WebNotificationService.class);

    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setTo("john");

    when(webNotificationService.getNotificationInfo(anyString())).thenReturn(notificationInfo);

    WebNotificationRestService webNotificationRestService = new WebNotificationRestService(webNotificationService);
    Response response = webNotificationRestService.updateNotifications("markAsRead", "1");

    assertEquals(response.getStatus(), 204);

  }

  public void testUnauthorizedHide() throws Exception {
    startSessionAs("john");

    webNotificationService = mock(WebNotificationService.class);

    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setTo("mary");
    when(webNotificationService.getNotificationInfo(anyString())).thenReturn(notificationInfo);

    WebNotificationRestService webNotificationRestService = new WebNotificationRestService(webNotificationService);
    Response response = webNotificationRestService.updateNotifications("hide", "1");

    assertEquals(response.getStatus(), 401);

  }

  public void testAuthorizedHide() throws Exception {
    startSessionAs("john");

    webNotificationService = mock(WebNotificationService.class);

    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setTo("john");
    when(webNotificationService.getNotificationInfo(anyString())).thenReturn(notificationInfo);

    WebNotificationRestService webNotificationRestService = new WebNotificationRestService(webNotificationService);
    Response response = webNotificationRestService.updateNotifications("hide", "1");

    assertEquals(response.getStatus(), 204);

  }

  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

}
