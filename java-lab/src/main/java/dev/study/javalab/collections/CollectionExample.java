package dev.study.javalab.collections;

import java.util.LinkedHashSet;
import java.util.List;

public class CollectionExample {
    public static List<ProductCode> uniqueCodes(List<ProductCode> codes) {
        // 처음 나타난 순서로 중복을 제거하고 입력 목록과 분리된 결과를 반환한다.
        return List.copyOf(new LinkedHashSet<>(codes));
    }

    public static void main(String[] args) {
        var input = List.of(new ProductCode("CREAM"), new ProductCode("TONER"), new ProductCode("CREAM"));
        System.out.println("입력: " + input);
        System.out.println("중복 제거: " + uniqueCodes(input));
        System.out.println("실습: record를 일반 클래스로 바꾸면 중복 판정이 어떻게 달라지는지 확인하세요.");
    }
}
