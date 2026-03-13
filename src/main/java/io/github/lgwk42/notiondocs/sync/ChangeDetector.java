package io.github.lgwk42.notiondocs.sync;

import io.github.lgwk42.notiondocs.model.ApiEndpointInfo;
import io.github.lgwk42.notiondocs.model.FieldInfo;
import io.github.lgwk42.notiondocs.model.ResponseCaseInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Computes SHA-256 hashes of API endpoint metadata to detect changes.
 */
public class ChangeDetector {

    /**
     * Computes a content hash from the endpoint metadata.
     */
    public String computeHash(ApiEndpointInfo endpoint) {
        StringBuilder sb = new StringBuilder();
        sb.append(endpoint.uri()).append("|");
        sb.append(endpoint.httpMethod()).append("|");
        endpoint.headers().forEach(h ->
                sb.append("H:").append(h.name()).append(":").append(h.type())
                        .append(":").append(h.required()).append("|"));
        endpoint.pathVariables().forEach(p ->
                sb.append("PV:").append(p.name()).append(":").append(p.type())
                        .append(":").append(p.required()).append("|"));
        endpoint.queryParams().forEach(p ->
                sb.append("QP:").append(p.name()).append(":").append(p.type())
                        .append(":").append(p.required()).append(":").append(p.defaultValue()).append("|"));
        if (endpoint.requestBodyType() != null) {
            sb.append("RB:").append(endpoint.requestBodyType()).append("|");
            appendFields(sb, endpoint.requestBody(), "RBF");
        }
        if (endpoint.responseType() != null) {
            sb.append("RS:").append(endpoint.responseType()).append("|");
            appendFields(sb, endpoint.responseFields(), "RSF");
        } else {
            sb.append("RS:void|");
        }
        for (ResponseCaseInfo rc : endpoint.responseCases()) {
            sb.append("RC:").append(rc.status()).append(":").append(rc.description()).append("|");
            if (rc.responseType() != null) {
                sb.append("RCT:").append(rc.responseType()).append("|");
                appendFields(sb, rc.responseFields(), "RCF");
            }
        }
        return sha256(sb.toString());
    }

    private void appendFields(StringBuilder sb, List<FieldInfo> fields, String prefix) {
        for (FieldInfo field : fields) {
            sb.append(prefix).append(":").append(field.name()).append(":").append(field.type())
                    .append(":").append(field.required()).append("|");
            if (!field.children().isEmpty()) {
                appendFields(sb, field.children(), prefix);
            }
        }
    }

    /**
     * Compares two hashes and returns true if they differ.
     */
    public boolean hasChanged(String existingHash, String newHash) {
        return !newHash.equals(existingHash);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
