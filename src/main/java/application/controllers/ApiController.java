package application.controllers;

import application.infrastructure.models.Api;
import application.infrastructure.models.ApiError;
import application.infrastructure.models.SpecificationFile;
import application.services.ApiService;
import application.services.ApiValidationService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.tools.web.BadHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static java.lang.Integer.min;

@RestController
@RequestMapping("/catalogueApi/apis")
public class ApiController {

    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    ApiService apiService;

    @Autowired
    ApiValidationService apiValidationService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity saveApi(
            @RequestParam("api") String apiParameter,
            @RequestParam("file") MultipartFile uploadedFile) throws IOException, BadHttpRequest {
        Api api = parseApi(apiParameter);
        if (api == null) {
            return ResponseEntity.badRequest().build();
        }
        ApiError error = apiValidationService.validate(api);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        Api createdApi = apiService.saveApi(api, uploadedFile);

        return new ResponseEntity<>(createdApi, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{apiId}")
    @ResponseBody
    Api getApi(@PathVariable Long apiId) {
        return apiService.getApi(apiId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{apiId}/specificationFile")
    @ResponseBody
    public ResponseEntity<FileSystemResource> getSpecificationFile(@PathVariable Long apiId) {
        Api api = apiService.getApi(apiId);
        SpecificationFile specificationFile = api.getSpecificationFile();
        String specificationFilePath = specificationFile.getFilePath();
        FileSystemResource sendableSpecificationFile = new FileSystemResource(specificationFilePath);
        HttpHeaders headers = getHttpHeadersWithGivenContentType(specificationFile);
        return new ResponseEntity<>(sendableSpecificationFile, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{apiId}")
    public ResponseEntity deleteApi(@PathVariable Long apiId){
        apiService.deleteApi(apiId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    Page<Api> getAllApis(@RequestParam(name = "page", defaultValue = "0", required = false) int page, @RequestParam(name = "pageSize", defaultValue = "50", required = false) int givenPageSize) {
        int pageSize = min(givenPageSize, MAX_PAGE_SIZE);
        return apiService.getAllApis(new PageRequest(page, pageSize));
    }

    private Api parseApi(String apiParameter) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(apiParameter, Api.class);
        } catch (JsonParseException | JsonMappingException jsonParseException) {
            return null;
        }
    }

    private HttpHeaders getHttpHeadersWithGivenContentType(SpecificationFile specificationFile) {
        HttpHeaders headers = new HttpHeaders();
        String contentType = specificationFile.getContentType();
        headers.set("Content-Type", contentType);
        return headers;
    }

}
