/*
 * Jicofo, the Jitsi Conference Focus.
 *
 * Copyright @ 2015-Present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.jicofo.lipsynchack;

import org.jitsi.xmpp.extensions.colibri.*;
import org.jitsi.xmpp.extensions.jingle.*;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Wrapper for <tt>SourceGroupPacketExtension</tt>.
 *
 * @author Pawel Domas
 */
public class SourceGroup
{
    /**
     * Underlying source group packet extension.
     */
    private final SourceGroupPacketExtension group;

    /**
     * Extracts source groups from Jingle content packet extension.
     * @param content the <tt>ContentPacketExtension</tt> that contains(or not)
     *                the description of source groups.
     * @return the list of <tt>SourceGroup</tt>s described by given
     *         <tt>ContentPacketExtension</tt>.
     */
    public static List<SourceGroup> getSourceGroupsForContent(ContentPacketExtension content)
    {
        List<SourceGroup> groups = new ArrayList<>();

        RtpDescriptionPacketExtension rtpDescPe = JingleUtils.getRtpDescription(content);

        if (rtpDescPe == null)
        {
            return groups;
        }

        List<SourceGroupPacketExtension> groupExtensions
            = rtpDescPe.getChildExtensionsOfType(SourceGroupPacketExtension.class);

        for (SourceGroupPacketExtension groupPe : groupExtensions)
        {
            groups.add(new SourceGroup(groupPe));
        }

        return groups;
    }

    /**
     * Creates new instance of <tt>SourceGroup</tt>.
     * @param group the packet extension that described source group to be wrapped
     *              by new object.
     * @throws NullPointerException if <tt>group</tt> is <tt>null</tt>.
     */
    public SourceGroup(SourceGroupPacketExtension group)
    {
        this.group = Objects.requireNonNull(group, "group");
    }

    /**
     * Adds source to this group.
     *
     * @param source the <tt>SourcePacketExtension</tt> to be added to this
     *               group.
     */
    public void addSource(SourcePacketExtension source)
    {
        group.addChildExtension(source);
    }

    /**
     * Adds the list of sources to this group.
     *
     * @param video the list of <tt>SourcePacketExtension</tt> which will be
     *              added to this group.
     */
    public void addSources(List<SourcePacketExtension> video)
    {
        group.addSources(video);
    }

    /**
     * Returns the sources contained in this group.
     * @return the internal list that stores <tt>SourcePacketExtension</tt>
     */
    public List<SourcePacketExtension> getSources()
    {
        return group.getSources();
    }

    /**
     * Returns deep copy of underlying <tt>SourceGroupPacketExtension</tt>.
     */
    public SourceGroupPacketExtension getExtensionCopy()
    {
        return group.copy();
    }

    /**
     * Returns full copy of this <tt>SourceGroup</tt>.
     */
    public SourceGroup copy()
    {
        return new SourceGroup(getExtensionCopy());
    }

    /**
     * Overrides {@link Object#equals(Object)}. Two {@link SourceGroup}s are
     * considered equal if:
     * 1. They have the same semantics
     * 2. They have the same sources (according to
     * {@link SourcePacketExtension#equals(Object)}), and in the same order.
     *
     * @param obj the other source group.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof SourceGroup))
        {
            return false;
        }

        SourceGroup other = (SourceGroup) obj;
        String semantics = other.getSemantics();
        if (isBlank(semantics) && isNotBlank(getSemantics()))
        {
            return false;
        }

        if (!getSemantics().equals(semantics))
        {
            return false;
        }

        List<SourcePacketExtension> sources = getSources();
        List<SourcePacketExtension> otherSources = other.getSources();

        if (sources.size() != otherSources.size())
        {
            return false;
        }

        for (int i = 0; i < sources.size(); i++)
        {
            if (!sources.get(i).sourceEquals(otherSources.get(i)))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getSemantics(), getSources());
    }

    /**
     * Check if this <tt>SourceGroup</tt> contains any
     * <tt>SourceGroupPacketExtension</tt>s.
     *
     * @return <tt>true</tt> if this <tt>SourceGroup</tt> is empty or
     *         <tt>false</tt> otherwise.
     */
    public boolean isEmpty()
    {
        return this.group.getSources().isEmpty();
    }

    @Override
    public String toString()
    {
        StringBuilder sources = new StringBuilder();
        for (SourcePacketExtension source : this.group.getSources())
        {
            // FIXME do not print for the last element
            sources.append(source.toString()).append(", ");
        }
        return "SourceGroup[" + this.group.getSemantics() + ", " + sources
            + "]@" + Integer.toHexString(hashCode());
    }

    public String getSemantics()
    {
        return group.getSemantics();
    }
}
