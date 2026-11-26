@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package uk.co.androidmcr.memorymatch

import kotlin.js.JsModule

@JsModule("@js-joda/timezone")
external object JsJodaTimeZoneModule

@Suppress("unused")
private val jsJodaTimeZone = JsJodaTimeZoneModule
