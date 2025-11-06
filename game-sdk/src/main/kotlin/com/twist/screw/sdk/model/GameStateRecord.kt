package com.twist.screw.sdk.model

class GameStateRecord {
    var isUserInitMax: Boolean = false
    var isGameInitStart: Boolean = false
    var isGameInitFinish: Boolean = false
    var isGameStart: Boolean = false
    var isUserFirstVideo: Boolean = false

    override fun toString(): String {
        return "DeviceLogRecord{" +
                "userInitMax=" + isUserInitMax +
                ", gameInitStart=" + isGameInitStart +
                ", gameInitFinish=" + isGameInitFinish +
                ", gameStart=" + isGameStart +
                ", userFirstVideo=" + isUserFirstVideo
        '}'
    }
}

