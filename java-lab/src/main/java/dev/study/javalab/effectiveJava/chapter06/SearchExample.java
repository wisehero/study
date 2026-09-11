package dev.study.javalab.effectiveJava.chapter06;

import java.io.IOException;
import java.util.List;

public final class SearchExample {
    private SearchExample() {}

    @FunctionalInterface
    public interface SearchClient {
        List<String> search() throws IOException;
    }

    // 잘못된 예: 조회 실패와 정상적인 결과 없음을 구분할 수 없다.
    public static List<String> hiddenFailure(SearchClient client) {
        try {
            return client.search();
        } catch (IOException e) {
            return List.of();
        }
    }

    // 개선 예: 호출자에게 실패를 전달하고 원인도 보존한다.
    public static List<String> search(SearchClient client) {
        try {
            return client.search();
        } catch (IOException e) {
            throw new IllegalStateException("상품 검색에 실패했습니다.", e);
        }
    }
}
