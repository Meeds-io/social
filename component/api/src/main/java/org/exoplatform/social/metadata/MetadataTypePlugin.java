package org.exoplatform.social.metadata;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.metadata.model.MetadataType;

public class MetadataTypePlugin extends BaseComponentPlugin {

  private static final String METADATA_TYPE_PARAM_NAME = "metadataType";

  protected MetadataType      metadataType;

  public MetadataTypePlugin(InitParams params) {
    if (params != null && params.containsKey(METADATA_TYPE_PARAM_NAME)) {
      this.metadataType = (MetadataType) params.getObjectParam(METADATA_TYPE_PARAM_NAME).getObject();
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

  public MetadataType getMetadataType() {
    return metadataType;
  }

}
