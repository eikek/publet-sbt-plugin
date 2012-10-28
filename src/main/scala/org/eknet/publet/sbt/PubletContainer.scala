/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 THIS CLASS IS INSPIRED BY THE Container CLASS OF XSBT-WEB-PLUGIN:

Copyright (c) 2011, Artyom Olshevskiy
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote
   products derived from this software without specific prior written
   permission.


THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.eknet.publet.sbt

import sbt._
import sbt.Keys._
import Classpaths._
import classpath.ClasspathUtilities._
import PubletPlugin.Keys._

/**
 * This class is very much inspired by `sbt-web-plugin` from Artyom Olshevskiy,
 * which you can find on github: https://github.com/siasia/xsbt-web-plugin.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.10.12 00:45
 */
class PubletContainer(name: String) {

  def Configuration = config(name).hide.extend(Runtime)
  def attribute = AttributeKey[ServiceRunner](name)

  private implicit def attributeToRunner[Runner](key: AttributeKey[Runner])(implicit state: State):Runner = state.get(key).get

  private implicit def stateToRunner(state: State): ServiceRunner = state.get(attribute).get

  object Impl {
    def eval[T](key: Project.ScopedKey[sbt.Task[T]])(implicit state: State):T =
      EvaluateTask.processResult(
        Project.runTask(key, state) map (_._2) getOrElse sys.error("Error getting " + key),
        state.log
      )

    private implicit def keyToResult[T](key: TaskKey[T])(implicit state: State): T = eval(key)

    def newRunner(ref: ProjectRef, state: State, wd: File, cd: File, pf: File) = {
      implicit val s = state
      val classpath = Set(cd) ++
        Build.data( (fullClasspath in (ref, Configuration)).filter(filterClasspath(_, pf))  )
      val loader = (scalaInstance in ref).loader

      state.put(attribute, new ServiceRunner(toLoader(classpath.distinct, loader), wd.getAbsolutePath))
    }
  }

  //needed for attaching this configuration to the project...
  def globalSettings = Seq(ivyConfigurations += Configuration)

  def containerSettings = Seq(
    managedClasspath <<= (classpathTypes, update) map {(ct, up) => managedJars(Configuration, ct, up)},
    fullClasspath <<= managedClasspath,
    port := 8088,
    workdir <<= target(dir => {
      dir / ("publetwork-"+name)
    }),
    cleanWorkdir <<= workdir map (dir => {
      IO.delete(dir)
    }),
    onLoad in Global <<= (onLoad in Global, thisProjectRef, workdir, classDirectory in Compile, target) {
      (onLoad, containerProject, wd, cd, pf) =>
        (state) => {
          Impl.newRunner(containerProject, onLoad(state), wd, cd, pf)
        }
    },
    onUnload in Global <<= (onUnload in Global) {
      (onUnload) =>
        (state) =>
          state.stop()
          onUnload(state)
    },
    start <<= (state, Keys.`package` in Compile) map ((state, _) => {
      state.start()
    }),
    stop <<= (state).map (state => {
      state.stop()
    })
  )

  def settings = globalSettings ++  inConfig(Configuration)(containerSettings)

  def filterClasspath(classpath: Attributed[File], target: File) = {
    if (classpath.data.getAbsolutePath.startsWith(target.getAbsolutePath))
      false
    else {
      val excludes = Set("scala-library.jar", "scala-compiler.jar")
      !excludes.contains(classpath.data.asFile.getName)
    }
  }
}
