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
        return launcher;
    }

    private Launcher localRonn(FileNode dir) {
        Launcher launcher;

        launcher = dir.launcher("ronn", "--roff");
        return launcher;
    }
}
