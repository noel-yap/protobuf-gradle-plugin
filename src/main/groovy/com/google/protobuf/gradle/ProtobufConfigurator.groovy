/*
 * Copyright (c) 2015, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.protobuf.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.SourceSet
import org.gradle.util.ConfigureUtil

/**
 * The main configuration block exposed as {@code protobuf} in the build script.
 */
public class ProtobufConfigurator {
  private final Project project
  private final GenerateProtoTaskCollection tasks
  private final ToolsLocator tools

  /**
   * The base directory of generated files. The default is
   * "${project.buildDir}/generated/source/proto".
   */
  public String generatedFilesBaseDir

  public ProtobufConfigurator(Project project, FileResolver fileResolver) {
    this.project = project
    if (Utils.isAndroidProject(project)) {
      tasks = new AndroidGenerateProtoTaskCollection()
    } else {
      tasks = new JavaGenerateProtoTaskCollection()
    }
    tools = new ToolsLocator(project)
    generatedFilesBaseDir = "${project.buildDir}/generated/source/proto"
  }

  //===========================================================================
  //         Configuration methods
  //===========================================================================

  /**
   * Locates the protoc executable. The closure will be manipulating an
   * ExecutableLocator.
   */
  public void protoc(Closure configureClosure) {
    ConfigureUtil.configure(configureClosure, tools.protoc)
  }

  /**
   * Locate the codegen plugin executables. The closure will be manipulating a
   * NamedDomainObjectContainer<ExecutableLocator>.
   */
  public void plugins(Closure configureClosure) {
    ConfigureUtil.configure(configureClosure, tools.plugins)
  }

  /**
   * Configures the generateProto tasks in the given closure.  The closure will
   * be manipulating a JavaGenerateProtoTaskCollection or an
   * AndroidGenerateProtoTaskCollection depending on whether the project is
   * Java or Android.
   */
  public void generateProtoTasks(Closure configureClosure) {
    // TODO(zhangkun83): make sure to run it after tasks are generated
    project.afterEvaluate {
      ConfigureUtil.configure(configureClosure, tasks)
    }
  }

  public class GenerateProtoTaskCollection {
    public Collection<GenerateProtoTask> all() {
      return project.tasks.findAll { task ->
        task instanceof GenerateProtoTask
      }
    }
  }

  public class AndroidGenerateProtoTaskCollection
      extends GenerateProtoTaskCollection {
    public Collection<GenerateProtoTask> ofFlavor(String flavor) {
      return []
    }
  }

  public class JavaGenerateProtoTaskCollection
      extends GenerateProtoTaskCollection {
    public Collection<GenerateProtoTask> ofSourceSet(String sourceSet) {
      return all().findAll { task ->
        task.sourceSet.name == sourceSet
      }
    }
  }
}
