/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.listeners;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

public class SynchronizedUserProfileListener extends Listener<IDMExternalStoreImportService, Map<String, String>> {

  /**
   * Optional external store attribute holding the Base64 encoded profile
   * picture (LDAP {@code thumbnailPhoto}, {@code jpegPhoto}...). Only present
   * when the administrator mapped it with
   * {@code exo.ldap.users.attributes.profilePicture.mapping}.
   */
  public static final String PROFILE_PICTURE_ATTRIBUTE = "profilePicture";

  private static final Log LOG = ExoLogger.getLogger(SynchronizedUserProfileListener.class);

  /** Largest accepted picture, in bytes (AD bounds thumbnailPhoto to 100 KB). */
  private static final int MAX_PICTURE_BYTES = 1024 * 1024;

  /** Largest accepted picture width or height, in pixels. */
  private static final int MAX_PICTURE_SIDE = 2048;

  /** The only formats browsers render as an avatar. */
  private static final Set<String> ACCEPTED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif");

  @Override
  public void onEvent(Event<IDMExternalStoreImportService, Map<String, String>> event) throws Exception {
    Map<String, String> userPropertiesMap = event.getData();
    String userName = userPropertiesMap.get("username");
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    if (userIdentity != null) {
      Profile profile = userIdentity.getProfile();
      Map<String, Object> internalUserProperties = profile.getProperties();
      Map<String, Object> externalUserProperties = buildProfilePropertiesMap(userPropertiesMap);
      boolean isModified = false;
      Set<String> externalUserProfileAttributes = externalUserProperties.keySet();
      for (String externalUserProfileAttribute : externalUserProfileAttributes) {
        Object externalPropertyValue = externalUserProperties.getOrDefault(externalUserProfileAttribute, null);
        if ((!internalUserProperties.containsKey(externalUserProfileAttribute) && externalPropertyValue != null)
            || !Objects.equals(externalUserProperties.get(externalUserProfileAttribute),
                               internalUserProperties.get(externalUserProfileAttribute))) {
          // set the newly or modified property
          profile.setProperty(externalUserProfileAttribute, externalUserProperties.get(externalUserProfileAttribute));
          isModified = true;
        }
      }
      if (updateAvatar(identityManager, userIdentity, profile, userPropertiesMap.get(PROFILE_PICTURE_ATTRIBUTE))) {
        isModified = true;
      }
      if (isModified) {
        // broadcast changes
        identityManager.updateProfile(cleanProfilePropertySetting(profile), true);
      }
    }
  }

  /**
   * Sets the user avatar from the Base64 encoded picture of the external store,
   * only when it differs from the current one so that it isn't re-uploaded at
   * each synchronization.
   *
   * @return true when the profile avatar was changed
   */
  private boolean updateAvatar(IdentityManager identityManager, Identity identity, Profile profile, String encodedPicture) {
    if (StringUtils.isBlank(encodedPicture)) {
      return false;
    }
    byte[] picture;
    try {
      picture = Base64.getDecoder().decode(encodedPicture.trim());
    } catch (IllegalArgumentException e) {
      LOG.warn("Ignoring profile picture of user '{}' coming from external store: not a Base64 value",
               identity.getRemoteId());
      return false;
    }
    if (picture.length > MAX_PICTURE_BYTES) {
      LOG.warn("Ignoring profile picture of user '{}' coming from external store: larger than {} bytes",
               identity.getRemoteId(),
               MAX_PICTURE_BYTES);
      return false;
    }
    try {
      try (InputStream current = identityManager.getAvatarInputStream(identity)) {
        if (current != null && Arrays.equals(current.readAllBytes(), picture)) {
          // Unchanged: nothing to decode or upload
          return false;
        }
      }
      String mimeType = readImageMimeType(picture);
      if (mimeType == null) {
        LOG.warn("Ignoring profile picture of user '{}' coming from external store: not a readable JPEG, PNG or GIF "
            + "image of at most {} pixels a side", identity.getRemoteId(), MAX_PICTURE_SIDE);
        return false;
      }
      AvatarAttachment attachment = new AvatarAttachment(null,
                                                         "avatar",
                                                         mimeType,
                                                         new ByteArrayInputStream(picture),
                                                         System.currentTimeMillis());
      profile.setProperty(Profile.AVATAR, attachment);
      return true;
    } catch (Exception e) {
      LOG.warn("Error while setting profile picture of user '{}' coming from external store", identity.getRemoteId(), e);
      return false;
    }
  }

  /**
   * Reads the picture with the image decoder that recognizes it, checking its
   * dimensions before decoding it.
   *
   * @return its MIME type from the decoder's provider, or null when it is not
   *         a readable JPEG, PNG or GIF image of at most
   *         {@value #MAX_PICTURE_SIDE} pixels a side
   */
  private String readImageMimeType(byte[] picture) {
    try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(picture))) {
      Iterator<ImageReader> readers = input == null ? null : ImageIO.getImageReaders(input);
      if (readers == null || !readers.hasNext()) {
        return null;
      }
      ImageReader reader = readers.next();
      try {
        String[] mimeTypes = reader.getOriginatingProvider().getMIMETypes();
        if (mimeTypes == null || mimeTypes.length == 0 || !ACCEPTED_MIME_TYPES.contains(mimeTypes[0])) {
          return null;
        }
        reader.setInput(input);
        if (reader.getWidth(0) > MAX_PICTURE_SIDE || reader.getHeight(0) > MAX_PICTURE_SIDE) {
          return null;
        }
        reader.read(0);
        return mimeTypes[0];
      } finally {
        reader.dispose();
      }
    } catch (IOException | RuntimeException e) {
      return null;
    }
  }

  private Map<String, Object> buildProfilePropertiesMap(Map<String, String> externalUserPropertiesMap) {
    Map<String, Object> proprtiesMap = new HashMap<>();
    ProfilePropertyService profilePropertyService = CommonsUtils.getService(ProfilePropertyService.class);
    List<String> profilePropertySettingNames = profilePropertyService.getPropertySettingNames();
    for (Map.Entry<String, String> entry : externalUserPropertiesMap.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue();
      if (PROFILE_PICTURE_ATTRIBUTE.equals(name)) {
        // Used as the avatar only, never copied (nor indexed) as a profile property
        continue;
      }
      if (profilePropertySettingNames.contains(name)
          || (name.contains(".") && profilePropertySettingNames.contains(name.substring(0, name.indexOf('.'))))) {
        // check if the parent property exists
        ProfilePropertySetting propertySetting = name.contains(".") ? profilePropertyService.getProfileSettingByName(name.substring(0, name.indexOf('.'))) : null;
        // look for the property that has the full name including the dot "."
        if (propertySetting == null) {
          propertySetting = profilePropertyService.getProfileSettingByName(name);
        }
        if (propertySetting != null) {
          String propertyName = propertySetting.getPropertyName();
          List<String> systemMultivaluedFields = Arrays.asList("user", "phones", "ims");
          if (systemMultivaluedFields.contains(propertyName)) {
            List<Map<String, String>> maps;
            // child list is empty
            if (!proprtiesMap.containsKey(propertyName)) {
              maps = new ArrayList<>();
            } else {
              maps = (List<Map<String, String>>) proprtiesMap.get(propertyName);
            }
            Map<String, String> childProperty = new HashMap<>();
            childProperty.put("key", name);
            childProperty.put("value", value);
            maps.add(childProperty);
            proprtiesMap.put(propertyName, maps);
          } else {
            // check if the property is multivalued and its value matches the patern "["value one", "value two", "value three", etc ...]"
            if (propertySetting.isMultiValued() && value.matches("^\\[(.*)\\]$")) {
              List<Map<String, String>> multivaluedPropsList = new ArrayList<>();
              String[] valuesArray = value.substring(1, value.length() - 1).split(",");
              for (String valueString : valuesArray) {
                Map<String, String> map = new HashMap<>();
                if (StringUtils.isNotBlank(valueString)) {
                  map.put("value", valueString.trim());
                  multivaluedPropsList.add(map);
                }
              }
              proprtiesMap.put(name, multivaluedPropsList);
            } else {
              proprtiesMap.put(name, value);
            }
          }
        }
      }
    }
    return proprtiesMap;
  }
  /*
   *  Remove the old saved multivalued property as a map of string key-value pairs.
   */
  private Profile cleanProfilePropertySetting(Profile internalProfile){
    Iterator<Map.Entry<String, Object>> iterator = internalProfile.getProperties().entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      if (key.contains(".") && key.split(".").length < 2) {
        String parentfield = key.substring(0, key.indexOf('.'));
        if (internalProfile.getProperties().containsKey(parentfield)) {
          iterator.remove();
        }
      }
    }
    return internalProfile;
  }
}
