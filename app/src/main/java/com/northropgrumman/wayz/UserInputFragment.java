package com.northropgrumman.wayz;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class UserInputFragment extends Fragment {
    private UserInputFragment.OnUserSubmitInteraction mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_user_input, container, false);

        Button submitButton = rootView.findViewById(R.id.submitButton);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSubmit();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UserInputFragment.OnUserSubmitInteraction) {
            mListener = (UserInputFragment.OnUserSubmitInteraction) context;
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

    public interface OnUserSubmitInteraction {
        void onSubmit();
    }
}
