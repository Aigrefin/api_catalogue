package application.services;

import application.infrastructure.models.Api;
import application.infrastructure.models.ApiError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ApiValidationServiceTest {

    @InjectMocks
    ApiValidationService specificationValidationService;

    @Test
    public void shouldReturnNone_WhenSpecificationIsOk() {
        // Given
        Api api = new Api();
        api.setName("someName");
        api.setVersion("1.1");
        api.setSpecificationType("Swagger");

        // When
        ApiError error = specificationValidationService.validate(api);

        // Then
        assertThat(error).isEqualTo(null);
    }

    @Test
    public void shouldReturnMissingParameter_WhenSpecificationIsMissingApiVersion() {
        // Given
        Api api = new Api();
        api.setName("someName");
        api.setSpecificationType("Swagger");

        // When
        ApiError error = specificationValidationService.validate(api);

        // Then
        assertThat(error.type).isEqualTo(ApiValidationError.MISSING_PARAMETER);
    }

    @Test
    public void shouldReturnMissingParameter_WhenSpecificationIsMissingApiName() {
        // Given
        Api api = new Api();
        api.setVersion("1.2");
        api.setSpecificationType("Swagger");

        // When
        ApiError error = specificationValidationService.validate(api);

        // Then
        assertThat(error.type).isEqualTo(ApiValidationError.MISSING_PARAMETER);
    }

    @Test
    public void shouldReturnMissingParameter_WhenSpecificationIsMissingSpecificationType() {
        // Given
        Api api = new Api();
        api.setName("someApiName");
        api.setVersion("1.2");

        // When
        ApiError error = specificationValidationService.validate(api);

        // Then
        assertThat(error.type).isEqualTo(ApiValidationError.MISSING_PARAMETER);
    }

    @Test
    public void shouldReturnErrorMessage_WithErrorType_AndSpecification_WhenParametersAreNull() {
        // Given
        Api api = new Api();

        // When
        ApiError errorMessage = specificationValidationService.validate(api);

        // Then
        assertThat(errorMessage.type).isEqualTo(ApiValidationError.MISSING_PARAMETER);
        assertThat(errorMessage.message).startsWith("Required parameters are missing :");
        assertThat(errorMessage.message).contains("name");
        assertThat(errorMessage.message).contains("version");
        assertThat(errorMessage.message).contains("specificationType");
    }

    @Test
    public void shouldReturnErrorMessage_WithErrorType_AndSpecification_WhenParametersAreEmpty() {
        // Given
        Api api = new Api();
        api.setName("");
        api.setVersion("");
        api.setSpecificationType("");

        // When
        ApiError errorMessage = specificationValidationService.validate(api);

        // Then
        assertThat(errorMessage.type).isEqualTo(ApiValidationError.MISSING_PARAMETER);
        assertThat(errorMessage.message).startsWith("Required parameters are missing :");
        assertThat(errorMessage.message).contains("name");
        assertThat(errorMessage.message).contains("version");
        assertThat(errorMessage.message).contains("specificationType");
    }
}