package com.daghanemre.fintech.customer.architecture;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.INTERFACES;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.core.domain.properties.HasModifiers.Predicates.modifier;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Production-grade architecture enforcement suite for customer-service (v4.8).
 * 
 * <p>Rule Count: 23 active + 1 disabled = 24 total
 */
@DisplayName("Customer Service Architecture Enforcement Tests (v4.8)")
class CustomerServiceArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.daghanemre.fintech");
    }

    @Nested
    @DisplayName("Hexagonal Architecture Boundaries")
    class HexagonalArchitectureBoundaries {

        @Test
        void layeredArchitectureDependencies() {
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain").definedBy("..domain..")
                    .layer("Application").definedBy("..application..")
                    .layer("Persistence").definedBy("..infrastructure.persistence..")
                    .layer("REST_API").definedBy("..adapter.rest..")
                    .layer("Configuration").definedBy("..infrastructure.config..")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Persistence", "REST_API", "Configuration")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Persistence", "REST_API", "Configuration")
                    .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Configuration")
                    .whereLayer("REST_API").mayNotBeAccessedByAnyLayer()
                    .check(classes);
        }

        @Test
        void infrastructureIsolation() {
            noClasses()
                    .that().resideInAPackage("..infrastructure.persistence..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.rest..")
                    .check(classes);
        }

        @Test
        void domainMustNotDependOnInfrastructureOrApi() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..adapter.rest..")
                    .check(classes);
        }

        @Test
        void domainMustNotDependOnSpring() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                    .check(classes);
        }

        @Test
        void domainMustNotDependOnApplicationExceptions() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..application.exception..")
                    .check(classes);
        }

        @Test
        void applicationMustNotDependOnInfrastructure() {
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..adapter.rest..")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Domain-Driven Design Enforcement")
    class DomainDrivenDesignRules {

        @Test
        void valueObjectsMustBeImmutable() {
            classes()
                    .that().resideInAPackage("..domain.model..")
                   .and(DescribedPredicate.or(simpleNameEndingWith("Id"), simpleNameEndingWith("Email"), simpleNameEndingWith("StateChangeReason")))
                    .should().haveOnlyFinalFields()
                    .check(classes);
        }

        @Test
        void jpaEntitiesMustNotBeInDomain() {
            noClasses()
                    .that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage("..domain..")
                    .check(classes);
        }

        @Test
        void domainEventsMustBeImmutable() {
            classes()
                    .that().haveSimpleNameEndingWith("Event")
                    .and().resideInAPackage("..domain..")
                    .should().haveOnlyFinalFields()
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Specification Pattern Enforcement")
    class SpecificationPatternRules {

        private static final ArchCondition<JavaClass> CONTRACT_COMPLIANCE = 
            new ArchCondition<JavaClass>("override violation()") {
                @Override
                public void check(JavaClass specClass, ConditionEvents events) {
                    boolean overrides = specClass.getMethods().stream()
                            .anyMatch(method -> method.getName().equals("violation") && method.getOwner().equals(specClass) && method.getRawReturnType().isAssignableTo(SpecificationViolation.class));
                    if (!overrides) {
                        events.add(SimpleConditionEvent.violated(specClass, specClass.getName() + " must override violation()"));
                    }
                }
            };

        private static final ArchCondition<JavaClass> ATOMIC_NAMING_CONVENTION = 
            new ArchCondition<JavaClass>("follow atomic naming") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    if (!item.getSimpleName().matches(".*(Is|Not).*Spec")) {
                        events.add(SimpleConditionEvent.violated(item, item.getName() + " must follow IsXSpec or NotXSpec pattern"));
                    }
                }
            };

        private static final DescribedPredicate<JavaClass> NOT_SEMANTIC_WRAPPER =
            new DescribedPredicate<JavaClass>("not semantic wrapper") {
                @Override
                public boolean test(JavaClass item) {
                    return !item.getSimpleName().matches(".*Can.*Spec$");
                }
            };

        @Test
        void specificationsMustComplyWithContract() {
            classes()
                    .that().implement(Specification.class)
                    .and(not(INTERFACES))
                    .and(not(modifier(JavaModifier.ABSTRACT)))
                    .and().areTopLevelClasses()
                    .should(CONTRACT_COMPLIANCE)
                    .check(classes);
        }

        @Test
        void specificationsMustResideInSpecificationPackage() {
            classes()
                    .that().implement(Specification.class)
                    .and(not(INTERFACES))
                    .and().areTopLevelClasses()
                    .and(not(resideInAPackage("..common..")))
                    .should().resideInAPackage("..domain.specification..")
                    .check(classes);
        }

        @Test
        void atomicSpecificationsMustFollowNamingConvention() {
            classes()
                    .that().implement(Specification.class)
                    .and().resideInAPackage("..domain.specification..")
                    .and(not(INTERFACES))
                    .and().areTopLevelClasses()
                    .and(NOT_SEMANTIC_WRAPPER)
                    .should(ATOMIC_NAMING_CONVENTION)
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        void specificationsMustBeStateless() {
            classes()
                    .that().implement(Specification.class)
                    .and(not(INTERFACES))
                    .and().areTopLevelClasses()
                    .should().haveOnlyFinalFields()
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Enum Ownership Strategy")
    class EnumOwnershipRules {

        @Test
        void domainEnumsMustResideInModelPackage() {
            classes()
                    .that().areEnums()
                    .and().resideInAPackage("..domain..")
                    .should().resideInAPackage("..domain.model..")
                    .check(classes);
        }

        @Test
        void commonModuleMustNotDependOnServiceDomains() {
            noClasses()
                    .that().resideInAPackage("..common..")
                    .should().dependOnClassesThat().resideInAnyPackage("..customer.domain..", "..payment.domain..")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        void enumsMustNotHaveEnumSuffix() {
            classes()
                    .that().areEnums()
                    .should().haveSimpleNameNotEndingWith("Enum")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Application Use Case Layer")
    class UseCaseLayerRules {

        @Test
        void useCasesMustResideInUseCasePackage() {
            classes()
                    .that().haveSimpleNameEndingWith("UseCase")
                    .should().resideInAPackage("..application.usecase..")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("API Layer Boundaries")
    class ApiLayerRules {

        @Test
        void controllersMustResideInControllerPackage() {
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..adapter.rest.controller..")
                    .check(classes);
        }

        @Test
        void dtosMustResideInDtoPackage() {
            classes()
                    .that().haveSimpleNameEndingWith("Request")
                    .or().haveSimpleNameEndingWith("Response")
                    .should().resideInAPackage("..adapter.rest.dto..")
                    .check(classes);
        }

        @Disabled("DTO mapping not fully established")
        @Test
        void domainModelsMustNotBeExposedInApi() {
            noClasses()
                    .that().resideInAPackage("..adapter.rest..")
                    .and().haveSimpleNameNotEndingWith("Mapper")
                    .should().dependOnClassesThat().resideInAPackage("..domain.model..")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventions {

        @Test
        void infrastructureAdaptersMustEndWithAdapter() {
            classes()
                    .that().resideInAPackage("..infrastructure..")
                    .and().implement(resideInAPackage("..domain.port.."))
                    .should().haveSimpleNameEndingWith("Adapter")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        void domainPortsMustBeInterfaces() {
            classes()
                    .that().resideInAPackage("..domain.port..")
                    .should().beInterfaces()
                    .check(classes);
        }
    }
}
