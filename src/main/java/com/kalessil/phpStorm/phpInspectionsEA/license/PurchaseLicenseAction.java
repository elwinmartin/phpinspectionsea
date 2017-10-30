package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.*;
import org.jetbrains.annotations.NotNull;

final public class PurchaseLicenseAction {
    public void perform(@NotNull IdeaPluginDescriptor plugin) {
        final String pluginName       = plugin.getName();
        final NotificationGroup group = new NotificationGroup(pluginName, NotificationDisplayType.STICKY_BALLOON, true);
        Notifications.Bus.notify(group.createNotification(
            "<b>" + pluginName + "</b>",
            PurchaseLicenseAction.class.getName(),
            NotificationType.INFORMATION,
            NotificationListener.URL_OPENING_LISTENER
        ));
    }
}