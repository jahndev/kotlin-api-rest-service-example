# Payment Solutions Kotlin Code Style

This document should be used as a reference when writing code for **Payment Solutions'** projects
based on Kotlin. It contains a set of recommendations to make easier to navigate and understand the
codebase of a project and, at the same time, to improve code review. However, common sense and best
practices should always be used, making each point open to further discussions and improvements.

The official [Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html)
is the main reference, with this document extending it. Some points are inspired from the list of
open issues in [Kotlin Style Guide](https://github.com/yole/kotlin-style-guide). Points with a
**issue #num** are links to the issue where the recommendation was first discussed.

## Naming and General Style

- **Variable** names are `camelCase`.
- **Type** names are `CapitalCamelCase`.
- **Functions**, **methods** and **properties** names are `camelCase`.
- **Constants** and **Enum** entries are `SCREAMING_CASE`.
- When naming things, treat acronyms as full words, `XmlHttpRequest` instead of `XMLHTTPRequest`.
- **Source File** names are `CapitalCamelCase.extension`.
- 4 spaces indentation.
- Line length should be no more than 120 characters.

## Syntax

- **Named Parameters** should be used in the following cases:
    - if the number of parameters required for calling a function or instantiate a class are
    **greater than or equal to 3**, with each parameter on its own line.
    - if two or more parameters are of the same type.

```kotlin
// when the number of parameters is less than 3
buildResponse(statusCode)
val inputData = Data(input1, input2)
val httpResponse = buildDefaultHttpResponse(
    statusCode, applicationType
)

// when the number of parameters is less than 3 but all parameters have the same type
val user = User(firstName = "firstName", lastName = "lastName")

// when the number of parameters is equal or greater than 3
buildResponse(
    statusCode = code,
    applicationType = type,
    body = entity
)
val user = UserData(
    firstName = name,
    lastName = surname,
    address = street,
    cityName = city
)
```

- The `when` expression should always be used with the goal to be as readable as possible and not as a replacement for
`if...else...` blocks. For this reason, each `when` branch should prefer expression body over block body.

```kotlin
when(statusCode) {
    is Success -> return true
    is Redirect -> redirectUser(statusCode)
    is ClientError, ServerError  -> throw HttpError()
    else -> return false
}
```

- The `Class Layout` follows the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html#class-layout),
with rule of thumb:
> ... put related stuff together, so that someone reading the class from top to bottom would be
able to follow the logic of what's happening.

## Lambdas

- Avoid parentheses when possible `list.filter { it > 10 }`.
- Use `it` if a lambda is short and does not contain nested lambdas, otherwise a meaningful name
should be used.

```kotlin
webServices.getUsersPerCompany()
    .flatMap { it }
    .map { user ->
        when {
            user.isAdmin() -> Admin(user)
            user.isManager() -> Manager(user)
            else -> Engineer(user)
        }
    }
```
- Unused parameters should be replaced with underscore, `onTriple { _, _, third -> println(third) }`.
- A variable should be used as the return value of a non-immediate lambda.

```kotlin
listOfEmployers.map { employer ->
    val employees = getEmployeesFromEmployer(employer)
    val companies = getCompaniesFromEmployer(employer)
    val employeesForCompany = mapEmployeesToCompanies(employees, companies)
    employeesForCompany
}
```

## Testing

When writing tests, these rules should always be followed:

- The name of each test should be in the format `` `purpose of the test` ``.
- The name of each test should follow the convention:
    - what is being tested;
    - under which conditions;
    - expected outcome.
- Except for motivated reasons, the assertion of the test should always be the last line of the
method.

```kotlin
fun `response has status code 200`() {
    val response = buildSuccessResponse()
    assertThat(respose.status, equalTo(200))
}
```

- `Assert.assertThat(actual, CoreMatchers.equalTo(expected))` should be used in tests.
- In the rare cases where multiple assertions on the same object are needed in a test, one of the
following formats should be used.

```kotlin
// (1)
fun `invalid input response is mapped to 422 error code`() {
    val response = buildInvalidInputResponse()
    with(response) {
        assertThat(status, equalTo(422))
        assertThat(this, hasValidationErrorMessage("Invalid JSON object"))
    }
}

// (2)
fun `resource returns 422 error code for invalid object`() {
    resource.target(resourcePath)
            .request()
            .put(json(objectToTest))
            .let {
                assertThat(status, equalTo(422))
                assertThat(this, hasValidationErrorMessage("Invalid JSON object"))
            }
}
```
