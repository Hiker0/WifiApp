package com.allen.mdns.discover;

import android.net.nsd.NsdServiceInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.allen.mdns.R;

import java.util.ArrayList;


public class ServiceAdapter extends BaseAdapter {
    ArrayList<NsdServiceInfo> mServices;
    LayoutInflater mInflater;
    ServiceAdapter(LayoutInflater inflater){
        mInflater = inflater;
        mServices = new ArrayList<NsdServiceInfo>();
    }

    public void refreshData(ArrayList<NsdServiceInfo> services){
        mServices = services;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mServices.size();
    }

    @Override
    public NsdServiceInfo getItem(int position) {
        return mServices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view;
        if(convertView != null){
            view = (View) convertView;
            holder = (ViewHolder) convertView.getTag();
        }else{
            view = mInflater.inflate(R.layout.list_service, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView)view.findViewById(R.id.id_name);
            holder.host = (TextView)view.findViewById(R.id.id_host);
            holder.port = (TextView)view.findViewById(R.id.id_port);
            view.setTag(holder);
        }
        NsdServiceInfo serviceInfo= getItem(position);
        holder.refresh(serviceInfo);
        return view;
    }

    class ViewHolder {
        TextView name;
        TextView host;
        TextView port;

       void refresh(NsdServiceInfo serviceInfo){
           name.setText(serviceInfo.getServiceName());
           host.setText(serviceInfo.getHost().getHostAddress());
           port.setText(serviceInfo.getPort()+"");
       }
    }
}
