package com.ucc.complaints.evidence;

import com.ucc.complaints.storage.ImageMetadataSanitizer;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

class ImageMetadataSanitizerTest {
    @Test
    void rewritesImageWithoutMetadataContainer() throws Exception {
        BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream input = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(source, "jpg", input));
        byte[] sanitized = new ImageMetadataSanitizer()
            .sanitize(new ByteArrayInputStream(input.toByteArray()));
        assertNotNull(ImageIO.read(new ByteArrayInputStream(sanitized)));
        assertTrue(sanitized.length > 0);
    }
}
