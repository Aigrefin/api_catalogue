package application.services;

import application.infrastructure.models.Api;
import application.infrastructure.models.ViewApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(MockitoJUnitRunner.class)
public class ViewServiceTest {

    @InjectMocks
    ViewService viewService;

    @Test
    public void shouldFormatApis_ForView_WhenGivenApiModels() {
        // Given
        Api api1 = new Api();
        api1.setName("test name a");
        api1.setVersion("1.2");
        api1.setSpecificationType("Swagger");
        api1.setSpecificationPath("/catalogueApi/apis/1/specificationFile");
        Api api2 = new Api();
        api2.setName("test name b");
        api2.setVersion("1.3");
        api2.setSpecificationType("Blueprint");
        api2.setSpecificationPath("/catalogueApi/apis/2/specificationFile");
        List<Api> apis = asList(api1, api2);

        // When
        List<ViewApi> result = viewService.listApis(apis);

        // Then
        assertThat(result).extracting("displayName","downloadLink", "specificationType").contains(
                tuple("<b>test name a</b> - 1.2", "/catalogueApi/apis/1/specificationFile", "Swagger"),
                tuple("<b>test name b</b> - 1.3", "/catalogueApi/apis/2/specificationFile", "Blueprint")
        );
    }
}