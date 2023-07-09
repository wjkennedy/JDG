// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config.module;

import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import java.util.Collections;
import java.util.Collection;

public class CommentParameters
{
    public static final CommentParameters EMPTY;
    private final String author;
    private final Collection<CommentProperties> commentPropertieses;
    
    private CommentParameters() {
        this.author = "";
        this.commentPropertieses = (Collection<CommentProperties>)Collections.emptyList();
    }
    
    public CommentParameters(final String author, final Collection<CommentProperties> commentPropertieses) {
        Preconditions.checkArgument(StringUtils.isNotBlank(author), (Object)"author is required");
        this.author = author;
        final ImmutableList.Builder<CommentProperties> builder = (ImmutableList.Builder<CommentProperties>)ImmutableList.builder();
        if (commentPropertieses != null) {
            builder.addAll((Iterable)commentPropertieses);
        }
        this.commentPropertieses = (Collection<CommentProperties>)builder.build();
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public Collection<CommentProperties> getCommentProperties() {
        return this.commentPropertieses;
    }
    
    static {
        EMPTY = new CommentParameters();
    }
}
