package org.exoplatform.social.metadata;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.metadata.model.MetadataType;

import lombok.Getter;

public class MetadataTypePlugin extends BaseComponentPlugin {

  private static final String METADATA_TYPE_PARAM_NAME        = "metadataType";

  private static final String SHAREABLE_PARAM_NAME            = "shareable";

  private static final String ALLOW_MULTIPLE_ITEMS_PARAM_NAME = "allowMultipleItemsPerObject";

  @Getter
  protected MetadataType      metadataType;

  @Getter
  protected boolean           shareable;

  @Getter
  protected boolean           allowMultipleItemsPerObject;

  public MetadataTypePlugin(InitParams params) {
    if (params != null) {
      if (params.containsKey(METADATA_TYPE_PARAM_NAME)) {
        this.metadataType = (MetadataType) params.getObjectParam(METADATA_TYPE_PARAM_NAME).getObject();
      }
      if (params.containsKey(SHAREABLE_PARAM_NAME)) {
        this.shareable = StringUtils.equals(params.getValueParam(SHAREABLE_PARAM_NAME).getValue(), "true");
      }
      if (params.containsKey(ALLOW_MULTIPLE_ITEMS_PARAM_NAME)) {
        this.allowMultipleItemsPerObject = StringUtils.equals(params.getValueParam(ALLOW_MULTIPLE_ITEMS_PARAM_NAME).getValue(),
                                                              "true");
      }
    }
    if (this.metadataType == null || this.metadataType.getId() == 0 || StringUtils.isBlank(this.metadataType.getName())) {
      throw new IllegalStateException("MetadataType is mandatory");
    }
  }

  @Override
  public String getName() {
    return this.metadataType.getName();
  }

  public long getId() {
    return this.metadataType.getId();
  }

}
