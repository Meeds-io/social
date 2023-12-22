package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import jakarta.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.ActivityShareActionEntity;

public class ActivityShareActionDAO extends GenericDAOJPAImpl<ActivityShareActionEntity, Long> {

  public List<ActivityShareActionEntity> getShareActionsByActivityId(Long activityId) {
    TypedQuery<ActivityShareActionEntity> query = getEntityManager().createNamedQuery("SocActivityShareAction.getShareActionsByActivityId", ActivityShareActionEntity.class);
    query.setParameter("activityId", activityId);
    return query.getResultList();
  }

}
