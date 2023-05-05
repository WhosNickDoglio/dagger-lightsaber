package es.lidlplus.libs.lightsaber.checkers

import com.google.testing.compile.CompilationSubject
import es.lidlplus.libs.lightsaber.ReportType
import es.lidlplus.libs.lightsaber.createCompiler
import es.lidlplus.libs.lightsaber.createSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnusedDependenciesKtTest {

    private val compiler = createCompiler(unusedDependencies = ReportType.Error)

    private val dependency = createSource(
        """
            package test;

            public interface Dependency {
                String dependency();
            }
        """.trimIndent(),
    )

    @Test
    fun dependencyNotUsed() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(dependencies = {Dependency.class})
                public interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(component, dependency)

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The dependency `test.Dependency` is not used.")
            .inFile(component)
            .onLineContaining("MyComponent")
    }

    @Test
    fun dependencyUsedOnComponent() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(dependencies = {Dependency.class})
                public interface MyComponent {
                    String dependency();
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, dependency)

        CompilationSubject.assertThat(compilation)
            .succeededWithoutWarnings()
    }

    @Test
    fun dependencyUsedOnSubcomponent() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(dependencies = {Dependency.class})
                public interface MyComponent {
                    MySubcomponent subcomponent();
                }
            """.trimIndent(),
        )
        val subcomponent = createSource(
            """
                package test;

                import dagger.Subcomponent;

                @Subcomponent
                public interface MySubcomponent {
                    String dependency();
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, dependency)

        CompilationSubject.assertThat(compilation)
            .succeededWithoutWarnings()
    }

    @Nested
    internal inner class ReportTypes {
        private val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(dependencies = {Dependency.class})
                public interface MyComponent {
                }
            """.trimIndent(),
        )
        private val dependency = createSource(
            """
            package test;

            public interface Dependency {
                String dependency();
            }
        """.trimIndent(),
        )

        @Test
        fun testError() {
            val compilation = createCompiler(unusedDependencies = ReportType.Error)
                .compile(component, dependency)

            CompilationSubject.assertThat(compilation)
                .hadErrorCount(1)
        }

        @Test
        fun testWarning() {
            val compilation = createCompiler(unusedDependencies = ReportType.Warning)
                .compile(component, dependency)

            CompilationSubject.assertThat(compilation)
                .hadWarningCount(1)
        }

        @Test
        fun testIgnore() {
            val compilation = createCompiler(unusedDependencies = ReportType.Ignore)
                .compile(component, dependency)

            CompilationSubject.assertThat(compilation)
                .succeededWithoutWarnings()
        }
    }
}
