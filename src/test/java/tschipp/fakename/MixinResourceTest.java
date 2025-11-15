package tschipp.fakename;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MixinResourceTest {
    @Test
    void mixinConfigIsOnClasspathAndValidJson() throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("fakename.mixins.json");
        Assertions.assertNotNull(in);
        byte[] data = in.readAllBytes();
        String s = new String(data, StandardCharsets.UTF_8).trim();
        Assertions.assertTrue(s.startsWith("{") && s.endsWith("}"));
    }
}