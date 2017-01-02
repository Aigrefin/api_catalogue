package application.services;

import application.infrastructure.models.Api;
import application.infrastructure.models.ViewApi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ViewService {

    public List<ViewApi> listApis(List<Api> apis) {
        List<ViewApi> viewApis = new ArrayList<>();
        for (Api api : apis) {
            ViewApi viewApi = new ViewApi();
            viewApi.setDisplayName(String.format("<b>%s</b> - %s", api.getName(), api.getVersion()));
            viewApi.setDownloadLink(api.getSpecificationPath());
            viewApi.setSpecificationType(api.getSpecificationType());
            viewApis.add(viewApi);
        }
        return viewApis;
    }
}
