package com.texi;

import android.app.Dialog;
import android.content.Context;
import android.widget.RelativeLayout;

import com.victor.loading.rotate.RotateLoading;

/**
 * Created by techintegrity on 08/10/16.
 */

public class LoaderView extends Dialog {

    private RelativeLayout rl;
    private Context context;
    private RotateLoading rotateloading;

    public LoaderView(Context context) {
        super(context, R.style.Theme_AppCompat_Translucent);
        this.context=context;
        setContentView(R.layout.loader_dialog);
        rl = (RelativeLayout)findViewById(R.id.rlLoaderMainView);
        rotateloading=(RotateLoading)findViewById(R.id.rotateloading);
        rotateloading.start();
        setCancelable(true);

    }

    public RotateLoading loaderObject(){
        return rotateloading;
    }

    public void loaderDismiss(){
        cancel();
        dismiss();
    }

}
