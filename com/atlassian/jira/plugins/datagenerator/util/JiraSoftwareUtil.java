// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.util;

import org.slf4j.LoggerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.greenhopper.api.rapid.view.RapidViewCreationService;
import org.slf4j.Logger;

public class JiraSoftwareUtil
{
    private static final Logger log;
    
    public static boolean isSoftwarePresent() {
        final RapidViewCreationService rapidViewCreationService = (RapidViewCreationService)ComponentAccessor.getOSGiComponentInstanceOfType((Class)RapidViewCreationService.class);
        if (rapidViewCreationService == null) {
            JiraSoftwareUtil.log.error("Jira Software not installed!");
            return false;
        }
        return true;
    }
    
    static {
        log = LoggerFactory.getLogger((Class)JiraSoftwareUtil.class);
    }
}
