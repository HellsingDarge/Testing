package com.github.hellsingdarge.testing

import com.google.gson.GsonBuilder
import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.Response
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
                        "file.jpg",
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

        Scenario("Valid parameters")
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

        Scenario("Erroneous parameters")
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

            When("Image size exceeds 150kb")
            {

            }

            Then("Should fail - maximum image size is 150k")
            {
                post(2048) {
                    body("error", equalTo("maximum file size 150kb"))
                }
            }

            When("Avatar is an empty file")
            {

            }

            Then("Should fail with \"exceeding\" maximum size")
            {
                Given {
                    multiPart(
                            "avatar",
                            "file.jpg",
                            byteArrayOf(),
                            "image/jpeg"
                    )
                    formParam("email", "$randStr@example.com")
                } When {
                    post("/addavatar")
                } Then {
                    body("error", equalTo("maximum file size 150kb"))
                }
            }
        }

        Scenario("Oddities")
        {
            lateinit var request: Response

            When("File name doesn't have jpg or png extension")
            {
                request = Given {
                    multiPart(
                            "avatar",
                            "file",
                            byteArrayOf(),
                            "image/jpeg"
                    )
                    formParam("email", "manager@mail.ru")
                } When {
                    post("/addavatar")
                }
            }

            Then("Should complain about size beeing too big")
            {
                request.Then {
                    body("error", equalTo("maximum file size 150kb"))
                }
            }
        }
    }
})

