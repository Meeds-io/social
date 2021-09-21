package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocMetadataEntity")
@ExoEntity
@Table(name = "SOC_METADATAS")
@NamedQueries(
  {
      @NamedQuery(
          name = "SocMetadataEntity.findMetadata",
          query = "SELECT sm FROM SocMetadataEntity sm WHERE"
              + " sm.type = :type AND"
              + " sm.name = :name AND"
              + " sm.audienceId = :audienceId"
      )
  }
)
public class MetadataEntity implements Serializable {

  private static final long   serialVersionUID = -743950558885709009L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_METADATA_ID", sequenceName = "SEQ_SOC_METADATA_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_METADATA_ID")
  @Column(name = "METADATA_ID")
  private Long                id;

  @Column(name = "TYPE", nullable = false)
  private Long                type;

  @Column(name = "NAME")
  private String              name;

  @Column(name = "CREATOR_ID", nullable = false)
  private long                creatorId;

  @Column(name = "AUDIENCE_ID")
  private long                audienceId;

  @Column(name = "CREATED_DATE", nullable = false)
  private Date                createdDate;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "NAME")
  @Column(name = "VALUE")
  @CollectionTable(name = "SOC_METADATA_PROPERTIES", joinColumns = { @JoinColumn(name = "METADATA_ID") })
  private Map<String, String> properties;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getType() {
    return type;
  }

  public void setType(Long type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(long creatorId) {
    this.creatorId = creatorId;
  }

  public long getAudienceId() {
    return audienceId;
  }

  public void setAudienceId(long audienceId) {
    this.audienceId = audienceId;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getCreatedDate() {
    return createdDate;
  }
}
