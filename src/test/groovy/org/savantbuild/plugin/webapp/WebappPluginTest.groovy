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
package org.savantbuild.plugin.webapp

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream

import org.savantbuild.dep.domain.Artifact
import org.savantbuild.dep.domain.Dependencies
import org.savantbuild.dep.domain.DependencyGroup
import org.savantbuild.dep.domain.License
import org.savantbuild.dep.workflow.FetchWorkflow
import org.savantbuild.dep.workflow.PublishWorkflow
import org.savantbuild.dep.workflow.Workflow
import org.savantbuild.dep.workflow.process.CacheProcess
import org.savantbuild.dep.workflow.process.URLProcess
import org.savantbuild.domain.Project
import org.savantbuild.domain.Version
import org.savantbuild.output.Output
import org.savantbuild.output.SystemOutOutput
import org.savantbuild.runtime.RuntimeConfiguration
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test

import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertFalse
import static org.testng.Assert.assertNotNull
import static org.testng.Assert.assertTrue
import static org.testng.Assert.fail

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
      projectDir = Paths.get("../webapp-plugin")
    }
  }

  @BeforeMethod
  void beforeMethod() {
    output = new SystemOutOutput(true)
    output.enableDebug()

    project = new Project(projectDir.resolve("test-project"), output)
    project.group = "org.savantbuild.test"
    project.name = "test-project"
    project.version = new Version("1.0")
    project.licenses.add(License.parse("ApacheV2_0", null))

    project.dependencies = new Dependencies(new DependencyGroup("compile", false, new Artifact("org.testng:testng:6.8.7:jar")))
    project.workflow = new Workflow(
        new FetchWorkflow(output,
            new CacheProcess(output, projectDir.resolve("build/cache").toString()),
            new URLProcess(output, "https://repository.savantbuild.org", null, null)
        ),
        new PublishWorkflow(
            new CacheProcess(output, projectDir.resolve("build/cache").toString())
        )
    )
  }

  @Test
  void all() {
    WebappPlugin plugin = new WebappPlugin(project, new RuntimeConfiguration(), output)
    plugin.settings.cleanClassesDirectory = true
    plugin.settings.copyResources = true

    plugin.clean()
    assertFalse(Files.isDirectory(project.directory.resolve("web/WEB-INF/lib")))
    assertFalse(Files.isDirectory(project.directory.resolve("web/WEB-INF/classes")))

    plugin.build()
    assertTrue(Files.isDirectory(project.directory.resolve("web/WEB-INF/lib")))
    assertTrue(Files.isRegularFile(project.directory.resolve("web/WEB-INF/lib/testng-6.8.7.jar")))
    assertTrue(Files.isDirectory(project.directory.resolve("web/WEB-INF/classes")))
    assertTrue(Files.isRegularFile(project.directory.resolve("web/WEB-INF/classes/logging.properties")))

    plugin.war()
    assertJarContains(project.directory.resolve("build/wars/test-project-1.0.0.war"), "WEB-INF/lib/testng-6.8.7.jar", "WEB-INF/classes/logging.properties")
    assertJarFileEquals(project.directory.resolve("build/wars/test-project-1.0.0.war"), "WEB-INF/lib/testng-6.8.7.jar", project.directory.resolve("web/WEB-INF/lib/testng-6.8.7.jar"))
    assertJarFileEquals(project.directory.resolve("build/wars/test-project-1.0.0.war"), "WEB-INF/classes/logging.properties", project.directory.resolve("src/main/web-resources/logging.properties"))

    plugin.settings.cleanClassesDirectory = false
    plugin.settings.copyResources = false
    plugin.clean()
    assertFalse(Files.isDirectory(project.directory.resolve("web/WEB-INF/lib")))
    assertTrue(Files.isDirectory(project.directory.resolve("web/WEB-INF/classes")))
    assertTrue(Files.isRegularFile(project.directory.resolve("web/WEB-INF/classes/logging.properties")))

    // Just for clean up
    plugin.settings.cleanClassesDirectory = true
    plugin.clean()
    assertFalse(Files.isDirectory(project.directory.resolve("web/WEB-INF/lib")))
    assertFalse(Files.isDirectory(project.directory.resolve("web/WEB-INF/classes")))
    plugin.settings.cleanClassesDirectory = false

    Files.createDirectories(project.directory.resolve("web/WEB-INF/classes"))
    Files.write(project.directory.resolve("web/WEB-INF/classes/some-resource.txt"), "Hello world".getBytes())
    plugin.build()
    assertTrue(Files.isDirectory(project.directory.resolve("web/WEB-INF/lib")))
    assertTrue(Files.isRegularFile(project.directory.resolve("web/WEB-INF/lib/testng-6.8.7.jar")))
    assertTrue(Files.isDirectory(project.directory.resolve("web/WEB-INF/classes")))
    assertTrue(Files.isRegularFile(project.directory.resolve("web/WEB-INF/classes/some-resource.txt")))
    assertFalse(Files.isRegularFile(project.directory.resolve("web/WEB-INF/classes/logging.properties")))

    plugin.war()
    assertJarContains(project.directory.resolve("build/wars/test-project-1.0.0.war"), "WEB-INF/lib/testng-6.8.7.jar", "WEB-INF/classes/some-resource.txt")
    assertJarFileEquals(project.directory.resolve("build/wars/test-project-1.0.0.war"), "WEB-INF/lib/testng-6.8.7.jar", project.directory.resolve("web/WEB-INF/lib/testng-6.8.7.jar"))
    assertJarFileEquals(project.directory.resolve("build/wars/test-project-1.0.0.war"), "WEB-INF/classes/some-resource.txt", project.directory.resolve("web/WEB-INF/classes/some-resource.txt"))

    plugin.clean()
    assertFalse(Files.isDirectory(project.directory.resolve("web/WEB-INF/lib")))
    assertTrue(Files.isDirectory(project.directory.resolve("web/WEB-INF/classes")))
    assertTrue(Files.isRegularFile(project.directory.resolve("web/WEB-INF/classes/some-resource.txt")))
    assertFalse(Files.isRegularFile(project.directory.resolve("web/WEB-INF/classes/logging.properties")))
  }

  private static void assertJarContains(Path jarFile, String... entries) {
    JarFile jf = new JarFile(jarFile.toFile())
    entries.each({ entry -> assertNotNull(jf.getEntry(entry), "Jar [${jarFile}] is missing entry [${entry}]") })
    jf.close()
  }

  private static void assertJarFileEquals(Path jarFile, String entry, Path original) throws IOException {
    JarInputStream jis = new JarInputStream(Files.newInputStream(jarFile))
    JarEntry jarEntry = jis.getNextJarEntry()
    while (jarEntry != null && !jarEntry.getName().equals(entry)) {
      jarEntry = jis.getNextJarEntry()
    }

    if (jarEntry == null) {
      fail("Jar [" + jarFile + "] is missing entry [" + entry + "]")
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream()
    byte[] buf = new byte[1024]
    int length
    while ((length = jis.read(buf)) != -1) {
      baos.write(buf, 0, length)
    }

    println Files.getLastModifiedTime(original)
    assertEquals(Files.readAllBytes(original), baos.toByteArray())
    assertEquals(jarEntry.getSize(), Files.size(original))
  }
}
