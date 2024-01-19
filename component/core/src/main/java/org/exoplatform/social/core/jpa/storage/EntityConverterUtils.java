package org.exoplatform.social.core.jpa.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.IdentityWithRelationship;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.entity.AppEntity;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.storage.entity.ProfileExperienceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity.PRIORITY;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity.REGISTRATION;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity.VISIBILITY;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity.Status;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;

public class EntityConverterUtils {

  public static final String DEFAULT_AVATAR = "DEFAULT_AVATAR";

  private static final Log    LOG            = ExoLogger.getLogger(EntityConverterUtils.class);

  public static Identity convertToIdentity(IdentityEntity entity) {
    return convertToIdentity(entity, true);
  }

  public static Identity convertToIdentity(IdentityEntity entity, boolean mapDeleted) {
    if (entity.isDeleted() && !mapDeleted) {
      return null;
    }

    Identity identity = new Identity(entity.getStringId());
    mapToIdentity(entity, identity);
    return identity;
  }

  public static void mapToIdentity(IdentityEntity entity, Identity identity) {
    identity.setProviderId(entity.getProviderId());
    identity.setRemoteId(entity.getRemoteId());
    identity.setProfileLoader(() -> convertToProfile(entity, identity));
    identity.setEnable(entity.isEnabled());
    identity.setDeleted(entity.isDeleted());
  }

  public static Profile convertToProfile(IdentityEntity entity, Identity identity) {
    Profile p = new Profile(identity);
    p.setId(String.valueOf(identity.getId()));
    mapToProfile(entity, p);
    if (OrganizationIdentityProvider.NAME.equals(identity.getProviderId()) && p.getProperty(Profile.USERNAME) == null) {
      p.getProperties().put(Profile.USERNAME, identity.getRemoteId());
    }
    return p;
  }

  public static void mapToProfile(IdentityEntity entity, Profile p) {
    Map<String, String> properties = entity.getProperties();
    
    Map<String, Object> props = p.getProperties();
    Identity identity = p.getIdentity();
    Long avatarLastUpdated = null;
    if (entity.getAvatarFileId() != null && entity.getAvatarFileId() > 0) {
      avatarLastUpdated = getFileLastUpdated(entity.getAvatarFileId());
      p.setDefaultAvatar(isDefaultAvatar(entity.getAvatarFileId()));

    } else if (identity.isUser() || identity.isSpace()) {
      // Allow to generate new default avatar file
      // for user or space
      avatarLastUpdated = System.currentTimeMillis();
    }
    Long bannerLastUpdated = null;
    if (entity.getBannerFileId() != null && entity.getBannerFileId() > 0) {
      bannerLastUpdated = getFileLastUpdated(entity.getBannerFileId());
    }
    if (!identity.isUser() && !identity.isSpace()) {
      p.setUrl(properties.get(Profile.URL));
      p.setAvatarUrl(LinkProvider.buildAvatarURL(identity.getProviderId(),
                                                 identity.getRemoteId(),
                                                 avatarLastUpdated));
      p.setBannerUrl(LinkProvider.buildBannerURL(identity.getProviderId(),
                                                 identity.getRemoteId(),
                                                 bannerLastUpdated));
    } else {
      String remoteId = entity.getRemoteId();
      if (identity.isUser()) {
        p.setUrl(LinkProvider.getUserProfileUri(remoteId));
      } else if (identity.isSpace()) {
        p.setUrl(LinkProvider.getSpaceUri(remoteId));
      }
      if (avatarLastUpdated != null) {
        p.setAvatarLastUpdated(avatarLastUpdated);
      }
      if (bannerLastUpdated != null) {
        p.setBannerLastUpdated(bannerLastUpdated);
      }
      p.setAvatarUrl(LinkProvider.buildAvatarURL(identity.getProviderId(),
                                                 identity.isUser() ? identity.getId() : identity.getRemoteId(),
                                                 identity.isUser(),
                                                 avatarLastUpdated));
      p.setBannerUrl(LinkProvider.buildBannerURL(identity.getProviderId(),
                                                 identity.isUser() ? identity.getId() : identity.getRemoteId(),
                                                 identity.isUser(),
                                                 bannerLastUpdated));
    }
    StringBuilder skills = new StringBuilder();
    StringBuilder positions = new StringBuilder();
    Set<ProfileExperienceEntity> experiences = entity.getExperiences();
    if (experiences != null && !experiences.isEmpty()) {
      List<Map<String, Object>> xpData = new ArrayList<>();
      for (ProfileExperienceEntity exp : experiences){
        Map<String, Object> xpMap = new HashMap<>();
        if (exp.getSkills() != null && !exp.getSkills().isEmpty()) {
          skills.append(exp.getSkills()).append(",");
        }
        if (exp.getPosition() != null && !exp.getPosition().isEmpty()) {
          positions.append(exp.getPosition()).append(",");
        }
        xpMap.put(Profile.EXPERIENCES_ID, String.valueOf(exp.getId()));
        xpMap.put(Profile.EXPERIENCES_SKILLS, exp.getSkills());
        xpMap.put(Profile.EXPERIENCES_POSITION, exp.getPosition());
        xpMap.put(Profile.EXPERIENCES_START_DATE, exp.getStartDate());
        xpMap.put(Profile.EXPERIENCES_END_DATE, exp.getEndDate());
        xpMap.put(Profile.EXPERIENCES_COMPANY, exp.getCompany());
        xpMap.put(Profile.EXPERIENCES_DESCRIPTION, exp.getDescription());
        xpMap.put(Profile.EXPERIENCES_IS_CURRENT, exp.isCurrent());
        xpData.add(xpMap);
      }
      props.put(Profile.EXPERIENCES, xpData);
    }
    if (skills.length() > 0) {
      skills.deleteCharAt(skills.length() - 1);
      props.put(Profile.EXPERIENCES_SKILLS, skills.toString());
    }
    if (positions.length() > 0) {
      positions.deleteCharAt(positions.length() - 1);
      props.put(Profile.POSITION, positions.toString());
    }
    
    if (properties != null && properties.size() > 0) {
      for (Entry<String, String> entry : properties.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        boolean isJsonArray = false ;
        JSONArray arr = null ;
        try {
          arr = new JSONArray(value);
          isJsonArray = true;
        } catch (Exception ex) {
          // Ignore this exception
        }
        if (Profile.CONTACT_IMS.equals(key) || Profile.CONTACT_PHONES.equals(key) || Profile.CONTACT_URLS.equals(key) || isJsonArray) {
          List<Map<String, String>> list = new ArrayList<>();
          if (arr != null) {
            try {
              for (int i = 0; i < arr.length(); i++) {
                Map<String, String> map = new HashMap<>();
                JSONObject json = arr.getJSONObject(i);
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                  String k = keys.next();
                  map.put(k, json.optString(k));
                }
                list.add(map);
              }
            } catch (JSONException ex) {
              // Ignore this exception
            }
          }
          props.put(key, list);

        } else if (!Profile.URL.equals(key)) {
          props.put(key, value);
        }
      }
    }

    p.setCreatedTime(entity.getCreatedDate().getTime());
    p.setLastLoaded(System.currentTimeMillis());
  }

  public static void mapToEntity(Identity identity, IdentityEntity entity) {
    entity.setProviderId(identity.getProviderId());
    entity.setRemoteId(identity.getRemoteId());
    entity.setEnabled(identity.isEnable());
    entity.setDeleted(identity.isDeleted());
  }

  public static long parseId(String id) {
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  public static List<IdentityWithRelationship> convertToIdentitiesWithRelationship(ListAccess<Entry<IdentityEntity, ConnectionEntity>> list, int offset, int limit) {
    try {
      if (list == null) {
        return Collections.emptyList();
      }

      Entry<IdentityEntity, ConnectionEntity>[] entities = list.load(offset, limit);
      
      List<IdentityWithRelationship> result = new ArrayList<>(limit);
      for (Entry<IdentityEntity, ConnectionEntity> tuple : entities) {
        IdentityEntity identityEntity = (IdentityEntity) tuple.getKey();
        ConnectionEntity connectionEntity = (ConnectionEntity) tuple.getValue();

        IdentityWithRelationship identityWithRelationship = new IdentityWithRelationship(identityEntity.getStringId());
        mapToIdentity(identityEntity, identityWithRelationship);

        Relationship relationship = convertRelationshipItemToRelationship(connectionEntity);
        identityWithRelationship.setRelationship(relationship);

        result.add(identityWithRelationship);
      }
      return result;
    } catch (Exception ex) {
      return Collections.emptyList();
    }
  }

  public static List<Identity> convertToIdentities(ListAccess<IdentityEntity> list, long offset, long limit) {
    try {
      return convertToIdentities(list.load((int)offset, (int)limit));
    } catch (Exception ex) {
      return Collections.emptyList();
    }
  }

  public static List<Identity> convertToIdentities(IdentityEntity[] entities) {
    if (entities == null || entities.length == 0) {
      return Collections.emptyList();
    }

    List<Identity> result = new ArrayList<>(entities.length);
    for (IdentityEntity entity : entities) {
      Identity idt = convertToIdentity(entity);
      if (idt != null) {
        result.add(idt);
      }
    }
    return result;
  }

  public static SpaceEntity buildFrom(Space space, SpaceEntity spaceEntity) {
    spaceEntity.setApp(AppEntity.parse(space.getApp()));
    if (space.getAvatarLastUpdated() != null) {
      spaceEntity.setAvatarLastUpdated(space.getAvatarLastUpdated() > 0 ? new Date(space.getAvatarLastUpdated()) : null);
    } else {
      spaceEntity.setAvatarLastUpdated(null);
    }
    if (space.getBannerLastUpdated() != null) {
      spaceEntity.setBannerLastUpdated(space.getBannerLastUpdated() > 0 ? new Date(space.getBannerLastUpdated()) : null);
    } else {
      spaceEntity.setBannerLastUpdated(null);
    }
    spaceEntity.setCreatedDate(space.getCreatedTime() > 0 ? new Date(space.getCreatedTime()) : new Date());
    spaceEntity.setDescription(space.getDescription());
    spaceEntity.setTemplate(space.getTemplate());
    spaceEntity.setDisplayName(space.getDisplayName());
    spaceEntity.setGroupId(space.getGroupId());
    spaceEntity.setPrettyName(space.getPrettyName());
    PRIORITY priority = null;
    if (Space.HIGH_PRIORITY.equals(space.getPriority())) {
      priority = PRIORITY.HIGH;
    } else if (Space.INTERMEDIATE_PRIORITY.equals(space.getPriority())) {
      priority = PRIORITY.INTERMEDIATE;
    } else if (Space.LOW_PRIORITY.equals(space.getPriority())) {
      priority = PRIORITY.LOW;
    }
    spaceEntity.setPriority(priority);
    if (space.getRegistration() != null) {
      spaceEntity.setRegistration(REGISTRATION.valueOf(space.getRegistration().toUpperCase()));
    }
    spaceEntity.setUrl(space.getUrl());
    VISIBILITY visibility = null;
    if (space.getVisibility() != null) {
      visibility = VISIBILITY.valueOf(space.getVisibility().toUpperCase());
    }
    spaceEntity.setVisibility(visibility);
    buildMembers(space, spaceEntity);
    return spaceEntity;
  }

  public static Relationship convertRelationshipItemToRelationship(ConnectionEntity item) {
    if (item == null) return null;
    //
    Relationship relationship = new Relationship(Long.toString(item.getId()));
    relationship.setId(String.valueOf(item.getId()));
    relationship.setSender(convertToIdentity(item.getSender()));
    relationship.setReceiver(convertToIdentity(item.getReceiver()));
    relationship.setStatus(item.getStatus());
    return relationship;
  }
  
  private static Long getFileLastUpdated(Long fileId) {
    FileService fileService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(FileService.class);
    if (fileService != null) {
      FileInfo fileInfo = fileService.getFileInfo(fileId);
      if (fileInfo != null && fileInfo.getUpdatedDate() != null) {
        return fileInfo.getUpdatedDate().getTime();
      }
      return null;
    } else {
      LOG.warn("File service is null");
      return null;
    }
  }

  private static boolean isDefaultAvatar(Long fileId) {
    FileService fileService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(FileService.class);
    FileInfo fileInfo = fileService.getFileInfo(fileId);
    return fileInfo != null && DEFAULT_AVATAR.equals(fileInfo.getName());
  }

  private static String[] getPendingMembersId(SpaceEntity spaceEntity) {
    return getUserIds(spaceEntity, Status.PENDING);
  }

  private static String[] getInvitedMembersId(SpaceEntity spaceEntity) {
    return getUserIds(spaceEntity, Status.INVITED);
  }

  private static String[] getMembersId(SpaceEntity spaceEntity) {
    return getUserIds(spaceEntity, Status.MEMBER);
  }

  private static String[] getManagerMembersId(SpaceEntity spaceEntity) {
    return getUserIds(spaceEntity, Status.MANAGER);
  }

  private static void buildMembers(Space space, SpaceEntity spaceEntity) {
    Set<SpaceMemberEntity> invited = getMembers(spaceEntity, Status.INVITED);
    merge(spaceEntity, invited, space.getInvitedUsers(), Status.INVITED);

    Set<SpaceMemberEntity> manager = getMembers(spaceEntity, Status.MANAGER);
    merge(spaceEntity, manager, space.getManagers(), Status.MANAGER);

    Set<SpaceMemberEntity> member = getMembers(spaceEntity, Status.MEMBER);
    merge(spaceEntity, member, space.getMembers(), Status.MEMBER);
    
    Set<SpaceMemberEntity> redactor = getMembers(spaceEntity, Status.REDACTOR);
    merge(spaceEntity, redactor, space.getRedactors(), Status.REDACTOR);
    
    Set<SpaceMemberEntity> publisher = getMembers(spaceEntity, Status.PUBLISHER);
    merge(spaceEntity, publisher, space.getPublishers(), Status.PUBLISHER);

    Set<SpaceMemberEntity> pending = getMembers(spaceEntity, Status.PENDING);
    merge(spaceEntity, pending, space.getPendingUsers(), Status.PENDING);
  }

  private static void merge(SpaceEntity spaceEntity, Set<SpaceMemberEntity> spaceMembers, String[] userIds, Status status) {
    Set<String> ids = new HashSet<>(userIds != null ? Arrays.asList(userIds) : Collections.<String> emptyList());

    Iterator<SpaceMemberEntity> mems = spaceMembers.iterator();
    while (mems.hasNext()) {
      SpaceMemberEntity mem = mems.next();
      String id = mem.getUserId();

      if (ids.contains(mem.getUserId())) {
        ids.remove(id);
      } else {
        spaceEntity.getMembers().remove(mem);
      }
    }

    for (String id : ids) {
      if (StringUtils.isNotBlank(id)) {
        spaceEntity.getMembers().add(new SpaceMemberEntity(spaceEntity, id, status));
      }
    }
  }

  private static Set<SpaceMemberEntity> getMembers(SpaceEntity spaceEntity, Status status) {
    Set<SpaceMemberEntity> mems = new HashSet<>();
    for (SpaceMemberEntity mem : spaceEntity.getMembers()) {
      if (mem.getStatus().equals(status)) {
        mems.add(mem);
      }
    }
    return mems;
  }

  private static String[] getUserIds(SpaceEntity spaceEntity, Status status) {
    List<String> ids = new LinkedList<>();
    for (SpaceMemberEntity mem : getMembers(spaceEntity, status)) {
      ids.add(mem.getUserId());
    }
    return ids.toArray(new String[ids.size()]);
  }

}
