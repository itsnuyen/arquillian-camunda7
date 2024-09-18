package com.camunda.consulting;

import jakarta.inject.Inject;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
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

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@RunWith(Arquillian.class)
public class ProcessUnitTest {
    protected static JavaArchive CACHED_CLIENT_ASSET;
    protected static JavaArchive CACHED_ENGINE_CDI_ASSET;

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive base = ShrinkWrap.create(WebArchive.class);

        JavaArchive[] jakartaCdi = Maven.configureResolver()
                .workOffline()
                .loadPomFromFile("pom.xml")
                .resolve("org.camunda.bpm.javaee:camunda-ejb-client-jakarta")
                .withTransitivity()
                .as(JavaArchive.class);
        JavaArchive[] engineCdi = Maven.configureResolver()
                .workOffline()
                .loadPomFromFile("pom.xml")
                .resolve("org.camunda.bpm:camunda-engine-cdi-jakarta")
                .withTransitivity()
                .as(JavaArchive.class);

        JavaArchive[] test = Maven.configureResolver()
                .workOffline()
                .loadPomFromFile("pom.xml")
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .as(JavaArchive.class);
        JavaArchive[] bpmAware = Maven.configureResolver()
                .workOffline()
                .loadPomFromFile("pom.xml")
                .resolve("org.camunda.bpm:camunda-bpm-assert")
                .withTransitivity()
                .as(JavaArchive.class);
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies()
                .resolve()
                .withTransitivity()
                .asFile();
        var archive = base
                .addPackages(true, "com.camunda.consulting")
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("process.bpmn")
                .addClass(ProcessUnitTest.class)
                .addAsLibraries(jakartaCdi, engineCdi, test, bpmAware);
//        System.out.println(archive.toString(true));
        return archive;
    }


    @Inject
    private ProcessEngine processEngine;
    private ProcessEngineService processEngineService;

    protected RuntimeService runtimeService;


    @Before
    public void setupBeforeTest() {
        processEngineService = BpmPlatform.getProcessEngineService();
        processEngine = processEngineService.getDefaultProcessEngine();
        runtimeService = processEngine.getRuntimeService();
    }

    @Test
    public void testHappyPath() {
        // Drive the process by API and assert correct behavior by camunda-bpm-assert
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(ProcessConstants.PROCESS_DEFINITION_KEY);
        assertThat(processInstance).isNotNull();
//        System.out.println(processInstance.toString());
//        assertThat(processInstance.getProcessDefinitionId()).isEqualTo(ProcessConstants.PROCESS_DEFINITION_KEY);
        BpmnAwareTests.assertThat(processInstance).isEnded();
    }

}
