package com.tobiasfried.brewkeeper.constants;

public enum Stage {

    COMPLETE(0),
    PRIMARY(1),
    SECONDARY(2),
    TERTIARY(3);

    private int code;

    Stage(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
