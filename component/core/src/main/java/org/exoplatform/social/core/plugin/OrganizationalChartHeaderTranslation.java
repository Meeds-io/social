package org.exoplatform.social.core.plugin;

import io.meeds.social.translation.plugin.TranslationPlugin;

public class OrganizationalChartHeaderTranslation extends TranslationPlugin {

  public static final String ORGANIZATIONAL_CHART_OBJECT_TYPE = "organizationalChart";

  @Override
  public String getObjectType() {
    return ORGANIZATIONAL_CHART_OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(long objectId, String username) {
    return true;
  }

  @Override
  public boolean hasEditPermission(long objectId, String username) {
    return true;
  }

  @Override
  public long getAudienceId(long objectId) {
    return 0;
  }

  @Override
  public long getSpaceId(long objectId) {
    return 0;
  }
}
