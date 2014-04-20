package com.mordor.creepme;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class CreepListFragment extends ListFragment {
	private ArrayList<Creep> mCreeps;
	private Boolean byYou;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		byYou = true;

		mCreeps = CreepLab.get(getActivity(), byYou).getCreeps(byYou);

		CreepListAdapter adapter = new CreepListAdapter(getActivity(),
				R.layout.creep_list_element, mCreeps);

		setListAdapter(adapter);
	}
}
