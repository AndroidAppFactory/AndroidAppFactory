package com.bihe0832.android.common.compose.ui.activity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.R
import com.bihe0832.android.common.compose.base.BaseComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/3.
 * Description: Description
 *
 */
@Composable
fun ActivityThemeView(
    themeType: ColorScheme,
    currentLanguage: Locale,
    contentRender: RenderState
) {
    MaterialTheme(themeType) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setStatusBarColor(themeType.primary)
            systemUiController.setNavigationBarColor(themeType.surface)
        }
        Surface {
            contentRender.Content(currentLanguage)
        }
    }
}

@Composable
fun ActivityRootView(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(topBar = {
        topBar()
    }, bottomBar = {
        bottomBar()
    }, modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityToolBarView(
    navigationIcon: ImageVector,
    navigationOnClick: () -> Unit,
    title: String,
    textSize: TextUnit,
    isCenter: Boolean,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    if (isCenter) {
        ActivityRootView(topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = textSize
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigationOnClick()
                    }) {
                        Icon(
                            imageVector = navigationIcon, contentDescription = null
                        )
                    }
                },
                actions = {
                    actions()
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }, bottomBar = {

        }, content = {
            content()
        })
    } else {
        ActivityRootView(topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = textSize
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigationOnClick()
                    }) {
                        Icon(
                            imageVector = navigationIcon, contentDescription = null
                        )
                    }
                },
                actions = {
                    actions()
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }, bottomBar = {

        }, content = {
            content()
        })
    }


}

@Composable
fun ActivityBottomBarView(bottomBar: @Composable () -> Unit, content: @Composable () -> Unit) {
    ActivityRootView(topBar = {}, bottomBar = {
        bottomBar()
    }, content = {
        content()
    })
}

@Composable
fun BaseComposeActivity.CommonActivityToolbarView(
    title: String, content: @Composable () -> Unit
) {
    ActivityToolBarView(
        navigationIcon = ImageVector.vectorResource(R.drawable.icon_left_go),
        navigationOnClick = {
            onBackPressed()
        },
        title = title,
        textSize = 18.sp,
        isCenter = true,
        actions = {},
        content = { content() })
}


@Preview
@Composable
fun Preview() {
    ActivityToolBarView(navigationIcon = ImageVector.vectorResource(R.drawable.icon_left_go),
        navigationOnClick = {

        },
        title = "sdfsfdfsd",
        18.sp,
        true,
        actions = {},
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "fsdf", modifier = Modifier.align(Alignment.TopStart) // 左上角
                )
                Text(
                    text = "fsdf323", modifier = Modifier.align(Alignment.BottomEnd)   // 右上角
                )
            }
        })

//    ActivityBottomBarView(bottomBar = {
//        Text(
//            text = "fsdf", modifier = Modifier
//                .background(Color.Red)
//                .fillMaxWidth()
//        )
//    }, content = {
//
//    })

}