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
import net.oneandone.sushi.launcher.Failure;
import net.oneandone.sushi.launcher.Launcher;

import java.io.IOException;
import java.util.List;

public class Ronn {
    public static Ronn probe(World world) throws IOException {
        try {
            world.getWorking().exec("which", "ronn");
            return new Ronn(false);
        } catch (Failure e) {
            // fall-through
        }
        try {
            world.getWorking().exec("which", "docker");
            return new Ronn(true);
        } catch (Failure e) {
            throw new IOException("cannot invoke ronn, neither docker nor local ronn is available");
        }
    }

    public final boolean docker;

    public Ronn(boolean docker) {
        this.docker = docker;
    }

    public String run(FileNode dir, List<Manpage> lst) throws Failure {
        Launcher launcher;

        launcher = launcher(dir);
        for (Manpage mp : lst) {
            launcher.arg(mp.file.getName());
        }
        return launcher.exec();
    }

    private Launcher launcher(FileNode dir) {
        if (docker) {
            return dockerRonn(dir);
        } else {
            return localRonn(dir);
        }
    }

    private Launcher dockerRonn(FileNode dir) {
        Launcher launcher;

        launcher = dir.launcher("docker", "run", "--rm", "-i");
        launcher.arg("--mount", "type=bind,source=" + dir.getAbsolute() + ",dst=" + dir.getAbsolute());
        launcher.arg("--workdir", dir.getAbsolute());
        launcher.arg("mlhartme/active-markdown-ronn:1.0.0");
        launcher.arg("ronn", "--roff");
        System.out.println("launcher: " + launcher);
        return launcher;
    }

    private Launcher localRonn(FileNode dir) {
        Launcher launcher;

        launcher = dir.launcher("ronn", "--roff");
        return launcher;
    }
}
