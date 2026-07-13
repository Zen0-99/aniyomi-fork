package eu.kanade.presentation.browse.novel.components

import android.util.DisplayMetrics
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil3.compose.AsyncImage
import eu.kanade.presentation.util.rememberResourceBitmapPainter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.extension.novel.model.NovelExtension
import eu.kanade.tachiyomi.extension.novel.util.NovelExtensionLoader
import tachiyomi.core.common.util.lang.withIOContext

private val defaultModifier = Modifier
    .height(40.dp)
    .aspectRatio(1f)

@Composable
fun NovelExtensionIcon(
    extension: NovelExtension,
    modifier: Modifier = Modifier,
    density: Int = DisplayMetrics.DENSITY_DEFAULT,
) {
    when (extension) {
        is NovelExtension.Available -> {
            AsyncImage(
                model = extension.iconUrl,
                contentDescription = null,
                placeholder = ColorPainter(Color(0x1F888888)),
                error = rememberResourceBitmapPainter(id = R.drawable.cover_error),
                modifier = modifier
                    .clip(MaterialTheme.shapes.extraSmall),
            )
        }
        is NovelExtension.Installed -> {
            val icon by extension.getIcon(density)
            when (icon) {
                NovelIconResult.Loading -> Box(modifier = modifier)
                is NovelIconResult.Success -> Image(
                    bitmap = (icon as NovelIconResult.Success<ImageBitmap>).value,
                    contentDescription = null,
                    modifier = modifier,
                )
                NovelIconResult.Error -> Image(
                    bitmap = ImageBitmap.imageResource(id = R.mipmap.ic_default_source),
                    contentDescription = null,
                    modifier = modifier,
                )
            }
        }
        is NovelExtension.Untrusted -> Image(
            imageVector = Icons.Filled.Dangerous,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
            modifier = modifier.then(defaultModifier),
        )
    }
}

@Composable
private fun NovelExtension.Installed.getIcon(density: Int = DisplayMetrics.DENSITY_DEFAULT): State<NovelIconResult<ImageBitmap>> {
    val context = LocalContext.current
    return produceState<NovelIconResult<ImageBitmap>>(initialValue = NovelIconResult.Loading, this) {
        withIOContext {
            value = try {
                val appInfo = NovelExtensionLoader.getNovelExtensionPackageInfoFromPkgName(
                    context,
                    pkgName,
                )!!.applicationInfo!!
                val appResources = context.packageManager.getResourcesForApplication(appInfo)
                NovelIconResult.Success(
                    appResources.getDrawableForDensity(appInfo.icon, density, null)!!
                        .toBitmap()
                        .asImageBitmap(),
                )
            } catch (e: Exception) {
                NovelIconResult.Error
            }
        }
    }
}

sealed class NovelIconResult<out T> {
    data object Loading : NovelIconResult<Nothing>()
    data object Error : NovelIconResult<Nothing>()
    data class Success<out T>(val value: T) : NovelIconResult<T>()
}
