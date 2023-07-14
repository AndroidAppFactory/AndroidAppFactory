package com.bihe0832.android.services.google

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/6/16.
 * Description: Description
 *
 */
class AAFGoogleOAuth(val activity: Activity, val serverClientId: String) {

    private val mGoogleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(activity!!, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(serverClientId).requestEmail().build())
    }

    fun startLogin(requesetCode: Int) {
        val signInIntent = mGoogleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, requesetCode)
    }

    fun parseIntent(data: Intent?): GoogleSignInAccount? {
        try {
            GoogleSignIn.getSignedInAccountFromIntent(data).let {
                return it.getResult(ApiException::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getLastUserInfo(): GoogleSignInAccount? {
        try {
            return GoogleSignIn.getLastSignedInAccount(activity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun refreshToken(completedTask: OnCompleteListener<GoogleSignInAccount>?) {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(activity) { completedTask?.onComplete(it) }
    }

    fun revokeAccess(completedTask: OnCompleteListener<Void>?) {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(activity) {
            completedTask?.onComplete(it)
        }
    }

    fun logout(completedTask: OnCompleteListener<Void>?) {
        mGoogleSignInClient.signOut().addOnCompleteListener(activity) {
            completedTask?.onComplete(it)
        }
    }


}