package com.camunda.consulting;

import jakarta.inject.Inject;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.hibernate.annotations.Target;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@RunWith(Arquillian.class)
public class ProcessUnitTest {
    protected static JavaArchive CACHED_CLIENT_ASSET;
    protected static JavaArchive CACHED_ENGINE_CDI_ASSET;
    protected static JavaArchive[] CACHED_WELD_ASSETS;

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive base = ShrinkWrap.create(WebArchive.class, "sample-test.war");

        var jakartaCdi = resolveDepdencies("org.camunda.bpm.javaee:camunda-ejb-client-jakarta");
        var engineCdi = resolveDepdencies("org.camunda.bpm:camunda-engine-cdi-jakarta");
        var assertTestClient = resolveDepdencies("org.camunda.bpm:camunda-engine-cdi-jakarta");
        var bpmAware = resolveDepdencies("org.camunda.bpm:camunda-bpm-assert");
        var assertJ = resolveDepdencies("org.assertj:assertj-core");
//        var weld = resolveDepdencies("org.jboss.weld.servlet:weld-servlet-shaded");
        var archive = base
                .addPackages(true, "com.camunda.consulting")
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("process.bpmn")
                .addClass(ProcessUnitTest.class)
                .addAsLibraries(jakartaCdi, engineCdi, assertTestClient, bpmAware, assertJ);

//        base.addAsLibraries(weld);

        System.out.println(archive.toString(true));
        return archive;
    }


    private ProcessEngine processEngine;
    private ProcessEngineService processEngineService;
    protected RuntimeService runtimeService;

    @Before
    public void setupBeforeTest() {
        System.out.println("jboss.home.dir: " + System.getProperty("jboss.home.dir"));

        init(processEngine);
        processEngineService = BpmPlatform.getProcessEngineService();
        processEngine = processEngineService.getDefaultProcessEngine();
        runtimeService = processEngine.getRuntimeService();
        processEngine.getRepositoryService().createDeployment().addClasspathResource("process.bpmn").deploy();
    }

    @Test
    public void testHappyPath() {
        // Drive the process by API and assert correct behavior by camunda-bpm-assert
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(ProcessConstants.PROCESS_DEFINITION_KEY);
        assertThat(processInstance).isNotNull();
        BpmnAwareTests.assertThat(processInstance).isEnded();
    }

    protected static JavaArchive[] resolveDepdencies(String depdencyPath) {
        return Maven.configureResolver()
                .workOffline()
                .loadPomFromFile("pom.xml")
                .resolve(depdencyPath)
                .withTransitivity()
                .as(JavaArchive.class);
    }
}
