package com.cnlaunch.physics.impl;

import com.cnlaunch.physics.LinkParameters;

/**
 * 用于实现一种辅助通讯连接
 * 应用于下面场景，诊断主通讯使用蓝牙连接
 * 但是可能存在几种辅助通讯连接用于实现诊断的其他功能
 * 但局限于一问一答的诊断模式，命令格式同标准诊断，且数据供诊断服务使用。
 * 一个IAssitsPhysics 类也必须继承IPhysics
 * 辅助设备连接成功与失败不走IPhysics设备广播告知途径，需要另外提供广播或其他方式
 */
public interface IAssitsPhysics {
    /**
     * 获取连接模式，来自于DeviceFactoryManager中定义的连接常量
     * @return
     */
    int getLinkMode();
    /**
     * 获取建立连接所需的必要参数
     * 比如串口一般需要设备文件名
     * @param linkParameters
     */
    void setLinkParameters(LinkParameters linkParameters);

    /**
     * 获取IPhysics对象
     * @return
     */
    IPhysics getPhysics();

    /**
     * 设置匹配器
     */
    void setAssitsPhysicsMatcher(IAssitsPhysicsMatcher assitsPhysicsMatcher);
    /**
     * 获取匹配器
     */
    IAssitsPhysicsMatcher getAssitsPhysicsMatcher();
}
