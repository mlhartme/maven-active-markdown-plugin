/**
 * This file is part of maven-activemarkdown-plugin.
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * maven-activemarkdown-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * maven-activemarkdown-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with maven-activemarkdown-plugin.  If not, see <http://www.gnu.org/licenses/>.
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
