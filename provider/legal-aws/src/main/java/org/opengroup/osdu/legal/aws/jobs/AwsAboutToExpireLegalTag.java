package org.opengroup.osdu.legal.aws.jobs;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTag;

import java.sql.Date;
import lombok.Data;

@Data
public class AwsAboutToExpireLegalTag extends AboutToExpireLegalTag {

	private String dataPartitionId;

    public AwsAboutToExpireLegalTag(String tagName, String dataPartitionId, Date expirationDate) {
        super(tagName, expirationDate);
        this.dataPartitionId = dataPartitionId;
    }

}
