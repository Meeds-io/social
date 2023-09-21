package org.exoplatform.social.core.utils;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.junit.Test;

import java.util.regex.Pattern;


public class MentionUtilsTest extends AbstractCoreTest {

    private IdentityManager identityManager;

    private static final Pattern MENTION_PATTERN                 = Pattern.compile("@([^\\s<]+)|@([^\\s<]+)$");

    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("gatein.email.domain.url", "http://test.com");
        identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);

    }

    @Test
    public void testSubstituteUsernames(){
        Identity mentionedUserIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
        Profile profile = mentionedUserIdentity.getProfile();
        profile.setUrl("/profile/root");
        profile.setProperty("fullName", "Root Root");

        String portalOwner = "dw";
        String message = "hello <p>@root</p>";

        message = MentionUtils.substituteUsernames(portalOwner, message);
        assertEquals("hello <p><a href=\"http://test.com/portal/dw/profile/root\" target=\"_parent\">Root Root</a></p>", message);
    }

    @Test
    public void testWrongFormatMentionWithSubstituteUsernames(){
        String portalOwner = "dw";
        String message = "hello @ jhon";
        message = MentionUtils.substituteUsernames(portalOwner, message);
        assertEquals("hello @ jhon", message);
    }

    @Test
    public void testMultipleSubstituteUsernames(){
        Identity mentionedUserIdentity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
        Profile profile1 = mentionedUserIdentity1.getProfile();
        profile1.setUrl("/profile/root");
        profile1.setProperty("fullName", "Root Root");

        Identity mentionedUserIdentity12 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
        Profile profile2 = mentionedUserIdentity12.getProfile();
        profile2.setUrl("/profile/john");
        profile2.setProperty("fullName", "John Anthony");

        String portalOwner = "dw";
        String message = "hello <p>@root</p> hey! <p>@john</p> ";

        message = MentionUtils.substituteUsernames(portalOwner, message);
        assertEquals("hello <p><a href=\"http://test.com/portal/dw/profile/root\" target=\"_parent\">Root Root</a></p>" +
                " hey! <p><a href=\"http://test.com/portal/dw/profile/john\" target=\"_parent\">John Anthony</a></p> ", message);
    }
}