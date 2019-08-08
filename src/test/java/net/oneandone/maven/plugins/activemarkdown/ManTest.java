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
        Markdown.run(Ronn.probe(WORLD), file).manpages(work.join("dir").mkdir());
    }
}
