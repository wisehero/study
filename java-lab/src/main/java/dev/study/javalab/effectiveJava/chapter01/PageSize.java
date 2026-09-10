package dev.study.javalab.effectiveJava.chapter01;

/** 이 실습의 페이지 크기 계약은 1~100이다. 실제 업무에 맞춰 범위를 정한다. */
public record PageSize(int value) {
    public PageSize {
        if (value < 1 || value > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100이어야 합니다.");
        }
    }
}
