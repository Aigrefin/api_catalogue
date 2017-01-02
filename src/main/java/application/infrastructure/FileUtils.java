package application.infrastructure;

import application.infrastructure.models.Api;
import application.infrastructure.models.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileUtils {

    @Autowired
    ApplicationConfiguration applicationConfiguration;

    public File moveToUploadDirectory(MultipartFile uploadedFile, Api api) throws IOException {
        String uniqueString = api.getName() + "-" + api.getVersion();
        int uniqueNumber = uniqueString.hashCode();
        String fileName = "specification_"+String.valueOf(uniqueNumber);
        File destinationFile = new File(applicationConfiguration.specificationsFolder, fileName);
        uploadedFile.transferTo(destinationFile);
        return destinationFile;
    }

    public void deleteFile(File filePath) {
        filePath.delete();
    }
}
