package com.allen.mdns.discover;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.allen.mdns.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Author: allen.z
 * Date  : 2017-06-06
 * last modified: 2017-06-06
 */
public class DiscoverFragment extends Fragment implements DiscoverContract.View {
    DiscoverContract.Presenter mPresenter;

    @BindView(R.id.id_list)
    ListView mListView;
    @BindView(R.id.start_discover)
    Button mStartButton;
    @BindView(R.id.stop_discover)
    Button mStopButton;
    @BindView(R.id.id_info)
    TextView mInfoView;

    ServiceAdapter mAdapter;
    private StringBuffer mInfo = null;

    public DiscoverFragment(){
        mInfo = new StringBuffer();
    }

    public static DiscoverFragment newInstance() {
        return new DiscoverFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nsd, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        mListView = (ListView) view.findViewById(R.id.id_list);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mAdapter = new ServiceAdapter(inflater);
        mListView.setAdapter(mAdapter);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.startResearch();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.stopResearch();
            }
        });
        mInfo = new StringBuffer();
    }

    @Override
    public void setPresenter(DiscoverContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void updateInfo(String info) {
        mInfo.append(info);
        mInfo.append("\n");
        updateInfo();
    }


    private void updateInfo(){
        if(this.isResumed()){
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mInfoView.setText(mInfo);
                }
            });
        }

    }

    @Override
    public void clearInfo() {
        mInfo = new StringBuffer();
        mInfoView.setText(mInfo);
    }

    @Override
    public void updateServices() {
        if(this.isResumed()){
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.refreshData(mPresenter.getServices());
                }
            });
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stop();
    }
}
