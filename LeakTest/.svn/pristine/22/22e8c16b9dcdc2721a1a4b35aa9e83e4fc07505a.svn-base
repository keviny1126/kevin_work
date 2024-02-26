package com.cnlaunch.physics.simulator;

/**
 * 模拟ecu数据处理器
 */
public interface ISimulatorDataProcessor {
    /**
     * 模拟ecu 诊断回复数据，该方法在通讯线程中实现，对于阻塞方法可以不用另外创建线程，
     * 编写测试用例时则需要另外创建线程实现
     * 如诊断一问一答原始数据（数据都为十六进制格式）
     * answer:  55,AA,F0,F8,00,0C,0E,27,01,60,01,FF,00,01,01,00,FF,00,4D
     * request: 55,AA,F8,F0,00,05,0E,67,01,FF,00,9A
     * 方法中体现如下：
     * 传入参数command2701：60，01，FF，00，01，01，00，FF，00
     * 回复数据 FF,00
     * @param command2701 2701请求命令
     * @return
     */
    byte[] getDiagnoseAnswerData(byte[] command2701,int offset,int length);
}
