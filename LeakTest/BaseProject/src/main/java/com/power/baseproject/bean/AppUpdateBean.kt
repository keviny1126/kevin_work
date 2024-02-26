package com.power.baseproject.bean

import java.io.Serializable

class AppUpdateBean : Serializable {
    /**
     * id : 24
     * type : android
     * version : 2.0
     * version_code : 1
     * description : 1.Fixed some bugs in the diagnostic module
     *
     *
     * 2.Some processes have been optimized
     * link : http://netpic.ithinkcar.com/thinktool/com.cnlaunch.uvccamera.apk
     * is_forced : 0
     * created_at : 1582537185
     * updated_at : 1582537185
     * app_name : uvccamera
     * package_name : com.cnlaunch.uvccamera
     */
    var id = 0
    var type: String? = null
    var version: String? = null
    var version_code = 0
    var description: String? = null
    var link: String? = null
    var is_forced = 0
    var created_at: String? = null
    var updated_at: String? = null
    var app_name: String? = null
    var package_name: String? = null
    var file_size: String? = null
    private val is_open = 0
    private val lang_id: String? = null
    private val app_name_id = 0
    override fun toString(): String {
        return "AppUpdateBean{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", version='" + version + '\'' +
                ", version_code=" + version_code +
                ", description='" + description + '\'' +
                ", link='" + link + '\'' +
                ", is_forced=" + is_forced +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", app_name='" + app_name + '\'' +
                ", package_name='" + package_name + '\'' +
                ", file_size='" + file_size + '\'' +
                '}'
    }
}