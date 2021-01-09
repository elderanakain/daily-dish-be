
import io.krugosvet.dailydish.main
import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.dto.AddMeal
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.junit.After
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.File
import kotlin.test.assertEquals


class MealTests :
  KoinComponent {

  private val mealRepository: MealRepository by inject()

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

    val validMeal = runBlocking { mealRepository.meals.first() }

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

    val validMeal = runBlocking { mealRepository.meals.first() }

    // when

    val request = handleRequest(HttpMethod.Delete, "/meal/${validMeal.id}")

    // then

    assertEquals(HttpStatusCode.Accepted, request.response.status())
  }

  @Test
  fun whenAddMealWithNewImage_thenValidMealIsAdded(): Unit = withTestApplication({ main() }) {

    // given

    val mockBoundaryHeaderValue = "boundary"

    val mockTitle = "title"
    val mockDescription = "description"
    val mockLastCookingDate = "2020-01-01"

    val addMeal = AddMeal(mockTitle, mockDescription, mockLastCookingDate, image = null)

    val mockImage = File(application.environment.classLoader.getResource("mock_image.jpg")!!.toURI())

    val formData = formData {
      append("meal", Json.encodeToString(addMeal))
      append("meal_image", "", ContentType.Image.PNG, mockImage.length()) {
        writeFully(mockImage.readBytes())
      }
    }

    // when

    val createMealRequest = handleRequest(HttpMethod.Post, "/meal") {
      addHeader(HttpHeaders.ContentType, "${ContentType.MultiPart.FormData}; boundary=$mockBoundaryHeaderValue")

      setBody(mockBoundaryHeaderValue, formData)
    }

    // then

    assertEquals(HttpStatusCode.Created, createMealRequest.response.status())

    val createdMeal = runBlocking { mealRepository.get(createMealRequest.response.content!!) }

    assertThat(createdMeal.title, `is`(mockTitle))
    assertThat(createdMeal.description, `is`(mockDescription))
    assertThat(createdMeal.image, matchesPattern("https://daily-dish-be.com.herokuapp.com/static/.*".toPattern()))
    assertThat(createdMeal.lastCookingDate, `is`(mockLastCookingDate))

    // when

    val mealImageId = "/".toPattern().split(createdMeal.image).last()

    val getImageRequest = handleRequest(HttpMethod.Get, "/static/$mealImageId")

    // then

    assertThat(getImageRequest.response.status(), `is`(HttpStatusCode.OK))

    // when

    val deleteRequest = handleRequest(HttpMethod.Delete, "/meal/${createdMeal.id}")

    assertThat(deleteRequest.response.status(), `is`(HttpStatusCode.Accepted))
    assertThat(File("resources/static/$mealImageId").exists(), `is`(false))
  }

  @Test
  fun whenUpdateMeal_thenChangesArePropagated(): Unit = withTestApplication({ main() }) {

    // given

    val mockTitle = "newTitle"

    val existingMeal = runBlocking { mealRepository.meals.first() }
    val editedMeal = existingMeal.copy(title = mockTitle)

    // when

    val request = handleRequest(HttpMethod.Put, "/meal") {
      addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      setBody(Json.encodeToString(editedMeal))
    }

    // then

    val savedMeal = runBlocking { mealRepository.get(editedMeal.id) }

    assertEquals(HttpStatusCode.Accepted, request.response.status())
    assertEquals(editedMeal, savedMeal)
  }

  @After
  fun tearDown() = withTestApplication({ main() }) {
    get<DatabaseHelper>().reset()
  }
}
