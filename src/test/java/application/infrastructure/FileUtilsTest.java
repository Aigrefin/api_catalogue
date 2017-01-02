package application.infrastructure;

import application.infrastructure.models.Api;
import application.infrastructure.models.ApplicationConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FileUtilsTest {

    @InjectMocks
    FileUtils fileUtils;

    @Mock
    ApplicationConfiguration applicationConfiguration;

    @Test
    public void shouldMoveUploadedFile_ToUploadDirectory() throws IOException {
        // Given
        MultipartFile uploadedFile = mock(MultipartFile.class);
        applicationConfiguration.specificationsFolder = "/some/path";
        Api api = new Api();
        api.setName("test");
        api.setVersion("1.2");
        int uniqueNumber = "test-1.2".hashCode();

        // When
        fileUtils.moveToUploadDirectory(uploadedFile, api);

        // Then
        ArgumentCaptor<File> fileArgumentCaptor = ArgumentCaptor.forClass(File.class);
        verify(uploadedFile).transferTo(fileArgumentCaptor.capture());
        File transferredFile = fileArgumentCaptor.getValue();
        assertThat(transferredFile.getAbsolutePath()).isEqualTo("/some/path/specification_"+uniqueNumber);
    }

    @Test
    public void shouldReturnDestinationFile() throws IOException {
        // Given
        MultipartFile uploadedFile = mock(MultipartFile.class);

        // When
        File result = fileUtils.moveToUploadDirectory(uploadedFile, mock(Api.class));

        // Then
        ArgumentCaptor<File> fileArgumentCaptor = ArgumentCaptor.forClass(File.class);
        verify(uploadedFile).transferTo(fileArgumentCaptor.capture());
        File transferredFile = fileArgumentCaptor.getValue();
        assertThat(transferredFile).isEqualTo(result);
    }

    @Test
    public void shouldDeleteGivenFile() {
        // Given
        File file = mock(File.class);

        // When
        fileUtils.deleteFile(file);

        // Then
        verify(file).delete();
    }
}