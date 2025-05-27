package com.example.rijekabusapp

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object SavedPreference {
    private const val EMAIL = "email"
    private const val USERNAME = "username"
    private const val PICTURE_URL = "picture_url"
    private const val GIVEN_NAME = "given_name"
    private const val FAMILY_NAME = "family_name"

    private fun getSharedPreference(ctx: Context?): SharedPreferences? {
        return ctx?.let { PreferenceManager.getDefaultSharedPreferences(it) }
    }

    private fun editor(
        context: Context,
        const: String,
        string: String,
    ) {
        getSharedPreference(
            context,
        )?.edit()?.putString(const, string)?.apply()
    }

    fun getEmail(context: Context) =
        getSharedPreference(
            context,
        )?.getString(EMAIL, "")

    fun setEmail(
        context: Context,
        email: String,
    ) {
        editor(
            context,
            EMAIL,
            email,
        )
    }

    fun setUsername(
        context: Context,
        username: String,
    ) {
        editor(
            context,
            USERNAME,
            username,
        )
    }

    fun getUsername(context: Context) =
        getSharedPreference(
            context,
        )?.getString(USERNAME, "")

    fun getPictureUrl(context: Context) =
        getSharedPreference(
            context,
        )?.getString(PICTURE_URL, "")

    fun setPictureUrl(
        context: Context,
        pictureUrl: String,
    ) {
        editor(
            context,
            PICTURE_URL,
            pictureUrl,
        )
    }

    fun getGivenName(context: Context) =
        getSharedPreference(
            context,
        )?.getString(GIVEN_NAME, "")

    fun setGivenName(
        context: Context,
        givenName: String,
    ) {
        editor(
            context,
            GIVEN_NAME,
            givenName,
        )
    }

    fun getFamilyName(context: Context) =
        getSharedPreference(
            context,
        )?.getString(FAMILY_NAME, "")

    fun setFamilyName(
        context: Context,
        familyName: String,
    ) {
        editor(
            context,
            FAMILY_NAME,
            familyName,
        )
    }

    fun clearPreferences(context: Context) {
        val editor = getSharedPreference(context)?.edit()
        editor?.putString(EMAIL, "")
        editor?.putString(USERNAME, "")
        editor?.putString(PICTURE_URL, "")
        editor?.putString(GIVEN_NAME, "")
        editor?.putString(FAMILY_NAME, "")
        editor?.apply()
    }
}
