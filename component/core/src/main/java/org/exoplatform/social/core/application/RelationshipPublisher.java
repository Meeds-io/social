/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.application;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.processor.I18NActivityUtils;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * Publish a status update in activity streams of 2 confirmed relations.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class RelationshipPublisher extends RelationshipListenerPlugin {

  public enum TitleId {
    CONNECTION_REQUESTED,
    CONNECTION_CONFIRMED
  }

  public static final String USER_RELATION_CONFIRMED = "user_relation_confirmed";

  public static final String SENDER_PARAM = "SENDER";
  public static final String RECEIVER_PARAM = "RECEIVER";
  public static final String RELATIONSHIP_UUID_PARAM = "RELATIONSHIP_UUID";
  public static final String RELATIONSHIP_ACTIVITY_TYPE = "exosocial:relationship";
  public static final String NUMBER_OF_CONNECTIONS = "NUMBER_OF_CONNECTIONS";
  public static final String USER_ACTIVITIES_FOR_RELATIONSHIP = "USER_ACTIVITIES_FOR_RELATIONSHIP";
  public static final String USER_COMMENTS_ACTIVITY_FOR_RELATIONSHIP = "USER_COMMENTS_ACTIVITY_FOR_RELATIONSHIP";
  public static final String USER_DISPLAY_NAME_PARAM = "USER_DISPLAY_NAME_PARAM";

  private static final Log LOG = ExoLogger.getLogger(RelationshipPublisher.class);
  private ActivityManager  activityManager;

  private IdentityManager identityManager;

  public RelationshipPublisher(ActivityManager activityManager, IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  private ExoSocialActivity createNewComment(Identity userIdenity, String fullName) {
    ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
    comment.setType(USER_COMMENTS_ACTIVITY_FOR_RELATIONSHIP);
    String message = String.format("I'm now connected with %s", fullName);
    comment.setTitle(message);
    comment.setUserId(userIdenity.getId());

    I18NActivityUtils.addResourceKeyToProcess(comment, USER_RELATION_CONFIRMED);
    I18NActivityUtils.addResourceKey(comment, USER_RELATION_CONFIRMED, fullName);
    return comment;
  }
  
  private void updateActivity(ExoSocialActivity activity, int numberOfConnections) {
    String title = String.format("I'm now connected with %s user(s)", numberOfConnections);
    String titleId = "user_relations";
    Map<String,String> params = new HashMap<>();
    activity.setTitle(title);
    activity.setTitleId(null);
    activity.setTemplateParams(params);
    I18NActivityUtils.addResourceKey(activity, titleId, "" + numberOfConnections);
  }

  /**
   * Publish an activity on both user's stream to indicate their new connection
   */
  @Override
  public void confirmed(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    try {
      Identity sender = identityManager.getOrCreateUserIdentity(relationship.getSender().getRemoteId()); 
      Identity receiver = identityManager.getOrCreateUserIdentity(relationship.getReceiver().getRemoteId());

      String activityIdSender = getIdentityStorage().getProfileActivityId(sender.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
      int nbOfSenderConnections = getRelationShipManager().getConnections(sender).getSize();
      String activityIdReceiver = getIdentityStorage().getProfileActivityId(receiver.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
      int nbOfReceiverConnections = getRelationShipManager().getConnections(receiver).getSize();
      
      String fullNameReceiver = receiver.getProfile().getFullName();
      ExoSocialActivity senderComment = createNewComment(sender, fullNameReceiver);
      String fullNameSender = sender.getProfile().getFullName();
      ExoSocialActivity receiverComment = createNewComment(receiver, fullNameSender);
      
      if (activityIdSender != null) {
        ExoSocialActivity activitySender = activityManager.getActivity(activityIdSender);
        if (activitySender != null) {
          updateActivity(activitySender, nbOfSenderConnections);
          activityManager.updateActivity(activitySender);
          activityManager.saveComment(activitySender, senderComment);
        } else {
          activityIdSender = null;
        }
      }
      if (activityIdSender == null) {
        ExoSocialActivity newActivitySender = createNewActivity(nbOfSenderConnections);
        activityManager.saveActivityNoReturn(sender, newActivitySender);
        getIdentityStorage().updateProfileActivityId(sender, newActivitySender.getId(), Profile.AttachedActivityType.RELATIONSHIP);
        activityManager.saveComment(newActivitySender, senderComment);
      }
      
      if (activityIdReceiver != null) {
        ExoSocialActivity activityReceiver = activityManager.getActivity(activityIdReceiver);
        if (activityReceiver != null) {
          updateActivity(activityReceiver, nbOfReceiverConnections);
          activityManager.updateActivity(activityReceiver);
          activityManager.saveComment(activityReceiver, receiverComment);
        } else {
          activityIdReceiver = null;
        }
      }
      if (activityIdReceiver == null) {
        ExoSocialActivity newActivityReceiver = createNewActivity(nbOfReceiverConnections);
        activityManager.saveActivityNoReturn(receiver, newActivityReceiver);
        getIdentityStorage().updateProfileActivityId(receiver, newActivityReceiver.getId(), Profile.AttachedActivityType.RELATIONSHIP);
        activityManager.saveComment(newActivityReceiver, receiverComment);
      }
    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

  @Override
  public void removed(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    try {
      Identity sender = identityManager.getOrCreateUserIdentity(relationship.getSender().getRemoteId()); 
      Identity receiver = identityManager.getOrCreateUserIdentity(relationship.getReceiver().getRemoteId());

      String activityIdSender = getIdentityStorage().getProfileActivityId(sender.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
      int nbOfSenderConnections = getRelationShipManager().getConnections(sender).getSize();
      String activityIdReceiver = getIdentityStorage().getProfileActivityId(receiver.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
      int nbOfReceiverConnections = getRelationShipManager().getConnections(receiver).getSize();
      
      if (activityIdSender != null) {
        ExoSocialActivity activitySender = activityManager.getActivity(activityIdSender);
        if (activitySender != null) {
          updateActivity(activitySender, nbOfSenderConnections);
          activityManager.updateActivity(activitySender);
        }
      }
      
      if (activityIdReceiver != null) {
        ExoSocialActivity activityReceiver = activityManager.getActivity(activityIdReceiver);
        if (activityReceiver != null) {
          updateActivity(activityReceiver, nbOfReceiverConnections);
          activityManager.updateActivity(activityReceiver);
        }
      }

    } catch (Exception e) {
      LOG.debug("Failed to update relationship activity when remove connection : " + e.getMessage());
    }
  }

  private ExoSocialActivity createNewActivity(int nbOfConnections) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setType(USER_ACTIVITIES_FOR_RELATIONSHIP);
    updateActivity(activity, nbOfConnections);
    return activity;
  }

  private IdentityStorage getIdentityStorage() {
    return PortalContainer.getInstance().getComponentInstanceOfType(IdentityStorage.class);
  }
  
  private RelationshipManager getRelationShipManager() {
    return PortalContainer.getInstance().getComponentInstanceOfType(RelationshipManager.class);
  }
}