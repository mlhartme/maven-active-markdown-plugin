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
import org.junit.Test;

import java.io.IOException;

public class ManTest {
    private static final World WORLD = World.createMinimal();

    @Test
    public void man() throws IOException {
        FileNode in;
        FileNode file;
        FileNode work;

        // don't use tmp files, they are not visible to docker on mac os
        in = WORLD.guessProjectHome(getClass()).join("src/test/synopsis.in");
        work = WORLD.guessProjectHome(getClass()).join("target/man-test").deleteTreeOpt().mkdir();
        file = work.join("file");
        in.copyFile(file);
        Markdown.run(file).manpages(work.join("dir").mkdir());
    }
}
