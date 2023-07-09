// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import java.util.Collection;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Iterator;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.google.common.collect.HashMultimap;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.config.CustomFields;
import com.atlassian.jira.project.Project;
import com.google.common.collect.Multimap;
import com.atlassian.jira.plugins.datagenerator.model.ScreenInfo;

public class FieldScreenDistribution
{
    final ScreenInfo[] allScreens;
    final Multimap<Integer, ScreenInfo> customFieldScreenMap;
    final Multimap<Integer, Project> screenProjectsMap;
    
    public FieldScreenDistribution(final ScreenInfo[] allScreens, final Multimap<Integer, ScreenInfo> customFieldScreenMap, final Multimap<Integer, Project> screenProjectsMap) {
        this.allScreens = allScreens.clone();
        this.customFieldScreenMap = customFieldScreenMap;
        this.screenProjectsMap = screenProjectsMap;
    }
    
    public static FieldScreenDistribution calculateRandomDistribution(final CustomFields customFields, final List<Project> generatedProjects) {
        final Multimap<Integer, ScreenInfo> customFieldScreenMap = (Multimap<Integer, ScreenInfo>)HashMultimap.create();
        final ScreenInfo[] screens = new ScreenInfo[customFields.screenSchemes];
        for (int screenIndex = 0; screenIndex < customFields.screenSchemes; ++screenIndex) {
            final ScreenInfo screenInfo = new ScreenInfo(screenIndex, randomFieldList(customFields.customFieldsPerScreen, customFields.fieldCount));
            screens[screenIndex] = screenInfo;
            for (final Integer fieldIndex : screens[screenIndex].getVisibleFields()) {
                customFieldScreenMap.put((Object)fieldIndex, (Object)screenInfo);
            }
        }
        final Multimap<Integer, Project> screenProjectsMap = (Multimap<Integer, Project>)HashMultimap.create();
        if (customFields.screenSchemes > 0) {
            for (final Project generatedProject : generatedProjects) {
                final int screenIndex2 = Randomizer.randomInt(customFields.screenSchemes);
                screenProjectsMap.put((Object)screenIndex2, (Object)generatedProject);
            }
        }
        return new FieldScreenDistribution(screens, customFieldScreenMap, screenProjectsMap);
    }
    
    private static List<Integer> randomFieldList(final int customFieldsPerScreen, final int fieldCount) {
        final List<Integer> list = new LinkedList<Integer>();
        final BitSet bitSet = Randomizer.randomBitSet(customFieldsPerScreen, fieldCount);
        for (int i = 0; i < bitSet.length(); ++i) {
            if (bitSet.get(i)) {
                list.add(i);
            }
        }
        return list;
    }
    
    public List<Project> getProjectsForCustomField(final int customFieldIndex) {
        final List<Project> projects = new LinkedList<Project>();
        final Collection<ScreenInfo> screens = this.customFieldScreenMap.get((Object)customFieldIndex);
        for (final ScreenInfo screen : screens) {
            projects.addAll(this.screenProjectsMap.get((Object)screen.getIndex()));
        }
        return projects;
    }
    
    public ScreenInfo[] getAllScreens() {
        return this.allScreens.clone();
    }
    
    public Collection<Project> getProjectsForScreen(final ScreenInfo screenInfo) {
        return this.screenProjectsMap.get((Object)screenInfo.getIndex());
    }
}
