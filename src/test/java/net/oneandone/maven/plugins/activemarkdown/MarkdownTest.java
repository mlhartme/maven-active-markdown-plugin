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
        Markdown.run(file);
        assertEquals(expected, file.readString());
    }
}
