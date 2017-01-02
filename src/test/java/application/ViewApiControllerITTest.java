package application;

import application.infrastructure.models.Api;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class ViewApiControllerITTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnUploadedSpecifications_WhenCallingApisList() throws Exception {
        // Given
        uploadSpecification("My test API", "42.23", "swagger");
        uploadSpecification("My other test API", "4.8", "blueprint");

        // When
        MockHttpServletResponse response = this.mockMvc.perform(get("/")).andReturn().getResponse();

        // Then
        int status = response.getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
        String pageContent = response.getContentAsString();
        assertThat(pageContent).contains("<h2>APIs list</h2>");
        assertThat(pageContent).contains("<span><b>My test API</b> - 42.23</span>");
        assertThat(pageContent).contains("<span><b>My other test API</b> - 4.8</span>");
        assertThat(pageContent).contains("<a href=\"/catalogueApi/apis/1/specificationFile\">swagger</a>");
        assertThat(pageContent).contains("<a href=\"/catalogueApi/apis/2/specificationFile\">blueprint</a>");
    }

    @Test
    public void shouldReturnFormWithFieldsForApiCreation() throws Exception {
        // When
        MockHttpServletResponse response = this.mockMvc.perform(get("/new-api")).andReturn().getResponse();

        // Then
        String pageContent = response.getContentAsString();
        assertThat(pageContent).contains("<form action=\"/new-api\" method=\"post\" enctype=\"multipart/form-data\">");
        assertThat(pageContent).contains("<input type=\"text\" name=\"name\" />");
        assertThat(pageContent).contains("<input type=\"text\" name=\"version\" />");
        assertThat(pageContent).contains("<input type=\"text\" name=\"specificationType\" />");
        assertThat(pageContent).contains("<input type=\"file\" name=\"file\" />");
    }

    @Test
    public void shouldShowLinkToApiListPage() throws Exception {
        // When
        MockHttpServletResponse response = this.mockMvc.perform(get("/")).andReturn().getResponse();

        // Then
        String pageContent = response.getContentAsString();
        assertThat(pageContent).contains("<a href=\"/\">API List</a>");
    }

    @Test
    public void shouldShowLinkToNewApiPage() throws Exception {
        // When
        MockHttpServletResponse response = this.mockMvc.perform(get("/")).andReturn().getResponse();

        // Then
        String pageContent = response.getContentAsString();
        assertThat(pageContent).contains("<a href=\"/new-api\">New API</a>");
    }

    private String toJson(Api apiObject) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(apiObject);
    }

    private MvcResult uploadSpecification(String apiName, String apiVersion, String specificationType) throws Exception {
        Api api = new Api();
        api.setName(apiName);
        api.setVersion(apiVersion);
        api.setSpecificationType(specificationType);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "someName", "application/yum", "test".getBytes());
        return mockMvc.perform(fileUpload("/catalogueApi/apis")
                .file(mockMultipartFile)
                .contentType("application/yum")
                .param("api", toJson(api))).andReturn();
    }
}