package com.bihe0832.android.common.splash

import android.app.Activity
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.textview.ext.addClickActionText
import com.bihe0832.android.lib.utils.intent.IntentUtils

object AgreementPrivacy {

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
                    Config.writeConfig(Constants.CONFIG_KEY_PRIVACY_AGREEMENT_ENABLED, true)
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

