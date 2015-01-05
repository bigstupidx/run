package com.run.record;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.run.R;

public class ImageFragment extends Fragment {
	
	private ImageView imageView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {			
		return inflater.inflate(R.layout.rundetail_right, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		imageView = (ImageView) getActivity().findViewById(R.id.detail_image);
		super.onActivityCreated(savedInstanceState);
	}
	
	public ImageView getImageView() {
		return imageView;
	}
	
}
