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

import org.savantbuild.dep.domain.ArtifactID
import org.savantbuild.dep.domain.ReifiedArtifact
import org.savantbuild.domain.Project
import org.savantbuild.io.FileTools
import org.savantbuild.output.Output
import org.savantbuild.plugin.dep.DependencyPlugin
import org.savantbuild.plugin.file.FilePlugin
import org.savantbuild.plugin.groovy.BaseGroovyPlugin
import org.savantbuild.runtime.RuntimeConfiguration

import java.nio.file.Files
import java.nio.file.Path

/**
 * Web application plugin.
 * <p/>
 * This plugin provides the ability to create an exploded web application in the project and then create a WAR from that
 * exploded web application directory.
 *
 * @author Brian Pontarelli
 */
class WebappPlugin extends BaseGroovyPlugin {
  FilePlugin filePlugin

  DependencyPlugin dependencyPlugin

  WebappSettings settings  = new WebappSettings()

  WebappPlugin(Project project, RuntimeConfiguration runtimeConfiguration, Output output) {
    super(project, runtimeConfiguration, output)
    filePlugin = new FilePlugin(project, runtimeConfiguration, output)
    dependencyPlugin = new DependencyPlugin(project, runtimeConfiguration, output)
  }

  /**
   * Cleans the web application by deleting all of the files in the WEB-INF/lib directory.
   */
  void clean() {
    FileTools.prune(project.directory.resolve(settings.webDirectory.resolve("WEB-INF/lib")))

    if (settings.cleanClassesDirectory) {
      FileTools.prune(project.directory.resolve(settings.webDirectory.resolve("WEB-INF/classes")))
    }
  }

  /**
   * Creates the web application by copying the dependencies and project JAR files into the WEB-INF/lib directory and
   * any web-resources into the WEB-INF/classes directory. Here's an example of using the plugin.
   * <p>
   * <pre>
   *   webapp.build()
   * </pre>
   */
  void build() {
    Path libDirectory = settings.webDirectory.resolve("WEB-INF/lib")
    dependencyPlugin.copy(to: libDirectory) {
      settings.dependencies.each { dep ->
        dependencies(dep)
      }
    }

    // Copy the project jars
    if (Files.isDirectory(project.directory.resolve(settings.jarOutputDirectory))) {
      filePlugin.copy(to: libDirectory) {
        fileSet(dir: settings.jarOutputDirectory)
      }
    }

    if (settings.copyResources) {
      // Copy the resources over
      filePlugin.copy(to: settings.webDirectory.resolve("WEB-INF/classes")) {
        fileSet(dir: settings.webResourceDirectory)
      }
    }
  }

  /**
   * Creates the web application archive by JARring the web directory. Here's an example of using the plugin.
   * <p>
   * <pre>
   *   webapp.war()
   * </pre>
   */
  void war() {
    ReifiedArtifact artifact = new ReifiedArtifact(new ArtifactID(project.group, project.name, project.name, "war"), project.version, project.license)
    filePlugin.jar(file: settings.jarOutputDirectory.resolve(artifact.getArtifactFile())) {
      fileSet(dir: settings.webDirectory)
    }
  }
}
