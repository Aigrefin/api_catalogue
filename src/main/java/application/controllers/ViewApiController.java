package application.controllers;

import application.infrastructure.models.Api;
import application.infrastructure.models.ApiError;
import application.infrastructure.models.ViewApi;
import application.services.ApiService;
import application.services.ApiValidationService;
import application.services.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class ViewApiController {

    private static final int PAGE_SIZE = 50;
    private static final String SUCCESS_FIELD = "success";
    private static final String ERROR_MSG_FIELD = "errormsg";

    @Autowired
    ApiService apiService;

    @Autowired
    ApiValidationService apiValidationService;

    @Autowired
    ViewService viewService;

    @RequestMapping("/")
    public String list(@RequestParam(value = "page", required = false, defaultValue = "0") Integer pageNumber, Model model){
        PageRequest pageRequest = new PageRequest(pageNumber, PAGE_SIZE);
        Page<Api> apisPage = apiService.getAllApis(pageRequest);
        List<Api> apis = apisPage.getContent();
        List<ViewApi> viewApis = viewService.listApis(apis);
        model.addAttribute("apis", viewApis);
        return "apis/list";
    }

    @RequestMapping(value = "/new-api", method = RequestMethod.GET)
    public String newApi() {
        return "apis/new";
    }

    @RequestMapping(value = "/new-api", method = RequestMethod.POST)
    public String createApi(@RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "version", required = false) String version,
                            @RequestParam(value = "specificationType", required = false) String specificationType,
                            @RequestParam(value = "file", required = false) MultipartFile uploadedFile,
                            Model model) throws IOException {
        Api api = buildApi(name, version, specificationType);
        ApiError error = apiValidationService.validate(api);
        if (error == null) {
            apiService.saveApi(api, uploadedFile);
            model.addAttribute(SUCCESS_FIELD, true);
        } else {
            model.addAttribute(SUCCESS_FIELD, false);
            model.addAttribute(ERROR_MSG_FIELD, error.message);
        }
        return "apis/new";
    }

    private Api buildApi(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "specificationType", required = false) String specificationType) {
        Api api = new Api();
        api.setName(name);
        api.setVersion(version);
        api.setSpecificationType(specificationType);
        return api;
    }
}
