package com.example.socialbike.utilities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.socialbike.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class pictureSheetDialog extends BottomSheetDialogFragment {
        private BottomSheetListener mListener;



        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.activity_profile_bottom_sheet, container, false);
            Button button2 = v.findViewById(R.id.picture_locally);
            Button button3 = v.findViewById(R.id.button_remove_picture);


                button3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onButtonClicked("remove");
                        dismiss();
                    }
                });



            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onButtonClicked("locally");
                    dismiss();
                }
            });


            return v;
        }

        public interface BottomSheetListener {
            void onButtonClicked(String text);
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                mListener = (BottomSheetListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() +  " must implement BottomSheetListener");
            }
        }
}
