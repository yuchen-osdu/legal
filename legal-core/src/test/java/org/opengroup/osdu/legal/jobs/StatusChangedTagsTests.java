package org.opengroup.osdu.legal.jobs;

import org.junit.Test;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;

import static junit.framework.TestCase.assertEquals;

public class StatusChangedTagsTests {

    @Test
    public void should_print_object_whenFormattedToString(){
        StatusChangedTags statusChangedTags = new StatusChangedTags();
        statusChangedTags.getStatusChangedTags().add(new StatusChangedTag(null, null));
        String s = String.format("%s", statusChangedTags);

        assertEquals("StatusChangedTags(statusChangedTags=[StatusChangedTag(changedTagName=null, changedTagStatus=null)])", s);
    }
}
