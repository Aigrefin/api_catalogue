package application.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;

@Service
public class FileUtils {

    public String multipartfileToString(MultipartFile uploadedFile) throws IOException {
        byte[] fileByteContent = uploadedFile.getBytes();
        return new String(fileByteContent, Charset.forName("UTF-8"));
    }
}
