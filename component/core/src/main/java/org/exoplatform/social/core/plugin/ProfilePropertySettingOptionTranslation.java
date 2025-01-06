package org.exoplatform.social.core.plugin;

import io.meeds.social.translation.plugin.TranslationPlugin;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;

public class ProfilePropertySettingOptionTranslation extends TranslationPlugin {

  private final UserACL      userACL;

  public static final String PROFILE_PROPERTY_SETTING_OPTION_OBJECT_TYPE = "propertySettingOption";

  public ProfilePropertySettingOptionTranslation(UserACL userACL) {
    this.userACL = userACL;
  }

  @Override
  public String getObjectType() {
    return PROFILE_PROPERTY_SETTING_OPTION_OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(long objectId, String username) throws ObjectNotFoundException {
    return userACL.isAdministrator(userACL.getUserIdentity(username));
  }

  @Override
  public boolean hasEditPermission(long objectId, String username) throws ObjectNotFoundException {
    return userACL.isAdministrator(userACL.getUserIdentity(username));
  }

  @Override
  public long getAudienceId(long objectId) throws ObjectNotFoundException {
    return 0;
  }

  @Override
  public long getSpaceId(long objectId) throws ObjectNotFoundException {
    return 0;
  }
}
