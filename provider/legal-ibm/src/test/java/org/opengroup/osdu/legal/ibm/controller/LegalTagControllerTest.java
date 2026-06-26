/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

 
package org.opengroup.osdu.legal.ibm.controller;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.controller.LegalTagController;
import org.opengroup.osdu.legal.ibm.LegalApplication;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.legal.tags.dto.LegalTagDto;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes={LegalApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LegalTagControllerTest {
	
    @Mock
    TenantInfo tenantInfo;

    @Mock
    LegalTagService legalTagService;

    @Mock
    RequestInfo requestInfo;

    @InjectMocks
    @Inject
    private LegalTagController sut;

    @Before
    public void Setup() {
       initMocks(this);
       when(requestInfo.getTenantInfo()).thenReturn(tenantInfo);
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void givenUnauthenticated_whenCallCreateLegalTag_thenThrowsException(){
        LegalTagDto legalTag = new LegalTagDto();
        this.sut.createLegalTag(legalTag);
    }

    @WithMockUser(username="admin", roles={ServiceConfig.LEGAL_ADMIN})
    @Test
    public void given1AuthenticatedAdmin_whenCallCreateLegalTag_thenOk() {
        LegalTagDto legalTag = new LegalTagDto();
        Assert.assertEquals(HttpStatus.CREATED, this.sut.createLegalTag(legalTag).getStatusCode());
    }

    @WithMockUser(username="viewer", roles={ServiceConfig.LEGAL_USER})
    @Test
    public void givenAuthenticatedViewer_whenCallCreateLegalTag_thenForbidden() {
        try {
            LegalTagDto legalTag = new LegalTagDto();
            this.sut.createLegalTag(legalTag);
        } catch (AppException e) {
            Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), e.getError().getCode());
        }
    }

    @WithMockUser(username="viewer", roles={ServiceConfig.LEGAL_USER})
    @Test
    public void given2AuthenticatedViewer_whenCallGetLegalTag_thenOK() {
        Assert.assertEquals(HttpStatus.OK, this.sut.listLegalTags(true).getStatusCode());
    }
}
