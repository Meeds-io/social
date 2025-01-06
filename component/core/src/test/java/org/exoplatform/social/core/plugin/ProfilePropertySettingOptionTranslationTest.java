package org.exoplatform.social.core.plugin;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml") })
public class ProfilePropertySettingOptionTranslationTest extends AbstractKernelTest {

  private TranslationService translationService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    translationService = getContainer().getComponentInstanceOfType(TranslationService.class);
    begin();
  }

  public void testTranslatePropertyOption() throws ObjectNotFoundException {

    Map<Locale, String> labels = new HashMap<>();
    labels.put(Locale.US, "option en");
    labels.put(Locale.FRANCE, "option fr");
    translationService.saveTranslationLabels("propertySettingOption", 1L, "optionValue", labels);

    TranslationField translationField = translationService.getTranslationField("propertySettingOption", 1L, "optionValue");
    assertNotNull(translationField);
    assertNotNull(translationField.getLabels());
    assertEquals(2, translationField.getLabels().size());
    end();
  }
}
