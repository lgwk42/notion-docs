package io.github.lgwk42.notiondocs.notion.exception;

/**
 * Exception thrown when a Notion API call fails.
 */
public class NotionApiException extends RuntimeException {

    private final int statusCode;
    private final String notionErrorCode;

    public NotionApiException(String message, int statusCode, String notionErrorCode) {
        super(message);
        this.statusCode = statusCode;
        this.notionErrorCode = notionErrorCode;
    }

    public NotionApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.notionErrorCode = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getNotionErrorCode() {
        return notionErrorCode;
    }
}
