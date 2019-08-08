/*
 * Copyright 1&1 Internet AG, https://github.com/1and1/
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
package net.oneandone.maven.plugins.activemarkdown;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;

/**
 * Generates an application file. Merges dependency jars into a single file, prepended with a launch shell script.
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.NONE)
public class RunMojo extends AbstractMojo {
    protected final World world;

    /**
     * Markdown file to process.
     */
    @Parameter(defaultValue = "${basedir}/documentation.md")
    protected String file;

    /**
     * True to generate man pages.
     */
    @Parameter(defaultValue = "false")
    protected boolean man;

    /**
     * True to generate man pages.
     */
    @Parameter(defaultValue = "${project.build.directory}/man")
    protected String mandir;

    public RunMojo() throws IOException {
        this(World.create());
    }

    public RunMojo(World world) {
        this.world = world;
    }

    public void execute() throws MojoExecutionException {
        try {
            doExecute();
        } catch (IOException e) {
            throw new MojoExecutionException("cannot generate application: " + e.getMessage(), e);
        }
    }

    private void doExecute() throws IOException {
        FileNode m;
        FileNode f;
        Markdown md;
        boolean problem;
        Ronn ronn;

        m = man ? world.file(mandir) : null;
        f = world.file(file);
        world.setWorking(f.getParent());
        ronn = Ronn.probe(world);
        getLog().info("docker ronn: " + ronn.docker);
        md = Markdown.run(ronn, f);
        problem = false;
        for (String broken : md.checkCrossReferences()) {
            getLog().error("broken reference: " + broken);
            problem = true;
        }
        if (problem) {
            throw new IOException("broken references in markdown file. See above messages for details");
        }
        if (m != null) {
            m.mkdirsOpt();
            getLog().debug(md.manpages(m));
        }
    }
}
