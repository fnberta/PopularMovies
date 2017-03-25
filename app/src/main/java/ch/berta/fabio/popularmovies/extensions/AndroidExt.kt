package ch.berta.fabio.popularmovies.extensions

import android.database.Cursor
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.widget.*

fun TextView.setTextIfNotEqual(text: CharSequence) {
    if (this.text != text) {
        this.text = text
    }
}

fun EditText.setTextIfNotEqual(text: CharSequence) {
    if (this.text.toString() != text.toString()) {
        this.setText(text)
    }
}

fun ProgressBar.setProgressIfNotEqual(progress: Int) {
    if (this.progress != progress) {
        this.progress = progress
    }
}

fun <T> ArrayAdapter<T>.addInitialData(items: List<T>) {
    if (this.isEmpty && items.isNotEmpty()) {
        this.addAll(items)
        notifyDataSetChanged()
    }
}

fun <T : Adapter> AdapterView<T>.setSelectionIfNotSelected(selectedItemPosition: Int) {
    if (selectedItemPosition != this.selectedItemPosition) {
        this.setSelection(selectedItemPosition)
    }
}

fun Cursor.getStringFromColumn(columnName: String): String {
    return this.getString(this.getColumnIndexOrThrow(columnName))
}

fun Cursor.getIntFromColumn(columnName: String): Int {
    return this.getInt(this.getColumnIndexOrThrow(columnName))
}

fun Cursor.getLongFromColumn(columnName: String): Long {
    return this.getLong(this.getColumnIndexOrThrow(columnName))
}

fun Cursor.getDoubleFromColumn(columnName: String): Double {
    return this.getDouble(this.getColumnIndexOrThrow(columnName))
}

