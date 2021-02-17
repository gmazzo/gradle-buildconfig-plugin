import com.github.gmazzo.example_kts.IntegrationTestBuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildConfigITest {

    @Test
    fun testBuildConfigIntegrationTestProperties() {
        assertEquals("aIntTestValue", IntegrationTestBuildConfig.INTEGRATION_TEST_CONSTANT)
    }

}
