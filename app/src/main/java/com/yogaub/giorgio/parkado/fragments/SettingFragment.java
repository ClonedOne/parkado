package com.yogaub.giorgio.parkado.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.yogaub.giorgio.parkado.R;
import com.yogaub.giorgio.parkado.utilties.Constants;


public class SettingFragment extends Fragment {


    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.car_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinner);
        TextView contactNameTV = (TextView) getActivity().findViewById(R.id.settings_sms_contact_name);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PREF_PARKADO, Context.MODE_PRIVATE);
        int carType = sharedPreferences.getInt(Constants.CAR_TYPE, 0);
        String contactName = sharedPreferences.getString(Constants.SMS_NAME, "");

        spinner.setSelection(carType);
        if (!contactName.equals(""))
            contactNameTV.setText(contactName);
    }
}
