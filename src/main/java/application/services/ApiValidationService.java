package application.services;

import application.infrastructure.models.Api;
import application.infrastructure.models.ApiError;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiValidationService {
    public ApiError validate(Api api) {
        if (!StringUtils.isEmpty(api.getVersion())
                && !StringUtils.isEmpty(api.getName())
                && !StringUtils.isEmpty(api.getSpecificationType())) {
            return null;
        }
        ApiError error = new ApiError();
        error = makeErrorMessage(error, api);
        error.type = ApiValidationError.MISSING_PARAMETER;
        return error;
    }

    private ApiError makeErrorMessage(ApiError error, Api api) {
        List<String> nullFields = new ArrayList<>();
        if (StringUtils.isEmpty(api.getName())) {
            nullFields.add("name");
        }
        if (StringUtils.isEmpty(api.getVersion())) {
            nullFields.add("version");
        }
        if (StringUtils.isEmpty(api.getSpecificationType())) {
            nullFields.add("specificationType");
        }

        String errorMessageTemplate = "Required parameters are missing : %s";
        error.message = String.format(errorMessageTemplate, String.join(", ", nullFields));
        System.out.println(error.message);
        return error;
    }
}
