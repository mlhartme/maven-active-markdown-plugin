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

import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Manpage {
    public static Manpage check(FileNode dir, List<String> lines, int current) throws IOException {
        int start;
        String header;
        String line;
        int depth;
        String name;
        Manpage result;

        line = lines.get(current);
        if (!Markdown.isSynopsis(line)) {
            return null;
        }
        for (start = current - 1; start >= 0; start--) {
            header = lines.get(start);
            depth = Markdown.depth(header);
            if (depth > 0) {
                if (Markdown.depth(line) - 1 != depth) {
                    throw new IOException("nesting error");
                }
                header = header.substring(depth - 1);
                name = Markdown.trimHeader(header);
                result = new Manpage(header, depth, dir.join(name + ".md"));
                for (int i = start + 1; i < lines.size(); i++) {
                    if (!result.add(lines.get(i))) {
                        break;
                    }
                }
                result.write();
                return result;
            }
        }
        throw new IOException("missing header");
    }

    private final String title;
    private final List<String> body;
    private final int depth;
    public final FileNode file;

    public Manpage(String title, int depth, FileNode file) {
        this.title = title;
        this.body = new ArrayList<>();
        this.depth = depth;
        this.file = file;
    }

    public boolean add(String line) {
        int d;

        d = Markdown.depth(line);
        if (d > 0 && d <= depth) {
            return false;
        } else {
            if (d > 0) {
                line = line.substring(depth - 1);
            }
            body.add(line);
            return true;
        }
    }

    private void write() throws IOException {
        boolean inHeader;

        try (Writer dest = file.newWriter()) {
            dest.write(title);
            dest.write(" -- ");
            inHeader = true;
            for (String line : body) {
                if (inHeader) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    if (Markdown.depth(line) > 0) {
                        inHeader = false;
                        dest.write("\n\n");
                        dest.write(line);
                        dest.write('\n');
                    } else {
                        dest.write(line);
                        dest.write(' ');
                    }
                } else {
                    dest.write(line);
                    dest.write('\n');
                }
            }
        }
    }
}
