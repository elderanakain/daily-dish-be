
import io.krugosvet.dailydish.main
import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.dto.AddMeal
import io.krugosvet.dailydish.repository.dto.Meal
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.test.assertEquals

class MealTests :
  KoinComponent {

  @Test
  fun whenRequestMeals_thenValidCollectionIsReturned(): Unit = withTestApplication({ main() }) {

    // when

    val request = handleRequest(HttpMethod.Get, "/meal")

    // then

    assertEquals(false, request.response.content!!.isBlank())
    assertEquals(HttpStatusCode.OK, request.response.status())
  }

  @Test
  fun whenRequestMeal_thenValidMealIsReturned(): Unit = withTestApplication({ main() }) {

    // given

    val validMeal = runBlocking { get<MealRepository>().meals.first() }

    // when

    val request = handleRequest(HttpMethod.Get, "/meal/${validMeal.id}")

    // then

    assertEquals(Json.encodeToString(validMeal), request.response.content)
    assertEquals(HttpStatusCode.OK, request.response.status())
  }

  @Test
  fun whenRequestMeal_thenIdIsNotFound(): Unit = withTestApplication({ main() }) {

    // when

    val request = handleRequest(HttpMethod.Get, "/meal/-1")

    // then

    assertEquals(null, request.response.content)
    assertEquals(HttpStatusCode.BadRequest, request.response.status())
  }

  @Test
  fun whenDeleteMeal_thenValidMealIsDeleted(): Unit = withTestApplication({ main() }) {

    // given

    val validMeal = runBlocking { get<MealRepository>().meals.first() }

    // when

    val request = handleRequest(HttpMethod.Delete, "/meal/${validMeal.id}")

    // then

    assertEquals(HttpStatusCode.Accepted, request.response.status())
  }

  @Test
  fun whenAddMeal_thenValidMealIsAdded(): Unit = withTestApplication({ main() }) {

    // given

    val mockTitle = "title"
    val mockDescription = "description"
    val mockImage = "http://127.0.0.1:8081/static/image"
    val mockLastCookingDate = "2020-01-01"

    val addMeal = AddMeal(mockTitle, mockDescription, mockImage, mockLastCookingDate)

    // when

    val request = handleRequest(HttpMethod.Post, "/meal") {
      addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      setBody(Json.encodeToString(addMeal))
    }

    // then

    assertEquals(HttpStatusCode.Created, request.response.status())

    val createdMeal = runBlocking { get<MealRepository>().get(request.response.content!!) }

    val validMeal = Meal(createdMeal.id, mockTitle, mockDescription, mockImage, mockLastCookingDate)

    assertEquals(validMeal, createdMeal)
  }

  @After
  fun tearDown() = withTestApplication({ main() }) {
    get<DatabaseHelper>().reset()
  }
}
