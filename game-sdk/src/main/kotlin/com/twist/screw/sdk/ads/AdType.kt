package com.twist.screw.sdk.ads

@Suppress("EnumEntryName")
enum class AdType {
    item_reward,
    cash_reward,
    interstitial,
    banner;

    companion object {
        fun fromName(name: String?): AdType? {
            return if (name == null) null else valueOf(name.trim())
        }
    }
}