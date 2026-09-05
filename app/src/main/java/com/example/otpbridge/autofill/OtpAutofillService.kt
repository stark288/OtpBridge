package com.example.otpbridge.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.example.otpbridge.R
import com.example.otpbridge.data.OtpStore

class OtpAutofillService : AutofillService() {
    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {
        val code = OtpStore.code(this)
        if (code.isNullOrBlank()) { callback.onSuccess(null); return }
        val ids = mutableListOf<AutofillId>()
        request.fillContexts.lastOrNull()?.structure?.let { collectOtpFields(it, ids) }
        if (ids.isEmpty()) { callback.onSuccess(null); return }
        val presentation = RemoteViews(packageName, R.layout.autofill_otp).apply { setTextViewText(R.id.otp_text, code) }
        val dataset = Dataset.Builder(presentation)
        ids.forEach { dataset.setValue(it, AutofillValue.forText(code), presentation) }
        callback.onSuccess(FillResponse.Builder().addDataset(dataset.build()).build())
    }

    private fun collectOtpFields(structure: AssistStructure, out: MutableList<AutofillId>) {
        for (i in 0 until structure.windowNodeCount) collectNode(structure.getWindowNodeAt(i).rootViewNode, out)
    }
    private fun collectNode(node: AssistStructure.ViewNode, out: MutableList<AutofillId>) {
        val hints = node.autofillHints?.map { it.lowercase() }.orEmpty()
        val hintMatch = hints.any { it.contains("one-time") || it.contains("otp") || it.contains("verification") || it.contains("sms") }
        val input = node.inputType
        val numeric = input != 0 && (input and android.text.InputType.TYPE_CLASS_NUMBER) != 0
        if (node.autofillId != null && (hintMatch || numeric)) out += node.autofillId
        for (i in 0 until node.childCount) collectNode(node.getChildAt(i), out)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) { callback.onSuccess() }
}
