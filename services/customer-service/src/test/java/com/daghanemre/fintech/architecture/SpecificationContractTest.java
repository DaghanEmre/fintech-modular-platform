package com.daghanemre.fintech.architecture;

import com.daghanemre.fintech.common.specification.Specification;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ArchUnit-based architectural tests for Specification Pattern compliance.
 * 
 * <p>These tests enforce compile-time contracts that prevent common mistakes
 * in specification implementations. Violations cause build failures, ensuring
 * broken specifications never reach production.
 * 
 * <p><b>Enforced Rules:</b>
 * <ul>
 *   <li>All concrete specifications MUST override {@code violation()} method</li>
 *   <li>Prevents specifications from using default empty violation</li>
 *   <li>Ensures domain errors are always observable</li>
 * </ul>
 * 
 * <p>See ADR-0004 for rationale on specification contract enforcement.
 */
@DisplayName("Specification Pattern - Architectural Contract Tests")
class SpecificationContractTest {

    /**
     * Enforces that all concrete specifications override the violation() method.
     * 
     * <p><b>Rationale:</b>
     * If a specification can fail ({@code isSatisfiedBy} returns false), it MUST
     * provide a meaningful violation. The default {@code violation()} returns
     * {@code SpecificationViolation.none()}, which is only appropriate for
     * specifications that always pass or for abstract base classes.
     * 
     * <p><b>Why This Rule Exists:</b>
     * <ul>
     *   <li>Prevents silent failures in domain logic</li>
     *   <li>Ensures observability (metrics, logs, HTTP errors)</li>
     *   <li>Catches developer mistakes at build time, not runtime</li>
     * </ul>
     * 
     * <p><b>Exemptions:</b>
     * <ul>
     *   <li>Abstract specifications (not concrete classes)</li>
     *   <li>Composite specifications (AndSpecification, OrSpecification, NotSpecification)</li>
     * </ul>
     * 
     * <p><b>Example Failure:</b>
     * <pre>
     * // This will FAIL the test:
     * public class CustomerIsFooSpec implements Specification&lt;Customer&gt; {
     *     public boolean isSatisfiedBy(Customer c) { return false; }
     *     // Missing: violation() override
     * }
     * 
     * // Error message:
     * "Specification CustomerIsFooSpec must override violation() method"
     * </pre>
     */
    @Test
    @DisplayName("All concrete specifications must override violation() method")
    void allConcreteSpecificationsMustOverrideViolationMethod() {
        // Import all classes from fintech packages
        var classes = new ClassFileImporter()
            .importPackages("com.daghanemre.fintech");

        // Filter to concrete classes implementing Specification
        var concreteSpecs = StreamSupport.stream(classes.spliterator(), false)
            .filter(c -> !c.getModifiers().contains(JavaModifier.ABSTRACT)) // Not abstract
            .filter(c -> !c.isInterface()) // Not interface
            .filter(c -> c.isAssignableTo(Specification.class))
            .filter(c -> !c.getSimpleName().equals("Specification")) // Exclude interface itself
            .toList();

        // Check each concrete specification
        for (JavaClass specClass : concreteSpecs) {
            boolean overridesViolation = hasViolationMethodOverride(specClass);
            
            assertThat(overridesViolation)
                .as("""
                    Specification %s must override violation() method.
                    
                    All concrete specifications that can fail MUST provide a meaningful violation.
                    This ensures domain errors are observable (metrics, logs, HTTP responses).
                    
                    Fix: Add this method to your specification:
                    
                    @Override
                    public SpecificationViolation violation(%s candidate) {
                        return new SpecificationViolation(
                            "YOUR_ERROR_CODE",
                            "Your error message"
                        );
                    }
                    
                    See ADR-0004 for specification naming conventions and best practices.
                    """,
                    specClass.getName(),
                    specClass.getTypeParameters().isEmpty() ? "T" : "...")
                .isTrue();
        }
    }

    /**
     * Checks if a class overrides the violation() method from Specification interface.
     * 
     * <p>A method is considered an override if:
     * <ul>
     *   <li>Method name is "violation"</li>
     *   <li>Method is not declared in the Specification interface itself</li>
     *   <li>Method is not inherited from a parent class (direct override)</li>
     * </ul>
     * 
     * @param specClass the specification class to check
     * @return true if violation() is overridden, false otherwise
     */
    private boolean hasViolationMethodOverride(JavaClass specClass) {
        return specClass.getMethods().stream()
            .filter(m -> m.getName().equals("violation"))
            .anyMatch(m -> isDeclaredInClass(m, specClass));
    }

    /**
     * Checks if a method is declared directly in the given class (not inherited).
     * 
     * @param method the method to check
     * @param declaringClass the class that should declare the method
     * @return true if method is declared in declaringClass
     */
    private boolean isDeclaredInClass(JavaMethod method, JavaClass declaringClass) {
        String methodOwner = method.getOwner().getName();
        String className = declaringClass.getName();
        
        // Exclude default method from Specification interface
        return !methodOwner.equals(Specification.class.getName())
            && methodOwner.equals(className);
    }
}
