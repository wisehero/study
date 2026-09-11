package dev.study.javalab.effectiveJava.chapter08;

import java.io.IOException;

public final class ResourceExample {
    private ResourceExample() {}

    // 이 메서드는 전달받은 자원을 닫을 책임도 인수한다.
    public static String read(Resource resource, IOException readFailure) throws IOException {
        try (resource) {
            if (readFailure != null) throw readFailure;
            return "읽기 성공";
        }
    }

    // 파일 없이 close 호출과 정리 실패를 관찰하기 위한 모의 자원이다.
    public static final class Resource implements AutoCloseable {
        private final IOException closeFailure;
        private boolean closeCalled;

        public Resource(IOException closeFailure) { this.closeFailure = closeFailure; }
        public boolean closeCalled() { return closeCalled; }

        @Override
        public void close() throws IOException {
            closeCalled = true;
            if (closeFailure != null) throw closeFailure;
        }
    }
}
