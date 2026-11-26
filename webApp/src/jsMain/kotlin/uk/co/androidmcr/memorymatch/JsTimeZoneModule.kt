package uk.co.androidmcr.memorymatch

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsModule
import kotlin.js.JsNonModule

@JsModule("@js-joda/timezone")
@JsNonModule
external object JsJodaTimeZoneModule

@OptIn(ExperimentalJsExport::class)
@JsExport
val jsJodaTimeZone = JsJodaTimeZoneModule
