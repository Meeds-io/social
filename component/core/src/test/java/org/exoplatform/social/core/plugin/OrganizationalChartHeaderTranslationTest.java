package org.exoplatform.social.core.plugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;


@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/exo.social.component.core-local-root-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml") })
public class OrganizationalChartHeaderTranslationTest extends AbstractKernelTest {

  private TranslationService translationService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    translationService = getContainer().getComponentInstanceOfType(TranslationService.class);
    begin();
  }

  public void testTranslateOrganizationalChartHeaderTitle() throws ObjectNotFoundException {

    Map<Locale, String> labels = new HashMap<>();
    labels.put(new Locale("en"), "Organizational chart");
    labels.put(new Locale("fr"), "Organigramme");
    translationService.saveTranslationLabels("organizationalChart", 1L, "chartHeaderTitle", labels);

    TranslationField translationField = translationService.getTranslationField("organizationalChart", 1L, "chartHeaderTitle");
    assertNotNull(translationField);
    assertNotNull(translationField.getLabels());
    assertEquals(2, translationField.getLabels().size());
    end();
  }
}
