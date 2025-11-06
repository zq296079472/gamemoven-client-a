package com.twist.screw.sdk.ads.max

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxNetworkResponseInfo
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.ads.AdType
import kotlin.math.abs

class BannerAd(activity: Activity) : BaseMaxAd(activity), MaxAdViewAdListener {
    override val tag: String = BannerAd::class.java.simpleName
    private lateinit var maxBanner: MaxAdView
    private val id: Int = hashCode()
    override var isLoading = false
    override var loadStartTime = 0L
    override fun getUnitId(): String {
        return GameSDK.getConfig().applovinBannerUnit
    }

    override fun getAdType(): AdType {
        return AdType.banner
    }

    override fun load() {
        maxBanner = MaxAdView(getUnitId(), activity).let {
            it.setListener(this)
            it.setRevenueListener(this)
            it
        }
        maxBanner.loadAd()
    }

    override fun show() {
        if (activity.findViewById<ViewGroup>(id) == null) {
            val isTablet = AppLovinSdkUtils.isTablet(activity) // Available on Android SDK 9.6.2+
            val heightPx = AppLovinSdkUtils.dpToPx(activity, if (isTablet) 90 else 50)
            maxBanner.setLayoutParams(
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    heightPx
                )
            )
            val horizontalCenterOffset = 0
            val anchorType = Gravity.BOTTOM
            val anchorOffset = 0
            val content = LinearLayout(activity)
            content.id = id
            content.orientation = LinearLayout.VERTICAL
            content.gravity = anchorType
            content.addView(maxBanner)
            val scale = activity.resources.displayMetrics.density
            val left =
                if (horizontalCenterOffset > 0) (horizontalCenterOffset * scale).toInt() else 0
            val right =
                if (horizontalCenterOffset < 0) (abs(horizontalCenterOffset) * scale).toInt() else 0
            content.setPadding(left, 0, right, (anchorOffset * scale).toInt())
            activity.addContentView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            maxBanner.visibility = View.VISIBLE
            maxBanner.startAutoRefresh()
        } else {
            maxBanner.startAutoRefresh()
            maxBanner.visibility = View.VISIBLE
        }
    }

    override fun destroy() {
        maxBanner.destroy()
        val contentView = activity.findViewById<ViewGroup>(id)
        if (contentView == null || contentView.parent !is ViewGroup) return
        val contentParent = contentView.parent as ViewGroup
        contentParent.removeView(contentView)
    }

    override fun isReady(): Boolean {
        TODO("Not yet implemented")
    }

    fun hide() {
        if (activity.findViewById<ViewGroup>(id) != null) {
            maxBanner.setExtraParameter("allow_pause_auto_refresh_immediately", "true")
            maxBanner.stopAutoRefresh()
            maxBanner.visibility = View.GONE
        }
    }

    override fun onAdLoaded(ad: MaxAd) {
        try {
            ad.waterfall?.let { waterfall ->
                logWarn("Waterfall Name: " + waterfall.name + " and Test Name: " + waterfall.testName)
                logWarn("Waterfall latency was: " + waterfall.latencyMillis + " milliseconds")

                for (networkResponse: MaxNetworkResponseInfo in waterfall.networkResponses) {
                    logWarn(
                        "Network -> " + networkResponse.mediatedNetwork +
                                "...latency: " + networkResponse.latencyMillis +
                                "...credentials: " + networkResponse.credentials + " milliseconds" +
                                "...error: " + networkResponse.error
                    )
                }
                return@let waterfall.name
            } ?: "unknown"
        } catch (throwable: Throwable) {
            logError("onAdLoadFailed error: ${throwable.message}")
        }
    }

    override fun onAdDisplayed(p0: MaxAd) {
    }

    override fun onAdHidden(p0: MaxAd) {
    }

    override fun onAdClicked(p0: MaxAd) {
    }

    override fun onAdLoadFailed(ad: String, err: MaxError) {
        try {
            err.waterfall?.let { waterfall ->
                logWarn("Waterfall Name: " + waterfall.name + " and Test Name: " + waterfall.testName)
                logWarn("Waterfall latency was: " + waterfall.latencyMillis + " milliseconds")

                for (networkResponse: MaxNetworkResponseInfo in waterfall.networkResponses) {
                    logWarn(
                        "Network -> " + networkResponse.mediatedNetwork +
                                "...latency: " + networkResponse.latencyMillis +
                                "...credentials: " + networkResponse.credentials + " milliseconds" +
                                "...error: " + networkResponse.error
                    )
                }
                return@let waterfall.name
            } ?: "unknown"
        } catch (throwable: Throwable) {
            logError("onAdLoadFailed error: ${throwable.message}")
        }
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
    }

    override fun onAdExpanded(p0: MaxAd) {
    }

    override fun onAdCollapsed(p0: MaxAd) {
    }

    private fun logWarn(message: String) {
        Log.w("BannerAd", message)
    }

    private fun logError(message: String) {
        Log.e("BannerAd", message)
    }
}
