package com.github.hellsingdarge.testing

import com.google.gson.GsonBuilder
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.filter.log.LogDetail
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import org.hamcrest.Matchers.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

object GetUserTest: Spek({
    Feature("Getting user by email")
    {
        RestAssured.reset()
        RestAssured.baseURI = "http://users.bugred.ru"
        RestAssured.basePath = "tasks/rest"
        RestAssured.config = RestAssured.config().encoderConfig(EncoderConfig.encoderConfig().defaultCharsetForContentType("UTF-8", "application/json"))
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.BODY)

        val gson by memoized { GsonBuilder().serializeNulls().create() }
        val json by memoized { mutableMapOf<String, Any?>() }
        val randStr by memoized { Helpers.randomString() }

        fun post(block: ValidatableResponse.() -> Unit)
        {
            Given { body(gson.toJson(json)) } When { post("/getUser") } Then { block() }
        }

        Scenario("Valid parameters")
        {
            Given("User with all fields being set")
            {
                json["name"] = randStr
                json["email"] = "$randStr@example.com"
                json["tasks"] = listOf(12)
                json["companies"] = listOf(35, 36)
                json["hobby"] = "Стрельба из лука, Настолки"
                json["adres"] = "адрес 1"
                json["name1"] = "Тестовый, ясен пень"
                json["surname1"] = "Иванов"
                json["fathername1"] = "Петров"
                json["cat"] = "Маруся"
                json["dog"] = "Ушастый"
                json["parrot"] = "Васька"
                json["cavy"] = "Кто ты?"
                json["hamster"] = "Хомяк"
                json["squirrel"] = "Белая горячка к нам пришла"
                json["phone"] = "333 33 33"
                json["inn"] = "123456789012"
                json["gender"] = "m"
                json["birthday"] = "01.01.1900"
                json["date_start"] = "11.11.2000"
                Given { body(gson.toJson(json)) } When { post("http://users.bugred.ru/tasks/rest/CreateUser") }
            }

            When("Getting info of an existing user")
            {
                json["email"] = "$randStr@example.com"
            }

            Then("Should return valid user with specified fields")
            {
                post {
                    body("name", equalTo(randStr))
                    body("email", equalTo("${randStr.toLowerCase()}@example.com"))
                    body("name1", equalTo("Тестовый, ясен пень"))
                    body("hobby", emptyString())
                    body("surname1", equalTo("Иванов"))
                    body("fathername1", equalTo("Петров"))
                    body("cat", equalTo("Маруся"))
                    body("dog", equalTo("Ушастый"))
                    body("parrot", equalTo("Васька"))
                    body("cavy", equalTo("Кто ты?"))
                    body("hamster", equalTo("Хомяк"))
                    body("squirrel", equalTo("Белая горячка к нам пришла"))
                    body("phone", equalTo("333 33 33"))
                    body("adres", equalTo("адрес 1"))
                    body("gender", equalTo("m"))
                }
            }

            Given("Valid email, but user doesn't exist")
            {
                json["email"] = "$randStr@$randStr.com"
            }

            Then("Should fail - no such user with specified email")
            {
                post {
                    body("type", equalTo("error"))
                    body("message", containsString("Пользователь не найден"))
                }
            }
        }

        Scenario("Erroneous parameters")
        {
            Given("User with invalid email")
            {
                json["email"] = "invalid email"
            }

            Then("Should fail with saying about wrong email format")
            {
                post {
                    body("type", equalTo("error"))
                    body("message", equalTo("Вы ввели неправильный email"))
                }
            }
        }
    }
})

