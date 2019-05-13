package com.report.enst

/**
 * 样式场景配置， 数值由前端制定
 */
enum StyleSceneEnum {
    /** 共用 **/
    CURRENCY(0, "共用"),
    /** APP **/
    APP(1, "手机"),
    /** PC **/
    PC(2, "PC"),
    /** 大屏 **/
    SCREEN(3, "大屏")
    ;
    Integer scene
    String msg
    private StyleSceneEnum(scene, msg) {
        this.scene = scene
        this.msg = msg
    }
}