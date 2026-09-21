// Créditos: oEnzoRibas
package br.com.codejr.podiss.backend.post;
import br.com.codejr.podiss.backend.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
/**
 * Validates upload size and recognized image signatures. This is not full image decoding or a malware scanner.
 *
 * @author oEnzoRibas
 */
@Component
public class ImageValidator {
    public record Image(byte[] bytes, String contentType) {}
    /**
     * Reads a bounded upload and derives its MIME type from bytes rather than client metadata.
     * @param file nonempty upload, at most 5 MiB
     * @return bytes and detected MIME type; bytes are not defensively copied
     * @throws ApiException for empty, oversized, unreadable or unrecognized uploads
     */
    public Image read(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Envie uma imagem não vazia.");
        if (file.getSize() > 5L * 1024 * 1024)
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "A imagem deve ter no máximo 5 MB.");
        try {
            byte[] bytes = file.getBytes();
            String mime = detect(bytes);
            if (mime == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Use imagem JPEG, PNG, GIF ou WebP válida.");
            return new Image(bytes, mime);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Não foi possível ler a imagem.");
        }
    }
    public static String detect(byte[] b) {
        if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255) return "image/jpeg";
        if (b.length >= 8 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71
            && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10) return "image/png";
        if (b.length >= 6) {
            String signature = new String(b, 0, 6, StandardCharsets.US_ASCII);
            if (signature.equals("GIF87a") || signature.equals("GIF89a")) return "image/gif";
        }
        if (b.length >= 12 && new String(b, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
            && new String(b, 8, 4, StandardCharsets.US_ASCII).equals("WEBP")) return "image/webp";
        return null;
    }
}
