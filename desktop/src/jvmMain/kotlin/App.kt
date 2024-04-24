import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.netdiscovery.compose.imageloader.core.ImageLoaderFactory
import cn.netdiscovery.compose.imageloader.core.ImageCallback
import cn.netdiscovery.compose.imageloader.imageFile
import cn.netdiscovery.compose.imageloader.imageUrl
import cn.netdiscovery.compose.imageloader.transform.filter.*
import java.io.File

@Composable
fun defaultPlaceHolderView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
    }
}

@Composable
fun defaultErrorView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("error view")
    }
}

@Composable
fun App() {

    LaunchedEffect(Unit) {
        // configure the image loader
        ImageLoaderFactory.configuration(1024 * 1024 * 100L, 1024 * 1024 * 50L, File(System.getProperty("user.dir")))
    }

    MaterialTheme(colors = lightColors().copy(primary = Color(0xFFF5730A))) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                imageUrl(imageUrl,
                    transformations = transformations,
                    imageCallback = ImageCallback(placeHolderView = {
                        defaultPlaceHolderView()
                    }, errorView = {
                        defaultErrorView()
                    }) {
                        Image(modifier = Modifier.size(800.dp), painter = it, contentDescription = "")
                    })
            }
        }
    }
}
