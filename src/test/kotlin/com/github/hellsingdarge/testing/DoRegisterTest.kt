package com.github.hellsingdarge.testing

import com.google.gson.GsonBuilder
import io.restassured.RestAssured
import io.restassured.filter.log.LogDetail
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import org.hamcrest.Matchers.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

object DoRegisterTest: Spek({
    Feature("doRegister")
    {
        RestAssured.baseURI = "http://users.bugred.ru"
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.BODY)

        val gson by memoized { GsonBuilder().serializeNulls().create() }
        val json by memoized { mutableMapOf<String, Any?>() }
        val randStr by memoized { Helpers.randomString() }

        fun post(content: String = gson.toJson(json), block: ValidatableResponse.() -> Unit)
        {
            Given { body(content) } When { post("/tasks/rest/doregister") } Then { block() }
        }

        Scenario("Valid parameters")
        {
            Given("Valid unique user")
            {
                json["email"] = "$randStr@example.com"
                json["name"] = randStr
                json["password"] = "123456789"
            }

            Then("Should return JSON with only email, name and password filled")
            {
                post {
                    body("name", equalTo(randStr))
                    body("avatar", anything())
                    body("password", anything())
                    body("birthday", equalTo(0))
                    body("email", equalTo("$randStr@example.com"))
                    body("gender", equalTo(""))
                    body("date_start", equalTo(0))
                    body("hobby", equalTo(""))
                }
            }
        }

        Scenario("Empty parameters")
        {
            Given("Empty password")
            {
                json["email"] = "$randStr@example.com"
                json["name"] = randStr
                json["password"] = ""
            }

            Then("Should succeed - password can be empty string")
            {
                post {
                    body("name", equalTo(randStr))
                    body("email", equalTo("$randStr@example.com"))
                    body("password", anything())
                }
            }
        }

        Scenario("Erroneous parameters")
        {
            Given("Empty name")
            {
                json["name"] = ""
                json["email"] = "$randStr@example.com"
                json["password"] = "123456789"
            }

            Then("Should fail - must specify name ")
            {
                post {
                    body("type", equalTo("error"))
                    body("message", containsString("фио"))
                }
            }

            Given("Nulls for keys") // aka they aren't specified
            {
                json["name"] = null
                json["email"] = null
                json["password"] = null
            }

            Then("Should complain that required fields isn't specified")
            {
                post {
                    body("type", equalTo("error"))
                    body("message", containsString("является обязательным"))
                }
            }

        }

        Scenario("Faulty parameters")
        {

            Given("Empty json string")
            {
                json.clear()
            }

            Then("Should fail - field isn't specified")
            {
                post {
                    body("type", equalTo("error"))
                    body("message", containsString("обязательным"))
                }
            }

            Given("Empty string as a request")
            {

            }

            Then("Should fail - field isn't specified")
            {
                post("") {
                    body("type", equalTo("error"))
                    body("message", containsString("обязательным"))
                }
            }

            Given("Null char as a value")
            {
                json["name"] = "\u0000" // 0.toChar() is transformed to this, and \0 is illegal escape
                json["email"] = "$randStr@example.com"
                json["password"] = "132456789"
            }

            Then("Should succeed - user generated successfully")
            {
                post {
                    body("name", anything())
                }
            }

            Given("Json string for another method (magicsearch in this case")
            {
                json.clear()
                json["query"] = "Ромашка"
            }

            Then("Should fail - field sin't specified")
            {
                post {
                    body("type", equalTo("error"))
                    body("message", containsString("обязательным"))
                }
            }

            Given("Json with extra fields")
            {
                json["name"] = randStr
                json["email"] = "$randStr@example.com"
                json["password"] = "12345678"
                json["I shouldn't be here"] = "Neither should I"
            }

            Then("Should succeed - isn't read")
            {
                post {
                    body("name", equalTo(randStr))
                }
            }
        }
    }
})
