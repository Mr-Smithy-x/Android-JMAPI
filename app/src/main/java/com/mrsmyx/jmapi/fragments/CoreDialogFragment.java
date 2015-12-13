package com.mrsmyx.jmapi.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.mrsmyx.jmapi.MainActivity;
import com.mrsmyx.jmapi.R;

/**
 * Created by cj on 10/10/15.
 */
public class CoreDialogFragment extends AppCompatDialogFragment implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.core_idps_set:
                if(mIDPSEdit.length() != 32){
                    mIDPSEdit.setError("IDPS is not 32 bits long", getResources().getDrawable(R.drawable.ic_warning_24dp));
                    return;
                }
                onCoreSetListener.onCoreSet(OnCoreSetListener.CORETYPE.IDPS, mIDPSEdit.getText().toString());
                break;
            case R.id.core_psid_set:
                if(mPSIDEdit.length() != 32){
                    mPSIDEdit.setError("PSID is not 32 bits long",getResources().getDrawable(R.drawable.ic_warning_24dp));
                    return;
                }
                onCoreSetListener.onCoreSet(OnCoreSetListener.CORETYPE.PSID, mPSIDEdit.getText().toString());
                break;
            case R.id.core_close:
                dismiss();
                break;
        }
    }

    public interface OnCoreSetListener{
        public enum CORETYPE{
            IDPS,PSID
        }
        public boolean onCoreSet(CORETYPE coretype, String str);
    }

    private OnCoreSetListener onCoreSetListener;
    private AppCompatButton mIDPSSet,mPSIDSet, mCoreClose;
    private AppCompatEditText mIDPSEdit, mPSIDEdit;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.core_dialog_fragment, container, false);
        mIDPSSet = (AppCompatButton) view.findViewById(R.id.core_idps_set);
        mPSIDSet = (AppCompatButton) view.findViewById(R.id.core_psid_set);
        mCoreClose = (AppCompatButton) view.findViewById(R.id.core_close);
        mIDPSEdit = (AppCompatEditText) view.findViewById(R.id.core_idps_edit);
        mPSIDEdit = (AppCompatEditText) view.findViewById(R.id.core_psid_edit);
        mIDPSSet.setOnClickListener(this);
        mPSIDSet.setOnClickListener(this);
        mCoreClose.setOnClickListener(this);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        d.getWindow().setAttributes(lp);
        d.setTitle("PS3 Core");
        return d;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.onCoreSetListener = (MainActivity)getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.onCoreSetListener = null;
    }
}
