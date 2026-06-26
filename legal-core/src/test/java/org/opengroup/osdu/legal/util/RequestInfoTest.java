package org.opengroup.osdu.legal.util;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestInfoTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private ServiceConfig config;
    @Mock
    private ITenantFactory tenantStorage;

    @InjectMocks
    private RequestInfo sut;

    @Before
    public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void should_returnFalse_when_schemeIsHttp(){
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://nonexistent.nonexisent.domain/path"));
        assertFalse(sut.isHttps());
    }
    @Test
    public void should_returnTrue_when_schemeIsHttp()throws Exception{
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://nonexistent.nonexisent.domain/path"));
        assertTrue(sut.isHttps());
    }
    @Test
    public void should_returnFullUrlwhen_queryIsGiven()throws Exception{
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://nonexistent.nonexisent.domain/path"));
        when(request.getQueryString()).thenReturn("a=2");
        assertEquals("https://nonexistent.nonexisent.domain/path?a=2", sut.getUri());
    }

    @Test
    public void should_returnUser_when_requested(){
        setHeaderValues(true, "127.0.0.1");
        assertEquals("nonexistent@nonexisent.domain", sut.getUser());
    }

    @Test
    public void should_returnTrue_when_cronHeaderAndIpIsSet(){
        setHeaderValues(true, "127.0.0.1");
        assertTrue(sut.isCronRequest());
    }

    @Test
    public void should_returnfalse_when_cronHeaderIsNotSet(){
        setHeaderValues(false, "127.0.0.1");
        assertFalse(sut.isCronRequest());
    }
    @Test
    public void should_returnfalse_when_IpIsNotSet(){
        setHeaderValues(false, "127.0.0.2");
        assertFalse(sut.isCronRequest());
    }

    @Test
    public void should_addCorrelationId_when_gettingHeaders(){
        setHeaderValues(false, "127.0.0.2");
        assertTrue(sut.getHeaders().getHeaders().containsKey(DpsHeaders.CORRELATION_ID));
    }
    @Test
    public void should_returnCompliancdRuleSet_from_tenantInfo(){
        setHeaderValues(false, "127.0.0.1");
        TenantInfo ti = new TenantInfo();
        ti.setComplianceRuleSet("MyCompany");
        when(tenantStorage.getTenantInfo(any())).thenReturn(ti);

        assertEquals("MyCompany", sut.getComplianceRuleSet());
    }

    private void setHeaderValues(Boolean isCron, String ip) {
        List<String> headerNames = new ArrayList<>();
        headerNames.add(DpsHeaders.USER_EMAIL);
        headerNames.add(DpsHeaders.DATA_PARTITION_ID);
        headerNames.add("X-Appengine-Cron");

        when(request.getHeaderNames()).thenReturn(Collections.enumeration(headerNames));
        when(request.getHeader(DpsHeaders.USER_EMAIL)).thenReturn("nonexistent@nonexisent.domain");
        when(request.getHeader(DpsHeaders.DATA_PARTITION_ID)).thenReturn("tenant1");
        when(request.getHeader("X-Appengine-Cron")).thenReturn(isCron.toString());
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(config.getCronIpAddress()).thenReturn(ip);
    }
}
