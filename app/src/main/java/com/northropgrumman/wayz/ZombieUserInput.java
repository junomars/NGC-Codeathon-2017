package com.northropgrumman.wayz;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ZombieUserInput extends Fragment {
    private OnZombieUserSubmitInteraction mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_zombie_user_input, container, false);

        Button submitButton = rootView.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onZombieSubmit();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ZombieUserInput.OnZombieUserSubmitInteraction) {
            mListener = (ZombieUserInput.OnZombieUserSubmitInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserSubmitInteraction");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnZombieUserSubmitInteraction {
        void onZombieSubmit();
    }
}
