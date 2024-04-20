package ir.erfansn.nsmavpn.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import com.android.ide.common.rendering.api.SessionParams
import ir.erfansn.nsmavpn.R
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

const val FakeImageUrl = "https://example.com/image.png"

@OptIn(ExperimentalCoilApi::class)
class UserAvatarScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Before
    fun setUp() {
        val engine = FakeImageLoaderEngine.Builder()
            .intercept(
                FakeImageUrl,
                paparazzi.context.getDrawable(R.drawable.ic_launcher_foreground)!!
            )
            .build()
        val imageLoader = ImageLoader.Builder(paparazzi.context)
            .components { add(engine) }
            .build()
        Coil.setImageLoader(imageLoader)
    }

    @Test
    fun userAvatar_imageLoadingAnimation() {
        val hostView = ComposeView(paparazzi.context)
        hostView.setContent {
            Box(
                modifier = Modifier
                    .background(color = androidx.compose.ui.graphics.Color.White)
                    .padding(8.dp)
            ) {
                var avatarUrl by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    delay(1.seconds)
                    avatarUrl = FakeImageUrl
                }
                UserAvatar(
                    modifier = Modifier.size(64.dp),
                    avatarUrl = avatarUrl,
                    borderWidth = 2.dp,
                    contentDescription = ""
                )
            }
        }
        paparazzi.gif(view = hostView, end = 3000)
    }
}