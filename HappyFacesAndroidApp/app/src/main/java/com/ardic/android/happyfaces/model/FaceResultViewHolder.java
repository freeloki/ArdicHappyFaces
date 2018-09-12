package com.ardic.android.happyfaces.model;

import android.graphics.Bitmap;

import java.util.List;

public class FaceResultViewHolder {

    private ArdicFace face;
    private String resultMsg;
    private Bitmap inputPreviewBmp;

    public FaceResultViewHolder(ArdicFace faces, String resultMsg, Bitmap inputPreviewBmp) {
        this.face = faces;
        this.resultMsg = resultMsg;
        this.inputPreviewBmp = inputPreviewBmp;
    }

    public ArdicFace getFace() {
        return face;
    }

    public void setFace(ArdicFace face) {
        this.face = face;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Bitmap getInputPreviewBmp() {
        return inputPreviewBmp;
    }

    public void setInputPreviewBmp(Bitmap inputPreviewBmp) {
        this.inputPreviewBmp = inputPreviewBmp;
    }
}
