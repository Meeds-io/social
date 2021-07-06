package org.exoplatform.social.core.activity;

import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/*
 * A class to be able to inject system title ids and system activity/comment types
 * This will allow to determine if an activity/comment is editable
 */
public class ActivitySystemTypePlugin extends BaseComponentPlugin {

  public static final String SYSTEM_TITLE_IDS_PARAM = "system.titleIds";

  public static final String SYSTEM_TYPES_PARAM     = "system.types";

  private List<String>       systemActivityTypes    = null;

  private List<String>       systemActivityTitleIds = null;

  public ActivitySystemTypePlugin(InitParams initParams) {
    if (initParams != null) {
      if (initParams.containsKey(SYSTEM_TYPES_PARAM)) {
        systemActivityTypes = initParams.getValuesParam(SYSTEM_TYPES_PARAM).getValues();
      }
      if (initParams.containsKey(SYSTEM_TITLE_IDS_PARAM)) {
        systemActivityTitleIds = initParams.getValuesParam(SYSTEM_TITLE_IDS_PARAM).getValues();
      }
    }
  }

  public List<String> getSystemActivityTitleIds() {
    return systemActivityTitleIds;
  }

  public List<String> getSystemActivityTypes() {
    return systemActivityTypes;
  }
}
