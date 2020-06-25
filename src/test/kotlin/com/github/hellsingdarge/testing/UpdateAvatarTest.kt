package com.github.hellsingdarge.testing

import com.google.gson.GsonBuilder
import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import org.hamcrest.Matchers.equalTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.net.URL

object UpdateAvatarTest: Spek({
    Feature("Updating avatar")
    {
        RestAssured.baseURI = "http://users.bugred.ru"
        RestAssured.basePath = "tasks/rest"
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
        RestAssured.proxy(8080)

        val gson by memoized { GsonBuilder().serializeNulls().create() }
        val json by memoized { mutableMapOf<String, Any?>() }
        val randStr by memoized { Helpers.randomString() }

        fun post(imageSize: Int = 256, block: ValidatableResponse.() -> Unit)
        {
            Given {
                multiPart(
                        "avatar",
                        "avatar",
                        URL("https://picsum.photos/$imageSize.jpg").readBytes(),
                        "image/jpeg"
                )
                formParam("email", "$randStr@example.com")
            } When {
                post("/addavatar")
            } Then {
                block()
            }
        }

        Scenario("Valid paremeters")
        {
            Given("Existing user")
            {
                json["name"] = randStr
                json["email"] = "$randStr@example.com"
                json["tasks"] = listOf(56)
                json["companies"] = listOf(7, 8)
                // must be done with createUser, won't work with doRegister
                Given { body(gson.toJson(json)) } When { post("/createuser") }
            }

            Then("Should be successful")
            {
                post {
                    body("status", equalTo("ok"))
                }
            }
        }
    }
})

