package com.bihe0832.android.framework.privacy

import android.app.Activity
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.textview.ext.addClickActionText
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.model.res.R as ModelResR

object AgreementPrivacy {

    // 是否同意隐私协议
    const val CONFIG_KEY_PRIVACY_AGREEMENT_ENABLED = "KEY_PRIVACY_AGREEMENT_ENABLED"

    fun hasAgreedPrivacy(): Boolean {
        return Config.isSwitchEnabled(CONFIG_KEY_PRIVACY_AGREEMENT_ENABLED, false)
    }

    fun doAgreedPrivacy() {
        Config.writeConfig(CONFIG_KEY_PRIVACY_AGREEMENT_ENABLED, true)
    }

    fun resetPrivacy() {
        Config.writeConfig(CONFIG_KEY_PRIVACY_AGREEMENT_ENABLED, false)
    }

    fun getAgreementAndPrivacyClickActionMap(context: Context): HashMap<String, View.OnClickListener> {
        return HashMap<String, View.OnClickListener>().apply {
            put(
                ThemeResourcesManager.getString(ModelResR.string.privacy_title)!!,
                View.OnClickListener {
                    IntentUtils.openWebPage(context,ThemeResourcesManager.getString(R.string.privacy_url))
                },
            )
            put(
                ThemeResourcesManager.getString(ModelResR.string.agreement_title)!!,
                View.OnClickListener {
                    IntentUtils.openWebPage(context,ThemeResourcesManager.getString(R.string.agreement_url))
                },
            )
        }
    }

    fun setAgreementAndPrivacyText(textview: TextView) {
        textview.addClickActionText(
            ThemeResourcesManager.getString(ModelResR.string.privacy_title) + "和" +
                ThemeResourcesManager.getString(ModelResR.string.agreement_title),
            getAgreementAndPrivacyClickActionMap(textview.context),
        )
    }

    fun showPrivacy(activity: Activity, nextAction: () -> Unit) {
        showPrivacy(activity, getAgreementAndPrivacyClickActionMap(activity), nextAction)
    }

    fun showPrivacy(activity: Activity, linkList: HashMap<String, View.OnClickListener>, nextAction: () -> Unit) {
        showPrivacy(
            activity,
            ThemeResourcesManager.getString(ModelResR.string.dialog_title_privacy_and_agreement)!!,
            ThemeResourcesManager.getString(ModelResR.string.privacy_agreement_content)!!,
            linkList,
            nextAction,
        )
    }

    fun showPrivacy(
        activity: Activity,
        title: String,
        content: String,
        linkList: HashMap<String, View.OnClickListener>,
        nextAction: () -> Unit,
    ) {
        CommonDialog(activity).apply {
            this.title = title
            setHtmlContent(
                TextFactoryUtils.getCharSequenceWithClickAction(content, linkList),
                LinkMovementMethod.getInstance(),
            )
            positive = ThemeResourcesManager.getString(ModelResR.string.agreement_positive)
            negative = ThemeResourcesManager.getString(ModelResR.string.agreement_negative)
            setOnClickBottomListener(object :
                OnDialogListener {
                override fun onPositiveClick() {
                    dismiss()
                    doAgreedPrivacy()
                    nextAction()
                }

                override fun onNegativeClick() {
                    dismiss()
                    ZixieContext.exitAPP()
                }

                override fun onCancel() {
                    onNegativeClick()
                }
            })
        }.let {
            it.show()
        }
    }
}
