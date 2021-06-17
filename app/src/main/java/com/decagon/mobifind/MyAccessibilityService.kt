package com.decagon.mobifind

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * This accessibility class is needed as a workaround for receiving the
 * boot_completed intent and nothing more. So, we just implemented the methods that
 * needed to be implemented and leave them empty since we really don't have need for them.
 */
class MyAccessibilityService: AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}