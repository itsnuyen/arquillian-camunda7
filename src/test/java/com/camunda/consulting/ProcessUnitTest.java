package com.camunda.consulting;

import jakarta.inject.Inject;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@RunWith(Arquillian.class)
public class ProcessUnitTest {
//
//    @Rule
//    public ProcessEngineRule processEngineRule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive base = ShrinkWrap.create(WebArchive.class);
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies()
                .resolve()
                .withTransitivity()
                .asFile();
        return base
                .addPackages(true, "com.camunda.consulting")
                .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsResource("process.bpmn")
                .addAsLibraries(libs);
    }

    @Inject
    private ProcessEngine processEngine;

    @Before
    public void setup() {
//        ProcessEngineAssertions.init(processEngineRule.getProcessEngine());
//        processEngineRule.getRepositoryService().createDeployment().addClasspathResource("process.bpmn").deploy();
        if (processEngine == null) {
            processEngine = ProcessEngineConfiguration
                    .createStandaloneInMemProcessEngineConfiguration()
                    .buildProcessEngine();
        }
        init(processEngine);
    }

    @Test
    public void testHappyPath() {
        // Drive the process by API and assert correct behavior by camunda-bpm-assert
        ProcessInstance processInstance = runtimeService()
                .startProcessInstanceByKey(ProcessConstants.PROCESS_DEFINITION_KEY);
        assertThat(processInstance).isEnded();
    }

}
