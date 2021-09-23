package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocMetadataItemEntity")
@ExoEntity
@Table(name = "SOC_METADATA_ITEMS")
@NamedQueries(
  {
      @NamedQuery(
          name = "SocMetadataItemEntity.getMetadataItemsByObject",
          query = "SELECT mi FROM SocMetadataItemEntity mi WHERE "
              + " mi.objectType = :objectType AND"
              + " mi.objectId = :objectId"
              + " ORDER BY mi.createdDate DESC, mi.id ASC"
      ),
      @NamedQuery(
          name = "SocMetadataItemEntity.getMetadataItemsByMetadataAndObject",
          query = "SELECT mi FROM SocMetadataItemEntity mi WHERE "
              + " mi.metadata.id = :metadataId AND"
              + " mi.objectType = :objectType AND"
              + " mi.objectId = :objectId"
      ),
      @NamedQuery(
          name = "SocMetadataItemEntity.getMetadataObjectIds",
          query = "SELECT mi.objectId, mi.createdDate, mi.id FROM SocMetadataItemEntity mi WHERE "
              + " mi.metadata.type = :metadataType AND"
              + " mi.metadata.name = :metadataName AND"
              + " mi.objectType = :objectType"
              + " ORDER BY mi.createdDate DESC, mi.id ASC"
      ),
      @NamedQuery(
          name = "SocMetadataItemEntity.deleteMetadataItemsByObject",
          query = "DELETE FROM SocMetadataItemEntity mi WHERE "
              + " mi.objectType = :objectType AND"
              + " mi.objectId = :objectId"
      ),
      @NamedQuery(
          name = "SocMetadataItemEntity.deleteMetadataItemById",
          query = "DELETE FROM SocMetadataItemEntity mi WHERE "
              + " mi.id = :id"
      ),
  }
)
public class MetadataItemEntity implements Serializable {

  private static final long   serialVersionUID = 5011906982712067379L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_METADATA_ITEM_ID", sequenceName = "SEQ_SOC_METADATA_ITEM_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_METADATA_ITEM_ID")
  @Column(name = "METADATA_ITEM_ID")
  private Long                id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "METADATA_ID", nullable = false)
  private MetadataEntity      metadata;

  @Column(name = "OBJECT_TYPE", nullable = false)
  private String              objectType;

  @Column(name = "OBJECT_ID", nullable = false)
  private String              objectId;

  @Column(name = "PARENT_OBJECT_ID")
  private String              parentObjectId;

  @Column(name = "CREATOR_ID", nullable = false)
  private long                creatorId;

  @Column(name = "CREATED_DATE", nullable = false)
  private Date                createdDate;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "NAME")
  @Column(name = "VALUE")
  @CollectionTable(name = "SOC_METADATA_ITEMS_PROPERTIES", joinColumns = { @JoinColumn(name = "METADATA_ITEM_ID") })
  private Map<String, String> properties;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public MetadataEntity getMetadata() {
    return metadata;
  }

  public void setMetadata(MetadataEntity metadata) {
    this.metadata = metadata;
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getParentObjectId() {
    return parentObjectId;
  }

  public void setParentObjectId(String parentObjectId) {
    this.parentObjectId = parentObjectId;
  }

  public long getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(long creatorId) {
    this.creatorId = creatorId;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

}
