// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.model;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import com.atlassian.core.util.FileUtils;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.io.File;
import java.util.Map;

public class AttachmentFile
{
    public static Map<String, String> RESOURCES;
    private File file;
    private String contentType;
    private String fileName;
    
    public AttachmentFile(final File file, final String contentType, final String fileName) {
        this.file = file;
        this.contentType = contentType;
        this.fileName = fileName;
    }
    
    public static List<AttachmentFile> createDefaultAttachments() throws IOException {
        final ImmutableList.Builder<AttachmentFile> attachmentFiles = (ImmutableList.Builder<AttachmentFile>)new ImmutableList.Builder();
        for (final Map.Entry<String, String> resource : AttachmentFile.RESOURCES.entrySet()) {
            final InputStream cpResource = AttachmentFile.class.getResourceAsStream(resource.getKey());
            final File attachmentFile = File.createTempFile("attachment", "file");
            FileUtils.copyFile(cpResource, attachmentFile, true);
            cpResource.close();
            attachmentFiles.add((Object)new AttachmentFile(attachmentFile, resource.getValue(), resource.getKey()));
        }
        return (List<AttachmentFile>)attachmentFiles.build();
    }
    
    public static boolean closeAttachments(final List<AttachmentFile> attachments) {
        boolean status = true;
        for (final AttachmentFile attachmentFile : attachments) {
            status &= attachmentFile.getFile().delete();
        }
        return status;
    }
    
    public File getFile() {
        return this.file;
    }
    
    public String getContentType() {
        return this.contentType;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    static {
        AttachmentFile.RESOURCES = (Map<String, String>)ImmutableMap.of((Object)"text-attachment.txt", (Object)"text/plain", (Object)"screenshot-attachment.png", (Object)"image/png", (Object)"zip-attachment.zip", (Object)"application/zip");
    }
}
