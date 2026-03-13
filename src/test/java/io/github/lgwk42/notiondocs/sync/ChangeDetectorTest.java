package io.github.lgwk42.notiondocs.sync;

import io.github.lgwk42.notiondocs.model.ApiEndpointInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangeDetectorTest {

    private final ChangeDetector detector = new ChangeDetector();

    @Test
    void sameEndpointProducesSameHash() {
        ApiEndpointInfo endpoint = createEndpoint("GET", "/api/users");
        String hash1 = detector.computeHash(endpoint);
        String hash2 = detector.computeHash(endpoint);
        assertEquals(hash1, hash2);
    }

    @Test
    void differentMethodProducesDifferentHash() {
        String hash1 = detector.computeHash(createEndpoint("GET", "/api/users"));
        String hash2 = detector.computeHash(createEndpoint("POST", "/api/users"));
        assertNotEquals(hash1, hash2);
    }

    @Test
    void differentUriProducesDifferentHash() {
        String hash1 = detector.computeHash(createEndpoint("GET", "/api/users"));
        String hash2 = detector.computeHash(createEndpoint("GET", "/api/posts"));
        assertNotEquals(hash1, hash2);
    }

    @Test
    void hasChangedReturnsTrueForDifferentHashes() {
        assertTrue(detector.hasChanged("hash1", "hash2"));
    }

    @Test
    void hasChangedReturnsFalseForSameHashes() {
        assertFalse(detector.hasChanged("hash1", "hash1"));
    }

    private ApiEndpointInfo createEndpoint(String method, String uri) {
        return new ApiEndpointInfo(
                "Test API", "", "", List.of(),
                uri, method,
                List.of(), List.of(), List.of(),
                List.of(), null,
                List.of(), null,
                "TestController", "testMethod"
        );
    }
}
