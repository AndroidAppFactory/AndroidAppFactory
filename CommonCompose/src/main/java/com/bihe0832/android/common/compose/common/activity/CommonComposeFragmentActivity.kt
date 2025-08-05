package com.bihe0832.android.common.compose.common.activity

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.EmptyView

open class CommonComposeFragmentActivity : CommonComposeActivity() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                getFragment().let {
                    if (it == null) {
                        EmptyView(
                            message = "没有获取到Fragment",
                            colorP = MaterialTheme.colorScheme.surface
                        )
                    } else {
                        FragmentContainer(
                            modifier = Modifier.fillMaxWidth(), it
                        )
                    }
                }
            }
        }
    }

    open fun getFragment(): Fragment? {
        return null
    }
}

@Composable
fun FragmentContainer(modifier: Modifier = Modifier, fragment: Fragment) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    AndroidView(factory = { ctx ->
        // 创建 Fragment 容器
        FrameLayout(ctx).apply {
            id = ViewCompat.generateViewId()
        }
    }, update = { view ->
        activity?.let { act ->
            // 创建并添加 Fragment
            val transaction = act.supportFragmentManager.beginTransaction()
            transaction.replace(view.id, fragment)
            transaction.commit()
        }
    }, modifier = modifier
    )
}
