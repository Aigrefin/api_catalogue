package application.infrastructure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileUtilsTest {

    @InjectMocks
    FileUtils fileUtils;


    @Test
    public void shouldExtractFileContent_FromMultipartFile() throws IOException {
        // Given
        MultipartFile uploadedFile = mock(MultipartFile.class);
        String fileContent = "fileContent";
        byte[] fileBytesContent = fileContent.getBytes();
        when(uploadedFile.getBytes()).thenReturn(fileBytesContent);

        // When
        String result = fileUtils.multipartfileToString(uploadedFile);

        // Then
        assertThat(result).isEqualTo(fileContent);
    }
}