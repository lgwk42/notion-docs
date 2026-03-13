package io.github.lgwk42.notiondocs.scanner;

import jakarta.validation.constraints.NotNull;
import io.github.lgwk42.notiondocs.model.ApiEndpointInfo;
import io.github.lgwk42.notiondocs.model.ControllerGroup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = "notion-docs.enabled=false")
@Import(ApiEndpointScannerTest.SampleUserController.class)
class ApiEndpointScannerTest {

    @SpringBootApplication
    static class TestApp {}

    @RestController
    @RequestMapping("/api/v1/users")
    static class SampleUserController {
        @GetMapping("/{id}")
        public UserResponse getUser(@PathVariable("id") Long id,
                                     @RequestHeader("Authorization") String token) {
            return null;
        }
        @PostMapping
        public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
            return null;
        }
        @GetMapping
        public List<UserResponse> listUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "20") int size) {
            return null;
        }
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
            return null;
        }
    }

    record CreateUserRequest(@NotNull String name, @NotNull String email, Integer age) {}
    record UserResponse(Long id, String name, String email) {}

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void scanFindsAllEndpoints() {
        DtoFieldExtractor extractor = new DtoFieldExtractor();
        ApiEndpointScanner scanner = new ApiEndpointScanner(
                handlerMapping, extractor,
                new EndpointMetadataResolver("", List.of()),
                new EndpointParameterExtractor(),
                List.of(), List.of());
        List<ControllerGroup> groups = scanner.scan();
        ControllerGroup userGroup = groups.stream()
                .filter(g -> g.controllerName().equals("SampleUserController"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("SampleUserController not found"));
        assertEquals("/api/v1/users", userGroup.basePath());
        assertEquals(4, userGroup.endpoints().size());
    }

    @Test
    void scanExtractsGetEndpointDetails() {
        DtoFieldExtractor extractor = new DtoFieldExtractor();
        ApiEndpointScanner scanner = new ApiEndpointScanner(
                handlerMapping, extractor,
                new EndpointMetadataResolver("", List.of()),
                new EndpointParameterExtractor(),
                List.of(), List.of());
        List<ControllerGroup> groups = scanner.scan();
        ControllerGroup userGroup = groups.stream()
                .filter(g -> g.controllerName().equals("SampleUserController"))
                .findFirst().orElseThrow();
        ApiEndpointInfo getUser = userGroup.endpoints().stream()
                .filter(e -> e.methodName().equals("getUser"))
                .findFirst().orElseThrow();
        assertEquals("GET", getUser.httpMethod());
        assertTrue(getUser.uri().contains("{id}"));
        assertEquals(1, getUser.headers().size());
        assertEquals("Authorization", getUser.headers().get(0).name());
        assertTrue(getUser.headers().get(0).required());
        assertEquals(1, getUser.pathVariables().size());
        assertEquals("id", getUser.pathVariables().get(0).name());
        assertNotNull(getUser.responseType());
        assertFalse(getUser.responseFields().isEmpty());
    }

    @Test
    void scanExtractsPostEndpointWithRequestBody() {
        DtoFieldExtractor extractor = new DtoFieldExtractor();
        ApiEndpointScanner scanner = new ApiEndpointScanner(
                handlerMapping, extractor,
                new EndpointMetadataResolver("", List.of()),
                new EndpointParameterExtractor(),
                List.of(), List.of());
        List<ControllerGroup> groups = scanner.scan();
        ControllerGroup userGroup = groups.stream()
                .filter(g -> g.controllerName().equals("SampleUserController"))
                .findFirst().orElseThrow();
        ApiEndpointInfo createUser = userGroup.endpoints().stream()
                .filter(e -> e.methodName().equals("createUser"))
                .findFirst().orElseThrow();
        assertEquals("POST", createUser.httpMethod());
        assertNotNull(createUser.requestBodyType());
        assertFalse(createUser.requestBody().isEmpty());
        assertTrue(createUser.requestBody().stream()
                .anyMatch(f -> f.name().equals("name") && f.required()));
    }

    @Test
    void scanDetectsVoidResponse() {
        DtoFieldExtractor extractor = new DtoFieldExtractor();
        ApiEndpointScanner scanner = new ApiEndpointScanner(
                handlerMapping, extractor,
                new EndpointMetadataResolver("", List.of()),
                new EndpointParameterExtractor(),
                List.of(), List.of());
        List<ControllerGroup> groups = scanner.scan();
        ControllerGroup userGroup = groups.stream()
                .filter(g -> g.controllerName().equals("SampleUserController"))
                .findFirst().orElseThrow();
        ApiEndpointInfo deleteUser = userGroup.endpoints().stream()
                .filter(e -> e.methodName().equals("deleteUser"))
                .findFirst().orElseThrow();
        assertNull(deleteUser.responseType());
        assertTrue(deleteUser.responseFields().isEmpty());
    }

    @Test
    void scanExtractsQueryParams() {
        DtoFieldExtractor extractor = new DtoFieldExtractor();
        ApiEndpointScanner scanner = new ApiEndpointScanner(
                handlerMapping, extractor,
                new EndpointMetadataResolver("", List.of()),
                new EndpointParameterExtractor(),
                List.of(), List.of());
        List<ControllerGroup> groups = scanner.scan();
        ControllerGroup userGroup = groups.stream()
                .filter(g -> g.controllerName().equals("SampleUserController"))
                .findFirst().orElseThrow();
        ApiEndpointInfo listUsers = userGroup.endpoints().stream()
                .filter(e -> e.methodName().equals("listUsers"))
                .findFirst().orElseThrow();
        assertEquals(2, listUsers.queryParams().size());
        assertFalse(listUsers.queryParams().get(0).required());
        assertEquals("0", listUsers.queryParams().get(0).defaultValue());
    }

    @Test
    void scanExcludesPackages() {
        DtoFieldExtractor extractor = new DtoFieldExtractor();
        ApiEndpointScanner scanner = new ApiEndpointScanner(
                handlerMapping, extractor,
                new EndpointMetadataResolver("", List.of()),
                new EndpointParameterExtractor(),
                List.of(),
                List.of("io.github.lgwk42.notiondocs.scanner"));
        List<ControllerGroup> groups = scanner.scan();
        assertTrue(groups.stream()
                .noneMatch(g -> g.controllerName().equals("SampleUserController")));
    }
}
