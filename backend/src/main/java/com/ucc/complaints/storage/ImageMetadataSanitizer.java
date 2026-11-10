package com.ucc.complaints.storage;

import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public class ImageMetadataSanitizer {
    public byte[] sanitize(InputStream input) throws IOException {
        BufferedImage image = ImageIO.read(input);
        if (image == null) {
            throw new IOException("El archivo no es una imagen compatible.");
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", output)) {
            throw new IOException("No fue posible sanitizar la imagen.");
        }
        return output.toByteArray();
    }

}
