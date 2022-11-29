package org.exoplatform.social.websocket;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cometd.bayeux.server.ServerChannel;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.websocket.entity.WebSocketMessage;

public class SpaceWebNotificationWebSocketService {

  private static final Log      LOG            = ExoLogger.getLogger(SpaceWebNotificationWebSocketService.class);

  public static final String    COMETD_CHANNEL = "/spaceWebNotification/unread";

  private ContinuationService   continuationService;

  private EXoContinuationBayeux continuationBayeux;

  private SpaceService          spaceService;

  private String                cometdContextName;

  public SpaceWebNotificationWebSocketService(SpaceService spaceService,
                                              ContinuationService continuationService,
                                              EXoContinuationBayeux continuationBayeux) {

    this.spaceService = spaceService;
    this.continuationService = continuationService;
    this.cometdContextName = continuationBayeux.getCometdContextName();
  }

  /**
   * Propagate an event from Backend to frontend to add dynamism in pages
   * 
   * @param wsEventName event name that will allow Browser to distinguish which
   *          behavior to adopt in order to update UI
   * @param spaceWebNotificationItem The unread item object
   */
  
  public void sendMessage(String wsEventName, SpaceWebNotificationItem spaceWebNotificationItem) {
    Set<String> recipientUsers = new HashSet<>();
    long spaceId = spaceWebNotificationItem.getSpaceId();
    Space space = spaceService.getSpaceById(Long.toString(spaceId));
    if (space == null) {
      LOG.warn("Space with id " + spaceId + " wasn't found");
      return;
    }
    Collections.addAll(recipientUsers, space.getMembers());
    sendMessage(wsEventName, recipientUsers, spaceWebNotificationItem);

  }

  public void sendMessage(String wsEventName, Collection<String> recipientUsers, Object... params) {
    WebSocketMessage messageObject = new WebSocketMessage(wsEventName, params);
    String message = messageObject.toString();
    for (String recipientUser : recipientUsers) {
      if (continuationService.isPresent(recipientUser)) {
        continuationService.sendMessage(recipientUser, COMETD_CHANNEL, message);
      }
    }
  }

  /**
   * @return 'cometd' webapp context name
   */
  public String getCometdContextName() {
    return cometdContextName;
  }

  /**
   * Generate a cometd Token for each user
   * 
   * @param username user name for whom the token will be generated
   * @return generated cometd token
   */
  public String getUserToken(String username) {
    try {
      return continuationService.getUserToken(username);
    } catch (Exception e) {
      LOG.warn("Could not retrieve continuation token for user " + username, e);
      return "";
    }
  }
}
