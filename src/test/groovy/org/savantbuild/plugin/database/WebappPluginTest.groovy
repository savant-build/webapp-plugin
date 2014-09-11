/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.plugin.database

import org.savantbuild.dep.domain.Artifact
import org.savantbuild.dep.domain.Dependencies
import org.savantbuild.dep.domain.DependencyGroup
import org.savantbuild.dep.domain.License
import org.savantbuild.dep.domain.Version
import org.savantbuild.dep.workflow.FetchWorkflow
import org.savantbuild.dep.workflow.PublishWorkflow
import org.savantbuild.dep.workflow.Workflow
import org.savantbuild.dep.workflow.process.CacheProcess
import org.savantbuild.dep.workflow.process.URLProcess
import org.savantbuild.domain.Project
import org.savantbuild.output.Output
import org.savantbuild.output.SystemOutOutput
import org.savantbuild.plugin.webapp.WebappPlugin
import org.savantbuild.runtime.RuntimeConfiguration
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.testng.Assert.assertFalse
import static org.testng.Assert.assertTrue

/**
 * Tests the webapp plugin.
 *
 * @author Brian Pontarelli
 */
class WebappPluginTest {
  public static Path projectDir

  Output output

  Project project

  @BeforeSuite
  static void beforeSuite() {
    projectDir = Paths.get("")
    if (!Files.isRegularFile(projectDir.resolve("LICENSE"))) {
      projectDir = Paths.get("../database-plugin")
    }
  }

  @BeforeMethod
  void beforeMethod() {
    output = new SystemOutOutput(true)
    output.enableDebug()

    project = new Project(projectDir, output)
    project.group = "org.savantbuild.test"
    project.name = "database-plugin-test"
    project.version = new Version("1.0")
    project.license = License.Apachev2

    project.dependencies = new Dependencies(new DependencyGroup("compile", false, new Artifact("org.testng:testng:6.8.7:jar")))
    project.workflow = new Workflow(
        new FetchWorkflow(output,
            new CacheProcess(output, projectDir.resolve("build/cache").toString()),
            new URLProcess(output, "http://savant.inversoft.org", null, null)
        ),
        new PublishWorkflow(
            new CacheProcess(output, projectDir.resolve("build/cache").toString())
        )
    )
  }

  @Test
  void all() {
    WebappPlugin plugin = new WebappPlugin(project, new RuntimeConfiguration(), output)
    plugin.clean()
    assertFalse(Files.isDirectory(projectDir.resolve("web/WEB-INF/lib")))

    plugin.build()
    assertTrue(Files.isDirectory(projectDir.resolve("web/WEB-INF/lib")))
    assertTrue(Files.isRegularFile(projectDir.resolve("web/WEB-INF/lib/testng-6.8.7.jar")))
  }
}
