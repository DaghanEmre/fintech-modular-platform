package com.daghanemre.fintech.common.specification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpecificationTest {

    @Test
    @DisplayName("AND specification should be satisfied only if both are satisfied")
    void andSpecificationTest() {
        Specification<String> trueSpec = s -> true;
        Specification<String> falseSpec = s -> false;

        assertThat(trueSpec.and(trueSpec).isSatisfiedBy("test")).isTrue();
        assertThat(trueSpec.and(falseSpec).isSatisfiedBy("test")).isFalse();
        assertThat(falseSpec.and(trueSpec).isSatisfiedBy("test")).isFalse();
        assertThat(falseSpec.and(falseSpec).isSatisfiedBy("test")).isFalse();
    }

    @Test
    @DisplayName("OR specification should be satisfied if either is satisfied")
    void orSpecificationTest() {
        Specification<String> trueSpec = s -> true;
        Specification<String> falseSpec = s -> false;

        assertThat(trueSpec.or(trueSpec).isSatisfiedBy("test")).isTrue();
        assertThat(trueSpec.or(falseSpec).isSatisfiedBy("test")).isTrue();
        assertThat(falseSpec.or(trueSpec).isSatisfiedBy("test")).isTrue();
        assertThat(falseSpec.or(falseSpec).isSatisfiedBy("test")).isFalse();
    }

    @Test
    @DisplayName("NOT specification should negate the wrapped specification")
    void notSpecificationTest() {
        Specification<String> trueSpec = s -> true;
        Specification<String> falseSpec = s -> false;

        assertThat(trueSpec.not().isSatisfiedBy("test")).isFalse();
        assertThat(falseSpec.not().isSatisfiedBy("test")).isTrue();
    }

    @Test
    @DisplayName("AND specification should return the first violation encountered")
    void andViolationTest() {
        SpecificationViolation v1 = new SpecificationViolation("V1", "Violation 1");
        SpecificationViolation v2 = new SpecificationViolation("V2", "Violation 2");

        Specification<String> s1 = new Specification<>() {
            public boolean isSatisfiedBy(String c) {
                return false;
            }

            public SpecificationViolation violation(String c) {
                return v1;
            }
        };

        Specification<String> s2 = new Specification<>() {
            public boolean isSatisfiedBy(String c) {
                return false;
            }

            public SpecificationViolation violation(String c) {
                return v2;
            }
        };

        SpecificationViolation violation = s1.and(s2).violation("test");
        assertThat(violation.code()).isEqualTo("V1");

        violation = s2.and(s1).violation("test");
        assertThat(violation.code()).isEqualTo("V2");
    }
}
