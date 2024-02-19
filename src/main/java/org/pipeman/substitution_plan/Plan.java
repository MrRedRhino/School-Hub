package org.pipeman.substitution_plan;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import org.pipeman.utils.LazyInitializer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Function;

public class Plan {
    private static final String SPIRE_WARNING = "Evaluation Warning : The document was created with Spire.PDF for java.";
    private static final MessageDigest SHA_DIGEST;
    private final LazyInitializer<String> html;
    private final LazyInitializer<PlanData> data;
    private final LazyInitializer<byte[]> image;
    private final byte[] pdf;
    private final PdfDocument document;

    static {
        try {
            SHA_DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Plan(byte[] data) {
        this.pdf = data;
        this.document = new PdfDocument(data);
        this.html = new LazyInitializer<>(() -> runWithPdfDocument(this::convertToHtml));
        this.data = new LazyInitializer<>(() -> runWithPdfDocument(PlanData::from));
        this.image = new LazyInitializer<>(() -> runWithPdfDocument(this::convertToImage));
    }

    public String html() {
        return html.get();
    }

    public PlanData data() {
        return data.get();
    }

    public byte[] image() {
        return image.get();
    }

    public byte[] pdf() {
        return pdf;
    }

    private String convertToHtml(PdfDocument document) {
        OutputStream output = new ByteArrayOutputStream();
        document.saveToStream(output, FileFormat.HTML);
        return output.toString().replace(SPIRE_WARNING, "");
    }

    private byte[] convertToImage(PdfDocument pdf) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            BufferedImage image = pdf.saveAsImage(0);

            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 460, 16);
            g2d.dispose();

            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }

    private synchronized <T> T runWithPdfDocument(Function<PdfDocument, T> action) {
        return action.apply(document);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Plan plan = (Plan) o;
        return Arrays.equals(pdf, plan.pdf);
    }

    public byte[] getHash() {
        return SHA_DIGEST.digest(pdf);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pdf);
    }
}
