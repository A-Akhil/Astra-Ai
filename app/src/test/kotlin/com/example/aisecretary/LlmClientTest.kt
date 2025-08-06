import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for [LlamaClient].
 * This class checks if LlamaClient responds correctly to user inputs.
 */
class LlamaClientTest {

    // Fake version of OllamaService for testing
    @Mock
    private lateinit var mockOllamaService: OllamaService

    // The class we are testing
    private lateinit var llamaClient: LlamaClient

    /**
     * Runs before each test.
     * Sets up the mock objects and initializes LlamaClient with the fake service.
     */
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        llamaClient = LlamaClient(mockOllamaService)
    }

    /**
     * Test if LlamaClient returns the expected response when given a valid input.
     */
    @Test
    fun testGetResponse() {
        val userInput = "Hello, how can you assist me?"
        val expectedResponse = "I can help you with various tasks."

        // Tells the fake service what to return
        whenever(mockOllamaService.sendRequest(userInput)).thenReturn(expectedResponse)

        val actualResponse = llamaClient.getResponse(userInput)

        // Check if the response matches what we expect
        assertEquals(expectedResponse, actualResponse)
    }

    /**
     * Test how LlamaClient handles empty input.
     */
    @Test
    fun testGetResponseWithEmptyInput() {
        val userInput = ""
        val expectedResponse = "Please provide a valid input."

        val actualResponse = llamaClient.getResponse(userInput)

        // Check if the fallback message is correct
        assertEquals(expectedResponse, actualResponse)
    }
}