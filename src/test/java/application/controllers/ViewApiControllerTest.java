package application.controllers;

import application.infrastructure.models.Api;
import application.infrastructure.models.ApiError;
import application.infrastructure.models.ViewApi;
import application.services.ApiService;
import application.services.ApiValidationService;
import application.services.ViewService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ViewApiControllerTest {

    @InjectMocks
    ViewApiController apiViewController;

    @Mock
    private ApiService apiService;

    @Mock
    private ViewService viewService;

    @Mock
    private ApiValidationService apiValidationService;

    @Test
    public void shouldReturnTemplateForApiList_WhenRequestingApiList() {
        // Given
        when(apiService.getAllApis(any(PageRequest.class))).thenReturn(mock(Page.class));

        // When
        String result = apiViewController.list(0, mock(Model.class));

        // Then
        assertThat(result).isEqualTo("apis/list");
    }

    @Test
    public void shouldAskForAPage_Of50Apis_WhenRequestingApiList() {
        // Given
        when(apiService.getAllApis(any(PageRequest.class))).thenReturn(mock(Page.class));

        // When
        apiViewController.list(0, mock(Model.class));

        // Then
        ArgumentCaptor<PageRequest> pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(apiService).getAllApis(pageRequestArgumentCaptor.capture());
        PageRequest pageRequest = pageRequestArgumentCaptor.getValue();
        int maxPageSize = 50;
        assertThat(pageRequest.getPageSize()).isEqualTo(maxPageSize);
    }

    @Test
    public void shouldAskForThirdPage_WhenRequestingApiList_WithThirdPageArgument() {
        // Given
        when(apiService.getAllApis(any(PageRequest.class))).thenReturn(mock(Page.class));

        // When
        apiViewController.list(3, mock(Model.class));

        // Then
        ArgumentCaptor<PageRequest> pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(apiService).getAllApis(pageRequestArgumentCaptor.capture());
        PageRequest pageRequest = pageRequestArgumentCaptor.getValue();
        int thirdPage = 3;
        assertThat(pageRequest.getPageNumber()).isEqualTo(thirdPage);
    }

    @Test
    public void shouldPrepareApiListForView_WhenRequestingApiList() {
        // Given
        Model model = mock(Model.class);
        Page apisPage = mock(Page.class);
        List<ViewApi> viewApiList = mock(List.class);
        when(apiService.getAllApis(any(PageRequest.class))).thenReturn(apisPage);
        when(viewService.listApis(eq(apisPage.getContent()))).thenReturn(viewApiList);

        // When
        apiViewController.list(0, model);

        // Then
        verify(model).addAttribute(eq("apis"), eq(viewApiList));
    }

    @Test
    public void shouldReturnShowNewPage() {
        // When
        String destinationPage = apiViewController.newApi();

        // Then
        assertThat(destinationPage).isEqualTo("apis/new");
    }

    @Test
    public void shouldReturnToNewPage() throws IOException {
        // When
        String destinationPage = apiViewController.createApi("", "", "", mock(MultipartFile.class), mock(Model.class));

        // Then
        assertThat(destinationPage).isEqualTo("apis/new");
    }

    @Test
    public void shouldSaveUploadedFile_WhenApiParametersAreValid() throws IOException {
        // Given
        MultipartFile uploadedFile = mock(MultipartFile.class);
        when(apiValidationService.validate(any(Api.class))).thenReturn(null);

        // When
        apiViewController.createApi("name", "1.2", "blueprint", uploadedFile, mock(Model.class));

        // Then
        verify(apiService).saveApi(any(Api.class), eq(uploadedFile));
    }

    @Test
    public void shouldSaveApiName_WhenApiParametersAreValid() throws IOException {
        // Given
        String apiName = "name";
        when(apiValidationService.validate(any(Api.class))).thenReturn(null);

        // When
        apiViewController.createApi(apiName, "1.2", "blueprint", mock(MultipartFile.class), mock(Model.class));

        // Then
        ArgumentCaptor<Api> apiArgumentCaptor = ArgumentCaptor.forClass(Api.class);
        verify(apiService).saveApi(apiArgumentCaptor.capture(), any(MultipartFile.class));
        Api capturedApi = apiArgumentCaptor.getValue();
        String savedApiName = capturedApi.getName();
        assertThat(savedApiName).isEqualTo(apiName);
    }

    @Test
    public void shouldSaveApiVersion_WhenApiParametersAreValid() throws IOException {
        // Given
        String apiVersion = "1.3";
        when(apiValidationService.validate(any(Api.class))).thenReturn(null);

        // When
        apiViewController.createApi("api name", apiVersion, "blueprint", mock(MultipartFile.class), mock(Model.class));

        // Then
        ArgumentCaptor<Api> apiArgumentCaptor = ArgumentCaptor.forClass(Api.class);
        verify(apiService).saveApi(apiArgumentCaptor.capture(), any(MultipartFile.class));
        Api capturedApi = apiArgumentCaptor.getValue();
        String savedApiVersion = capturedApi.getVersion();
        assertThat(savedApiVersion).isEqualTo(apiVersion);
    }

    @Test
    public void shouldSaveApiSpecificationType_WhenApiParametersAreValid() throws IOException {
        // Given
        String apiSpecificationType = "RAML";
        when(apiValidationService.validate(any(Api.class))).thenReturn(null);

        // When
        apiViewController.createApi("api name", "1.2", apiSpecificationType, mock(MultipartFile.class), mock(Model.class));

        // Then
        ArgumentCaptor<Api> apiArgumentCaptor = ArgumentCaptor.forClass(Api.class);
        verify(apiService).saveApi(apiArgumentCaptor.capture(), any(MultipartFile.class));
        Api capturedApi = apiArgumentCaptor.getValue();
        String savedApiSpecificationType = capturedApi.getSpecificationType();
        assertThat(savedApiSpecificationType).isEqualTo(apiSpecificationType);
    }

    @Test
    public void shouldNotSaveApi_WhenApiParametersAreNotValid() throws IOException {
        // Given
        when(apiValidationService.validate(any(Api.class))).thenReturn(new ApiError());

        // When
        apiViewController.createApi("", "", "", mock(MultipartFile.class), mock(Model.class));

        // Then
        verify(apiService, never()).saveApi(any(Api.class), any(MultipartFile.class));
    }

    @Test
    public void shouldSetModelSuccessFieldWithTrue_WhenApiParametersAreValid() throws IOException {
        // Given
        Model model = mock(Model.class);
        when(apiValidationService.validate(any(Api.class))).thenReturn(null);

        // When
        apiViewController.createApi("", "", "", mock(MultipartFile.class), model);

        // Then
        String successField = "success";
        verify(model).addAttribute(eq(successField), eq(true));
    }

    @Test
    public void shouldSetModelSuccessFieldWithTrue_WhenApiParametersAreNotValid() throws IOException {
        // Given
        Model model = mock(Model.class);
        when(apiValidationService.validate(any(Api.class))).thenReturn(new ApiError());

        // When
        apiViewController.createApi("", "", "", mock(MultipartFile.class), model);

        // Then
        String successField = "success";
        verify(model).addAttribute(eq(successField), eq(false));
    }

    @Test
    public void shouldSetModelErrorMessageFieldWithValidationErrorMessage_WhenApiParametersAreNotValid() throws IOException {
        // Given
        String errorMessage = "error message";
        Model model = mock(Model.class);
        ApiError apiError = new ApiError();
        apiError.message = errorMessage;
        when(apiValidationService.validate(any(Api.class))).thenReturn(apiError);

        // When
        apiViewController.createApi("", "", "", mock(MultipartFile.class), model);

        // Then
        verify(model).addAttribute(eq("errormsg"), eq(errorMessage));
    }
}