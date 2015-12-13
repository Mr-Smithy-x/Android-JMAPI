package com.mrsmyx.jmapi.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrsmyx.jmapi.MainActivity;
import com.mrsmyx.jmapi.R;
import com.mrsmyx.jmapi.adapters.SystemAdapter;

/**
 * Created by cj on 10/10/15.
 */
public class SystemFragment extends Fragment {

    public interface OnSystemListener {
        public void onSystemOptionSelected(SystemAdapter.SystemStruct.PS3OPTIONS ps3OPTIONS);
    }

    private OnSystemListener onSystemListener;
    private RecyclerView mRec;
    private SystemAdapter systemAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.system_fragment, container, false);
        mRec = (RecyclerView) view.findViewById(R.id.sys_rec);
        mRec.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        mRec.setAdapter(systemAdapter = new SystemAdapter() {
            @Override
            public void onItemClicked(View view, int position) {
                onSystemListener.onSystemOptionSelected(getItemAtPosition(position).getPs3OP());
            }

            @Override
            public boolean onItemLongClicked(View view, int position) {
                return true;
            }
        });
        systemAdapter.appendItem(SystemAdapter.SystemStruct.Builder().setTitle("Shutdown").setPs3OP(SystemAdapter.SystemStruct.PS3OPTIONS.SHUTDOWN));
        systemAdapter.appendItem(SystemAdapter.SystemStruct.Builder().setTitle("Reboot").setPs3OP(SystemAdapter.SystemStruct.PS3OPTIONS.REBOOT));
        systemAdapter.appendItem(SystemAdapter.SystemStruct.Builder().setTitle("Softboot").setPs3OP(SystemAdapter.SystemStruct.PS3OPTIONS.SOFTBOOT));
        systemAdapter.appendItem(SystemAdapter.SystemStruct.Builder().setTitle("Hardboot").setPs3OP(SystemAdapter.SystemStruct.PS3OPTIONS.HARDBOOT));
        systemAdapter.appendItem(SystemAdapter.SystemStruct.Builder().setTitle("Del. Hist.").setPs3OP(SystemAdapter.SystemStruct.PS3OPTIONS.DELHIST));
        systemAdapter.appendItem(SystemAdapter.SystemStruct.Builder().setTitle("Del. Hist. Inc.Dir").setPs3OP(SystemAdapter.SystemStruct.PS3OPTIONS.DELHISTD));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setRetainInstance(true);
        onSystemListener = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSystemListener = null;
    }
}
