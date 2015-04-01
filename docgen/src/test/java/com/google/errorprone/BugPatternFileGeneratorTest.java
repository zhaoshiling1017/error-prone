/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.errorprone;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import com.google.common.io.CharStreams;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@RunWith(JUnit4.class)
public class BugPatternFileGeneratorTest {

  @Rule
  public TemporaryFolder tmpfolder = new TemporaryFolder();

  private Path exampleDir;
  private Path wikiDir;
  private Path exampleDirBase;
  private Path explanationDirBase;

  @Before
  public void setUp() throws Exception {
    wikiDir = tmpfolder.newFolder("wiki").toPath();
    exampleDirBase = tmpfolder.newFolder("examples").toPath();
    explanationDirBase = tmpfolder.newFolder("explanations").toPath();
    exampleDir = exampleDirBase.resolve("com/google/errorprone/bugpatterns");
    Files.createDirectories(exampleDir);
    Files.write(exampleDir.resolve("DeadExceptionPositiveCase.java"),
        Arrays.asList("here is an example"), UTF_8);
  }

  private static final String BUGPATTERN_LINE =
      "com.google.errorprone.bugpatterns.DeadException\t"
      + "DeadException\tThrowableInstanceNeverThrown\tJDK\tERROR\tMATURE\tSUPPRESS_WARNINGS\t"
      + "com.google.errorprone.BugPattern.NoCustomSuppression\t"
      + "Exception created but not thrown\t"
      + "The exception is created with new, but is not thrown, and the reference is lost.\n";
  
  private static final String BUGPATTERN_LINE_SIDECAR =
      "com.google.errorprone.bugpatterns.DeadException\t"
      + "DeadException\tThrowableInstanceNeverThrown\tJDK\tERROR\tMATURE\tSUPPRESS_WARNINGS\t"
      + "com.google.errorprone.BugPattern.NoCustomSuppression\t"
      + "Exception created but not thrown\t\n";

  // Assert that the generator produces the same output it did before.
  // This is brittle, but you can open the golden file
  // src/test/resources/com/google/errorprone/DeadException.md
  // in the same Jekyll environment you use for prod, and verify it looks good.
  @Test
  public void regressionTest_frontmatter_pygments() throws Exception {
    BugPatternFileGenerator generator =
        new BugPatternFileGenerator(wikiDir, exampleDirBase, explanationDirBase, true, true);
    generator.processLine(BUGPATTERN_LINE);
    String expected = CharStreams.toString(new InputStreamReader(
        getClass().getResourceAsStream("DeadException_frontmatter_pygments.md"), UTF_8));
    String actual = CharStreams.toString(
        Files.newBufferedReader(wikiDir.resolve("DeadException.md"), UTF_8));
    System.err.println(actual);
    System.err.println(expected);
    assertEquals(expected.trim(), actual.trim());
  }

  @Test
  public void regressionTest_nofrontmatter_gfm() throws Exception {
    BugPatternFileGenerator generator =
        new BugPatternFileGenerator(wikiDir, exampleDirBase, explanationDirBase, false, false);
    generator.processLine(BUGPATTERN_LINE);
    String expected = CharStreams.toString(new InputStreamReader(
        getClass().getResourceAsStream("DeadException_nofrontmatter_gfm.md"), UTF_8));
    String actual = new String(Files.readAllBytes(wikiDir.resolve("DeadException.md")), UTF_8);
    assertEquals(expected.trim(), actual.trim());
  }

  @Test
  public void regressionTest_sidecar() throws Exception {
    BugPatternFileGenerator generator =
        new BugPatternFileGenerator(wikiDir, exampleDirBase, explanationDirBase, false, false);
    Files.write(explanationDirBase.resolve("DeadException.md"),
        Arrays.asList(
            "The exception is created with new, but is not thrown, and the reference is lost."), 
            UTF_8);
    generator.processLine(BUGPATTERN_LINE_SIDECAR);
    String expected = CharStreams.toString(new InputStreamReader(
        getClass().getResourceAsStream("DeadException_nofrontmatter_gfm.md"), UTF_8));
    String actual = new String(Files.readAllBytes(wikiDir.resolve("DeadException.md")), UTF_8);
    assertEquals(expected.trim(), actual.trim());
  }
}
