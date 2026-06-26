package org.opengroup.osdu.legal.ibm.tags;

import org.opengroup.osdu.core.common.model.legal.LegalTag;

public class BanckendLegalTag extends LegalTag {
    public Boolean getIs_Valid() {
        return is_valid;
    }

    public void setIs_Valid(Boolean is_valid) {
        this.is_valid = is_valid;
    }

    private Boolean is_valid = false;
}
