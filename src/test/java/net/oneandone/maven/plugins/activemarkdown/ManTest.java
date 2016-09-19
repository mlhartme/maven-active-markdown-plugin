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

        in = WORLD.guessProjectHome(getClass()).join("src/test/synopsis.in");
        file = WORLD.getTemp().createTempFile();
        file.writeString(in.readString());
        Markdown.run(file, WORLD.getTemp().createTempDirectory());
        System.out.println("done");
    }
}
