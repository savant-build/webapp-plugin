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

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Settings for the webapp plugin.
 *
 * @author Brian Pontarelli
 */
class WebappSettings {
  /**
   * Controls whether or not the WEB-INF/classes directory is cleaned. Defaults to {@code false}
   */
  boolean cleanClassesDirectory = false

  /**
   * Controls whether or not the web application resources are copied to the WEB-INF/classes directory during the build
   * process or not. Defaults to {@code false}.
   */
  boolean copyResources = false

  /**
   * The list of dependencies to include in the web application. This defaults to:
   * <p>
   * <pre>
   *   [
   *     [group: "compile", transitive: true, fetchSource: false, transitiveGroups: ["compile", "runtime"]],
   *     [group: "runtime", transitive: true, fetchSource: false, transitiveGroups: ["compile", "runtime"]]
   *   ]
   * </pre>
   */
  List<Map<String, Object>> dependencies = [
      [group: "compile", transitive: true, fetchSource: false, transitiveGroups: ["compile", "runtime"]],
      [group: "runtime", transitive: true, fetchSource: false, transitiveGroups: ["compile", "runtime"]]
  ]

  /**
   * The output directory where the project JAR files are placed. Defaults to {@code build/jars}
   */
  Path jarOutputDirectory = Paths.get("build/jars")

  /**
   * The web application directory inside the project. Defaults to {@code web}
   */
  Path webDirectory = Paths.get("web")

  /**
   * The directory where the web resources are stored. Defaults to {@code src/main/web-resources}
   */
  Path webResourceDirectory = Paths.get("src/main/web-resources")
}
