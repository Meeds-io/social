package org.exoplatform.social.websocket.listenr;

import org.exoplatform.social.websocket.SpaceWebNotificationWebSocketService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;

/**
 * A listener that will be triggered synchronously after marking an item as
 * unread to transmit modifications to end users via WebSocket
 */
@Asynchronous
public class SpaceWebNotificationWebSocketEventListener extends Listener<SpaceWebNotificationItem, Long> {

  private PortalContainer                      container;

  private SpaceWebNotificationWebSocketService spaceWebNotificationWebSocketService;

  public SpaceWebNotificationWebSocketEventListener(PortalContainer container,
                                                    SpaceWebNotificationWebSocketService spaceWebNotificationWebSocketService) {
    this.container = container;
    this.spaceWebNotificationWebSocketService = spaceWebNotificationWebSocketService;
  }

  @Override
  public void onEvent(Event<SpaceWebNotificationItem, Long> event) throws Exception {
    RequestLifeCycle.begin(container);
    try {
      SpaceWebNotificationItem spaceWebNotificationItem = event.getSource();
      spaceWebNotificationWebSocketService.sendMessage(event.getEventName(), spaceWebNotificationItem);
    } finally {
      RequestLifeCycle.end();
    }
  }

}
