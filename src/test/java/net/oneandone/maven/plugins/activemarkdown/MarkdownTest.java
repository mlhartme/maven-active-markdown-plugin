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

    private void check(String expected, String actual) throws IOException {
        FileNode file;

        file = WORLD.getTemp().createTempFile();
        file.writeString(actual);
        Markdown.run(file, null);
        assertEquals(expected, actual);
    }
}
