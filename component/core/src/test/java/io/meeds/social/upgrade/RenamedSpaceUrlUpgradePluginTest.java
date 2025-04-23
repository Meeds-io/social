package io.meeds.social.upgrade;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeData;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@RunWith(MockitoJUnitRunner.class)
public class RenamedSpaceUrlUpgradePluginTest {

  private RenamedSpaceUrlUpgradePlugin renamedSpaceUrlUpgradePlugin;

  @Mock
  private SpaceService                 spaceService;

  @Mock
  private NavigationService            navigationService;

  @Mock
  private EntityManagerService         entityManagerService;

  InitParams                           initParams = new InitParams();

  @Before
  public void setUp() {
    ValueParam productGroupIdValueParam = new ValueParam();
    productGroupIdValueParam.setName("product.group.id");
    productGroupIdValueParam.setValue("org.exoplatform.platform");
    initParams.addParameter(productGroupIdValueParam);
    this.renamedSpaceUrlUpgradePlugin = new RenamedSpaceUrlUpgradePlugin(spaceService,
                                                                         navigationService,
                                                                         entityManagerService,
                                                                         initParams);
  }

  @Test
  public void testProcessUpgrade() {
    EntityManager entityManager = mock(EntityManager.class);
    when(entityManagerService.getEntityManager()).thenReturn(entityManager);
    Query query = mock(Query.class);
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    SpaceEntity spaceEntity1 = mock(SpaceEntity.class);
    when(spaceEntity1.getGroupId()).thenReturn("/spaces/space1");
    SpaceEntity spaceEntity2 = mock(SpaceEntity.class);
    when(spaceEntity2.getGroupId()).thenReturn("/spaces/space2");
    SpaceEntity spaceEntity3 = mock(SpaceEntity.class);
    when(spaceEntity3.getGroupId()).thenReturn("/spaces/space3");
    List<SpaceEntity> spaceEntities = Arrays.asList(spaceEntity1, spaceEntity2, spaceEntity3);
    when(query.getResultList()).thenReturn(spaceEntities);
    NavigationContext spaceNavigationContext = mock(NavigationContext.class);
    when(navigationService.loadNavigation(any())).thenReturn(spaceNavigationContext);
    NodeContext rootSpaceNavigationNode = mock(NodeContext.class);
    when(navigationService.loadNode(any(), any(), any(), any())).thenReturn(rootSpaceNavigationNode);
    Collection spaceSiteNavigationNodes = mock(Collection.class);
    when(rootSpaceNavigationNode.getNodes()).thenReturn(spaceSiteNavigationNodes);
    Iterator firstSpaceNavigationNodeContextIterator = mock(Iterator.class);
    when(spaceSiteNavigationNodes.iterator()).thenReturn(firstSpaceNavigationNodeContextIterator);
    NodeContext firstSpaceNavigationNodeContext = mock(NodeContext.class);
    when(firstSpaceNavigationNodeContextIterator.next()).thenReturn(firstSpaceNavigationNodeContext);
    NodeData firstSpaceNavigationNodeData = mock(NodeData.class);
    when(firstSpaceNavigationNodeContext.getData()).thenReturn(firstSpaceNavigationNodeData);
    Space space1 = mock(Space.class);
    Space space2 = mock(Space.class);
    Space space3 = mock(Space.class);
    when(spaceService.getSpaceByGroupId(anyString())).thenReturn(space1).thenReturn(space2).thenReturn(space3);
    when(firstSpaceNavigationNodeData.getName()).thenReturn("space1").thenReturn("space2").thenReturn("space3");

    renamedSpaceUrlUpgradePlugin.processUpgrade("oldVersion", "newVersion");

    // Verify the result
    assertEquals(3, renamedSpaceUrlUpgradePlugin.getMigratedRenamedSpacesUrls());
    
    verify(entityManager, times(1)).createNativeQuery(anyString());
    verify(spaceService, times(3)).updateSpace(any(Space.class));
    
  }
}
