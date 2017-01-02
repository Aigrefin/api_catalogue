package application.services;

import application.infrastructure.ApiRepository;
import application.infrastructure.FileUtils;
import application.infrastructure.SpecificationFileRepository;
import application.infrastructure.models.Api;
import application.infrastructure.models.SpecificationFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiServiceTest {

    @InjectMocks
    ApiService apiService;

    @Mock
    ApiRepository apiRepository;

    @Mock
    FileUtils fileUtils;

    @Mock
    SpecificationFileRepository specificationFileRepository;
    private Api apiToSave;
    private Api savedApi;
    private MultipartFile fileToSave;

    @Before
    public void setUp() throws Exception {
        apiToSave = new Api();
        savedApi = mock(Api.class);
        fileToSave = mock(MultipartFile.class);
        when(fileUtils.moveToUploadDirectory(any(MultipartFile.class), any(Api.class))).thenReturn(mock(File.class));
        when(apiRepository.save(any(Api.class))).thenReturn(mock(Api.class));
    }

    @Test
    public void shouldReturnApiWithId_ComingFromDatabase_WhenSavingAPI() throws IOException {
        // Given
        long apiId = 42L;
        when(savedApi.getId()).thenReturn(apiId);
        when(apiRepository.save(apiToSave)).thenReturn(savedApi);

        // When
        Api resultingApi = apiService.saveApi(apiToSave, fileToSave);

        // Then
        long resultingApiId = resultingApi.getId();
        assertThat(resultingApiId).isEqualTo(apiId);
    }

    @Test
    public void shouldReturnApiWithSpecification() throws IOException {
        // Given
        Api apiFromDatabase = new Api();
        apiFromDatabase.setId(23L);
        when(apiRepository.save(apiToSave)).thenReturn(apiFromDatabase);

        // When
        Api resultingApi = apiService.saveApi(apiToSave, fileToSave);

        // Then
        String specificationPath = resultingApi.getSpecificationPath();
        assertThat(specificationPath).isEqualTo("/catalogueApi/apis/23/specificationFile");
    }

    @Test
    public void shouldReturnApiWithApiName_WhenSavingAPI() throws IOException {
        // Given
        String apiName = "apiName";
        when(savedApi.getName()).thenReturn(apiName);
        when(apiRepository.save(apiToSave)).thenReturn(savedApi);

        // When
        Api resultingApi = apiService.saveApi(apiToSave, fileToSave);

        // Then
        String resultingApiVersion = resultingApi.getName();
        assertThat(resultingApiVersion).isEqualTo(apiName);
    }

    @Test
    public void shouldReturnApiWithApiVersion_WhenSavingAPI() throws IOException {
        // Given
        String apiVersion = "apiVersion";
        when(savedApi.getVersion()).thenReturn(apiVersion);
        when(apiRepository.save(apiToSave)).thenReturn(savedApi);

        // When
        Api resultingApi = apiService.saveApi(apiToSave, fileToSave);

        // Then
        String resultingApiVersion = resultingApi.getVersion();
        assertThat(resultingApiVersion).isEqualTo(apiVersion);
    }

    @Test
    public void shouldSetFilePathInSpecification_FromFileMove_WhenSavingAPI() throws IOException {
        // Given
        String absolutePath = "/some/absolute/path";
        File savedFile = mock(File.class);
        when(savedFile.getAbsolutePath()).thenReturn(absolutePath);
        when(fileUtils.moveToUploadDirectory(fileToSave, apiToSave)).thenReturn(savedFile);

        // When
        apiService.saveApi(apiToSave, fileToSave);

        // Then
        ArgumentCaptor<SpecificationFile> specificationFileCaptor = ArgumentCaptor.forClass(SpecificationFile.class);
        verify(specificationFileRepository).save(specificationFileCaptor.capture());
        SpecificationFile capturedSpecificationFile = specificationFileCaptor.getValue();
        String capturedFilePath = capturedSpecificationFile.getFilePath();
        assertThat(capturedFilePath).isEqualTo(absolutePath);
    }

    @Test
    public void shouldSetContentTypeInSpecification_FromUploadedFile_WhenSavingAPI() throws IOException {
        // Given
        String contentType = "app/yummy";
        when(fileToSave.getContentType()).thenReturn(contentType);

        // When
        apiService.saveApi(apiToSave, fileToSave);

        // Then
        ArgumentCaptor<SpecificationFile> specificationFileCaptor = ArgumentCaptor.forClass(SpecificationFile.class);
        verify(specificationFileRepository).save(specificationFileCaptor.capture());
        SpecificationFile capturedSpecificationFile = specificationFileCaptor.getValue();
        String capturedContentType = capturedSpecificationFile.getContentType();
        assertThat(capturedContentType).isEqualTo(contentType);
    }

    @Test
    public void shouldSetApiId_FromExistingApi_WithSameName_AndVersion_WhenSavingApi() throws IOException {
        // Given
        String apiName = "name";
        String apiVersion = "2.1";
        apiToSave.setName(apiName);
        apiToSave.setVersion(apiVersion);
        Api foundApi = new Api();
        long expectedApiId = 42L;
        foundApi.setId(expectedApiId);
        when(apiRepository.findByNameAndVersion(apiName, apiVersion)).thenReturn(foundApi);

        // When
        apiService.saveApi(apiToSave, fileToSave);

        // Then
        ArgumentCaptor<Api> apiArgumentCaptor = ArgumentCaptor.forClass(Api.class);
        verify(apiRepository).save(apiArgumentCaptor.capture());
        Api capturedApi = apiArgumentCaptor.getValue();
        Long capturedApiId = capturedApi.getId();
        assertThat(capturedApiId).isEqualTo(expectedApiId);
    }

    @Test
    public void shouldSetApiWithSpecification_ComingFromDatabase_WhenSavingAPI() throws IOException {
        // Given
        long specificationFileId = 23L;
        SpecificationFile savedSpecificationFile = mock(SpecificationFile.class);
        when(savedSpecificationFile.getId()).thenReturn(specificationFileId);
        when(specificationFileRepository.save(any(SpecificationFile.class))).thenReturn(savedSpecificationFile);

        // When
        apiService.saveApi(apiToSave, fileToSave);

        // Then
        ArgumentCaptor<Api> apiArgumentCaptor = ArgumentCaptor.forClass(Api.class);
        verify(apiRepository).save(apiArgumentCaptor.capture());
        Api capturedApiArgument = apiArgumentCaptor.getValue();
        SpecificationFile specificationFile = capturedApiArgument.getSpecificationFile();
        long capturedSpecificationFileId = specificationFile.getId();
        assertThat(capturedSpecificationFileId).isEqualTo(specificationFileId);
    }

    @Test
    public void shouldReturnApi_WithSpecificationPath() {
        // Given
        Long apiId = 23L;
        Api givenApi = new Api();
        givenApi.setId(23L);
        when(apiRepository.findOne(23L)).thenReturn(givenApi);

        // When
        Api api = apiService.getApi(apiId);

        // Then
        assertThat(api.getSpecificationPath()).isEqualTo("/catalogueApi/apis/23/specificationFile");
    }

    @Test
    public void shouldThrowRessourceNotFound_WhenRequestedApiDoesntExist() {
        // Given
        doThrow(EmptyResultDataAccessException.class).when(apiRepository).delete(anyLong());

        // Then
        assertThatThrownBy(() -> apiService.getApi(23L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void shouldReturnApis_WithSpecificationPaths() {
        // Given
        Api givenApi1 = new Api();
        givenApi1.setId(23L);
        Api givenApi2 = new Api();
        givenApi2.setId(24L);

        Page<Api> page = mock(Page.class);
        when(page.getContent()).thenReturn(asList(givenApi1, givenApi2));
        when(apiRepository.findAllByOrderByNameAscVersionAsc(any(PageRequest.class))).thenReturn(page);
        PageRequest pageRequest = new PageRequest(0, 10);

        // When
        Page<Api> allApis = apiService.getAllApis(pageRequest);

        // Then
        allApis.getContent();
        assertThat(allApis.getContent()).extracting("specificationPath").contains("/catalogueApi/apis/23/specificationFile", "/catalogueApi/apis/24/specificationFile");
    }

    @Test
    public void shouldDeleteApi_WhenCallingDeleteWithAnId() {
        // Given
        long specificationId = 23L;

        // When
        apiService.deleteApi(specificationId);

        // Then
        verify(apiRepository).delete(eq(23L));
    }

    @Test
    public void shouldThrowRessourceNotFound_WhenSpecificationFileToDeleteIsNotFound() {
        // Given
        doThrow(EmptyResultDataAccessException.class).when(apiRepository).delete(anyLong());

        // Then
        assertThatThrownBy(() -> apiService.deleteApi(23L)).isInstanceOf(ResourceNotFoundException.class);
    }
}