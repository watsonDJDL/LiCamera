package com.linfeng.licamera.camera.frame;

public enum FrameMode {

    FRAME_9_16(9/16f) {
        @Override
        public FrameMode getNextMode() {
            return FRAME_3_4;
        }
    },

    FRAME_3_4(3/4f) {
        @Override
        public FrameMode getNextMode() {
            return FRAME_1_1;
        }
    },

    FRAME_1_1(1/1f) {
        @Override
        public FrameMode getNextMode() {
            return FRAME_9_16;
        }
    };
    public float value;

    //获取下一个mode，用于点击图标时判断
    public abstract FrameMode getNextMode();

    private FrameMode(float value) {
        this.value = value;
    }
}
