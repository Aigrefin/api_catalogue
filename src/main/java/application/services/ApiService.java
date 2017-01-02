package application.services;

import application.infrastructure.ApiRepository;
import application.infrastructure.FileUtils;
import application.infrastructure.SpecificationFileRepository;
import application.infrastructure.models.Api;
import application.infrastructure.models.SpecificationFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class ApiService {

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    SpecificationFileRepository specificationFileRepository;

    @Autowired
    FileUtils fileUtils;

    public Api saveApi(Api newApi, MultipartFile uploadedFile) throws IOException {
        Api foundApi = findApi(newApi);
        if (foundApi != null) {
            long foundApiId = foundApi.getId();
            newApi.setId(foundApiId);
        }

        SpecificationFile savedSpecificationFile = makeSpecificationFile(newApi, uploadedFile);
        newApi.setSpecificationFile(savedSpecificationFile);

        Api savedApi = apiRepository.save(newApi);
        savedApi.setSpecificationPath(makeSpecificationPath(savedApi));
        return savedApi;
    }

    public void deleteApi(Long api) {
        try {
            apiRepository.delete(api);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException();
        }
    }

    public Api getApi(Long apiId) {
        Api api = apiRepository.findOne(apiId);
        if (api == null) {
            throw new ResourceNotFoundException();
        }
        api.setSpecificationPath(makeSpecificationPath(api));
        return api;
    }

    public Page<Api> getAllApis(PageRequest pageRequest) {
        Page<Api> apis = apiRepository.findAllByOrderByNameAscVersionAsc(pageRequest);
        for (Api api : apis.getContent()) {
            api.setSpecificationPath(makeSpecificationPath(api));
        }
        return apis;
    }

    private Api findApi(Api newApi) {
        String apiName = newApi.getName();
        String apiVersion = newApi.getVersion();
        return apiRepository.findByNameAndVersion(apiName, apiVersion);
    }

    private SpecificationFile makeSpecificationFile(Api newApi, MultipartFile uploadedFile) throws IOException {
        File savedFile = fileUtils.moveToUploadDirectory(uploadedFile, newApi);
        String contentType = uploadedFile.getContentType();
        String filePath = savedFile.getAbsolutePath();
        SpecificationFile specificationFile = new SpecificationFile();
        specificationFile.setContentType(contentType);
        specificationFile.setFilePath(filePath);
        return specificationFileRepository.save(specificationFile);
    }

    private String makeSpecificationPath(Api savedApi) {
        return "/catalogueApi/apis/" + savedApi.getId() + "/specificationFile";
    }

}
