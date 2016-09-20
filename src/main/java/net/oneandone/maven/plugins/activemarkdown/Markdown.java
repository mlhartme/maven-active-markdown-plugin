/**
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
import net.oneandone.sushi.launcher.Launcher;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses special comments (http://stackoverflow.com/questions/4823468/comments-in-markdown) to implement "markdown actions":
 * [//] # (some code)     is an action header
 * [//] # (-)             is an action footer
 */
public class Markdown {
    public static void run(FileNode file, FileNode man) throws IOException {
        List<String> lines;

        file.checkFile();
        if (man != null) {
            man.mkdirsOpt();
        }
        lines = load(file);
        checkCrossReferences(lines);
        lines = actions(file.getWorld(), lines);
        file.writeLines(lines);
        if (man != null) {
            manpages(lines, man);
        }
    }

    private static void checkCrossReferences(List<String> lines) throws IOException {
        List<String> labels;
        int depth;
        int start;
        int end;
        int last;
        String l;

        labels = new ArrayList<>();
        for (String line : lines) {
            depth = depth(line);
            if (depth > 0) {
                labels.add(toLabel(line.substring(depth)));
            }
        }
        for (String line : lines) {
            last = 0;
            while (true) {
                start = line.indexOf("](#", last);
                if (start == -1) {
                    break;
                }
                start += 3;
                end = line.indexOf(')', start);
                if (end == -1) {
                    throw new IOException("missing )");
                }
                l = line.substring(start, end);
                if (!labels.contains(l)) {
                    System.out.println("cross reference not found: " + l);
                }
                last = end + 1;
            }
        }
    }

    private static String toLabel(String str) {
        str = str.trim();
        str = str.toLowerCase();
        return str.replace(' ', '-');
    }

    private static void manpages(List<String> lines, FileNode dir) throws IOException {
        FileNode roff;
        Launcher launcher;
        List<FileNode> ronns;

        for (int i = 0; i < lines.size(); i++) {
            Manpage.check(dir, lines, i);
        }
        ronns = dir.find("*.ronn");
        launcher = dir.launcher("ronn", "--roff");
        for (FileNode file :ronns) {
            launcher.arg(file.getName());
        }
        System.out.println(launcher.exec());
        for (FileNode file : ronns) {
            file.deleteFile();
            roff = file.getParent().join(Strings.removeRight(file.getName(), ".ronn"));
            roff.gzip(roff.getParent().join(roff.getName() + ".gz"));
            roff.deleteFile();
        }
    }

    public static List<String> load(FileNode src) throws IOException {
        src.checkFile();
        return src.readLines();
    }

    private static List<String> synopsis(List<String> lines) {
        int count;
        boolean collect;
        List<String> result;

        result = new ArrayList<>();
        collect = false;
        for (String line : lines) {
            count = depth(line);
            if (count > 0) {
                collect = isSynopsis(line);
            } else {
                if (collect) {
                    if (isAction(line)) {
                        // TODO
                        // this is to skip recursive ALL_SYNOPSIS, but it actually skips all actions
                    } else {
                        result.add(line);
                    }
                }
            }
        }
        return result;
    }

    private static List<String> actions(World world, List<String> lines) throws IOException {
        String startLine;
        String endLine;
        List<String> result;
        String code;
        List<String> value;
        int next;

        result = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            startLine = lines.get(i);
            if (isAction(startLine)) {
                code = getActionCode(startLine);
                if ("ALL_SYNOPSIS".equals(code)) {
                    value = synopsis(lines);
                } else if (code.startsWith("include ")) {
                    value = load(world.file(code.substring(8)));
                } else {
                    throw new IOException("not found: " + code);
                }
                next = nextAction(lines, i + 1);
                if (next == -1) {
                    throw new IOException("missing end marker for action " + code);
                }
                endLine = lines.get(next);
                if (!"-".equals(getActionCode(endLine))) {
                    throw new IOException("unexpected end marker for action " + code + ": " + getActionCode(endLine));
                }
                result.add(startLine);
                result.addAll(value);
                result.add(endLine);
                i = next; // caution: no +1 because that done by the for loop
            } else {
                result.add(startLine);
            }
        }
        return result;
    }

    private static int nextAction(List<String> lines, int start) {
        for (int i = start; i < lines.size(); i++) {
            if (isAction(lines.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isAction(String line) {
        return line.startsWith("[//]");
    }

    private static String getActionCode(String line) throws IOException {
        final String start = "[//]: # (";

        if (!line.startsWith(start) || !line.endsWith(")")) {
            throw new IOException("invalid action line: " + line);
        }
        return line.substring(start.length(), line.length() - 1);
    }

    static int depth(String header) {
        int count;

        count = 0;
        for (int i = 0, max = header.length(); i < max; i++) {
            if (header.charAt(i) != '#') {
                return count;
            }
            count++;
        }
        return count;
    }

    static String trimHeader(String header) {
        return header.substring(depth(header)).trim();
    }

    public static boolean isSynopsis(String line) {
        return line.endsWith("# SYNOPSIS");
    }

}