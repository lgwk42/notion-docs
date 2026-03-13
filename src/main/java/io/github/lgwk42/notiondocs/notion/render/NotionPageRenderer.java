package io.github.lgwk42.notiondocs.notion.render;

import io.github.lgwk42.notiondocs.model.ApiEndpointInfo;
import io.github.lgwk42.notiondocs.model.ParameterInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.lgwk42.notiondocs.notion.render.NotionBlockBuilder.codeBlock;
import static io.github.lgwk42.notiondocs.notion.render.NotionBlockBuilder.emptyParagraph;
import static io.github.lgwk42.notiondocs.notion.render.NotionBlockBuilder.heading2;
import static io.github.lgwk42.notiondocs.notion.render.NotionBlockBuilder.heading3;
import static io.github.lgwk42.notiondocs.notion.render.NotionBlockBuilder.paragraph;
import static io.github.lgwk42.notiondocs.notion.render.NotionBlockBuilder.table;

/**
 * Renders the detail page body of an API endpoint as Notion blocks.
 * Delegates JSON example generation to {@link JsonExampleGenerator}
 * and parameter table generation to {@link FieldTableBuilder}.
 */
public class NotionPageRenderer {

    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH");

    private final JsonExampleGenerator jsonExampleGenerator;
    private final FieldTableBuilder fieldTableBuilder;

    public NotionPageRenderer(JsonExampleGenerator jsonExampleGenerator,
                              FieldTableBuilder fieldTableBuilder) {
        this.jsonExampleGenerator = jsonExampleGenerator;
        this.fieldTableBuilder = fieldTableBuilder;
    }

    /**
     * Generates the page body blocks for an endpoint.
     */
    public List<Map<String, Object>> renderEndpointPageBody(ApiEndpointInfo endpoint) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        blocks.add(heading2("Description"));
        if (endpoint.description() != null && !endpoint.description().isEmpty()) {
            blocks.add(paragraph(endpoint.description()));
        } else {
            blocks.add(emptyParagraph());
        }
        blocks.add(heading2("Request"));
        blocks.addAll(renderRequest(endpoint));
        blocks.add(heading2("Response"));
        blocks.addAll(renderResponse(endpoint));
        return blocks;
    }

    private List<Map<String, Object>> renderRequest(ApiEndpointInfo endpoint) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        if (BODY_METHODS.contains(endpoint.httpMethod())) {
            blocks.add(heading3("Request Body"));
            if (!endpoint.requestBody().isEmpty()) {
                String jsonExample = jsonExampleGenerator.generate(endpoint.requestBody());
                blocks.add(codeBlock(jsonExample, "json"));
                blocks.add(fieldTableBuilder.build(endpoint.requestBody()));
            } else {
                blocks.add(paragraph("None"));
            }
        } else {
            if (!endpoint.queryParams().isEmpty() || !endpoint.pathVariables().isEmpty()) {
                blocks.add(heading3("Request Param"));
                blocks.add(buildQueryParamTable(endpoint));
            } else {
                blocks.add(paragraph("None"));
            }
        }
        if (BODY_METHODS.contains(endpoint.httpMethod()) &&
                (!endpoint.queryParams().isEmpty() || !endpoint.pathVariables().isEmpty())) {
            blocks.add(heading3("Request Param"));
            blocks.add(buildQueryParamTable(endpoint));
        }
        return blocks;
    }

    private List<Map<String, Object>> renderResponse(ApiEndpointInfo endpoint) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        if (endpoint.responseType() == null) {
            blocks.add(paragraph("No response (void)"));
            return blocks;
        }
        if (!endpoint.responseFields().isEmpty()) {
            String jsonExample = jsonExampleGenerator.generate(endpoint.responseFields());
            blocks.add(codeBlock(jsonExample, "json"));
            blocks.add(fieldTableBuilder.build(endpoint.responseFields()));
        } else {
            blocks.add(paragraph(endpoint.responseType()));
        }
        return blocks;
    }

    private Map<String, Object> buildQueryParamTable(ApiEndpointInfo endpoint) {
        List<List<String>> rows = new ArrayList<>();
        for (ParameterInfo pv : endpoint.pathVariables()) {
            String desc = pv.required() ? "(required)" : "(optional)";
            rows.add(List.of(pv.name(), pv.type(), desc));
        }
        for (ParameterInfo qp : endpoint.queryParams()) {
            String desc = qp.required() ? "(required)" : "(optional)";
            if (qp.defaultValue() != null) {
                desc += " default: " + qp.defaultValue();
            }
            rows.add(List.of(qp.name(), qp.type(), desc));
        }
        return table(
                List.of("Parameter", "Type", "Description"),
                rows
        );
    }
}
