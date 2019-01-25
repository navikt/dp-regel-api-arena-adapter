package no.nav.dagpenger.regel.api.arena.adapter

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    plugin = [
        "pretty",
        "json:build/cucumber.json"
    ],
    features = ["src/test/resources/features"],
    tags = ["not @ignored"]
)
class RunCucumber
