package application.controllers;

import application.infrastructure.models.Api;
import application.infrastructure.models.ApiError;
import application.infrastructure.models.SpecificationFile;
import application.services.ApiService;
import application.services.ApiValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.tools.web.BadHttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiControllerTest {

    @InjectMocks
    ApiController apiController;

    @Mock
    ApiService apiService;

    @Mock
    ApiValidationService apiValidationService;

    @Before
    public void setUp() throws Exception {
        when(apiValidationService.validate(any(Api.class))).thenReturn(null);
    }

    @Test
    public void shouldGiveFile_ToSpecificationFileService() throws IOException, BadHttpRequest {
        // Given
        String apiParameter = "{}";
        MultipartFile uploadedFile = mock(MultipartFile.class);

        // When
        apiController.saveApi(apiParameter, uploadedFile);

        // Then
        verify(apiService).saveApi(any(Api.class), eq(uploadedFile));
    }

    @Test
    public void shouldCreateAPIEntry_WithSpecificationFile() throws IOException, BadHttpRequest {
        // Given
        MultipartFile uploadedFile = mock(MultipartFile.class);
        String apiName = "testApiName";
        String apiVersion = "1.0";
        String specificationType = "sWagger";
        Api specificationParameter = new Api();
        specificationParameter.setName(apiName);
        specificationParameter.setVersion(apiVersion);
        specificationParameter.setSpecificationType(specificationType);

        // When
        apiController.saveApi(toJson(specificationParameter), uploadedFile);

        // Then
        ArgumentCaptor<Api> specificationArgumentCaptor = ArgumentCaptor.forClass(Api.class);
        verify(apiService).saveApi(specificationArgumentCaptor.capture(), any(MultipartFile.class));
        Api api = specificationArgumentCaptor.getValue();
        assertThat(api.getName()).isEqualTo(apiName);
        assertThat(api.getVersion()).isEqualTo(apiVersion);
        assertThat(api.getSpecificationType()).isEqualTo("sWagger");
    }

    @Test
    public void shouldReturnBadRequest_WithMissingParameters_WhenParametersAreMissing() throws IOException, BadHttpRequest {
        // Given
        String givenSpecification = "{}";
        ApiError givenError = mock(ApiError.class);
        when(apiValidationService.validate(any(Api.class))).thenReturn(givenError);

        // When
        ResponseEntity specificationResponseEntity = apiController.saveApi(givenSpecification, mock(MultipartFile.class));

        // Then
        assertThat(specificationResponseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiError error = (ApiError) specificationResponseEntity.getBody();
        assertThat(error).isEqualTo(givenError);
    }

    @Test
    public void shouldSaveSpecification_WhenSpecificationAlreadyExists() throws IOException, BadHttpRequest {
        // Given
        String givenSpecification = "{}";
        Api savedApi = mock(Api.class);
        when(apiService.saveApi(any(Api.class), any(MultipartFile.class))).thenReturn(savedApi);

        // When
        ResponseEntity responseEntity = apiController.saveApi(givenSpecification, mock(MultipartFile.class));

        // Then
        assertThat(responseEntity.getBody()).isEqualTo(savedApi);
    }

    @Test
    public void shouldReturnSelectedSpecification() {
        // Given
        long givenSpecificationId = 42L;
        Api givenApi = mock(Api.class);
        when(givenApi.getId()).thenReturn(givenSpecificationId);
        when(apiService.getApi(givenSpecificationId)).thenReturn(givenApi);

        // When
        Api api = apiController.getApi(givenSpecificationId);

        // Then
        assertThat(api.getId()).isEqualTo(givenSpecificationId);
    }

    @Test
    public void shouldAskForSpecificationPage_fromRequestParameters() {
        // Given
        int pageNumber = 42;
        int pageSize = 20;

        // When
        apiController.getAllApis(pageNumber, pageSize);

        // Then
        ArgumentCaptor<PageRequest> pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(apiService).getAllApis(pageRequestArgumentCaptor.capture());
        PageRequest specificationPage = pageRequestArgumentCaptor.getValue();
        assertThat(specificationPage.getPageNumber()).isEqualTo(pageNumber);
        assertThat(specificationPage.getPageSize()).isEqualTo(pageSize);
    }

    @Test
    public void shouldCapPageSize_ToFifty() {
        // Given
        int pageNumber = 0;
        int pageSize = 100;

        // When
        apiController.getAllApis(pageNumber, pageSize);

        // Then
        ArgumentCaptor<PageRequest> pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(apiService).getAllApis(pageRequestArgumentCaptor.capture());
        PageRequest specificationPage = pageRequestArgumentCaptor.getValue();
        assertThat(specificationPage.getPageNumber()).isEqualTo(pageNumber);
        int cappedPageSize = 50;
        assertThat(specificationPage.getPageSize()).isEqualTo(cappedPageSize);
    }

    @Test
    public void shouldReturnSpecificationFile_WithFilePath() {
        // Given
        String expectedPath = "/absolute/path";
        SpecificationFile serverSpecificationFile = mock(SpecificationFile.class);
        when(serverSpecificationFile.getFilePath()).thenReturn(expectedPath);
        Api api = mock(Api.class);
        when(api.getSpecificationFile()).thenReturn(serverSpecificationFile);
        when(apiService.getApi(anyLong())).thenReturn(api);

        // When
        ResponseEntity<FileSystemResource> specificationFile = apiController.getSpecificationFile(42L);

        // Then
        assertThat(specificationFile.getBody().getFile().getAbsolutePath()).isEqualTo(expectedPath);
    }

    @Test
    public void shouldReturnSpecificationFile_WithContentType() {
        // Given
        SpecificationFile serverSpecificationFile = mock(SpecificationFile.class);
        when(serverSpecificationFile.getFilePath()).thenReturn("/path");
        when(serverSpecificationFile.getContentType()).thenReturn("application/yumyum");
        Api api = mock(Api.class);
        when(api.getSpecificationFile()).thenReturn(serverSpecificationFile);
        when(apiService.getApi(anyLong())).thenReturn(api);

        // When
        ResponseEntity<FileSystemResource> specificationFile = apiController.getSpecificationFile(42L);

        // Then
        assertThat(specificationFile.getHeaders().getFirst("Content-Type")).isEqualTo("application/yumyum");
    }

    @Test
    public void shouldDeleteApi_AndAnswerNoContent() {
        // Given
        long apiId = 23L;

        // When
        ResponseEntity responseEntity = apiController.deleteApi(apiId);

        // Then
        verify(apiService).deleteApi(eq(apiId));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void shouldReturnBadRequest_WithErrorMessage_WhenSendingBadJson() throws IOException, BadHttpRequest {
        // Given
        String api = "{";

        // When
        ResponseEntity responseEntity = apiController.saveApi(api, mock(MultipartFile.class));

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String toJson(Api apiObject) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(apiObject);
    }
}