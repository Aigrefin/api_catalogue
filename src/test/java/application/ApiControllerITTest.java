package application;


import application.infrastructure.models.Api;
import application.infrastructure.models.ApiError;
import application.services.ApiValidationError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class ApiControllerITTest {

    private static final String APIS_ENDPOINT = "/catalogueApi/apis";
    private static final String SPECIFICATION_FILE_ENDPOINT = "/specificationFile";

    @Autowired
    private MockMvc mvc;

    private Api api;
    private String apiName;
    private String apiVersion;
    private String specificationType;
    private String contentType;
    private MockMultipartFile dataFile;
    private String fileContent;

    @Before
    public void setUp() throws Exception {
        apiName = "testApiName";
        apiVersion = "1.99";
        specificationType = "swagger";
        contentType = "application/yumyum";
        api = new Api();
        api.setName(apiName);
        api.setVersion(apiVersion);
        api.setSpecificationType(specificationType);
        String fileParameterName = "file";
        String fileName = "foo.yum";
        fileContent = "content";
        dataFile = new MockMultipartFile(fileParameterName, fileName, contentType, fileContent.getBytes());
    }

    @Test
    public void shouldReturnOk_WhenRequestedFileUploaded() throws Exception {
        // When
        ResultActions result = mvc.perform(fileUpload(APIS_ENDPOINT)
                .file(dataFile)
                .contentType(contentType)
                .param("api", toJson(api))
        );

        // Then
        result.andExpect(status().isOk());
    }

    @Test
    public void shouldReturnApiFields_WhenApiCreated() throws Exception {
        // When
        ResultActions result = mvc.perform(fileUpload(APIS_ENDPOINT)
                .file(dataFile)
                .contentType(contentType)
                .param("api", toJson(api))
        );

        // Then
        result.andExpect(status().isOk());
        MockHttpServletResponse response = result.andReturn().getResponse();
        String responseBody = response.getContentAsString();
        assertThat(responseBody).contains("\"name\":\"" + apiName + "\"");
        assertThat(responseBody).contains("\"version\":\"" + apiVersion + "\"");
        assertThat(responseBody).contains("\"specificationType\":\"" + specificationType + "\"");
        assertThat(response.getContentAsString()).contains("\"specificationPath\":\"/catalogueApi/apis/1/specificationFile");
    }

    @Test
    public void shouldReturnSpecifications_WithUploadedAttributes() throws Exception {
        // Given
        uploadSpecification();

        // When
        MvcResult result = get(APIS_ENDPOINT);

        // Then
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        String responseBody = response.getContentAsString();
        assertThat(responseBody).contains("\"name\":\"" + apiName + "\"");
        assertThat(responseBody).contains("\"version\":\"" + apiVersion + "\"");
        assertThat(responseBody).contains("\"specificationType\":\"" + specificationType + "\"");
    }

    @Test
    public void shouldReturnSpecifications_WithPathToSpecificationPath() throws Exception {
        // Given
        uploadSpecification();

        // When
        MvcResult result = get(APIS_ENDPOINT);

        // Then
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("\"specificationPath\":\"" + APIS_ENDPOINT + "/1" + SPECIFICATION_FILE_ENDPOINT);
    }

    @Test
    public void shouldReturnSpecification_WhenRequestedFileWasUploaded() throws Exception {
        // Given
        MvcResult mvcResult = uploadSpecification();
        long specificationId = getSpecificationId(mvcResult);

        // When
        MvcResult result = get(APIS_ENDPOINT + '/' + specificationId);

        // Then
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("\"name\":\"" + apiName + "\"");
        assertThat(response.getContentAsString()).contains("\"version\":\"" + apiVersion + "\"");
        assertThat(response.getContentAsString()).contains("\"specificationType\":\"" + specificationType + "\"");
    }

    @Test
    public void shouldReturnSpecification_WithPathToSpecificationPath_WhenRequestedFileWasUploaded() throws Exception {
        // Given
        MvcResult mvcResult = uploadSpecification();
        long specificationId = getSpecificationId(mvcResult);

        // When
        MvcResult result = get(APIS_ENDPOINT + '/' + specificationId);

        // Then
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("\"specificationPath\":\"" + APIS_ENDPOINT + "/" + specificationId + SPECIFICATION_FILE_ENDPOINT);
    }

    @Test
    public void shouldDownloadPreviouslyUploadedSpecificationFile() throws Exception {
        // Given
        MvcResult mvcResult = uploadSpecification();

        long specificationId = getSpecificationId(mvcResult);
        String specificationFileURL = APIS_ENDPOINT + "/" + specificationId + SPECIFICATION_FILE_ENDPOINT;

        // When
        MockHttpServletResponse result = get(specificationFileURL).getResponse();

        // Then
        assertThat(result.getStatus()).isEqualTo(200);
        String resultBody = result.getContentAsString();
        assertThat(resultBody).isEqualTo(fileContent);
    }

    @Test
    public void shouldDownloadUploadedSpecificationFile_WithContentType() throws Exception {
        // Given
        MvcResult mvcResult = uploadSpecification();

        long specificationId = getSpecificationId(mvcResult);
        String specificationFileURL = APIS_ENDPOINT + "/" + specificationId + SPECIFICATION_FILE_ENDPOINT;

        // When
        MockHttpServletResponse result = get(specificationFileURL).getResponse();

        // Then
        assertThat(result.getContentType()).isEqualTo(contentType);
    }

    @Test
    public void shouldReturnBadRequest_WithErrorStructure_WhenParametersAreMissing() throws Exception {
        // Given
        api.setName(apiName);
        api.setVersion(null);
        api.setSpecificationType(null);

        // When
        MockHttpServletResponse response = uploadSpecification().getResponse();

        // Then
        assertThat(response.getStatus()).isEqualTo(400);
        ApiError specificationError = getSpecificationErrorFromResponse(response);
        ApiError expectedError = new ApiError();
        expectedError.type = ApiValidationError.MISSING_PARAMETER;
        expectedError.message = "Required parameters are missing : version, specificationType";
        assertThat(specificationError).isEqualToComparingFieldByField(expectedError);
    }


    @Test
    public void shouldOverrideApiOfSameName_AndVersion_AndSpecificationType() throws Exception {
        // Given two uploads of the same API
        uploadSpecification();
        uploadSpecification();

        // When
        MockHttpServletResponse response = get(APIS_ENDPOINT).getResponse();

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("\"totalElements\":1");
    }

    @Test
    public void should_replaceOldSpecificationFile() throws Exception {
        // Given
        uploadSpecification();
        String differentContent = "differentContent";
        MockMultipartFile differentFile = new MockMultipartFile("file", "someFileName", contentType, differentContent.getBytes());

        // When
        MvcResult mvcResult = mvc.perform(fileUpload(APIS_ENDPOINT)
                .file(differentFile)
                .contentType(contentType)
                .param("api", toJson(api))).andReturn();

        // Then
        long specificationId = getSpecificationId(mvcResult);
        String specificationFileURL = APIS_ENDPOINT + "/" + specificationId + SPECIFICATION_FILE_ENDPOINT;
        MockHttpServletResponse result = get(specificationFileURL).getResponse();
        String resultBody = result.getContentAsString();
        assertThat(resultBody).isEqualTo(differentContent);
    }

    @Test
    public void should_deleteSpecification_withANoContentStatus() throws Exception {
        // Given
        MvcResult mvcResult = uploadSpecification();
        long specificationId = getSpecificationId(mvcResult);

        // When
        MockHttpServletResponse deletionResponse = delete(APIS_ENDPOINT + "/" + specificationId).getResponse();

        // Then
        assertThat(deletionResponse.getStatus()).isEqualTo(204);
        MockHttpServletResponse getResponse = get(APIS_ENDPOINT + "/" + specificationId).getResponse();
        assertThat(getResponse.getStatus()).isEqualTo(404);
    }

    @Test
    public void should_returnNotFound_whenSpecificationToDeleteDoesntExist() throws Exception {
        // When
        MockHttpServletResponse deletionResponse = delete(APIS_ENDPOINT + "/1").getResponse();

        // Then
        assertThat(deletionResponse.getStatus()).isEqualTo(404);
    }

    @Test
    public void should_returnNotFound_whenSpecificationToGetDoesntExist() throws Exception {
        // When
        MockHttpServletResponse deletionResponse = get(APIS_ENDPOINT + "/1").getResponse();

        // Then
        assertThat(deletionResponse.getStatus()).isEqualTo(404);
    }

    private String toJson(Api apiObject) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(apiObject);
    }

    private MvcResult get(String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(url)).andReturn();
    }

    private MvcResult delete(String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete(url)).andReturn();
    }

    private ApiError getSpecificationErrorFromResponse(MockHttpServletResponse response) throws java.io.IOException {
        String jsonError = response.getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonError, ApiError.class);
    }

    private MvcResult uploadSpecification() throws Exception {
        return mvc.perform(fileUpload(APIS_ENDPOINT)
                .file(dataFile)
                .contentType(contentType)
                .param("api", toJson(api))).andReturn();
    }

    private long getSpecificationId(MvcResult mvcResult) throws java.io.IOException {
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Api api = objectMapper.readValue(jsonResponse, Api.class);
        return api.getId();
    }
}