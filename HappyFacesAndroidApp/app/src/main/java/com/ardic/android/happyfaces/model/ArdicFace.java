package com.ardic.android.happyfaces.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.ardic.android.happyfaces.R;

public class ArdicFace {

    private String name;
    private String surname;
    private float confidence;
    private String id;
    private String title;
    private Drawable mDrawable;
    private Context mContext;


    public ArdicFace(String title, String id, float confidence, Context mContext) {
        this.title = title;
        this.id = id;
        this.confidence = confidence;
        this.mContext = mContext;
        setResourcesFromTitle(title);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setmDrawable(Drawable mDrawable) {
        this.mDrawable = mDrawable;
    }

    public int getPercentage() {
        return (int) Math.floor(confidence * 100);
    }

    private void setResourcesFromTitle(final String str) {
        switch (str) {
            case "afsincelik":
                mDrawable = mContext.getResources().getDrawable(R.drawable.afsincelik);
                name = "Afşin";
                surname = "Çelik";
                break;
            case "ahmetcakman":
                mDrawable = mContext.getResources().getDrawable(R.drawable.profile_iconmin);
                name = "Ahmet";
                surname = "Çakman";
                break;
            case "alpparkan":
                mDrawable = mContext.getResources().getDrawable(R.drawable.alpparkan);
                name = "Alp";
                surname = "Parkan";
                break;
            case "barisinanc":
                mDrawable = mContext.getResources().getDrawable(R.drawable.barisinanc);
                name = "Barış";
                surname = "İnanç";
                break;
            case "ceyhunerturk":
                mDrawable = mContext.getResources().getDrawable(R.drawable.ceyhunerturk);
                name = "Ceyhun";
                surname = "Ertürk";
                break;

            case "duygukalinyilmaz":
                mDrawable = mContext.getResources().getDrawable(R.drawable.duygukalinyilmaz);
                name = "Duygu";
                surname = "Kalınyılmaz";
                break;

            case "elifcakmak":
                mDrawable = mContext.getResources().getDrawable(R.drawable.elifcakmak);
                name = "Elif";
                surname = "Çakmak";
                break;
            case "ebruzeybek":
                mDrawable = mContext.getResources().getDrawable(R.drawable.ebruzeybek);
                name = "Ebru";
                surname = "Zeybek";
                break;
            case "farshaddelirabdinia":
                mDrawable = mContext.getResources().getDrawable(R.drawable.farshaddelirabdinia);
                name = "Farshad";
                surname = "Delirabdinia";
                break;
            case "goncayaman":
                mDrawable = mContext.getResources().getDrawable(R.drawable.farshaddelirabdinia);
                name = "Gonca";
                surname = "Yaman";
                break;
            case "haluktufekci":
                mDrawable = mContext.getResources().getDrawable(R.drawable.haluktufekci);
                name = "Haluk";
                surname = "Tüfekci";
                break;
            case "hulyakaraerkekkahveci":
                mDrawable = mContext.getResources().getDrawable(R.drawable.hulyakaraerkekkahveci);
                name = "Hülya";
                surname = "Karaerkek Kahveci";
                break;
            case "huseyinbashan":
                mDrawable = mContext.getResources().getDrawable(R.drawable.huseyinbashan);
                name = "Hüseyin";
                surname = "Başhan";
                break;
            case "ibrahimtezcan":
                mDrawable = mContext.getResources().getDrawable(R.drawable.ibrahimtezcan);
                name = "İbrahim";
                surname = "Tezcan";
                break;
            case "leventbabacan":
                mDrawable = mContext.getResources().getDrawable(R.drawable.leventbabacan);
                name = "Levent";
                surname = "Babacan";
                break;
            case "mehmetaksayan":
                mDrawable = mContext.getResources().getDrawable(R.drawable.mehmetaksayan);
                name = "Mehmet";
                surname = "Aksayan";
                break;
            case "mertacel":
                mDrawable = mContext.getResources().getDrawable(R.drawable.mertacel);
                name = "Mert";
                surname = "Acel";
                break;
            case "metinpar":
                mDrawable = mContext.getResources().getDrawable(R.drawable.metinpar);
                name = "Metin";
                surname = "Par";
                break;
            case "oguzcakir":
                mDrawable = mContext.getResources().getDrawable(R.drawable.oguzcakir);
                name = "Oğuz";
                surname = "Çakır";
                break;
            case "ozgurozkok":
                mDrawable = mContext.getResources().getDrawable(R.drawable.ozgurozkok);
                name = "Özgür";
                surname = "Özkök";
                break;
            case "paperguy":
                mDrawable = mContext.getResources().getDrawable(R.drawable.guest_prof);
                name = "Paper";
                surname = "Guy";
                break;
            case "perihanmirkelam":
                mDrawable = mContext.getResources().getDrawable(R.drawable.perihanmirkelam);
                name = "Perihan";
                surname = "Mirkelam";
                break;
            case "samiozdil":
                mDrawable = mContext.getResources().getDrawable(R.drawable.samiozdil);
                name = "Sami";
                surname = "Özdil";
                break;
            case "sinanpayaslioglu":
                mDrawable = mContext.getResources().getDrawable(R.drawable.sinanpayaslioglu);
                name = "Sinan";
                surname = "Payaslıoğlu";
                break;
            case "sonerugraskan":
                mDrawable = mContext.getResources().getDrawable(R.drawable.sonerugraskan);
                name = "Soner";
                surname = "Uğraşkan";
                break;
            case "sukrankomurcu":
                mDrawable = mContext.getResources().getDrawable(R.drawable.sukrankomurcu);
                name = "Şükran";
                surname = "Kömürcü";
                break;
            case "taalaialmasova":
                mDrawable = mContext.getResources().getDrawable(R.drawable.taalaialmasova);
                name = "Taalai";
                surname = "Almasova";
                break;
            case "teddybear":
                mDrawable = mContext.getResources().getDrawable(R.drawable.teddybear);
                name = "Teddy";
                surname = "Bear";
                break;
            case "tunckahveci":
                mDrawable = mContext.getResources().getDrawable(R.drawable.tunckahveci);
                name = "Tunç";
                surname = "Kahveci";
                break;
            case "suleymanfakir":
                mDrawable = mContext.getResources().getDrawable(R.drawable.suleymanfakir);
                name = "Süleyman";
                surname = "Fakir";
                break;
            case "ugurgelisken":
                mDrawable = mContext.getResources().getDrawable(R.drawable.ugurgelisken);
                name = "Uğur";
                surname = "Gelişken";
                break;
            case "yavuzerzurumlu":
                mDrawable = mContext.getResources().getDrawable(R.drawable.yavuzerzurumlu);
                name = "Yavuz";
                surname = "Erzurumlu";
                break;

            case "NONE":
                mDrawable = mContext.getResources().getDrawable(R.drawable.profile_iconmin);
                name = "New";
                surname = "Guest";
                break;

        }
    }
}
