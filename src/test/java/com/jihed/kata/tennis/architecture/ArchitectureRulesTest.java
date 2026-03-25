package com.jihed.kata.tennis.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.core.importer.ImportOption;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.jihed.kata.tennis",
        importOptions = {ImportOption.DoNotIncludeTests.class},
        cacheMode = CacheMode.FOREVER
)
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_infrastructure_or_frameworks =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..", "org.springframework..", "jakarta..");

    @ArchTest
    static final ArchRule application_must_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..");

    @ArchTest
    static final ArchRule application_must_not_depend_on_frameworks =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta..", "org.slf4j..");


    @ArchTest
    static final ArchRule controller_should_not_depend_on_domain_directly =
            noClasses()
                    .that().resideInAPackage("..infrastructure.adapter.in.rest..")
                    .and().haveSimpleNameEndingWith("Controller")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..domain..");



    @ArchTest
    static final ArchRule rest_dto_should_not_depend_on_domain_or_application =
            noClasses()
                    .that().resideInAPackage("..infrastructure.adapter.in.rest.dto..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..domain..", "..application..");


    @ArchTest
    static final ArchRule strict_layered_architecture =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain").definedBy("..domain..")
                    .layer("Application").definedBy("..application..")
                    .layer("Infrastructure").definedBy("..infrastructure..")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();
}
