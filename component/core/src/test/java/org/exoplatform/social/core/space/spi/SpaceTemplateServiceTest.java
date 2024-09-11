package org.exoplatform.social.core.space.spi;

import java.util.*;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.*;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.*;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.impl.SpaceTemplateServiceImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SpaceTemplateServiceTest extends AbstractCoreTest {
  private SpaceTemplateService spaceTemplateService;

  private IdentityStorage      identityStorage;

  private List<Space>          tearDownSpaceList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    spaceTemplateService = CommonsUtils.getService(SpaceTemplateService.class);
    identityStorage = CommonsUtils.getService(IdentityStorage.class);
    spaceService = CommonsUtils.getService(SpaceService.class);
    tearDownSpaceList = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();

    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        try {
          identityStorage.deleteIdentity(spaceIdentity);
        } catch (IdentityStorageException e) {
          // It's expected on some identities that could be deleted in tests
        }
      }
      try {
        spaceService.deleteSpace(space);
      } catch (Exception e) {
        // It's expected on some entities that could be deleted in tests
      }
    }
    super.tearDown();
  }

  /**
   * Test {@link SpaceTemplateService#getSpaceTemplates()}
   */
  public void testGetSpaceTemplates() {
    // When
    List<SpaceTemplate> templates = spaceTemplateService.getSpaceTemplates();
    // Then
    assertTrue(Collections.unmodifiableList(Collections.EMPTY_LIST).getClass().isInstance(templates));
  }

  public void testCreateSpaceWithRolesInApplication() {
    Space space = createSpace("Space Settings Test", "root");
    assertNotNull(space);

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    Page page = layoutService.getPage("group::" + space.getGroupId() + "::SpaceSettingPortlet");
    assertNotNull(page);
    assertNotNull(page.getAccessPermissions());
    assertEquals(1, page.getAccessPermissions().length);
    assertEquals("manager:" + space.getGroupId(), page.getAccessPermissions()[0]);

    page = layoutService.getPage("group::" + space.getGroupId() + "::MembersPortlet");
    assertNotNull(page);
    assertNotNull(page.getAccessPermissions());
    assertEquals(1, page.getAccessPermissions().length);
    assertEquals("*:" + space.getGroupId(), page.getAccessPermissions()[0]);
  }

  /**
   * Test {@link SpaceTemplateService#getSpaceTemplates(String)} ()}
   */
  public void testGetAllSpaceTemplatesWithNoPermissions() throws Exception {
    // When
    List<SpaceTemplate> templates = spaceTemplateService.getSpaceTemplates("toto");
    // Then
    assertEquals(0, templates.size());
  }

  /**
   * Test {@link SpaceTemplateService#getSpaceTemplateByName(String)}
   */
  public void testGetSpaceTemplateByName() {
    // When
    SpaceTemplate template = spaceTemplateService.getSpaceTemplateByName("classic");
    SpaceTemplate templateFake = spaceTemplateService.getSpaceTemplateByName("fake");
    // Then
    assertEquals(template.getName(), "classic");
    assertNotNull(templateFake);
    assertEquals(templateFake.getName(), "classic");
  }

  /**
   * Test
   * {@link SpaceTemplateService#registerSpaceTemplatePlugin(SpaceTemplateConfigPlugin)}
   */
  public void testRegisterSpaceTemplatePlugin() {
    SpaceApplication homeApplication = new SpaceApplication();
    homeApplication.setAppTitle("fakeHome");
    homeApplication.setPortletApp("fakeHomeApp");
    homeApplication.setPortletName("fakeHomeName");

    List<SpaceApplication> applicationList = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      SpaceApplication app = new SpaceApplication();
      app.setAppTitle("fakeTitle" + i);
      app.setPortletApp("fakeApp" + i);
      app.setPortletName("fakeName" + i);
      applicationList.add(app);
    }
    SpaceTemplate spaceTemplate = new SpaceTemplate();
    spaceTemplate.setName("custom");
    spaceTemplate.setVisibility("private");
    spaceTemplate.setRegistration("open");
    spaceTemplate.setHomeApplication(homeApplication);
    spaceTemplate.setSpaceApplicationList(applicationList);
    InitParams params = new InitParams();
    ObjectParameter objParam = new ObjectParameter();
    objParam.setName("template");
    objParam.setObject(spaceTemplate);
    params.addParameter(objParam);
    // Given
    int initialSize = spaceTemplateService.getSpaceTemplates().size();
    // when
    spaceTemplateService.registerSpaceTemplatePlugin(new SpaceTemplateConfigPlugin(params));
    // Then
    assertEquals(initialSize + 1, spaceTemplateService.getSpaceTemplates().size());
  }

  /**
   * Test
   * {@link SpaceTemplateService#extendSpaceTemplatePlugin(SpaceTemplateConfigPlugin)}
   */
  public void testExtendSpaceTemplatePlugin() {
    List<SpaceApplication> applicationList = new ArrayList<>();
    SpaceTemplateConfigPlugin spaceTemplateConfigPlugin = getSpaceTemplateList(applicationList, 3, 6);

    // Given
    SpaceTemplate template = spaceTemplateService.getSpaceTemplateByName("classic");
    assertEquals(3, template.getSpaceApplicationList().size());
    // when
    spaceTemplateService.extendSpaceTemplatePlugin(spaceTemplateConfigPlugin);
    // Then
    ((SpaceTemplateServiceImpl) spaceTemplateService).start();
    template = spaceTemplateService.getSpaceTemplateByName("classic");
    assertEquals(6, template.getSpaceApplicationList().size());

    spaceTemplateConfigPlugin = getSpaceTemplateList(applicationList, 0, 3);

    // when
    spaceTemplateService.extendSpaceTemplatePlugin(spaceTemplateConfigPlugin);
    // Then
    ((SpaceTemplateServiceImpl) spaceTemplateService).start();
    template = spaceTemplateService.getSpaceTemplateByName("classic");
    assertEquals(9, template.getSpaceApplicationList().size());
  }

  private SpaceTemplateConfigPlugin getSpaceTemplateList(List<SpaceApplication> applicationList, int from, int to) {
    for (int i = from; i < to; i++) {
      SpaceApplication app = new SpaceApplication();
      app.setAppTitle("fakeTitle" + i);
      app.setPortletApp("fakeApp" + i);
      app.setPortletName("fakeName" + i);
      app.setOrder(4 + i);
      applicationList.add(app);
    }
    SpaceTemplate spaceTemplate = new SpaceTemplate();
    spaceTemplate.setName("classic");
    spaceTemplate.setSpaceApplicationList(applicationList);
    InitParams params = new InitParams();
    ObjectParameter objParam = new ObjectParameter();
    objParam.setName("template");
    objParam.setObject(spaceTemplate);
    params.addParameter(objParam);
    return new SpaceTemplateConfigPlugin(params);
  }

  /**
   * Test
   * {@link SpaceTemplateService#registerSpaceApplicationHandler(SpaceApplicationHandler)}
   */
  public void testRegisterSpaceApplicationHandler() {
    // Given
    Map<String, SpaceApplicationHandler> handlerMap = spaceTemplateService.getSpaceApplicationHandlers();
    int initialSize = handlerMap.size();
    InitParams params = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("templateName");
    valueParam.setValue("custom");
    params.addParameter(valueParam);
    SpaceApplicationHandler applicationHandler = new DefaultSpaceApplicationHandler(params, null, null, null, null);
    // when
    spaceTemplateService.registerSpaceApplicationHandler(applicationHandler);
    // then
    handlerMap = spaceTemplateService.getSpaceApplicationHandlers();
    assertEquals(initialSize + 1, handlerMap.size());
  }

  /**
   * Test {@link SpaceTemplateService#getSpaceApplicationHandlers()}
   */
  public void testGetSpaceApplicationHandlers() {
    // When
    Map<String, SpaceApplicationHandler> handlers = spaceTemplateService.getSpaceApplicationHandlers();
    // Then
    assertTrue(Collections.unmodifiableMap(Collections.EMPTY_MAP).getClass().isInstance(handlers));
  }

  /**
   * Test {@link SpaceTemplateService#getDefaultSpaceTemplate()}
   */
  public void testGetDefaultSpaceTemplate() {
    assertEquals("classic", spaceTemplateService.getDefaultSpaceTemplate());
  }

  private Space createSpace(String spaceName, String creator) {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setTemplate("template");
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    String[] managers = new String[] { creator };
    String[] members = new String[] { creator };
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.createSpace(space, creator);
    tearDownSpaceList.add(space);
    return space;
  }
}
