package com.notes.colornotes.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

fun String.checkIsEmpty(): Boolean =
    isNullOrEmpty() || "" == this || this.equals("null", ignoreCase = true)


fun getDate(date: Date): String {
    return try {
        val simple = SimpleDateFormat("dd MMM yyyy hh:MM", Locale.ENGLISH)
        return simple.format(date)
    } catch (e: Exception) {
        "0"
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun EditText.textToString(): String = this.text.toString()

fun EditText.checkIsEmpty(): Boolean =
    text == null || "" == textToString() || text.toString().equals("null", ignoreCase = true)

fun EditText.isNotValidEmail(): Boolean =
    TextUtils.isEmpty(text) && Patterns.EMAIL_ADDRESS.matcher(text).matches()

fun EditText.isValidPhoneNumber(): Boolean =
    text.matches("^((\\+92)?(0092)?(92)?(0)?)(3)([0-9]{9})\$".toRegex())

fun EditText.validPassword(): Boolean = text.length < 5

fun EditText.showError(error: String) {
    this.error = error
    this.showSoftKeyboard()
}

fun View.showSoftKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}

fun Fragment.copyText(text: String) {
    val clipboard: ClipboardManager? =
        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("Account Number", text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
}