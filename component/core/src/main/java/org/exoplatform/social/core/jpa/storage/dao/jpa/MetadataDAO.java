package org.exoplatform.social.core.jpa.storage.dao.jpa;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.MetadataEntity;

public class MetadataDAO extends GenericDAOJPAImpl<MetadataEntity, Long> {

  public MetadataEntity findMetadata(long type, String name, long audienceId) {
    TypedQuery<MetadataEntity> query = getEntityManager().createNamedQuery("SocMetadataEntity.findMetadata",
                                                                           MetadataEntity.class);
    query.setParameter("type", type);
    query.setParameter("name", name);
    query.setParameter("audienceId", audienceId);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

}
