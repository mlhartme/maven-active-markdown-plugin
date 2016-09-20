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

public class Markdown {
    public static void run(FileNode file, FileNode man) throws IOException {
        Markdown md;

        file.checkFile();
        if (man != null) {
            man.mkdirsOpt();
        }
        md = load(file);
        md.checkCrossReferences();
        md.actions(file.getWorld());
        file.writeLines(md.lines);
        if (man != null) {
            md.manpages(man);
        }
    }

    public static Markdown load(FileNode src) throws IOException {
        Markdown result;

        src.checkFile();
        result = new Markdown();
        result.lines.addAll(src.readLines());
        return result;
    }


    //--

    private final List<String> lines;

    public Markdown() {
        this.lines = new ArrayList<>();
    }

    public void checkCrossReferences() throws IOException {
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

    public void manpages(FileNode dir) throws IOException {
        List<Manpage> lst;
        Manpage p;
        FileNode roff;
        Launcher launcher;

        lst = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            p = Manpage.check(dir, lines, i);
            if (p != null) {
                lst.add(p);
            }
        }
        launcher = dir.launcher("ronn", "--roff");
        for (Manpage mp : lst) {
            launcher.arg(mp.file.getName());
        }
        System.out.println(launcher.exec());
        for (Manpage mp : lst) {
            mp.file.deleteFile();
            roff = mp.file.getParent().join(Strings.removeRight(mp.file.getName(), ".ronn"));
            roff.gzip(roff.getParent().join(roff.getName() + ".gz"));
            roff.deleteFile();
        }
    }

    private List<String> synopsis() {
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

    private void actions(World world) throws IOException {
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
                    value = synopsis();
                } else if (code.startsWith("include ")) {
                    value = world.file(code.substring(8)).checkFile().readLines();
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
        lines.clear();
        lines.addAll(result);
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