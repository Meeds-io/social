package org.exoplatform.social.core.jpa.search;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ProfileIndexingServiceConnectorTest extends AbstractCoreTest {

  private IdentityManager                 identityManager;

  private Identity                        userIdentity;

  @Mock
  private ConnectionDAO                   connectionDAO;

  @Mock
  private IdentityDAO                     identityDAO;

  private ProfilePropertyService          profilePropertyService;

  private ProfileIndexingServiceConnector profileIndexingServiceConnector;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getService(IdentityManagerImpl.class);
    profilePropertyService = getService(ProfilePropertyService.class);
    InitParams initParams = new InitParams();
    PropertiesParam params = new PropertiesParam();
    params.setName("constructor.params");
    params.setProperty("index_alias", "profile_alias");
    params.setProperty("index_current", "profile_v2");
    initParams.addParam(params);
    userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = userIdentity.getProfile();
    profile.setProperty("profession", "Developer");
    identityManager.updateProfile(profile, true);
    this.profileIndexingServiceConnector = new ProfileIndexingServiceConnector(initParams,
                                                                               identityManager,
                                                                               identityDAO,
                                                                               connectionDAO,
                                                                               profilePropertyService);
  }

  @Test
  public void testUpdate() {
    Document document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("Developer",document.getFields().get("profession"));
    profilePropertyService.hidePropertySetting(Long.parseLong(userIdentity.getId()),
                                               profilePropertyService.getProfileSettingByName("profession").getId());
    document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("hidden",document.getFields().get("profession"));
  }
}
