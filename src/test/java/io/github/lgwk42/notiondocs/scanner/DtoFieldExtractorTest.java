package io.github.lgwk42.notiondocs.scanner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.github.lgwk42.notiondocs.model.FieldInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoFieldExtractorTest {

    private DtoFieldExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new DtoFieldExtractor();
    }

    record SimpleDto(@NotNull String name, Integer age) {}

    record NestedDto(@NotBlank String title, SimpleDto author) {}

    record GenericWrapper(String status, List<SimpleDto> items) {}

    static class PojoDto {
        @NotNull
        private String email;
        private int count;
        private static String IGNORED = "static";
        private transient String alsoIgnored;
    }

    record CircularA(String value, CircularB other) {}
    record CircularB(String value, CircularA other) {}

    // Generic wrapper similar to BaseResponseData<T>
    static class BaseResponse {
        private int status;
        private String message;
    }
    static class BaseResponseData<T> extends BaseResponse {
        private T data;
    }
    record InnerData(String id, String email) {}

    @SuppressWarnings("unused")
    private BaseResponseData<InnerData> dummyWrappedField;

    @Test
    void extractSimpleRecord() {
        List<FieldInfo> fields = extractor.extract(SimpleDto.class);
        assertEquals(2, fields.size());
        assertEquals("name", fields.get(0).name());
        assertEquals("String", fields.get(0).type());
        assertTrue(fields.get(0).required());
        assertEquals("age", fields.get(1).name());
        assertEquals("Integer", fields.get(1).type());
        assertFalse(fields.get(1).required());
    }

    @Test
    void extractNestedRecord() {
        List<FieldInfo> fields = extractor.extract(NestedDto.class);
        assertEquals(2, fields.size());
        assertEquals("title", fields.get(0).name());
        assertTrue(fields.get(0).required());
        FieldInfo authorField = fields.get(1);
        assertEquals("author", authorField.name());
        assertEquals("SimpleDto", authorField.type());
        assertFalse(authorField.children().isEmpty());
        assertEquals(2, authorField.children().size());
        assertEquals("name", authorField.children().get(0).name());
    }

    @Test
    void extractPojo() {
        List<FieldInfo> fields = extractor.extract(PojoDto.class);
        assertEquals(2, fields.size());
        assertEquals("email", fields.get(0).name());
        assertTrue(fields.get(0).required());
        assertEquals("count", fields.get(1).name());
    }

    @Test
    void extractVoidReturnsEmpty() {
        List<FieldInfo> fields = extractor.extract(void.class);
        assertTrue(fields.isEmpty());
    }

    @Test
    void extractStringReturnsEmpty() {
        List<FieldInfo> fields = extractor.extract(String.class);
        assertTrue(fields.isEmpty());
    }

    @Test
    void circularReferenceDoesNotInfiniteLoop() {
        List<FieldInfo> fields = extractor.extract(CircularA.class);
        assertNotNull(fields);
        assertFalse(fields.isEmpty());
    }

    @Test
    void formatTypeName() {
        assertEquals("String", DtoFieldExtractor.formatTypeName(String.class));
        assertEquals("int", DtoFieldExtractor.formatTypeName(int.class));
    }

    @SuppressWarnings("unused")
    private List<SimpleDto> dummyListField;

    @Test
    void extractListType() throws NoSuchFieldException {
        Type listType = DtoFieldExtractorTest.class.getDeclaredField("dummyListField").getGenericType();
        List<FieldInfo> fields = extractor.extract(listType);
        assertEquals(2, fields.size());
        assertEquals("name", fields.get(0).name());
    }

    @Test
    void extractGenericWrapperResolvesTypeVariable() throws NoSuchFieldException {
        Type wrappedType = DtoFieldExtractorTest.class.getDeclaredField("dummyWrappedField").getGenericType();
        List<FieldInfo> fields = extractor.extract(wrappedType);
        // Should have: data (with children id, email), status, message from BaseResponse
        FieldInfo dataField = fields.stream()
                .filter(f -> f.name().equals("data"))
                .findFirst().orElseThrow(() -> new AssertionError("data field not found"));
        assertEquals("InnerData", dataField.type());
        assertFalse(dataField.children().isEmpty());
        assertEquals(2, dataField.children().size());
        assertEquals("id", dataField.children().get(0).name());
        assertEquals("email", dataField.children().get(1).name());
        // Should also have status and message from parent
        assertTrue(fields.stream().anyMatch(f -> f.name().equals("status")));
        assertTrue(fields.stream().anyMatch(f -> f.name().equals("message")));
    }
}
