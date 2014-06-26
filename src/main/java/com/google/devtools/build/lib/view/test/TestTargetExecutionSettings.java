// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.view.test;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.devtools.build.lib.actions.Artifact;
import com.google.devtools.build.lib.packages.TargetUtils;
import com.google.devtools.build.lib.view.FilesToRunProvider;
import com.google.devtools.build.lib.view.RuleConfiguredTarget.Mode;
import com.google.devtools.build.lib.view.RuleContext;
import com.google.devtools.build.lib.view.RunfilesSupport;
import com.google.devtools.build.lib.view.TransitiveInfoCollection;
import com.google.devtools.build.lib.view.config.BuildConfiguration;
import com.google.devtools.build.lib.view.config.RunUnder;

import java.util.List;
import java.util.Map;

/**
 * Container for common test execution settings shared by all
 * all TestRunnerAction instances for the given test target.
 */
public final class TestTargetExecutionSettings {

  private final List<String> testArguments;
  private final Map<String, String> testEnv;
  private final String testFilter;
  private final int totalShards;
  private final RunUnder runUnder;
  private final Artifact runUnderExecutable;
  private final Artifact executable;
  private final Artifact runfilesManifest;
  private final Artifact runfilesInputManifest;
  private final Artifact instrumentedFileManifest;

  TestTargetExecutionSettings(RuleContext ruleContext, RunfilesSupport runfiles,
      Artifact executable, Artifact instrumentedFileManifest, int shards) {
    Preconditions.checkArgument(TargetUtils.isTestRule(ruleContext.getRule()));
    Preconditions.checkArgument(shards >= 0);
    BuildConfiguration config = ruleContext.getConfiguration();

    List<String> targetArgs = runfiles.getArgs();
    testArguments = targetArgs.isEmpty()
      ? config.getTestArguments()
      : ImmutableList.copyOf(Iterables.concat(targetArgs, config.getTestArguments()));

    // Environment variables from BuildConfiguration override
    // environment variables passed in through extraEnv.
    testEnv = ImmutableMap.<String, String>builder()
        .putAll(config.getTestEnv())
        .build();

    totalShards = shards;
    runUnder = config.getRunUnder();
    runUnderExecutable = getRunUnderExecutable(ruleContext);

    this.testFilter = config.getTestFilter();
    this.executable = executable;
    this.runfilesManifest = runfiles.getRunfilesManifest();
    this.runfilesInputManifest = runfiles.getRunfilesInputManifest();
    this.instrumentedFileManifest = instrumentedFileManifest;
  }

  private static Artifact getRunUnderExecutable(RuleContext ruleContext) {
    TransitiveInfoCollection runUnderTarget = ruleContext
        .getPrerequisite(":run_under", Mode.DATA);
    return runUnderTarget == null
        ? null
        : runUnderTarget.getProvider(FilesToRunProvider.class).getExecutable();
  }

  public List<String> getArgs() {
    return testArguments;
  }

  public Map<String, String> getTestEnv() {
    return testEnv;
  }

  public String getTestFilter() {
    return testFilter;
  }

  public int getTotalShards() {
    return totalShards;
  }

  public RunUnder getRunUnder() {
    return runUnder;
  }

  public Artifact getRunUnderExecutable() {
    return runUnderExecutable;
  }

  public Artifact getExecutable() {
    return executable;
  }

  /**
   * Returns the runfiles manifest for this test.
   *
   * <p>This returns either the input manifest outside of the runfiles tree,
   * if blaze is run with --nobuild_runfile_links or the manifest inside the
   * runfiles tree, if blaze is run with --build_runfile_links.
   *
   * @see com.google.devtools.build.lib.view.RunfilesSupport#getRunfilesManifest()
   */
  public Artifact getManifest() {
    return runfilesManifest;
  }

  /**
   * Returns the input runfiles manifest for this test.
   *
   * <p>This always returns the input manifest outside of the runfiles tree.
   *
   * @see com.google.devtools.build.lib.view.RunfilesSupport#getRunfilesInputManifest()
   */
  public Artifact getInputManifest() {
    return runfilesInputManifest;
  }

  /**
   * Returns instrumented file manifest or null if code coverage is not
   * collected.
   */
  public Artifact getInstrumentedFileManifest() {
    return instrumentedFileManifest;
  }
}
