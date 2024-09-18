package com.camunda.consulting;

import jakarta.inject.Inject;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.filter;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@RunWith(Arquillian.class)
public class ProcessUnitTest {
    protected static JavaArchive CACHED_CLIENT_ASSET;
    protected static JavaArchive CACHED_ENGINE_CDI_ASSET;

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive base = ShrinkWrap.create(WebArchive.class, "sample-test.war");
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies()
                .resolve()
                .withTransitivity().asFile();
        return base
                .addPackages(true, "com.camunda.consulting")
                .addAsResource("META-INF/persistence.xml", "META-INF/processes.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("process.bpmn")
                .addClass(ProcessUnitTest.class)
                .addAsLibraries(libs);
    }


//    @Inject
    private ProcessEngine processEngine;
    private ProcessEngineService processEngineService;
    //  protected ProcessArchiveService processArchiveService;
    private FormService formService;
    private HistoryService historyService;
    private IdentityService identityService;
    private ManagementService managementService;
    private RepositoryService repositoryService;
    protected RuntimeService runtimeService;
    private TaskService taskService;
    private CaseService caseService;
    private DecisionService decisionService;

    @Before
    public void setupBeforeTest() {
        init(processEngine);
        processEngineService = BpmPlatform.getProcessEngineService();
        processEngine = processEngineService.getDefaultProcessEngine();
        formService = processEngine.getFormService();
        historyService = processEngine.getHistoryService();
        identityService = processEngine.getIdentityService();
        managementService = processEngine.getManagementService();
        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
        taskService = processEngine.getTaskService();
        caseService = processEngine.getCaseService();
        decisionService = processEngine.getDecisionService();
    }

    @Test
    public void testHappyPath() {
        // Drive the process by API and assert correct behavior by camunda-bpm-assert
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(ProcessConstants.PROCESS_DEFINITION_KEY);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getProcessDefinitionId()).isEqualTo(ProcessConstants.PROCESS_DEFINITION_KEY);
    }

}
