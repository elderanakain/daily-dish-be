
import io.krugosvet.dailydish.main
import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.dto.AddMeal
import io.krugosvet.dailydish.repository.dto.Meal
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
import org.koin.core.component.inject
import java.io.File
import kotlin.test.assertEquals

private const val MOCK_BOUNDARY = "boundary"
private val MOCK_MULTIPART_HEADER = "${ContentType.MultiPart.FormData}; boundary=$MOCK_BOUNDARY"

class MealTests :
  KoinComponent {

  private val mealRepository: MealRepository by inject()
  private val databaseHelper: DatabaseHelper by inject()

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
      addHeader(HttpHeaders.ContentType, MOCK_MULTIPART_HEADER)

      setBody(MOCK_BOUNDARY, formData)
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

    deleteMeal(createdMeal)
  }

  @Test
  fun whenUpdateWithImageMeal_thenChangesArePropagated(): Unit = withTestApplication({ main() }) {

    // given

    val mockTitle = "newTitle"

    val mockImage = File(application.environment.classLoader.getResource("mock_image.jpg")!!.toURI())

    val existingMeal = runBlocking { mealRepository.meals.first() }
    val editedMeal = existingMeal.copy(title = mockTitle)

    val formData = formData {
      append("meal", Json.encodeToString(editedMeal))
      append("meal_image", "", ContentType.Image.PNG, mockImage.length()) {
        writeFully(mockImage.readBytes())
      }
    }

    // when

    val request = handleRequest(HttpMethod.Put, "/meal") {
      addHeader(HttpHeaders.ContentType, MOCK_MULTIPART_HEADER)

      setBody(MOCK_BOUNDARY, formData)
    }

    // then

    val savedMeal = runBlocking { mealRepository.get(editedMeal.id) }

    assertEquals(HttpStatusCode.Accepted, request.response.status())
    assertEquals(editedMeal.copy(image = savedMeal.image), savedMeal)

    deleteMeal(savedMeal)
  }

  @Test
  fun whenUpdateMeal_thenChangesArePropagated(): Unit = withTestApplication({ main() }) {

    // given

    val mockTitle = "newTitle"

    val existingMeal = runBlocking { mealRepository.meals.first() }
    val editedMeal = existingMeal.copy(title = mockTitle)

    val formData = formData {
      append("meal", Json.encodeToString(editedMeal))
    }

    // when

    val request = handleRequest(HttpMethod.Put, "/meal") {
      addHeader(HttpHeaders.ContentType, MOCK_MULTIPART_HEADER)

      setBody(MOCK_BOUNDARY, formData)
    }

    // then

    val savedMeal = runBlocking { mealRepository.get(editedMeal.id) }

    assertEquals(HttpStatusCode.Accepted, request.response.status())
    assertEquals(editedMeal, savedMeal)
  }

  @Test
  fun whenUpdateWithDeletedImageMeal_thenChangesArePropagated(): Unit = withTestApplication({ main() }) {

    // given

    val mockImage = File(application.environment.classLoader.getResource("mock_image.jpg")!!.toURI())

    var existingMeal = runBlocking { mealRepository.meals.first() }

    var formData = formData {
      append("meal", Json.encodeToString(existingMeal))
      append("meal_image", "", ContentType.Image.PNG, mockImage.length()) {
        writeFully(mockImage.readBytes())
      }
    }

    // when

    var request = handleRequest(HttpMethod.Put, "/meal") {
      addHeader(HttpHeaders.ContentType, MOCK_MULTIPART_HEADER)

      setBody(MOCK_BOUNDARY, formData)
    }

    // then

    var savedMeal = runBlocking { mealRepository.get(existingMeal.id) }

    assertEquals(HttpStatusCode.Accepted, request.response.status())
    assertEquals(existingMeal.copy(image = savedMeal.image), savedMeal)

    // given

    val editedMeal = savedMeal.copy(image = null)

    formData = formData {
      append("meal", Json.encodeToString(editedMeal))
    }

    // when

    request = handleRequest(HttpMethod.Put, "/meal") {
      addHeader(HttpHeaders.ContentType, MOCK_MULTIPART_HEADER)

      setBody(MOCK_BOUNDARY, formData)
    }

    // then

    savedMeal = runBlocking { mealRepository.get(editedMeal.id) }

    assertEquals(HttpStatusCode.Accepted, request.response.status())
    assertEquals(editedMeal, savedMeal)

    assertThat(getImageFile(existingMeal).exists(), `is`(false))
  }

  @After
  fun tearDown() = withTestApplication({ main() }) {
    databaseHelper.reset()
  }

  private fun TestApplicationEngine.deleteMeal(meal: Meal) {

    // when

    val deleteRequest = handleRequest(HttpMethod.Delete, "/meal/${meal.id}")

    // then

    assertThat(deleteRequest.response.status(), `is`(HttpStatusCode.Accepted))
    assertThat(getImageFile(meal).exists(), `is`(false))
  }

  private fun getImageFile(meal: Meal): File {
    val mealImageId = "/".toPattern().split(meal.image).last()

    return File("resources/static/$mealImageId")
  }
}
