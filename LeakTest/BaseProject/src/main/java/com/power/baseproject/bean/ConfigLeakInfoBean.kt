package com.power.baseproject.bean

import com.power.baseproject.utils.ConstantsUtils
import com.power.baseproject.utils.Tools
import java.io.Serializable

data class ConfigLeakInfoBean(
    var workpieceNo: String = "A001",//工件号
    var workpieceVolume: Float = 50.0f,//工件容积 L
    var testPressure: Float = 30.0f,//测试压力
    var upperPressureLimit: Float = 35.0f,//压力上限
    var lowerPressureLimit: Float = 0.0f,//压力下限
    var pressureUnit: String? = ConstantsUtils.KPA,
    var leakageAlarm: Float = 20f,//泄露报警
    var leakageUnit: String? = ConstantsUtils.PA,
    var inflationTime: Int = 15,//充气时间 S
    var stabilizationTime: Int = 10,//稳定时间 S
    var detectionTime: Int = 15,//检测时间 S
    var exhaustTime: Int = 10,//排气时间 S
    var testMode: Int = 0//1:高压  0:低压
) : Serializable {
    companion object {
        const val KPA = ConstantsUtils.KPA
        const val PA = ConstantsUtils.PA
        const val PSI = ConstantsUtils.PSI
    }

    fun switchUnits(newPressureUnit: String?, newLeakageUnit: String?): ConfigLeakInfoBean {
        val configLeakInfoBean = ConfigLeakInfoBean(
            workpieceNo,
            workpieceVolume,
            testPressure,
            upperPressureLimit,
            lowerPressureLimit,
            pressureUnit,
            leakageAlarm,
            leakageUnit,
            inflationTime,
            stabilizationTime,
            detectionTime,
            exhaustTime,
            testMode
        )

        newPressureUnit?.let {
            when (it) {
                KPA -> {
                    configLeakInfoBean.testPressure =
                        Tools.convertToKpa(testPressure, pressureUnit ?: ConstantsUtils.KPA)
                    configLeakInfoBean.upperPressureLimit =
                        Tools.convertToKpa(upperPressureLimit, pressureUnit ?: ConstantsUtils.KPA)
                    configLeakInfoBean.lowerPressureLimit =
                        Tools.convertToKpa(lowerPressureLimit, pressureUnit ?: ConstantsUtils.KPA)
                    configLeakInfoBean.pressureUnit = KPA
                }

                PSI -> {
                    configLeakInfoBean.testPressure =
                        Tools.convertToPsi(testPressure, pressureUnit ?: ConstantsUtils.KPA)
                    configLeakInfoBean.upperPressureLimit =
                        Tools.convertToPsi(upperPressureLimit, pressureUnit ?: ConstantsUtils.KPA)
                    configLeakInfoBean.lowerPressureLimit =
                        Tools.convertToPsi(lowerPressureLimit, pressureUnit ?: ConstantsUtils.KPA)
                    configLeakInfoBean.pressureUnit = PSI
                }

                PA -> {
                    configLeakInfoBean.testPressure =
                        Tools.convertToPa(testPressure, pressureUnit ?: ConstantsUtils.KPA).toInt().toFloat()
                    configLeakInfoBean.upperPressureLimit =
                        Tools.convertToPa(upperPressureLimit, pressureUnit ?: ConstantsUtils.KPA).toInt().toFloat()
                    configLeakInfoBean.lowerPressureLimit =
                        Tools.convertToPa(lowerPressureLimit, pressureUnit ?: ConstantsUtils.KPA).toInt().toFloat()
                    configLeakInfoBean.pressureUnit = PA
                }
            }
        }

        newLeakageUnit?.let {
            when (it) {
                KPA -> {
                    configLeakInfoBean.leakageAlarm =
                        Tools.convertToKpa(leakageAlarm, leakageUnit ?: ConstantsUtils.PA)
                    configLeakInfoBean.leakageUnit = KPA
                }

                PSI -> {
                    configLeakInfoBean.leakageAlarm =
                        Tools.convertToPsi(leakageAlarm, leakageUnit ?: ConstantsUtils.PA)
                    configLeakInfoBean.leakageUnit = PSI
                }

                PA -> {
                    configLeakInfoBean.leakageAlarm =
                        Tools.convertToPa(leakageAlarm, leakageUnit ?: ConstantsUtils.PA).toInt().toFloat()
                    configLeakInfoBean.leakageUnit = PA
                }
            }
        }

        return configLeakInfoBean
    }

    override fun toString(): String {
        return "ConfigLeakInfoBean(workpieceNo='$workpieceNo', workpieceVolume=$workpieceVolume, testPressure=$testPressure, upperPressureLimit=$upperPressureLimit, lowerPressureLimit=$lowerPressureLimit, pressureUnit=$pressureUnit, leakageAlarm=$leakageAlarm, leakageUnit=$leakageUnit, inflationTime=$inflationTime, stabilizationTime=$stabilizationTime, detectionTime=$detectionTime, exhaustTime=$exhaustTime, testMode=$testMode)"
    }
}