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

import static org.junit.Assert.assertEquals;


public class MarkdownTest {
    private static final World WORLD = World.createMinimal();

    @Test
    public void empty() throws IOException {
        check("", "");
    }

    @Test
    public void synopsisEmpty() throws IOException {
        check(l("[//]: # (ALL_SYNOPSIS)",
                "[//]: # (-)",
                "blink"
                ),
              l("[//]: # (ALL_SYNOPSIS)",
                "blub",
                "[//]: # (-)",
                      "blink")
                );
    }

    @Test
    public void synopsis() throws IOException {
        checkFile("synopsis");
    }

    private static String l(String ... lines) {
        StringBuilder builder;

        builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
            builder.append('\n');
        }
        return builder.toString();
    }

    private void checkFile(String name) throws IOException {
        FileNode in;
        FileNode out;

        in = WORLD.guessProjectHome(getClass()).join("src/test", name + ".in");
        out = in.getParent().join(name + ".out");
        check(out.readString(), in.readString());
    }

    private void check(String expected, String actual) throws IOException {
        FileNode file;

        file = WORLD.getTemp().createTempFile();
        file.writeString(actual);
        Markdown.run(Ronn.probe(WORLD), file);
        assertEquals(expected, file.readString());
    }
}
