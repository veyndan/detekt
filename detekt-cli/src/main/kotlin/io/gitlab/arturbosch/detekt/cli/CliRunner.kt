package io.gitlab.arturbosch.detekt.cli

import io.github.detekt.tooling.api.AnalysisResult
import io.github.detekt.tooling.api.DetektCli
import io.github.detekt.tooling.api.UnexpectedError
import io.gitlab.arturbosch.detekt.cli.runners.AstPrinter
import io.gitlab.arturbosch.detekt.cli.runners.ConfigExporter
import io.gitlab.arturbosch.detekt.cli.runners.Runner
import io.gitlab.arturbosch.detekt.cli.runners.VersionPrinter
import io.gitlab.arturbosch.detekt.core.DetektResult
import io.gitlab.arturbosch.detekt.core.tooling.DefaultAnalysisResult

class CliRunner : DetektCli {

    override fun run(args: Array<String>): AnalysisResult = run(args, System.out, System.err)

    override fun run(args: Array<String>, outputChannel: Appendable, errorChannel: Appendable): AnalysisResult {
        val arguments = runCatching { parseArguments(args) }
            .getOrElse { return DefaultAnalysisResult(null, UnexpectedError(it)) }

        val specialRunner = when {
            arguments.showVersion -> VersionPrinter(outputChannel)
            arguments.generateConfig -> ConfigExporter(arguments, outputChannel)
            arguments.printAst -> AstPrinter(arguments, outputChannel)
            else -> null
        }

        return if (specialRunner != null) {
            runCatching { specialRunner.execute() }
                .map { DefaultAnalysisResult(DetektResult(emptyMap())) }
                .getOrElse { DefaultAnalysisResult(null, UnexpectedError(it)) }
        } else {
            Runner(arguments, outputChannel, errorChannel).call()
        }
    }
}
