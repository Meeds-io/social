package org.exoplatform.social.notification.channel.template;

import java.util.Calendar;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.DlpUserRestoredItemPlugin;


public class DlpUserRestoredItemMailBuilderTest extends AbstractPluginTest {
    private ChannelManager manager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = getService(ChannelManager.class);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Override
    public AbstractTemplateBuilder getTemplateBuilder() {
        AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
        assertNotNull(channel);
        assertTrue(channel.hasTemplateBuilder(PluginKey.key(DlpUserRestoredItemPlugin.ID)));
        return channel.getTemplateBuilder(PluginKey.key(DlpUserRestoredItemPlugin.ID));
    }

    @Override
    public BaseNotificationPlugin getPlugin() {
        return pluginService.getPlugin(PluginKey.key(DlpUserRestoredItemPlugin.ID));
    }

    public void testSimpleCase() throws Exception {
        //When
        PortalContainer container = PortalContainer.getInstance();
        DlpPositiveItemService dlpPositiveItemService = container.getComponentInstanceOfType(DlpPositiveItemService.class);
        DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setDetectionDate(Calendar.getInstance());
        dlpPositiveItemEntity.setReference("file1234");
        dlpPositiveItemEntity.setTitle("file1");
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setAuthor(maryIdentity.getRemoteId());
        
        dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
        DlpPositiveItem dlpPositiveItem = dlpPositiveItemService.getDlpPositiveItemByReference("file1234");
        dlpPositiveItemService.restoreDlpPositiveItem(dlpPositiveItem.getId());

        //assert Message Info
        List<NotificationInfo> list = assertMadeMailDigestNotifications(maryIdentity.getRemoteId(), 1);
        NotificationContext ctx = NotificationContextImpl.cloneInstance();
        ctx.setNotificationInfo(list.get(0).setTo(maryIdentity.getRemoteId()));
        MessageInfo message = buildMessageInfo(ctx);

        assertSubject(message, "Item has been validated");
        assertBody(message, "has been analyzed during a quarantine phase. It has been validated and restored in his initial location");
        notificationService.clearAll();
    }
}
