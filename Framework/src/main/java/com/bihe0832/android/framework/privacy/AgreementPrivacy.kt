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
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.textview.ext.addClickActionText
import com.bihe0832.android.lib.utils.intent.IntentUtils

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
            put(context.resources.getString(R.string.privacy_title), View.OnClickListener {
                IntentUtils.openWebPage(context.resources.getString(R.string.privacy_url), context)
            })
            put(context.getString(R.string.agreement_title), View.OnClickListener {
                IntentUtils.openWebPage(context.resources.getString(R.string.agreement_url), context)
            })
        }
    }

    fun setAgreementAndPrivacyText(textview: TextView) {
        textview.addClickActionText(
                textview.context.resources.getString(R.string.privacy_title).toString() + "和"
                        + textview.context.resources.getString(R.string.agreement_title),
                getAgreementAndPrivacyClickActionMap(textview.context)
        )
    }

    fun showPrivacy(activity: Activity, nextAction: () -> Unit) {
        CommonDialog(activity).apply {
            title = "用户协议和隐私政策"
            setHtmlContent(
                    TextFactoryUtils.getCharSequenceWithClickAction(
                            activity.resources.getString(R.string.privacy_agreement_content),
                            getAgreementAndPrivacyClickActionMap(activity)),
                    LinkMovementMethod.getInstance()
            )
            positive = "同意"
            negative = "暂不使用"
            setOnClickBottomListener(object : OnDialogListener {
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

