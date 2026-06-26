package org.opengroup.osdu.legal.ibm.jobs;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.provider.interfaces.IAboutToExpireLegalTagPublisher;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

import org.springframework.stereotype.Service;

@Service
public class AboutToExpireLegalTagPublisherImpl implements IAboutToExpireLegalTagPublisher {
    @Override
    public void publish(String projectId, DpsHeaders headers, AboutToExpireLegalTags aboutToExpireLegalTags) {
        // to be implemented
    }
}
