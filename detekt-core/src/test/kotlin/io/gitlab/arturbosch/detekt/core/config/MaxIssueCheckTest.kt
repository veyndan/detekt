package io.gitlab.arturbosch.detekt.core.config

import io.github.detekt.tooling.api.MaxIssuesReached
import io.github.detekt.tooling.api.spec.ProcessingSpec
import io.github.detekt.tooling.api.spec.RulesSpec
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.yamlConfigFromContent
import org.assertj.core.api.Assertions.assertThatCode
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class MaxIssueCheckTest : Spek({

    describe("based only on MaxIssuePolicy") {

        fun createFixture(policy: RulesSpec.MaxIssuePolicy) =
            MaxIssueCheck(ProcessingSpec { rules { maxIssuePolicy = policy } }.rulesSpec, Config.empty)

        context("policy of any") {
            val fixture = createFixture(RulesSpec.MaxIssuePolicy.AllowAny)

            listOf(-1, 0, 1, 100).forEach {
                it("passes on $it issues") {
                    assertThatCode { fixture.check(it) }.doesNotThrowAnyException()
                }
            }
        }

        context("policy of none") {
            val fixture = createFixture(RulesSpec.MaxIssuePolicy.NoneAllowed)

            it("passes on zero issues") {
                assertThatCode { fixture.check(0) }.doesNotThrowAnyException()
            }

            listOf(-1, 1, 100).forEach {
                it("fails on $it issues") {
                    assertThatCode { fixture.check(it) }.isInstanceOf(MaxIssuesReached::class.java)
                }
            }
        }

        context("policy of specified amount of 2 issues") {
            val fixture = createFixture(RulesSpec.MaxIssuePolicy.AllowAmount(2))

            listOf(-1, 0, 1, 2).forEach {
                it("passes on $it issues") {
                    assertThatCode { fixture.check(it) }.doesNotThrowAnyException()
                }
            }

            listOf(3, 100).forEach {
                it("fails on $it issues") {
                    assertThatCode { fixture.check(it) }.isInstanceOf(MaxIssuesReached::class.java)
                }
            }
        }
    }

    describe("based on config") {
        it("ignores RulesSpec when Config declares build section due to historical reasons") {
            val fixture = MaxIssueCheck(
                ProcessingSpec { rules { maxIssuePolicy = RulesSpec.MaxIssuePolicy.NoneAllowed } }.rulesSpec,
                yamlConfigFromContent("""
             build:
               maxIssues: 1
         """.trimIndent())
            )

            assertThatCode { fixture.check(1) }.doesNotThrowAnyException()
        }
    }
})
