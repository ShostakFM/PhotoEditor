package com.marinka.photoeditor;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.marinka.photoeditor.interfaces.EditImageFragmentListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditImageFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private EditImageFragmentListener listener;
    SeekBar brightnessSeekBar, contrastSeekBar, saturationSeekBar;

    public EditImageFragment() {
        // Required empty public constructor
    }

    public void setListener(EditImageFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_edit_image, container, false);
        brightnessSeekBar = itemView.findViewById(R.id.seekbar_brightness);
        contrastSeekBar = itemView.findViewById(R.id.seekbar_contrast);
        saturationSeekBar = itemView.findViewById(R.id.seekbar_saturation);

        brightnessSeekBar.setMax(200);
        brightnessSeekBar.setProgress(100);

        contrastSeekBar.setMax(20);
        contrastSeekBar.setProgress(0);

        saturationSeekBar.setMax(30);
        saturationSeekBar.setProgress(10);

        brightnessSeekBar.setOnSeekBarChangeListener(this);
        contrastSeekBar.setOnSeekBarChangeListener(this);
        saturationSeekBar.setOnSeekBarChangeListener(this);

        return itemView;

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (listener != null) {
            if (seekBar.getId() == R.id.seekbar_brightness) {
                listener.onBrightnessChanged(progress-100);
            }
            else if (seekBar.getId() == R.id.seekbar_contrast) {
                progress+=10;
                float value = .10f*progress;
                listener.onContrastChanged(value);
            }
            else if (seekBar.getId() == R.id.seekbar_saturation) {
                float value = .10f*progress;
                listener.onSaturationChanged(value);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null) {
            listener.onEditStarted();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null) {
            listener.onEditComplete();
        }
    }

    public void resetControls() {
        brightnessSeekBar.setProgress(100);
        contrastSeekBar.setProgress(0);
        saturationSeekBar.setProgress(10);
    }
}
