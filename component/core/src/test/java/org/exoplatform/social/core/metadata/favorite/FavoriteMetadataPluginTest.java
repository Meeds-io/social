package org.exoplatform.social.core.metadata.favorite;

import static org.junit.Assert.*;

import org.junit.Test;

import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;

public class FavoriteMetadataPluginTest {

  private static final FavoriteMetadataPlugin FAVORITE_METADATA_PLUGIN = new FavoriteMetadataPlugin();

  @Test
  public void testCanCreateMetadata() {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(1);

    assertFalse(FAVORITE_METADATA_PLUGIN.canCreateMetadata(metadata, 0));
    assertTrue(FAVORITE_METADATA_PLUGIN.canCreateMetadata(metadata, 1));
    assertFalse(FAVORITE_METADATA_PLUGIN.canCreateMetadata(metadata, 2));
  }

  @Test
  public void testCanUpdateMetadata() {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(1);

    assertFalse(FAVORITE_METADATA_PLUGIN.canUpdateMetadata(metadata, 0));
    assertTrue(FAVORITE_METADATA_PLUGIN.canUpdateMetadata(metadata, 1));
    assertFalse(FAVORITE_METADATA_PLUGIN.canUpdateMetadata(metadata, 2));
  }

  @Test
  public void testCanDeleteMetadata() {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(1);

    assertFalse(FAVORITE_METADATA_PLUGIN.canDeleteMetadata(metadata, 0));
    assertTrue(FAVORITE_METADATA_PLUGIN.canDeleteMetadata(metadata, 1));
    assertFalse(FAVORITE_METADATA_PLUGIN.canDeleteMetadata(metadata, 2));
  }

  @Test
  public void testCanCreateMetadataItem() {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(1);
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setMetadata(metadata);

    assertFalse(FAVORITE_METADATA_PLUGIN.canCreateMetadataItem(metadataItem, 0));
    assertTrue(FAVORITE_METADATA_PLUGIN.canCreateMetadataItem(metadataItem, 1));
    assertFalse(FAVORITE_METADATA_PLUGIN.canCreateMetadataItem(metadataItem, 2));
  }

  @Test
  public void testCanUpdateMetadataItem() {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(1);
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setMetadata(metadata);

    assertFalse(FAVORITE_METADATA_PLUGIN.canUpdateMetadataItem(metadataItem, 0));
    assertTrue(FAVORITE_METADATA_PLUGIN.canUpdateMetadataItem(metadataItem, 1));
    assertFalse(FAVORITE_METADATA_PLUGIN.canUpdateMetadataItem(metadataItem, 2));
  }

  @Test
  public void testCanDeleteMetadataItem() {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(1);
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setMetadata(metadata);

    assertFalse(FAVORITE_METADATA_PLUGIN.canDeleteMetadataItem(metadataItem, 0));
    assertTrue(FAVORITE_METADATA_PLUGIN.canDeleteMetadataItem(metadataItem, 1));
    assertFalse(FAVORITE_METADATA_PLUGIN.canDeleteMetadataItem(metadataItem, 2));
  }

  @Test
  public void testAllowMultipleItemsPerObject() {
    assertFalse(FAVORITE_METADATA_PLUGIN.allowMultipleItemsPerObject());
  }

  @Test
  public void testGetMetadataType() {
    assertNotNull(FAVORITE_METADATA_PLUGIN.getMetadataType());
    assertEquals(1, FAVORITE_METADATA_PLUGIN.getMetadataType().getId());
    assertEquals("favorites", FAVORITE_METADATA_PLUGIN.getMetadataType().getName());
    assertEquals(1, FAVORITE_METADATA_PLUGIN.getId());
    assertEquals("favorites", FAVORITE_METADATA_PLUGIN.getName());
  }

}
