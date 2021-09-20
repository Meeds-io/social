package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.MetadataItemEntity;
import org.exoplatform.social.metadata.model.MetadataItem;

public class MetadataItemDAO extends GenericDAOJPAImpl<MetadataItem, Long> {

  public List<MetadataItemEntity> getMetadatasByObject(String objectType, String objectId) {
    TypedQuery<MetadataItemEntity> query = getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadatasByObject",
                                                                               MetadataItemEntity.class);
    query.setParameter("objectType", objectType);
    query.setParameter("objectId", objectId);
    return query.getResultList();
  }

}
