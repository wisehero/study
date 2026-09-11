package dev.study.javalab.effectiveJava.chapter06;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SearchExampleTest {
    @Test
    void hiddenFailureLooksLikeASuccessfulEmptyResult() {
        assertEquals(List.of(), SearchExample.hiddenFailure(() -> List.of()));
        assertEquals(List.of(), SearchExample.hiddenFailure(() -> {
            throw new IOException("검색 서버 연결 실패");
        }));
    }

    @Test
    void successfulSearchPreservesEmptyAndNonEmptyResults() {
        assertEquals(List.of(), SearchExample.search(() -> List.of()));
        assertEquals(List.of("cream"), SearchExample.search(() -> List.of("cream")));
    }

    @Test
    void failureReachesTheCallerWithItsOriginalCause() {
        var cause = new IOException("검색 서버 연결 실패");
        var error = assertThrows(IllegalStateException.class, () -> SearchExample.search(() -> {
            throw cause;
        }));
        assertEquals("상품 검색에 실패했습니다.", error.getMessage());
        assertSame(cause, error.getCause());
    }
}
