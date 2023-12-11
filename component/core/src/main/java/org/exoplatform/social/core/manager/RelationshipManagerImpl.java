/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.ConnectionFilterListAccess;
import org.exoplatform.social.core.identity.ConnectionListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.RelationshipLifeCycle;
import org.exoplatform.social.core.relationship.RelationshipListener;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.storage.RelationshipStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;

/**
 * The Class RelationshipManager implements RelationshipManager without caching.
 * 
 * @since   Nov 24, 2010
 * @version 1.2.0-GA
 */
public class RelationshipManagerImpl implements RelationshipManager {

  /**
   * The default offset when get list identities with connection list access.
   * 
   * @since 1.2.0-Beta3
   */
  protected static final int      OFFSET    = 0;

  /**
   * The default limit when get list identities with connection list access.
   * 
   * @since 1.2.0-Beta3
   */
  protected static final int      LIMIT     = 200;

  private RelationshipLifeCycle lifeCycle = new RelationshipLifeCycle();

  private RelationshipStorage   relationshipStorage;

  private IdentityStorage       identityStorage;

  private IdentityManager       identityManager;

  public RelationshipManagerImpl(IdentityManager identityManager,
                                 IdentityStorage identityStorage,
                                 RelationshipStorage relationshipStorage) {
    this.identityManager = identityManager;
    this.relationshipStorage = relationshipStorage;
    this.identityStorage = identityStorage;
  }

  @Override
  public Relationship inviteToConnect(Identity invitingIdentity, Identity invitedIdentity) {
    Relationship relationship = get(invitingIdentity, invitedIdentity);
    if (relationship == null || Type.IGNORED.equals(relationship.getStatus())) {
      relationship = new Relationship(invitingIdentity, invitedIdentity);
      relationship.setStatus(Type.PENDING);
      saveRelationship(relationship);
      lifeCycle.relationshipRequested(this, relationship);
    }
    return relationship;
  }

  @Override
  public Relationship confirm(Identity invitedIdentity, Identity invitingIdentity) {
    Relationship relationship = get(invitedIdentity, invitingIdentity);
    if (relationship != null && relationship.getStatus() == Relationship.Type.PENDING) {
      relationship.setStatus(Relationship.Type.CONFIRMED);
      saveRelationship(relationship);
      lifeCycle.relationshipConfirmed(this, relationship);
    }
    return relationship;
  }

  @Override
  public void deny(Identity invitedIdentity, Identity invitingIdentity) {
    Relationship relationship = get(invitedIdentity, invitingIdentity);
    if (relationship != null) {
      relationshipStorage.removeRelationship(relationship);
      lifeCycle.relationshipDenied(this, relationship);
    }
  }

  @Override
  public Relationship ignore(Identity sender, Identity receiver) {
    Relationship relationship = get(sender, receiver);
    if (relationship == null || !Type.IGNORED.equals(relationship.getStatus())) {
      relationship = new Relationship(sender, receiver);
      relationship.setStatus(Type.IGNORED);
      saveRelationship(relationship);
      lifeCycle.relationshipIgnored(this, relationship);
    }
    return relationship;
  }

  @Override
  public void delete(Relationship existingRelationship) {
    relationshipStorage.removeRelationship(existingRelationship);
    lifeCycle.relationshipRemoved(this, existingRelationship);
  }

  @Override
  public List<Identity> findRelationships(Identity ownerIdentity, Type relationshipType) throws RelationshipStorageException {
    List<Relationship> allRelationships = getAll(ownerIdentity, relationshipType, null);
    List<Identity> identities = new ArrayList<>();
    if (allRelationships == null || allRelationships.isEmpty()) {
      return identities;
    }
    for (Relationship relationship : allRelationships) {
      identities.add(relationship.getPartner(ownerIdentity));
    }
    return identities;
  }

  @Override
  public Relationship get(Identity identity1, Identity identity2) throws RelationshipStorageException {
    return relationshipStorage.getRelationship(identity1, identity2);
  }

  @Override
  public Relationship get(String relationshipId) {
    Relationship relationship = null;
    try {
      relationship = relationshipStorage.getRelationship(relationshipId);
    } catch (Exception e) {
      return null;
    }
    return relationship;
  }

  @Override
  public List<Relationship> getAll(Identity identity) throws RelationshipStorageException {
    return getRelationships(identity, Type.ALL, null);
  }

  @Override
  public List<Relationship> getAll(Identity identity, List<Identity> identities) throws RelationshipStorageException {
    return getRelationships(identity, Type.ALL, identities);
  }

  @Override
  public List<Relationship> getAll(Identity identity, Relationship.Type type,
                                   List<Identity> identities) throws RelationshipStorageException {
    return getRelationships(identity, type, identities);
  }

  @Override
  public List<Relationship> getAllRelationships(Identity identity) throws RelationshipStorageException {
    return getAll(identity);
  }

  @Override
  public ListAccess<Identity> getAllWithListAccess(Identity existingIdentity) {
    return new ConnectionListAccess(relationshipStorage, existingIdentity, ConnectionListAccess.Type.ALL);
  }

  @Override
  public List<Relationship> getConfirmed(Identity identity) throws RelationshipStorageException {
    return getRelationships(identity, Relationship.Type.CONFIRMED, null);
  }

  @Override
  public List<Relationship> getConfirmed(Identity identity, List<Identity> identities) throws RelationshipStorageException {
    return getRelationships(identity, Relationship.Type.CONFIRMED, identities);
  }

  @Override
  public ListAccess<Identity> getConnections(Identity identity) {
    return (new ConnectionListAccess(relationshipStorage, identity, ConnectionListAccess.Type.CONNECTION));
  }

  @Override
  public ListAccess<Identity> getConnectionsByFilter(Identity existingIdentity, ProfileFilter profileFilter) {
    profileFilter = checkSortingOnProfileFilter(profileFilter);
    return new ConnectionFilterListAccess(identityStorage,
                                          relationshipStorage,
                                          existingIdentity,
                                          profileFilter,
                                          ConnectionFilterListAccess.Type.PROFILE_FILTER_CONNECTION);
  }

  @Override
  public int getConnectionsInCommonCount(Identity currentUserIdentity, Identity identity) {
    return relationshipStorage.getConnectionsInCommonCount(currentUserIdentity, identity);
  }

  @Override
  public Type getConnectionStatus(Identity fromIdentity, Identity toIdentity) throws Exception {
    return getStatus(fromIdentity, toIdentity);
  }

  @Override
  public List<Relationship> getContacts(Identity identity) throws RelationshipStorageException {
    return getAll(identity, Type.CONFIRMED, null);
  }

  @Override
  public List<Relationship> getContacts(Identity currIdentity, List<Identity> identities) throws RelationshipStorageException {
    return getAll(currIdentity, Type.CONFIRMED, identities);
  }

  @Override
  public List<Identity> getIdentities(Identity id) throws Exception {
    return Arrays.asList(getConnections(id).load(OFFSET, LIMIT));
  }

  @Override
  public List<Relationship> getIncoming(Identity receiver) throws RelationshipStorageException {
    return getReceiver(receiver, Relationship.Type.PENDING, null);
  }

  @Override
  public List<Relationship> getIncoming(Identity receiver, List<Identity> identities) throws RelationshipStorageException {
    return getReceiver(receiver, Relationship.Type.PENDING, identities);
  }

  @Override
  public ListAccess<Identity> getIncomingByFilter(Identity existingIdentity, ProfileFilter profileFilter) {
    profileFilter = checkSortingOnProfileFilter(profileFilter);
    return new ConnectionFilterListAccess(identityStorage,
                                          relationshipStorage,
                                          existingIdentity,
                                          profileFilter,
                                          ConnectionFilterListAccess.Type.PROFILE_FILTER_INCOMMING);
  }

  @Override
  public ListAccess<Identity> getIncomingWithListAccess(Identity existingIdentity) {
    return new ConnectionListAccess(relationshipStorage, existingIdentity, ConnectionListAccess.Type.INCOMING);
  }

  @Override
  public List<Identity> getLastConnections(Identity identity, int limit) {
    return relationshipStorage.getLastConnections(identity, limit);
  }

  @Override
  public ListAccess<Identity> getOutgoing(Identity existingIdentity) {
    return new ConnectionListAccess(relationshipStorage, existingIdentity, ConnectionListAccess.Type.OUTGOING);
  }

  @Override
  public ListAccess<Identity> getOutgoingByFilter(Identity existingIdentity, ProfileFilter profileFilter) {
    profileFilter = checkSortingOnProfileFilter(profileFilter);
    return new ConnectionFilterListAccess(identityStorage,
                                          relationshipStorage,
                                          existingIdentity,
                                          profileFilter,
                                          ConnectionFilterListAccess.Type.PROFILE_FILTER_OUTGOING);
  }

  @Override
  public List<Relationship> getPending(Identity sender) throws RelationshipStorageException {
    return getSender(sender, Relationship.Type.PENDING, null);
  }

  @Override
  public List<Relationship> getPending(Identity sender, List<Identity> identities) throws RelationshipStorageException {
    return getSender(sender, Relationship.Type.PENDING, identities);
  }

  @Override
  public List<Relationship> getPendingRelationships(Identity identity) throws RelationshipStorageException {
    return getPending(identity);
  }

  @Override
  public List<Relationship> getPendingRelationships(Identity identity, boolean toConfirm) throws RelationshipStorageException {
    return getAll(identity, Type.PENDING, null);
  }

  @Override
  public List<Relationship> getPendingRelationships(Identity currIdentity, List<Identity> identities,
                                                    boolean toConfirm) throws RelationshipStorageException {
    return getAll(currIdentity, Type.PENDING, identities);
  }

  @Override
  public Relationship getRelationship(Identity sender, Identity receiver) throws RelationshipStorageException {
    return get(sender, receiver);
  }

  @Override
  public Relationship getRelationshipById(String id) throws RelationshipStorageException {
    return get(id);
  }

  @Override
  public List<Relationship> getRelationshipsByIdentityId(String id) throws RelationshipStorageException {
    return getAll(identityManager.getIdentity(id));
  }

  @Override
  public List<Relationship> getRelationshipsByStatus(Identity identity, Relationship.Type type, int offset, int limit) {
    return relationshipStorage.getRelationshipsByStatus(identity, type, offset, limit);
  }

  @Override
  public int getRelationshipsCountByStatus(Identity identity, Relationship.Type type) {
    return relationshipStorage.getRelationshipsCountByStatus(identity, type);
  }

  @Override
  public Type getRelationshipStatus(Relationship rel, Identity id) {
    return rel.getStatus();
  }

  @Override
  public Relationship.Type getStatus(Identity identity1, Identity identity2) throws RelationshipStorageException {
    Relationship relationship = get(identity1, identity2);
    return relationship == null ? null : relationship.getStatus();
  }

  @Override
  public void confirm(Relationship relationship) throws RelationshipStorageException {
    confirm(relationship.getReceiver(), relationship.getSender());
  }

  @Override
  public void deny(Relationship relationship) throws RelationshipStorageException {
    deny(relationship.getReceiver(), relationship.getSender());
  }

  @Override
  public Map<Identity, Integer> getSuggestions(Identity identity, int offset, int limit) { // NOSONAR
    Map<Identity, Integer> result = relationshipStorage.getSuggestions(identity, -1, -1, offset + limit);
    if (result != null && !result.isEmpty()) {
      if (offset > 0) {
        result = new LinkedHashMap<>(result);
        int o = 0;
        for (Iterator<Map.Entry<Identity, Integer>> it = result.entrySet().iterator(); it.hasNext() && o < offset; o++) {
          it.next();
          it.remove();
        }
      }
      if (result.size() > limit) {
        result = new LinkedHashMap<>(result);
        int i = 0;
        for (Iterator<Map.Entry<Identity, Integer>> it = result.entrySet().iterator(); it.hasNext(); i++) {
          it.next();
          if (i >= limit)
            it.remove();
        }
      }
    }
    return result;
  }

  @Override
  public Map<Identity, Integer> getSuggestions(Identity identity, int maxConnections,
                                               int maxConnectionsToLoad, int maxSuggestions) {
    return relationshipStorage.getSuggestions(identity,
                                              maxConnections,
                                              maxConnectionsToLoad,
                                              maxSuggestions);
  }

  @Override
  public void ignore(Relationship relationship) throws RelationshipStorageException {
    ignore(relationship.getSender(), relationship.getReceiver());
  }

  @Override
  public Relationship invite(Identity sender, Identity receiver) throws RelationshipStorageException {
    return inviteToConnect(sender, receiver);
  }

  @Override
  public void remove(Relationship relationship) throws RelationshipStorageException {
    delete(relationship);
  }

  /**
   * Adds a RelationshipListenerPlugin.
   *
   * @param plugin {@link RelationshipListenerPlugin}
   */
  public void addListenerPlugin(RelationshipListenerPlugin plugin) {
    registerListener(plugin);
  }

  /**
   * Registers a RelationshipLister.
   *
   * @param listener {@link RelationshipListener}
   */
  public void registerListener(RelationshipListener listener) {
    lifeCycle.addListener(listener);
  }

  /**
   * Unregisters a RelationshipLister.
   *
   * @param listener {@link RelationshipListener}
   */
  public void unregisterListener(RelationshipListener listener) {
    lifeCycle.removeListener(listener);
  }

  /**
   * Get relationships by identity and status from cache or activityStorage
   * 
   * @param  receiver
   * @param  identities
   * @return                              list of relationship
   * @throws RelationshipStorageException
   */
  protected List<Relationship> getReceiver(Identity receiver, Relationship.Type type,
                                           List<Identity> identities) throws RelationshipStorageException {
    return relationshipStorage.getReceiverRelationships(receiver, type, identities);
  }

  /**
   * Get relationships by identity and status from cache or activityStorage
   * 
   * @param  identity
   * @param  identities
   * @return                              list of relationship
   * @throws RelationshipStorageException
   */
  protected List<Relationship> getRelationships(Identity identity, Relationship.Type type,
                                                List<Identity> identities) throws RelationshipStorageException {
    return relationshipStorage.getRelationships(identity, type, identities);
  }

  /**
   * Get relationships by identity and status and identities from
   * activityStorage
   * 
   * @param  sender
   * @return                              list of relationship
   * @throws RelationshipStorageException
   */
  protected List<Relationship> getSender(Identity sender, Relationship.Type type,
                                         List<Identity> identities) throws RelationshipStorageException {
    return relationshipStorage.getSenderRelationships(sender, type, identities);
  }

  private ProfileFilter checkSortingOnProfileFilter(ProfileFilter profileFilter) {
    if (profileFilter == null) {
      profileFilter = new ProfileFilter();
    }
    if (profileFilter.isSortingEmpty()) {
      profileFilter.setSorting(identityManager.getDefaultSorting());
    }
    return profileFilter;
  }

  private void saveRelationship(Relationship relationship) throws RelationshipStorageException {
    String senderId = relationship.getSender().getId();
    String receiverId = relationship.getReceiver().getId();
    if (senderId.equals(receiverId)) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.FAILED_TO_SAVE_RELATIONSHIP,
                                             "the two identity are the same");
    }
    relationshipStorage.saveRelationship(relationship);
  }

}
